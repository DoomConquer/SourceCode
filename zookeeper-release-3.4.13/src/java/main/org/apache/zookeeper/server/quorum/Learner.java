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
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jute.BinaryInputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.InputArchive;
import org.apache.jute.OutputArchive;
import org.apache.jute.Record;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.ServerCnxn;
import org.apache.zookeeper.server.ZooTrace;
import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.apache.zookeeper.server.util.SerializeUtils;
import org.apache.zookeeper.server.util.ZxidUtils;
import org.apache.zookeeper.txn.TxnHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the superclass of two of the three main actors in a ZK
 * ensemble: Followers and Observers. Both Followers and Observers share 
 * a good deal of code which is moved into Peer to avoid duplication.
 * 跟随者和观察者的父类（避免跟随者和观察者公共代码重复）
 */
public class Learner {
    // 封装未提交事务请求数据
    static class PacketInFlight {
        TxnHeader hdr;
        Record rec;
    }
    QuorumPeer self;
    LearnerZooKeeperServer zk; // learner服务
    
    protected BufferedOutputStream bufferedOutput; // 缓冲输出流（向leader发送数据）
    
    protected Socket sock; // socket连接
    
    /**
     * Socket getter
     * @return 
     */
    public Socket getSocket() {
        return sock;
    }

    // leader网络输入输出流
    protected InputArchive leaderIs;
    protected OutputArchive leaderOs;

    /** the protocol version of the leader 协议版本*/
    protected int leaderProtocolVersion = 0x01;
    
    protected static final Logger LOG = LoggerFactory.getLogger(Learner.class);

    // tcp nodelay属性
    static final private boolean nodelay = System.getProperty("follower.nodelay", "true").equals("true");
    static {
        LOG.info("TCP NoDelay set to: " + nodelay);
    }

    // 挂起的需要验证session的连接
    final ConcurrentHashMap<Long, ServerCnxn> pendingRevalidations = new ConcurrentHashMap<Long, ServerCnxn>();
    
    public int getPendingRevalidationsCount() {
        return pendingRevalidations.size();
    }
    
    /**
     * validate a session for a client
     * 验证客户端session
     *
     * @param clientId
     *                the client to be revalidated（sessionId）
     * @param timeout
     *                the timeout for which the session is valid
     * @return
     * @throws IOException
     */
    void validateSession(ServerCnxn cnxn, long clientId, int timeout) throws IOException {
        LOG.info("Revalidating client: 0x" + Long.toHexString(clientId));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(clientId);
        dos.writeInt(timeout);
        dos.close();
        QuorumPacket qp = new QuorumPacket(Leader.REVALIDATE, -1, baos.toByteArray(), null);
        pendingRevalidations.put(clientId, cnxn); // 挂起客户端连接
        if (LOG.isTraceEnabled()) {
            ZooTrace.logTraceMessage(LOG,
                                     ZooTrace.SESSION_TRACE_MASK,
                                     "To validate session 0x"
                                     + Long.toHexString(clientId));
        }
        writePacket(qp, true); // 发送重新验证session数据包给leader
    }
    
    /**
     * write a packet to the leader
     * 写入数据到输出流
     *
     * @param pp      the proposal packet to be sent to the leader
     *
     * @param flush   flush now 是否立即刷新流
     *
     * @throws IOException
     */
    void writePacket(QuorumPacket pp, boolean flush) throws IOException {
        synchronized (leaderOs) {
            if (pp != null) {
                leaderOs.writeRecord(pp, "packet");
            }
            if (flush) {
                bufferedOutput.flush(); // 立即刷新输出流
            }
        }
    }

    /**
     * read a packet from the leader
     * 从leader读取数据包
     *
     * @param pp
     *                the packet to be instantiated
     * @throws IOException
     */
    void readPacket(QuorumPacket pp) throws IOException {
        synchronized (leaderIs) {
            leaderIs.readRecord(pp, "packet"); // 读取数据放入pp（反序列化）
        }
        long traceMask = ZooTrace.SERVER_PACKET_TRACE_MASK;
        if (pp.getType() == Leader.PING) { // 心跳数据包
            traceMask = ZooTrace.SERVER_PING_TRACE_MASK;
        }
        if (LOG.isTraceEnabled()) {
            ZooTrace.logQuorumPacket(LOG, traceMask, 'i', pp);
        }
    }
    
