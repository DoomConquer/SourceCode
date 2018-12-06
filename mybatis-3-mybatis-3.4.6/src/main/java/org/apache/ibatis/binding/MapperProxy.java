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
package org.apache.ibatis.binding;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.ibatis.lang.UsesJava7;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private final SqlSession sqlSession;
  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache;

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface; // mapper接口类型
    this.methodCache = methodCache;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if (Object.class.equals(method.getDeclaringClass())) { // 如果是Object的方法，hashcode、equals、toString等
        return method.invoke(this, args);
      } else if (isDefaultMethod(method)) { // 如果是接口的默认方法
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args); // 执行mapper方法
  }

  // 从缓存中获取封装的MapperMethod，如果没有生成method放到缓存中
  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
      mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }

  // 使用java7的api执行接口的默认方法，通过MethodHandles反射（反射字节码，和Method方式不同）执行
  @UsesJava7
  private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
      throws Throwable {
    final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
        .getDeclaredConstructor(Class.class, int.class);
    if (!constructor.isAccessible()) {
      constructor.setAccessible(true);
    }
    final Class<?> declaringClass = method.getDeclaringClass();
    // unreflectSpecial方法返回一个MethodHandle（对应method方法的句柄），说明：Produces a method handle for a reflected method.
    // 这种方式是通过反射获取MethodHandles.Lookup，因为method对应的是接口，所以通过构造函数方式实例化Lookup，如果是一个类，可以通过
    // MethodHandles.lookup()方式获取，但是接口中的默认方法是不能这样获取。这时invoke方法执行的是接口中的默认方法，与bindTo的参数proxy没有关系，
    // proxy仅仅提供类型让MethodHandle判断是否与unreflectSpecial或findSpecial中提供的类型相匹配。如果是一个类，且用MethodHandles.lookup()
    // 方式获取的lookup，那么invoke调用时会调用bindTo的参数实例对应的方法。
    // bindTo方法，绑定方法句柄的第一个参数（方法接受者） Binds a value to the first argument of a method handle, without invoking it.
    return constructor
        .newInstance(declaringClass,
            MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
        .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
  }

  /**
   * Backport of java.lang.reflect.Method#isDefault()
   * 是否是接口的默认方法
   */
  private boolean isDefaultMethod(Method method) {
    // A default method is a public non-abstract instance method, that
    // is, a non-static method with a body, declared in an interface type.
    // 这是Method.isDefault()方法的解释，默认方法是一个在接口中定义的public、非抽象、非静态的方法。
    // 可以直接return method.isDefault();isDefault是java8版本以上方法
    return (method.getModifiers()
        & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC
        && method.getDeclaringClass().isInterface();
  }
}
