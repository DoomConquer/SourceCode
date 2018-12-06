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

import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.security.sasl.SaslException;

import org.apache.jute.BinaryOutputArchive;
import org.apache.zookeeper.common.Time;
import org.apache.zookeeper.server.FinalRequestProcessor;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.ZooKeeperThread;
import org.apache.zookeeper.server.quorum.QuorumPeer.LearnerType;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;
import org.apache.zookeeper.server.util.SerializeUtils;
import org.apache.zookeeper.server.util.ZxidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has the control logic for the Leader.
 * leader的处理逻辑
 */
public class Leader {
    private static final Logger LOG = LoggerFactory.getLogger(Leader.class);
    
    static final private boolean nodelay = System.getProperty("leader.nodelay", "true").equals("true"); // tcp nodelay标志
    static {
        LOG.info("TCP NoDelay set to: " + nodelay);
    }

    // 提案（用于将请求封装成提议发送给其他服务）
    static public class Proposal {
        public QuorumPacket packet; // 请求的数据包

        public HashSet<Long> ackSet = new HashSet<Long>(); // 等待的ack确认集合

        public Request request; // 该提议对应的请求

        @Override
        public String toString() {
            return packet.getType() + ", " + packet.getZxid() + ", " + request;
        }
    }

    final LeaderZooKeeperServer zk; // leader处理客户端服务

    final QuorumPeer self;

    // VisibleForTesting 测试使用
    protected boolean quorumFormed = false; // 是否构成仲裁集群
    
    // the follower acceptor thread learner等待连接线程
    LearnerCnxAcceptor cnxAcceptor;
    
    // list of all the followers
    // learner列表
    private final HashSet<LearnerHandler> learners = new HashSet<LearnerHandler>();

    /**
     * Returns a copy of the current learner snapshot
     * 获取learner列表
     */
    public List<LearnerHandler> getLearners() {
        synchronized (learners) {
            return new ArrayList<LearnerHandler>(learners);
        }
    }

    // list of followers that are ready to follow (i.e synced with the leader)
    // 完成同步的follower列表
    private final HashSet<LearnerHandler> forwardingFollowers = new HashSet<LearnerHandler>();

    private final ProposalStats proposalStats; // 提议状态统计信息

    // 获取提议状态信息
    public ProposalStats getProposalStats() {
        return proposalStats;
    }

    /**
     * Returns a copy of the current forwarding follower snapshot
     * 获取已经完成同步的follower列表
     */
    public List<LearnerHandler> getForwardingFollowers() {
        synchronized (forwardingFollowers) {
            return new ArrayList<LearnerHandler>(forwardingFollowers);
        }
    }

    // 添加完成同步的follower
    private void addForwardingFollower(LearnerHandler lh) {
        synchronized (forwardingFollowers) {
            forwardingFollowers.add(lh);
        }
    }

    // 完成同步的观察者集合
    private final HashSet<LearnerHandler> observingLearners = new HashSet<LearnerHandler>();

    /**
     * Returns a copy of the current observer snapshot
     * 获取完成同步的观察者列表
     */
    public List<LearnerHandler> getObservingLearners() {
        synchronized (observingLearners) {
            return new ArrayList<LearnerHandler>(observingLearners);
        }
    }

    // 添加完成同步的观察者到集合
    private void addObserverLearnerHandler(LearnerHandler lh) {
        synchronized (observingLearners) {
            observingLearners.add(lh);
        }
    }

    // Pending sync requests. Must access under 'this' lock.
    // 挂起同步的请求（必须在this锁下操作）
    private final HashMap<Long, List<LearnerSyncRequest>> pendingSyncs = new HashMap<Long, List<LearnerSyncRequest>>();

    // 获取挂起的同步请求数量
    synchronized public int getNumPendingSyncs() {
        return pendingSyncs.size();
    }

    //Follower counter 跟随者计数器
    final AtomicLong followerCounter = new AtomicLong(-1);

    /**
     * Adds peer to the leader. 添加learner对应的handler到learners列表
     * 
     * @param learner
     *                instance of learner handle
     */
    void addLearnerHandler(LearnerHandler learner) {
        synchronized (learners) {
            learners.add(learner);
        }
    }

    /**
     * Remove the learner from the learner list
     * 移除关闭的learner对应的handler
     * 
     * @param peer
     */
    void removeLearnerHandler(LearnerHandler peer) {
        synchronized (forwardingFollowers) {
            forwardingFollowers.remove(peer);            
        }        
        synchronized (learners) {
            learners.remove(peer);
        }
        synchronized (observingLearners) {
            observingLearners.remove(peer);
        }
    }

    // 判断learner是否完成同步
    boolean isLearnerSynced(LearnerHandler peer){
        synchronized (forwardingFollowers) {
            return forwardingFollowers.contains(peer);
        }        
    }
    
