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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.reflection.ExceptionUtil;

/**
 * ResultSet proxy to add logging
 * 
 * @author Clinton Begin
 * @author Eduardo Macarron
 * ResultSet日志增强代理类
 */
public final class ResultSetLogger extends BaseJdbcLogger implements InvocationHandler {

  private static Set<Integer> BLOB_TYPES = new HashSet<Integer>(); // 数据库中blob类型
  private boolean first = true; // 标识第一行，打印列名
  private int rows; // 结果行数
  private final ResultSet rs;
  private final Set<Integer> blobColumns = new HashSet<Integer>(); // 结果中blob的列序号（第几列）

  static {
    BLOB_TYPES.add(Types.BINARY);
    BLOB_TYPES.add(Types.BLOB);
    BLOB_TYPES.add(Types.CLOB);
    BLOB_TYPES.add(Types.LONGNVARCHAR);
    BLOB_TYPES.add(Types.LONGVARBINARY);
    BLOB_TYPES.add(Types.LONGVARCHAR);
    BLOB_TYPES.add(Types.NCLOB);
    BLOB_TYPES.add(Types.VARBINARY);
  }
  
  private ResultSetLogger(ResultSet rs, Log statementLog, int queryStack) {
    super(statementLog, queryStack);
    this.rs = rs;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      if (Object.class.equals(method.getDeclaringClass())) { // Object方法
        return method.invoke(this, params);
      }    
      Object o = method.invoke(rs, params);  // 执行method方法
      if ("next".equals(method.getName())) { // ResultSet的next方法
        if (((Boolean) o)) { // next方法返回true
          rows++; // 统计结果行数
          if (isTraceEnabled()) {
            ResultSetMetaData rsmd = rs.getMetaData();
            final int columnCount = rsmd.getColumnCount(); // 列数
            if (first) {
              first = false;
              printColumnHeaders(rsmd, columnCount); // 打印column列名
            }
            printColumnValues(columnCount); // 打印一行结果
          }
        } else { // 最后打印总行数
          debug("     Total: " + rows, false);
        }
      }
      clearColumnInfo(); // 清空列信息
      return o;
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  // 打印结果列名称
  private void printColumnHeaders(ResultSetMetaData rsmd, int columnCount) throws SQLException {
    StringBuilder row = new StringBuilder();
    row.append("   Columns: ");
    for (int i = 1; i <= columnCount; i++) {
      if (BLOB_TYPES.contains(rsmd.getColumnType(i))) { // 记录blob类型的列序号
        blobColumns.add(i);
      }
      String colname = rsmd.getColumnLabel(i);
      row.append(colname);
      if (i != columnCount) { // 不是最后一列
        row.append(", ");
      }
    }
    trace(row.toString(), false);
  }

  // 打印一行结果
  private void printColumnValues(int columnCount) {
    StringBuilder row = new StringBuilder();
    row.append("       Row: ");
    for (int i = 1; i <= columnCount; i++) {
      String colname;
      try {
        if (blobColumns.contains(i)) {
          colname = "<<BLOB>>";      // blob显示<<BLOB>>
        } else {
          colname = rs.getString(i); // 列值
        }
      } catch (SQLException e) {
        // generally can't call getString() on a BLOB column
        colname = "<<Cannot Display>>";
      }
      row.append(colname);
      if (i != columnCount) {
        row.append(", ");
      }
    }
    trace(row.toString(), false);
  }

  /*
   * Creates a logging version of a ResultSet
   * 创建ResultSet的代理类，增加处理日志功能
   * @param rs - the ResultSet to proxy
   * @return - the ResultSet with logging
   */
  public static ResultSet newInstance(ResultSet rs, Log statementLog, int queryStack) {
    InvocationHandler handler = new ResultSetLogger(rs, statementLog, queryStack);
    ClassLoader cl = ResultSet.class.getClassLoader();
    return (ResultSet) Proxy.newProxyInstance(cl, new Class[]{ResultSet.class}, handler);
  }

  /*
   * Get the wrapped result set
   *
   * @return the resultSet
   */
  public ResultSet getRs() {
    return rs;
  }

}
