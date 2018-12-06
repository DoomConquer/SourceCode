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
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.security.sasl.SaslException;

import org.apache.jute.BinaryInputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.Record;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.ZooDefs.OpCode;
import org.apache.zookeeper.server.ByteBufferInputStream;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.ZooKeeperThread;
import org.apache.zookeeper.server.ZooTrace;
import org.apache.zookeeper.server.quorum.Leader.Proposal;
import org.apache.zookeeper.server.quorum.QuorumPeer.LearnerType;
import org.apache.zookeeper.server.util.SerializeUtils;
import org.apache.zookeeper.server.util.ZxidUtils;
import org.apache.zookeeper.txn.TxnHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * There will be an instance of this class created by the Leader for each
 * learner. All communication with a learner is handled by this
 * class. 该类主要用于leader和learner之间的通信处理，每个learner会有一个LearnerHandler与之对应
 */
public class LearnerHandler extends ZooKeeperThread {
    private static final Logger LOG = LoggerFactory.getLogger(LearnerHandler.class);

    protected final Socket sock; // learner和leader的socket连接

    public Socket getSocket() {
        return sock;
    }

    final Leader leader;

    /** Deadline for receiving the next ack. If we are bootstrapping then
     * it's based on the initLimit, if we are done bootstrapping it's based
     * on the syncLimit. Once the deadline is past this learner should
     * be considered no longer "sync'd" with the leader.
     * 接收下一个ack的截止时间，刚开始启动时tickOfNextAckDeadline = tick + initLimit + syncLimit，
     * 启动后tickOfNextAckDeadline = tick + syncLimit。一旦超过了该时间就认为learner和leader不同步
     */
    volatile long tickOfNextAckDeadline;
    
    /**
     * ZooKeeper server identifier of this learner
     * learner的服务id
     */
    protected long sid = 0;

    // 获取服务id
    long getSid(){
        return sid;
    }

    // 版本协议（新的zookeeper使用zab1.0协议），当前版本中做了版本的适配，例如新版本节点去连接旧版本的leader，
    // 或旧版本的节点连接新版本的leader
    protected int version = 0x1;

    // 获取协议版本号
    int getVersion() {
    	return version;
    }
    
    /**
     * The packets to be sent to the learner
     * 待发送给learner的发送队列
     */
    final LinkedBlockingQueue<QuorumPacket> queuedPackets = new LinkedBlockingQueue<QuorumPacket>();

    /**
     * This class controls the time that the Leader has been
     * waiting for acknowledgement of a proposal from this Learner.
     * If the time is above syncLimit, the connection will be closed.
     * It keeps track of only one proposal at a time, when the ACK for
     * that proposal arrives, it switches to the last proposal received
     * or clears the value if there is no pending proposal.
     * 检查（上一次接收到ack到当前时间）是否超过syncLimit时间，超过syncLimit时间，
     * 关闭该LearnerHandler
     */
    private class SyncLimitCheck {
        private boolean started = false;
        private long currentZxid = 0;
        private long currentTime = 0;
        private long nextZxid = 0;
        private long nextTime = 0;

        public synchronized void start() {
            started = true;
        }

        // 发送提议时，更新时间和zxid
        public synchronized void updateProposal(long zxid, long time) {
            if (!started) {
                return;
            }
            if (currentTime == 0) {
                currentTime = time;
                currentZxid = zxid;
            } else {
                nextTime = time;
                nextZxid = zxid;
            }
        }

        // 收到ack更新时间和zxid
        public synchronized void updateAck(long zxid) {
             if (currentZxid == zxid) { // 收到ack，更新当前zxid和时间为下一个的zxid和时间
                 currentTime = nextTime;
                 currentZxid = nextZxid;
                 nextTime = 0;
                 nextZxid = 0;
             } else if (nextZxid == zxid) { // 先收到了下一个的ack
                 LOG.warn("ACK for " + zxid + " received before ACK for " + currentZxid + "!!!!");
                 nextTime = 0;
                 nextZxid = 0;
             }
        }

        // 检查是否超过syncLimit时间
        public synchronized boolean check(long time) {
            if (currentTime == 0) { // 此时没有等待ack的请求
                return true;
            } else {
                long msDelay = (time - currentTime) / 1000000;
                return (msDelay < (leader.self.tickTime * leader.self.syncLimit));
            }
        }
    };

