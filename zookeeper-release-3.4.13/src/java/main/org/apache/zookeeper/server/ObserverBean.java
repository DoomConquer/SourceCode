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

import org.apache.zookeeper.server.quorum.Observer;
import org.apache.zookeeper.server.quorum.ObserverMXBean;

/**
 * ObserverBean
 * Observer MXBean
 *
 */
public class ObserverBean extends ZooKeeperServerBean implements ObserverMXBean{

    private Observer observer;
    
    public ObserverBean(Observer observer, ZooKeeperServer zks) {
        super(zks);        
        this.observer = observer;
    }

    // 名称
    public String getName() {
        return "Observer";
    }

    public int getPendingRevalidationCount() {
       return this.observer.getPendingRevalidationsCount(); 
    }

    // 获取Observer连接地址
    public String getQuorumAddress() {
        return observer.getSocket().toString();
    }

}
