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

package org.apache.zookeeper.server;

import java.io.Flushable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This RequestProcessor logs requests to disk. It batches the requests to do
 * the io efficiently. The request is not passed to the next RequestProcessor
 * until its log has been synced to disk.
 *
 * SyncRequestProcessor is used in 3 different cases
 * 1. Leader - Sync request to disk and forward it to AckRequestProcessor which
 *             send ack back to itself.
 * 2. Follower - Sync request to disk and forward request to
 *             SendAckRequestProcessor which send the packets to leader.
 *             SendAckRequestProcessor is flushable which allow us to force
 *             push packets to leader.
 * 3. Observer - Sync committed request to disk (received as INFORM packet).
 *             It never send ack back to the leader, so the nextProcessor will
 *             be null. This change the semantic of txnlog on the observer
 *             since it only contains committed txns.
 * 事务日志记录处理器，该处理器主要用于将事务请求记录到事务日志中去，同时还会触发zookeeper进行数据快照
 */
public class SyncRequestProcessor extends ZooKeeperCriticalThread implements RequestProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(SyncRequestProcessor.class);
    private final ZooKeeperServer zks;
    private final LinkedBlockingQueue<Request> queuedRequests = new LinkedBlockingQueue<Request>(); // 请求阻塞队列
    private final RequestProcessor nextProcessor; // 下一个请求处理器

    private Thread snapInProcess = null;
    volatile private boolean running; // 是否运行中

    /**
     * Transactions that have been written and are waiting to be flushed to
     * disk. Basically this is the list of SyncItems whose callbacks will be
     * invoked after flush returns successfully.
     */
    private final LinkedList<Request> toFlush = new LinkedList<Request>(); // 事务等待flush到磁盘
    private final Random r = new Random(System.nanoTime());
    /**
     * The number of log entries to log before starting a snapshot
     */
    private static int snapCount = ZooKeeperServer.getSnapCount(); // 相邻两次快照之间的事务操作次数
    
    /**
     * The number of log entries before rolling the log, number
     * is chosen randomly
     */
    private static int randRoll; // 回滚前事务日志数量

    private final Request requestOfDeath = Request.requestOfDeath;

    public SyncRequestProcessor(ZooKeeperServer zks, RequestProcessor nextProcessor) {
        super("SyncThread:" + zks.getServerId(), zks.getZooKeeperServerListener()); // 初始化ZooKeeperCriticalThread
        this.zks = zks;
        this.nextProcessor = nextProcessor;
        running = true;
    }
    
    /**
     * used by tests to check for changing
     * snapcounts
     * @param count
     */
    public static void setSnapCount(int count) { // 用于测试
        snapCount = count;
        randRoll = count;
    }

    /**
     * used by tests to get the snapcount
     * @return the snapcount
     */
    public static int getSnapCount() {
        return snapCount;
    }
    
    /**
     * Sets the value of randRoll. This method 
     * is here to avoid a findbugs warning for
     * setting a static variable in an instance
     * method. 
     * 
     * @param roll
     */
    private static void setRandRoll(int roll) {
        randRoll = roll;
    }

    // 执行线程
    @Override
    public void run() {
        try {
            int logCount = 0; // 事务日志数量

            // we do this in an attempt to ensure that not all of the servers
            // in the ensemble take a snapshot at the same time
            // 防止所有的服务同时进行数据快照
            setRandRoll(r.nextInt(snapCount / 2));
            while (true) {
                Request si = null;
                if (toFlush.isEmpty()) {
                    si = queuedRequests.take(); // 若队列为空阻塞
                } else {
                    si = queuedRequests.poll(); // 若队列为空返回null
                    if (si == null) { // queuedRequests队列中没有请求
                        flush(toFlush);
                        continue;
                    }
                }
                if (si == requestOfDeath) { // 跳出循环
                    break;
                }
                if (si != null) {
                    // track the number of records written to the log
                    if (zks.getZKDatabase().append(si)) { // 向事务日志文件中记录请求
                        logCount++;
                        if (logCount > (snapCount / 2 + randRoll)) { // logCount超过snapCount一半 + 随机数
                            setRandRoll(r.nextInt(snapCount / 2));
                            // roll the log
                            zks.getZKDatabase().rollLog(); // 切换事务日志文件，等到append数据时新建
                            // take a snapshot
                            if (snapInProcess != null && snapInProcess.isAlive()) { // 上一次还没处理完，直接跳过，下一次再处理
                                LOG.warn("Too busy to snap, skipping");
                            } else { // 新起一个线程处理数据快照
                                snapInProcess = new ZooKeeperThread("Snapshot Thread") {
                                        public void run() {
                                            try {
                                                zks.takeSnapshot(); // 进行数据快照
                                            } catch(Exception e) {
                                                LOG.warn("Unexpected exception", e);
                                            }
                                        }
                                    };
                                snapInProcess.start();
                            }
                            logCount = 0; // 复位0
                        }
                    } else if (toFlush.isEmpty()) { // 有些请求的hdr为空，zks.getZKDatabase().append会返回false，所以如果是不需要挂起处理的可以先让后续的处理器处理
                        // optimization for read heavy workloads
                        // iff this is a read, and there are no pending
                        // flushes (writes), then just pass this to the next
                        // processor
                        if (nextProcessor != null) {
                            nextProcessor.processRequest(si);
                            if (nextProcessor instanceof Flushable) { // 如果下一个处理器实现了接口Flushable
                                ((Flushable)nextProcessor).flush(); // 刷新（在follower的请求处理链中，SendAckRequestProcessor实现了Flushable）
                            }
                        }
                        continue;
                    }
                    toFlush.add(si);
                    if (toFlush.size() > 1000) { // 超过1000
                        flush(toFlush);
                    }
                }
            }
        } catch (Throwable t) {
            handleException(this.getName(), t); // 处理异常，通知服务关闭
            running = false;
        }
        LOG.info("SyncRequestProcessor exited!");
    }

    // 将缓存区数据刷到磁盘，并让下一个处理器处理toFlush中的请求
    private void flush(LinkedList<Request> toFlush)
        throws IOException, RequestProcessorException
    {
        if (toFlush.isEmpty())
            return;

        zks.getZKDatabase().commit(); // 提交日志事务
        while (!toFlush.isEmpty()) {
            Request i = toFlush.remove();
            if (nextProcessor != null) {
                nextProcessor.processRequest(i);
            }
        }
        // 下一个处理器flush
        if (nextProcessor != null && nextProcessor instanceof Flushable) {
            ((Flushable)nextProcessor).flush();
        }
    }

    // 关闭处理器
    public void shutdown() {
        LOG.info("Shutting down");
        queuedRequests.add(requestOfDeath);
        try {
            if(running){
                this.join(); // 等待当前线程执行完
            }
            if (!toFlush.isEmpty()) { // 刷新缓冲区数据到磁盘
                flush(toFlush);
            }
        } catch(InterruptedException e) {
            LOG.warn("Interrupted while wating for " + this + " to finish");
        } catch (IOException e) {
            LOG.warn("Got IO exception during shutdown");
        } catch (RequestProcessorException e) {
            LOG.warn("Got request processor exception during shutdown");
        }
        if (nextProcessor != null) { // 关闭后续处理器
            nextProcessor.shutdown();
        }
    }

    // 处理请求，请求阻塞队列中添加请求，线程去执行
    public void processRequest(Request request) {
        // request.addRQRec(">sync");
        queuedRequests.add(request);
    }

}
