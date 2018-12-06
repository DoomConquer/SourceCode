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

package org.apache.zookeeper.server.auth;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.server.ServerCnxn;

// sasl认证模式
public class SASLAuthenticationProvider implements AuthenticationProvider {

    // 获取模式
    public String getScheme() {
        return "sasl";
    }

    // 该方法在sasl模式下不会被调用，认证过程在session初始化时处理
    public KeeperException.Code handleAuthentication(ServerCnxn cnxn, byte[] authData) {
        // Should never call this: SASL authentication is negotiated at session initiation.
        // TODO: consider substituting current implementation of direct ClientCnxn manipulation with
        // a call to this method (SASLAuthenticationProvider:handleAuthentication()) at session initiation.
        return KeeperException.Code.AUTHFAILED; // 抛出异常
    }

    // 校验id和aclExpr是否匹配
    public boolean matches(String id, String aclExpr) {
        if (System.getProperty("zookeeper.superUser") != null) { // 设置了zookeeper.superUser用户
            return (id.equals(System.getProperty("zookeeper.superUser")) || id.equals(aclExpr));
        }
        return (id.equals("super") || id.equals(aclExpr));
    }

    public boolean isAuthenticated() {
        return true;
    }

    // 使用Kerberos验证id的有效性，构造KerberosName时没有异常就有效
    public boolean isValid(String id) {
        // Since the SASL authenticator will usually be used with Kerberos authentication,
        // it should enforce that these names are valid according to Kerberos's
        // syntax for principals.
        //
        // Use the KerberosName(id) constructor to define validity:
        // if KerberosName(id) throws IllegalArgumentException, then id is invalid.
        // otherwise, it is valid.
        //
        try {
            new KerberosName(id);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
   }


}
