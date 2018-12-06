/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.server.quorum;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.zookeeper.server.ZooKeeperThread;
import org.apache.zookeeper.server.quorum.auth.QuorumAuthLearner;
import org.apache.zookeeper.server.quorum.auth.QuorumAuthServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a connection manager for leader election using TCP. It
 * maintains one connection for every pair of servers. The tricky part is to
 * guarantee that there is exactly one connection for every pair of servers that
 * are operating correctly and that can communicate over the network.
 * 该类用于leader选举通信连接，保证服务与其他服务保持连接
 * 
 * If two servers try to start a connection concurrently, then the connection
 * manager uses a very simple tie-breaking（平局决胜） mechanism to decide which connection
 * to drop based on the IP addressed of the two parties. 
 * 
 * For every peer, the manager maintains a queue of messages to send. If the
 * connection to any particular peer drops, then the sender thread puts the
 * message back on the list. As this implementation currently uses a queue
 * implementation to maintain messages to send to another peer, we add the
 * message to the tail of the queue, thus changing the order of messages.
 * Although this is not a problem for the leader election, it could be a problem
 * when consolidating peer communication. This is to be verified, though.
 * 对于每个服务，管理类维护一个发送消息队列，如果和某个服务的连接断开会将对应的消息放到队列的末尾
 */

public class QuorumCnxManager {
    private static final Logger LOG = LoggerFactory.getLogger(QuorumCnxManager.class);

    /*
     * Maximum capacity of thread queues
     * 线程接受队列最大容量
     */
    static final int RECV_CAPACITY = 100;

    // Initialized to 1 to prevent sending stale notifications to peers
    // 发送消息队列容量，设置成1是为了防止发送旧的消息
    static final int SEND_CAPACITY = 1;

    // 接收消息最大长度（512kb）
    static final int PACKETMAXSIZE = 1024 * 512;

    /*
     * Max buffer size to be read from the network.
     * 从网络读取数据最大缓冲大小
     */
    static public final int maxBuffer = 2048;
    
    /*
     * Negative counter for observer server ids.
     * 标识观察者的服务id（负数）
     */
    private AtomicLong observerCounter = new AtomicLong(-1);
    
    /*
     * Connection time out value in milliseconds
     * 连接超时（5秒）
     */
    private int cnxTO = 5000;
    
    /*
     * Local IP address
     */
    final long mySid; // 服务id
    final int socketTimeout; // socket超时
    final Map<Long, QuorumPeer.QuorumServer> view; // 所有集群服务视图（包括观察者）
    final boolean tcpKeepAlive = Boolean.getBoolean("zookeeper.tcpKeepAlive"); // tcpKeepAlive属性
    final boolean listenOnAllIPs; // 是否监听本地服务的所有ip（本地服务可能有多个网卡，提供多个ip）
    private ThreadPoolExecutor connectionExecutor; // 连接线程池
    private final Set<Long> inprogressConnections = Collections.synchronizedSet(new HashSet<Long>()); // 记录正在进行中的连接（异步初始化时防止同时和一个服务建立多个连接）
    private QuorumAuthServer authServer;   // 选举认证服务（该服务）
    private QuorumAuthLearner authLearner; // 选举认证服务（learner）
    private boolean quorumSaslAuthEnabled; // 是否允许sasl认证

    /*
     * Counter to count connection processing threads.
     * 异步连接接收线程计数器
     */
    private AtomicInteger connectionThreadCnt = new AtomicInteger(0);

    /*
     * Mapping from Peer to Thread number
     * 服务id -> 发送线程
     */
    final ConcurrentHashMap<Long, SendWorker> senderWorkerMap;

    // 全局消息发送队列，内容为服务id -> 发送该服务的消息队列
    final ConcurrentHashMap<Long, ArrayBlockingQueue<ByteBuffer>> queueSendMap;
    final ConcurrentHashMap<Long, ByteBuffer> lastMessageSent; // 记录发送给某服务的最后一条消息

    /*
     * Reception queue 消息接收队列
     */
    public final ArrayBlockingQueue<Message> recvQueue;

    /*
     * Object to synchronize access to recvQueue
     * 访问recvQueue的同步锁
     */
    private final Object recvQLock = new Object();

    /*
     * Shutdown flag 监听线程关闭标志
     */
    volatile boolean shutdown = false;

    /*
     * Listener thread 监听端口线程
     */
    public final Listener listener;

    /*
     * Counter to count worker threads
     * 工作线程（worker）计数器
     */
    private AtomicInteger threadCnt = new AtomicInteger(0);

    // 消息类
    static public class Message {
        Message(ByteBuffer buffer, long sid) {
            this.buffer = buffer;
            this.sid = sid;
        }

        ByteBuffer buffer;
        long sid;
    }

