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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.ServerCnxn;

// digest认证模式
public class DigestAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DigestAuthenticationProvider.class);

    /** specify a command line property with key of 
     * "zookeeper.DigestAuthenticationProvider.superDigest"
     * and value of "super:<base64encoded(SHA1(password))>" to enable
     * super user access (i.e. acls disabled)
     * 超级管理员
     */
    private final static String superDigest = System.getProperty("zookeeper.DigestAuthenticationProvider.superDigest");

    // digest模式
    public String getScheme() {
        return "digest";
    }

    // base64编码
    static final private String base64Encode(byte b[]) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length;) {
            int pad = 0; // 记录填充了几次
            int v = (b[i++] & 0xff) << 16;
            if (i < b.length) {
                v |= (b[i++] & 0xff) << 8;
            } else {
                pad++;
            }
            if (i < b.length) {
                v |= (b[i++] & 0xff);
            } else {
                pad++;
            }
            // 当总字节数最后剩一个（8位）或两个（16位）时，需要填充才能分组成6位一组，所以如果8位填充4位后面填充两个=号，
            // 如果是16位填充2位，后面填充一个=号
            sb.append(encode(v >> 18)); // 总共24位有效数据，右移18位，后6位刚好转换换成base编码
            sb.append(encode(v >> 12)); // 右移12位，读取第二个6位
            if (pad < 2) {
                sb.append(encode(v >> 6)); // 右移6位，读取第三个6位
            } else { // 填充了两次
                sb.append('=');
            }
            if (pad < 1) {
                sb.append(encode(v)); // 读取最后的6位
            } else {
                sb.append('=');
            }
        }
        return sb.toString();
    }

    // 将i的后6位转换成base64编码字符
    static final private char encode(int i) {
        i &= 0x3f; // 后6位
        if (i < 26) {
            return (char) ('A' + i);
        }
        if (i < 52) {
            return (char) ('a' + i - 26);
        }
        if (i < 62) {
            return (char) ('0' + i - 52);
        }
        return i == 62 ? '+' : '/';
    }

    // 生成digest（username:base64编码的idPassword）
    static public String generateDigest(String idPassword)
            throws NoSuchAlgorithmException {
        String parts[] = idPassword.split(":", 2);
        byte digest[] = MessageDigest.getInstance("SHA1").digest(
                idPassword.getBytes());
        return parts[0] + ":" + base64Encode(digest);
    }

    // 处理认证信息
    public KeeperException.Code handleAuthentication(ServerCnxn cnxn, byte[] authData) {
        String id = new String(authData);
        try {
            String digest = generateDigest(id);
            if (digest.equals(superDigest)) { // 超级管理员
                cnxn.addAuthInfo(new Id("super", "")); // 添加认证信息到cnxn
            }
            cnxn.addAuthInfo(new Id(getScheme(), digest));
            return KeeperException.Code.OK;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Missing algorithm",e);
        }
        return KeeperException.Code.AUTHFAILED;
    }

    public boolean isAuthenticated() {
        return true;
    }

    // 验证权限格式是否有效
    public boolean isValid(String id) {
        String parts[] = id.split(":");
        return parts.length == 2;
    }

    // 验证ACL权限是否匹配
    public boolean matches(String id, String aclExpr) {
        return id.equals(aclExpr);
    }

    /** Call with a single argument of user:pass to generate authdata.
     * Authdata output can be used when setting superDigest for example. 
     * @param args single argument of user:pass
     * @throws NoSuchAlgorithmException
     */
    public static void main(String args[]) throws NoSuchAlgorithmException {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i] + "->" + generateDigest(args[i]));
        }
    }
}
