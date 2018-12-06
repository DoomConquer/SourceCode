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

import java.util.Collections;
import java.util.Map;

import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 *
 * 鉴别器，鉴别器非常容易理解，因为它的表现很像Java语言中的switch语句。
 */
public class Discriminator {

  private ResultMapping resultMapping; // 鉴别器映射，如<discriminator javaType="int" column="vehicle_type">
  // 鉴别器关系映射。discriminatorMap保存的是case中的value和resultMap关系，如<case value="1" resultMap="carResult"/>
  // key为value，value为namespace + resultMap
  private Map<String, String> discriminatorMap;

  Discriminator() {

}
  public static class Builder {
    private Discriminator discriminator = new Discriminator();

    public Builder(Configuration configuration, ResultMapping resultMapping, Map<String, String> discriminatorMap) {
      discriminator.resultMapping = resultMapping;
      discriminator.discriminatorMap = discriminatorMap;
    }

    public Discriminator build() {
      assert discriminator.resultMapping != null;
      assert discriminator.discriminatorMap != null;
      assert !discriminator.discriminatorMap.isEmpty();
      //lock down map 设置为不可修改的map
      discriminator.discriminatorMap = Collections.unmodifiableMap(discriminator.discriminatorMap);
      return discriminator;
    }
  }

  public ResultMapping getResultMapping() {
    return resultMapping;
  }

  public Map<String, String> getDiscriminatorMap() {
    return discriminatorMap;
  }

  // s为case元素中的value值，获取对应的resultMapId
  public String getMapIdFor(String s) {
    return discriminatorMap.get(s);
  }

}
