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

/**
 * @author Clinton Begin
 * 存储过程有三种类型的参数，分别为 IN（输入参数），OUT（输出参数），INOUT（输入输出参数）。
 * 一个存储过程，可以有多个 IN 参数，至多有一个 OUT 或 INOUT 参数。
 * 1)  如果仅仅想把数据传给存储过程，那就用in类型参数；
 * 2)  如果仅仅从存储过程返回值，那就用out类型参数；
 * 3)  如果需要把数据传给存储过程经过计算再传回给我们，那就用inout类型参数。
 */
public enum ParameterMode {
  IN, OUT, INOUT
}
