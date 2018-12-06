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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.jute.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.server.FinalRequestProcessor;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.SyncRequestProcessor;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.txn.TxnHeader;

/**
 * Just like the standard ZooKeeperServer. We just replace the request
 * processors: FollowerRequestProcessor -> CommitProcessor ->
 * FinalRequestProcessor
 * 
 * A SyncRequestProcessor is also spawned（衍生） off to log proposals from the leader.
 * 跟随者ZooKeeperServer服务
 */
public class FollowerZooKeeperServer extends LearnerZooKeeperServer {
    private static final Logger LOG = LoggerFactory.getLogger(FollowerZooKeeperServer.class);

    CommitProcessor commitProcessor;    // 提交事务请求处理器

    SyncRequestProcessor syncProcessor; // 事务日志请求处理器

    /*
     * Pending sync requests
     */
    ConcurrentLinkedQueue<Request> pendingSyncs; // 挂起的等待同步的请求
    
    /**
     * 构造函数
     * @throws IOException
     */
    FollowerZooKeeperServer(FileTxnSnapLog logFactory, QuorumPeer self,
                            DataTreeBuilder treeBuilder, ZKDatabase zkDb) throws IOException {
        super(logFactory, self.tickTime, self.minSessionTimeout, self.maxSessionTimeout, treeBuilder, zkDb, self);
        this.pendingSyncs = new ConcurrentLinkedQueue<Request>();
    }

    // 获取follower
    public Follower getFollower(){
        return self.follower;
    }      

    // 设置请求处理器链（覆盖父类方法）
    // follower有两条处理器链：
    // 1、FollowerRequestProcessor -> CommitProcessor -> FinalRequestProcessor
    // 2、SyncRequestProcessor -> SendAckRequestProcessor
    @Override
    protected void setupRequestProcessors() {
        RequestProcessor finalProcessor = new FinalRequestProcessor(this);
        commitProcessor = new CommitProcessor(finalProcessor, Long.toString(getServerId()), true,
                getZooKeeperServerListener());
        commitProcessor.start(); // 启动CommitProcessor处理器
        firstProcessor = new FollowerRequestProcessor(this, commitProcessor);
        ((FollowerRequestProcessor) firstProcessor).start(); // 启动FollowerRequestProcessor处理器
        syncProcessor = new SyncRequestProcessor(this, new SendAckRequestProcessor((Learner)getFollower()));
        syncProcessor.start(); // 启动SyncRequestProcessor处理器
    }

    LinkedBlockingQueue<Request> pendingTxns = new LinkedBlockingQueue<Request>(); // 挂起提议请求队列（等待提交）

    // 记录事务日志
    public void logRequest(TxnHeader hdr, Record txn) {
        Request request = new Request(null, hdr.getClientId(), hdr.getCxid(), hdr.getType(), null, null);
        request.hdr = hdr;
        request.txn = txn;
        request.zxid = hdr.getZxid();
        if ((request.zxid & 0xffffffffL) != 0) { // 排除zxid后32位为0的请求（这些请求不是事务请求）
            pendingTxns.add(request); // 事务请求加入挂起提议请求队列
        }
        syncProcessor.processRequest(request); // 记录事务日志（加到请求队列queuedRequests等待记录）
    }

    /**
     * When a COMMIT message is received, eventually this method is called, 
     * which matches up the zxid from the COMMIT with (hopefully) the head of
     * the pendingTxns queue and hands it to the commitProcessor to commit.
     * 提交事务请求
     *
     * @param zxid - must correspond to the head of pendingTxns if it exists
     */
    public void commit(long zxid) {
        if (pendingTxns.size() == 0) { // 挂起提议请求队列为空
            LOG.warn("Committing " + Long.toHexString(zxid) + " without seeing txn");
            return;
        }
        long firstElementZxid = pendingTxns.element().zxid; // 取出对头元素
        if (firstElementZxid != zxid) { // 接收的提交事务不是提议请求队列中对头元素，说明顺序乱了，停止服务
            LOG.error("Committing zxid 0x" + Long.toHexString(zxid)
                    + " but next pending txn 0x"
                    + Long.toHexString(firstElementZxid));
            System.exit(12);
        }
        Request request = pendingTxns.remove();
        commitProcessor.commit(request); // 提交事务（加入commitProcessor的committedRequests队列）
    }

    // follower接收到leader同步命令sync
    synchronized public void sync() {
        if (pendingSyncs.size() == 0) {
            LOG.warn("Not expecting a sync.");
            return;
        }

        Request r = pendingSyncs.remove();
        commitProcessor.commit(r); // 交给commitProcessor处理
    }

    // 获取服务的最大请求堆积数量
    @Override
    public int getGlobalOutstandingLimit() {
        return super.getGlobalOutstandingLimit() / (self.getQuorumSize() - 1);
    }

    // 关闭FollowerZookeeperServer服务
    @Override
    public void shutdown() {
        LOG.info("Shutting down");
        try {
            super.shutdown();
        } catch (Exception e) {
            LOG.warn("Ignoring unexpected exception during shutdown", e);
        }
        try {
            if (syncProcessor != null) { // 关闭SyncRequestProcessor处理器
                syncProcessor.shutdown();
            }
        } catch (Exception e) {
            LOG.warn("Ignoring unexpected exception in syncprocessor shutdown", e);
        }
    }

    // 获取该服务角色
    @Override
    public String getState() {
        return "follower";
    }

    @Override
    public Learner getLearner() {
        return getFollower();
    }
}
