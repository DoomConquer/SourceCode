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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import java.util.concurrent.TimeUnit;
import java.util.Random;

import org.apache.zookeeper.common.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.zookeeper.jmx.MBeanRegistry;
import org.apache.zookeeper.server.ZooKeeperThread;
import org.apache.zookeeper.server.quorum.Election;
import org.apache.zookeeper.server.quorum.Vote;
import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.apache.zookeeper.server.quorum.QuorumPeer.ServerState;

/**
 * @deprecated This class has been deprecated as of release 3.4.0. 
 */
@Deprecated
public class AuthFastLeaderElection implements Election {
    private static final Logger LOG = LoggerFactory.getLogger(AuthFastLeaderElection.class);

    /* Sequence numbers for messages 消息序列号*/
    static int sequencer = 0;
    static int maxTag = 0;

    /*
     * Determine how much time a process has to wait once it believes that it
     * has reached the end of leader election.
     */
    static int finalizeWait = 100; // 决定一个leader的等待时间

    /*
     * Challenge counter to avoid replay attacks
     * 用于生成challenge的计数
     */
    static int challengeCounter = 0;

    /*
     * Flag to determine whether to authenticate or not
     * 是否需要认证
     */
    private boolean authEnabled = false;

    // 选举通知类
    static public class Notification {
        /*
         * Proposed leader 提议的leader
         */
        long leader;

        /*
         * zxid of the proposed leader 提议leader的zxid
         */
        long zxid;

        /*
         * Epoch 选举epoch
         */
        long epoch;

        /*
         * current state of sender 发送方服务状态
         */
        QuorumPeer.ServerState state;

        /*
         * Address of the sender 发送方地址
         */
        InetSocketAddress addr;
    }

    /*
     * Messages to send, both Notifications and Acks 待发送的消息（包括通知和确认）
     */
    static public class ToSend {
        // 消息类型
        static enum mType {
            crequest, challenge, notification, ack
        }

        ToSend(mType type, long tag, long leader, long zxid, long epoch,
                ServerState state, InetSocketAddress addr) {

            // 不同消息类型
            switch (type) {
            case crequest:
                this.type = 0;
                this.tag = tag;
                this.leader = leader;
                this.zxid = zxid;
                this.epoch = epoch;
                this.state = state;
                this.addr = addr;

                break;
            case challenge:
                this.type = 1;
                this.tag = tag;
                this.leader = leader;
                this.zxid = zxid;
                this.epoch = epoch;
                this.state = state;
                this.addr = addr;

                break;
            case notification:
                this.type = 2;
                this.leader = leader;
                this.zxid = zxid;
                this.epoch = epoch;
                this.state = QuorumPeer.ServerState.LOOKING;
                this.tag = tag;
                this.addr = addr;

                break;
            case ack:
                this.type = 3;
                this.tag = tag;
                this.leader = leader;
                this.zxid = zxid;
                this.epoch = epoch;
                this.state = state;
                this.addr = addr;

                break;
            default:
                break;
            }
        }

        /*
         * Message type: 0 notification, 1 acknowledgement 消息类型
         */
        int type;

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
        long epoch;

        /*
         * Current state;
         */
        QuorumPeer.ServerState state;

        /*
         * Message tag
         */
        long tag;

        InetSocketAddress addr;
    }

    LinkedBlockingQueue<ToSend> sendqueue;       // 发送消息队列
    LinkedBlockingQueue<Notification> recvqueue; // 接收消息队列

    // 选举消息通信处理类
    private class Messenger {
        final DatagramSocket mySocket; // 选举通信socket（UDP协议）
        // 记录最后提议的leader，zxid及epoch
        long lastProposedLeader;
        long lastProposedZxid;
        long lastEpoch;
        final Set<Long> ackset;
        final ConcurrentHashMap<Long, Long> challengeMap; // 记录消息tag -> challenge
        final ConcurrentHashMap<Long, Semaphore> challengeMutex; // challenge的信号量，消息tag -> 信号量
        final ConcurrentHashMap<Long, Semaphore> ackMutex; // 确认信号量，消息tag -> 信号量
        final ConcurrentHashMap<InetSocketAddress, ConcurrentHashMap<Long, Long>> addrChallengeMap; // 记录服务地址 -> （消息tag -> challenge）

