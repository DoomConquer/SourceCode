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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.zookeeper.common.Time;
import org.apache.zookeeper.jmx.MBeanRegistry;
import org.apache.zookeeper.server.ZooKeeperThread;
import org.apache.zookeeper.server.quorum.QuorumCnxManager.Message;
import org.apache.zookeeper.server.quorum.QuorumPeer.LearnerType;
import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.apache.zookeeper.server.quorum.QuorumPeer.ServerState;
import org.apache.zookeeper.server.util.ZxidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of leader election using TCP. It uses an object of the class
 * QuorumCnxManager to manage connections. Otherwise, the algorithm is push-based
 * as with the other UDP implementations.
 * leader选举（使用QuorumCnxManager管理连接）
 *
 * There are a few parameters that can be tuned（调优） to change its behavior. First,
 * finalizeWait determines the amount of time to wait until deciding upon a leader.
 * This is part of the leader election algorithm.
 * 一些参数可以用于调优，例如，finalizeWait用于决定一个leader的等待时间
 */
public class FastLeaderElection implements Election {
    private static final Logger LOG = LoggerFactory.getLogger(FastLeaderElection.class);

    /**
     * Determine how much time a process has to wait
     * once it believes that it has reached the end of
     * leader election.
     */
    final static int finalizeWait = 200; // 决定一个leader的等待时间（防止消息交错导致一个服务器选择另一个leader，最后导致整个服务重新选举，浪费时间）

    /**
     * Upper bound on the amount of time between two consecutive
     * notification checks. This impacts the amount of time to get
     * the system up again after long partitions. Currently 60 seconds.
     */
    final static int maxNotificationInterval = 60000; // 两个连续通知确认之间的最大时间间隔，默认60秒

    /**
     * Connection manager. Fast leader election uses TCP for
     * communication between peers, and QuorumCnxManager manages
     * such connections.
     */
    QuorumCnxManager manager; // 选举通信TCP连接管理

    /**
     * Notifications are messages that let other peers know that
     * a given peer has changed its vote, either because it has
     * joined leader election or because it learned of another
     * peer with higher zxid or same zxid and higher server id
     * 通知其他服务（peer）选票变更（刚加入选举过程或当前选票的zxid或服务id比其他peer的小，需要改变自己的选票）
     */
    static public class Notification {

        /*
         * Format version, introduced in 3.4.6
         */
        public final static int CURRENTVERSION = 0x1; 

        int version;
                
        /*
         * Proposed leader
         */
        long leader; // 提议leader的服务id

        /*
         * zxid of the proposed leader
         */
        long zxid; // 提议leader的zxid

        /*
         * Epoch
         */
        long electionEpoch; // 当前选举时钟周期（选举轮次）

        /*
         * current state of sender
         */
        QuorumPeer.ServerState state; // 发送方服务状态

        /*
         * Address of sender
         */
        long sid; // 发送方服务id

        /*
         * epoch of the proposed leader
         */
        long peerEpoch; // 提议leader的epoch

        // 通知信息打印
        @Override
        public String toString() {
            return Long.toHexString(version) + " (message format version), "
                    + leader + " (n.leader), 0x"
                    + Long.toHexString(zxid) + " (n.zxid), 0x"
                    + Long.toHexString(electionEpoch) + " (n.round), " + state
                    + " (n.state), " + sid + " (n.sid), 0x"
                    + Long.toHexString(peerEpoch) + " (n.peerEpoch) ";
        }
    }

    // 构造要发送的通知（Notification）
    static ByteBuffer buildMsg(int state,
            long leader,
            long zxid,
            long electionEpoch,
            long epoch) {
        byte requestBytes[] = new byte[40];
        ByteBuffer requestBuffer = ByteBuffer.wrap(requestBytes);

        /*
         * Building notification packet to send 
         */
        requestBuffer.clear();
        requestBuffer.putInt(state);
        requestBuffer.putLong(leader);
        requestBuffer.putLong(zxid);
        requestBuffer.putLong(electionEpoch);
        requestBuffer.putLong(epoch);
        requestBuffer.putInt(Notification.CURRENTVERSION);
        
        return requestBuffer;
    }

    /**
     * Messages that a peer wants to send to other peers.
     * These messages can be both Notifications and Acks
     * of reception of notification.
     * 待发送消息
     */
    static public class ToSend {
        // 消息类型
        static enum mType {crequest, challenge, notification, ack}