    public QuorumCnxManager(final long mySid,
                            Map<Long,QuorumPeer.QuorumServer> view,
                            QuorumAuthServer authServer,
                            QuorumAuthLearner authLearner,
                            int socketTimeout,
                            boolean listenOnAllIPs,
                            int quorumCnxnThreadsSize,
                            boolean quorumSaslAuthEnabled) {
        this(mySid, view, authServer, authLearner, socketTimeout, listenOnAllIPs,
                quorumCnxnThreadsSize, quorumSaslAuthEnabled, new ConcurrentHashMap<Long, SendWorker>());
    }

    // visible for testing 测试使用
    public QuorumCnxManager(final long mySid,
                            Map<Long,QuorumPeer.QuorumServer> view,
                            QuorumAuthServer authServer,
                            QuorumAuthLearner authLearner,
                            int socketTimeout,
                            boolean listenOnAllIPs,
                            int quorumCnxnThreadsSize,
                            boolean quorumSaslAuthEnabled,
                            ConcurrentHashMap<Long, SendWorker> senderWorkerMap) {
        this.senderWorkerMap = senderWorkerMap;

        this.recvQueue = new ArrayBlockingQueue<Message>(RECV_CAPACITY);
        this.queueSendMap = new ConcurrentHashMap<Long, ArrayBlockingQueue<ByteBuffer>>();
        this.lastMessageSent = new ConcurrentHashMap<Long, ByteBuffer>();
        String cnxToValue = System.getProperty("zookeeper.cnxTimeout");
        if(cnxToValue != null){
            this.cnxTO = Integer.parseInt(cnxToValue);
        }

        this.mySid = mySid;
        this.socketTimeout = socketTimeout;
        this.view = view;
        this.listenOnAllIPs = listenOnAllIPs;

        initializeAuth(mySid, authServer, authLearner, quorumCnxnThreadsSize, quorumSaslAuthEnabled);

        // Starts listener thread that waits for connection requests
        // 开启监听线程，等待连接请求
        listener = new Listener();
    }

