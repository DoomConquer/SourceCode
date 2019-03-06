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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.server.SessionTracker;
import org.apache.zookeeper.server.SessionTrackerImpl;
import org.apache.zookeeper.server.ZooKeeperServerListener;

/**
 * This is really just a shell（外壳） of a SessionTracker that tracks session activity
 * to be forwarded to the Leader using a PING.
 * 实现SessionTracker类（PING时转发给leader）
 */
public class LearnerSessionTracker implements SessionTracker {
    SessionExpirer expirer; // session过期

    HashMap<Long, Integer> touchTable = new HashMap<Long, Integer>(); // 活动的session

    long serverId = 1;      // 服务id
    long nextSessionId = 0; // 下一个sessionId值（开始用SessionTrackerImpl.initializeNextSession初始化一个，以后自增）
    
    private ConcurrentHashMap<Long, Integer> sessionsWithTimeouts; // session及其对应的超时时间（客户端连接learner的session）

    public LearnerSessionTracker(SessionExpirer expirer,
            ConcurrentHashMap<Long, Integer> sessionsWithTimeouts, long id,
            ZooKeeperServerListener listener) {
        this.expirer = expirer;
        this.sessionsWithTimeouts = sessionsWithTimeouts;
        this.serverId = id;
        nextSessionId = SessionTrackerImpl.initializeNextSession(this.serverId); // 初始化sessionId
        
    }

    // 移除session
    synchronized public void removeSession(long sessionId) {
        sessionsWithTimeouts.remove(sessionId);
        touchTable.remove(sessionId);
    }

    public void shutdown() {
    }

    // 添加session
    synchronized public void addSession(long sessionId, int sessionTimeout) {
        sessionsWithTimeouts.put(sessionId, sessionTimeout);
        touchTable.put(sessionId, sessionTimeout);
    }

    // 续租session
    synchronized public boolean touchSession(long sessionId, int sessionTimeout) {
        touchTable.put(sessionId, sessionTimeout);
        return true;
    }

    // 获取当前活动的session（map内容是sessionId -> sessionTimeout）
    synchronized HashMap<Long, Integer> snapshot() {
        HashMap<Long, Integer> oldTouchTable = touchTable;
        touchTable = new HashMap<Long, Integer>();
        return oldTouchTable;
    }

    // 创建session，sessionId自增
    synchronized public long createSession(int sessionTimeout) {
        return (nextSessionId++);
    }

    public void checkSession(long sessionId, Object owner)  {
        // Nothing to do here. Sessions are checked at the Leader
    }
    
    public void setOwner(long sessionId, Object owner) {
        // Nothing to do here. Sessions are checked at the Leader
    }

    public void dumpSessions(PrintWriter pwriter) {
    	// the original class didn't have tostring impl, so just
    	// dup what we had before
    	pwriter.println(toString());
    }

    public void setSessionClosing(long sessionId) {
        // Nothing to do here.
    }
}