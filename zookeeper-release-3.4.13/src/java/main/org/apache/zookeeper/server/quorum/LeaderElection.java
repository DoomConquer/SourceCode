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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.zookeeper.jmx.MBeanRegistry;
import org.apache.zookeeper.server.quorum.Vote;
import org.apache.zookeeper.server.quorum.QuorumPeer.LearnerType;
import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.apache.zookeeper.server.quorum.QuorumPeer.ServerState;

/**
 * @deprecated This class has been deprecated as of release 3.4.0.
 * 旧版leader选举
 */
@Deprecated
public class LeaderElection implements Election  {
    private static final Logger LOG = LoggerFactory.getLogger(LeaderElection.class);
    protected static final Random epochGen = new Random(); // 选举轮次随机数生成器

    protected QuorumPeer self;

    public LeaderElection(QuorumPeer self) {
        this.self = self;
    }

    // 选举结果
    protected static class ElectionResult {
        public Vote vote; // 记录zxid更大或zxid相等服务id更大的选票

        public int count; // 记录zxid更大或zxid相等服务id更大的选票的数量

        public Vote winner; // 获胜选票获胜选票

        public int winningCount; // 获胜选票的数量

        public int numValidVotes; // 有效选票数量
    }

    // 计算选票
    protected ElectionResult countVotes(HashMap<InetSocketAddress, Vote> votes, HashSet<Long> heardFrom) {
        final ElectionResult result = new ElectionResult(); // 选举结果
        // Initialize with null vote 初始化选举结果为最小值
        result.vote = new Vote(Long.MIN_VALUE, Long.MIN_VALUE);
        result.winner = new Vote(Long.MIN_VALUE, Long.MIN_VALUE);

        // First, filter out votes from unheard-from machines. Then
        // make the views consistent. Sometimes peers will have
        // different zxids for a server depending on timing.
        final HashMap<InetSocketAddress, Vote> validVotes = new HashMap<InetSocketAddress, Vote>(); // 有效选票
        final Map<Long, Long> maxZxids = new HashMap<Long,Long>(); // 记录选票最大的zxid
        for (Map.Entry<InetSocketAddress, Vote> e : votes.entrySet()) {
            // Only include votes from machines that we heard from
            final Vote v = e.getValue();
            if (heardFrom.contains(v.getId())) {
                validVotes.put(e.getKey(), v);
                Long val = maxZxids.get(v.getId());
                if (val == null || val < v.getZxid()) {
                    maxZxids.put(v.getId(), v.getZxid());
                }
            }
        }

        // Make all zxids for a given vote id equal to the largest zxid seen for that id
        // 使所有的选票的zxid等于最大的zxid
        for (Map.Entry<InetSocketAddress, Vote> e : validVotes.entrySet()) {
            final Vote v = e.getValue();
            Long zxid = maxZxids.get(v.getId());
            if (v.getZxid() < zxid) {
                // This is safe inside an iterator as per
                // http://download.oracle.com/javase/1.5.0/docs/api/java/util/Map.Entry.html
                e.setValue(new Vote(v.getId(), zxid, v.getElectionEpoch(), v.getPeerEpoch(), v.getState()));
            }
        }

        result.numValidVotes = validVotes.size();

        final HashMap<Vote, Integer> countTable = new HashMap<Vote, Integer>(); // 计算选票
        // Now do the tally 计算
        for (Vote v : validVotes.values()) {
            Integer count = countTable.get(v);
            if (count == null) {
                count = 0;
            }
            countTable.put(v, count + 1);
            if (v.getId() == result.vote.getId()) {
                result.count++;
            } else if (v.getZxid() > result.vote.getZxid()
                    || (v.getZxid() == result.vote.getZxid() && v.getId() > result.vote.getId())) { // 保留zxid最大或zxid相等服务id最大的选票
                result.vote = v;
                result.count = 1;
            }
        }
        result.winningCount = 0; // 选票最多的数量
        LOG.info("Election tally: ");
        for (Entry<Vote, Integer> entry : countTable.entrySet()) {
            if (entry.getValue() > result.winningCount) {
                result.winningCount = entry.getValue();
                result.winner = entry.getKey();
            }
            LOG.info(entry.getKey().getId() + "\t-> " + entry.getValue());
        }
        return result;
    }

    /**
     * There is nothing to shutdown in this implementation of
     * leader election, so we simply have an empty method.
     */
    public void shutdown(){}
    