    ServerSocket ss; // leader和learner的socket连接
    // leader构造函数
    Leader(QuorumPeer self, LeaderZooKeeperServer zk) throws IOException {
        this.self = self;
        this.proposalStats = new ProposalStats();
        try {
            if (self.getQuorumListenOnAllIPs()) { // 监听本地服务的所有ip
                ss = new ServerSocket(self.getQuorumAddress().getPort());
            } else {
                ss = new ServerSocket();
            }
            ss.setReuseAddress(true); // 设置tcp SO_REUSEADDR属性
            if (!self.getQuorumListenOnAllIPs()) {
                ss.bind(self.getQuorumAddress()); // 不监听本地服务的所有ip，绑定地址，否则只绑定端口
            }
        } catch (BindException e) { // 绑定异常
            if (self.getQuorumListenOnAllIPs()) {
                LOG.error("Couldn't bind to port " + self.getQuorumAddress().getPort(), e);
            } else {
                LOG.error("Couldn't bind to " + self.getQuorumAddress(), e);
            }
            throw e;
        }
        this.zk = zk;
    }

    /**
     * This message is for follower to expect diff
     * 跟随者diff数据
     */
    final static int DIFF = 13;
    
    /**
     * This is for follower to truncate its logs
     * 跟随者截断日志
     */
    final static int TRUNC = 14;
    
    /**
     * This is for follower to download the snapshots
     * 跟随者下载数据快照
     */
    final static int SNAP = 15;
    
    /**
     * This tells the leader that the connecting peer is actually an observer
     * 连接的是观察者
     */
    final static int OBSERVERINFO = 16;
    
    /**
     * This message type is sent by the leader to indicate it's zxid and if
     * needed, its database. 通知learner进行快照（旧版本的不一样）
     */
    final static int NEWLEADER = 10;

    /**
     * This message type is sent by a follower to pass the last zxid. This is here
     * for backward compatibility purposes. 跟随者发送其最近的zxid
     */
    final static int FOLLOWERINFO = 11;

    /**
     * This message type is sent by the leader to indicate that the follower is
     * now uptodate andt can start responding to clients.
     */
    final static int UPTODATE = 12;

    /**
     * This message is the first that a follower receives from the leader.
     * It has the protocol version and the epoch of the leader.
     * 发送leader信息给learner
     */
    public static final int LEADERINFO = 17;

    /**
     * This message is used by the follow to ack a proposed epoch.
     * 回复LEADERINFO确认
     */
    public static final int ACKEPOCH = 18;
    
    /**
     * This message type is sent to a leader to request and mutation operation.
     * The payload will consist of a request header followed by a request.
     */
    final static int REQUEST = 1;

    /**
     * This message type is sent by a leader to propose a mutation.
     */
    public final static int PROPOSAL = 2;

    /**
     * This message type is sent by a follower after it has synced a proposal.
     * 跟随者完成同步
     */
    final static int ACK = 3;

    /**
     * This message type is sent by a leader to commit a proposal and cause
     * followers to start serving the corresponding data. 提交
     */
    final static int COMMIT = 4;

    /**
     * This message type is enchanged between follower and leader (initiated by
     * follower) to determine liveliness. 心跳
     */
    final static int PING = 5;

    /**
     * This message type is to validate a session that should be active. 验证session
     */
    final static int REVALIDATE = 6;

    /**
     * This message is a reply to a synchronize command flushing the pipe
     * between the leader and the follower. 同步
     */
    final static int SYNC = 7;
        
    /**
     * This message type informs observers of a committed proposal. 通知观察者提交
     */
    final static int INFORM = 8;

    // 保存提交的事务id及其对应的提议，用于等待其他服务返回ack确认，完成事务的提交。同时需要发送这些提议给刚完成同步的
    // follower（还未完成，只是数据保证顺序发送给follower）
    ConcurrentMap<Long, Proposal> outstandingProposals = new ConcurrentHashMap<Long, Proposal>();

    // 保存可以提交的提议，等待commitProcessor处理器处理完后，再由FinalRequestProcessor逐个处理
    ConcurrentLinkedQueue<Proposal> toBeApplied = new ConcurrentLinkedQueue<Proposal>();

    Proposal newLeaderProposal = new Proposal();

    // 等待learner连接线程（每当learner来连接就实例化一个LearnerHandler，用于处理leader和learner之间的通信及逻辑）
    class LearnerCnxAcceptor extends ZooKeeperThread{
        private volatile boolean stop = false; // 线程运行标志

        public LearnerCnxAcceptor() {
            super("LearnerCnxAcceptor-" + ss.getLocalSocketAddress());
        }

        @Override
        public void run() {
            try {
                while (!stop) {
                    try{
                        Socket s = ss.accept(); // 等待learner连接
                        // start with the initLimit, once the ack is processed
                        // in LearnerHandler switch to the syncLimit
                        // 开始使用initLimit，一旦LearnerHandler中ack确认被处理换成syncLimit
                        s.setSoTimeout(self.tickTime * self.initLimit); // 设置socket超时
                        s.setTcpNoDelay(nodelay);

                        BufferedInputStream is = new BufferedInputStream(s.getInputStream());
                        LearnerHandler fh = new LearnerHandler(s, is, Leader.this); // 每当有learner来连接就实例化一个LearnerHandler
                        fh.start(); // 开启LearnerHandler线程
                    } catch (SocketException e) {
                        if (stop) { // Leader.shutdown()时stop被设置为true，accept会抛出SocketException异常
                            LOG.info("exception while shutting down acceptor: " + e);

                            // When Leader.shutdown() calls ss.close(),
                            // the call to accept throws an exception.
                            // We catch and set stop to true.
                            stop = true;
                        } else {
                            throw e;
                        }
                    } catch (SaslException e){ // SaslException异常后重试
                        LOG.error("Exception while connecting to quorum learner", e);
                    }
                }
            } catch (Exception e) {
                LOG.warn("Exception while accepting follower", e);
            }
        }

