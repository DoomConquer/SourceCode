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
package org.apache.ibatis.scripting.xmltags;

import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 * set标签，继承TrimSqlNode
 */
public class SetSqlNode extends TrimSqlNode {

  private static List<String> suffixList = Arrays.asList(",");

  // 前缀prefix为SET，suffixesToOverride为,（去除结尾的逗号）
  public SetSqlNode(Configuration configuration,SqlNode contents) {
    super(configuration, contents, "SET", null, null, suffixList);
  }

}