    /**
     * send a request packet to the leader
     * 发送request请求数据包给leader
     *
     * @param request
     *                the request from the client
     * @throws IOException
     */
    void request(Request request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream oa = new DataOutputStream(baos);
        oa.writeLong(request.sessionId);
        oa.writeInt(request.cxid);
        oa.writeInt(request.type);
        if (request.request != null) { // 写入request内容
            request.request.rewind(); // position重头开始
            int len = request.request.remaining();
            byte b[] = new byte[len];
            request.request.get(b);
            request.request.rewind();
            oa.write(b);
        }
        oa.close();
        QuorumPacket qp = new QuorumPacket(Leader.REQUEST, -1, baos.toByteArray(), request.authInfo);
        writePacket(qp, true);
    }
    
    /**
     * Returns the address of the node we think is the leader.
     * 返回leader节点
     */
    protected QuorumServer findLeader() {
        QuorumServer leaderServer = null;
        // Find the leader by id
        Vote current = self.getCurrentVote(); // 当前选票
        for (QuorumServer s : self.getView().values()) {
            if (s.id == current.getId()) {
                // Ensure we have the leader's correct IP address before attempting to connect.
                s.recreateSocketAddresses(); // 连接前确保leader的地址已经被解析（hostname解析成ip地址）
                leaderServer = s;
                break;
            }
        }
        if (leaderServer == null) { // 没有找到leader
            LOG.warn("Couldn't find the leader with id = " + current.getId());
        }
        return leaderServer;
    }
    
    /**
     * Establish a connection with the Leader found by findLeader. Retries
     * 5 times before giving up. 与findLeader找到的leader建立连接，5次重试机会
     * @param addr - the address of the Leader to connect to.
     * @throws IOException <li>if the socket connection fails on the 5th attempt</li>
     * <li>if there is an authentication failure while connecting to leader</li>
     * @throws ConnectException
     * @throws InterruptedException
     */
    protected void connectToLeader(InetSocketAddress addr, String hostname)
            throws IOException, ConnectException, InterruptedException {
        sock = new Socket();
        sock.setSoTimeout(self.tickTime * self.initLimit);
        for (int tries = 0; tries < 5; tries++) {
            try {
                sock.connect(addr, self.tickTime * self.syncLimit); // 连接leader
                sock.setTcpNoDelay(nodelay); // 设置TCP_NODELAY
                break;
            } catch (IOException e) {
                if (tries == 4) { // 5次重试后还是没连接上
                    LOG.error("Unexpected exception",e);
                    throw e;
                } else { // 接着重试
                    LOG.warn("Unexpected exception, tries="+tries+ ", connecting to " + addr,e);
                    sock = new Socket();
                    sock.setSoTimeout(self.tickTime * self.initLimit);
                }
            }
            Thread.sleep(1000); // 1秒后再重试
        }

        self.authLearner.authenticate(sock, hostname); // 进行sasl认证

        // 初始化leaderIs和leaderOs
        leaderIs = BinaryInputArchive.getArchive(new BufferedInputStream(sock.getInputStream()));
        bufferedOutput = new BufferedOutputStream(sock.getOutputStream());
        leaderOs = BinaryOutputArchive.getArchive(bufferedOutput);
    }
    