        // 接收消息线程
        class WorkerReceiver implements Runnable {
            DatagramSocket mySocket;
            Messenger myMsg;

            WorkerReceiver(DatagramSocket s, Messenger msg) {
                mySocket = s;
                myMsg = msg;
            }

            // 保存challenge
            boolean saveChallenge(long tag, long challenge) {
                Semaphore s = challengeMutex.get(tag); // 获取challenge互斥信号量
                if (s != null) {
                    synchronized (Messenger.this) {
                        challengeMap.put(tag, challenge);
                        challengeMutex.remove(tag);
                    }
                    s.release();
                } else {
                    LOG.error("No challenge mutex object");
                }
                return true;
            }

            // 执行线程
            public void run() {
                byte responseBytes[] = new byte[48]; // 响应消息字节数组
                ByteBuffer responseBuffer = ByteBuffer.wrap(responseBytes);
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
                while (true) {
                    // Sleeps on receive
                    try {
                        responseBuffer.clear();
                        mySocket.receive(responsePacket); // 阻塞直到接收到数据
                    } catch (IOException e) {
                        LOG.warn("Ignoring exception receiving", e);
                    }
                    // Receive new message
                    if (responsePacket.getLength() != responseBytes.length) { // 接收到的数据长度不相符（不是48字节）
                        LOG.warn("Got a short response: "
                                + responsePacket.getLength() + " "
                                + responsePacket.toString());
                        continue; // 继续接收
                    }
                    responseBuffer.clear(); // position = 0（这里为了方便直接用clear方法，把position设置成0，如果是需要同时读写转换可用flip方法）
                    int type = responseBuffer.getInt(); // 获取消息类型
                    if ((type > 3) || (type < 0)) { // 非法类型
                        LOG.warn("Got bad Msg type: " + type);
                        continue; // 忽略
                    }
                    long tag = responseBuffer.getLong(); // 读取tag

                    QuorumPeer.ServerState ackstate = QuorumPeer.ServerState.LOOKING;
                    switch (responseBuffer.getInt()) { // 发送消息的服务状态（int型）
                    case 0:
                        ackstate = QuorumPeer.ServerState.LOOKING;
                        break;
                    case 1:
                        ackstate = QuorumPeer.ServerState.LEADING;
                        break;
                    case 2:
                        ackstate = QuorumPeer.ServerState.FOLLOWING;
                        break;
                    }

                    Vote current = self.getCurrentVote(); // 获取当前选票

                    // 0 crequest, 1 challenge, 2 notification, 3 ack
                    switch (type) {
                    case 0:
                        // Receive challenge request 接收到challenge请求
                        ToSend c = new ToSend(ToSend.mType.challenge, tag,
                                current.getId(), current.getZxid(),
                                logicalclock, self.getPeerState(),
                                (InetSocketAddress) responsePacket.getSocketAddress());
                        sendqueue.offer(c); // 生成challenge加入消息发送队列
                        break;
                    case 1:
                        // Receive challenge and store somewhere else 接收到challenge
                        long challenge = responseBuffer.getLong();
                        saveChallenge(tag, challenge); // 保存challenge
                        break;
                    case 2: // 接收到通知
                        Notification n = new Notification();
                        n.leader = responseBuffer.getLong();
                        n.zxid = responseBuffer.getLong();
                        n.epoch = responseBuffer.getLong();
                        n.state = ackstate;
                        n.addr = (InetSocketAddress) responsePacket.getSocketAddress();

                        if ((myMsg.lastEpoch <= n.epoch)
                                && ((n.zxid > myMsg.lastProposedZxid) 
                                || ((n.zxid == myMsg.lastProposedZxid) 
                                && (n.leader > myMsg.lastProposedLeader)))) { // 更新该服务选举提议信息为接收到的信息
                            myMsg.lastProposedZxid = n.zxid;
                            myMsg.lastProposedLeader = n.leader;
                            myMsg.lastEpoch = n.epoch;
                        }

                        long recChallenge;
                        InetSocketAddress addr = (InetSocketAddress) responsePacket.getSocketAddress();
                        if (authEnabled) { // 需要认证
                            ConcurrentHashMap<Long, Long> tmpMap = addrChallengeMap.get(addr);
                            if(tmpMap != null){
                                if (tmpMap.get(tag) != null) {
                                    recChallenge = responseBuffer.getLong(); // 接收到的challenge
                                    if (tmpMap.get(tag) == recChallenge) {
                                        recvqueue.offer(n);

                                        ToSend a = new ToSend(ToSend.mType.ack,
                                                tag, current.getId(),
                                                current.getZxid(),
                                                logicalclock, self.getPeerState(),
                                                addr);

                                        sendqueue.offer(a); // 生成ack确认通知加入消息发送队列
                                    } else {
                                        LOG.warn("Incorrect challenge: "
                                                + recChallenge + ", "
                                                + addrChallengeMap.toString());
                                    }
                                } else {
                                    LOG.warn("No challenge for host: " + addr + " " + tag);
                                }
                            }
                        } else { // 不需要认证
                            recvqueue.offer(n); // 将接收的通知加入接收队列

                            // 生成ack确认通知
                            ToSend a = new ToSend(ToSend.mType.ack, tag,
                                    current.getId(), current.getZxid(),
                                    logicalclock, self.getPeerState(),
                                    (InetSocketAddress) responsePacket.getSocketAddress());

                            sendqueue.offer(a); // 加入发送队列
                        }
                        break;

                    // Upon reception of an ack message, remove it from the queue
                    case 3: // 接收到ack确认消息
                        Semaphore s = ackMutex.get(tag);
                        
                        if(s != null)
                            s.release(); // 接收到tag对应的ack确认消息，释放对应的信号量
                        else LOG.error("Empty ack semaphore");
                        
                        ackset.add(tag); // 确认消息加入到ackset集合

                        if (authEnabled) { // 需要认证
                            ConcurrentHashMap<Long, Long> tmpMap = addrChallengeMap.get(responsePacket.getSocketAddress());
                            if(tmpMap != null) {
                                tmpMap.remove(tag); // 移除tag对应的challenge
                            } else {
                                LOG.warn("No such address in the ensemble configuration " + responsePacket.getSocketAddress());
                            }
                        }

                        if (ackstate != QuorumPeer.ServerState.LOOKING) { // 发送消息服务状态不是looking（选举完成）
                            Notification outofsync = new Notification();
                            outofsync.leader = responseBuffer.getLong();
                            outofsync.zxid = responseBuffer.getLong();
                            outofsync.epoch = responseBuffer.getLong();
                            outofsync.state = ackstate;
                            outofsync.addr = (InetSocketAddress) responsePacket.getSocketAddress();

                            recvqueue.offer(outofsync); // 接收已经选举完成服务发送来的消息
                        }
                        break;
                    // Default case
                    default:
                        LOG.warn("Received message of incorrect type " + type);
                        break;
                    }
                }
            }
        }

