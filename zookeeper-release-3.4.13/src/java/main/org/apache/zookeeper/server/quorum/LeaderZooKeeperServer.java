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

import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.jmx.MBeanRegistry;
import org.apache.zookeeper.server.DataTreeBean;
import org.apache.zookeeper.server.FinalRequestProcessor;
import org.apache.zookeeper.server.PrepRequestProcessor;
import org.apache.zookeeper.server.RequestProcessor;
import org.apache.zookeeper.server.ServerCnxn;
import org.apache.zookeeper.server.SessionTrackerImpl;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;

/**
 * Just like the standard ZooKeeperServer. We just replace the request
 * processors: PrepRequestProcessor -> ProposalRequestProcessor ->
 * CommitProcessor -> Leader.ToBeAppliedRequestProcessor -> FinalRequestProcessor
 * LeaderZooKeeperServer和单机ZooKeeperServer基本一样，只是请求的处理过程不同。
 */
public class LeaderZooKeeperServer extends QuorumZooKeeperServer {
    CommitProcessor commitProcessor;

    /**
     * @param logFactory
     * @param self
     * @param treeBuilder
     * @param zkDb
     * @throws IOException
     */
    LeaderZooKeeperServer(FileTxnSnapLog logFactory, QuorumPeer self,
            DataTreeBuilder treeBuilder, ZKDatabase zkDb) throws IOException {
        super(logFactory, self.tickTime, self.minSessionTimeout,
                self.maxSessionTimeout, treeBuilder, zkDb, self);
    }

    // 获取leader
    public Leader getLeader(){
        return self.leader;
    }

    // 设置leader服务的请求处理链
    // PrepRequestProcessor -> ProposalRequestProcessor -> CommitProcessor -> Leader.ToBeAppliedRequestProcessor -> FinalRequestProcessor
    //                                                 \
    //                                                  -> SyncRequestProcessor -> AckRequestProcessor
    @Override
    protected void setupRequestProcessors() {
        RequestProcessor finalProcessor = new FinalRequestProcessor(this);
        RequestProcessor toBeAppliedProcessor = new Leader.ToBeAppliedRequestProcessor(finalProcessor,
                getLeader().toBeApplied); // 将toBeApplied队列中的提议逐个取出交给finalProcessor处理
        commitProcessor = new CommitProcessor(toBeAppliedProcessor, Long.toString(getServerId()), false,
                getZooKeeperServerListener());
        commitProcessor.start(); // 启动commitProcessor
        ProposalRequestProcessor proposalProcessor = new ProposalRequestProcessor(this, commitProcessor);
        proposalProcessor.initialize(); // 启动SyncRequestProcessor处理器
        firstProcessor = new PrepRequestProcessor(this, proposalProcessor); // 头请求处理器
        ((PrepRequestProcessor)firstProcessor).start(); // 启动firstProcessor
    }

    // 获取最大请求堆积数量
    @Override
    public int getGlobalOutstandingLimit() {
        return super.getGlobalOutstandingLimit() / (self.getQuorumSize() - 1);
    }

    // 创建SessionTrackerImpl
    @Override
    public void createSessionTracker() {
        sessionTracker = new SessionTrackerImpl(this, getZKDatabase()
                .getSessionWithTimeOuts(), tickTime, self.getId(), getZooKeeperServerListener());
    }
    
    @Override
    protected void startSessionTracker() {
        ((SessionTrackerImpl)sessionTracker).start();
    }

    // 续租session
    public boolean touch(long sess, int to) {
        return sessionTracker.touchSession(sess, to);
    }

    // 注册JMX
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

    // 注册JMX
    public void registerJMX(LeaderBean leaderBean, LocalPeerBean localPeerBean) {
        // register with JMX
        if (self.jmxLeaderElectionBean != null) {
            try {
                MBeanRegistry.getInstance().unregister(self.jmxLeaderElectionBean);
            } catch (Exception e) {
                LOG.warn("Failed to register with JMX", e);
            }
            self.jmxLeaderElectionBean = null;
        }

        try {
            jmxServerBean = leaderBean;
            MBeanRegistry.getInstance().register(leaderBean, localPeerBean);
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            jmxServerBean = null;
        }
    }

    // 注销JMX jmxDataTreeBean
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

    // 注销JMX jmxServerBean
    protected void unregisterJMX(Leader leader) {
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
        return "leader";
    }

    /**
     * Returns the id of the associated QuorumPeer, which will do for a unique
     * id of this server. 获取服务id
     */
    @Override
    public long getServerId() {
        return self.getId();
    }    

    // 重新验证session,同时设置session的owner
    @Override
    protected void revalidateSession(ServerCnxn cnxn, long sessionId, int sessionTimeout) throws IOException {
        super.revalidateSession(cnxn, sessionId, sessionTimeout);
        try {
            // setowner as the leader itself, unless updated via the follower handlers
            setOwner(sessionId, ServerCnxn.me); // 设置session的owner
        } catch (SessionExpiredException e) {
            // this is ok, it just means that the session revalidation failed.
        }
    }
}
