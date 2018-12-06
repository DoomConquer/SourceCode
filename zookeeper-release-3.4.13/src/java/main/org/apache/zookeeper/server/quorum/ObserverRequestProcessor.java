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
 */
public class ObserverRequestProcessor extends ZooKeeperCriticalThread implements RequestProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ObserverRequestProcessor.class);

    ObserverZooKeeperServer zks; // 观察者ZooKeeperServer服务

    RequestProcessor nextProcessor; // 下一个请求处理器

    // We keep a queue of requests. As requests get submitted they are 
    // stored here. The queue is drained in the run() method. 
    LinkedBlockingQueue<Request> queuedRequests = new LinkedBlockingQueue<Request>(); // 请求队列

    boolean finished = false; // ObserverRequestProcessor处理器线程运行标志

    /**
     * Constructor - takes an ObserverZooKeeperServer to associate with
     * and the next processor to pass requests to after we're finished. 
     * @param zks
     * @param nextProcessor
     */
    public ObserverRequestProcessor(ObserverZooKeeperServer zks, RequestProcessor nextProcessor) {
        super("ObserverRequestProcessor:" + zks.getServerId(), zks.getZooKeeperServerListener());
        this.zks = zks;
        this.nextProcessor = nextProcessor;
    }

    // 线程执行
    @Override
    public void run() {
        try {
            while (!finished) {
                Request request = queuedRequests.take(); // 从请求队列取出请求处理
                if (LOG.isTraceEnabled()) {
                    ZooTrace.logRequest(LOG, ZooTrace.CLIENT_REQUEST_TRACE_MASK, 'F', request, "");
                }
                if (request == Request.requestOfDeath) { // 结束标志请求
                    break;
                }
                // We want to queue the request to be processed before we submit
                // the request to the leader so that we are ready to receive
                // the response
                nextProcessor.processRequest(request); // 先加入CommitProcessor的请求队列中，然后发送给leader
                
                // We now ship the request to the leader. As with all
                // other quorum operations, sync also follows this code
                // path, but different from others, we need to keep track
                // of the sync operations this Observer has pending, so we
                // add it to pendingSyncs.
                // 只有那些会改变zk结点状态的请求需要发送给leader，如果是查询的请求不用发送leader，
                // 该服务自己通过FinalRequestProcessor处理器处理后返回结果
                switch (request.type) {
                case OpCode.sync:
                    zks.pendingSyncs.add(request); // 如果是sync请求，将请求放入挂起同步请求队列，等leader响应后处理
                    zks.getObserver().request(request); // 发送request给leader
                    break;
                case OpCode.create:
                case OpCode.delete:
                case OpCode.setData:
                case OpCode.setACL:
                case OpCode.createSession:
                case OpCode.closeSession:
                case OpCode.multi:
                    zks.getObserver().request(request); // 发送request给leader
                    break;
                }
            }
        } catch (Exception e) { // 异常处理
            handleException(this.getName(), e);
        }
        LOG.info("ObserverRequestProcessor exited loop!");
    }

    /**
     * Simply queue the request, which will be processed in FIFO order.
     * 加入请求队列，等待线程处理
     */
    public void processRequest(Request request) {
        if (!finished) {
            queuedRequests.add(request);
        }
    }

    /**
     * Shutdown the processor. 关闭处理器
     */
    public void shutdown() {
        LOG.info("Shutting down");
        finished = true;
        queuedRequests.clear(); // 清空请求队列
        queuedRequests.add(Request.requestOfDeath); // 加入结束标志请求
        nextProcessor.shutdown(); // 关闭下一个请求处理器
    }

}