        // 发送消息线程
        class WorkerSender implements Runnable {
            Random rand; // 生成challenge随机数
            int maxAttempts; // 最多尝试次数
            int ackWait = finalizeWait; // 等待确认时间

            /*
             * Receives a socket and max number of attempts as input
             */

            WorkerSender(int attempts) {
                maxAttempts = attempts;
                rand = new Random(java.lang.Thread.currentThread().getId() + Time.currentElapsedTime());
            }

            // 生成challenge
            long genChallenge() {
                byte buf[] = new byte[8];

                buf[0] = (byte) ((challengeCounter & 0xff000000) >>> 24);
                buf[1] = (byte) ((challengeCounter & 0x00ff0000) >>> 16);
                buf[2] = (byte) ((challengeCounter & 0x0000ff00) >>> 8);
                buf[3] = (byte) ((challengeCounter & 0x000000ff));

                challengeCounter++;
                int secret = rand.nextInt(java.lang.Integer.MAX_VALUE);

                buf[4] = (byte) ((secret & 0xff000000) >>> 24);
                buf[5] = (byte) ((secret & 0x00ff0000) >>> 16);
                buf[6] = (byte) ((secret & 0x0000ff00) >>> 8);
                buf[7] = (byte) ((secret & 0x000000ff));

                return (((long)(buf[0] & 0xFF)) << 56)  
                        + (((long)(buf[1] & 0xFF)) << 48)
                        + (((long)(buf[2] & 0xFF)) << 40) 
                        + (((long)(buf[3] & 0xFF)) << 32)
                        + (((long)(buf[4] & 0xFF)) << 24) 
                        + (((long)(buf[5] & 0xFF)) << 16)
                        + (((long)(buf[6] & 0xFF)) << 8) 
                        + ((long)(buf[7] & 0xFF));
            }

