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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.security.sasl.SaslException;

import org.apache.zookeeper.common.AtomicFileOutputStream;
import org.apache.zookeeper.jmx.MBeanRegistry;
import org.apache.zookeeper.jmx.ZKMBeanInfo;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.ZooKeeperThread;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.auth.QuorumAuth;
import org.apache.zookeeper.server.quorum.auth.QuorumAuthLearner;
import org.apache.zookeeper.server.quorum.auth.QuorumAuthServer;
import org.apache.zookeeper.server.quorum.auth.SaslQuorumAuthLearner;
import org.apache.zookeeper.server.quorum.auth.SaslQuorumAuthServer;
import org.apache.zookeeper.server.quorum.auth.NullQuorumAuthLearner;
import org.apache.zookeeper.server.quorum.auth.NullQuorumAuthServer;
import org.apache.zookeeper.server.quorum.flexible.QuorumMaj;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;
import org.apache.zookeeper.server.util.ZxidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the quorum protocol（选举）. There are three states this server
 * can be in:
 * <ol>
 * <li>Leader election - each server will elect a leader (proposing itself as a
 * leader initially).</li>
 * <li>Follower - the server will synchronize with the leader and replicate any
 * transactions.</li>
 * <li>Leader - the server will process requests and forward them to followers.
 * A majority of followers must log the request before it can be accepted.
 * </ol>
 *
 * This class will setup a datagram socket that will always respond with its
 * view of the current leader. The response will take the form of:
 *
 * <pre>
 * int xid; 数据包id
 *
 * long myid; 服务序号id
 *
 * long leader_id; leader id
 *
 * long leader_zxid; leader事务id
 * </pre>
 *
 * The request for the current leader will consist solely of an xid: int xid;
 */
public class QuorumPeer extends ZooKeeperThread implements QuorumStats.Provider {
    private static final Logger LOG = LoggerFactory.getLogger(QuorumPeer.class);

    QuorumBean jmxQuorumBean; // zookeeper集群节点MBean
    LocalPeerBean jmxLocalPeerBean; // 本地peer的MBean
    LeaderElectionBean jmxLeaderElectionBean; // leader选举MBean
    QuorumCnxManager qcm; // 选举通信连接
    QuorumAuthServer authServer;
    QuorumAuthLearner authLearner;
    // VisibleForTesting. This flag is used to know whether qLearner's and
    // qServer's login context has been initialized as ApacheDS has concurrency
    // issues. Refer https://issues.apache.org/jira/browse/ZOOKEEPER-2712
    private boolean authInitialized = false; // login context是否被初始化，测试使用

    /* ZKDatabase is a top level member of quorumpeer 
     * which will be used in all the zookeeperservers
     * instantiated later. Also, it is created once on 
     * bootup and only thrown away in case of a truncate
     * message from the leader
     */
    private ZKDatabase zkDb; // 内存数据库

    // 选举服务，保存选举服务基本信息（id，服务地址，选举通信地址）
    public static class QuorumServer {
        private QuorumServer(long id, InetSocketAddress addr, InetSocketAddress electionAddr) {
            this.id = id;
            this.addr = addr;
            this.electionAddr = electionAddr; // 选举通信地址
        }

        // VisibleForTesting
        public QuorumServer(long id, InetSocketAddress addr) {
            this.id = id;
            this.addr = addr;
            this.electionAddr = null;
        }
        
        private QuorumServer(long id, InetSocketAddress addr, InetSocketAddress electionAddr, LearnerType type) {
            this.id = id;
            this.addr = addr;
            this.electionAddr = electionAddr;
            this.type = type;
        }
        
        public QuorumServer(long id, String hostname,
                            Integer port, Integer electionPort,
                            LearnerType type) {
            this.id = id;
            this.hostname = hostname;
            if (port != null){
                this.port = port;
            }
            if (electionPort != null){
                this.electionPort = electionPort;
            }
            if (type != null){
                    this.type = type;
            }
            this.recreateSocketAddresses();
	    }

        /**
         * Performs a DNS lookup of hostname and (re)creates the this.addr and
         * this.electionAddr InetSocketAddress objects as appropriate
         *
         * If the DNS lookup fails, this.addr and electionAddr remain
         * unmodified, unless they were never set. If this.addr is null, then
         * it is set with an unresolved InetSocketAddress object. this.electionAddr
         * is handled similarly.
         * 根据hostname解析成InetAddress
         */
        public void recreateSocketAddresses() {
            InetAddress address = null; // 解析后的host对应ip地址（如果host对应很多ip，返回一个可达的）
            try {
                // the time, in milliseconds, before {@link InetAddress#isReachable} aborts
                // in {@link #getReachableAddress}.
                int ipReachableTimeout = 0; // 调用isReachable超时时间
                String ipReachableValue = System.getProperty("zookeeper.ipReachableTimeout");
                if (ipReachableValue != null) {
                    try {
                        ipReachableTimeout = Integer.parseInt(ipReachableValue);
                    } catch (NumberFormatException e) {
                        LOG.error("{} is not a valid number", ipReachableValue);
                    }
                }
                // zookeeper.ipReachableTimeout is not defined
                if (ipReachableTimeout <= 0) {
                    address = InetAddress.getByName(this.hostname);
                } else {
                    address = getReachableAddress(this.hostname, ipReachableTimeout);
                }
                LOG.info("Resolved hostname: {} to address: {}", this.hostname, address);
                this.addr = new InetSocketAddress(address, this.port);
                if (this.electionPort > 0){
                    this.electionAddr = new InetSocketAddress(address, this.electionPort);
                }
            } catch (UnknownHostException ex) {
                LOG.warn("Failed to resolve address: {}", this.hostname, ex);
                // Have we succeeded in the past?
                if (this.addr != null) {
                    // Yes, previously the lookup succeeded. Leave things as they are
                    return;
                }
                // The hostname has never resolved. Create our InetSocketAddress(es) as unresolved
                this.addr = InetSocketAddress.createUnresolved(this.hostname, this.port);
                if (this.electionPort > 0){
                    this.electionAddr = InetSocketAddress.createUnresolved(this.hostname, this.electionPort);
                }
            }
        }