    /**
     * Once connected to the leader, perform the handshake protocol to
     * establish a following / observing connection.
     * 连接上leader后，通知leader跟随者或观察者的zxid，并同步leader的zxid
     *
     * @param pktType 数据包类型（FOLLOWERINFO或OBSERVERINFO）
     * @return the zxid the Leader sends for synchronization purposes.
     * @throws IOException
     */
    protected long registerWithLeader(int pktType) throws IOException{
        /*
         * Send follower info, including last zxid and sid
         * 发送learner信息
         */
    	long lastLoggedZxid = self.getLastLoggedZxid(); // 获取最后的zxid
        QuorumPacket qp = new QuorumPacket();
        qp.setType(pktType);
        qp.setZxid(ZxidUtils.makeZxid(self.getAcceptedEpoch(), 0));
        
        /*
         * Add sid to payload
         */
        LearnerInfo li = new LearnerInfo(self.getId(), 0x10000); // learner服务id信息和协议版本
        ByteArrayOutputStream bsid = new ByteArrayOutputStream();
        BinaryOutputArchive boa = BinaryOutputArchive.getArchive(bsid);
        boa.writeRecord(li, "LearnerInfo");
        qp.setData(bsid.toByteArray());
        
        writePacket(qp, true); // 发送FOLLOWERINFO或OBSERVERINFO数据给leader
        readPacket(qp); // 接收leader发送的消息
        final long newEpoch = ZxidUtils.getEpochFromZxid(qp.getZxid());
		if (qp.getType() == Leader.LEADERINFO) { // 新版本服务会接收到LEADERINFO类型信息
        	// we are connected to a 1.0 server so accept the new epoch and read the next packet
        	leaderProtocolVersion = ByteBuffer.wrap(qp.getData()).getInt(); // 版本信息
        	byte epochBytes[] = new byte[4];
        	final ByteBuffer wrappedEpochBytes = ByteBuffer.wrap(epochBytes);
        	if (newEpoch > self.getAcceptedEpoch()) { // 当前服务的epoch落后leader
        		wrappedEpochBytes.putInt((int)self.getCurrentEpoch()); // 发送给leader当前服务的epoch
        		self.setAcceptedEpoch(newEpoch); // 设置该服务的acceptedEpoch
        	} else if (newEpoch == self.getAcceptedEpoch()) { // acceptedEpoch和leader的相等，表示leader已经确认过该epoch（因为newEpoch是上阶段中选出的所有服务的最大的acceptedEpoch + 1）
        		// since we have already acked an epoch equal to the leaders, we cannot ack
        		// again, but we still need to send our lastZxid to the leader so that we can
        		// sync with it if it does assume leadership of the epoch.
        		// the -1 indicates that this reply should not count as an ack for the new epoch
                // 该服务acceptedEpoch和leader相等（leader已经确认过该epoch），但还是要发送该服务的lastLoggedZxid给leader，
                // 但是该回复不应该算为new epoch的ack确认，所以用-1表示不算入ack确认
                wrappedEpochBytes.putInt(-1);
        	} else { // leader的epoch小于该服务的epoch（leader落后），抛出异常
        		throw new IOException("Leaders epoch, " + newEpoch + " is less than accepted epoch, " + self.getAcceptedEpoch());
        	}
        	QuorumPacket ackNewEpoch = new QuorumPacket(Leader.ACKEPOCH, lastLoggedZxid, epochBytes, null);
        	writePacket(ackNewEpoch, true); // 回复leader LEADERINFO确认
            return ZxidUtils.makeZxid(newEpoch, 0);
        } else { // 旧版本会接收到NEWLEADER类型消息（收到NEWLEADER后才进行同步）
        	if (newEpoch > self.getAcceptedEpoch()) {
        		self.setAcceptedEpoch(newEpoch);
        	}
            if (qp.getType() != Leader.NEWLEADER) { // 第一个收到数据包应该是NEWLEADER类型
                LOG.error("First packet should have been NEWLEADER");
                throw new IOException("First packet should have been NEWLEADER");
            }
            return qp.getZxid();
        }
    }
    