            public void run() {
                while (true) {
                    try {
                        ToSend m = sendqueue.take(); // 发送队列中取出待发送消息，没有消息阻塞等待
                        process(m); // 发送
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            // 发送消息
            private void process(ToSend m) {
                int attempts = 0; // 尝试次数
                byte zeroes[]; // 填充0字节数组
                byte requestBytes[] = new byte[48];
                DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length);
                ByteBuffer requestBuffer = ByteBuffer.wrap(requestBytes);

                // 根据不同的消息的类型进行处理发送
                switch (m.type) {
                case 0: // challenge请求
                    /*
                     * Building challenge request packet to send
                     */
                    requestBuffer.clear();
                    requestBuffer.putInt(ToSend.mType.crequest.ordinal());
                    requestBuffer.putLong(m.tag);
                    requestBuffer.putInt(m.state.ordinal());
                    zeroes = new byte[32]; // 填充0
                    requestBuffer.put(zeroes);

                    requestPacket.setLength(48);
                    try {
                        requestPacket.setSocketAddress(m.addr);
                    } catch (IllegalArgumentException e) {
                        // Sun doesn't include the address that causes this
                        // exception to be thrown, so we wrap the exception
                        // in order to capture this critical detail.
                        throw new IllegalArgumentException(
                                "Unable to set socket address on packet, msg:"
                                + e.getMessage() + " with addr:" + m.addr,
                                e);
                    }

                    try {
                        if (challengeMap.get(m.tag) == null) { // challengeMap中没有保存该tag的challenge，发送challenge请求，否则表示已经存在
                            mySocket.send(requestPacket);
                        }
                    } catch (IOException e) {
                        LOG.warn("Exception while sending challenge: ", e);
                    }
                    break;
                case 1: // 发送challenge
                    /*
                     * Building challenge packet to send
                     */
                    long newChallenge;
                    ConcurrentHashMap<Long, Long> tmpMap = addrChallengeMap.get(m.addr); 
                    if(tmpMap != null){
                        Long tmpLong = tmpMap.get(m.tag); // 获取保存的challenge
                        if (tmpLong != null) {
                            newChallenge = tmpLong;
                        } else {
                            newChallenge = genChallenge(); // 第一次，生成challenge
                        }
                        tmpMap.put(m.tag, newChallenge); // 保存challenge

                        requestBuffer.clear();
                        requestBuffer.putInt(ToSend.mType.challenge.ordinal());
                        requestBuffer.putLong(m.tag);
                        requestBuffer.putInt(m.state.ordinal());
                        requestBuffer.putLong(newChallenge);
                        zeroes = new byte[24];
                        requestBuffer.put(zeroes);

                        requestPacket.setLength(48);
                        try {
                            requestPacket.setSocketAddress(m.addr);
                        } catch (IllegalArgumentException e) {
                            // Sun doesn't include the address that causes this
                            // exception to be thrown, so we wrap the exception
                            // in order to capture this critical detail.
                            throw new IllegalArgumentException(
                                    "Unable to set socket address on packet, msg:"
                                    + e.getMessage() + " with addr:" + m.addr,
                                    e);
                        }

                        try {
                            mySocket.send(requestPacket); // 发送
                        } catch (IOException e) {
                            LOG.warn("Exception while sending challenge: ", e);
                        }
                    } else {
                        LOG.error("Address is not in the configuration: " + m.addr);
                    }
                    break;
                case 2: // 发送通知
                    /*
                     * Building notification packet to send
                     */
                    requestBuffer.clear();
                    requestBuffer.putInt(m.type);
                    requestBuffer.putLong(m.tag);
                    requestBuffer.putInt(m.state.ordinal());
                    requestBuffer.putLong(m.leader);
                    requestBuffer.putLong(m.zxid);
                    requestBuffer.putLong(m.epoch);
                    zeroes = new byte[8];
                    requestBuffer.put(zeroes);

                    requestPacket.setLength(48);
                    try {
                        requestPacket.setSocketAddress(m.addr); // 设置发送地址
                    } catch (IllegalArgumentException e) {
                        // Sun doesn't include the address that causes this
                        // exception to be thrown, so we wrap the exception
                        // in order to capture this critical detail.
                        throw new IllegalArgumentException(
                                "Unable to set socket address on packet, msg:"
                                + e.getMessage() + " with addr:" + m.addr,
                                e);
                    }

                    boolean myChallenge = false; // 是否收到challenge标志
                    boolean myAck = false; // 是否收到确认标志

                    while (attempts < maxAttempts) {
                        try {
                            /*
                             * Try to obtain a challenge only if does not have
                             * one yet
                             */
                            if (!myChallenge && authEnabled) { // 需要认证
                                ToSend crequest = new ToSend(
                                        ToSend.mType.crequest, m.tag, m.leader,
                                        m.zxid, m.epoch,
                                        QuorumPeer.ServerState.LOOKING, m.addr);
                                sendqueue.offer(crequest); // 生成challenge请求，并加入发送队列

                                try {
                                    double timeout = ackWait * java.lang.Math.pow(2, attempts); // 获取许可超时时间

                                    Semaphore s = new Semaphore(0); // 信号量许可初始为0，调用tryAcquire一直阻塞直到release释放
                                    synchronized(Messenger.this) {
                                        challengeMutex.put(m.tag, s);
                                        // 获取s信号量许可（等待该服务接收到challenge，调用saveChallenge时释放），上面加入发送队列的
                                        // challenge请求会被其他线程（发送线程有多个）发送出去，被远程服务处理后该服务会接收到challenge
                                        s.tryAcquire((long) timeout, TimeUnit.MILLISECONDS);
                                        myChallenge = challengeMap.containsKey(m.tag);
                                    }
                                } catch (InterruptedException e) {
                                    LOG.warn("Challenge request exception: ", e);
                                }
                            }

                            /*
                             * If don't have challenge yet, skip sending
                             * notification 如果还是没有challenge，跳过发送通知继续尝试
                             */
                            if (authEnabled && !myChallenge) {
                                attempts++;
                                continue;
                            }

                            if (authEnabled) {
                                requestBuffer.position(40);
                                Long tmpLong = challengeMap.get(m.tag);
                                if(tmpLong != null){
                                    requestBuffer.putLong(tmpLong); // 最后8byte设置challenge
                                } else {
                                    LOG.warn("No challenge with tag: " + m.tag);
                                }
                            }
                            mySocket.send(requestPacket); // 发送通知，等待ack确认
                            try {
                                Semaphore s = new Semaphore(0);
                                double timeout = ackWait * java.lang.Math.pow(10, attempts);
                                ackMutex.put(m.tag, s);
                                s.tryAcquire((int) timeout, TimeUnit.MILLISECONDS); // 等待确认
                            } catch (InterruptedException e) {
                                LOG.warn("Ack exception: ", e);
                            }
                            
                            if(ackset.remove(m.tag)){ // 该服务收到确认会加入ackset集合
                                myAck = true; // 收到确认
                            }
                        } catch (IOException e) {
                            LOG.warn("Sending exception: ", e);
                            /*
                             * Do nothing, just try again
                             */
                        }
                        if (myAck) { // 收到确认
                            /*
                             * Received ack successfully, so return 成功收到ack确认
                             */
                            challengeMap.remove(m.tag); // 移除tag对应的challenge
                            return;
                        } else
                            attempts++; // 继续尝试
                    }
                    /*
                     * Return message to queue for another attempt later if
                     * epoch hasn't changed.
                     */
                    if (m.epoch == logicalclock) {
                        challengeMap.remove(m.tag);
                        sendqueue.offer(m);
                    }
                    break;
                case 3: // 发送ack确认
                    requestBuffer.clear();
                    requestBuffer.putInt(m.type);
                    requestBuffer.putLong(m.tag);
                    requestBuffer.putInt(m.state.ordinal());
                    requestBuffer.putLong(m.leader);
                    requestBuffer.putLong(m.zxid);
                    requestBuffer.putLong(m.epoch);

                    requestPacket.setLength(48);
                    try {
                        requestPacket.setSocketAddress(m.addr);
                    } catch (IllegalArgumentException e) {
                        // Sun doesn't include the address that causes this
                        // exception to be thrown, so we wrap the exception
                        // in order to capture this critical detail.
                        throw new IllegalArgumentException(
                                "Unable to set socket address on packet, msg:"
                                + e.getMessage() + " with addr:" + m.addr,
                                e);
                    }

                    try {
                        mySocket.send(requestPacket); // 发送
                    } catch (IOException e) {
                        LOG.warn("Exception while sending ack: ", e);
                    }
                    break;
                }
            }
        }

        // 判断发送队列、确认集合或接收队列是否为空
        public boolean queueEmpty() {
            return (sendqueue.isEmpty() || ackset.isEmpty() || recvqueue.isEmpty());
        }

        // 实例化Messenger
        Messenger(int threads, DatagramSocket s) {
            mySocket = s;
            ackset =  Collections.<Long>newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
            challengeMap = new ConcurrentHashMap<Long, Long>();
            challengeMutex = new ConcurrentHashMap<Long, Semaphore>();
            ackMutex = new ConcurrentHashMap<Long, Semaphore>();
            addrChallengeMap = new ConcurrentHashMap<InetSocketAddress, ConcurrentHashMap<Long, Long>>();
            lastProposedLeader = 0;
            lastProposedZxid = 0;
            lastEpoch = 0;

            // 创建并启动threads个发送消息线程，实例化WorkerSender(3)中的参数是超时重新尝试次数
            for (int i = 0; i < threads; ++i) {
                Thread t = new ZooKeeperThread(new WorkerSender(3), "WorkerSender Thread: " + (i + 1));
                t.setDaemon(true);
                t.start();
            }

            // 初始化addrChallengeMap
            for (QuorumServer server : self.getVotingView().values()) {
                InetSocketAddress saddr = new InetSocketAddress(server.addr.getAddress(), port);
                addrChallengeMap.put(saddr, new ConcurrentHashMap<Long, Long>());
            }

            // 创建并启动接收消息线程
            Thread t = new ZooKeeperThread(new WorkerReceiver(s, this), "WorkerReceiver-" + s.getRemoteSocketAddress());
            t.start();
        }
    }