        /**
         * Resolve the hostname to IP addresses, and find one reachable address.
         * 解析hostname成ip地址（找到一个可达的就返回）
         *
         * @param hostname the name of the host
         * @param timeout the time, in milliseconds, before {@link InetAddress#isReachable}
         *                aborts
         * @return a reachable IP address. If no such IP address can be found,
         *         just return the first IP address of the hostname.
         *
         * @exception UnknownHostException
         */
        public InetAddress getReachableAddress(String hostname, int timeout) throws UnknownHostException {
            InetAddress[] addresses = InetAddress.getAllByName(hostname); // 获取host对应的ip
            for (InetAddress a : addresses) {
                try {
                    if (a.isReachable(timeout)) { // timeout时间内可达，直接返回
                        return a;
                    } 
                } catch (IOException e) {
                    LOG.warn("IP address {} is unreachable", a);
                }
            }
            // All the IP addresses are unreachable, just return the first one.
            return addresses[0]; // 所有的都不可达
        }

        public InetSocketAddress addr;         // 集群服务地址

        public InetSocketAddress electionAddr; // 集群服务选举地址
        
        public String hostname; // host

        public int port = 2888; // 客户端连接端口（默认2888）

        public int electionPort = -1; // 选举端口

        public long id; // 服务id
        
        public LearnerType type = LearnerType.PARTICIPANT; // learner类型
    }

    // 集群服务状态
    public enum ServerState {
        LOOKING, FOLLOWING, LEADING, OBSERVING;
    }
    
    /*
     * A peer can either be participating, which implies that it is willing to
     * both vote in instances of consensus and to elect or become a Leader, or
     * it may be observing in which case it isn't.
     * 
     * We need this distinction to decide which ServerState to move to when 
     * conditions change (e.g. which state to become after LOOKING). 
     */
    public enum LearnerType { // learner类型
        PARTICIPANT, OBSERVER;
    }
    
    /*
     * To enable observers to have no identifier, we need a generic identifier
     * at least for QuorumCnxManager. We use the following constant to as the
     * value of such a generic identifier. 
     */
    static final long OBSERVER_ID = Long.MAX_VALUE;

    /*
     * Record leader election time
     * 记录leader选举时间（开始、结束时间）
     */
    public long start_fle, end_fle;
    
    /*
     * Default value of peer is participant
     */
    private LearnerType learnerType = LearnerType.PARTICIPANT; // 默认集群中服务为参与者（参与选举）
    
    public LearnerType getLearnerType() {
        return learnerType;
    }
    
    /**
     * Sets the LearnerType both in the QuorumPeer and in the peerMap
     * 设置LearnerType
     */
    public void setLearnerType(LearnerType p) {
        learnerType = p;
        if (quorumPeers.containsKey(this.myid)) {
            this.quorumPeers.get(myid).type = p;
        } else {
            LOG.error("Setting LearnerType to " + p + " but " + myid + " not in QuorumPeers. ");
        }
    }

    /**
     * The servers that make up the cluster
     */
    protected Map<Long, QuorumServer> quorumPeers; // 组成集群的服务

    // 集群参与选举节点数目
    public int getQuorumSize(){
        return getVotingView().size();
    }
    
    /**
     * QuorumVerifier implementation; default (majority). 
     */
    private QuorumVerifier quorumConfig; // 选举规则（默认少数服从多数，及过半原则）
    
    /**
     * My id 服务id
     */
    private long myid;

    /**
     * get the id of this quorum peer.
     * 获取该节点服务id
     */
    public long getId() {
        return myid;
    }

    /**
     * This is who I think the leader currently is.
     * 当前选票
     */
    volatile private Vote currentVote;
    
    /**
     * ... and its counterpart for backward compatibility
     * 之前的选票，为了适应向下兼容
     */
    volatile private Vote bcVote;
        
    public synchronized Vote getCurrentVote(){
        return currentVote;
    }
       
    public synchronized void setCurrentVote(Vote v){
        currentVote = v;
    }
    
    synchronized Vote getBCVote() {
        if (bcVote == null) {
            return currentVote;
        } else {
            return bcVote;
        }
    }

    synchronized void setBCVote(Vote v) {
        bcVote = v;
    }
    
    volatile boolean running = true; // 线程运行标志

    /**
     * The number of milliseconds of each tick
     * tick时间（zookeeper中基准时间）
     */
    protected int tickTime;

    /**
     * Minimum number of milliseconds to allow for session timeout.
     * A value of -1 indicates unset, use default.
     */
    protected int minSessionTimeout = -1;

    /**
     * Maximum number of milliseconds to allow for session timeout.
     * A value of -1 indicates unset, use default.
     */
    protected int maxSessionTimeout = -1;

    /**
     * The number of ticks that the initial synchronization phase can take
     * 启动时follower同步时间，tick的倍数
     */
    protected int initLimit;

    /**
     * The number of ticks that can pass between sending a request and getting
     * an acknowledgment
     * leader和follower心跳检测最大延时，超过该时间，leader默认follower断开连接
     */
    protected int syncLimit;
    
    /**
     * Enables/Disables sync request processor. This option is enabled
     * by default and is to be used with observers. 是否允许观察者进行同步操作
     */
    protected boolean syncEnabled = true;

    /**
     * The current tick
     */
    protected AtomicInteger tick = new AtomicInteger();

    /**
     * Whether or not to listen on all IPs for the two quorum ports
     * (broadcast and fast leader election).
     * 是否监听本地服务的所有ip（本地服务可能有多个网卡，提供多个ip）
     */
    protected boolean quorumListenOnAllIPs = false;

    /**
     * Enable/Disables quorum authentication using sasl. Defaulting to false.
     * 是否允许sasl认证， 默认false
     */
    protected boolean quorumSaslEnableAuth;

    /**
     * If this is false, quorum peer server will accept another quorum peer client
     * connection even if the authentication did not succeed. This can be used while
     * upgrading ZooKeeper server. Defaulting to false (required).
     * 集群服务间连接是否需要sasl认证，默认false
     */
    protected boolean quorumServerSaslAuthRequired;

    /**
     * If this is false, quorum peer learner will talk to quorum peer server
     * without authentication. This can be used while upgrading ZooKeeper
     * server. Defaulting to false (required).
     * learner和集群服务间通信是否需要sasl认证
     */
    protected boolean quorumLearnerSaslAuthRequired;

    /**
     * Kerberos quorum service principal. Defaulting to 'zkquorum/localhost'.
     * Kerberos服务principal，默认为zkquorum/localhost
     */
    protected String quorumServicePrincipal;

    /**
     * Quorum learner login context name in jaas-conf file to read the kerberos
     * security details. Defaulting to 'QuorumLearner'.
     * jaas-conf文件中Quorum learner login context名称，默认QuorumLearner
     */
    protected String quorumLearnerLoginContext;

    /**
     * Quorum server login context name in jaas-conf file to read the kerberos
     * security details. Defaulting to 'QuorumServer'.
     * jaas-conf文件中Quorum server login context名称，默认QuorumServer
     */
    protected String quorumServerLoginContext;