    /**
     * Finally, synchronize our history with the Leader.
     * 与leader进行数据同步，leader等待接收到的ACKEPOCH，根据lastLoggedZxid决定和learner
     * 以什么方式（DIFF、SNAP、TRUNC）进行数据同步
     *
     * @param newLeaderZxid leader新的zxid
     * @throws IOException
     * @throws InterruptedException
     */
    protected void syncWithLeader(long newLeaderZxid) throws IOException, InterruptedException{
        QuorumPacket ack = new QuorumPacket(Leader.ACK, 0, null, null); // 完成同步的ack确认
        QuorumPacket qp = new QuorumPacket();
        long newEpoch = ZxidUtils.getEpochFromZxid(newLeaderZxid); // leader的epoch
        // In the DIFF case we don't need to do a snapshot because the transactions will sync on top of any existing snapshot
        // For SNAP and TRUNC the snapshot is needed to save that history
        boolean snapshotNeeded = true; // 是否需要进行数据快照
        readPacket(qp); // 获取leader数据包
        LinkedList<Long> packetsCommitted = new LinkedList<Long>(); // 保存leader提交的事务
        LinkedList<PacketInFlight> packetsNotCommitted = new LinkedList<PacketInFlight>(); // 提议但未提交的事务队列
        synchronized (zk) {
            if (qp.getType() == Leader.DIFF) { // 以DIFF方式进行数据同步，不用进行数据快照
                LOG.info("Getting a diff from the leader 0x{}", Long.toHexString(qp.getZxid()));
                snapshotNeeded = false;
            } else if (qp.getType() == Leader.SNAP) { // 以SNAP方式进行数据同步
                LOG.info("Getting a snapshot from leader 0x" + Long.toHexString(qp.getZxid()));
                // The leader is going to dump the database, clear our own database and read
                // leader准备进行数据快照，learner服务清空自己的数据库，准备接收leader的快照
                zk.getZKDatabase().clear();
                zk.getZKDatabase().deserializeSnapshot(leaderIs); // 反序列化接收的快照数据
                String signature = leaderIs.readString("signature"); // 读取签名
                if (!signature.equals("BenWasHere")) { // 签名不正确
                    LOG.error("Missing signature. Got " + signature);
                    throw new IOException("Missing signature");
                }
                zk.getZKDatabase().setlastProcessedZxid(qp.getZxid()); // 设置lastProcessedZxid
            } else if (qp.getType() == Leader.TRUNC) { // 以TRUNC方式进行数据同步，截取该服务lastzxid后的日志
                //we need to truncate the log to the lastzxid of the leader
                LOG.warn("Truncating log to get in sync with the leader 0x" + Long.toHexString(qp.getZxid()));
                boolean truncated = zk.getZKDatabase().truncateLog(qp.getZxid());
                if (!truncated) { // 截取失败
                    // not able to truncate the log
                    LOG.error("Not able to truncate the log " + Long.toHexString(qp.getZxid()));
                    System.exit(13); // 截取日志失败停止系统运行
                }
                zk.getZKDatabase().setlastProcessedZxid(qp.getZxid()); // 设置lastProcessedZxid
            } else { // 未知类型数据包
                LOG.error("Got unexpected packet from leader " + qp.getType() + " exiting ... " );
                System.exit(13);
            }
            zk.createSessionTracker(); // 创建LearnerSessionTracker
            
            long lastQueued = 0;

            // in Zab V1.0 (ZK 3.4+) we might take a snapshot when we get the NEWLEADER message, but in pre V1.0
            // we take the snapshot on the UPTODATE message, since Zab V1.0 also gets the UPTODATE (after the NEWLEADER)
            // we need to make sure that we don't take the snapshot twice.
            boolean isPreZAB1_0 = true; // 是否V1.0之前的版本（V1.0前接收到UPDATE会进行数据快照，而之后是NEWLEADER时会进行快照）
            //If we are not going to take the snapshot be sure the transactions are not applied in memory
            // but written out to the transaction log
            boolean writeToTxnLog = !snapshotNeeded; // 是否需要写事务日志（如果进行了数据快照就不需要写事务日志了，DIFF方式需要写事务日志）
            // we are now going to start getting transactions to apply followed by an UPTODATE
            outerLoop:
            while (self.isRunning()) {
                readPacket(qp); // 接收leader数据
                switch(qp.getType()) {
                // 接收到leader的PROPOSAL数据（同步区间接收到PROPOSAL数据，先保存在packetsNotCommitted队列），
                // 该PROPOSAL数据是DIFF同步方式leader发送的数据（包括两个数据库对比的差异数据和同步过程中leader新接收到的提议数据），
                // 同步完成后leader接收到事务请求也会发送PROPOSAL，该数据会在Follower的processPacket中处理
                case Leader.PROPOSAL:
                    PacketInFlight pif = new PacketInFlight();
                    pif.hdr = new TxnHeader();
                    pif.rec = SerializeUtils.deserializeTxn(qp.getData(), pif.hdr); // 从数据包中反序列化事务
                    if (pif.hdr.getZxid() != lastQueued + 1) { // 接收的事务id没有增加
                    LOG.warn("Got zxid 0x"
                            + Long.toHexString(pif.hdr.getZxid())
                            + " expected 0x"
                            + Long.toHexString(lastQueued + 1));
                    }
                    lastQueued = pif.hdr.getZxid();
                    packetsNotCommitted.add(pif); // 加入到未提交事务队列（等待leader确认后commit）
                    break;
                case Leader.COMMIT: // 接收到leader的COMMIT（接收到PROPOSAL和接收到COMMIT必须是对应的，即COMMIT的必须是packetsNotCommitted队头的元素）
                    if (!writeToTxnLog) { // 不需要写事务日志
                        pif = packetsNotCommitted.peekFirst();
                        if (pif.hdr.getZxid() != qp.getZxid()) { // 接收到提交的事务不是未提交事务队列中队头元素（为了保证事务有序）
                            LOG.warn("Committing " + qp.getZxid() + ", but next proposal is " + pif.hdr.getZxid());
                        } else {
                            zk.processTxn(pif.hdr, pif.rec); // 将该事务应用到该服务的数据库
                            packetsNotCommitted.remove(); // 移出未提交事务队列
                        }
                    } else { // DIFF方式需要写事务日志
                        packetsCommitted.add(qp.getZxid()); // 添加到packetsCommitted队列，下面统一处理
                    }
                    break;
                case Leader.INFORM: // 接收到leader的INFORM消息，只有观察者会收到该数据
                    /*
                     * Only observer get this type of packet. We treat this
                     * as receiving PROPOSAL and COMMMIT.
                     * 该信息看成是PROPOSAL和COMMMIT，操作也和PROPOSAL、COMMMIT基本一样
                     */
                    PacketInFlight packet = new PacketInFlight();
                    packet.hdr = new TxnHeader();
                    packet.rec = SerializeUtils.deserializeTxn(qp.getData(), packet.hdr);
                    // Log warning message if txn comes out-of-order 日志提醒，如果事务没按顺序
                    if (packet.hdr.getZxid() != lastQueued + 1) {
                        LOG.warn("Got zxid 0x"
                                + Long.toHexString(packet.hdr.getZxid())
                                + " expected 0x"
                                + Long.toHexString(lastQueued + 1));
                    }
                    lastQueued = packet.hdr.getZxid();
                    if (!writeToTxnLog) { // 不写事务日志，直接应用到数据库
                        // Apply to db directly if we haven't taken the snapshot
                        zk.processTxn(packet.hdr, packet.rec);
                    } else { // 写事务日志
                        packetsNotCommitted.add(packet);
                        packetsCommitted.add(qp.getZxid());
                    }
                    break;
                case Leader.UPTODATE: // 接收到leader的UPTODATE消息
                    // V1.0之前的版本，收到UPTODATE消息会进行数据快照，V1.0之后的版本在收到NEWLEADER时会进行数据快照，
                    // isPreZAB1_0是为了防止进行两次数据快照
                    if (isPreZAB1_0) {
                        zk.takeSnapshot();
                        self.setCurrentEpoch(newEpoch); // 设置当前epoch（已经完成和leader同步）
                    }
                    self.cnxnFactory.setZooKeeperServer(zk); // 设置zk客户端服务，让客户端可以连接follower或observer
                    break outerLoop; // 结束跳出循环
                case Leader.NEWLEADER: // Zab 1.0新版本会接收到该消息，1.0之前的版本在registerWithLeader过程就已经收到该消息了
                    // Getting NEWLEADER here instead of in discovery
                    // means this is Zab 1.0
                    // Create updatingEpoch file and remove it after current
                    // epoch is set. QuorumPeer.loadDataBase() uses this file to
                    // detect the case where the server was terminated after
                    // taking a snapshot but before setting the current epoch.
                    // 创建updateEpoch文件，设置当前epoch后删除。QuorumPeer.loadDataBase()
                    // 使用这个文件来检测进行数据快照后但是当前epoch还没设置服务被终止的情况。
                    File updating = new File(self.getTxnFactory().getSnapDir(),
                                        QuorumPeer.UPDATING_EPOCH_FILENAME); // updateEpoch文件
                    if (!updating.exists() && !updating.createNewFile()) {
                        throw new IOException("Failed to create " + updating.toString());
                    }
                    if (snapshotNeeded) { // 进行数据快照
                        zk.takeSnapshot();
                    }
                    self.setCurrentEpoch(newEpoch); // 设置当前epoch（已经完成和leader同步）
                    if (!updating.delete()) { // 删除updateEpoch文件
                        throw new IOException("Failed to delete " + updating.toString());
                    }
                    // 刚进行了数据快照，之后的请求需要写到事务日志里，不能直接应用到数据库
                    // Anything after this needs to go to the transaction log, not applied directly in memory
                    writeToTxnLog = true;
                    isPreZAB1_0 = false;
                    // 返回NEWLEADER的ACK确认给leader，leader在收到过半的确认后才能继续后面的流程
                    writePacket(new QuorumPacket(Leader.ACK, newLeaderZxid, null, null), true);
                    break;
                }
            }
        }
        ack.setZxid(ZxidUtils.makeZxid(newEpoch, 0));
        writePacket(ack, true); // 发送ACK确认给leader（可能为了兼容旧版本的leader）
        sock.setSoTimeout(self.tickTime * self.syncLimit); // 设置socket超时
        zk.startup(); // 启动zk客户端服务
        /*
         * Update the election vote here to ensure that all members of the
         * ensemble report the same vote to new servers that start up and
         * send leader election notifications to the ensemble.
         * 
         * @see https://issues.apache.org/jira/browse/ZOOKEEPER-1732
         */
        self.updateElectionVote(newEpoch); // 更新当前服务的选票epoch为newEpoch

        // We need to log the stuff that came in between the snapshot and the uptodate
        // 处理在进行数据快照到接收UPTODATE区间leader接收和提交的事务
        if (zk instanceof FollowerZooKeeperServer) { // 如果该服务是跟随者
            FollowerZooKeeperServer fzk = (FollowerZooKeeperServer) zk;
            for(PacketInFlight p : packetsNotCommitted) { // 将没有提交的提议数据写到事务日志
                fzk.logRequest(p.hdr, p.rec);
            }
            for(Long zxid : packetsCommitted) { // 提交已经完成commit的事务
                fzk.commit(zxid);
            }
        } else if (zk instanceof ObserverZooKeeperServer) { // 如果该服务是观察者
            // Similar to follower, we need to log requests between the snapshot and UPTODATE
            ObserverZooKeeperServer ozk = (ObserverZooKeeperServer) zk;
            // 取出没有提交的提议，和packetsCommitted中应该是对应的，旧版的leader会发送outstanding中的提议给观察者，
            // 这里打印警告（不影响结果正确性）
            for (PacketInFlight p : packetsNotCommitted) {
                Long zxid = packetsCommitted.peekFirst();
                if (p.hdr.getZxid() != zxid) {
                    // log warning message if there is no matching commit
                    // old leader send outstanding proposal to observer
                    LOG.warn("Committing " + Long.toHexString(zxid)
                            + ", but next proposal is "
                            + Long.toHexString(p.hdr.getZxid()));
                    continue; // 不匹配继续（连接旧版的leader可能会不匹配）
                }
                packetsCommitted.remove(); // 移除对头元素
                // 生成请求
                Request request = new Request(null, p.hdr.getClientId(), p.hdr.getCxid(), p.hdr.getType(), null, null);
                request.txn = p.rec;
                request.hdr = p.hdr;
                ozk.commitRequest(request); // 观察者服务提交事务请求
            }
        } else { // 未知类型
            // New server type need to handle in-flight packets
            throw new UnsupportedOperationException("Unknown server type");
        }
    }