    /**
     * Invoked in QuorumPeer to find or elect a new leader.
     * 进行选举leader
     * 
     * @throws InterruptedException
     */
    public Vote lookForLeader() throws InterruptedException {
        try { // 注册JMX
            self.jmxLeaderElectionBean = new LeaderElectionBean();
            MBeanRegistry.getInstance().register(self.jmxLeaderElectionBean, self.jmxLocalPeerBean);
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            self.jmxLeaderElectionBean = null;
        }

        try {
            self.setCurrentVote(new Vote(self.getId(), self.getLastLoggedZxid()));
            // We are going to look for a leader by casting a vote for ourself
            byte requestBytes[] = new byte[4];
            ByteBuffer requestBuffer = ByteBuffer.wrap(requestBytes);
            byte responseBytes[] = new byte[28];
            ByteBuffer responseBuffer = ByteBuffer.wrap(responseBytes);
            /* The current vote for the leader. Initially me! */
            DatagramSocket s = null;
            try { // 创建socket连接
                s = new DatagramSocket();
                s.setSoTimeout(200);
            } catch (SocketException e1) {
                LOG.error("Socket exception when creating socket for leader election", e1);
                System.exit(4);
            }
            // 数据报文
            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length);
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
            int xid = epochGen.nextInt(); // 选举轮次
            while (self.isRunning()) {
                HashMap<InetSocketAddress, Vote> votes = new HashMap<InetSocketAddress, Vote>(self.getVotingView().size());

                requestBuffer.clear();
                requestBuffer.putInt(xid);
                requestPacket.setLength(4);
                HashSet<Long> heardFrom = new HashSet<Long>(); // 保存接收到数据的远程服务id
                for (QuorumServer server : self.getVotingView().values()) {
                    LOG.info("Server address: " + server.addr);
                    try {
                        requestPacket.setSocketAddress(server.addr); // 设置socket地址
                    } catch (IllegalArgumentException e) {
                        // Sun doesn't include the address that causes this
                        // exception to be thrown, so we wrap the exception
                        // in order to capture this critical detail.
                        throw new IllegalArgumentException(
                                "Unable to set socket address on packet, msg:"
                                + e.getMessage() + " with addr:" + server.addr,
                                e);
                    }

                    try {
                        s.send(requestPacket); // 发送xid请求
                        responsePacket.setLength(responseBytes.length);
                        s.receive(responsePacket); // 该方法阻塞直到接收到响应数据
                        // 接收数据长度不相符，只有looking，leading，following状态返回的响应是28字节，int + long + long + long
                        if (responsePacket.getLength() != responseBytes.length) {
                            LOG.error("Got a short response: " + responsePacket.getLength());
                            continue;
                        }
                        responseBuffer.clear();
                        // 读取响应的xid（所有的请求都会返回响应，包括观察者，但观察者返回的数据长度不足28字节，响应头都是请求中的xid）
                        int recvedXid = responseBuffer.getInt();
                        if (recvedXid != xid) {
                            LOG.error("Got bad xid: expected " + xid + " got " + recvedXid);
                            continue;
                        }
                        long peerId = responseBuffer.getLong(); // 远程服务id
                        heardFrom.add(peerId);
                        //if(server.id != peerId){
                            Vote vote = new Vote(responseBuffer.getLong(), responseBuffer.getLong());
                            InetSocketAddress addr = (InetSocketAddress) responsePacket.getSocketAddress();
                            votes.put(addr, vote); // 保存接收的数据生成的选票
                        //}
                    } catch (IOException e) { // 忽略异常
                        LOG.warn("Ignoring exception while looking for leader", e);
                        // Errors are okay, since hosts may be down
                    }
                }

                ElectionResult result = countVotes(votes, heardFrom); // 计算选举结果
                // ZOOKEEPER-569:
                // If no votes are received for live peers, reset to voting 
                // for ourselves as otherwise we may hang on to a vote 
                // for a dead peer                 
                if (result.numValidVotes == 0) { // 没有有效的选票，将选票设置成服务自己
                    self.setCurrentVote(new Vote(self.getId(), self.getLastLoggedZxid()));
                } else {
                    if (result.winner.getId() >= 0) { // 服务id大于等于0
                        self.setCurrentVote(result.vote); // 设置选票为zxid更大或zxid相等服务id更大的选票，如果这轮没有选举出leader，之后就选举result.vote这个选票
                        // To do: this doesn't use a quorum verifier 该选举方法并没有使用quorum verifier
                        if (result.winningCount > (self.getVotingView().size() / 2)) { // 被选举的服务选票过半
                            self.setCurrentVote(result.winner); // 设置该选票当选leader
                            s.close(); // 选举完成，关闭socket连接
                            Vote current = self.getCurrentVote();
                            LOG.info("Found leader: my type is: " + self.getLearnerType());
                            /*
                             * We want to make sure we implement the state machine
                             * correctly. If we are a PARTICIPANT, once a leader
                             * is elected we can move either to LEADING or 
                             * FOLLOWING. However if we are an OBSERVER, it is an
                             * error to be elected as a Leader.
                             */
                            if (self.getLearnerType() == LearnerType.OBSERVER) { // 当前服务是观察者
                                if (current.getId() == self.getId()) { // 观察者被选举为leader（正常不可能发生）
                                    // This should never happen!
                                    LOG.error("OBSERVER elected as leader!");
                                    Thread.sleep(100);
                                } else { //设置当前服务为观察者
                                    self.setPeerState(ServerState.OBSERVING);
                                    Thread.sleep(100);
                                    return current; // 返回选票
                                }
                            } else { // 否则根据服务id设置当前服务为leader或follower
                                self.setPeerState((current.getId() == self.getId())
                                        ? ServerState.LEADING: ServerState.FOLLOWING);
                                if (self.getPeerState() == ServerState.FOLLOWING) { // 如果是跟随者，sleep100毫秒，等待leader处理
                                    Thread.sleep(100);
                                }                            
                                return current;
                            }
                        }
                    }
                }
                Thread.sleep(1000); // 如果没有选出leader，等1秒后再进行选举（所以这种选举算法效率低）
            }
            return null;
        } finally { // 最后注销JMX
            try {
                if(self.jmxLeaderElectionBean != null){
                    MBeanRegistry.getInstance().unregister(self.jmxLeaderElectionBean);
                }
            } catch (Exception e) {
                LOG.warn("Failed to unregister with JMX", e);
            }
            self.jmxLeaderElectionBean = null;
        }
    }
}