    // TODO: need to tune the default value of thread size
    // quorum连接中默认线程数
    private static final int QUORUM_CNXN_THREADS_SIZE_DEFAULT_VALUE = 20;

    /**
     * The maximum number of threads to allow in the connectionExecutors thread
     * pool which will be used to initiate quorum server connections.
     * connectionExecutors线程池中允许的最大线程数
     */
    protected int quorumCnxnThreadsSize = QUORUM_CNXN_THREADS_SIZE_DEFAULT_VALUE;

    /**
     * Keeps time taken for leader election in milliseconds. Sets the value to
     * this variable only after the completion of leader election.
     * 选举耗时
     */
    private long electionTimeTaken = -1;

    /**
     * @deprecated As of release 3.4.0, this class has been deprecated, since
     * it is used with one of the udp-based versions of leader election, which
     * we are also deprecating. 废弃（旧的选举算法使用）
     * 
     * This class simply responds to requests for the current leader of this
     * node. 该类用于简单响应当前leader的请求
     * <p>
     * The request contains just an xid generated by the requestor.
     * <p>
     * The response has the xid, the id of this server, the id of the leader,
     * and the zxid of the leader.
     *
     */
    @Deprecated
    class ResponderThread extends ZooKeeperThread {
        ResponderThread() {
            super("ResponderThread");
        }

        volatile boolean running = true; // 线程运行标志
        
        @Override
        public void run() {
            try {
                byte b[] = new byte[36];
                ByteBuffer responseBuffer = ByteBuffer.wrap(b);
                DatagramPacket packet = new DatagramPacket(b, b.length);
                while (running) {
                    udpSocket.receive(packet); // 该方法阻塞直到接收到响应数据
                    if (packet.getLength() != 4) { // 接收数据不只有xid，数据无效，因为选举时只发送了xid
                        LOG.warn("Got more than just an xid! Len = " + packet.getLength());
                    } else {
                        responseBuffer.clear(); // position = 0
                        responseBuffer.getInt(); // Skip the xid （跳过xid，position跳到xid位置后面，接着往后面添加数据，最后发送数据）
                        responseBuffer.putLong(myid);
                        Vote current = getCurrentVote(); // 获取当前选票
                        switch (getPeerState()) {
                        case LOOKING: // 正在选举
                            responseBuffer.putLong(current.getId());
                            responseBuffer.putLong(current.getZxid());
                            break;
                        case LEADING: // 群首
                            responseBuffer.putLong(myid);
                            try {
                                long proposed;
                                synchronized(leader) {
                                    proposed = leader.lastProposed;
                                }
                                responseBuffer.putLong(proposed);
                            } catch (NullPointerException npe) {
                                // This can happen in state transitions,
                                // just ignore the request 忽略
                            }
                            break;
                        case FOLLOWING: // 跟随者
                            responseBuffer.putLong(current.getId());
                            try {
                                responseBuffer.putLong(follower.getZxid());
                            } catch (NullPointerException npe) {
                                // This can happen in state transitions,
                                // just ignore the request
                            }
                            break;
                        case OBSERVING: // 观察者
                            // Do nothing, Observers keep themselves to
                            // themselves. 观察者什么都不做
                            break;
                        }
                        packet.setData(b);
                        udpSocket.send(packet); // 发送
                    }
                    packet.setLength(b.length);
                }
            } catch (RuntimeException e) {
                LOG.warn("Unexpected runtime exception in ResponderThread",e);
            } catch (IOException e) {
                LOG.warn("Unexpected IO exception in ResponderThread",e);
            } finally {
                LOG.warn("QuorumPeer responder thread exited");
            }
        }
    }

    private ServerState state = ServerState.LOOKING; // 服务当前状态（刚开始为LOOKING，进行选举）

    // 设置服务状态
    public synchronized void setPeerState(ServerState newState){
        state = newState;
    }

    // 获取该节点当前状态
    public synchronized ServerState getPeerState(){
        return state;
    }

    DatagramSocket udpSocket; // 旧版选举算法使用udp socket连接

    private InetSocketAddress myQuorumAddr; // 该节点的选举地址

    public InetSocketAddress getQuorumAddress(){
        return myQuorumAddr;
    }

    private int electionType; // 选举算法类型

    Election electionAlg; // 选举算法

    ServerCnxnFactory cnxnFactory; // 服务端连接

    private FileTxnSnapLog logFactory = null; // 数据快照、事务日志处理

    private final QuorumStats quorumStats; // 集群中该节点状态

    public static QuorumPeer testingQuorumPeer() throws SaslException {
        return new QuorumPeer();
    }

    protected QuorumPeer() throws SaslException {
        super("QuorumPeer");
        quorumStats = new QuorumStats(this);
        initialize(); // 初始化sasl认证
    }
   
    /**
     * For backward compatibility purposes, we instantiate QuorumMaj by default.
     * 为了向后兼容，选举规则使用少数服从多数
     */
    public QuorumPeer(Map<Long, QuorumServer> quorumPeers, File dataDir,
            File dataLogDir, int electionType,
            long myid, int tickTime, int initLimit, int syncLimit,
            ServerCnxnFactory cnxnFactory) throws IOException {
        this(quorumPeers, dataDir, dataLogDir, electionType, myid, tickTime, 
        		initLimit, syncLimit, false, cnxnFactory, 
        		new QuorumMaj(countParticipants(quorumPeers)));
    }
    
    public QuorumPeer(Map<Long, QuorumServer> quorumPeers, File dataDir,
            File dataLogDir, int electionType,
            long myid, int tickTime, int initLimit, int syncLimit,
            boolean quorumListenOnAllIPs,
            ServerCnxnFactory cnxnFactory, 
            QuorumVerifier quorumConfig) throws IOException {
        this();
        this.cnxnFactory = cnxnFactory;
        this.quorumPeers = quorumPeers;
        this.electionType = electionType;
        this.myid = myid;
        this.tickTime = tickTime;
        this.initLimit = initLimit;
        this.syncLimit = syncLimit;        
        this.quorumListenOnAllIPs = quorumListenOnAllIPs;
        this.logFactory = new FileTxnSnapLog(dataLogDir, dataDir);
        this.zkDb = new ZKDatabase(this.logFactory); // 实例化内存数据库
        if(quorumConfig == null)
            this.quorumConfig = new QuorumMaj(countParticipants(quorumPeers));
        else this.quorumConfig = quorumConfig;
    }

