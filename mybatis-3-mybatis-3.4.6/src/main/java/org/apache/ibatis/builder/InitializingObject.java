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
package org.apache.ibatis.builder;

/**
 * Interface that indicate to provide a initialization method.
 *
 * @since 3.4.2
 * @author Kazuki Shimizu
 * 从3.4.2版本开始，MyBatis已经支持在所有属性设置完毕以后可以调用一个初始化方法。如果你想要使用这个特性，
 * 请在你的自定义缓存类里实现 org.apache.ibatis.builder.InitializingObject 接口。
 */
public interface InitializingObject {

  /**
   * Initialize a instance.
   * <p>
   * This method will be invoked after it has set all properties.
   * </p>
   * @throws Exception in the event of misconfiguration (such as failure to set an essential property) or if initialization fails
   */
  void initialize() throws Exception;

}