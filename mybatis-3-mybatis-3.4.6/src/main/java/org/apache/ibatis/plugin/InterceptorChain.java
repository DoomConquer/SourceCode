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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Clinton Begin
 * 动态代理实现的责任链模式
 * （动态代理 + 责任链）
 */
public class InterceptorChain {

  private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

  // 对target进行包装，所有的拦截器都会调用wrap方法进行包装，有可能出现对代理类再进行代理情况，
  // 这样执行方法时就会形成链式执行interceptor的intercept方法
  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      // 一般interceptor的plugin方法会调用Plugin.wrap(target, this)方法，对target进行一层层的包装
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }

  // 获取所有的interceptor，这些interceptor不能被修改
  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}
