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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;

import org.apache.zookeeper.server.ZooKeeperSaslServer;

/**
 下面示例配置zookeeper使用sasl（例子来源网络）
 1、单节点zookeeper配置
 注意，这里我只使用Kafka里集成的zookeeper。先配置zookeeper.properties配置文件，写入如下配置项：
 authProvider.1=org.apache.zookeeper.server.auth.SASLAuthenticationProvider
 requireClientAuthScheme=sasl
 jaasLoginRenew=3600000

 2、创建JAAS配置文件
 JAAS配置文件指定身份认证插件，并提供登录到zookeeper的登录名与密码。创建了一个配置文件kafka_zoo_jaas.conf ：
 ZKServer {
     org.apache.kafka.common.security.plain.PlainLoginModule required
     username="admin"
     password="admin-secret"
     user_admin="admin-secret";
 };

 3、启动前设置环境变量
 设置的环境变量其实只是传入到JVM的参数。我们设置的环境变量是KAFKA_OPTS。zookeeper的启动脚本调用如下：
 bin/zookeeper-server-start.sh

 这里配置两个变量：
 -Djava.security.auth.login.config=$ROOT/config/kafka_zoo_jaas.conf
 -Dzookeeper.sasl.serverconfig=ZKServer

 */
// 服务端sasl认证回调
public class SaslServerCallbackHandler implements CallbackHandler {
    private static final String USER_PREFIX = "user_"; // 配置文件中username前缀
    private static final Logger LOG = LoggerFactory.getLogger(SaslServerCallbackHandler.class);
    private static final String SYSPROP_SUPER_PASSWORD = "zookeeper.SASLAuthenticationProvider.superPassword";
    private static final String SYSPROP_REMOVE_HOST = "zookeeper.kerberos.removeHostFromPrincipal";
    private static final String SYSPROP_REMOVE_REALM = "zookeeper.kerberos.removeRealmFromPrincipal";

    private String userName; // 用户名
    private final Map<String,String> credentials = new HashMap<String,String>();

    public SaslServerCallbackHandler(Configuration configuration)
            throws IOException {
        String serverSection = System.getProperty(
                ZooKeeperSaslServer.LOGIN_CONTEXT_NAME_KEY,
                ZooKeeperSaslServer.DEFAULT_LOGIN_CONTEXT_NAME);
        AppConfigurationEntry configurationEntries[] = configuration.getAppConfigurationEntry(serverSection);

        if (configurationEntries == null) { // 没有找到配置
            String errorMessage = "Could not find a '" + serverSection + "' entry in this configuration: Server cannot start.";
            LOG.error(errorMessage);
            throw new IOException(errorMessage);
        }
        credentials.clear();
        for(AppConfigurationEntry entry: configurationEntries) { // 配置文件实体，如上面例子中的ZKServer
            Map<String, ?> options = entry.getOptions(); // 配置属性
            // Populate DIGEST-MD5 user -> password map with JAAS configuration entries from the "Server" section.
            // Usernames are distinguished from other options by prefixing the username with a "user_" prefix.
            for(Map.Entry<String, ?> pair : options.entrySet()) {
                String key = pair.getKey();
                if (key.startsWith(USER_PREFIX)) { // username
                    String userName = key.substring(USER_PREFIX.length());
                    credentials.put(userName, (String)pair.getValue());
                }
            }
        }
    }

    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                handleNameCallback((NameCallback) callback);
            } else if (callback instanceof PasswordCallback) {
                handlePasswordCallback((PasswordCallback) callback);
            } else if (callback instanceof RealmCallback) {
                handleRealmCallback((RealmCallback) callback);
            } else if (callback instanceof AuthorizeCallback) {
                handleAuthorizeCallback((AuthorizeCallback) callback);
            }
        }
    }

    private void handleNameCallback(NameCallback nc) {
        // check to see if this user is in the user password database.
        if (credentials.get(nc.getDefaultName()) == null) {
            LOG.warn("User '" + nc.getDefaultName() + "' not found in list of DIGEST-MD5 authenticateable users.");
            return;
        }
        nc.setName(nc.getDefaultName());
        userName = nc.getDefaultName();
    }

    private void handlePasswordCallback(PasswordCallback pc) {
        if ("super".equals(this.userName) && System.getProperty(SYSPROP_SUPER_PASSWORD) != null) {
            // superuser: use Java system property for password, if available. 超级管理员
            pc.setPassword(System.getProperty(SYSPROP_SUPER_PASSWORD).toCharArray());
        } else if (credentials.containsKey(userName) ) { // 设置password
            pc.setPassword(credentials.get(userName).toCharArray());
        } else {
            LOG.warn("No password found for user: " + userName);
        }
    }

    private void handleRealmCallback(RealmCallback rc) {
        LOG.debug("client supplied realm: " + rc.getDefaultText());
        rc.setText(rc.getDefaultText());
    }

    // 处理认证回调
    private void handleAuthorizeCallback(AuthorizeCallback ac) {
        String authenticationID = ac.getAuthenticationID();
        String authorizationID = ac.getAuthorizationID();

        LOG.info("Successfully authenticated client: authenticationID=" + authenticationID
                + ";  authorizationID=" + authorizationID + ".");
        ac.setAuthorized(true);

        // canonicalize authorization id according to system properties:
        // zookeeper.kerberos.removeRealmFromPrincipal(={true,false})
        // zookeeper.kerberos.removeHostFromPrincipal(={true,false})
        KerberosName kerberosName = new KerberosName(authenticationID);
        try {
            StringBuilder userNameBuilder = new StringBuilder(kerberosName.getShortName());
            if (shouldAppendHost(kerberosName)) {
                userNameBuilder.append("/").append(kerberosName.getHostName());
            }
            if (shouldAppendRealm(kerberosName)) {
                userNameBuilder.append("@").append(kerberosName.getRealm());
            }
            LOG.info("Setting authorizedID: " + userNameBuilder);
            ac.setAuthorizedID(userNameBuilder.toString());
        } catch (IOException e) {
            LOG.error("Failed to set name based on Kerberos authentication rules.", e);
        }
    }

    // 是否需要添加realm
    private boolean shouldAppendRealm(KerberosName kerberosName) {
        return !isSystemPropertyTrue(SYSPROP_REMOVE_REALM) && kerberosName.getRealm() != null;
    }

    // 是否需要添加host
    private boolean shouldAppendHost(KerberosName kerberosName) {
        return !isSystemPropertyTrue(SYSPROP_REMOVE_HOST) && kerberosName.getHostName() != null;
    }

    private boolean isSystemPropertyTrue(String propertyName) {
        return "true".equals(System.getProperty(propertyName));
    }
}
