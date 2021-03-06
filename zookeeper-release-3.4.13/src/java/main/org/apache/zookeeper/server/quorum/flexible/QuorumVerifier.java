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

package org.apache.zookeeper.server.quorum.flexible;

        import java.util.Set;

/**
 * All quorum validators have to implement a method called
 * containsQuorum, which verifies if a Set of server
 * identifiers constitutes a quorum.
 * 验证是否能够完成选举，containsQuorum方法验证一组服务id是否能完成选举，如过半选举
 */

public interface QuorumVerifier {
    long getWeight(long id);
    boolean containsQuorum(Set<Long> set);
}
