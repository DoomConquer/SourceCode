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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.server.FinalRequestProcessor;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.SyncRequestProcessor;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;

/**
 * A ZooKeeperServer for the Observer node type. Not much is different, but
 * we anticipate specializing the request processors in the future. 
 * 观察者ZooKeeperServer服务
 */
public class ObserverZooKeeperServer extends LearnerZooKeeperServer {
    private static final Logger LOG = LoggerFactory.getLogger(ObserverZooKeeperServer.class);
    
    /**
     * Enable since request processor for writing txnlog to disk and
     * take periodic snapshot. Default is ON. 是否允许SyncRequestProcessor进行事务操作（记录事务日志和定期进行快照）
     */
    private boolean syncRequestProcessorEnabled = this.self.getSyncEnabled();
    
    /*
     * Request processors 请求处理器
     */
    private CommitProcessor commitProcessor;
    private SyncRequestProcessor syncProcessor;
    
    /*
     * Pending sync requests 挂起的同步请求队列
     */
    ConcurrentLinkedQueue<Request> pendingSyncs = new ConcurrentLinkedQueue<Request>();

    ObserverZooKeeperServer(FileTxnSnapLog logFactory, QuorumPeer self,
            DataTreeBuilder treeBuilder, ZKDatabase zkDb) throws IOException {
        super(logFactory, self.tickTime, self.minSessionTimeout, self.maxSessionTimeout, treeBuilder, zkDb, self);
        LOG.info("syncEnabled =" + syncRequestProcessorEnabled);
    }
    
    public Observer getObserver() {
        return self.observer;
    }
    
    @Override
    public Learner getLearner() {
        return self.observer;
    }
    
    /**
     * Unlike a Follower, which sees a full request only during the PROPOSAL
     * phase, Observers get all the data required with the INFORM packet. 
     * This method commits a request that has been unpacked by from an INFORM
     * received from the Leader.
     * 提交请求
     *      
     * @param request
     */
    public void commitRequest(Request request) {
        if (syncRequestProcessorEnabled) { // 如果允许syncProcessor处理器进行事务操作
            // Write to txnlog and take periodic snapshot
            syncProcessor.processRequest(request); // 记录事务日志和定期进行数据快照
        }
        commitProcessor.commit(request);
    }
    
    /**
     * Set up the request processors for an Observer:
     * firstProcesor -> commitProcessor -> finalProcessor
     * 设置请求处理链
     */
    @Override
    protected void setupRequestProcessors() {
        // We might consider changing the processor behaviour of 
        // Observers to, for example, remove the disk sync requirements.
        // Currently, they behave almost exactly the same as followers.
        RequestProcessor finalProcessor = new FinalRequestProcessor(this);
        commitProcessor = new CommitProcessor(finalProcessor, Long.toString(getServerId()), true,
                getZooKeeperServerListener());
        commitProcessor.start(); // 启动CommitProcessor处理器
        firstProcessor = new ObserverRequestProcessor(this, commitProcessor);
        ((ObserverRequestProcessor) firstProcessor).start(); // 启动ObserverRequestProcessor处理器

        /*
         * Observer should write to disk, so that it won't request
         * too old txn from the leader which may lead to getting an entire
         * snapshot.
         *
         * However, this may degrade performance as it has to write to disk
         * and do periodic snapshot which may double the memory requirements
         */
        if (syncRequestProcessorEnabled) { // 设置SyncRequestProcessor处理器
            syncProcessor = new SyncRequestProcessor(this, null);
            syncProcessor.start();
        }
    }

    /*
     * Process a sync request 处理同步请求
     */
    synchronized public void sync() {
        if (pendingSyncs.size() == 0) {
            LOG.warn("Not expecting a sync.");
            return;
        }

        Request r = pendingSyncs.remove(); // 接收到leader的SYNC消息，从挂起的同步请求队列中取出请求进行提交
        commitProcessor.commit(r);
    }

    // 观察者服务的角色
    @Override
    public String getState() {
        return "observer";
    };    

    // 关闭观察者ZooKeeperServer服务
    @Override
    public synchronized void shutdown() {
        if (!canShutdown()) { // ZooKeeper server没有在运行
            LOG.debug("ZooKeeper server is not running, so not proceeding to shutdown!");
            return;
        }
        super.shutdown(); // 关闭ZooKeeperServer服务
        if (syncRequestProcessorEnabled && syncProcessor != null) { // 关闭SyncRequestProcessor处理器
            syncProcessor.shutdown();
        }
    }
}
