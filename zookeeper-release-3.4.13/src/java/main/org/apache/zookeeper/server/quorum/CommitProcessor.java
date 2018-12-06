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

import java.util.ArrayList;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.ZooDefs.OpCode;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.ZooKeeperCriticalThread;
import org.apache.zookeeper.server.ZooKeeperServerListener;

/**
 * This RequestProcessor matches the incoming committed requests with the
 * locally submitted requests. The trick is that locally submitted requests that
 * change the state of the system will come back as incoming committed requests,
 * so we need to match them up.
 * 该请求处理器用于处理提交的事务请求，如果是会改变结点状态的请求，需要先发送给leader处理，在发送前
 * 会先保存请求到该处理器的queuedRequests队列中，在leader发送响应给learner时，验证请求是否匹配。
 */
public class CommitProcessor extends ZooKeeperCriticalThread implements RequestProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(CommitProcessor.class);

    /**
     * Requests that we are holding until the commit comes in.
     * 请求队列（先保存请求，等待leader提交）
     */
    LinkedList<Request> queuedRequests = new LinkedList<Request>();

    /**
     * Requests that have been committed.
     * 已经提交的请求
     */
    LinkedList<Request> committedRequests = new LinkedList<Request>();

    RequestProcessor nextProcessor; // 下一个请求处理器
    ArrayList<Request> toProcess = new ArrayList<Request>(); // 可以处理的请求（不需要等待leader响应）

    /**
     * This flag indicates whether we need to wait for a response to come back from the
     * leader or we just let the sync operation flow through like a read. The flag will
     * be false if the CommitProcessor is in a Leader pipeline.
     * leader请求处理链中的CommitProcessor该值为false
     */
    boolean matchSyncs; // sync请求是否需要等待leader的响应

    public CommitProcessor(RequestProcessor nextProcessor, String id,
            boolean matchSyncs, ZooKeeperServerListener listener) {
        super("CommitProcessor:" + id, listener);
        this.nextProcessor = nextProcessor;
        this.matchSyncs = matchSyncs;
    }

    volatile boolean finished = false; // 该处理器线程运行标志

    @Override
    public void run() {
        try {
            Request nextPending = null; // 等待leader响应的请求
            while (!finished) {
                int len = toProcess.size();
                for (int i = 0; i < len; i++) { // 取出toProcess中的请求让下一个处理器处理
                    nextProcessor.processRequest(toProcess.get(i));
                }
                toProcess.clear(); // 清空toProcess
                synchronized (this) {
                    // 如果请求队列为空或nextPending不为空并且已提交队列为空，线程等待（commit方法和processRequest方法会唤醒）
                    if ((queuedRequests.size() == 0 || nextPending != null) && committedRequests.size() == 0) {
                        wait();
                        continue;
                    }
                    // First check and see if the commit came in for the pending request
                    if ((queuedRequests.size() == 0 || nextPending != null) && committedRequests.size() > 0) {
                        Request r = committedRequests.remove(); // 取出committedRequests队头元素
                        /*
                         * We match with nextPending so that we can move to the
                         * next request when it is committed. We also want to
                         * use nextPending because it has the cnxn member set
                         * properly.
                         */
                        if (nextPending != null
                                && nextPending.sessionId == r.sessionId
                                && nextPending.cxid == r.cxid) { // 接收到leader请求（队头元素）和nextPending匹配
                            // we want to send our version of the request.
                            // the pointer to the connection in the request
                            nextPending.hdr = r.hdr;
                            nextPending.txn = r.txn;
                            nextPending.zxid = r.zxid;
                            toProcess.add(nextPending); // 添加到待处理请求队列toProcess
                            nextPending = null; // 设置nextPending为空，寻找下一个需要等待leader响应的请求
                        } else { // 处理其他提交的请求（例如同步时leader已经提交的请求，syncWithLeader中最后的处理）
                            // this request came from someone else so just send the commit packet
                            toProcess.add(r);
                        }
                    }
                }

                // We haven't matched the pending requests, so go back to waiting
                // 没有和nextPending匹配的请求，继续等待
                if (nextPending != null) {
                    continue;
                }

                synchronized (this) {
                    // Process the next requests in the queuedRequests
                    while (nextPending == null && queuedRequests.size() > 0) { // 寻找需要等待leader响应的请求
                        Request request = queuedRequests.remove();
                        switch (request.type) {
                        case OpCode.create:
                        case OpCode.delete:
                        case OpCode.setData:
                        case OpCode.multi:
                        case OpCode.setACL:
                        case OpCode.createSession:
                        case OpCode.closeSession:
                            nextPending = request;
                            break;
                        case OpCode.sync:
                            // sync请求是否需要等待leader响应（leader的请求处理链中不需要等待，所以直接加入到toProcess处理）
                            if (matchSyncs) {
                                nextPending = request;
                            } else { // 不用等待leader响应，直接处理
                                toProcess.add(request);
                            }
                            break;
                        default: // 非事务请求（不改变结点状态的请求，例如查询），直接进行处理，不用等待leader响应
                            toProcess.add(request);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted exception while waiting", e);
        } catch (Throwable e) {
            LOG.error("Unexpected exception causing CommitProcessor to exit", e);
        }
        LOG.info("CommitProcessor exited loop!");
    }

     // 提交请求，将请求加入committedRequests队列
    synchronized public void commit(Request request) {
        if (!finished) {
            if (request == null) {
                LOG.warn("Committed a null!", new Exception("committing a null! "));
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Committing request:: " + request);
            }
            committedRequests.add(request);
            notifyAll(); // 唤醒该处理器等待的线程
        }
    }

    // 处理请求，将请求加入queuedRequests队列
    synchronized public void processRequest(Request request) {
        // request.addRQRec(">commit");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing request:: " + request);
        }
        
        if (!finished) {
            queuedRequests.add(request);
            notifyAll(); // 唤醒等待的线程
        }
    }

    // 关闭该请求处理器
    public void shutdown() {
        LOG.info("Shutting down");
        synchronized (this) {
            finished = true;
            queuedRequests.clear(); // 清空queuedRequests队列
            notifyAll(); // 唤醒等待的线程
        }
        if (nextProcessor != null) {
            nextProcessor.shutdown(); // 关闭下一个请求处理器
        }
    }

}
