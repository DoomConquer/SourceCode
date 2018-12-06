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

import org.apache.zookeeper.server.ZooKeeperServerMXBean;

/**
 * Follower MBean
 * follower MBean接口
 */
public interface FollowerMXBean extends ZooKeeperServerMXBean {
    /**
     * @return socket address
     */
    public String getQuorumAddress();
    
    /**
     * @return last queued zxid
     */
    public String getLastQueuedZxid();
    
    /**
     * @return count of pending revalidations
     * 获取挂起的需要验证session的连接
     */
    public int getPendingRevalidationCount();

    /**
     * @return time taken for leader election in milliseconds.
     * 获取选举耗费时间
     */
    public long getElectionTimeTaken();
}
