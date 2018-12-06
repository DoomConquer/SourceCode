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
package org.apache.ibatis.logging.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.reflection.ExceptionUtil;

/**
 * Statement proxy to add logging
 * 
 * @author Clinton Begin
 * @author Eduardo Macarron
 * Statement日志增强处理类
 *
 */

public final class StatementLogger extends BaseJdbcLogger implements InvocationHandler {

  private final Statement statement;

  private StatementLogger(Statement stmt, Log statementLog, int queryStack) {
    super(statementLog, queryStack);
    this.statement = stmt;
  }

  // 拦截方法处理
  @Override
  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      if (Object.class.equals(method.getDeclaringClass())) { // Object的方法
        return method.invoke(this, params);
      }    
      if (EXECUTE_METHODS.contains(method.getName())) { // 执行sql的方法（execute、executeUpdate、executeQuery、addBatch）
        if (isDebugEnabled()) {
          debug(" Executing: " + removeBreakingWhitespace((String) params[0]), true);
        }
        if ("executeQuery".equals(method.getName())) { // executeQuery方法
          ResultSet rs = (ResultSet) method.invoke(statement, params);
          return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
        } else {
          return method.invoke(statement, params);
        }
      } else if ("getResultSet".equals(method.getName())) { // getResultSet方法
        ResultSet rs = (ResultSet) method.invoke(statement, params);
        return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
      } else { // 其他方法
        return method.invoke(statement, params);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  /*
   * Creates a logging version of a Statement
   * 创建Statement代理类，增加处理日志功能
   *
   * @param stmt - the statement
   * @return - the proxy
   */
  public static Statement newInstance(Statement stmt, Log statementLog, int queryStack) {
    InvocationHandler handler = new StatementLogger(stmt, statementLog, queryStack);
    ClassLoader cl = Statement.class.getClassLoader();
    return (Statement) Proxy.newProxyInstance(cl, new Class[]{Statement.class}, handler);
  }

  /*
   * return the wrapped statement
   *
   * @return the statement
   */
  public Statement getStatement() {
    return statement;
  }

}
