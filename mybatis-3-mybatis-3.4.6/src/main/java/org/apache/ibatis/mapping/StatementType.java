/**
 *    Copyright ${license.git.copyrightYears} the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.mapping;

/**
 * @author Clinton Begin
 * 这会让MyBatis分别使用 Statement，PreparedStatement 或 CallableStatement，默认值：PREPARED。
 * Statement 对象用于将 SQL 语句发送到数据库中。实际上有三种 Statement 对象，它们都作为在给定连接上执行
 * SQL 语句的包容器：Statement、PreparedStatement（它从 Statement 继承而来）和 CallableStatement
 * （它从 PreparedStatement 继承而来）。它们都专用于发送特定类型的 SQL 语句： Statement 对象用于执行不
 * 带参数的简单SQL语句；PreparedStatement对象用于执行带或不带IN参数的预编译SQL语句；CallableStatement
 * 对象用于执行对数据库已存储过程的调用。
 */
public enum StatementType {
  STATEMENT, PREPARED, CALLABLE
}
