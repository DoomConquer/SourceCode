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
package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;

/**
 * @author Clinton Begin
 * 接口ObjectWrapperFactory的默认实现，因为使用的地方会根据类型生成具体的Wrapper，所以这里hasWrapperFor返回false，该接口主要可以满足用户
 * 自己实现的Wrapper，通过getWrapperFor获取ObjectWrapper。例如中的代码：
 *
 * if (object instanceof ObjectWrapper) {
 *   this.objectWrapper = (ObjectWrapper) object;
 * } else if (objectWrapperFactory.hasWrapperFor(object)) {
 *   this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
 * } else if (object instanceof Map) {
 *   this.objectWrapper = new MapWrapper(this, (Map) object);
 * } else if (object instanceof Collection) {
 *   this.objectWrapper = new CollectionWrapper(this, (Collection) object);
 * } else {
 *   this.objectWrapper = new BeanWrapper(this, object);
 * }
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {

  @Override
  public boolean hasWrapperFor(Object object) {
    return false;
  }

  @Override
  public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
    throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
  }

}
