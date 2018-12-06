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
package org.apache.ibatis.plugin;

import java.util.Properties;

/**
 * @author Clinton Begin
 *  MyBatis 允许你在已映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法调用包括：
 *
 *  Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
 *  ParameterHandler (getParameterObject, setParameters)
 *  ResultSetHandler (handleResultSets, handleOutputParameters)
 *  StatementHandler (prepare, parameterize, batch, update, query)
 *  这些类中方法的细节可以通过查看每个方法的签名来发现，或者直接查看 MyBatis 发行包中的源代码。
 *  如果你想做的不仅仅是监控方法的调用，那么你最好相当了解要重写的方法的行为。
 *  因为如果在试图修改或重写已有方法的行为的时候，你很可能在破坏 MyBatis 的核心模块。
 *  这些都是更低层的类和方法，所以使用插件的时候要特别当心。
 *
 *  示例：
 *  @Intercepts({@Signature(
 *  type= Executor.class,
 *  method = "update",
 *  args = {MappedStatement.class,Object.class})})
 *  public class ExamplePlugin implements Interceptor {
 *    public Object intercept(Invocation invocation) throws Throwable {
 *      return invocation.proceed();
 *    }
 *    public Object plugin(Object target) {
 *      return Plugin.wrap(target, this);
 *    }
 *    public void setProperties(Properties properties) {
 *    }
 *  }
 *  在plugin的intercept方法中需要执行invocation.proceed()调用被代理类的method方法，在plugin方法中执行Plugin.wrap(target, this)将
 *  target包装，返回target的代理类，如果还有其他plugin，会形成链式代理
 */
public interface Interceptor {

  Object intercept(Invocation invocation) throws Throwable;

  Object plugin(Object target);

  void setProperties(Properties properties);

}
