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

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.zookeeper.ZooDefs.OpCode;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.ZooKeeperCriticalThread;
import org.apache.zookeeper.server.ZooTrace;

/**
 * This RequestProcessor forwards any requests that modify the state of the
 * system to the Leader.
 * 跟随者的第一个请求处理器，将任何改变结点状态的请求（事务请求）转发给leader
 */
public class FollowerRequestProcessor extends ZooKeeperCriticalThread implements RequestProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FollowerRequestProcessor.class);

    FollowerZooKeeperServer zks;

    RequestProcessor nextProcessor; // 下一个请求处理器

    LinkedBlockingQueue<Request> queuedRequests = new LinkedBlockingQueue<Request>(); // 请求队列

    boolean finished = false; // 该处理器线程运行标志

    public FollowerRequestProcessor(FollowerZooKeeperServer zks, RequestProcessor nextProcessor) {
        super("FollowerRequestProcessor:" + zks.getServerId(), zks.getZooKeeperServerListener());
        this.zks = zks;
        this.nextProcessor = nextProcessor;
    }

    @Override
    public void run() {
        try {
            while (!finished) {
                Request request = queuedRequests.take(); // 取出请求
                if (LOG.isTraceEnabled()) {
                    ZooTrace.logRequest(LOG, ZooTrace.CLIENT_REQUEST_TRACE_MASK, 'F', request, "");
                }
                if (request == Request.requestOfDeath) { // 结束
                    break;
                }
                // We want to queue the request to be processed before we submit
                // the request to the leader so that we are ready to receive
                // the response
                nextProcessor.processRequest(request); // 先将请求加入CommitProcessor处理器的请求队列
                
                // We now ship the request to the leader. As with all
                // other quorum operations, sync also follows this code
                // path, but different from others, we need to keep track
                // of the sync operations this follower has pending, so we
                // add it to pendingSyncs.
                switch (request.type) { // 发送事务请求给leader
                case OpCode.sync:
                    zks.pendingSyncs.add(request); // 挂起同步请求
                    zks.getFollower().request(request);
                    break;
                case OpCode.create:
                case OpCode.delete:
                case OpCode.setData:
                case OpCode.setACL:
                case OpCode.createSession:
                case OpCode.closeSession:
                case OpCode.multi:
                    zks.getFollower().request(request);
                    break;
                }
            }
        } catch (Exception e) { // 处理异常
            handleException(this.getName(), e);
        }
        LOG.info("FollowerRequestProcessor exited loop!");
    }

    // 将请求添加到请求队列queuedRequests
    public void processRequest(Request request) {
        if (!finished) {
            queuedRequests.add(request);
        }
    }

    // 关闭该处理器
    public void shutdown() {
        LOG.info("Shutting down");
        finished = true;
        queuedRequests.clear(); // 清空请求队列
        queuedRequests.add(Request.requestOfDeath); // 放入结束标志请求
        nextProcessor.shutdown(); // 关闭下一个请求处理器
    }

}