    QuorumPeer self;
    int port;
    volatile long logicalclock; /* Election instance 选举轮次*/
    DatagramSocket mySocket; // 选举通信连接（UDP）
    long proposedLeader; // 提议的leader
    long proposedZxid;   // 提议的leader事务id

    public AuthFastLeaderElection(QuorumPeer self, boolean auth) {
        this.authEnabled = auth; // 是否需要认证
        starter(self);
    }

    public AuthFastLeaderElection(QuorumPeer self) {
        starter(self);
    }

    // 初始化
    private void starter(QuorumPeer self) {
        this.self = self;
        port = self.getVotingView().get(self.getId()).electionAddr.getPort(); // 选举端口
        proposedLeader = -1;
        proposedZxid = -1;

        try {
            mySocket = new DatagramSocket(port); // 初始化socket
            // mySocket.setSoTimeout(20000);
        } catch (SocketException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        sendqueue = new LinkedBlockingQueue<ToSend>(2 * self.getVotingView().size());
        recvqueue = new LinkedBlockingQueue<Notification>(2 * self.getVotingView().size());
        new Messenger(self.getVotingView().size() * 2, mySocket); // 发送线程数量初始化为2倍参与选举的服务数量
    }

    // 完成本轮选举
    private void leaveInstance() {
        logicalclock++;
    }

    // 给所有参与选举的服务发送通知
    private void sendNotifications() {
        for (QuorumServer server : self.getView().values()) {
            ToSend notmsg = new ToSend(ToSend.mType.notification,
                    AuthFastLeaderElection.sequencer++, proposedLeader,
                    proposedZxid, logicalclock, QuorumPeer.ServerState.LOOKING,
                    self.getView().get(server.id).electionAddr);
            sendqueue.offer(notmsg);
        }
    }

    // 优先选择zxid更大的或者zxid相等，服务id更大的服务作为leader
    private boolean totalOrderPredicate(long id, long zxid) {
        if ((zxid > proposedZxid)
                || ((zxid == proposedZxid) && (id > proposedLeader)))
            return true;
        else
            return false;

    }

    // 判断服务id为l，事务为zxid的服务是否能够胜任leader（选票是否过半）
    private boolean termPredicate(HashMap<InetSocketAddress, Vote> votes, long l, long zxid) {
        Collection<Vote> votesCast = votes.values();
        int count = 0;
        /*
         * First make the views consistent. Sometimes peers will have different
         * zxids for a server depending on timing.
         */
        for (Vote v : votesCast) {
            if ((v.getId() == l) && (v.getZxid() == zxid))
                count++;
        }

        if (count > (self.getVotingView().size() / 2))
            return true;
        else
            return false;
    }

    /**
     * There is nothing to shutdown in this implementation of
     * leader election, so we simply have an empty method.
     */
    public void shutdown(){}
    
    /**
     * Invoked in QuorumPeer to find or elect a new leader.
     * 选举leader
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
            HashMap<InetSocketAddress, Vote> recvset = new HashMap<InetSocketAddress, Vote>(); // 接收到的选票，服务地址 -> 选票
            HashMap<InetSocketAddress, Vote> outofelection = new HashMap<InetSocketAddress, Vote>(); // 收集已经完成选举（leading或following）的服务发送的选票
    
            logicalclock++; // 选举时钟周期（轮次）
    
            proposedLeader = self.getId();
            proposedZxid = self.getLastLoggedZxid();
    
            LOG.info("Election tally");
            sendNotifications(); // 向所有参与选举的服务发送通知（自己的选票）
    
            /*
             * Loop in which we exchange notifications until we find a leader
             * 一直循环直到选出leader
             */
            while (self.getPeerState() == ServerState.LOOKING) {
                /*
                 * Remove next notification from queue, times out after 2 times
                 * the termination time 取出接收到的通知，默认超时为2倍finalizeWait
                 */
                Notification n = recvqueue.poll(2 * finalizeWait, TimeUnit.MILLISECONDS);
    
                /*
                 * Sends more notifications if haven't received enough.
                 * Otherwise processes new notification.
                 */
                if (n == null) { // 没有接收到消息，再次发送通知给所有参选的服务
                    if (((!outofelection.isEmpty()) || (recvset.size() > 1)))
                        sendNotifications();
                } else
                    switch (n.state) { // 发送选票服务的状态
                    case LOOKING:
                        if (n.epoch > logicalclock) { // 接收到选票的轮次大于该服务的轮次，丢弃该服务的信息，使用选票的信息
                            logicalclock = n.epoch;
                            recvset.clear(); // 清除收集的选票
                            if (totalOrderPredicate(n.leader, n.zxid)) { // 选票中的提议leader是否更适合做leader，更新该服务的提议内容
                                proposedLeader = n.leader;
                                proposedZxid = n.zxid;
                            }
                            sendNotifications();
                        } else if (n.epoch < logicalclock) { // 该服务的选举轮次超前选票的轮次，丢弃这张选票
                            break;
                        } else if (totalOrderPredicate(n.leader, n.zxid)) { // 选票中的提议leader是否更适合做leader，更新该服务的提议内容
                            proposedLeader = n.leader;
                            proposedZxid = n.zxid;
                            sendNotifications();
                        }
    
                        recvset.put(n.addr, new Vote(n.leader, n.zxid)); // 收集选票
    
                        // If have received from all nodes, then terminate
                        // 收集到所有服务的选票，结束选举
                        if (self.getVotingView().size() == recvset.size()) {
                            self.setPeerState((proposedLeader == self.getId()) ?
                                    ServerState.LEADING: ServerState.FOLLOWING);
                            // if (self.state == ServerState.FOLLOWING) {
                            // Thread.sleep(100);
                            // }
                            leaveInstance(); // 结束选举
                            return new Vote(proposedLeader, proposedZxid);
                        } else if (termPredicate(recvset, proposedLeader, proposedZxid)) { // 该服务提议的leader在收集的选票中已经过半
                            // Otherwise, wait for a fixed amount of time
                            LOG.info("Passed predicate");
                            Thread.sleep(finalizeWait); // 等待finalizeWait时间（具体原因和FastLeaderElection中一样）
    
                            // Notification probe = recvqueue.peek();
    
                            // Verify if there is any change in the proposed leader
                            // 验证是否所有的选票都认为该服务提议的leader更能胜任leader（根据优先选择zxid更大的服务做leader原则）
                            while ((!recvqueue.isEmpty())
                                    && !totalOrderPredicate(recvqueue.peek().leader, recvqueue.peek().zxid)) {
                                recvqueue.poll();
                            }
                            if (recvqueue.isEmpty()) {
                                // LOG.warn("Proposed leader: " + proposedLeader);
                                // 设置该服务状态
                                self.setPeerState(
                                        (proposedLeader == self.getId()) ? ServerState.LEADING : ServerState.FOLLOWING);
                                leaveInstance(); // 结束选举
                                return new Vote(proposedLeader, proposedZxid);
                            }
                        }
                        break;
                    case LEADING: // 如果发送通知的服务已经是leader
                        outofelection.put(n.addr, new Vote(n.leader, n.zxid)); // 收集选票
                        if (termPredicate(outofelection, n.leader, n.zxid)) { // 选票已经过半
                            self.setPeerState((n.leader == self.getId()) ? 
                                    ServerState.LEADING: ServerState.FOLLOWING); // 设置服务状态
                            leaveInstance(); // 结束选举
                            return new Vote(n.leader, n.zxid);
                        }
                        break;
                    case FOLLOWING: // 如果发送通知的服务已经是跟随者，处理方式和上面发送通知的服务是leader情况一样
                        outofelection.put(n.addr, new Vote(n.leader, n.zxid));
                        if (termPredicate(outofelection, n.leader, n.zxid)) {
                            self.setPeerState((n.leader == self.getId()) ? 
                                    ServerState.LEADING: ServerState.FOLLOWING);
                            leaveInstance();
                            return new Vote(n.leader, n.zxid);
                        }
                        break;
                    default:
                        break;
                    }
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
