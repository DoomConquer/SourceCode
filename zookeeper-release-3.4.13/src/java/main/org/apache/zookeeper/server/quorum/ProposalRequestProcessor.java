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

import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.SyncRequestProcessor;
import org.apache.zookeeper.server.quorum.Leader.XidRolloverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This RequestProcessor simply forwards requests to an AckRequestProcessor and
 * SyncRequestProcessor.
 * 该处理器转发请求给AckRequestProcessor和SyncRequestProcessor，对于learner的同步请求直接进行处理返回给learner
 */
public class ProposalRequestProcessor implements RequestProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ProposalRequestProcessor.class);

    LeaderZooKeeperServer zks; // leader ZooKeeperServer服务
    
    RequestProcessor nextProcessor; // 下一个请求处理器

    SyncRequestProcessor syncProcessor; // SyncRequestProcessor请求处理器，请求被ProposalRequestProcessor转发到该处理器

    public ProposalRequestProcessor(LeaderZooKeeperServer zks, RequestProcessor nextProcessor) {
        this.zks = zks;
        this.nextProcessor = nextProcessor;
        // 初始化SyncRequestProcessor和AckRequestProcessor处理器链
        AckRequestProcessor ackProcessor = new AckRequestProcessor(zks.getLeader());
        syncProcessor = new SyncRequestProcessor(zks, ackProcessor);
    }
    
    /**
     * initialize this processor
     * 启动SyncRequestProcessor处理器
     */
    public void initialize() {
        syncProcessor.start();
    }

    // 处理请求
    public void processRequest(Request request) throws RequestProcessorException {
        // LOG.warn("Ack>>> cxid = " + request.cxid + " type = " +
        // request.type + " id = " + request.sessionId);
        // request.addRQRec(">prop");
                
        
        /* In the following IF-THEN-ELSE block, we process syncs on the leader. 
         * If the sync is coming from a follower, then the follower
         * handler adds it to syncHandler. Otherwise, if it is a client of
         * the leader that issued the sync command, then syncHandler won't 
         * contain the handler. In this case, we add it to syncHandler, and 
         * call processRequest on the next processor.
         */
        if(request instanceof LearnerSyncRequest){ // 该请求是learner的同步请求
            zks.getLeader().processSync((LearnerSyncRequest)request); // leader直接处理返回给learner
        } else { // 其他请求（包括learner的其他事务请求和leader接收到客户端的其他事务请求）
                nextProcessor.processRequest(request); // 下一个处理器处理
            if (request.hdr != null) { // 事务请求（事务请求在前一个处理器设置了请求头hdr）
                // We need to sync and get consensus on any transactions
                try {
                    zks.getLeader().propose(request); // leader给follower发送提议，等待确认，接收到过半确认后才提交该事务
                } catch (XidRolloverException e) { // zxid翻转异常，等待重新选举，更新zxid的epoch为新的epoch
                    throw new RequestProcessorException(e.getMessage(), e);
                }
                syncProcessor.processRequest(request); // 将请求转发给SyncRequestProcessor处理
            }
        }
    }

    // 关闭该处理器
    public void shutdown() {
        LOG.info("Shutting down");
        nextProcessor.shutdown(); // 关闭下一个处理器
        syncProcessor.shutdown(); // 关闭SyncRequestProcessor处理器
    }

}
