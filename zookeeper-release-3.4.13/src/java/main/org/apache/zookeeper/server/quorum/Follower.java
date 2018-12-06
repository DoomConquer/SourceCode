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
import java.net.InetSocketAddress;

import org.apache.jute.Record;
import org.apache.zookeeper.common.Time;
import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.apache.zookeeper.server.util.SerializeUtils;
import org.apache.zookeeper.server.util.ZxidUtils;
import org.apache.zookeeper.txn.TxnHeader;

/**
 * This class has the control logic for the Follower.
 * follower类
 */
public class Follower extends Learner{

    private long lastQueued; // 提议的序号
    // This is the same object as this.zk, but we cache the downcast op
    final FollowerZooKeeperServer fzk; // 和this.zk是同一个ZooKeeperServer
    
    Follower(QuorumPeer self, FollowerZooKeeperServer zk) {
        this.self = self;
        this.zk = zk;
        this.fzk = zk;
    }

    // 返回follower服务信息
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Follower ").append(sock);
        sb.append(" lastQueuedZxid:").append(lastQueued);
        sb.append(" pendingRevalidationCount:").append(pendingRevalidations.size());
        return sb.toString();
    }

    /**
     * the main method called by the follower to follow the leader
     * 执行follower跟随leader的逻辑
     *
     * @throws InterruptedException
     */
    void followLeader() throws InterruptedException {
        self.end_fle = Time.currentElapsedTime();
        long electionTimeTaken = self.end_fle - self.start_fle; // 选举耗费时间
        self.setElectionTimeTaken(electionTimeTaken);
        LOG.info("FOLLOWING - LEADER ELECTION TOOK - {}", electionTimeTaken);
        self.start_fle = 0; // 重置
        self.end_fle = 0;
        fzk.registerJMX(new FollowerBean(this, zk), self.jmxLocalPeerBean); // 注册JMX
        try {
            QuorumServer leaderServer = findLeader(); // 找到当前的leader服务
            try {
                connectToLeader(leaderServer.addr, leaderServer.hostname);   // 连接leader
                long newEpochZxid = registerWithLeader(Leader.FOLLOWERINFO); // 注册该follower到leader

                //check to see if the leader zxid is lower than ours
                //this should never happen but is just a safety check
                long newEpoch = ZxidUtils.getEpochFromZxid(newEpochZxid); // 获取leader的epoch
                if (newEpoch < self.getAcceptedEpoch()) { // leader的epoch小于follower服务的epoch，抛出异常
                    LOG.error("Proposed leader epoch " + ZxidUtils.zxidToString(newEpochZxid)
                            + " is less than our accepted epoch " + ZxidUtils.zxidToString(self.getAcceptedEpoch()));
                    throw new IOException("Error: Epoch of leader is lower");
                }
                syncWithLeader(newEpochZxid); // 同步leader数据
                QuorumPacket qp = new QuorumPacket();
                while (this.isRunning()) { // 一直连接leader，进行数据处理
                    readPacket(qp);    // 读取leader数据
                    processPacket(qp); // 处理leader数据
                }
            } catch (Exception e) {
                LOG.warn("Exception when following the leader", e);
                try {
                    sock.close(); // 关闭socket
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
    
                // clear pending revalidations 清空挂起等待revalidate的session
                pendingRevalidations.clear();
            }
        } finally { // 最后注销JMX
            zk.unregisterJMX((Learner)this);
        }
    }

    /**
     * Examine the packet received in qp and dispatch based on its contents.
     * 处理数据包
     * @param qp
     * @throws IOException
     */
    protected void processPacket(QuorumPacket qp) throws IOException{
        switch (qp.getType()) {
        case Leader.PING: // 接收到leader心跳
            ping(qp); // 将follower活动的session状态返回给leader
            break;
        case Leader.PROPOSAL: // 接收到leader提议
            TxnHeader hdr = new TxnHeader();
            Record txn = SerializeUtils.deserializeTxn(qp.getData(), hdr);
            if (hdr.getZxid() != lastQueued + 1) { // 提议序号顺序不对
                LOG.warn("Got zxid 0x"
                        + Long.toHexString(hdr.getZxid())
                        + " expected 0x"
                        + Long.toHexString(lastQueued + 1));
            }
            lastQueued = hdr.getZxid();
            fzk.logRequest(hdr, txn); // 进行事务日志处理
            break;
        case Leader.COMMIT: // 接收到leader提交
            fzk.commit(qp.getZxid()); // 提交事务请求
            break;
        case Leader.UPTODATE: // 接收到leader的UPTODATE数据，正常情况不应该在这个过程接收到leader的UPTODATE
            LOG.error("Received an UPTODATE message after Follower started");
            break;
        case Leader.REVALIDATE: // 接收到leader验证session数据
            revalidate(qp); // 验证session，将之前挂起的session取出进行验证
            break;
        case Leader.SYNC: // 接收到leader的同步信息
            fzk.sync(); // 提交follower之前接受到客户端的同步请求
            break;
        default: // 未知类型
            LOG.error("Invalid packet type: {} received by Observer", qp.getType());
        }
    }

    /**
     * The zxid of the last operation seen 获取follower服务的zxid
     * @return zxid
     */
    public long getZxid() {
        try {
            synchronized (fzk) {
                return fzk.getZxid();
            }
        } catch (NullPointerException e) {
            LOG.warn("error getting zxid", e);
        }
        return -1;
    }
    
    /**
     * The zxid of the last operation queued 获取最后的提议的序号
     * @return zxid
     */
    protected long getLastQueued() {
        return lastQueued;
    }

    // 关闭follower
    @Override
    public void shutdown() {    
        LOG.info("shutdown called", new Exception("shutdown Follower"));
        super.shutdown();
    }
}
