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

package org.apache.zookeeper.server.util;

// zxid处理方法
public class ZxidUtils {
	// 获取epoch，zxid的前32位
	static public long getEpochFromZxid(long zxid) {
		return zxid >> 32L;
	}
	// 获取zxid的后32位
	static public long getCounterFromZxid(long zxid) {
		return zxid & 0xffffffffL;
	}
	// 生成zxid，前32位为epoch，后32位为计数
	static public long makeZxid(long epoch, long counter) {
		return (epoch << 32L) | (counter & 0xffffffffL);
	}
	// 返回zxid的十六进制字符串
	static public String zxidToString(long zxid) {
		return Long.toHexString(zxid);
	}
}
