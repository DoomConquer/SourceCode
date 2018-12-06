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

package org.apache.zookeeper.server;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.jute.BinaryInputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.Record;
import org.apache.zookeeper.server.quorum.ProposalStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.Environment;
import org.apache.zookeeper.Version;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.proto.ReplyHeader;
import org.apache.zookeeper.proto.RequestHeader;
import org.apache.zookeeper.proto.WatcherEvent;
import org.apache.zookeeper.server.quorum.Leader;
import org.apache.zookeeper.server.quorum.LeaderZooKeeperServer;
import org.apache.zookeeper.server.quorum.ReadOnlyZooKeeperServer;
import org.apache.zookeeper.server.util.OSMXBean;

/**
 * This class handles communication with clients using NIO. There is one per
 * client, but only one thread doing the communication.
 * 服务端和客户端连接，每个客户端对应一个NIOServerCnxn，只有一个线程处理连接
 */
public class NIOServerCnxn extends ServerCnxn {
    static final Logger LOG = LoggerFactory.getLogger(NIOServerCnxn.class);

    NIOServerCnxnFactory factory;

    final SocketChannel sock; // 监听客户端与服务端数据传输ServerSocketChannel

    protected final SelectionKey sk; // selector上注册channel返回的SelectionKey

    boolean initialized; // 是否已经完成连接

    ByteBuffer lenBuffer = ByteBuffer.allocate(4); // buff数据或四字命令长度（长度占4字节）

    ByteBuffer incomingBuffer = lenBuffer; // 读取数据buff，根据lenBuffer中读取的长度分配

    LinkedBlockingQueue<ByteBuffer> outgoingBuffers = new LinkedBlockingQueue<ByteBuffer>(); // 等待发送队列

    int sessionTimeout; // session超时

    protected final ZooKeeperServer zkServer;

    /**
     * The number of requests that have been submitted but not yet responded to.
     * 提交了但未处理的请求数
     */
    int outstandingRequests;

    /**
     * This is the id that uniquely identifies the session of a client. Once
     * this session is no longer active, the ephemeral nodes will go away.
     * 客户端对应的唯一的sessionId
     */
    long sessionId;

    static long nextSessionId = 1;
    int outstandingLimit = 1; // 客户端最大请求堆积数

    public NIOServerCnxn(ZooKeeperServer zk, SocketChannel sock,
            SelectionKey sk, NIOServerCnxnFactory factory) throws IOException {
        this.zkServer = zk;
        this.sock = sock;
        this.sk = sk;
        this.factory = factory;
        if (this.factory.login != null) { // sasl登录
            this.zooKeeperSaslServer = new ZooKeeperSaslServer(factory.login);
        }
        if (zk != null) { 
            outstandingLimit = zk.getGlobalOutstandingLimit(); // 最大请求堆积数量（zookeeper.globalOutstandingLimit）
        }
        sock.socket().setTcpNoDelay(true); // 设置允许TCP_NODELAY（enable Nagle's algorithm）
        // set socket linger to false, so that socket close does not block
        sock.socket().setSoLinger(false, -1);
        InetAddress addr = ((InetSocketAddress) sock.socket()
                .getRemoteSocketAddress()).getAddress();
        authInfo.add(new Id("ip", addr.getHostAddress())); // 添加认证信息（ip模式）
        sk.interestOps(SelectionKey.OP_READ); // 设置SelectionKey的interest set
    }

    /** Send close connection packet to the client, doIO will eventually
     *  close the underlying machinery (like socket, selectorkey, etc...)
     *  发送关闭连接给客户端
     */
    public void sendCloseSession() {
        sendBuffer(ServerCnxnFactory.closeConn);
    }

    /**
     * send buffer without using the asynchronous
     * calls to selector and then close the socket
     * 同步发送，不放入发送队列
     * @param bb
     */
    void sendBufferSync(ByteBuffer bb) {
       try {
           /* configure socket to be blocking
            * so that we dont have to do write in 
            * a tight while loop
            */
           sock.configureBlocking(true); // 设置socket阻塞
           if (bb != ServerCnxnFactory.closeConn) { // 不是关闭连接
               if (sock.isOpen()) {
                   sock.write(bb); // bb中数据写入SocketChannel中
               }
               packetSent(); // 发送
           } 
       } catch (IOException ie) {
           LOG.error("Error sending data synchronously ", ie);
       }
    }

    // 异步发送
    public void sendBuffer(ByteBuffer bb) {
        try {
            internalSendBuffer(bb);
        } catch(Exception e) {
            LOG.error("Unexpected Exception: ", e);
        }
    }