    // 初始化sasl认证
    public void initialize() throws SaslException {
        // init quorum auth server & learner
        if (isQuorumSaslAuthEnabled()) {
            Set<String> authzHosts = new HashSet<String>();
            for (QuorumServer qs : getView().values()) {
                authzHosts.add(qs.hostname);
            }
            authServer = new SaslQuorumAuthServer(isQuorumServerSaslAuthRequired(),
                    quorumServerLoginContext, authzHosts);
            authLearner = new SaslQuorumAuthLearner(isQuorumLearnerSaslAuthRequired(),
                    quorumServicePrincipal, quorumLearnerLoginContext);
            authInitialized = true;
        } else { // 不需要sasl认证
            authServer = new NullQuorumAuthServer();
            authLearner = new NullQuorumAuthLearner();
        }
    }

    // 获取集群服务状态
    QuorumStats quorumStats() {
        return quorumStats;
    }

    // 启动集群服务
    @Override
    public synchronized void start() {
        loadDataBase(); // 加载数据库到内存
        cnxnFactory.start(); // 启动服务端与客户端连接工厂
        startLeaderElection(); // 开始选举
        super.start(); // 开启ZooKeeperThread线程处理突发异常
    }

    // 加载数据库
    private void loadDataBase() {
        File updating = new File(getTxnFactory().getSnapDir(), UPDATING_EPOCH_FILENAME); // epoch更新文件
		try {
            zkDb.loadDataBase(); // 加载数据快照

            // load the epochs
            long lastProcessedZxid = zkDb.getDataTree().lastProcessedZxid;
    		long epochOfZxid = ZxidUtils.getEpochFromZxid(lastProcessedZxid); // 从zxid中获取epoch
            try {
            	currentEpoch = readLongFromFile(CURRENT_EPOCH_FILENAME); // 从文件中读取currentEpoch
                // 当前epoch落后于last zxid，服务在保存快照后关闭，但是没有更新当前epoch
                if (epochOfZxid > currentEpoch && updating.exists()) {
                    LOG.info("{} found. The server was terminated after " +
                             "taking a snapshot but before updating current " +
                             "epoch. Setting current epoch to {}.",
                             UPDATING_EPOCH_FILENAME, epochOfZxid);
                    setCurrentEpoch(epochOfZxid); // 更新currentEpoch
                    if (!updating.delete()) { // 删除updateEpoch文件
                        throw new IOException("Failed to delete " +
                                              updating.toString());
                    }
                }
            } catch(FileNotFoundException e) { // currentEpoch文件不存在
            	// pick a reasonable epoch number
            	// this should only happen once when moving to a
            	// new code version 该异常只会在移动到新版本时发生
            	currentEpoch = epochOfZxid;
            	LOG.info(CURRENT_EPOCH_FILENAME
            	        + " not found! Creating with a reasonable default of {}. This should only happen when you are upgrading your installation",
            	        currentEpoch);
            	writeLongToFile(CURRENT_EPOCH_FILENAME, currentEpoch); // 当前epoch写到currentEpoch文件
            }
            if (epochOfZxid > currentEpoch) { // 当前epoch落后于last zxid
            	throw new IOException("The current epoch, " + ZxidUtils.zxidToString(currentEpoch) + ", is older than the last zxid, " + lastProcessedZxid);
            }
            try {
            	acceptedEpoch = readLongFromFile(ACCEPTED_EPOCH_FILENAME); // 从文件中读取acceptedEpoch
            } catch(FileNotFoundException e) {
            	// pick a reasonable epoch number
            	// this should only happen once when moving to a
            	// new code version 该异常只会在移动到新版本时发生
            	acceptedEpoch = epochOfZxid;
            	LOG.info(ACCEPTED_EPOCH_FILENAME
            	        + " not found! Creating with a reasonable default of {}. This should only happen when you are upgrading your installation",
            	        acceptedEpoch);
            	writeLongToFile(ACCEPTED_EPOCH_FILENAME, acceptedEpoch);
            }
            if (acceptedEpoch < currentEpoch) { // acceptedEpoch小于当前epoch，抛出异常
            	throw new IOException("The accepted epoch, " + ZxidUtils.zxidToString(acceptedEpoch) + " is less than the current epoch, " + ZxidUtils.zxidToString(currentEpoch));
            }
        } catch(IOException ie) {
            LOG.error("Unable to load database on disk", ie);
            throw new RuntimeException("Unable to run quorum server ", ie);
        }
	}

    ResponderThread responder; // 旧版选举算法发送消息线程

    // 停止leader选举
    synchronized public void stopLeaderElection() {
        responder.running = false;
        responder.interrupt();
    }

