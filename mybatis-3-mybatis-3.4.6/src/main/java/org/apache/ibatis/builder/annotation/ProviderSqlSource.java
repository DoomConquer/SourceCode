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
package org.apache.ibatis.builder.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 * 提供执行动态sql的sqlSource
 * 例子：
 * @SelectProvider(type = UserSqlBuilder.class, method = "buildGetUsersByName")
 * List<User> getUsersByName(String name);
 *
 * class UserSqlBuilder {
 *  public static String buildGetUsersByName(final String name) {
 *    return new SQL(){{
 *      SELECT("*");
 *      FROM("users");
 *      if (name != null) {
 *        WHERE("name like #{value} || '%'");
 *      }
 *      ORDER_BY("id");
 *    }}.toString();
 *  }
 * }
 */
public class ProviderSqlSource implements SqlSource {

  private final Configuration configuration;
  private final SqlSourceBuilder sqlSourceParser;
  private final Class<?> providerType;             // SqlProvider类类型
  private Method providerMethod;                   // SqlProvider方法
  private String[] providerMethodArgumentNames;    // SqlProvider方法参数名称
  private Class<?>[] providerMethodParameterTypes; // SqlProvider方法参数类型
  private ProviderContext providerContext;         // 有了这个参数后，就能获取到接口和当前执行的方法信息
  private Integer providerContextIndex;            // 标记providerContext参数的顺序index

  /**
   * @deprecated Please use the {@link #ProviderSqlSource(Configuration, Object, Class, Method)} instead of this.
   */
  @Deprecated
  public ProviderSqlSource(Configuration configuration, Object provider) {
    this(configuration, provider, null, null);
  }