    /**
     * This method implements the internals of sendBuffer. We
     * have separated it from send buffer to be able to catch
     * exceptions when testing.
     *
     * @param bb Buffer to send.
     */
    protected void internalSendBuffer(ByteBuffer bb) {
        if (bb != ServerCnxnFactory.closeConn) { // 不是关闭连接
            // We check if write interest here because if it is NOT set,
            // nothing is queued, so we can try to send the buffer right
            // away without waking up the selector
            if(sk.isValid() &&
                    ((sk.interestOps() & SelectionKey.OP_WRITE) == 0)) { // 当前没有SelectionKey.OP_WRITE，发送ByteBuffer数据
                try {
                    sock.write(bb);
                } catch (IOException e) {
                    // we are just doing best effort right now
                }
            }
            // if there is nothing left to send, we are done
            if (bb.remaining() == 0) { // 上面sock.write(bb)时读取了所有的数据，发送完成
                packetSent();
                return;
            }
        }

        synchronized(this.factory){
            sk.selector().wakeup(); // 唤醒selector阻塞线程
            if (LOG.isTraceEnabled()) {
                LOG.trace("Add a buffer to outgoingBuffers, sk " + sk
                        + " is valid: " + sk.isValid());
            }
            outgoingBuffers.add(bb); // 添加到待发送队列
            if (sk.isValid()) {
                sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE); // 设置interestOps
            }
        }
    }

    /** Read the request payload (everything following the length prefix) */
    // 读取channel中数据
    private void readPayload() throws IOException, InterruptedException {
        if (incomingBuffer.remaining() != 0) { // have we read length bytes?
            int rc = sock.read(incomingBuffer); // sock is non-blocking, so ok
            if (rc < 0) {
                throw new EndOfStreamException(
                        "Unable to read additional data from client sessionid 0x"
                        + Long.toHexString(sessionId)
                        + ", likely client has closed socket");
            }
        }

        if (incomingBuffer.remaining() == 0) { // have we read length bytes? 读取了完整的incomingBuffer长度字节（position=limit）
            packetReceived(); // 更新接收数据包统计
            // flip方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值。
            incomingBuffer.flip();
            if (!initialized) { // 还未完成连接，读取连接请求信息
                readConnectRequest();
            } else {
                readRequest();
            }
            lenBuffer.clear(); // 清空长度buff，准备下一次写buffer
            incomingBuffer = lenBuffer; // 准备读取下一个请求长度
        }
    }

    /**
     * Only used in order to allow testing
     * 用于测试
     */
    protected boolean isSocketOpen() {
        return sock.isOpen();
    }

    // 获取socket连接地址
    @Override
    public InetAddress getSocketAddress() {
        if (sock == null) {
            return null;
        }

        return sock.socket().getInetAddress();
    }

    /**
     * Handles read/write IO on connection.
     * 处理读写IO
     */
    void doIO(SelectionKey k) throws InterruptedException {
        try {
            if (isSocketOpen() == false) { // channel关闭
                LOG.warn("trying to do i/o on a null socket for session:0x"
                         + Long.toHexString(sessionId));

                return;
            }
            if (k.isReadable()) { // 等价于(k.readyOps() & SelectionKey.OP_READ) != 0
                // 从channel中读取数据到incomingBuffer，默认处理完一次请求，incomingBuffer被赋值为lenBuffer，
                // 所以下面从sock中读取的是长度buff
                int rc = sock.read(incomingBuffer);
                if (rc < 0) {
                    throw new EndOfStreamException(
                            "Unable to read additional data from client sessionid 0x"
                            + Long.toHexString(sessionId)
                            + ", likely client has closed socket");
                }
                if (incomingBuffer.remaining() == 0) { // 读取了完整的lenBuffer数据到incomingBuffer（position=limit）
                    boolean isPayload;
                    if (incomingBuffer == lenBuffer) { // start of next request 读取下一个请求
                        incomingBuffer.flip(); // incomingBuffer转换为读模式
                        isPayload = readLength(k); // 读取数据长度，同时分配空间给incomingBuffer
                        incomingBuffer.clear();
                    } else {
                        // 从sock中读取数据还没有完成，如果一个数据包很大，一次没有发送完成，通过下面readPayload处理，
                        // 直到incomingBuffer.remaining() == 0表示读取了完整的数据
                        // continuation
                        isPayload = true;
                    }
                    if (isPayload) { // not the case for 4letterword
                        readPayload();
                    } else { // 四字命令
                        // four letter words take care
                        // need not do anything else
                        return;
                    }
                }
            }
            if (k.isWritable()) {
                // ZooLog.logTraceMessage(LOG,
                // ZooLog.CLIENT_DATA_PACKET_TRACE_MASK
                // "outgoingBuffers.size() = " +
                // outgoingBuffers.size());
                if (outgoingBuffers.size() > 0) { // 需要发送的ByteBuffer
                    // ZooLog.logTraceMessage(LOG,
                    // ZooLog.CLIENT_DATA_PACKET_TRACE_MASK,
                    // "sk " + k + " is valid: " +
                    // k.isValid());

                    /*
                     * This is going to reset the buffer position to 0 and the
                     * limit to the size of the buffer, so that we can fill it
                     * with data from the non-direct buffers that we need to
                     * send.
                     */
                    ByteBuffer directBuffer = factory.directBuffer; // 发送ByteBuffer
                    directBuffer.clear(); // 重置position=0和limit=capacity

                    for (ByteBuffer b : outgoingBuffers) {
                        if (directBuffer.remaining() < b.remaining()) { // 需要发送的ByteBuffer太大，分割ByteBuffer
                            /*
                             * When we call put later, if the directBuffer is to
                             * small to hold everything, nothing will be copied,
                             * so we've got to slice the buffer if it's too big.
                             */
                            // 缓冲区分片，从position位置开始，capacity为limit - position进行分片（底层数据共享）
                            b = (ByteBuffer) b.slice().limit(
                                    directBuffer.remaining());
                        }
                        /*
                         * put() is going to modify the positions of both
                         * buffers, put we don't want to change the position of
                         * the source buffers (we'll do that after the send, if
                         * needed), so we save and reset the position after the
                         * copy
                         */
                        int p = b.position();
                        directBuffer.put(b); // 写入directBuffer
                        // 恢复缓冲区的position为put之前（put会改变position），用于下面计算bb.remaining()。这里的设置只对b缓冲区中
                        // 数据全部写到directBuffer有用，如果该缓冲区分片了，设置position没有作用
                        b.position(p);
                        if (directBuffer.remaining() == 0) { // directBuffer写满了
                            break;
                        }
                    }
                    /*
                     * Do the flip: limit becomes position, position gets set to
                     * 0. This sets us up for the write.
                     */
                    directBuffer.flip(); // 转为读模式

                    int sent = sock.write(directBuffer); // 读取directBuffer数据写到channel里，发送数据（send为发送数据的长度）
                    ByteBuffer bb;

                    // Remove the buffers that we have sent
                    while (outgoingBuffers.size() > 0) { // 移除已经发送的buff
                        bb = outgoingBuffers.peek();
                        if (bb == ServerCnxnFactory.closeConn) { // 关闭连接ByteBuffer
                            throw new CloseRequestException("close requested");
                        }
                        // 如果当前buff的长度减去已发送的长度 > 0表示该buff被截断发送（bb.remaining算出来的是bb的长度，
                        // 因为上面每次put完后又重置了position）
                        int left = bb.remaining() - sent;
                        if (left > 0) {
                            /*
                             * We only partially sent this buffer, so we update
                             * the position and exit the loop.
                             */
                            bb.position(bb.position() + sent); // 该缓冲区是被截断，设置下次读的起始位置position
                            break; // 跳出循环等待下次发送
                        }
                        packetSent(); // 统计发送的数据包
                        /* We've sent the whole buffer, so drop the buffer */
                        sent -= bb.remaining();
                        outgoingBuffers.remove(); // 如果bb中的数据完整发送，将其移除
                    }
                    // ZooLog.logTraceMessage(LOG,
                    // ZooLog.CLIENT_DATA_PACKET_TRACE_MASK, "after send,
                    // outgoingBuffers.size() = " + outgoingBuffers.size());
                }

                synchronized(this.factory){ // 同步interestOps操作
                    if (outgoingBuffers.size() == 0) {
                        if (!initialized
                                && (sk.interestOps() & SelectionKey.OP_READ) == 0) {
                            throw new CloseRequestException("responded to info probe");
                        }
                        sk.interestOps(sk.interestOps()
                                & (~SelectionKey.OP_WRITE)); // 没有需要发送的数据
                    } else {
                        sk.interestOps(sk.interestOps()
                                | SelectionKey.OP_WRITE); // 设置interest set，接着发送outgoingBuffers中的数据
                    }
                }
            }
        } catch (CancelledKeyException e) {
            LOG.warn("CancelledKeyException causing close of session 0x"
                     + Long.toHexString(sessionId));
            if (LOG.isDebugEnabled()) {
                LOG.debug("CancelledKeyException stack trace", e);
            }
            close(); // 发生异常关闭
        } catch (CloseRequestException e) {
            // expecting close to log session closure
            close();
        } catch (EndOfStreamException e) {
            LOG.warn(e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("EndOfStreamException stack trace", e);
            }
            // expecting close to log session closure
            close();
        } catch (IOException e) {
            LOG.warn("Exception causing close of session 0x"
                     + Long.toHexString(sessionId) + ": " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("IOException stack trace", e);
            }
            close();
        }
    }

    // 读取请求数据并处理
    private void readRequest() throws IOException {
        zkServer.processPacket(this, incomingBuffer);
    }

    // 未处理的请求数自增
    protected void incrOutstandingRequests(RequestHeader h) {
        if (h.getXid() >= 0) {
            synchronized (this) {
                outstandingRequests++;
            }
            synchronized (this.factory) {        
                // check throttling
                if (zkServer.getInProcess() > outstandingLimit) { // 请求堆积达到限制
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Throttling recv " + zkServer.getInProcess());
                    }
                    disableRecv();
                    // following lines should not be needed since we are
                    // already reading
                    // } else {
                    // enableRecv();
                }
            }
        }

    }

    // 不能接收数据
    public void disableRecv() {
        sk.interestOps(sk.interestOps() & (~SelectionKey.OP_READ));
    }

    // 可以接收数据
    public void enableRecv() {
        synchronized (this.factory) {
            sk.selector().wakeup(); // 唤醒等待selector线程
            if (sk.isValid()) {
                int interest = sk.interestOps();
                if ((interest & SelectionKey.OP_READ) == 0) {
                    sk.interestOps(interest | SelectionKey.OP_READ);
                }
            }
        }
    }

    // 读取连接请求
    private void readConnectRequest() throws IOException, InterruptedException {
        if (!isZKServerRunning()) {
            throw new IOException("ZooKeeperServer not running");
        }
        zkServer.processConnectRequest(this, incomingBuffer); // 处理连接请求
        initialized = true; // 完成连接
    }

    /**
     * clean up the socket related to a command and also make sure we flush the
     * data before we do that
     * 清理命令socket，先flush后清理
     * 
     * @param pwriter
     *            the pwriter for a command socket
     */
    private void cleanupWriterSocket(PrintWriter pwriter) {
        try {
            if (pwriter != null) {
                pwriter.flush(); // 刷新
                pwriter.close();
            }
        } catch (Exception e) {
            LOG.info("Error closing PrintWriter ", e);
        } finally {
            try {
                close(); // 关闭命令连接
            } catch (Exception e) {
                LOG.error("Error closing a command socket ", e);
            }
        }
    }

    /**
     * This class wraps the sendBuffer method of NIOServerCnxn. It is
     * responsible for chunking up the response to a client. Rather
     * than cons'ing up a response fully in memory, which may be large
     * for some commands, this class chunks up the result.
     * 包装sendBuffer方法，将大数据包分块
     */
    private class SendBufferWriter extends Writer {
        private StringBuffer sb = new StringBuffer();
        
        /**
         * Check if we are ready to send another chunk.
         * @param force force sending, even if not a full chunk
         */
        private void checkFlush(boolean force) {
            if ((force && sb.length() > 0) || sb.length() > 2048) { // 强制或长度超过2048，发送数据
                sendBufferSync(ByteBuffer.wrap(sb.toString().getBytes()));
                // clear our internal buffer
                sb.setLength(0);
            }
        }

        // 关闭
        @Override
        public void close() throws IOException {
            if (sb == null) return;
            checkFlush(true);
            sb = null; // clear out the ref to ensure no reuse
        }

        // 刷新
        @Override
        public void flush() throws IOException {
            checkFlush(true);
        }

        // 向StringBuffer中写数据
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            sb.append(cbuf, off, len);
            checkFlush(false);
        }
    }

    private static final String ZK_NOT_SERVING =
        "This ZooKeeper instance is not currently serving requests";
    
    /**
     * Set of threads for commmand ports. All the 4
     * letter commands are run via a thread. Each class
     * maps to a corresponding 4 letter command. CommandThread
     * is the abstract class from which all the others inherit.
     * 命令线程（所有四字命令都通过命令线程执行）
     */
    private abstract class CommandThread extends Thread {
        PrintWriter pw;
        
        CommandThread(PrintWriter pw) {
            this.pw = pw;
        }

        // 线程执行命令
        public void run() {
            try {
                commandRun();
            } catch (IOException ie) {
                LOG.error("Error in running command ", ie);
            } finally {
                cleanupWriterSocket(pw);
            }
        }

        // 执行命令
        public abstract void commandRun() throws IOException;
    }

    // 确定服务是否可用，但是该命令直接输出imok，不能用于确定服务是否可用
    private class RuokCommand extends CommandThread {
        public RuokCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            pw.print("imok");
        }
    }

    // 获取traceMask
    private class TraceMaskCommand extends CommandThread {
        TraceMaskCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            long traceMask = ZooTrace.getTextTraceLevel();
            pw.print(traceMask);
        }
    }
    
    private class SetTraceMaskCommand extends CommandThread {
        long trace = 0;
        SetTraceMaskCommand(PrintWriter pw, long trace) {
            super(pw);
            this.trace = trace;
        }
        
        @Override
        public void commandRun() {
            pw.print(trace);
        }
    }
    
    private class EnvCommand extends CommandThread {
        EnvCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            List<Environment.Entry> env = Environment.list();

            pw.println("Environment:");
            for(Environment.Entry e : env) {
                pw.print(e.getKey());
                pw.print("=");
                pw.println(e.getValue());
            }
            
        } 
    }
    
    private class ConfCommand extends CommandThread {
        ConfCommand(PrintWriter pw) {
            super(pw);
        }
            
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                zkServer.dumpConf(pw); // 输出zookeeper服务配置信息
            }
        }
    }

    // 重置state
    private class StatResetCommand extends CommandThread {
        public StatResetCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            }
            else {
                ServerStats serverStats = zkServer.serverStats();
                serverStats.reset();
                if (serverStats.getServerState().equals("leader")) { // 如果是leader
                    ((LeaderZooKeeperServer)zkServer).getLeader().getProposalStats().reset();
                }
                pw.println("Server stats reset.");
            }
        }
    }

    // 重置连接state
    private class CnxnStatResetCommand extends CommandThread {
        public CnxnStatResetCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                synchronized(factory.cnxns){
                    for(ServerCnxn c : factory.cnxns){
                        c.resetStats();
                    }
                }
                pw.println("Connection stats reset.");
            }
        }
    }

    // dump session信息
    private class DumpCommand extends CommandThread {
        public DumpCommand(PrintWriter pw) {
            super(pw);
        }
        
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                pw.println("SessionTracker dump:");
                zkServer.sessionTracker.dumpSessions(pw);
                pw.println("ephemeral nodes dump:");
                zkServer.dumpEphemerals(pw);
            }
        }
    }
    
    private class StatCommand extends CommandThread {
        int len;
        public StatCommand(PrintWriter pw, int len) {
            super(pw);
            this.len = len;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                pw.print("Zookeeper version: ");
                pw.println(Version.getFullVersion());
                if (zkServer instanceof ReadOnlyZooKeeperServer) { // 只读模式
                    pw.println("READ-ONLY mode; serving only " +
                               "read-only clients");
                }
                if (len == statCmd) {
                    LOG.info("Stat command output");
                    pw.println("Clients:");
                    // clone should be faster than iteration
                    // ie give up the cnxns lock faster
                    HashSet<NIOServerCnxn> cnxnset;
                    synchronized(factory.cnxns){
                        cnxnset = (HashSet<NIOServerCnxn>)factory
                        .cnxns.clone();
                    }
                    for(NIOServerCnxn c : cnxnset){ // 与客户端连接信息
                        c.dumpConnectionInfo(pw, true);
                        pw.println();
                    }
                    pw.println();
                }
                ServerStats serverStats = zkServer.serverStats(); // 服务端统计信息
                pw.print(serverStats.toString());
                pw.print("Node count: ");
                pw.println(zkServer.getZKDatabase().getNodeCount());
                if (serverStats.getServerState().equals("leader")) { // leader选举信息
                    Leader leader = ((LeaderZooKeeperServer)zkServer).getLeader();
                    ProposalStats proposalStats = leader.getProposalStats();
                    pw.printf("Proposal sizes last/min/max: %s%n", proposalStats.toString());
                }
            }
            
        }
    }

    // 客户端连接信息
    private class ConsCommand extends CommandThread {
        public ConsCommand(PrintWriter pw) {
            super(pw);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                // clone should be faster than iteration
                // ie give up the cnxns lock faster
                HashSet<NIOServerCnxn> cnxns;
                synchronized (factory.cnxns) {
                    cnxns = (HashSet<NIOServerCnxn>) factory.cnxns.clone();
                }
                for (NIOServerCnxn c : cnxns) {
                    c.dumpConnectionInfo(pw, false);
                    pw.println();
                }
                pw.println();
            }
        }
    }

    // dump监视器信息
    private class WatchCommand extends CommandThread {
        int len = 0;
        public WatchCommand(PrintWriter pw, int len) {
            super(pw);
            this.len = len;
        }

        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
            } else {
                DataTree dt = zkServer.getZKDatabase().getDataTree();
                if (len == wchsCmd) {
                    dt.dumpWatchesSummary(pw);
                } else if (len == wchpCmd) {
                    dt.dumpWatches(pw, true);
                } else {
                    dt.dumpWatches(pw, false);
                }
                pw.println();
            }
        }
    }

    // 监控信息
    private class MonitorCommand extends CommandThread {

        MonitorCommand(PrintWriter pw) {
            super(pw);
        }

        @Override
        public void commandRun() {
            if(!isZKServerRunning()) {
                pw.println(ZK_NOT_SERVING);
                return;
            }
            ZKDatabase zkdb = zkServer.getZKDatabase();
            ServerStats stats = zkServer.serverStats();

            print("version", Version.getFullVersion());

            print("avg_latency", stats.getAvgLatency());
            print("max_latency", stats.getMaxLatency());
            print("min_latency", stats.getMinLatency());

            print("packets_received", stats.getPacketsReceived());
            print("packets_sent", stats.getPacketsSent());
            print("num_alive_connections", stats.getNumAliveClientConnections());

            print("outstanding_requests", stats.getOutstandingRequests());

            print("server_state", stats.getServerState());
            print("znode_count", zkdb.getNodeCount());

            print("watch_count", zkdb.getDataTree().getWatchCount());
            print("ephemerals_count", zkdb.getDataTree().getEphemeralsCount());
            print("approximate_data_size", zkdb.getDataTree().approximateDataSize());

            OSMXBean osMbean = new OSMXBean();
            if (osMbean != null && osMbean.getUnix() == true) {
                print("open_file_descriptor_count", osMbean.getOpenFileDescriptorCount());
                print("max_file_descriptor_count", osMbean.getMaxFileDescriptorCount());
            }

            print("fsync_threshold_exceed_count", stats.getFsyncThresholdExceedCount());

            if(stats.getServerState().equals("leader")) { // 如果是leader
                Leader leader = ((LeaderZooKeeperServer)zkServer).getLeader();

                print("followers", leader.getLearners().size());
                print("synced_followers", leader.getForwardingFollowers().size());
                print("pending_syncs", leader.getNumPendingSyncs());

                print("last_proposal_size", leader.getProposalStats().getLastProposalSize());
                print("max_proposal_size", leader.getProposalStats().getMaxProposalSize());
                print("min_proposal_size", leader.getProposalStats().getMinProposalSize());
            }
        }

        private void print(String key, long number) {
            print(key, "" + number);
        }

        // 格式输出
        private void print(String key, String value) {
            pw.print("zk_");
            pw.print(key);
            pw.print("\t");
            pw.println(value);
        }

    }

    // 是否只读服务
    private class IsroCommand extends CommandThread {

        public IsroCommand(PrintWriter pw) {
            super(pw);
        }

        @Override
        public void commandRun() {
            if (!isZKServerRunning()) {
                pw.print("null");
            } else if (zkServer instanceof ReadOnlyZooKeeperServer) {
                pw.print("ro"); // 只读
            } else {
                pw.print("rw"); // 读写
            }
        }
    }

    // 无效（操作）命令，输出msg
    private class NopCommand extends CommandThread {
        private String msg;

        public NopCommand(PrintWriter pw, String msg) {
            super(pw);
            this.msg = msg;
        }

        @Override
        public void commandRun() {
            pw.println(msg);
        }
    }

    /** Return if four letter word found and responded to, otw false **/
    // 检查是否是四字命令，如果是就使用命令线程处理，否则返回false（注：上面注释otw是otherwise缩写）
    private boolean checkFourLetterWord(final SelectionKey k, final int len)
    throws IOException
    {
        // We take advantage of the limited size of the length to look
        // for cmds. They are all 4-bytes which fits inside of an int
        if (!ServerCnxn.isKnown(len)) { // 是否包含该命令（命令已经变成字节对应的32位int数值）
            return false;
        }

        packetReceived(); // 统计接收数据包

        /** cancel the selection key to remove the socket handling
         * from selector. This is to prevent netcat problem wherein
         * netcat immediately closes the sending side after sending the
         * commands and still keeps the receiving channel open. 
         * The idea is to remove the selectionkey from the selector
         * so that the selector does not notice the closed read on the
         * socket channel and keep the socket alive to write the data to
         * and makes sure to close the socket after its done writing the data
         */
        if (k != null) {
            try {
                k.cancel(); // 防止netcat发送命令后立即关闭会导致接收channel没有关闭
            } catch(Exception e) {
                LOG.error("Error cancelling command selection key ", e);
            }
        }

        final PrintWriter pwriter = new PrintWriter(
                new BufferedWriter(new SendBufferWriter()));

        String cmd = ServerCnxn.getCommandString(len); // 四字命令
        // ZOOKEEPER-2693: don't execute 4lw if it's not enabled.
        if (!ServerCnxn.isEnabled(cmd)) { // 命令没在白名单
            LOG.debug("Command {} is not executed because it is not in the whitelist.", cmd);
            NopCommand nopCmd = new NopCommand(pwriter, cmd + " is not executed because it is not in the whitelist.");
            nopCmd.start(); // 无效命令
            return true;
        }

        LOG.info("Processing " + cmd + " command from "
                + sock.socket().getRemoteSocketAddress());

        if (len == ruokCmd) {
            RuokCommand ruok = new RuokCommand(pwriter);
            ruok.start();
            return true;
        } else if (len == getTraceMaskCmd) {
            TraceMaskCommand tmask = new TraceMaskCommand(pwriter);
            tmask.start();
            return true;
        } else if (len == setTraceMaskCmd) {
            incomingBuffer = ByteBuffer.allocate(8); // 读取四字命令后面的参数，incomingBuffer在整个流程中是串行执行所以不存在竞争
            int rc = sock.read(incomingBuffer);
            if (rc < 0) {
                throw new IOException("Read error");
            }

            incomingBuffer.flip(); // 转换incomingBuffer为读模式
            long traceMask = incomingBuffer.getLong();
            ZooTrace.setTextTraceLevel(traceMask);
            SetTraceMaskCommand setMask = new SetTraceMaskCommand(pwriter, traceMask);
            setMask.start();
            return true;
        } else if (len == enviCmd) {
            EnvCommand env = new EnvCommand(pwriter);
            env.start();
            return true;
        } else if (len == confCmd) {
            ConfCommand ccmd = new ConfCommand(pwriter);
            ccmd.start();
            return true;
        } else if (len == srstCmd) {
            StatResetCommand strst = new StatResetCommand(pwriter);
            strst.start();
            return true;
        } else if (len == crstCmd) {
            CnxnStatResetCommand crst = new CnxnStatResetCommand(pwriter);
            crst.start();
            return true;
        } else if (len == dumpCmd) {
            DumpCommand dump = new DumpCommand(pwriter);
            dump.start();
            return true;
        } else if (len == statCmd || len == srvrCmd) {
            StatCommand stat = new StatCommand(pwriter, len);
            stat.start();
            return true;
        } else if (len == consCmd) {
            ConsCommand cons = new ConsCommand(pwriter);
            cons.start();
            return true;
        } else if (len == wchpCmd || len == wchcCmd || len == wchsCmd) {
            WatchCommand wcmd = new WatchCommand(pwriter, len);
            wcmd.start();
            return true;
        } else if (len == mntrCmd) {
            MonitorCommand mntr = new MonitorCommand(pwriter);
            mntr.start();
            return true;
        } else if (len == isroCmd) {
            IsroCommand isro = new IsroCommand(pwriter);
            isro.start();
            return true;
        }
        return false;
    }

    /** Reads the first 4 bytes of lenBuffer, which could be true length or
     *  four letter word.
     *  读取接收数据包长度或四字命令
     *
     * @param k selection key
     * @return true if length read, otw false (wasn't really the length)
     * @throws IOException if buffer size exceeds maxBuffer size
     */
    private boolean readLength(SelectionKey k) throws IOException {
        // Read the length, now get the buffer
        int len = lenBuffer.getInt(); // 读取数据长度（如果是四字命令该值是命令字节对应的int值）
        if (!initialized && checkFourLetterWord(sk, len)) { // 如果是四字命令进行处理
            return false; // 未完成连接或是四字命令
        }
        if (len < 0 || len > BinaryInputArchive.maxBuffer) { // 小于0或超过jute.maxbuffer，默认0xfffff（1048575）
            throw new IOException("Len error " + len);
        }
        if (!isZKServerRunning()) {
            throw new IOException("ZooKeeperServer not running");
        }
        incomingBuffer = ByteBuffer.allocate(len); // 分配数据buff
        return true;
    }

    // 获取未处理请求数
    public long getOutstandingRequests() {
        synchronized (this) {
            synchronized (this.factory) {
                return outstandingRequests;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.zookeeper.server.ServerCnxnIface#getSessionTimeout()
     */
    public int getSessionTimeout() {
        return sessionTimeout;
    }

    @Override
    public String toString() {
        return "NIOServerCnxn object with sock = " + sock + " and sk = " + sk;
    }

    /*
     * Close the cnxn and remove it from the factory cnxns list.
     * 关闭连接
     * 
     * This function returns immediately if the cnxn is not on the cnxns list.
     */
    @Override
    public void close() {
        factory.removeCnxn(this);

        if (zkServer != null) {
            zkServer.removeCnxn(this);
        }

        closeSock();

        if (sk != null) {
            try {
                // need to cancel this selection key from the selector
                sk.cancel();
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignoring exception during selectionkey cancel", e);
                }
            }
        }
    }

    /**
     * Close resources associated with the sock of this cnxn.
     * 关闭cnxn相关的资源
     */
    private void closeSock() {
        if (sock.isOpen() == false) {
            return;
        }

        LOG.info("Closed socket connection for client "
                + sock.socket().getRemoteSocketAddress()
                + (sessionId != 0 ?
                        " which had sessionid 0x" + Long.toHexString(sessionId) :
                        " (no session established for client)"));
        try {
            /*
             * The following sequence of code is stupid! You would think that
             * only sock.close() is needed, but alas, it doesn't work that way.
             * If you just do sock.close() there are cases where the socket
             * doesn't actually close...
             */
            sock.socket().shutdownOutput();
        } catch (IOException e) {
            // This is a relatively common exception that we can't avoid
            if (LOG.isDebugEnabled()) {
                LOG.debug("ignoring exception during output shutdown", e);
            }
        }
        try {
            sock.socket().shutdownInput();
        } catch (IOException e) {
            // This is a relatively common exception that we can't avoid
            if (LOG.isDebugEnabled()) {
                LOG.debug("ignoring exception during input shutdown", e);
            }
        }
        try {
            sock.socket().close();
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ignoring exception during socket close", e);
            }
        }
        try {
            sock.close();
            // XXX The next line doesn't seem to be needed, but some posts
            // to forums suggest that it is needed. Keep in mind if errors in
            // this section arise.
            // factory.selector.wakeup();
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ignoring exception during socketchannel close", e);
            }
        }
    }

    // 开头长度4字节
    private final static byte fourBytes[] = new byte[4];

    /*
     * (non-Javadoc)
     * 发送响应数据
     * @see org.apache.zookeeper.server.ServerCnxnIface#sendResponse(org.apache.zookeeper.proto.ReplyHeader,
     *      org.apache.jute.Record, java.lang.String)
     */
    @Override
    synchronized public void sendResponse(ReplyHeader h, Record r, String tag) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Make space for length
            BinaryOutputArchive bos = BinaryOutputArchive.getArchive(baos);
            try {
                baos.write(fourBytes); // 开头长度字节
                bos.writeRecord(h, "header");
                if (r != null) {
                    bos.writeRecord(r, tag);
                }
                baos.close();
            } catch (IOException e) {
                LOG.error("Error serializing response");
            }
            byte b[] = baos.toByteArray();
            ByteBuffer bb = ByteBuffer.wrap(b);
            // Buffer.rewind()将position设回0，所以你可以重读Buffer中的所有数据。limit保持不变，仍然表示能从Buffer中读取多少个元素（byte、char等）
            bb.putInt(b.length - 4).rewind(); // 开头写入数据的长度
            sendBuffer(bb);
            if (h.getXid() > 0) {
                synchronized(this){
                    outstandingRequests--; // 未处理请求数减一
                }
                // check throttling
                synchronized (this.factory) { // 允许接收数据
                    if (zkServer.getInProcess() < outstandingLimit
                            || outstandingRequests < 1) {
                        sk.selector().wakeup();
                        enableRecv();
                    }
                }
            }
         } catch(Exception e) {
            LOG.warn("Unexpected exception. Destruction averted.", e);
         }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.zookeeper.server.ServerCnxnIface#process(org.apache.zookeeper.proto.WatcherEvent)
     * 处理Watch事件
     */
    @Override
    synchronized public void process(WatchedEvent event) {
        ReplyHeader h = new ReplyHeader(-1, -1L, 0);
        if (LOG.isTraceEnabled()) {
            ZooTrace.logTraceMessage(LOG, ZooTrace.EVENT_DELIVERY_TRACE_MASK,
                                     "Deliver event " + event + " to 0x"
                                     + Long.toHexString(this.sessionId)
                                     + " through " + this);
        }

        // Convert WatchedEvent to a type that can be sent over the wire
        // 将WatchedEvent转换成WatcherEvent，发送事件
        WatcherEvent e = event.getWrapper();

        sendResponse(h, e, "notification");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.zookeeper.server.ServerCnxnIface#getSessionId()
     */
    @Override
    public long getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
        this.factory.addSession(sessionId, this);
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public int getInterestOps() {
        return sk.isValid() ? sk.interestOps() : 0;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        if (sock.isOpen() == false) {
            return null;
        }
        return (InetSocketAddress) sock.socket().getRemoteSocketAddress();
    }

    // 获取服务端state
    @Override
    protected ServerStats serverStats() {
        if (!isZKServerRunning()) {
            return null;
        }
        return zkServer.serverStats();
    }

    /**
     * @return true if the server is running, false otherwise.
     */
    boolean isZKServerRunning() {
        return zkServer != null && zkServer.isRunning();
    }
}