    // 初始化认证
    private void initializeAuth(final long mySid,
            final QuorumAuthServer authServer,
            final QuorumAuthLearner authLearner,
            final int quorumCnxnThreadsSize,
            final boolean quorumSaslAuthEnabled) {
        this.authServer = authServer;
        this.authLearner = authLearner;
        this.quorumSaslAuthEnabled = quorumSaslAuthEnabled;
        if (!this.quorumSaslAuthEnabled) {
            LOG.debug("Not initializing connection executor as quorum sasl auth is disabled");
            return;
        }

        // init connection executors 初始化连接线程池
        final AtomicInteger threadIndex = new AtomicInteger(1); // 线程序号
        SecurityManager s = System.getSecurityManager();
        final ThreadGroup group = (s != null) ? s.getThreadGroup()
                : Thread.currentThread().getThreadGroup(); // 获取线程组
        ThreadFactory daemonThFactory = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(group, r, "QuorumConnectionThread-"
                        + "[myid=" + mySid + "]-"
                        + threadIndex.getAndIncrement());
                return t;
            }
        }; // 线程工厂
        this.connectionExecutor = new ThreadPoolExecutor(3,
                quorumCnxnThreadsSize, 60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), daemonThFactory);
        this.connectionExecutor.allowCoreThreadTimeOut(true); // 允许core threads线程超时
    }

    /**
     * Invokes initiateConnection for testing purposes
     * 初始化连接（测试使用）
     * 
     * @param sid
     */
    public void testInitiateConnection(long sid) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Opening channel to server " + sid);
        }
        Socket sock = new Socket();
        setSockOpts(sock); // 设置socket属性
        sock.connect(QuorumPeer.viewToVotingView(view).get(sid).electionAddr, cnxTO); // 连接
        initiateConnection(sock, sid);
    }
    
    /**
     * If this server has initiated the connection, then it gives up on the
     * connection if it loses challenge. Otherwise, it keeps the connection.
     * 初始化连接，如果连接丢失challenge（没通过认证会抛出异常），丢弃该连接，否则保持该连接
     */
    public void initiateConnection(final Socket sock, final Long sid) {
        try {
            startConnection(sock, sid); // 开始连接服务sid
        } catch (IOException e) {
            LOG.error("Exception while connecting, id: {}, addr: {}, closing learner connection",
                     new Object[] { sid, sock.getRemoteSocketAddress() }, e);
            closeSocket(sock);
            return;
        }
    }

    /**
     * Server will initiate the connection request to its peer server
     * asynchronously via separate connection thread.
     * 异步初始化连接
     */
    public void initiateConnectionAsync(final Socket sock, final Long sid) {
        if(!inprogressConnections.add(sid)){ // 如果该服务和服务sid正在进行连接，直接返回
            // simply return as there is a connection request to server 'sid' already in progress.
            LOG.debug("Connection request to server id: {} is already in progress, so skipping this request", sid);
            closeSocket(sock);
            return;
        }
        try {
            connectionExecutor.execute(new QuorumConnectionReqThread(sock, sid));
            connectionThreadCnt.incrementAndGet();
        } catch (Throwable e) {
            // Imp: Safer side catching all type of exceptions and remove 'sid'
            // from inprogress connections. This is to avoid blocking further
            // connection requests from this 'sid' in case of errors.
            inprogressConnections.remove(sid); // 异常时移除服务sid
            LOG.error("Exception while submitting quorum connection request", e);
            closeSocket(sock);
        }
    }

    /**
     * Thread to send connection request to peer server.
     * 异步发送连接请求给其他服务线程
     */
    private class QuorumConnectionReqThread extends ZooKeeperThread {
        final Socket sock;
        final Long sid;
        QuorumConnectionReqThread(final Socket sock, final Long sid) {
            super("QuorumConnectionReqThread-" + sid);
            this.sock = sock;
            this.sid = sid;
        }

        // 线程执行初始化连接
        @Override
        public void run() {
            try{
                initiateConnection(sock, sid);
            } finally { // 最后从inprogressConnections中移除服务sid
                inprogressConnections.remove(sid);
            }
        }
    }

    // 开始连接，参数中的sock是该服务作为客户端，主动去连接其他服务
    private boolean startConnection(Socket sock, Long sid) throws IOException {
        DataOutputStream dout = null;
        DataInputStream din = null;
        try {
            // Sending id and challenge
            dout = new DataOutputStream(sock.getOutputStream());
            dout.writeLong(this.mySid); // 发送该服务的id
            dout.flush();

            din = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
        } catch (IOException e) {
            LOG.warn("Ignoring exception reading or writing challenge: ", e);
            closeSocket(sock);
            return false;
        }

        // authenticate learner 进行认证
        authLearner.authenticate(sock, view.get(sid).hostname);

        // If lost the challenge, then drop the new connection
        // 如果该服务的id小于sid服务，丢弃连接（默认让服务id大的服务保持连接）
        if (sid > this.mySid) {
            LOG.info("Have smaller server identifier, so dropping the " +
                     "connection: (" + sid + ", " + this.mySid + ")");
            closeSocket(sock); // 关闭连接
        // Otherwise proceed with the connection
        } else {
            // 实例化发送和接收线程
            SendWorker sw = new SendWorker(sock, sid);
            RecvWorker rw = new RecvWorker(sock, din, sid, sw);
            sw.setRecv(rw);

            SendWorker vsw = senderWorkerMap.get(sid);
            // 完成该服务对应的之前的发送线程
            if(vsw != null)
            vsw.finish();

            // 保存服务及其发送线程
            senderWorkerMap.put(sid, sw);
            queueSendMap.putIfAbsent(sid, new ArrayBlockingQueue<ByteBuffer>(SEND_CAPACITY));

            // 开启发送和接收线程
            sw.start();
            rw.start();
            
            return true;    
        }
        return false;
    }

    /**
     * If this server receives a connection request, then it gives up on the new
     * connection if it wins. Notice that it checks whether it has a connection
     * to this server already or not. If it does, then it sends the smallest
     * possible long value to lose the challenge.
     * 接收连接请求并进行处理，该参数中的sock是该服务作为server端，等待其它服务来连接生成的socket
     */
    public void receiveConnection(final Socket sock) {
        DataInputStream din = null;
        try {
            din = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
            handleConnection(sock, din); // 处理连接
        } catch (IOException e) {
            LOG.error("Exception handling connection, addr: {}, closing server connection", sock.getRemoteSocketAddress());
            closeSocket(sock);
        }
    }

    /**
     * Server receives a connection request and handles it asynchronously via
     * separate thread. 异步接收和处理连接请求
     */
    public void receiveConnectionAsync(final Socket sock) {
        try {
            connectionExecutor.execute(new QuorumConnectionReceiverThread(sock));
            connectionThreadCnt.incrementAndGet();
        } catch (Throwable e) {
            LOG.error("Exception handling connection, addr: {}, closing server connection", sock.getRemoteSocketAddress());
            closeSocket(sock);
        }
    }

    /**
     * Thread to receive connection request from peer server.
     * 异步接收连接请求线程
     */
    private class QuorumConnectionReceiverThread extends ZooKeeperThread {
        private final Socket sock;
        QuorumConnectionReceiverThread(final Socket sock) {
            super("QuorumConnectionReceiverThread-" + sock.getRemoteSocketAddress());
            this.sock = sock;
        }

        // 线程接收连接请求
        @Override
        public void run() {
            receiveConnection(sock);
        }
    }

    // 处理连接请求
    private void handleConnection(Socket sock, DataInputStream din) throws IOException {
        Long sid = null;
        try {
            // Read server id
            sid = din.readLong(); // 读取服务id
            if (sid < 0) { // this is not a server id but a protocol version (see ZOOKEEPER-1633) 不是服务id
                sid = din.readLong();

                // next comes the #bytes in the remainder of the message
                // note that 0 bytes is fine (old servers)
                int num_remaining_bytes = din.readInt(); // 剩余字节数
                if (num_remaining_bytes < 0 || num_remaining_bytes > maxBuffer) { // 数据长度超出范围
                    LOG.error("Unreasonable buffer length: {}", num_remaining_bytes);
                    closeSocket(sock);
                    return;
                }
                byte[] b = new byte[num_remaining_bytes];

                // remove the remainder of the message from din
                int num_read = din.read(b); // 从流中读取剩下的数据
                if (num_read != num_remaining_bytes) {
                    LOG.error("Read only " + num_read + " bytes out of " + num_remaining_bytes + " sent by server " + sid);
                }
            }
            if (sid == QuorumPeer.OBSERVER_ID) { // 如果sid为Long.MAX_VALUE，说明是观察者服务，sid设置为负数
                /*
                 * Choose identifier at random. We need a value to identify
                 * the connection.
                 */
                sid = observerCounter.getAndDecrement(); // 负数
                LOG.info("Setting arbitrary identifier to observer: " + sid);
            }
        } catch (IOException e) {
            closeSocket(sock);
            LOG.warn("Exception reading or writing challenge: " + e.toString());
            return;
        }

        // do authenticating learner 认证
        LOG.debug("Authenticating learner server.id: {}", sid);
        authServer.authenticate(sock, din);

        //If wins the challenge, then close the new connection.
        if (sid < this.mySid) { // 如果该服务的sid更大，关闭接收的连接（其它服务主动来连接该服务的连接），该服务主动去连接sid服务
            /*
             * This replica might still believe that the connection to sid is
             * up, so we have to shut down the workers before trying to open a
             * new connection.
             */
            SendWorker sw = senderWorkerMap.get(sid);
            if (sw != null) { // 关闭发送线程（如果存在）
                sw.finish();
            }

            /*
             * Now we start a new connection
             */
            LOG.debug("Create new connection to server: " + sid);
            closeSocket(sock);
            connectOne(sid); // 主动去连接服务sid（相当于该服务作为客户端）

        // Otherwise start worker threads to receive data.
        } else { // 否则保持该链接，启动发送接收线程
            SendWorker sw = new SendWorker(sock, sid);
            RecvWorker rw = new RecvWorker(sock, din, sid, sw);
            sw.setRecv(rw);

            SendWorker vsw = senderWorkerMap.get(sid);
            if(vsw != null)
                vsw.finish();

            senderWorkerMap.put(sid, sw);
            queueSendMap.putIfAbsent(sid, new ArrayBlockingQueue<ByteBuffer>(SEND_CAPACITY));

            // 开启发送和接收线程
            sw.start();
            rw.start();

            return;
        }
    }

    /**
     * Processes invoke this message to queue a message to send. Currently, 
     * only leader election uses it. 将消息加入sid对应的消息发送队列
     */
    public void toSend(Long sid, ByteBuffer b) {
        // If sending message to myself, then simply enqueue it (loopback).
        if (this.mySid == sid) { // 发送给自己
             b.position(0);
             addToRecvQueue(new Message(b.duplicate(), sid)); // 添加到接收队列
        // Otherwise send to the corresponding thread to send.
        } else {
             /*
              * Start a new connection if doesn't have one already.
              */
             ArrayBlockingQueue<ByteBuffer> bq = new ArrayBlockingQueue<ByteBuffer>(SEND_CAPACITY);
             ArrayBlockingQueue<ByteBuffer> bqExisting = queueSendMap.putIfAbsent(sid, bq); // 添加到发送队列queueSendMap
             if (bqExisting != null) { // 往服务sid对应的发送队列中添加待发送的消息
                 addToSendQueue(bqExisting, b);
             } else {
                 addToSendQueue(bq, b);
             }
             connectOne(sid); // 如果该服务和服务sid连接不存在（可能连接断开了），则建立连接
        }
    }
    
    /**
     * Try to establish a connection to server with id sid.
     * 尝试和sid服务建立连接
     * 
     *  @param sid  server id
     */
    synchronized public void connectOne(long sid){
        if (!connectedToPeer(sid)){ // 该服务没有和服务sid建立连接，则进行连接
            InetSocketAddress electionAddr; // 服务sid的选举连接地址
            if (view.containsKey(sid)) {
                electionAddr = view.get(sid).electionAddr;
            } else {
                LOG.warn("Invalid server id: " + sid);
                return;
            }
            try {
                LOG.debug("Opening channel to server " + sid);
                Socket sock = new Socket();
                setSockOpts(sock); // 设置socket属性
                sock.connect(view.get(sid).electionAddr, cnxTO); // 连接sid服务（该服务作为客户端client）
                LOG.debug("Connected to server " + sid);

                // Sends connection request asynchronously if the quorum
                // sasl authentication is enabled. This is required because
                // sasl server authentication process may take few seconds to
                // finish, this may delay next peer connection requests.
                if (quorumSaslAuthEnabled) { // 如果需要进行sasl认证。使用异步方式初始化连接，防止和其他服务连接延时
                    initiateConnectionAsync(sock, sid);
                } else {
                    initiateConnection(sock, sid);
                }
            } catch (UnresolvedAddressException e) {
                // Sun doesn't include the address that causes this
                // exception to be thrown, also UAE cannot be wrapped cleanly
                // so we log the exception in order to capture this critical
                // detail.
                LOG.warn("Cannot open channel to " + sid + " at election address " + electionAddr, e);
                // Resolve hostname for this server in case the
                // underlying ip address has changed.
                if (view.containsKey(sid)) {
                    view.get(sid).recreateSocketAddresses(); // 重新解析服务的hostname
                }
                throw e;
            } catch (IOException e) {
                LOG.warn("Cannot open channel to " + sid + " at election address " + electionAddr, e);
                // We can't really tell if the server is actually down or it failed
                // to connect to the server because the underlying IP address
                // changed. Resolve the hostname again just in case.
                if (view.containsKey(sid)) {
                    view.get(sid).recreateSocketAddresses(); // 重新解析服务的hostname
                }
            }
        } else { // 已经连接服务sid
            LOG.debug("There is a connection already for server " + sid);
        }
    }
    
    /**
     * Try to establish a connection with each server if one
     * doesn't exist. 连接queueSendMap中所有服务
     */
    public void connectAll(){
        long sid;
        for(Enumeration<Long> en = queueSendMap.keys(); en.hasMoreElements();){
            sid = en.nextElement();
            connectOne(sid);
        }      
    }

    /**
     * Check if all queues are empty, indicating that all messages have been delivered.
     * 该服务发送给所有其他服务的消息是否有发送完成的
     */
    boolean haveDelivered() {
        for (ArrayBlockingQueue<ByteBuffer> queue : queueSendMap.values()) {
            LOG.debug("Queue size: " + queue.size());
            if (queue.size() == 0) {
                return true; // 已经有一个完成
            }
        }
        return false; // 都没完成
    }

    /**
     * Flag that it is time to wrap up all activities and interrupt the listener.
     * 关闭监听线程，关闭资源
     */
    public void halt() {
        shutdown = true;
        LOG.debug("Halting listener");
        listener.halt();
        
        softHalt(); // 停止发送线程

        // clear data structures used for auth
        if (connectionExecutor != null) {
            connectionExecutor.shutdown();
        }
        inprogressConnections.clear();
        resetConnectionThreadCount();
    }
   
    /**
     * A soft halt simply finishes workers. 停止发送线程
     */
    public void softHalt() {
        for (SendWorker sw : senderWorkerMap.values()) {
            LOG.debug("Halting sender: " + sw);
            sw.finish();
        }
    }

    /**
     * Helper method to set socket options. 设置socket属性
     * 
     * @param sock
     *            Reference to socket
     */
    private void setSockOpts(Socket sock) throws SocketException {
        sock.setTcpNoDelay(true);
        sock.setKeepAlive(tcpKeepAlive);
        sock.setSoTimeout(socketTimeout);
    }

    /**
     * Helper method to close a socket. 关闭socket
     * 
     * @param sock
     *            Reference to socket
     */
    private void closeSocket(Socket sock) {
        try {
            sock.close();
        } catch (IOException ie) {
            LOG.error("Exception while closing", ie);
        }
    }

    /**
     * Return number of worker threads
     * 获取工作线程数
     */
    public long getThreadCount() {
        return threadCnt.get();
    }

    /**
     * Return number of connection processing threads.
     * 获取异步连接接收线程数
     */
    public long getConnectionThreadCount() {
        return connectionThreadCnt.get();
    }

    /**
     * Reset the value of connection processing threads count to zero.
     * 重置异步连接接收线程数
     */
    private void resetConnectionThreadCount() {
        connectionThreadCnt.set(0);
    }

    /**
     * Thread to listen on some port 监听端口接收请求消息线程
     */
    public class Listener extends ZooKeeperThread {

        volatile ServerSocket ss = null; // ServerSocket服务

        public Listener() {
            // During startup of thread, thread name will be overridden to
            // specific election address
            super("ListenerThread");
        }

        /**
         * Sleeps on accept(). 等待连接
         */
        @Override
        public void run() {
            int numRetries = 0; // 失败重试次数
            InetSocketAddress addr;
            while((!shutdown) && (numRetries < 3)){
                try {
                    ss = new ServerSocket();
                    ss.setReuseAddress(true);
                    if (listenOnAllIPs) { // 是否监听本地服务的所有ip（本地服务可能有多个网卡，提供多个ip）
                        int port = view.get(QuorumCnxManager.this.mySid).electionAddr.getPort(); // 该服务的选举端口
                        addr = new InetSocketAddress(port);
                    } else {
                        addr = view.get(QuorumCnxManager.this.mySid).electionAddr;
                    }
                    LOG.info("My election bind port: " + addr.toString());
                    setName(view.get(QuorumCnxManager.this.mySid).electionAddr.toString()); // 设置线程名称
                    ss.bind(addr); // 绑定选举地址，等待其他服务来连接
                    while (!shutdown) {
                        Socket client = ss.accept(); // 阻塞直到有连接请求
                        setSockOpts(client);
                        LOG.info("Received connection request " + client.getRemoteSocketAddress());

                        // Receive and handle the connection request
                        // asynchronously if the quorum sasl authentication is
                        // enabled. This is required because sasl server
                        // authentication process may take few seconds to finish,
                        // this may delay next peer connection requests.
                        if (quorumSaslAuthEnabled) { // 需要sasl认证，采用异步方式接收请求并处理
                            receiveConnectionAsync(client);
                        } else {
                            receiveConnection(client);
                        }

                        numRetries = 0;
                    }
                } catch (IOException e) {
                    LOG.error("Exception while listening", e);
                    numRetries++;
                    try {
                        ss.close(); // 异常关闭ServerSocket，等待重试（numRetries不超过3次）
                        Thread.sleep(1000);
                    } catch (IOException ie) {
                        LOG.error("Error closing server socket", ie);
                    } catch (InterruptedException ie) {
                        LOG.error("Interrupted while sleeping. " + "Ignoring exception", ie);
                    }
                }
            }
            LOG.info("Leaving listener");
            if (!shutdown) { // 监听线程退出，该服务将不再参加leader选举
                LOG.error("As I'm leaving the listener thread, "
                        + "I won't be able to participate in leader "
                        + "election any longer: "
                        + view.get(QuorumCnxManager.this.mySid).electionAddr);
            }
        }
        
        /**
         * Halts this listener thread. 停止监听线程
         */
        void halt(){
            try{
                LOG.debug("Trying to close listener: " + ss);
                if(ss != null) {
                    LOG.debug("Closing listener: " + QuorumCnxManager.this.mySid);
                    ss.close(); // 关闭ServerSocket服务
                }
            } catch (IOException e){
                LOG.warn("Exception when shutting down listener: " + e);
            }
        }
    }

    /**
     * Thread to send messages. Instance waits on a queue, and send a message as
     * soon as there is one available. If connection breaks, then opens a new
     * one. 发送消息线程，一旦发送队列有消息就及时发送，如果连接断开会重新开启一个新的连接
     */
    class SendWorker extends ZooKeeperThread {
        Long sid; // 远程服务id
        Socket sock; // 远程服务socket
        RecvWorker recvWorker; // 接收消息线程
        volatile boolean running = true; // 发送消息线程运行标志
        DataOutputStream dout; // socket输出流

        /**
         * An instance of this thread receives messages to send
         * through a queue and sends them to the server sid.
         * 
         * @param sock
         *            Socket to remote peer
         * @param sid
         *            Server identifier of remote peer
         */
        SendWorker(Socket sock, Long sid) {
            super("SendWorker:" + sid);
            this.sid = sid;
            this.sock = sock;
            recvWorker = null;
            try {
                dout = new DataOutputStream(sock.getOutputStream());
            } catch (IOException e) { // 访问socket输出流异常
                LOG.error("Unable to access socket output stream", e);
                closeSocket(sock);
                running = false; // 关闭线程
            }
            LOG.debug("Address of remote peer: " + this.sid);
        }

        // 设置接收消息线程
        synchronized void setRecv(RecvWorker recvWorker) {
            this.recvWorker = recvWorker;
        }

        /**
         * Returns RecvWorker that pairs up with this SendWorker.
         * 
         * @return RecvWorker 
         */
        synchronized RecvWorker getRecvWorker(){
            return recvWorker;
        }

        // 关闭发送线程
        synchronized boolean finish() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Calling finish for " + sid);
            }

            // 防止多次执行finish()方法
            if(!running){
                /*
                 * Avoids running finish() twice. 
                 */
                return running;
            }
            
            running = false;
            closeSocket(sock);
            // channel = null;

            this.interrupt(); // 中断该线程
            if (recvWorker != null) {
                recvWorker.finish();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing entry from senderWorkerMap sid=" + sid);
            }
            senderWorkerMap.remove(sid, this); // 移除发送线程
            threadCnt.decrementAndGet(); // 线程数减少
            return running;
        }

        // 发送消息
        synchronized void send(ByteBuffer b) throws IOException {
            byte[] msgBytes = new byte[b.capacity()];
            try {
                b.position(0);
                b.get(msgBytes); // 从b中读取数据到msgBytes
            } catch (BufferUnderflowException be) {
                LOG.error("BufferUnderflowException ", be);
                return;
            }
            dout.writeInt(b.capacity());
            dout.write(b.array());
            dout.flush();
        }

        @Override
        public void run() {
            threadCnt.incrementAndGet(); // 线程数增加
            try {
                /**
                 * If there is nothing in the queue to send, then we
                 * send the lastMessage to ensure that the last message
                 * was received by the peer. The message could be dropped
                 * in case self or the peer shutdown their connection
                 * (and exit the thread) prior to reading/processing
                 * the last message. Duplicate messages are handled correctly
                 * by the peer.
                 *
                 * If the send queue is non-empty, then we have a recent
                 * message than that stored in lastMessage. To avoid sending
                 * stale message, we should send the message in the send queue.
                 */
                ArrayBlockingQueue<ByteBuffer> bq = queueSendMap.get(sid); // 发送给sid服务的发送队列
                if (bq == null || isSendQueueEmpty(bq)) { // 阻塞队列为空
                   ByteBuffer b = lastMessageSent.get(sid); // 取出最后一个发送消息
                   if (b != null) {
                       LOG.debug("Attempting to send lastMessage to sid=" + sid);
                       send(b); // 发送
                   }
                }
            } catch (IOException e) {
                LOG.error("Failed to send last message. Shutting down thread.", e);
                this.finish(); // 停止
            }
            
            try {
                while (running && !shutdown && sock != null) {
                    ByteBuffer b = null;
                    try {
                        ArrayBlockingQueue<ByteBuffer> bq = queueSendMap.get(sid);
                        if (bq != null) {
                            b = pollSendQueue(bq, 1000, TimeUnit.MILLISECONDS); // 取出发送消息（等待1秒）
                        } else { // 没有发送到sid服务的消息队列
                            LOG.error("No queue of incoming messages for " + "server " + sid);
                            break;
                        }

                        if(b != null){
                            lastMessageSent.put(sid, b); // 记录最后一条消息
                            send(b); // 发送
                        }
                    } catch (InterruptedException e) {
                        LOG.warn("Interrupted while waiting for message on queue", e);
                    }
                }
            } catch (Exception e) {
                LOG.warn("Exception when using channel: for id " + sid
                         + " my id = " + QuorumCnxManager.this.mySid
                         + " error = " + e);
            }
            this.finish(); // 结束
            LOG.warn("Send worker leaving thread");
        }
    }

    /**
     * Thread to receive messages. Instance waits on a socket read. If the
     * channel breaks, then removes itself from the pool of receivers.
     * 接收消息线程
     */
    class RecvWorker extends ZooKeeperThread {
        Long sid;    // 远程服务id
        Socket sock; // 和远程服务socket连接
        volatile boolean running = true; // 接收线程运行标志
        final DataInputStream din; // socket输入流
        final SendWorker sw; // 发送线程

        RecvWorker(Socket sock, DataInputStream din, Long sid, SendWorker sw) {
            super("RecvWorker:" + sid);
            this.sid = sid;
            this.sock = sock;
            this.sw = sw;
            this.din = din;
            try {
                // OK to wait until socket disconnects while reading.
                sock.setSoTimeout(0); // 0表示read时如果没有数据会一直阻塞直到连接断开
            } catch (IOException e) { // 异常关闭
                LOG.error("Error while accessing socket for " + sid, e);
                closeSocket(sock);
                running = false;
            }
        }
        
        /**
         * Shuts down this worker 关闭接收线程
         * 
         * @return boolean  Value of variable running
         */
        synchronized boolean finish() {
            if(!running){
                /*
                 * Avoids running finish() twice. 
                 */
                return running;
            }
            running = false;            

            this.interrupt(); // 中断该线程
            threadCnt.decrementAndGet(); // 线程数减少
            return running;
        }

        @Override
        public void run() {
            threadCnt.incrementAndGet();
            try {
                while (running && !shutdown && sock != null) {
                    /**
                     * Reads the first int to determine the length of the message
                     * 消息首个int表示消息的长度
                     */
                    int length = din.readInt(); // 消息长度
                    if (length <= 0 || length > PACKETMAXSIZE) { // 长度非法
                        throw new IOException("Received packet with invalid packet: " + length);
                    }
                    /**
                     * Allocates a new ByteBuffer to receive the message
                     */
                    byte[] msgArray = new byte[length];
                    din.readFully(msgArray, 0, length); // 从流中读取消息
                    ByteBuffer message = ByteBuffer.wrap(msgArray);
                    addToRecvQueue(new Message(message.duplicate(), sid)); // 加入接收消息队列
                }
            } catch (Exception e) {
                LOG.warn("Connection broken for id " + sid + ", my id = "
                         + QuorumCnxManager.this.mySid + ", error = " , e);
            } finally { // 关闭发送线程
                LOG.warn("Interrupting SendWorker");
                sw.finish();
                if (sock != null) {
                    closeSocket(sock);
                }
            }
        }
    }

    /**
     * Inserts an element in the specified queue. If the Queue is full, this
     * method removes an element from the head of the Queue and then inserts
     * the element at the tail. It can happen that the an element is removed
     * by another thread in {@see SendWorker#processMessage() processMessage}
     * method before this method attempts to remove an element from the queue.
     * This will cause {@link ArrayBlockingQueue#remove() remove} to throw an
     * exception, which is safe to ignore.
     *
     * Unlike {@link #addToRecvQueue(Message) addToRecvQueue} this method does
     * not need to be synchronized since there is only one thread that inserts
     * an element in the queue and another thread that reads from the queue.
     * 添加消息到发送队列，如果消息队列满了移除队头的消息，该接口只有一个线程操作所以不用同步
     *
     * @param queue
     *          Reference to the Queue
     * @param buffer
     *          Reference to the buffer to be inserted in the queue
     */
    private void addToSendQueue(ArrayBlockingQueue<ByteBuffer> queue,
          ByteBuffer buffer) {
        if (queue.remainingCapacity() == 0) { // 队列满了，没有剩余空间
            try {
                queue.remove(); // 移除队头元素
            } catch (NoSuchElementException ne) { // 如果该元素已经被移除了（发送消息线程），不做其他处理
                // element could be removed by poll()
                LOG.debug("Trying to remove from an empty " + "Queue. Ignoring exception " + ne);
            }
        }
        try {
            queue.add(buffer); // 待发送消息加入队尾
        } catch (IllegalStateException ie) {
            // This should never happen
            LOG.error("Unable to insert an element in the queue " + ie);
        }
    }

    /**
     * Returns true if queue is empty. 判断队列是否为空
     * @param queue
     *          Reference to the queue
     * @return
     *      true if the specified queue is empty
     */
    private boolean isSendQueueEmpty(ArrayBlockingQueue<ByteBuffer> queue) {
        return queue.isEmpty();
    }

    /**
     * Retrieves and removes buffer at the head of this queue,
     * waiting up to the specified wait time if necessary for an element to
     * become available. 从队头取出并移除消息
     *
     * {@link ArrayBlockingQueue#poll(long, java.util.concurrent.TimeUnit)}
     */
    private ByteBuffer pollSendQueue(ArrayBlockingQueue<ByteBuffer> queue,
          long timeout, TimeUnit unit) throws InterruptedException {
       return queue.poll(timeout, unit);
    }

    /**
     * Inserts an element in the {@link #recvQueue}. If the Queue is full, this
     * methods removes an element from the head of the Queue and then inserts
     * the element at the tail of the queue.
     * 添加接收到的消息到接收队列
     *
     * This method is synchronized to achieve fairness between two threads that
     * are trying to insert an element in the queue. Each thread checks if the
     * queue is full, then removes the element at the head of the queue, and
     * then inserts an element at the tail. This three-step process is done to
     * prevent a thread from blocking while inserting an element in the queue.
     * If we do not synchronize the call to this method, then a thread can grab
     * a slot in the queue created by the second thread. This can cause the call
     * to insert by the second thread to fail.
     * Note that synchronizing this method does not block another thread
     * from polling the queue since that synchronization is provided by the
     * queue itself. 同步不会阻塞其他线程poll，只会阻塞添加消息到接收队列的线程（因为锁住的是recvQLock）
     *
     * @param msg
     *          Reference to the message to be inserted in the queue
     */
    public void addToRecvQueue(Message msg) {
        synchronized(recvQLock) {
            if (recvQueue.remainingCapacity() == 0) { // 队列满了
                try {
                    recvQueue.remove(); // 移除队头元素
                } catch (NoSuchElementException ne) {
                    // element could be removed by poll() 元素可能被poll()方法移除了，所以异常不做处理
                     LOG.debug("Trying to remove from an empty " + "recvQueue. Ignoring exception " + ne);
                }
            }
            try {
                recvQueue.add(msg); // 队尾添加消息
            } catch (IllegalStateException ie) {
                // This should never happen
                LOG.error("Unable to insert element in the recvQueue " + ie);
            }
        }
    }

    /**
     * Retrieves and removes a message at the head of this queue,
     * waiting up to the specified wait time if necessary for an element to
     * become available. 从接收队列取出消息并移除
     *
     * {@link ArrayBlockingQueue#poll(long, java.util.concurrent.TimeUnit)}
     */
    public Message pollRecvQueue(long timeout, TimeUnit unit) throws InterruptedException {
       return recvQueue.poll(timeout, unit);
    }

    // 该服务是否连接了服务peerSid
    public boolean connectedToPeer(long peerSid) {
        return senderWorkerMap.get(peerSid) != null;
    }
}
