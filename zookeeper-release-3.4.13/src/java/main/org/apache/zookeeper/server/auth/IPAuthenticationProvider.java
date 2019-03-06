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
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.ServerCnxn;

// ip认证模式
public class IPAuthenticationProvider implements AuthenticationProvider {

    public String getScheme() {
        return "ip";
    }

    public KeeperException.Code handleAuthentication(ServerCnxn cnxn, byte[] authData) {
        String id = cnxn.getRemoteSocketAddress().getAddress().getHostAddress(); // host地址
        cnxn.addAuthInfo(new Id(getScheme(), id));
        return KeeperException.Code.OK;
    }

    // This is a bit weird but we need to return the address and the number of
    // bytes (to distinguish between IPv4 and IPv6
    private byte[] addr2Bytes(String addr) {
        byte b[] = v4addr2Bytes(addr);
        // TODO Write the v6addr2Bytes
        return b;
    }

    // ipv4地址转换成字节数组
    private byte[] v4addr2Bytes(String addr) {
        String parts[] = addr.split("\\.", -1);
        if (parts.length != 4) {
            return null;
        }
        byte b[] = new byte[4];
        for (int i = 0; i < 4; i++) {
            try {
                int v = Integer.parseInt(parts[i]);
                if (v >= 0 && v <= 255) { // 0-255之间
                    b[i] = (byte) v; // 转换为字节
                } else {
                    return null;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return b;
    }

    // 掩码计算，保留前bits位
    private void mask(byte b[], int bits) {
        int start = bits / 8;
        int startMask = (1 << (8 - (bits % 8))) - 1;
        startMask = ~startMask; // 取反
        while (start < b.length) {
            b[start] &= startMask;
            startMask = 0; // 后面的字节都变为0（&0）
            start++;
        }
    }

    // 判断权限是否匹配（如：ip:192.168.0.1/24，掩码24位，保留前24位，后面为0）
    public boolean matches(String id, String aclExpr) {
        String parts[] = aclExpr.split("/", 2);
        byte aclAddr[] = addr2Bytes(parts[0]);
        if (aclAddr == null) {
            return false;
        }
        int bits = aclAddr.length * 8; // 32位
        if (parts.length == 2) {
            try {
                bits = Integer.parseInt(parts[1]); // 掩码
                if (bits < 0 || bits > aclAddr.length * 8) { // 掩码非法
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        mask(aclAddr, bits);
        byte remoteAddr[] = addr2Bytes(id);
        if (remoteAddr == null) {
            return false;
        }
        mask(remoteAddr, bits);
        for (int i = 0; i < remoteAddr.length; i++) { // 比价两个ip掩码处理后是否相等
            if (remoteAddr[i] != aclAddr[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isAuthenticated() {
        return false;
    }

    // 认证是否有效
    public boolean isValid(String id) {
        return addr2Bytes(id) != null;
    }
}