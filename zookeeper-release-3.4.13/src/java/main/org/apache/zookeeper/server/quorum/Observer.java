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

import org.apache.jute.Record;
import org.apache.zookeeper.server.ObserverBean;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.apache.zookeeper.server.util.SerializeUtils;
import org.apache.zookeeper.txn.TxnHeader;

/**
 * Observers are peers that do not take part in the atomic broadcast protocol.
 * Instead, they are informed of successful proposals by the Leader. Observers
 * therefore naturally act as a relay point for publishing the proposal stream
 * and can relieve Followers of some of the connection load. Observers may
 * submit proposals, but do not vote in their acceptance. 
 * 观察者
 *
 * See ZOOKEEPER-368 for a discussion of this feature. 
 */
public class Observer extends Learner{      

    Observer(QuorumPeer self, ObserverZooKeeperServer observerZooKeeperServer) {
        this.self = self;
        this.zk = observerZooKeeperServer;
    }

    // 观察者服务信息
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Observer ").append(sock);        
        sb.append(" pendingRevalidationCount:").append(pendingRevalidations.size()); // 挂起验证session请求的数量
        return sb.toString();
    }
    
    /**
     * the main method called by the observer to observe the leader
     * 观察leader，提供观察者服务
     *
     * @throws InterruptedException
     */
    void observeLeader() throws InterruptedException {
        zk.registerJMX(new ObserverBean(this, zk), self.jmxLocalPeerBean); // 注册JMX

        try {
            QuorumServer leaderServer = findLeader(); // 找到当前的leader服务
            LOG.info("Observing " + leaderServer.addr);
            try {
                connectToLeader(leaderServer.addr, leaderServer.hostname);    // 连接leader
                long newLeaderZxid = registerWithLeader(Leader.OBSERVERINFO); // 注册观察者服务到leader

                syncWithLeader(newLeaderZxid); // 同步leader数据
                QuorumPacket qp = new QuorumPacket();
                while (this.isRunning()) { // 开始接收leader信息，进行处理
                    readPacket(qp);    // 读取leader数据
                    processPacket(qp); // 处理leader数据
                }
            } catch (Exception e) {
                LOG.warn("Exception when observing the leader", e);
                try {
                    sock.close(); // 关闭socket
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
    
                // clear pending revalidations 清空挂起等待revalidate的session
                pendingRevalidations.clear();
            }
        } finally { // 注销JMX
            zk.unregisterJMX(this);
        }
    }
    
    /**
     * Controls the response of an observer to the receipt of a quorumpacket
     * 处理接收leader的数据
     * @param qp
     * @throws IOException
     */
    protected void processPacket(QuorumPacket qp) throws IOException{
        switch (qp.getType()) {
        case Leader.PING: // 接收到的心跳数据
            ping(qp);
            break;
        case Leader.PROPOSAL: // 接收到的提议数据，观察者不做任何处理
            LOG.warn("Ignoring proposal");
            break;
        case Leader.COMMIT:   // 接收到提交数据，观察者不做任何处理
            LOG.warn("Ignoring commit");            
            break;            
        case Leader.UPTODATE: // 该阶段不应该接收到leader的UPTODATE数据
            LOG.error("Received an UPTODATE message after Observer started");
            break;
        case Leader.REVALIDATE: // 接收到leader的验证session数据
            revalidate(qp); // 取出之前挂起的验证session，进行验证
            break;
        case Leader.SYNC: // 接收到leader的同步数据
            ((ObserverZooKeeperServer)zk).sync(); // 进行同步处理
            break;
        case Leader.INFORM: // 接收到leader的INFORM数据（leader提交的数据会以INFORM类型发送给observer）
            TxnHeader hdr = new TxnHeader();
            Record txn = SerializeUtils.deserializeTxn(qp.getData(), hdr);
            Request request = new Request (null, hdr.getClientId(), hdr.getCxid(), hdr.getType(), null, null); // 生成请求
            request.txn = txn;
            request.hdr = hdr;
            ObserverZooKeeperServer obs = (ObserverZooKeeperServer)zk;
            obs.commitRequest(request); // 提交事务请求
            break;
        default:
            LOG.error("Invalid packet type: {} received by Observer", qp.getType());
        }
    }

    /**
     * Shutdown the Observer.
     * 关闭观察者服务
     */
    public void shutdown() {       
        LOG.info("shutdown called", new Exception("shutdown Observer"));
        super.shutdown();
    }
}