        ToSend(mType type,
                long leader,
                long zxid,
                long electionEpoch,
                ServerState state,
                long sid,
                long peerEpoch) {
            this.leader = leader;
            this.zxid = zxid;
            this.electionEpoch = electionEpoch;
            this.state = state;
            this.sid = sid;
            this.peerEpoch = peerEpoch;
        }

        /*
         * Proposed leader in the case of notification
         */
        long leader;

        /*
         * id contains the tag for acks, and zxid for notifications
         */
        long zxid;

        /*
         * Epoch
         */
        long electionEpoch;

        /*
         * Current state;
         */
        QuorumPeer.ServerState state;

        /*
         * Address of recipient
         * 接收者的服务id
         */
        long sid;
        
        /*
         * Leader epoch
         */
        long peerEpoch;
    }

    LinkedBlockingQueue<ToSend> sendqueue;       // 发送消息队列
    LinkedBlockingQueue<Notification> recvqueue; // 接收通知对列

    /**
     * Multi-threaded implementation of message handler. Messenger
     * implements two sub-classes: WorkReceiver and  WorkSender. The
     * functionality of each is obvious from the name. Each of these
     * spawns（产生） a new thread.
     * 多线程消息处理，Messenger中包含两个子类WorkReceiver和WorkSender，用于发送和接收消息
     */
    protected class Messenger {

        /**
         * Receives messages from instance of QuorumCnxManager on
         * method run(), and processes such messages.
         * 接收消息线程，从QuorumCnxManager接收消息并处理
         */
        class WorkerReceiver extends ZooKeeperThread {
            volatile boolean stop; // 线程停止标志
            QuorumCnxManager manager;

            WorkerReceiver(QuorumCnxManager manager) {
                super("WorkerReceiver");
                this.stop = false;
                this.manager = manager;
            }