        // 停止线程
        public void halt() {
            stop = true;
        }
    }

    StateSummary leaderStateSummary; // leader的状态信息（该类封装服务状态用于两个状态相互比较）
    
    long epoch = -1;
    boolean waitingForNewEpoch = true; // 等待新的epoch
    volatile boolean readyToStart = false; // 准备开始lead
    
    /**
     * This method is main function that is called to lead
     * 开始进行lead
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    void lead() throws IOException, InterruptedException {
        self.end_fle = Time.currentElapsedTime(); // 选举结束时间
        long electionTimeTaken = self.end_fle - self.start_fle; // 选举耗费时间
        self.setElectionTimeTaken(electionTimeTaken);
        LOG.info("LEADING - LEADER ELECTION TOOK - {}", electionTimeTaken);
        self.start_fle = 0; // 重置
        self.end_fle = 0;

        zk.registerJMX(new LeaderBean(this, zk), self.jmxLocalPeerBean); // 注册JMX LeaderBean

        try {
            self.tick.set(0); // 初始化tick为0
            zk.loadData(); // 加载内存数据库
            
            leaderStateSummary = new StateSummary(self.getCurrentEpoch(), zk.getLastProcessedZxid());

            // Start thread that waits for connection requests from new followers.
            // 启动线程等待其他learner服务连接
            cnxAcceptor = new LearnerCnxAcceptor();
            cnxAcceptor.start();
            
            readyToStart = true; // 设置准备开始标志
            // 接收到follower或observer的信息后获取提议的epoch（将leader自己的也算在内）
            long epoch = getEpochToPropose(self.getId(), self.getAcceptedEpoch());
            
            zk.setZxid(ZxidUtils.makeZxid(epoch, 0)); // 生成新的事务id（根据新的epoch）
            
            synchronized(this){
                lastProposed = zk.getZxid(); // 设置最后一次的zxid
            }
            
            newLeaderProposal.packet = new QuorumPacket(NEWLEADER, zk.getZxid(), null, null);

            if ((newLeaderProposal.packet.getZxid() & 0xffffffffL) != 0) { // NEWLEADER提议的zxid后32位不为0
                LOG.info("NEWLEADER proposal has Zxid of "
                        + Long.toHexString(newLeaderProposal.packet.getZxid()));
            }
            
            waitForEpochAck(self.getId(), leaderStateSummary); // 等待其他服务进行epoch确认
            self.setCurrentEpoch(epoch); // 设置当前epoch

            // We have to get at least a majority of servers in sync with
            // us. We do this by waiting for the NEWLEADER packet to get
            // acknowledged
            try {
                waitForNewLeaderAck(self.getId(), zk.getZxid()); // 等待其他服务进行NewLeader确认
            } catch (InterruptedException e) {
                shutdown("Waiting for a quorum of followers, only synced with sids: [ "
                        + getSidSetString(newLeaderProposal.ackSet) + " ]"); // 关闭服务
                HashSet<Long> followerSet = new HashSet<Long>();
                for (LearnerHandler f : learners)
                    followerSet.add(f.getSid());

                // follower可以构成仲裁，可能是initTicks时间太短（没有等到ack）
                if (self.getQuorumVerifier().containsQuorum(followerSet)) {
                    LOG.warn("Enough followers present. "
                            + "Perhaps the initTicks need to be increased.");
                }
                Thread.sleep(self.tickTime); // 睡眠tickTime时间后重新选举
                self.tick.incrementAndGet();  // 增加tick
                return;
            }
            
            startZkServer(); // 开始zk服务（处理客户端请求）
            
            /**
             * WARNING: do not use this for anything other than QA testing
             * on a real cluster. Specifically to enable verification that quorum
             * can handle the lower 32bit roll-over issue identified in
             * ZOOKEEPER-1277. Without this option it would take a very long
             * time (on order of a month say) to see the 4 billion writes
             * necessary to cause the roll-over to occur.
             * 
             * This field allows you to override the zxid of the server. Typically
             * you'll want to set it to something like 0xfffffff0 and then
             * start the quorum, run some operations and see the re-election.
             * 测试使用，方便测试zxid达到上限翻转
             */
            String initialZxid = System.getProperty("zookeeper.testingonly.initialZxid"); // 测试时方便设置zxid
            if (initialZxid != null) {
                long zxid = Long.parseLong(initialZxid);
                zk.setZxid((zk.getZxid() & 0xffffffff00000000L) | zxid);
            }
            