    // 验证session（接收到leader返回的验证session信息，learner的客户端session信息在leader也会保存，
    // learner验证session时先发送REVALIDATE给leader，leader验证后会将结果返回给learner）
    protected void revalidate(QuorumPacket qp) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(qp.getData());
        DataInputStream dis = new DataInputStream(bis);
        long sessionId = dis.readLong();
        boolean valid = dis.readBoolean();
        ServerCnxn cnxn = pendingRevalidations.remove(sessionId); // 取出挂起的客户端连接
        if (cnxn == null) {
            LOG.warn("Missing session 0x"
                    + Long.toHexString(sessionId)
                    + " for validation");
        } else {
            zk.finishSessionInit(cnxn, valid); // 完成session初始化（续租session）
        }
        if (LOG.isTraceEnabled()) {
            ZooTrace.logTraceMessage(LOG,
                    ZooTrace.SESSION_TRACE_MASK,
                    "Session 0x" + Long.toHexString(sessionId)
                    + " is valid: " + valid);
        }
    }

    // 返回心跳给leader（发送learner活动的session信息）
    protected void ping(QuorumPacket qp) throws IOException {
        // Send back the ping with our session data
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        HashMap<Long, Integer> touchTable = zk.getTouchSnapshot(); // 获取活动的session（map中内容sessionId -> sessionTimeout）
        for (Entry<Long, Integer> entry : touchTable.entrySet()) {
            dos.writeLong(entry.getKey());
            dos.writeInt(entry.getValue());
        }
        qp.setData(bos.toByteArray());
        writePacket(qp, true); // 发送给leader
    }

    /**
     * Shutdown the Peer
     * 关闭服务
     */
    public void shutdown() {
        // set the zookeeper server to null
        self.cnxnFactory.setZooKeeperServer(null);
        // clear all the connections
        self.cnxnFactory.closeAll(); // 关闭客户端所有连接
        // shutdown previous zookeeper
        if (zk != null) {
            zk.shutdown(); // 关闭learner服务
        }
    }

    // 该服务（follower或observer）是否运行中
    boolean isRunning() {
        return self.isRunning() && zk.isRunning();
    }
}
