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

import org.apache.zookeeper.jmx.MBeanRegistry;
import org.apache.zookeeper.server.DataTreeBean;
import org.apache.zookeeper.server.FinalRequestProcessor;
import org.apache.zookeeper.server.PrepRequestProcessor;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.ZooKeeperServerBean;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;

/**
 * A ZooKeeperServer which comes into play when peer is partitioned from the
 * majority. Handles read-only clients, but drops connections from not-read-only
 * ones. 该服务用于集群产生分区时，只读客户端可以连接访问，非只读的客户端断开连接
 * <p>
 * The very first processor in the chain of request processors is a
 * ReadOnlyRequestProcessor which drops state-changing requests.
 * 第一个请求处理器ReadOnlyRequestProcessor会丢掉事务请求（改变结点状态的请求）
 */
public class ReadOnlyZooKeeperServer extends QuorumZooKeeperServer {

    private volatile boolean shutdown = false; // 该服务关闭标志
    ReadOnlyZooKeeperServer(FileTxnSnapLog logFactory, QuorumPeer self, DataTreeBuilder treeBuilder, ZKDatabase zkDb) {
        super(logFactory, self.tickTime, self.minSessionTimeout, self.maxSessionTimeout, treeBuilder, zkDb, self);
    }

    // 设置请求处理链
    // ReadOnlyRequestProcessor -> PrepRequestProcessor -> FinalRequestProcessor
    @Override
    protected void setupRequestProcessors() {
        RequestProcessor finalProcessor = new FinalRequestProcessor(this);
        RequestProcessor prepProcessor = new PrepRequestProcessor(this, finalProcessor);
        ((PrepRequestProcessor) prepProcessor).start(); // 启动PrepRequestProcessor处理器
        firstProcessor = new ReadOnlyRequestProcessor(this, prepProcessor);
        ((ReadOnlyRequestProcessor) firstProcessor).start(); // 启动ReadOnlyRequestProcessor处理器
    }

    // 启动ReadOnlyZooKeeperServer服务
    @Override
    public synchronized void startup() {
        // check to avoid startup follows shutdown
        if (shutdown) { // 已经关闭
            LOG.warn("Not starting Read-only server as startup follows shutdown!");
            return;
        }
        registerJMX(new ReadOnlyBean(this), self.jmxLocalPeerBean); // 注册JMX
        super.startup(); // 启动父类ZooKeeperServer
        self.cnxnFactory.setZooKeeperServer(this); // 设置ZooKeeperServer
        LOG.info("Read-only server started");
    }

    // 注册JMX DataTreeBean
    @Override
    protected void registerJMX() {
        // register with JMX
        try {
            jmxDataTreeBean = new DataTreeBean(getZKDatabase().getDataTree());
            MBeanRegistry.getInstance().register(jmxDataTreeBean, jmxServerBean);
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            jmxDataTreeBean = null;
        }
    }

    // 注册JMX ZooKeeperServerBean
    public void registerJMX(ZooKeeperServerBean serverBean, LocalPeerBean localPeerBean) {
        // register with JMX
        try {
            jmxServerBean = serverBean;
            MBeanRegistry.getInstance().register(serverBean, localPeerBean);
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            jmxServerBean = null;
        }
    }

    // 设置该服务运行状态
    @Override
    protected void setState(State state) {
        this.state = state;
    }

    // 注销JMX DataTreeBean
    @Override
    protected void unregisterJMX() {
        // unregister from JMX
        try {
            if (jmxDataTreeBean != null) {
                MBeanRegistry.getInstance().unregister(jmxDataTreeBean);
            }
        } catch (Exception e) {
            LOG.warn("Failed to unregister with JMX", e);
        }
        jmxDataTreeBean = null;
    }

    // 注销JMX ZooKeeperServerBean
    protected void unregisterJMX(ZooKeeperServer zks) {
        // unregister from JMX
        try {
            if (jmxServerBean != null) {
                MBeanRegistry.getInstance().unregister(jmxServerBean);
            }
        } catch (Exception e) {
            LOG.warn("Failed to unregister with JMX", e);
        }
        jmxServerBean = null;
    }

    // 获取服务角色
    @Override
    public String getState() {
        return "read-only";
    }

    /**
     * Returns the id of the associated QuorumPeer, which will do for a unique
     * id of this server. 获取服务id
     */
    @Override
    public long getServerId() {
        return self.getId();
    }

    // 关闭ReadOnlyZooKeeperServer服务
    @Override
    public synchronized void shutdown() {
        if (!canShutdown()) { // 服务没有处于运行中
            LOG.debug("ZooKeeper server is not running, so not proceeding to shutdown!");
            return;
        }
        shutdown = true;     // 设置关闭标志
        unregisterJMX(this); // 注销JMX

        // set peer's server to null
        self.cnxnFactory.setZooKeeperServer(null); // 设置ZooKeeperServer为null，客户端连接检测到服务关闭会做出相应的处理
        // clear all the connections
        self.cnxnFactory.closeAll(); // 关闭客户端所有连接

        // shutdown the server itself
        super.shutdown(); // 关闭该服务
    }

}