            if (!System.getProperty("zookeeper.leaderServes", "yes").equals("no")) { // 没有设置属性zookeeper.leaderServes为no
                self.cnxnFactory.setZooKeeperServer(zk);
            }
            // Everything is a go, simply start counting the ticks
            // WARNING: I couldn't find any wait statement on a synchronized
            // block that would be notified by this notifyAll() call, so
            // I commented it out
            //synchronized (this) {
            //    notifyAll();
            //}
            // We ping twice a tick, so we only update the tick every other iteration
            boolean tickSkip = true; // 是否跳过tick自增（一个tick ping两次）
    
            while (true) {
                Thread.sleep(self.tickTime / 2); // 一个tickTime发送两次ping
                if (!tickSkip) {
                    self.tick.incrementAndGet();
                }
                HashSet<Long> syncedSet = new HashSet<Long>(); // 和leader同步的参与选举的服务

                // lock on the followers when we use it.
                syncedSet.add(self.getId());

                for (LearnerHandler f : getLearners()) {
                    // Synced set is used to check we have a supporting quorum, so only
                    // PARTICIPANT, not OBSERVER, learners should be used
                    if (f.synced() && f.getLearnerType() == LearnerType.PARTICIPANT) { // 和leader同步的follower加入syncedSet集合
                        syncedSet.add(f.getSid());
                    }
                    f.ping(); // 发送ping请求（心跳）给其他服务
                }

                // check leader running status
                // leader没有在运行，关闭服务重新选举
                if (!this.isRunning()) {
                    shutdown("Unexpected internal error");
                    return;
                }

                // 时刻检查和leader同步的follower是否构成一个仲裁集群，如果不能构成就关闭服务，进行重新选举
                if (!tickSkip && !self.getQuorumVerifier().containsQuorum(syncedSet)) {
                  //if (!tickSkip && syncedCount < self.quorumPeers.size() / 2) {
                      // Lost quorum, shutdown
                      shutdown("Not sufficient followers synced, only synced with sids: [ "
                              + getSidSetString(syncedSet) + " ]");
                      // make sure the order is the same!
                      // the leader goes to looking
                      return;
                }
                tickSkip = !tickSkip;
            }
        } finally { // 最后注销JMX
            zk.unregisterJMX(this);
        }
    }

    boolean isShutdown; // LearnerHandlers关闭标志

    /**
     * Close down all the LearnerHandlers
     * 关闭所有LearnerHandlers和当前的服务
     */
    void shutdown(String reason) {
        LOG.info("Shutting down");
        if (isShutdown) {
            return;
        }
        
        LOG.info("Shutdown called", new Exception("shutdown Leader! reason: " + reason));

        if (cnxAcceptor != null) {
            cnxAcceptor.halt(); // 停止learner等待连接线程
        }
        
        // NIO should not accept conenctions
        self.cnxnFactory.setZooKeeperServer(null);
        try {
            ss.close(); // 关闭leader与learner的socket
        } catch (IOException e) {
            LOG.warn("Ignoring unexpected exception during close",e);
        }
        // clear all the connections 清除所有的客户端连接
        self.cnxnFactory.closeAll();
        // shutdown the previous zk
        if (zk != null) {
            zk.shutdown(); // 关闭LeaderZooKeeperServer服务
        }
        synchronized (learners) { // 关闭所有的LearnerHandler
            for (Iterator<LearnerHandler> it = learners.iterator(); it.hasNext();) {
                LearnerHandler f = it.next();
                it.remove();
                f.shutdown();
            }
        }
        isShutdown = true;
    }

    /**
     * Keep a count of acks that are received by the leader for a particular proposal
     * 处理对应提议的ack响应
     *
     * @param sid 服务id（配置文件中myid）
     * @param zxid the zxid of the proposal sent out 事务id
     * @param followerAddr
     */
    synchronized public void processAck(long sid, long zxid, SocketAddress followerAddr) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Ack zxid: 0x{}", Long.toHexString(zxid));
            for (Proposal p : outstandingProposals.values()) {
                long packetZxid = p.packet.getZxid();
                LOG.trace("outstanding proposal: 0x{}", Long.toHexString(packetZxid));
            }
            LOG.trace("outstanding proposals all");
        }

        // 忽略NEWLEADER确认（新版本接收到NEWLEADER时已经发送过ACK确认，但新旧版本在接收UPTODATE后都会发送ACK，
        // 这时新版本的ACK就需要忽略，旧版本的ACK在waitForNewLeaderAck方法中已经处理），NEWLEADER确认的zxid后32位为0
        if ((zxid & 0xffffffffL) == 0) {
            /*
             * We no longer process NEWLEADER ack by this method. However,
             * the learner sends ack back to the leader after it gets UPTODATE
             * so we just ignore the message.
             */
            return;
        }
    
        if (outstandingProposals.size() == 0) { // 没有需要确认的提议
            if (LOG.isDebugEnabled()) {
                LOG.debug("outstanding is 0");
            }
            return;
        }
        if (lastCommitted >= zxid) { // 提议已经提交
            if (LOG.isDebugEnabled()) {
                LOG.debug("proposal has already been committed, pzxid: 0x{} zxid: 0x{}",
                        Long.toHexString(lastCommitted), Long.toHexString(zxid));
            }
            // The proposal has already been committed
            return;
        }
        Proposal p = outstandingProposals.get(zxid); // 从outstandingProposals中取出提议
        if (p == null) {
            LOG.warn("Trying to commit future proposal: zxid 0x{} from {}", Long.toHexString(zxid), followerAddr);
            return;
        }
        
        p.ackSet.add(sid); // 添加ack响应到提议中的ack集合
        if (LOG.isDebugEnabled()) {
            LOG.debug("Count for zxid: 0x{} is {}", Long.toHexString(zxid), p.ackSet.size());
        }
        if (self.getQuorumVerifier().containsQuorum(p.ackSet)) { // 接收到ack满足选举原则（如过半）
            if (zxid != lastCommitted + 1) {
                LOG.warn("Commiting zxid 0x{} from {} not first!", Long.toHexString(zxid), followerAddr);
                LOG.warn("First is 0x{}", Long.toHexString(lastCommitted + 1));
            }
            outstandingProposals.remove(zxid); // 从等待ack确认的map中移除提议
            if (p.request != null) {
                toBeApplied.add(p); // 将提议加入toBeApplied队列
            }

            if (p.request == null) {
                LOG.warn("Going to commit null request for proposal: {}", p);
            }
            commit(zxid); // 通知所有follower进行提交
            inform(p); // 通知所有观察者
            zk.commitProcessor.commit(p.request); // 将请求提交给commitProcessor处理器
            // 如果存在zxid对应的同步请求被挂起，发送同步请求给对应的learner服务（确保接收到sync时的事务id被提交，
            // 现在可以发送SYNC信息给learner，当前的数据已经和leader一致了）
            if (pendingSyncs.containsKey(zxid)) {
                for(LearnerSyncRequest r : pendingSyncs.remove(zxid)) {
                    sendSync(r); // 发送SYNC给learner
                }
            }
        }
    }

    // 该处理器维护一个toBeApplied队列，保存可以提交的提议，等待commitProcessor处理器处理完后，
    // 然后调用FinalRequestProcessor的processRequest方法进行处理，处理完之后移除该提议
    static class ToBeAppliedRequestProcessor implements RequestProcessor {
        private RequestProcessor next; // 下一个请求处理器
        private ConcurrentLinkedQueue<Proposal> toBeApplied; // 保存可以提交的提议等待commitProcessor处理

        /**
         * This request processor simply maintains the toBeApplied list. For
         * this to work next must be a FinalRequestProcessor and
         * FinalRequestProcessor.processRequest MUST process the request
         * synchronously!
         * 
         * @param next
         *                a reference to the FinalRequestProcessor
         */
        ToBeAppliedRequestProcessor(RequestProcessor next, ConcurrentLinkedQueue<Proposal> toBeApplied) {
            if (!(next instanceof FinalRequestProcessor)) { // 如果下一个请求处理器不是FinalRequestProcessor，抛出异常
                throw new RuntimeException(ToBeAppliedRequestProcessor.class.getName()
                        + " must be connected to "
                        + FinalRequestProcessor.class.getName()
                        + " not "
                        + next.getClass().getName());
            }
            this.toBeApplied = toBeApplied;
            this.next = next;
        }

        /*
         * (non-Javadoc)
         * 调用FinalRequestProcessor的processRequest方法，处理完后将提议移除
         * 
         * @see org.apache.zookeeper.server.RequestProcessor#processRequest(org.apache.zookeeper.server.Request)
         */
        public void processRequest(Request request) throws RequestProcessorException {
            // request.addRQRec(">tobe");
            next.processRequest(request);
            Proposal p = toBeApplied.peek();
            if (p != null && p.request != null && p.request.zxid == request.zxid) {
                toBeApplied.remove();
            }
        }

        /*
         * (non-Javadoc)
         * 关闭请求处理器
         * 
         * @see org.apache.zookeeper.server.RequestProcessor#shutdown()
         */
        public void shutdown() {
            LOG.info("Shutting down");
            next.shutdown();
        }
    }

    /**
     * send a packet to all the followers ready to follow
     * 发送数据包给所有准备好的follower
     * 
     * @param qp
     *                the packet to be sent
     */
    void sendPacket(QuorumPacket qp) {
        synchronized (forwardingFollowers) { // 使用同步防止此时forwardingFollowers中添加提议导致提议丢失不能发送给follower
            for (LearnerHandler f : forwardingFollowers) {
                f.queuePacket(qp); // 添加到LearnerHandler的发送队列
            }
        }
    }
    
    /**
     * send a packet to all observers
     * 发送数据包给所有准备好的观察者
     */
    void sendObserverPacket(QuorumPacket qp) {        
        for (LearnerHandler f : getObservingLearners()) {
            f.queuePacket(qp);
        }
    }

    long lastCommitted = -1; // 最后一次commit的事务id

    /**
     * Create a commit packet and send it to all the members of the quorum
     * 创建一个commit数据包发送给准备好的follower
     * 
     * @param zxid
     */
    public void commit(long zxid) {
        synchronized(this){
            lastCommitted = zxid; // 记录最后一次commit的事务id
        }
        QuorumPacket qp = new QuorumPacket(Leader.COMMIT, zxid, null, null);
        sendPacket(qp); // 发送给所有准备好的follower
    }
    
    /**
     * Create an inform packet and send it to all observers.
     * 创建一个inform数据包发送给所有观察者
     *
     * @param proposal
     */
    public void inform(Proposal proposal) {   
        QuorumPacket qp = new QuorumPacket(Leader.INFORM, proposal.request.zxid, proposal.packet.getData(), null);
        sendObserverPacket(qp);
    }

    long lastProposed; // 最后一次提议的zxid

    /**
     * Returns the current epoch of the leader.
     * 返回当前leader的epoch
     * 
     * @return
     */
    public long getEpoch(){
        return ZxidUtils.getEpochFromZxid(lastProposed);
    }

    // zxid用完异常（要发生翻转了，超过能表示的最大值）
    @SuppressWarnings("serial")
    public static class XidRolloverException extends Exception {
        public XidRolloverException(String message) {
            super(message);
        }
    }

    /**
     * create a proposal and send it out to all the members
     * 创建一个提议（事务）并发送给所有准备好的follower
     * 
     * @param request
     * @return the proposal that is queued to send to all the members
     */
    public Proposal propose(Request request) throws XidRolloverException {
        /**
         * Address the rollover issue. All lower 32bits set indicate a new leader
         * election. Force a re-election instead. See ZOOKEEPER-1277
         */
        if ((request.zxid & 0xffffffffL) == 0xffffffffL) { // 如果zxid的低32位已经用到最大值了，强制重新选举，这样开启新的epoch
            String msg =
                    "zxid lower 32 bits have rolled over, forcing re-election, and therefore new epoch start";
            shutdown(msg); // 关闭当前服务
            throw new XidRolloverException(msg);
        }
        byte[] data = SerializeUtils.serializeRequest(request); // 序列化请求
        proposalStats.setLastProposalSize(data.length); // 设置最近一次请求的数据长度
        QuorumPacket pp = new QuorumPacket(Leader.PROPOSAL, request.zxid, data, null); // 生成提议数据包
        
        Proposal p = new Proposal();
        p.packet = pp;
        p.request = request;
        synchronized (this) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Proposing:: " + request);
            }

            lastProposed = p.packet.getZxid();
            outstandingProposals.put(lastProposed, p);
            sendPacket(pp); // 给所有准备好的follower发送PROPOSAL数据包，只是添加到LearnerHandler的发送队列
        }
        return p;
    }
            
    /**
     * Process sync requests 处理同步请求
     * 
     * @param r the request
     */
    synchronized public void processSync(LearnerSyncRequest r) {
        if (outstandingProposals.isEmpty()) { // 如果没有等待响应的提议，直接发送同步消息
            sendSync(r); // 发送同步消息
        } else { // 否则将该同步消息先加入挂起队列，等收到对应此时zxid（lastProposed）的ack时再发送（保证接收到sync之前的请求都已提交，这样能获取到跟leader一样最新的状态）
            List<LearnerSyncRequest> l = pendingSyncs.get(lastProposed);
            if (l == null) {
                l = new ArrayList<LearnerSyncRequest>();
            }
            l.add(r);
            pendingSyncs.put(lastProposed, l); // 添加到挂起同步队列
        }
    }

    /**
     * Sends a sync message to the appropriate server
     * 发送同步消息给相应的learner服务
     * 
     * @param r
     */
    public void sendSync(LearnerSyncRequest r) {
        QuorumPacket qp = new QuorumPacket(Leader.SYNC, 0, null, null);
        r.fh.queuePacket(qp);
    }
                
    /**
     * lets the leader know that a follower is capable of following and is done
     * syncing
     * 通知leader，learner已经准备好了并且完成同步（同步的提议已经加入发送队列，
     * 将同步区间leader接收到客户端的请求提议发送发送给learner）
     * 
     * @param handler handler of the follower
     * @return last proposed zxid
     */
    synchronized public long startForwarding(LearnerHandler handler, long lastSeenZxid) {
        // Queue up any outstanding requests enabling the receipt of
        // new requests
        if (lastProposed > lastSeenZxid) { // 当前leader的zxid比其他learner的更新
            for (Proposal p : toBeApplied) { // 将同步时间段提交的事务也加入发送队列一起同步
                if (p.packet.getZxid() <= lastSeenZxid) { // 跳过zxid <= lastSeenZxid的提议（在lastSeenZxid之前的Proposal）
                    continue;
                }
                handler.queuePacket(p.packet); // 加入发送队列（LearnerHandler的queuedPackets）
                // Since the proposal has been committed we need to send the
                // commit message also 同时生成commit消息，加入队列
                QuorumPacket qp = new QuorumPacket(Leader.COMMIT, p.packet.getZxid(), null, null);
                handler.queuePacket(qp);
            }
            // Only participant need to get outstanding proposals
            // 这些提议还没有收到ack，所以需要发送给follower，不需要发送给观察者，在leader接收到过半的ack后会commit提议，这时会发送给观察者
            if (handler.getLearnerType() == LearnerType.PARTICIPANT) { // 处理zxid小于lastSeenZxid的outstandingProposals
                List<Long> zxids = new ArrayList<Long>(outstandingProposals.keySet());
                Collections.sort(zxids);
                for (Long zxid : zxids) {
                    if (zxid <= lastSeenZxid) {
                        continue;
                    }
                    handler.queuePacket(outstandingProposals.get(zxid).packet);
                }
            }
        }
        // follower完成同步，添加到准备好的队列中，这样以后的提议会发送给forwardingFollowers的所有跟随者（该方法使用同步确保同步区间
        // 接收的提议都能被正确处理发送给learner）
        if (handler.getLearnerType() == LearnerType.PARTICIPANT) {
            addForwardingFollower(handler);
        } else { // 观察者完成同步，添加到准备好的观察者集合中
            addObserverLearnerHandler(handler);
        }

        return lastProposed;
    }

    // VisibleForTesting
    protected Set<Long> connectingFollowers = new HashSet<Long>(); // 已经连接的follower列表

    // 接收到跟随者或观察者的FOLLOWERINFO或OBSERVERINFO消息，根据接收的lastAcceptedEpoch和当前的epoch比较，记录最大的epoch
    public long getEpochToPropose(long sid, long lastAcceptedEpoch) throws InterruptedException, IOException {
        synchronized(connectingFollowers) {
            if (!waitingForNewEpoch) { // 根据learner信息，已经决议出epoch，返回现在的epoch，否则等待新的epoch
                return epoch;
            }
            if (lastAcceptedEpoch >= epoch) { // 根据接收到learner的acceptedEpoch，记录最大的epoch
                epoch = lastAcceptedEpoch + 1;
            }
            if (isParticipant(sid)) { // 参与选举的服务
                connectingFollowers.add(sid);
            }
            QuorumVerifier verifier = self.getQuorumVerifier();
            if (connectingFollowers.contains(self.getId()) && 
                                            verifier.containsQuorum(connectingFollowers)) { // 新的epoch产生
                waitingForNewEpoch = false;
                self.setAcceptedEpoch(epoch);
                connectingFollowers.notifyAll(); // 唤醒connectingFollowers
            } else { // 否则等待connectingFollowers被唤醒
                long start = Time.currentElapsedTime();
                long cur = start;
                long end = start + self.getInitLimit() * self.getTickTime(); // 等待结束时间
                while(waitingForNewEpoch && cur < end) { // 等待connectingFollowers
                    connectingFollowers.wait(end - cur);
                    cur = Time.currentElapsedTime();
                }
                if (waitingForNewEpoch) { // waitingForNewEpoch没有被置为false
                    throw new InterruptedException("Timeout while waiting for epoch from quorum");        
                }
            }
            return epoch;
        }
    }

    // VisibleForTesting
    protected Set<Long> electingFollowers = new HashSet<Long>(); // 收集follower返回的ACKEPOCH确认
    // VisibleForTesting
    protected boolean electionFinished = false; // 是否收到过半的ACKEPOCH确认

    // 等待LEADERINFO的ACKEPOCH确认
    public void waitForEpochAck(long id, StateSummary ss) throws IOException, InterruptedException {
        synchronized(electingFollowers) {
            if (electionFinished) { // 已经收到过半的ACKEPOCH确认
                return;
            }
            // -1表示learner服务和leader的epoch相等，leader已经确认过了，不再计入ack计算（例如在该阶段一个follower挂了，
            // 重启后又到这个阶段，但是之前已经确认过了）
            if (ss.getCurrentEpoch() != -1) {
                if (ss.isMoreRecentThan(leaderStateSummary)) { // learner的状态比leader更靠前，即leader过期，抛出异常重新开始
                    throw new IOException("Follower is ahead of the leader, leader summary: " 
                                                    + leaderStateSummary.getCurrentEpoch()
                                                    + " (current epoch), "
                                                    + leaderStateSummary.getLastZxid()
                                                    + " (last zxid)");
                }
                if (isParticipant(id)) { // 加入electingFollowers列表
                    electingFollowers.add(id);
                }
            }
            QuorumVerifier verifier = self.getQuorumVerifier();
            // leader收到过半的ACKEPOCH确认
            if (electingFollowers.contains(self.getId()) && verifier.containsQuorum(electingFollowers)) {
                electionFinished = true; // 收集ACKEPOCH确认完成
                electingFollowers.notifyAll(); // 唤醒等待的electingFollowers
            } else { // 等待electingFollowers被唤醒
                long start = Time.currentElapsedTime();
                long cur = start;
                long end = start + self.getInitLimit() * self.getTickTime();
                while(!electionFinished && cur < end) {
                    electingFollowers.wait(end - cur); // 等待electingFollowers被唤醒
                    cur = Time.currentElapsedTime();
                }
                if (!electionFinished) {
                    throw new InterruptedException("Timeout while waiting for epoch to be acked by quorum");
                }
            }
        }
    }

    /**
     * Return a list of sid in set as string
     * 返回服务集合id的字符串
     */
    private String getSidSetString(Set<Long> sidSet) {
        StringBuilder sids = new StringBuilder();
        Iterator<Long> iter = sidSet.iterator();
        while (iter.hasNext()) {
            sids.append(iter.next());
            if (!iter.hasNext()) {
              break;
            }
            sids.append(",");
        }
        return sids.toString();
    }

    /**
     * Start up Leader ZooKeeper server and initialize zxid to the new epoch
     * 启动zk服务并且初始化zxid和epoch
     */
    private synchronized void startZkServer() {
        // Update lastCommitted and Db's zxid to a value representing the new epoch
        lastCommitted = zk.getZxid();
        LOG.info("Have quorum of supporters, sids: [ "
                + getSidSetString(newLeaderProposal.ackSet)
                + " ]; starting up and setting last processed zxid: 0x{}",
                Long.toHexString(zk.getZxid()));
        zk.startup(); // 启动zk服务（提供客户端连接以及请求处理）
        /*
         * Update the election vote here to ensure that all members of the
         * ensemble report the same vote to new servers that start up and
         * send leader election notifications to the ensemble.
         * 
         * @see https://issues.apache.org/jira/browse/ZOOKEEPER-1732
         */
        self.updateElectionVote(getEpoch()); // 更新该服务当前的选票epoch为当前的epoch

        zk.getZKDatabase().setlastProcessedZxid(zk.getZxid()); // 设置内存数据库的lastProcessedZxid
    }

    /**
     * Process NEWLEADER ack of a given sid and wait until the leader receives
     * sufficient acks. 处理给定服务id的NEWLEADER确认，等待直到收到足够的ack确认
     *
     * @param sid
     * @throws InterruptedException
     */
    // 等待NewLeader的ACK确认（新版本的learner服务收到NEWLEADER会进行数据快照，完成后发送ACK确认）
    public void waitForNewLeaderAck(long sid, long zxid) throws InterruptedException {
        synchronized (newLeaderProposal.ackSet) {
            if (quorumFormed) { // 构成仲裁集群直接返回
                return;
            }

            long currentZxid = newLeaderProposal.packet.getZxid();
            if (zxid != currentZxid) { // 回复NEWLEADER确认的服务zxid和当前的zxid不相等
                LOG.error("NEWLEADER ACK from sid: " + sid
                        + " is from a different epoch - current 0x"
                        + Long.toHexString(currentZxid) + " receieved 0x"
                        + Long.toHexString(zxid));
                return;
            }

            if (isParticipant(sid)) { // 服务sid加入确认集合
                newLeaderProposal.ackSet.add(sid);
            }

            if (self.getQuorumVerifier().containsQuorum(newLeaderProposal.ackSet)) { // 数量达到仲裁条件（如过半）
                quorumFormed = true; // 构成仲裁
                newLeaderProposal.ackSet.notifyAll(); // 唤醒等待newLeaderProposal.ackSet的线程
            } else { // 等待
                long start = Time.currentElapsedTime();
                long cur = start;
                long end = start + self.getInitLimit() * self.getTickTime();
                while (!quorumFormed && cur < end) { // 等待newLeaderProposal.ackSet被唤醒
                    newLeaderProposal.ackSet.wait(end - cur);
                    cur = Time.currentElapsedTime();
                }
                if (!quorumFormed) {
                    throw new InterruptedException(
                            "Timeout while waiting for NEWLEADER to be acked by quorum");
                }
            }
        }
    }

    /**
     * Get string representation of a given packet type
     * 获取packet类型对应的字符串类型
     * @param packetType
     * @return string representing the packet type
     */
    public static String getPacketType(int packetType) {
        switch (packetType) {
        case DIFF:
            return "DIFF";
        case TRUNC:
            return "TRUNC";
        case SNAP:
            return "SNAP";
        case OBSERVERINFO:
            return "OBSERVERINFO";
        case NEWLEADER:
            return "NEWLEADER";
        case FOLLOWERINFO:
            return "FOLLOWERINFO";
        case UPTODATE:
            return "UPTODATE";
        case LEADERINFO:
            return "LEADERINFO";
        case ACKEPOCH:
            return "ACKEPOCH";
        case REQUEST:
            return "REQUEST";
        case PROPOSAL:
            return "PROPOSAL";
        case ACK:
            return "ACK";
        case COMMIT:
            return "COMMIT";
        case PING:
            return "PING";
        case REVALIDATE:
            return "REVALIDATE";
        case SYNC:
            return "SYNC";
        case INFORM:
            return "INFORM";
        default:
            return "UNKNOWN";
        }
    }

    // 判断leader是否运行
    private boolean isRunning() {
        return self.isRunning() && zk.isRunning();
    }

    // 判断服务sid是否参与选举
    private boolean isParticipant(long sid) {
        return self.getVotingView().containsKey(sid);
    }
}