    // 开始leader选举
    synchronized public void startLeaderElection() {
    	try {
    		currentVote = new Vote(myid, getLastLoggedZxid(), getCurrentEpoch()); // 生成初始选票
    	} catch(IOException e) {
    		RuntimeException re = new RuntimeException(e.getMessage());
    		re.setStackTrace(e.getStackTrace());
    		throw re;
    	}
        for (QuorumServer p : getView().values()) { // 该节点是否在集群节点中
            if (p.id == myid) {
                myQuorumAddr = p.addr; // 该节点地址
                break;
            }
        }
        if (myQuorumAddr == null) { // 该节点不在集群节点中
            throw new RuntimeException("My id " + myid + " not in the peer list");
        }
        if (electionType == 0) { // 选举算法类型为0（旧的选举算法LeaderElection）
            try {
                udpSocket = new DatagramSocket(myQuorumAddr.getPort()); // 启动该服务的选举socket服务，开始监听选举端口
                responder = new ResponderThread();
                responder.start(); // 开启responder线程，等待接收选举数据
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
        this.electionAlg = createElectionAlgorithm(electionType); // 生成选举算法
    }
    
    /**
     * Count the number of nodes in the map that could be followers.
     * 计算peers节点中能成为follower的数量（参加选举数量）
     * @param peers
     * @return The number of followers in the map
     */
    protected static int countParticipants(Map<Long, QuorumServer> peers) {
      int count = 0;
      for (QuorumServer q : peers.values()) {
          if (q.type == LearnerType.PARTICIPANT) {
              count++;
          }
      }
      return count;
    }
    
    /**
     * This constructor is only used by the existing unit test code.
     * It defaults to FileLogProvider persistence provider.
     * 该构造函数只用于单元测试
     */
    public QuorumPeer(Map<Long,QuorumServer> quorumPeers, File snapDir,
            File logDir, int clientPort, int electionAlg,
            long myid, int tickTime, int initLimit, int syncLimit)
        throws IOException {
        this(quorumPeers, snapDir, logDir, electionAlg,
                myid,tickTime, initLimit,syncLimit, false,
                ServerCnxnFactory.createFactory(new InetSocketAddress(clientPort), -1),
                new QuorumMaj(countParticipants(quorumPeers)));
    }
    
    /**
     * This constructor is only used by the existing unit test code.
     * It defaults to FileLogProvider persistence provider.
     * 该构造函数只用于单元测试
     */
    public QuorumPeer(Map<Long,QuorumServer> quorumPeers, File snapDir,
            File logDir, int clientPort, int electionAlg,
            long myid, int tickTime, int initLimit, int syncLimit, 
            QuorumVerifier quorumConfig)
        throws IOException {
        this(quorumPeers, snapDir, logDir, electionAlg,
                myid,tickTime, initLimit,syncLimit, false,
                ServerCnxnFactory.createFactory(new InetSocketAddress(clientPort), -1),
                quorumConfig);
    }
    
    /**
     * returns the highest zxid that this host has seen
     * 返回该服务上最近处理的zxid
     * 
     * @return the highest zxid for this host
     */
    public long getLastLoggedZxid() {
        if (!zkDb.isInitialized()) { // 如果数据库没有初始化，加载数据库到内存
        	loadDataBase();
        }
        return zkDb.getDataTreeLastProcessedZxid();
    }
    
    public Follower follower;
    public Leader leader;
    public Observer observer;

    // 生成follower
    protected Follower makeFollower(FileTxnSnapLog logFactory) throws IOException {
        return new Follower(this, new FollowerZooKeeperServer(logFactory, 
                this, new ZooKeeperServer.BasicDataTreeBuilder(), this.zkDb));
    }

    // 生成leader
    protected Leader makeLeader(FileTxnSnapLog logFactory) throws IOException {
        return new Leader(this, new LeaderZooKeeperServer(logFactory,
                this, new ZooKeeperServer.BasicDataTreeBuilder(), this.zkDb));
    }

    // 生成观察者
    protected Observer makeObserver(FileTxnSnapLog logFactory) throws IOException {
        return new Observer(this, new ObserverZooKeeperServer(logFactory,
                this, new ZooKeeperServer.BasicDataTreeBuilder(), this.zkDb));
    }

    // 生成选举算法
    protected Election createElectionAlgorithm(int electionAlgorithm){
        Election le = null;
                
        //TODO: use a factory rather than a switch
        switch (electionAlgorithm) { // 选举算法
        case 0:
            le = new LeaderElection(this);
            break;
        case 1:
            le = new AuthFastLeaderElection(this);
            break;
        case 2:
            le = new AuthFastLeaderElection(this, true);
            break;
        case 3:
            qcm = createCnxnManager(); // 创建选举通信连接
            QuorumCnxManager.Listener listener = qcm.listener;
            if(listener != null){
                listener.start(); // 开启选举监听端口线程
                le = new FastLeaderElection(this, qcm);
            } else {
                LOG.error("Null listener when initializing cnx manager");
            }
            break;
        default:
            assert false;
        }
        return le;
    }

    // 生成leader选举算法
    protected Election makeLEStrategy(){
        LOG.debug("Initializing leader election protocol...");
        if (getElectionType() == 0) {
            electionAlg = new LeaderElection(this);
        }        
        return electionAlg;
    }

    synchronized protected void setLeader(Leader newLeader){
        leader = newLeader;
    }

    synchronized protected void setFollower(Follower newFollower){
        follower = newFollower;
    }
    
    synchronized protected void setObserver(Observer newObserver){
        observer = newObserver;
    }

    // 获取该节点的ZooKeeperServer（根据该节点的角色返回对应的ZooKeeperServer）
    synchronized public ZooKeeperServer getActiveServer(){
        if(leader != null)
            return leader.zk;
        else if(follower != null)
            return follower.zk;
        else if (observer != null)
            return observer.zk;
        return null;
    }

    // 线程执行
    @Override
    public void run() {
        // 设置线程名称
        setName("QuorumPeer" + "[myid=" + getId() + "]" + cnxnFactory.getLocalAddress());

        LOG.debug("Starting quorum peer");
        try {
            jmxQuorumBean = new QuorumBean(this);
            MBeanRegistry.getInstance().register(jmxQuorumBean, null); // JMX注册QuorumBean
            for(QuorumServer s : getView().values()){
                ZKMBeanInfo p;
                if (getId() == s.id) { // 注册本地LocalPeerBean
                    p = jmxLocalPeerBean = new LocalPeerBean(this);
                    try {
                        MBeanRegistry.getInstance().register(p, jmxQuorumBean);
                    } catch (Exception e) {
                        LOG.warn("Failed to register with JMX", e);
                        jmxLocalPeerBean = null;
                    }
                } else { // 注册本地RemotePeerBean
                    p = new RemotePeerBean(s);
                    try {
                        MBeanRegistry.getInstance().register(p, jmxQuorumBean);
                    } catch (Exception e) {
                        LOG.warn("Failed to register with JMX", e);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            jmxQuorumBean = null; // 释放jmxQuorumBean（GC）
        }

        try {
            // Main loop 主循环
            while (running) {
                switch (getPeerState()) {
                case LOOKING: // 选举阶段
                    LOG.info("LOOKING");

                    // zookeeper的只读模式指一个服务器与集群中过半机器失去连接以后，这个服务器就不在不处理客户端的请求，
                    // 但仍然希望该服务器可以提供读服务
                    if (Boolean.getBoolean("readonlymode.enabled")) { // 启动ReadOnlyZooKeeperServer
                        LOG.info("Attempting to start ReadOnlyZooKeeperServer");

                        // Create read-only server but don't start it immediately
                        // 创建一个只读服务，不立即启动
                        final ReadOnlyZooKeeperServer roZk = new ReadOnlyZooKeeperServer(
                                logFactory, this,
                                new ZooKeeperServer.BasicDataTreeBuilder(),
                                this.zkDb);
    
                        // Instead of starting roZk immediately, wait some grace
                        // period before we decide we're partitioned.
                        //
                        // Thread is used here because otherwise it would require
                        // changes in each of election strategy classes which is
                        // unnecessary code coupling（耦合）.
                        Thread roZkMgr = new Thread() {
                            public void run() {
                                try {
                                    // lower-bound grace period to 2 secs （sleep下限2秒）
                                    sleep(Math.max(2000, tickTime));
                                    if (ServerState.LOOKING.equals(getPeerState())) {
                                        roZk.startup(); // sleep一会儿后启动
                                    }
                                } catch (InterruptedException e) {
                                    LOG.info("Interrupted while attempting to start ReadOnlyZooKeeperServer, not started");
                                } catch (Exception e) {
                                    LOG.error("FAILED to start ReadOnlyZooKeeperServer", e);
                                }
                            }
                        };
                        try {
                            // 如果产生网络分区，lookForLeader会一直进行选举，roZkMgr线程在sleep2秒后启动，对只读的客户端提供服务
                            roZkMgr.start();
                            setBCVote(null);
                            setCurrentVote(makeLEStrategy().lookForLeader()); // 设置选出的选票
                        } catch (Exception e) {
                            LOG.warn("Unexpected exception", e);
                            setPeerState(ServerState.LOOKING); // 继续选举
                        } finally {
                            // If the thread is in the the grace period, interrupt
                            // to come out of waiting.
                            roZkMgr.interrupt(); // 如果线程roZkMgr还在等待，中断等待
                            roZk.shutdown(); // 网络分区恢复，选举完成，关闭ReadOnlyZooKeeperServer服务
                        }
                    } else {
                        try {
                            setBCVote(null);
                            setCurrentVote(makeLEStrategy().lookForLeader()); // 进行选举
                        } catch (Exception e) {
                            LOG.warn("Unexpected exception", e);
                            setPeerState(ServerState.LOOKING);
                        }
                    }
                    break;
                case OBSERVING: // 观察者
                    try {
                        LOG.info("OBSERVING");
                        setObserver(makeObserver(logFactory)); // 设置观察者
                        observer.observeLeader(); // 观察leader
                    } catch (Exception e) {
                        LOG.warn("Unexpected exception",e );                        
                    } finally { // 最后关闭observer
                        observer.shutdown();
                        setObserver(null);
                        setPeerState(ServerState.LOOKING);
                    }
                    break;
                case FOLLOWING: // 跟随者
                    try {
                        LOG.info("FOLLOWING");
                        setFollower(makeFollower(logFactory)); // 设置跟随者
                        follower.followLeader(); // 跟随leader
                    } catch (Exception e) {
                        LOG.warn("Unexpected exception",e);
                    } finally { // 最后关闭follower
                        follower.shutdown();
                        setFollower(null);
                        setPeerState(ServerState.LOOKING);
                    }
                    break;
                case LEADING: // 领导者
                    LOG.info("LEADING");
                    try {
                        setLeader(makeLeader(logFactory)); // 设置leader
                        leader.lead(); // 处理leader工作
                        setLeader(null);
                    } catch (Exception e) {
                        LOG.warn("Unexpected exception",e);
                    } finally {
                        if (leader != null) { // 关闭leader
                            leader.shutdown("Forcing shutdown");
                            setLeader(null);
                        }
                        setPeerState(ServerState.LOOKING); // 重新进入选举状态
                    }
                    break;
                }
            }
        } finally { // 线程退出
            LOG.warn("QuorumPeer main thread exited");
            try {
                MBeanRegistry.getInstance().unregisterAll(); // 注销JMX
            } catch (Exception e) {
                LOG.warn("Failed to unregister with JMX", e);
            }
            // 释放JMX bean
            jmxQuorumBean = null;
            jmxLocalPeerBean = null;
        }
    }

    // 关闭线程和所有资源
    public void shutdown() {
        running = false; // 停止线程
        if (leader != null) { // 如果该节点是leader，关闭leader
            leader.shutdown("quorum Peer shutdown");
        }
        if (follower != null) { // 如果该节点是follower，关闭follower
            follower.shutdown();
        }
        cnxnFactory.shutdown(); // 关闭与客户端连接
        if(udpSocket != null) { // 关闭旧版选举算法发送请求socket
            udpSocket.close();
        }
        
        if(getElectionAlg() != null){ // 选举还没结束，中断选举过程
            this.interrupt();
        	getElectionAlg().shutdown(); // 结束选举
        }
        try {
            zkDb.close(); // 关闭数据库
        } catch (IOException ie) {
            LOG.warn("Error closing logs ", ie);
        }
    }

    /**
     * A 'view' is a node's current opinion of the membership of the entire
     * ensemble. 集群服务视图（所有构成集群服务节点）
     */
    public Map<Long, QuorumPeer.QuorumServer> getView() {
        return Collections.unmodifiableMap(this.quorumPeers);
    }

    /**
     * Observers are not contained in this view, only nodes with 
     * PeerType=PARTICIPANT. 参与选举的集群服务节点视图
     */
    public Map<Long,QuorumPeer.QuorumServer> getVotingView() {
        return QuorumPeer.viewToVotingView(getView());
    }

    // 从所有集群服务节点中挑选出参与选举的节点
    static Map<Long,QuorumPeer.QuorumServer> viewToVotingView(Map<Long,QuorumPeer.QuorumServer> view) {
        Map<Long,QuorumPeer.QuorumServer> ret = new HashMap<Long, QuorumPeer.QuorumServer>();
        for (QuorumServer server : view.values()) {
            if (server.type == LearnerType.PARTICIPANT) {
                ret.put(server.id, server);
            }
        }
        return ret;
    }

    /**
     * Returns only observers, no followers. 获取观察者视图
     */
    public Map<Long,QuorumPeer.QuorumServer> getObservingView() {
        Map<Long,QuorumPeer.QuorumServer> ret = new HashMap<Long, QuorumPeer.QuorumServer>();
        Map<Long,QuorumPeer.QuorumServer> view = getView();
        for (QuorumServer server : view.values()) {            
            if (server.type == LearnerType.OBSERVER) {
                ret.put(server.id, server);
            }
        }        
        return ret;
    }
    
    /**
     * Check if a node is in the current view. With static membership, the
     * result of this check will never change; only when dynamic membership
     * is introduced will this be more useful.
     * 确定一个节点是否在当前视图中
     */
    public boolean viewContains(Long sid) {
        return this.quorumPeers.containsKey(sid);
    }
    
    /**
     * Only used by QuorumStats at the moment 只在QuorumStats中获取当时集群信息时使用
     */
    public String[] getQuorumPeers() {
        List<String> l = new ArrayList<String>();
        synchronized (this) {
            if (leader != null) {
                for (LearnerHandler fh : leader.getLearners()) {
                    if (fh.getSocket() != null) {
                        String s = fh.getSocket().getRemoteSocketAddress().toString();
                        if (leader.isLearnerSynced(fh))
                            s += "*";
                        l.add(s);
                    }
                }
            } else if (follower != null) {
                l.add(follower.sock.getRemoteSocketAddress().toString());
            }
        }
        return l.toArray(new String[0]);
    }

    // 获取服务当前状态
    public String getServerState() {
        switch (getPeerState()) {
        case LOOKING:
            return QuorumStats.Provider.LOOKING_STATE;
        case LEADING:
            return QuorumStats.Provider.LEADING_STATE;
        case FOLLOWING:
            return QuorumStats.Provider.FOLLOWING_STATE;
        case OBSERVING:
            return QuorumStats.Provider.OBSERVING_STATE;
        }
        return QuorumStats.Provider.UNKNOWN_STATE;
    }


    /**
     * get the id of this quorum peer.
     * 获取服务id
     */
    public long getMyid() {
        return myid;
    }

    /**
     * set the id of this quorum peer.
     * 设置服务id
     */
    public void setMyid(long myid) {
        this.myid = myid;
    }

    /**
     * Get the number of milliseconds of each tick
     * 获取tick时间
     */
    public int getTickTime() {
        return tickTime;
    }

    /**
     * Set the number of milliseconds of each tick
     * 设置tick时间
     */
    public void setTickTime(int tickTime) {
        LOG.info("tickTime set to " + tickTime);
        this.tickTime = tickTime;
    }

    /** Maximum number of connections allowed from particular host (ip) */
    // 获取和客户端做大连接数
    public int getMaxClientCnxnsPerHost() {
        ServerCnxnFactory fac = getCnxnFactory();
        if (fac == null) {
            return -1;
        }
        return fac.getMaxClientCnxnsPerHost();
    }
    
    /** minimum session timeout in milliseconds */
    // 获取session最小超时时间（默认2倍tickTime）
    public int getMinSessionTimeout() {
        return minSessionTimeout == -1 ? tickTime * 2 : minSessionTimeout;
    }

    /** minimum session timeout in milliseconds */
    // 设置session最小超时时间
    public void setMinSessionTimeout(int min) {
        LOG.info("minSessionTimeout set to " + min);
        this.minSessionTimeout = min;
    }

    /** maximum session timeout in milliseconds */
    // 获取session最大超时时间（默认20倍tickTime）
    public int getMaxSessionTimeout() {
        return maxSessionTimeout == -1 ? tickTime * 20 : maxSessionTimeout;
    }

    /** minimum session timeout in milliseconds */
    // 设置session最大超时时间
    public void setMaxSessionTimeout(int max) {
        LOG.info("maxSessionTimeout set to " + max);
        this.maxSessionTimeout = max;
    }

    /**
     * Get the number of ticks that the initial synchronization phase can take
     * 获取初始连接leader同步数据时间（initLimit倍tickTime）
     */
    public int getInitLimit() {
        return initLimit;
    }

    /**
     * Set the number of ticks that the initial synchronization phase can take
     * 设置initLimit
     */
    public void setInitLimit(int initLimit) {
        LOG.info("initLimit set to " + initLimit);
        this.initLimit = initLimit;
    }

    /**
     * Get the current tick
     */
    public int getTick() {
        return tick.get();
    }
    
    /**
     * Return QuorumVerifier object
     */
    public QuorumVerifier getQuorumVerifier(){
        return quorumConfig;
    }
    
    public void setQuorumVerifier(QuorumVerifier quorumConfig){
       this.quorumConfig = quorumConfig;
    }
    
    /**
     * Get an instance of LeaderElection
     * 获取选举算法Election实例
     */
    public Election getElectionAlg(){
        return electionAlg;
    }
        
    /**
     * Get the synclimit
     * 获取leader和follower之间心跳最大延时时间
     */
    public int getSyncLimit() {
        return syncLimit;
    }

    /**
     * Set the synclimit
     * 设置leader和follower之间心跳最大延时时间
     */
    public void setSyncLimit(int syncLimit) {
        this.syncLimit = syncLimit;
    }

    /**
     * The syncEnabled can also be set via a system property.
     * 是否允许同步的系统变量名称
     */
    public static final String SYNC_ENABLED = "zookeeper.observer.syncEnabled";
    
    /**
     * Return syncEnabled.
     * 获取syncEnabled
     *
     * @return
     */
    public boolean getSyncEnabled() {
        if (System.getProperty(SYNC_ENABLED) != null) {
            LOG.info(SYNC_ENABLED + "=" + Boolean.getBoolean(SYNC_ENABLED));   
            return Boolean.getBoolean(SYNC_ENABLED);
        } else {        
            return syncEnabled;
        }
    }
    
    /**
     * Set syncEnabled.
     * 
     * @param syncEnabled
     */
    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    /**
     * Gets the election type 获取选举类型
     */
    public int getElectionType() {
        return electionType;
    }

    /**
     * Sets the election type 设置选举类型
     */
    public void setElectionType(int electionType) {
        this.electionType = electionType;
    }

    public boolean getQuorumListenOnAllIPs() {
        return quorumListenOnAllIPs;
    }

    public void setQuorumListenOnAllIPs(boolean quorumListenOnAllIPs) {
        this.quorumListenOnAllIPs = quorumListenOnAllIPs;
    }

    // 获取和客户端连接工厂
    public ServerCnxnFactory getCnxnFactory() {
        return cnxnFactory;
    }

    public void setCnxnFactory(ServerCnxnFactory cnxnFactory) {
        this.cnxnFactory = cnxnFactory;
    }

    public void setQuorumPeers(Map<Long,QuorumServer> quorumPeers) {
        this.quorumPeers = quorumPeers;
    }

    public int getClientPort() {
        return cnxnFactory.getLocalPort();
    }

    public void setClientPortAddress(InetSocketAddress addr) {
    }

    // 设置数据快照和事务日志工厂类
    public void setTxnFactory(FileTxnSnapLog factory) {
        this.logFactory = factory;
    }
    
    public FileTxnSnapLog getTxnFactory() {
        return this.logFactory;
    }

    /**
     * set zk database for this node 设置数据库
     * @param database
     */
    public void setZKDatabase(ZKDatabase database) {
        this.zkDb = database;
    }

    protected ZKDatabase getZkDb() {
        return zkDb;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * get reference to QuorumCnxManager 获取选举通信连接
     */
    public QuorumCnxManager getQuorumCnxManager() {
        return qcm;
    }

    // 从文件中读取currentEpoch或acceptedEpoch
    private long readLongFromFile(String name) throws IOException {
    	File file = new File(logFactory.getSnapDir(), name); // 文件存放在数据快照目录
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		try {
			line = br.readLine();
    		return Long.parseLong(line);
    	} catch(NumberFormatException e) {
    		throw new IOException("Found " + line + " in " + file);
    	} finally {
    		br.close();
    	}
    }

    private long acceptedEpoch = -1; // 接收的epoch
    private long currentEpoch = -1;  // 当前epoch

	public static final String CURRENT_EPOCH_FILENAME = "currentEpoch";   // 当前epoch文件名称

	public static final String ACCEPTED_EPOCH_FILENAME = "acceptedEpoch"; // acceptedEpoch文件名称

    // updatingEpoch文件名称，用于更新标志，保存快照前先创建该文件，保存完成后删除，如果存在该文件则表示保存完快照服务被关闭，
    // 这时可能没有设置currentEpoch，会导致数据库中的epoch和文件中保存的当前epoch不相符
    public static final String UPDATING_EPOCH_FILENAME = "updatingEpoch";

	/**
	 * Write a long value to disk atomically. Either succeeds or an exception
	 * is thrown.
     * 原子地写数据到文件
	 * @param name file name to write the long to
	 * @param value the long value to write to the named file
	 * @throws IOException if the file cannot be written atomically
	 */
    private void writeLongToFile(String name, long value) throws IOException {
        File file = new File(logFactory.getSnapDir(), name); // 文件在数据快照目录
        AtomicFileOutputStream out = new AtomicFileOutputStream(file); // 该文件输出流具有原子性
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
        boolean aborted = false;
        try {
            bw.write(Long.toString(value));
            bw.flush();
            
            out.flush();
        } catch (IOException e) { // 写文件异常
            LOG.error("Failed to write new file " + file, e);
            // worst case here the tmp file/resources(fd) are not cleaned up
            //   and the caller will be notified (IOException)
            aborted = true; // 中断
            out.abort();
            throw e;
        } finally {
            if (!aborted) { // 如果没有中断，关闭输出流
                // if the close operation (rename) fails we'll get notified.
                // worst case the tmp file may still exist
                out.close();
            }
        }
    }

    // 返回当前epoch
    public long getCurrentEpoch() throws IOException {
		if (currentEpoch == -1) {
			currentEpoch = readLongFromFile(CURRENT_EPOCH_FILENAME);
		}
		return currentEpoch;
	}

    // 返回acceptedEpoch
	public long getAcceptedEpoch() throws IOException {
		if (acceptedEpoch == -1) {
			acceptedEpoch = readLongFromFile(ACCEPTED_EPOCH_FILENAME);
		}
		return acceptedEpoch;
	}

    // 设置当前epoch
	public void setCurrentEpoch(long e) throws IOException {
		currentEpoch = e;
		writeLongToFile(CURRENT_EPOCH_FILENAME, e);
	}

    // 设置acceptedEpoch
	public void setAcceptedEpoch(long e) throws IOException {
		acceptedEpoch = e;
		writeLongToFile(ACCEPTED_EPOCH_FILENAME, e);
	}

    /**
     * Updates leader election info to avoid inconsistencies when
     * a new server tries to join the ensemble.
     * 新的服务加入集群时更新选票（为了保持一致）
     * See ZOOKEEPER-1732 for more info.
     */
    protected void updateElectionVote(long newEpoch) {
        Vote currentVote = getCurrentVote();
        setBCVote(currentVote); // 设置旧的选票
        if (currentVote != null) {
            setCurrentVote(new Vote(currentVote.getId(),
                currentVote.getZxid(),
                currentVote.getElectionEpoch(),
                newEpoch,
                currentVote.getState()));
        }
    }

    void setQuorumServerSaslRequired(boolean serverSaslRequired) {
        quorumServerSaslAuthRequired = serverSaslRequired;
        LOG.info("{} set to {}", QuorumAuth.QUORUM_SERVER_SASL_AUTH_REQUIRED, serverSaslRequired);
    }

    void setQuorumLearnerSaslRequired(boolean learnerSaslRequired) {
        quorumLearnerSaslAuthRequired = learnerSaslRequired;
        LOG.info("{} set to {}", QuorumAuth.QUORUM_LEARNER_SASL_AUTH_REQUIRED, learnerSaslRequired);
    }

    void setQuorumSaslEnabled(boolean enableAuth) {
        quorumSaslEnableAuth = enableAuth;
        if (!quorumSaslEnableAuth) {
            LOG.info("QuorumPeer communication is not secured!");
        } else {
            LOG.info("{} set to {}", QuorumAuth.QUORUM_SASL_AUTH_ENABLED, enableAuth);
        }
    }

    void setQuorumServicePrincipal(String servicePrincipal) {
        quorumServicePrincipal = servicePrincipal;
        LOG.info("{} set to {}",QuorumAuth.QUORUM_KERBEROS_SERVICE_PRINCIPAL, quorumServicePrincipal);
    }

    void setQuorumLearnerLoginContext(String learnerContext) {
        quorumLearnerLoginContext = learnerContext;
        LOG.info("{} set to {}", QuorumAuth.QUORUM_LEARNER_SASL_LOGIN_CONTEXT, quorumLearnerLoginContext);
    }

    void setQuorumServerLoginContext(String serverContext) {
        quorumServerLoginContext = serverContext;
        LOG.info("{} set to {}", QuorumAuth.QUORUM_SERVER_SASL_LOGIN_CONTEXT, quorumServerLoginContext);
    }

    // 设置QuorumCnxn线程池大小
    void setQuorumCnxnThreadsSize(int qCnxnThreadsSize) {
        if (qCnxnThreadsSize > QUORUM_CNXN_THREADS_SIZE_DEFAULT_VALUE) {
            quorumCnxnThreadsSize = qCnxnThreadsSize;
        }
        LOG.info("quorum.cnxn.threads.size set to {}", quorumCnxnThreadsSize);
    }

    boolean isQuorumSaslAuthEnabled() {
        return quorumSaslEnableAuth;
    }

    private boolean isQuorumServerSaslAuthRequired() {
        return quorumServerSaslAuthRequired;
    }

    private boolean isQuorumLearnerSaslAuthRequired() {
        return quorumLearnerSaslAuthRequired;
    }

    // VisibleForTesting. Returns true if both the quorumlearner and
    // quorumserver login has been finished. Otherwse, false. 测试使用
    public boolean hasAuthInitialized(){
        return authInitialized;
    }

    // 创建选举通信连接QuorumCnxManager
    public QuorumCnxManager createCnxnManager() {
        return new QuorumCnxManager(this.getId(),
                                    this.getView(),
                                    this.authServer,
                                    this.authLearner,
                                    this.tickTime * this.syncLimit,
                                    this.getQuorumListenOnAllIPs(),
                                    this.quorumCnxnThreadsSize,
                                    this.isQuorumSaslAuthEnabled());
    }

    /**
     * Sets the time taken for leader election in milliseconds.
     * 设置选举耗时
     *
     * @param electionTimeTaken
     *            time taken for leader election
     */
    void setElectionTimeTaken(long electionTimeTaken) {
        this.electionTimeTaken = electionTimeTaken;
    }

    /**
     * 获取选举耗时
     * @return the time taken for leader election in milliseconds.
     */
    long getElectionTimeTaken() {
        return electionTimeTaken;
    }
}