  /**
   * @since 3.4.5
   */
  public ProviderSqlSource(Configuration configuration, Object provider, Class<?> mapperType, Method mapperMethod) {
    String providerMethodName;
    try {
      this.configuration = configuration;
      this.sqlSourceParser = new SqlSourceBuilder(configuration);
      // provider为sqlProviderAnnotation（sql provider注解）
      this.providerType = (Class<?>) provider.getClass().getMethod("type").invoke(provider);
      providerMethodName = (String) provider.getClass().getMethod("method").invoke(provider); // 注解中method的值

      for (Method m : this.providerType.getMethods()) { // 获取SqlProvider的方法
        if (providerMethodName.equals(m.getName()) && CharSequence.class.isAssignableFrom(m.getReturnType())) { // 找到注解中的method方法
          if (providerMethod != null){ // SqlProvider指定的方法不唯一
            throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
                    + providerMethodName + "' is found multiple in SqlProvider '" + this.providerType.getName()
                    + "'. Sql provider method can not overload.");
          }
          this.providerMethod = m;
          this.providerMethodArgumentNames = new ParamNameResolver(configuration, m).getNames(); // 获取方法的参数名称
          this.providerMethodParameterTypes = m.getParameterTypes();
        }
      }
    } catch (BuilderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuilderException("Error creating SqlSource for SqlProvider.  Cause: " + e, e);
    }
    if (this.providerMethod == null) { // 未找到SqlProvider注解中指定的方法
      throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
          + providerMethodName + "' not found in SqlProvider '" + this.providerType.getName() + "'.");
    }
    for (int i = 0; i< this.providerMethodParameterTypes.length; i++) {
      Class<?> parameterType = this.providerMethodParameterTypes[i];
      // 如果method中有ProviderContext这个参数，可以使用这个参数获取SqlProvider上下文信息，但是传参时不用考虑这个参数，mybatis会自动处理
      // 例如上面例子中的buildGetUsersByName(final String name)方法，也可以写成buildGetUsersByName(final String name, ProviderContext providerContext)
      // 这样系统会自动处理providerContext参数，用户只用传入name参数即可
      if (parameterType == ProviderContext.class) {
        if (this.providerContext != null){
          throw new BuilderException("Error creating SqlSource for SqlProvider. ProviderContext found multiple in SqlProvider method ("
              + this.providerType.getName() + "." + providerMethod.getName()
              + "). ProviderContext can not define multiple in SqlProvider method argument.");
        }
        this.providerContext = new ProviderContext(mapperType, mapperMethod);
        this.providerContextIndex = i; // 记录ProviderContext参数的index
      }
    }
  }

  @Override
  // 参数parameterObject是执行sql语句时传进来的参数，会被mybatis处理成Object（如果多个参数放在map中）
  public BoundSql getBoundSql(Object parameterObject) {
    SqlSource sqlSource = createSqlSource(parameterObject);
    return sqlSource.getBoundSql(parameterObject);
  }

  // 创建数据源
  private SqlSource createSqlSource(Object parameterObject) {
    try {
      int bindParameterCount = providerMethodParameterTypes.length - (providerContext == null ? 0 : 1); // 排除providerContext参数
      String sql;
      if (providerMethodParameterTypes.length == 0) { // 无参数
        sql = invokeProviderMethod();
      } else if (bindParameterCount == 0) { // 只有一个providerContext参数
        sql = invokeProviderMethod(providerContext);
      } else if (bindParameterCount == 1 && // 两个参数（一个providerContext）
              (parameterObject == null || providerMethodParameterTypes[(providerContextIndex == null || providerContextIndex == 1) ? 0 : 1].isAssignableFrom(parameterObject.getClass()))) {
        sql = invokeProviderMethod(extractProviderMethodArguments(parameterObject));
      } else if (parameterObject instanceof Map) { // 参数为map
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) parameterObject;
        sql = invokeProviderMethod(extractProviderMethodArguments(params, providerMethodArgumentNames));
      } else { // 多个参数使用map
        throw new BuilderException("Error invoking SqlProvider method ("
                + providerType.getName() + "." + providerMethod.getName()
                + "). Cannot invoke a method that holds "
                + (bindParameterCount == 1 ? "named argument(@Param)": "multiple arguments")
                + " using a specifying parameterObject. In this case, please specify a 'java.util.Map' object.");
      }
      Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
      return sqlSourceParser.parse(replacePlaceholder(sql), parameterType, new HashMap<String, Object>());
    } catch (BuilderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuilderException("Error invoking SqlProvider method ("
          + providerType.getName() + "." + providerMethod.getName()
          + ").  Cause: " + e, e);
    }
  }

  // 将参数设置到Object数组中
  private Object[] extractProviderMethodArguments(Object parameterObject) {
    if (providerContext != null) { // 存在providerContext参数
      Object[] args = new Object[2];
      args[providerContextIndex == 0 ? 1 : 0] = parameterObject;
      args[providerContextIndex] = providerContext;
      return args;
    } else {
      return new Object[] { parameterObject };
    }
  }

  // 将参数设置到Object数组中
  private Object[] extractProviderMethodArguments(Map<String, Object> params, String[] argumentNames) {
    Object[] args = new Object[argumentNames.length];
    for (int i = 0; i < args.length; i++) {
      if (providerContextIndex != null && providerContextIndex == i) {
        args[i] = providerContext;
      } else {
        args[i] = params.get(argumentNames[i]);
      }
    }
    return args;
  }

  // 反射调用method方法，返回动态sql语句
  private String invokeProviderMethod(Object... args) throws Exception {
    Object targetObject = null;
    if (!Modifier.isStatic(providerMethod.getModifiers())) { // providerMethod是非静态方法，否则直接method.invoke执行（类方法）
      targetObject = providerType.newInstance();
    }
    // 执行注解提供的method方法，返回sql语句
    CharSequence sql = (CharSequence) providerMethod.invoke(targetObject, args); // 类方法，targetObject为null
    return sql != null ? sql.toString() : null;
  }

  // 替换占位符
  private String replacePlaceholder(String sql) {
    return PropertyParser.parse(sql, configuration.getVariables());
  }

}