            public void run() {
                Message response; // 响应消息
                while (!stop) {
                    // Sleeps on receive
                    try{
                        response = manager.pollRecvQueue(3000, TimeUnit.MILLISECONDS); // 从QuorumCnxManager接收消息
                        if(response == null) continue; // 继续

                        /*
                         * If it is from an observer, respond right away.
                         * Note that the following predicate assumes that
                         * if a server is not a follower, then it must be
                         * an observer. If we ever have any other type of
                         * learner in the future, we'll have to change the
                         * way we check for observers.（假设一个服务不是follower就认为其是observer）
                         */
                        if(!validVoter(response.sid)){ // 如果该服务不在参与选举的服务中（默认是观察者）
                            Vote current = self.getCurrentVote(); // 获取当前选票
                            ToSend notmsg = new ToSend(ToSend.mType.notification,
                                    current.getId(),
                                    current.getZxid(),
                                    logicalclock.get(),
                                    self.getPeerState(),
                                    response.sid,
                                    current.getPeerEpoch());

                            sendqueue.offer(notmsg); // 发送当前选票给观察者
                        } else { // 参与选举的服务
                            // Receive new message 接收新的消息
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Receive new notification message. My id = " + self.getId());
                            }

                            /*
                             * We check for 28 bytes for backward compatibility
                             */
                            if (response.buffer.capacity() < 28) { // 检验数据长度
                                LOG.error("Got a short response: " + response.buffer.capacity());
                                continue;
                            }
                            boolean backCompatibility = (response.buffer.capacity() == 28); // 是否是之前版本（向后兼容）
                            response.buffer.clear(); // position = 0，limit = capacity，并没有清空数据

                            // Instantiate Notification and set its attributes
                            Notification n = new Notification();
                            
                            // State of peer that sent this message
                            QuorumPeer.ServerState ackstate = QuorumPeer.ServerState.LOOKING;
                            switch (response.buffer.getInt()) { // 读取发送消息的服务状态（int型）
                            case 0:
                                ackstate = QuorumPeer.ServerState.LOOKING;
                                break;
                            case 1:
                                ackstate = QuorumPeer.ServerState.FOLLOWING;
                                break;
                            case 2:
                                ackstate = QuorumPeer.ServerState.LEADING;
                                break;
                            case 3:
                                ackstate = QuorumPeer.ServerState.OBSERVING;
                                break;
                            default:
                                continue;
                            }

                            // 读取接收的Message信息
                            n.leader = response.buffer.getLong();
                            n.zxid = response.buffer.getLong();
                            n.electionEpoch = response.buffer.getLong();
                            n.state = ackstate;
                            n.sid = response.sid;
                            if(!backCompatibility) { // 新版
                                n.peerEpoch = response.buffer.getLong(); // 读取epoch
                            } else {
                                if(LOG.isInfoEnabled()){
                                    LOG.info("Backward compatibility mode, server id=" + n.sid);
                                }
                                n.peerEpoch = ZxidUtils.getEpochFromZxid(n.zxid); // 从zxid中取epoch
                            }

                            /*
                             * Version added in 3.4.6
                             */
                            n.version = (response.buffer.remaining() >= 4) ? response.buffer.getInt() : 0x0;

                            /*
                             * Print notification info
                             * 打印通知信息
                             */
                            if(LOG.isInfoEnabled()){
                                printNotification(n);
                            }

                            /*
                             * If this server is looking, then send proposed leader
                             * 如果该服务正在进行选举，发送提议的leader
                             */
                            if(self.getPeerState() == QuorumPeer.ServerState.LOOKING){
                                recvqueue.offer(n); // 添加接收到的消息到接收通知对列

                                /*
                                 * Send a notification back if the peer that sent this
                                 * message is also looking and its logical clock is
                                 * lagging behind.
                                 * 如果peer状态也是looking且逻辑时钟落后当前逻辑时钟，发送一个通知回去
                                 */
                                if((ackstate == QuorumPeer.ServerState.LOOKING)
                                        && (n.electionEpoch < logicalclock.get())){ // 如果消息发送服务的选举轮次落后
                                    Vote v = getVote();
                                    ToSend notmsg = new ToSend(ToSend.mType.notification,
                                            v.getId(),
                                            v.getZxid(),
                                            logicalclock.get(),
                                            self.getPeerState(),
                                            response.sid,
                                            v.getPeerEpoch()); // 通知消息
                                    sendqueue.offer(notmsg); // 加入发送队列
                                }
                            } else {
                                /*
                                 * If this server is not looking, but the one that sent the ack
                                 * is looking, then send back what it believes to be the leader.
                                 * 如果该服务没有处于looking状态，但是接收到消息的服务仍在looking状态，发送
                                 * 当前服务的选票回去（例如一个新的节点要加入一个运行的集群，原集群的服务都在运行，
                                 * 或者该服务已经完成选举变成follower或leader）
                                 */
                                Vote current = self.getCurrentVote(); // 获取当前选票
                                if(ackstate == QuorumPeer.ServerState.LOOKING){ // 如果接收到消息的服务处于looking状态
                                    if(LOG.isDebugEnabled()){
                                        LOG.debug("Sending new notification. My id =  " +
                                                self.getId() + " recipient=" +
                                                response.sid + " zxid=0x" +
                                                Long.toHexString(current.getZxid()) +
                                                " leader=" + current.getId());
                                    }
                                    
                                    ToSend notmsg;
                                    if(n.version > 0x0) { // 如果选票版本大于0（新版）
                                        notmsg = new ToSend(
                                                ToSend.mType.notification,
                                                current.getId(),
                                                current.getZxid(),
                                                current.getElectionEpoch(),
                                                self.getPeerState(),
                                                response.sid,
                                                current.getPeerEpoch());
                                    } else {
                                        Vote bcVote = self.getBCVote(); // 获取之前版本的选票
                                        notmsg = new ToSend(
                                                ToSend.mType.notification,
                                                bcVote.getId(),
                                                bcVote.getZxid(),
                                                bcVote.getElectionEpoch(),
                                                self.getPeerState(),
                                                response.sid,
                                                bcVote.getPeerEpoch());
                                    }
                                    sendqueue.offer(notmsg); // 加入发送队列
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted Exception while waiting for new message" + e.toString());
                    }
                }
                LOG.info("WorkerReceiver is down"); // 接收线程结束
            }
        }


        /**
         * This worker simply dequeues a message to send and
         * and queues it on the manager's queue.
         * 消息发送线程
         */
        class WorkerSender extends ZooKeeperThread {
            volatile boolean stop; // 线程停止标志
            QuorumCnxManager manager;

            WorkerSender(QuorumCnxManager manager) {
                super("WorkerSender");
                this.stop = false;
                this.manager = manager;
            }

            public void run() {
                while (!stop) {
                    try {
                        ToSend m = sendqueue.poll(3000, TimeUnit.MILLISECONDS); // 取出待发送消息
                        if(m == null) continue;

                        process(m); // 发送
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                LOG.info("WorkerSender is down");
            }

            /**
             * Called by run() once there is a new message to send.
             *
             * @param m     message to send
             */
            void process(ToSend m) {
                ByteBuffer requestBuffer = buildMsg(m.state.ordinal(), 
                                                        m.leader,
                                                        m.zxid, 
                                                        m.electionEpoch, 
                                                        m.peerEpoch);
                manager.toSend(m.sid, requestBuffer); // 发送
            }
        }

        WorkerSender ws;   // 发送消息线程
        WorkerReceiver wr; // 接收消息线程

        /**
         * Constructor of class Messenger.
         * 构造Messenger，启动发送和接收线程
         *
         * @param manager   Connection manager
         */
        Messenger(QuorumCnxManager manager) {
            this.ws = new WorkerSender(manager);
            Thread t = new Thread(this.ws, "WorkerSender[myid=" + self.getId() + "]");
            t.setDaemon(true);
            t.start();

            this.wr = new WorkerReceiver(manager);
            t = new Thread(this.wr, "WorkerReceiver[myid=" + self.getId() + "]");
            t.setDaemon(true);
            t.start();
        }

        /**
         * Stops instances of WorkerSender and WorkerReceiver
         * 停止发送和接收线程
         */
        void halt(){
            this.ws.stop = true;
            this.wr.stop = true;
        }
    }

    QuorumPeer self; // 选举管理类
    Messenger messenger;
    AtomicLong logicalclock = new AtomicLong(); /* Election instance  逻辑时钟（选举轮次）*/
    long proposedLeader; // 提议的leader服务id
    long proposedZxid;   // 提议的leader事务id
    long proposedEpoch;  // 当前epoch

    /**
     * Returns the current vlue of the logical clock counter
     * 获取逻辑时钟
     */
    public long getLogicalClock(){
        return logicalclock.get();
    }

    /**
     * Constructor of FastLeaderElection. It takes two parameters, one
     * is the QuorumPeer object that instantiated this object, and the other
     * is the connection manager. Such an object should be created only once
     * by each peer during an instance of the ZooKeeper service.
     *
     * @param self  QuorumPeer that created this object 选举管理类
     * @param manager   Connection manager 选举通信连接管理类
     */
    public FastLeaderElection(QuorumPeer self, QuorumCnxManager manager){
        this.stop = false;
        this.manager = manager;
        starter(self, manager); // 实例化变量
    }

    /**
     * This method is invoked by the constructor. Because it is a
     * part of the starting procedure of the object that must be on
     * any constructor of this class, it is probably best to keep as
     * a separate method. As we have a single constructor currently,
     * it is not strictly necessary to have it separate.
     *
     * @param self      QuorumPeer that created this object
     * @param manager   Connection manager
     */
    private void starter(QuorumPeer self, QuorumCnxManager manager) {
        this.self = self;
        proposedLeader = -1;
        proposedZxid = -1;

        // 实例化消息发送接收队列
        sendqueue = new LinkedBlockingQueue<ToSend>();
        recvqueue = new LinkedBlockingQueue<Notification>();
        this.messenger = new Messenger(manager);
    }

    // 结束选举
    private void leaveInstance(Vote v) {
        if(LOG.isDebugEnabled()){
            LOG.debug("About to leave FLE instance: leader="
                + v.getId() + ", zxid=0x" +
                Long.toHexString(v.getZxid()) + ", my id=" + self.getId()
                + ", my state=" + self.getPeerState());
        }
        recvqueue.clear(); // 清空接收队列
    }

    public QuorumCnxManager getCnxManager(){
        return manager;
    }

    volatile boolean stop; // 选举是否停止

    // 停止选举
    public void shutdown(){
        stop = true;
        LOG.debug("Shutting down connection manager");
        manager.halt();   // 停止通信连接
        LOG.debug("Shutting down messenger");
        messenger.halt(); // 停止消息发送接收
        LOG.debug("FLE is down");
    }

    /**
     * Send notifications to all peers upon a change in our vote
     * 给所有的参与选举的服务发送选票
     */
    private void sendNotifications() {
        for (QuorumServer server : self.getVotingView().values()) {
            long sid = server.id;

            ToSend notmsg = new ToSend(ToSend.mType.notification,
                    proposedLeader,
                    proposedZxid,
                    logicalclock.get(),
                    QuorumPeer.ServerState.LOOKING,
                    sid,
                    proposedEpoch);
            if(LOG.isDebugEnabled()){
                LOG.debug("Sending Notification: " + proposedLeader + " (n.leader), 0x"  +
                      Long.toHexString(proposedZxid) + " (n.zxid), 0x" + Long.toHexString(logicalclock.get())  +
                      " (n.round), " + sid + " (recipient), " + self.getId() +
                      " (myid), 0x" + Long.toHexString(proposedEpoch) + " (n.peerEpoch)");
            }
            sendqueue.offer(notmsg); // 加入发送队列
        }
    }

    // 打印通知信息
    private void printNotification(Notification n){
        LOG.info("Notification: " + n.toString() + self.getPeerState() + " (my state)");
    }

    /**
     * Check if a pair (server id, zxid) succeeds our current vote.
     * 比较（newId，newZxid，newEpoch）和（curId，curZxid，curEpoch），决定选举哪个为leader
     */
    protected boolean totalOrderPredicate(long newId, long newZxid, long newEpoch, long curId, long curZxid, long curEpoch) {
        LOG.debug("id: " + newId + ", proposed id: " + curId + ", zxid: 0x" +
                Long.toHexString(newZxid) + ", proposed zxid: 0x" + Long.toHexString(curZxid));
        if(self.getQuorumVerifier().getWeight(newId) == 0){ // 该服务的权重为0，不参与
            return false;
        }
        
        /*
         * We return true if one of the following three cases hold:
         * 1- New epoch is higher
         * 2- New epoch is the same as current epoch, but new zxid is higher
         * 3- New epoch is the same as current epoch, new zxid is the same
         *  as current zxid, but server id is higher.
         */
        return ((newEpoch > curEpoch) ||
                    (
                            (newEpoch == curEpoch) && ((newZxid > curZxid) ||
                            ((newZxid == curZxid) && (newId > curId)))
                    )
                );
    }

    /**
     * Termination predicate. Given a set of votes, determines if
     * have sufficient to declare the end of the election round.
     * 判断vote选票是否能够胜出（例如过半）
     *
     *  @param votes    Set of votes
     *  @param vote     the vote received last
     */
    protected boolean termPredicate(HashMap<Long, Vote> votes, Vote vote) {
        HashSet<Long> set = new HashSet<Long>();

        /*
         * First make the views consistent. Sometimes peers will have
         * different zxids for a server depending on timing.
         */
        for (Map.Entry<Long, Vote> entry : votes.entrySet()) {
            if (vote.equals(entry.getValue())){
                set.add(entry.getKey());
            }
        }
        return self.getQuorumVerifier().containsQuorum(set);
    }

    /**
     * In the case there is a leader elected, and a quorum supporting
     * this leader, we have to check if the leader has voted and acked
     * that it is leading. We need this check to avoid that peers keep
     * electing over and over a peer that has crashed and it is no
     * longer leading.
     * 检查选出的leader是否可以胜任leader（防止一个服务已经crashed，其他服务还一遍又一遍地选它）
     *
     * @param votes set of votes
     * @param   leader  leader id
     * @param   electionEpoch   epoch id
     */
    protected boolean checkLeader(HashMap<Long, Vote> votes, long leader, long electionEpoch){
        boolean predicate = true;

        /*
         * If everyone else thinks I'm the leader, I must be the leader.
         * The other two checks are just for the case in which I'm not the
         * leader. If I'm not the leader and I haven't received a message
         * from leader stating that it is leading, then predicate is false.
         */
        if(leader != self.getId()){
            if(votes.get(leader) == null) predicate = false; // 选出的leader已经crash
            else if(votes.get(leader).getState() != ServerState.LEADING) predicate = false; // 如果选出的leader状态不是leading
        } else if(logicalclock.get() != electionEpoch) { // electionEpoch选举轮次不是当前的轮次
            predicate = false;
        }
        return predicate;
    }
    
    /**
     * This predicate checks that a leader has been elected. It doesn't
     * make a lot of sense without context (check lookForLeader) and it
     * has been separated for testing purposes.
     * 确定leader是否选举成功
     * 
     * @param recv  map of received votes 
     * @param ooe   map containing out of election votes (LEADING or FOLLOWING) 不参与选举的投票，即服务本身已经处于LEADING或FOLLOWING，发送过来的选票
     * @param n     Notification
     * @return          
     */
    protected boolean ooePredicate(HashMap<Long, Vote> recv, HashMap<Long, Vote> ooe, Notification n) {
        
        return (termPredicate(recv, new Vote(n.version, 
                                             n.leader,
                                             n.zxid, 
                                             n.electionEpoch, 
                                             n.peerEpoch, 
                                             n.state))
                && checkLeader(ooe, n.leader, n.electionEpoch));
    }

    // 更新提议（更新当前选举的leader）
    synchronized void updateProposal(long leader, long zxid, long epoch){
        if(LOG.isDebugEnabled()){
            LOG.debug("Updating proposal: " + leader + " (newleader), 0x"
                    + Long.toHexString(zxid) + " (newzxid), " + proposedLeader
                    + " (oldleader), 0x" + Long.toHexString(proposedZxid) + " (oldzxid)");
        }
        proposedLeader = leader;
        proposedZxid = zxid;
        proposedEpoch = epoch;
    }

    // 生成一个选票
    synchronized Vote getVote(){
        return new Vote(proposedLeader, proposedZxid, proposedEpoch);
    }

    /**
     * A learning state can be either FOLLOWING or OBSERVING.
     * This method simply decides which one depending on the
     * role of the server.
     * 简单根据服务角色确定该服务状态（FOLLOWING或OBSERVING）
     *
     * @return ServerState
     */
    private ServerState learningState(){
        if(self.getLearnerType() == LearnerType.PARTICIPANT){
            LOG.debug("I'm a participant: " + self.getId());
            return ServerState.FOLLOWING;
        }
        else{
            LOG.debug("I'm an observer: " + self.getId());
            return ServerState.OBSERVING;
        }
    }

    /**
     * Returns the initial vote value of server identifier.
     * 返回参与选举服务的id
     *
     * @return long
     */
    private long getInitId(){
        if(self.getLearnerType() == LearnerType.PARTICIPANT)
            return self.getId();
        else return Long.MIN_VALUE; // 没有参与选举返回MIN_VALUE
    }

    /**
     * Returns initial last logged zxid.
     * 返回参与选举服务最近一次的事务id
     *
     * @return long
     */
    private long getInitLastLoggedZxid(){
        if(self.getLearnerType() == LearnerType.PARTICIPANT)
            return self.getLastLoggedZxid();
        else return Long.MIN_VALUE;
    }

    /**
     * Returns the initial vote value of the peer epoch.
     * 返回当前epoch
     *
     * @return long
     */
    private long getPeerEpoch(){
        if(self.getLearnerType() == LearnerType.PARTICIPANT)
        	try {
        		return self.getCurrentEpoch();
        	} catch(IOException e) { // 抛出运行时异常
        		RuntimeException re = new RuntimeException(e.getMessage());
        		re.setStackTrace(e.getStackTrace());
        		throw re;
        	}
        else return Long.MIN_VALUE;
    }
    
    /**
     * Starts a new round of leader election. Whenever our QuorumPeer
     * changes its state to LOOKING, this method is invoked, and it
     * sends notifications to all other peers.
     * 开始进行选举，无论何时只要QuorumPeer的状态变为LOOKING，该方法就会调用，
     * 并向所有其他peer发送通知
     */
    public Vote lookForLeader() throws InterruptedException {
        try {
            self.jmxLeaderElectionBean = new LeaderElectionBean();
            MBeanRegistry.getInstance().register(self.jmxLeaderElectionBean, self.jmxLocalPeerBean); // 注册JMX
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            self.jmxLeaderElectionBean = null;
        }
        if (self.start_fle == 0) {
           self.start_fle = Time.currentElapsedTime(); // 选举开始时间（毫秒数）
        }
        try {
            HashMap<Long, Vote> recvset = new HashMap<Long, Vote>(); // 收集一轮选举的选票

            HashMap<Long, Vote> outofelection = new HashMap<Long, Vote>(); // 收集处于leading或following状态的服务发送的选票

            int notTimeout = finalizeWait;

            synchronized(this){
                logicalclock.incrementAndGet(); // 选举轮次增加
                updateProposal(getInitId(), getInitLastLoggedZxid(), getPeerEpoch()); // 更新选票
            }

            LOG.info("New election. My id =  " + self.getId() + ", proposed zxid=0x" + Long.toHexString(proposedZxid));
            sendNotifications(); // 向所有参与选举的服务发送通知

            /*
             * Loop in which we exchange notifications until we find a leader
             * 循环直到找到一个leader
             */
            while ((self.getPeerState() == ServerState.LOOKING) && (!stop)){
                /*
                 * Remove next notification from queue, times out after 2 times
                 * the termination time
                 */
                Notification n = recvqueue.poll(notTimeout, TimeUnit.MILLISECONDS); // 取出接收到的消息

                /*
                 * Sends more notifications if haven't received enough.
                 * Otherwise processes new notification.
                 */
                if(n == null){ // 如果接收消息为空，可能服务都刚启动，还没有连接
                    if(manager.haveDelivered()) { // 有消息完成发送，继续发送通知给其他参与选举的服务
                        sendNotifications();
                    } else {
                        manager.connectAll(); // 发送给所有其他服务的消息都还没有被发送，连接所有待发送消息的服务
                    }

                    /*
                     * Exponential backoff
                     */
                    int tmpTimeOut = notTimeout * 2;
                    // 通知超时时间为min(notTimeout * 2, maxNotificationInterval)
                    notTimeout = (tmpTimeOut < maxNotificationInterval ? tmpTimeOut : maxNotificationInterval);
                    LOG.info("Notification time out: " + notTimeout);
                } else if(validVoter(n.sid) && validVoter(n.leader)) { // 确保发来消息的服务和选举的leader在参与选举的集合中
                    /*
                     * Only proceed if the vote comes from a replica in the
                     * voting view for a replica in the voting view.
                     */
                    switch (n.state) {
                    case LOOKING: // 正在选举
                        // If notification > current, replace and send messages out
                        if (n.electionEpoch > logicalclock.get()) { // 如果接收到选票的选举轮次大于该服务当前的选举轮次
                            logicalclock.set(n.electionEpoch); // 将该服务的选举轮次设置为选票的选举轮次
                            recvset.clear(); // 清空之前收集的选票
                            if(totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                    getInitId(), getInitLastLoggedZxid(), getPeerEpoch())) { // 比较接收到的选票和该服务自己
                                updateProposal(n.leader, n.zxid, n.peerEpoch); // 更新自己的选票为接收的选票
                            } else { // 否则更新选票为自己
                                updateProposal(getInitId(), getInitLastLoggedZxid(), getPeerEpoch());
                            }
                            sendNotifications(); // 发送当前选票给所有参与选举的服务
                        } else if (n.electionEpoch < logicalclock.get()) { // 如果接收到选票的选举轮次小于该服务当前的选举轮次，接收到的选票无效
                            if(LOG.isDebugEnabled()){
                                LOG.debug("Notification election epoch is smaller than logicalclock. n.electionEpoch = 0x"
                                        + Long.toHexString(n.electionEpoch)
                                        + ", logicalclock=0x" + Long.toHexString(logicalclock.get()));
                            }
                            break;
                        } else if (totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                proposedLeader, proposedZxid, proposedEpoch)) { // 接收到选票和该服务的选举轮次相等，比较选票和该服务提议的选票，如果接收到的选票更适合做leader，修改该服务自己的选票（选择接收到选票所选的leader）
                            updateProposal(n.leader, n.zxid, n.peerEpoch); // 更新该服务自己的选票
                            sendNotifications(); // 并向所有选举服务发送消息
                        }

                        if(LOG.isDebugEnabled()){
                            LOG.debug("Adding vote: from=" + n.sid +
                                    ", proposed leader=" + n.leader +
                                    ", proposed zxid=0x" + Long.toHexString(n.zxid) +
                                    ", proposed election epoch=0x" + Long.toHexString(n.electionEpoch));
                        }

                        // 收集选票
                        recvset.put(n.sid, new Vote(n.leader, n.zxid, n.electionEpoch, n.peerEpoch));

                        // 判断收集的选票是否能选举出leader（选票是否支持proposedLeader）
                        if (termPredicate(recvset,
                                new Vote(proposedLeader, proposedZxid, logicalclock.get(), proposedEpoch))) {

                            // Verify if there is any change in the proposed leader
                            while((n = recvqueue.poll(finalizeWait, TimeUnit.MILLISECONDS)) != null){ // 取出接收到的选票，确认选票（其他服务回复的ack）是否选proposedLeader
                                if(totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                        proposedLeader, proposedZxid, proposedEpoch)){ // 当接收的选票没选举proposedLeader时，还要继续选举
                                    recvqueue.put(n);
                                    break;
                                }
                            }

                            /*
                             * This predicate is true once we don't read any new
                             * relevant message from the reception queue
                             */
                            if (n == null) { // 没有其他服务发来消息了，大家都选举结束（只是认为结束，可能有消息因为网络延时还没收到）
                                // 设置服务角色
                                self.setPeerState((proposedLeader == self.getId()) ?
                                        ServerState.LEADING: learningState());

                                // 最终leader选票
                                Vote endVote = new Vote(proposedLeader,
                                                        proposedZxid,
                                                        logicalclock.get(),
                                                        proposedEpoch);
                                leaveInstance(endVote); // 结束选举
                                return endVote;
                            }
                        }
                        break;
                    case OBSERVING: // 观察状态
                        LOG.debug("Notification from observer: " + n.sid);
                        break;
                    // 发送消息的服务已经是跟随者或leader（例如：
                    // 1、已经存在一个稳定zookeeper集群，该服务加入到集群中。
                    // 2、选举过程中部分服务已经变成跟随者或leader了，因为只要过半就可以选出leader，并不需要等待所有的服务响应ack通知。）
                    case FOLLOWING:
                    case LEADING:
                        /*
                         * Consider all notifications from the same epoch
                         * together.
                         */
                        if(n.electionEpoch == logicalclock.get()){ // 接收的选票的选举轮次相同（表示部分服务已经成为follower或leader）
                            recvset.put(n.sid, new Vote(n.leader,
                                                          n.zxid,
                                                          n.electionEpoch,
                                                          n.peerEpoch)); // 收集选票
                           
                            if(ooePredicate(recvset, outofelection, n)) { // 收集的选票满足选举的原则（例如多数原则），且所选的leader能够胜任
                                self.setPeerState((n.leader == self.getId()) ?
                                        ServerState.LEADING: learningState()); // 设置该服务状态

                                Vote endVote = new Vote(n.leader, 
                                        n.zxid, 
                                        n.electionEpoch, 
                                        n.peerEpoch);
                                leaveInstance(endVote); // 结束选举
                                return endVote;
                            }
                        }

                        /*
                         * Before joining an established ensemble, verify
                         * a majority is following the same leader.
                         * 收集处于leading或following状态的服务发送的选票
                         */
                        outofelection.put(n.sid, new Vote(n.version,
                                                            n.leader,
                                                            n.zxid,
                                                            n.electionEpoch,
                                                            n.peerEpoch,
                                                            n.state));
                        // 选票n在outofelection满足选举原则，并且n选票的leader能够胜任leader（存活且选举轮次是当前轮次）
                        if(ooePredicate(outofelection, outofelection, n)) {
                            synchronized(this){
                                logicalclock.set(n.electionEpoch); // 设置该服务的选举轮次
                                self.setPeerState((n.leader == self.getId()) ?
                                        ServerState.LEADING: learningState());
                            }
                            Vote endVote = new Vote(n.leader,
                                                    n.zxid,
                                                    n.electionEpoch,
                                                    n.peerEpoch);
                            leaveInstance(endVote); // 结束选举
                            return endVote;
                        }
                        break;
                    default:
                        LOG.warn("Notification state unrecognized: {} (n.state), {} (n.sid)", n.state, n.sid);
                        break;
                    }
                } else { // 发送消息的服务或选举的leader没有在参与选举的集合中
                    if (!validVoter(n.leader)) {
                        LOG.warn("Ignoring notification for non-cluster member sid {} from sid {}", n.leader, n.sid);
                    }
                    if (!validVoter(n.sid)) {
                        LOG.warn("Ignoring notification for sid {} from non-quorum member sid {}", n.leader, n.sid);
                    }
                }
            }
            return null;
        } finally { // 注销JMX
            try {
                if(self.jmxLeaderElectionBean != null){
                    MBeanRegistry.getInstance().unregister(self.jmxLeaderElectionBean);
                }
            } catch (Exception e) {
                LOG.warn("Failed to unregister with JMX", e);
            }
            self.jmxLeaderElectionBean = null;
            LOG.debug("Number of connection processing threads: {}", manager.getConnectionThreadCount());
        }
    }

    /**
     * Check if a given sid is represented in either the current or
     * the next voting view
     * 检查一个给定的服务id是否在参加选举的服务中（voting view）
     *
     * @param sid     Server identifier
     * @return boolean
     */
    private boolean validVoter(long sid) {
        return self.getVotingView().containsKey(sid);
    }
}