    private SyncLimitCheck syncLimitCheck = new SyncLimitCheck(); // syncLimit超时检测（上一次收到ack到现在是否超过syncLimit时间）

    private BinaryInputArchive ia;  // 和learner连接的输入流（接收learner的数据）

    private BinaryOutputArchive oa; // 和learner连接的输出流（向learner发送数据）

    private final BufferedInputStream bufferedInput; // 输入流
    private BufferedOutputStream bufferedOutput;     // 输出流

    // 构造函数
    LearnerHandler(Socket sock, BufferedInputStream bufferedInput, Leader leader) throws IOException {
        super("LearnerHandler-" + sock.getRemoteSocketAddress());
        this.sock = sock;
        this.leader = leader;
        this.bufferedInput = bufferedInput;
        try {
            leader.self.authServer.authenticate(sock, new DataInputStream(bufferedInput)); // sasl认证
        } catch (IOException e) { // 验证失败
            LOG.error("Server failed to authenticate quorum learner, addr: {}, closing connection",
                    sock.getRemoteSocketAddress(), e);
            try {
                sock.close(); // 关闭socket
            } catch (IOException ie) {
                LOG.error("Exception while closing socket", ie);
            }
            throw new SaslException("Authentication failure: " + e.getMessage());
        }
    }

    // LearnerHandler信息
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LearnerHandler ").append(sock);
        sb.append(" tickOfNextAckDeadline:").append(tickOfNextAckDeadline()); // 下一个ack截止时间
        sb.append(" synced?:").append(synced()); // learner是否和leader同步
        sb.append(" queuedPacketLength:").append(queuedPackets.size()); // 发送队列大小
        return sb.toString();
    }

    /**
     * If this packet is queued, the sender thread will exit
     * 发送线程结束标志
     */
    final QuorumPacket proposalOfDeath = new QuorumPacket();

    private LearnerType learnerType = LearnerType.PARTICIPANT; // learner角色
    public LearnerType getLearnerType() {
        return learnerType;
    }

    /**
     * This method will use the thread to send packets added to the
     * queuedPackets list
     * 该方法使用线程发送队列queuedPackets中的数据包
     *
     * @throws InterruptedException
     */
    private void sendPackets() throws InterruptedException {
        long traceMask = ZooTrace.SERVER_PACKET_TRACE_MASK;
        while (true) {
            try {
                QuorumPacket p;
                p = queuedPackets.poll(); // 取出队列中的数据包
                if (p == null) { // 队列中没有数据，先刷新输出流中数据给learner，然后阻塞等待队列中添加数据
                    bufferedOutput.flush();
                    p = queuedPackets.take();
                }

                if (p == proposalOfDeath) { // 结束标志
                    // Packet of death!
                    break;
                }
                if (p.getType() == Leader.PING) { // 发送心跳
                    traceMask = ZooTrace.SERVER_PING_TRACE_MASK;
                }
                if (p.getType() == Leader.PROPOSAL) { // 如果是事务请求，更新syncLimit检查中的zxid和时间
                    syncLimitCheck.updateProposal(p.getZxid(), System.nanoTime());
                }
                if (LOG.isTraceEnabled()) {
                    ZooTrace.logQuorumPacket(LOG, traceMask, 'o', p);
                }
                oa.writeRecord(p, "packet"); // 发送
            } catch (IOException e) {
                if (!sock.isClosed()) { // 关闭socket
                    LOG.warn("Unexpected exception at " + this, e);
                    try {
                        // this will cause everything to shutdown on
                        // this learner handler and will help notify
                        // the learner/observer instantaneously
                        sock.close();
                    } catch(IOException ie) {
                        LOG.warn("Error closing socket for handler " + this, ie);
                    }
                }
                break;
            }
        }
    }

    // 返回数据包QuorumPacket中信息
    static public String packetToString(QuorumPacket p) {
        String type = null;
        String mess = null;
        Record txn = null;
        
        switch (p.getType()) {
        case Leader.ACK:
            type = "ACK";
            break;
        case Leader.COMMIT:
            type = "COMMIT";
            break;
        case Leader.FOLLOWERINFO:
            type = "FOLLOWERINFO";
            break;    
        case Leader.NEWLEADER:
            type = "NEWLEADER";
            break;
        case Leader.PING:
            type = "PING";
            break;
        case Leader.PROPOSAL:
            type = "PROPOSAL";
            TxnHeader hdr = new TxnHeader();
            try {
                SerializeUtils.deserializeTxn(p.getData(), hdr);
                // mess = "transaction: " + txn.toString();
            } catch (IOException e) {
                LOG.warn("Unexpected exception",e);
            }
            break;
        case Leader.REQUEST:
            type = "REQUEST";
            break;
        case Leader.REVALIDATE:
            type = "REVALIDATE";
            ByteArrayInputStream bis = new ByteArrayInputStream(p.getData());
            DataInputStream dis = new DataInputStream(bis);
            try {
                long id = dis.readLong();
                mess = " sessionid = " + id;
            } catch (IOException e) {
                LOG.warn("Unexpected exception", e);
            }

            break;
        case Leader.UPTODATE:
            type = "UPTODATE";
            break;
        default:
            type = "UNKNOWN" + p.getType();
        }
        String entry = null;
        if (type != null) {
            entry = type + " " + Long.toHexString(p.getZxid()) + " " + mess;
        }
        return entry;
    }

    /**
     * This thread will receive packets from the peer and process them and
     * also listen to new connections from new peers.
     * 该线程主要处理leader接收到learner的请求
     */
    @Override
    public void run() {
        try {
            leader.addLearnerHandler(this); // leader中添加该learner对应的LearnerHandler
            // 初始tickOfNextAckDeadline
            tickOfNextAckDeadline = leader.self.tick.get() + leader.self.initLimit + leader.self.syncLimit;

            // 初始化和learner通信的输入输出流
            ia = BinaryInputArchive.getArchive(bufferedInput);
            bufferedOutput = new BufferedOutputStream(sock.getOutputStream());
            oa = BinaryOutputArchive.getArchive(bufferedOutput);

            QuorumPacket qp = new QuorumPacket();
            ia.readRecord(qp, "packet"); // 接收learner的数据
            // 没有接收到FOLLOWERINFO或OBSERVERINFO数据，错误返回（第一个接收到的必须是FOLLOWERINFO或OBSERVERINFO数据）
            if(qp.getType() != Leader.FOLLOWERINFO && qp.getType() != Leader.OBSERVERINFO){
            	LOG.error("First packet " + qp.toString() + " is not FOLLOWERINFO or OBSERVERINFO!");
                return;
            }
            byte learnerInfoData[] = qp.getData(); // LearnerInfo数据
            if (learnerInfoData != null) {
            	if (learnerInfoData.length == 8) { // 早期版本数据
            		ByteBuffer bbsid = ByteBuffer.wrap(learnerInfoData);
            		this.sid = bbsid.getLong(); // learner服务id
            	} else {
            		LearnerInfo li = new LearnerInfo();
            		ByteBufferInputStream.byteBuffer2Record(ByteBuffer.wrap(learnerInfoData), li);
            		this.sid = li.getServerid(); // learner服务id
            		this.version = li.getProtocolVersion(); // 版本协议
            	}
            } else { // 没有LearnerInfo数据，使用自增的id标识服务id
            	this.sid = leader.followerCounter.getAndDecrement();
            }

            LOG.info("Follower sid: " + sid + " : info : " + leader.self.quorumPeers.get(sid));
                        
            if (qp.getType() == Leader.OBSERVERINFO) { // learnerType为观察者
                learnerType = LearnerType.OBSERVER;
            }

            // 获取learner的最后接收的epoch（记录在acceptedEpoch文件中的lastAcceptedEpoch）
            long lastAcceptedEpoch = ZxidUtils.getEpochFromZxid(qp.getZxid());
            
            long peerLastZxid;
            StateSummary ss = null;
            long zxid = qp.getZxid(); // 接收learner的zxid
            // 等待leader决议出最新的epoch（过半的参选服务中lastAcceptedEpoch最大的）
            long newEpoch = leader.getEpochToPropose(this.getSid(), lastAcceptedEpoch);
            
            if (this.getVersion() < 0x10000) { // 旧版本
                // we are going to have to extrapolate（推断） the epoch information
                long epoch = ZxidUtils.getEpochFromZxid(zxid);
                ss = new StateSummary(epoch, zxid); // 接收到learner的zxid和epoch信息
                // fake the message 推断的数据，因为旧版本没有发送LEADERINFO这个过程
                leader.waitForEpochAck(this.getSid(), ss); // 为了和新版本一致，同样等待其他服务的ACKEPOCH确认
            } else { // Zab协议1.0后的新版本
                byte ver[] = new byte[4];
                ByteBuffer.wrap(ver).putInt(0x10000); // 版本信息
                QuorumPacket newEpochPacket = new QuorumPacket(Leader.LEADERINFO, ZxidUtils.makeZxid(newEpoch, 0), ver, null);
                oa.writeRecord(newEpochPacket, "packet"); // 发送LEADERINFO给learner
                bufferedOutput.flush(); // 刷新输出流，立即发送
                QuorumPacket ackEpochPacket = new QuorumPacket();
                ia.readRecord(ackEpochPacket, "packet"); // 接收到learner的ACKEPOCH确认信息
                if (ackEpochPacket.getType() != Leader.ACKEPOCH) { // 没有接收到ACKEPOCH确认，错误退出
                    LOG.error(ackEpochPacket.toString() + " is not ACKEPOCH");
                    return;
				}
                ByteBuffer bbepoch = ByteBuffer.wrap(ackEpochPacket.getData()); // 从data中获取learner发送的epoch和zxid
                ss = new StateSummary(bbepoch.getInt(), ackEpochPacket.getZxid());
                leader.waitForEpochAck(this.getSid(), ss); // 等待其他follower服务的ACKEPOCH确认
            }
            peerLastZxid = ss.getLastZxid(); // learner服务的最后事务id

            // 下面准备和learner同步数据
            /* the default to send to the follower */
            int packetToSend = Leader.SNAP; // 默认以快照方式同步数据
            long zxidToSend = 0; // 设置learner服务数据库最后处理的事务id（SNAP方式为leader数据库的lastProcessedZxid，TRUNC为截取点的zxid）
            long leaderLastZxid = 0; // leader最后一次提议的zxid
            /** the packets that the follower needs to get updates from **/
            long updates = peerLastZxid; // leader进行startForwarding时开始的zxid（只有updates之后的提议发送给learner）
            
            /* we are sending the diff check if we have proposals in memory to be able to 
             * send a diff to the learner
             */ 
            ReentrantReadWriteLock lock = leader.zk.getZKDatabase().getLogLock();
            ReadLock rl = lock.readLock(); // 获取事务日志读锁
            // 判断使用何种同步方式，如果以DIFF方式，将需要同步的Proposal放入发送队列
            try {
                rl.lock();
                final long maxCommittedLog = leader.zk.getZKDatabase().getmaxCommittedLog(); // leader内存数据库最大提交zxid
                final long minCommittedLog = leader.zk.getZKDatabase().getminCommittedLog(); // leader内存数据库最小提交zxid
                LOG.info("Synchronizing with Follower sid: " + sid
                        + " maxCommittedLog=0x" + Long.toHexString(maxCommittedLog)
                        + " minCommittedLog=0x" + Long.toHexString(minCommittedLog)
                        + " peerLastZxid=0x" + Long.toHexString(peerLastZxid));

                // leader维护的最近提交的事务Proposal（维护最近commitLogCount次默认500次）
                LinkedList<Proposal> proposals = leader.zk.getZKDatabase().getCommittedLog();

                // learner服务的lastZxid和leader数据库当前已经处理的zxid相等，不需要同步
                if (peerLastZxid == leader.zk.getZKDatabase().getDataTreeLastProcessedZxid()) {
                    // Follower is already sync with us, send empty diff
                    LOG.info("leader and follower are in sync, zxid=0x{}", Long.toHexString(peerLastZxid));
                    packetToSend = Leader.DIFF;
                    zxidToSend = peerLastZxid;
                } else if (proposals.size() != 0) { // 如果leader数据库已经有提交的事务
                    LOG.debug("proposal size is {}", proposals.size());
                    // 如果learner服务的lastZxid介于leader数据库最大提交zxid和最小提交zxid之间，使用DIFF方式同步
                    if ((maxCommittedLog >= peerLastZxid) && (minCommittedLog <= peerLastZxid)) {
                        LOG.debug("Sending proposals to follower");

                        // as we look through proposals, this variable keeps track of previous proposal Id.
                        // 保存最近一次小于等于learner服务zxid的提议zxid
                        long prevProposalZxid = minCommittedLog;

                        // Keep track of whether we are about to send the first packet.
                        // Before sending the first packet, we have to tell the learner
                        // whether to expect a trunc or a diff
                        // 发送第一个提议的时候确定learner是否需要截断数据（如果learner的zxid超过leader的zxid）
                        boolean firstPacket = true;

                        // If we are here, we can use committedLog to sync with
                        // follower. Then we only need to decide whether to
                        // send trunc or not
                        packetToSend = Leader.DIFF;
                        zxidToSend = maxCommittedLog;

                        for (Proposal propose : proposals) {
                            // skip the proposals the peer already has
                            // 跳过zxid小于learner服务的zxid的提议（learner服务已经接收过这些提议了）
                            if (propose.packet.getZxid() <= peerLastZxid) {
                                prevProposalZxid = propose.packet.getZxid();
                                continue; // 继续
                            } else { // 否则发送提议给learner（先加入发送队列，等待后面发送线程发送）
                                // If we are sending the first packet, figure out whether to trunc
                                // in case the follower has some proposals that the leader doesn't
                                if (firstPacket) { // 发送第一个提议
                                    firstPacket = false;
                                    // Does the peer have some proposals that the leader hasn't seen yet
                                    // learner服务存在但是leader不存在的提议，截断learner上这些事务
                                    if (prevProposalZxid < peerLastZxid) {
                                        // send a trunc message before sending the diff
                                        packetToSend = Leader.TRUNC; // 发送diff前需要截断
                                        zxidToSend = prevProposalZxid;
                                        updates = zxidToSend;
                                    }
                                }
                                queuePacket(propose.packet); // 发送提议Proposal
                                QuorumPacket qcommit = new QuorumPacket(Leader.COMMIT, propose.packet.getZxid(), null, null);
                                queuePacket(qcommit); // 发送COMMIT（发送完Proposal后立即发送commit）
                            }
                        }
                    } else if (peerLastZxid > maxCommittedLog) { // 如果learner服务的zxid比leader服务数据库当前最大的还大，learner只需要截断leader当前zxid之后的事务
                        LOG.debug("Sending TRUNC to follower zxidToSend=0x{} updates=0x{}",
                                Long.toHexString(maxCommittedLog),
                                Long.toHexString(updates));

                        packetToSend = Leader.TRUNC; // 截断learner事务
                        zxidToSend = maxCommittedLog;
                        updates = zxidToSend;
                    } else { // learner服务的zxid比leader数据库当前最小提交的zxid还小，使用SNAP方式同步
                        LOG.warn("Unhandled proposal scenario");
                    }
                } else { // leader数据库没有提交的事务
                    // just let the state transfer happen
                    LOG.debug("proposals is empty");
                }

                LOG.info("Sending " + Leader.getPacketType(packetToSend));
                // DIFF同步的数据已经加到queuePacket队列，leader可以将同步区间的提议也加到该队列，一起同步
                leaderLastZxid = leader.startForwarding(this, updates);
            } finally { // 释放读锁
                rl.unlock();
            }

            // NEWLEADER数据包
            QuorumPacket newLeaderQP = new QuorumPacket(Leader.NEWLEADER, ZxidUtils.makeZxid(newEpoch, 0), null, null);
            if (getVersion() < 0x10000) { // 旧版本
                oa.writeRecord(newLeaderQP, "packet"); // 旧版本直接发送NEWLEADER数据给learner服务
            } else {
                queuedPackets.add(newLeaderQP); // 新版本先将NEWLEADER数据包放入queuedPackets队列，同步后发送
            }
            bufferedOutput.flush(); // 刷新输出流
            //Need to set the zxidToSend to the latest zxid
            if (packetToSend == Leader.SNAP) { // 如果快照方式同步，zxidToSend为leader最后处理的事务id
                zxidToSend = leader.zk.getZKDatabase().getDataTreeLastProcessedZxid();
            }
            // 发送同步方式packetToSend（DIFF、SNAP或TRUNC）消息
            oa.writeRecord(new QuorumPacket(packetToSend, zxidToSend, null, null), "packet");
            bufferedOutput.flush(); // 立即发送
            
            /* if we are not truncating or sending a diff just send a snapshot */
            if (packetToSend == Leader.SNAP) { // SNAP快照方式同步，序列化快照数据发送给learner
                LOG.info("Sending snapshot last zxid of peer is 0x"
                        + Long.toHexString(peerLastZxid) + " " 
                        + " zxid of leader is 0x"
                        + Long.toHexString(leaderLastZxid)
                        + "sent zxid of db as 0x" 
                        + Long.toHexString(zxidToSend));
                // Dump data to peer 向learner发送快照数据
                leader.zk.getZKDatabase().serializeSnapshot(oa);
                oa.writeString("BenWasHere", "signature"); // 发送签名
            }
            bufferedOutput.flush();
            
            // Start sending packets 开启一个线程发送queuedPackets队列中的数据
            new Thread() {
                public void run() {
                    Thread.currentThread().setName("Sender-" + sock.getRemoteSocketAddress());
                    try {
                        sendPackets(); // 发送queuedPackets队列中的数据
                    } catch (InterruptedException e) {
                        LOG.warn("Unexpected interruption",e);
                    }
                }
            }.start();
            
            /*
             * Have to wait for the first ACK, wait until 
             * the leader is ready, and only then we can
             * start processing messages.
             */
            qp = new QuorumPacket();
            ia.readRecord(qp, "packet");
            if(qp.getType() != Leader.ACK){ // 如果不是ACK确认（NEWLEADER的确认），错误返回
                LOG.error("Next packet was supposed to be an ACK");
                return;
            }
            LOG.info("Received NEWLEADER-ACK message from " + getSid());
            leader.waitForNewLeaderAck(getSid(), qp.getZxid()); // leader等待其他服务NEWLEADER的ACK确认

            syncLimitCheck.start(); // 开始syncLimit检查
            
            // now that the ack has been processed expect the syncLimit
            sock.setSoTimeout(leader.self.tickTime * leader.self.syncLimit); // 设置socket超时（syncLimit * tickTime）

            /*
             * Wait until leader starts up
             * 如果leader没有启动，等待leader启动完成（zk.startup方法启动后会执行notifyAll）
             */
            synchronized(leader.zk){
                while(!leader.zk.isRunning() && !this.isInterrupted()){
                    leader.zk.wait(20);
                }
            }
            // Mutation packets will be queued during the serialize,
            // so we need to mark when the peer can actually start
            // using the data
            queuedPackets.add(new QuorumPacket(Leader.UPTODATE, -1, null, null)); // 给learner发送UPTODATE，表示同步完成

            // leader循环接收并处理learner的消息
            while (true) {
                qp = new QuorumPacket();
                ia.readRecord(qp, "packet"); // 接收learner数据

                long traceMask = ZooTrace.SERVER_PACKET_TRACE_MASK;
                if (qp.getType() == Leader.PING) { // 心跳消息
                    traceMask = ZooTrace.SERVER_PING_TRACE_MASK;
                }
                if (LOG.isTraceEnabled()) { // 输出日志
                    ZooTrace.logQuorumPacket(LOG, traceMask, 'i', qp);
                }
                // 同步完成后，tickOfNextAckDeadline更新为tick + syncLimit
                tickOfNextAckDeadline = leader.self.tick.get() + leader.self.syncLimit;

                ByteBuffer bb;
                long sessionId;
                int cxid;
                int type;

                switch (qp.getType()) {
                case Leader.ACK: // 接收到learner的ACK消息
                    if (this.learnerType == LearnerType.OBSERVER) { // 观察者
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Received ACK from Observer  " + this.sid);
                        }
                    }
                    syncLimitCheck.updateAck(qp.getZxid()); // 更新syncLimit检查
                    leader.processAck(this.sid, qp.getZxid(), sock.getLocalSocketAddress()); // leader处理接收到ACK消息
                    break;
                case Leader.PING: // 接收到learner心跳消息
                    // Process the touches
                    ByteArrayInputStream bis = new ByteArrayInputStream(qp.getData());
                    DataInputStream dis = new DataInputStream(bis);
                    while (dis.available() > 0) { // learner发送心跳时会发送当前活动的session信息（leather和客户端的session）
                        long sess = dis.readLong(); // sessionId
                        int to = dis.readInt();     // session超时时间
                        leader.zk.touch(sess, to); // 续租session
                    }
                    break;
                case Leader.REVALIDATE: // 接收到learner验证session消息
                    bis = new ByteArrayInputStream(qp.getData());
                    dis = new DataInputStream(bis);
                    long id = dis.readLong(); // sessionId
                    int to = dis.readInt();   // session超时时间
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(bos);
                    dos.writeLong(id);
                    boolean valid = leader.zk.touch(id, to); // leader续租session
                    if (valid) { // 验证成功
                        try {
                            //set the session owner as the follower that owns the session
                            leader.zk.setOwner(id, this); // 设置session owner
                        } catch (SessionExpiredException e) {
                            LOG.error("Somehow session " + Long.toHexString(id) +
                                    " expired right after being renewed! (impossible)", e);
                        }
                    }
                    if (LOG.isTraceEnabled()) {
                        ZooTrace.logTraceMessage(LOG,
                                                 ZooTrace.SESSION_TRACE_MASK,
                                                 "Session 0x" + Long.toHexString(id)
                                                 + " is valid: "+ valid);
                    }
                    dos.writeBoolean(valid);
                    qp.setData(bos.toByteArray());
                    queuedPackets.add(qp); // 发送验证结果给learner
                    break;
                case Leader.REQUEST: // 接收learner发送的事务请求（会改变zk结点状态的请求）
                    bb = ByteBuffer.wrap(qp.getData());
                    sessionId = bb.getLong();
                    cxid = bb.getInt();
                    type = bb.getInt();
                    bb = bb.slice();
                    Request si;
                    if(type == OpCode.sync) { // 请求是sync同步请求
                        si = new LearnerSyncRequest(this, sessionId, cxid, type, bb, qp.getAuthinfo());
                    } else { // 其他类型请求
                        si = new Request(null, sessionId, cxid, type, bb, qp.getAuthinfo());
                    }
                    si.setOwner(this); // 设置请求owner
                    leader.zk.submitRequest(si); // leader提交请求，第一个请求处理器firstProcessor开始处理请求
                    break;
                default: // 未知类型请求
                    LOG.warn("unexpected quorum packet, type: {}", packetToString(qp));
                    break;
                }
            }
        } catch (IOException e) { // 异常
            if (sock != null && !sock.isClosed()) { // 关闭leader和learner的socket连接
                LOG.error("Unexpected exception causing shutdown while sock " + "still open", e);
            	//close the socket to make sure the 
            	//other side can see it being close
            	try {
            		sock.close();
            	} catch(IOException ie) {
            		// do nothing
            	}
            }
        } catch (InterruptedException e) {
            LOG.error("Unexpected exception causing shutdown", e);
        } finally { // 最后关闭该LearnerHandler
            LOG.warn("******* GOODBYE " 
                    + (sock != null ? sock.getRemoteSocketAddress() : "<null>")
                    + " ********");
            shutdown();
        }
    }

    // 关闭该LearnerHandler
    public void shutdown() {
        // Send the packet of death
        try {
            queuedPackets.put(proposalOfDeath); // 加入发送线程结束标志请求
        } catch (InterruptedException e) {
            LOG.warn("Ignoring unexpected exception", e);
        }
        try {
            if (sock != null && !sock.isClosed()) { // 关闭socket
                sock.close();
            }
        } catch (IOException e) {
            LOG.warn("Ignoring unexpected exception during socket close", e);
        }
        this.interrupt(); // 中断LearnerHandler线程
        leader.removeLearnerHandler(this); // leader中移除关闭的learner对应的handler
    }

    // 获取下一次ack截止时间tickOfNextAckDeadline
    public long tickOfNextAckDeadline() {
        return tickOfNextAckDeadline;
    }

    /**
     * ping calls from the leader to the peers
     * leader发送心跳给learner
     */
    public void ping() {
        long id;
        if (syncLimitCheck.check(System.nanoTime())) { // 检查是否超过syncLimit时间
            synchronized(leader) {
                id = leader.lastProposed; // 心跳请求中发送最后一次提议的zxid
            }
            // 发送心跳请求
            QuorumPacket ping = new QuorumPacket(Leader.PING, id, null, null);
            queuePacket(ping);
        } else { // 超过syncLimit时间，关闭该LearnerHandler
            LOG.warn("Closing connection to peer due to transaction timeout.");
            shutdown();
        }
    }

    // 添加请求到发送队列
    void queuePacket(QuorumPacket p) {
        queuedPackets.add(p);
    }

    // leader和learner状态是否同步
    // LearnerHandler线程存活并且leader的tick没有超过下一次ack截止时间tickOfNextAckDeadline
    public boolean synced() {
        return isAlive() && leader.self.tick.get() <= tickOfNextAckDeadline;
    }
}
