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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * @author Clinton Begin
 * 标识resultMap中一条映射，association、collection、constructor都会保存成一个ResultMapping，其中包含的子元素会单独生成一个resultMap
 * 将id保存在ResultMapping的nestedResultMapId字段中
 */
public class ResultMapping {
  /*
  下面的例子显示了column有多列且与嵌套查询中的查询参数对应，其中属性对应的是嵌套查询中的参数，值对应的是父查询中的字段。column中的每一个数据会
  生成一个ResultMapping保存在composites中。column形如：column="{prop1=col1,prop2=col2}"，这种情况只针对嵌套查询
    <resultMap id="blogResult" type="Blog">
      <association property="author" column="{id=author_id,likename=author_name}" javaType="Author" select="selectAuthor"/>
    </resultMap>

    <select id="selectBlog" resultMap="blogResult" parameterType="java.lang.String">
      SELECT author_id,author_name FROM BLOG WHERE ID = #{id}
    </select>

    <select id="selectAuthor" resultType="Author" parameterType="java.util.HashMap">
      SELECT * FROM AUTHOR WHERE 1=1
      <if test="id != null and id != '' ">
         and ID = #{id}
      </if>
      <if test="likename != null and likename != '' ">
         and name like CONCAT('%',#{likename},'%')
      </if>
    </select>
  */

  private Configuration configuration;
  private String property;            // resultMap对应的property
  private String column;              // resultMap对应的column，可以包含多个列，用,隔开
  private Class<?> javaType;          // resultMap对应的javaType
  private JdbcType jdbcType;
  private TypeHandler<?> typeHandler;
  private String nestedResultMapId;   // 当前ResultMapping嵌套的ResultMap，一般由resultMap="xxx"指明
  private String nestedQueryId;       // 当前ResultMapping嵌套的查询，一般由select="xxx"指明
  // notNullColumn属性值，默认情况下，子对象仅在至少一个列映射到其属性非空时才创建。通过对这个属性指
  // 定非空的列将改变默认行为，这样做之后Mybatis将仅在这些列非空时才创建一个子对象。 可以指定多个列名，
  // 使用逗号分隔。默认值：未设置(unset)。
  private Set<String> notNullColumns;
  private String columnPrefix;        // column前缀，当association的两个resultMap是同一类型时，可以使用columnPrefix来进行区分
  private List<ResultFlag> flags;     // 如果该映射包含id、idArg、constructor元素
  private List<ResultMapping> composites; // 处理复合column，column包含多列
  // 对象属性值来自于多余的结果集对象中，这种来源的前提是allowMultiQueries=true允许多sql查询。还需要<select resultSets="rs1,rs2">给
  // 每个结果集设置一个名称。在属性映射中配置resultSet属性引用结果集名称（如：<association resultSet="rs2">）
  private String resultSet;           // xml中resultSet属性
  // xml中foreignColumn属性，可以包含多列，用,隔开。必须和column中的列对应。foreignColumn对应的是嵌套结果里面（结果集）的字段，
  // column对应的是父resultMap结果集中的字段。总结起来就是foreignColumn是在多结果集时指定resultSet结果集中关联的字段。
  private String foreignColumn;
  private boolean lazy;               // 是否懒加载，fetchType = lazy

  ResultMapping() {
  }

  public static class Builder {
    private ResultMapping resultMapping = new ResultMapping();

    public Builder(Configuration configuration, String property, String column, TypeHandler<?> typeHandler) {
      this(configuration, property);
      resultMapping.column = column;
      resultMapping.typeHandler = typeHandler;
    }

    public Builder(Configuration configuration, String property, String column, Class<?> javaType) {
      this(configuration, property);
      resultMapping.column = column;
      resultMapping.javaType = javaType;
    }

    public Builder(Configuration configuration, String property) {
      resultMapping.configuration = configuration;
      resultMapping.property = property;
      resultMapping.flags = new ArrayList<ResultFlag>();
      resultMapping.composites = new ArrayList<ResultMapping>();
      resultMapping.lazy = configuration.isLazyLoadingEnabled();
    }

    public Builder javaType(Class<?> javaType) {
      resultMapping.javaType = javaType;
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      resultMapping.jdbcType = jdbcType;
      return this;
    }

    public Builder nestedResultMapId(String nestedResultMapId) {
      resultMapping.nestedResultMapId = nestedResultMapId;
      return this;
    }

    public Builder nestedQueryId(String nestedQueryId) {
      resultMapping.nestedQueryId = nestedQueryId;
      return this;
    }

    public Builder resultSet(String resultSet) {
      resultMapping.resultSet = resultSet;
      return this;
    }

    public Builder foreignColumn(String foreignColumn) {
      resultMapping.foreignColumn = foreignColumn;
      return this;
    }

    public Builder notNullColumns(Set<String> notNullColumns) {
      resultMapping.notNullColumns = notNullColumns;
      return this;
    }

    public Builder columnPrefix(String columnPrefix) {
      resultMapping.columnPrefix = columnPrefix;
      return this;
    }

    public Builder flags(List<ResultFlag> flags) {
      resultMapping.flags = flags;
      return this;
    }

    public Builder typeHandler(TypeHandler<?> typeHandler) {
      resultMapping.typeHandler = typeHandler;
      return this;
    }

    public Builder composites(List<ResultMapping> composites) {
      resultMapping.composites = composites;
      return this;
    }

    public Builder lazy(boolean lazy) {
      resultMapping.lazy = lazy;
      return this;
    }
    
    public ResultMapping build() {
      // lock down collections
      resultMapping.flags = Collections.unmodifiableList(resultMapping.flags);
      resultMapping.composites = Collections.unmodifiableList(resultMapping.composites);
      resolveTypeHandler();
      validate(); // 验证
      return resultMapping;
    }

    private void validate() {
      // Issue #697: cannot define both nestedQueryId and nestedResultMapId
      // 嵌套查询和嵌套结果不能同时指定
      if (resultMapping.nestedQueryId != null && resultMapping.nestedResultMapId != null) {
        throw new IllegalStateException("Cannot define both nestedQueryId and nestedResultMapId in property " + resultMapping.property);
      }
      // Issue #5: there should be no mappings without typehandler
      // 非嵌套结果或非嵌套查询必须有相应的TypeHandler
      if (resultMapping.nestedQueryId == null && resultMapping.nestedResultMapId == null && resultMapping.typeHandler == null) {
        throw new IllegalStateException("No typehandler found for property " + resultMapping.property);
      }
      // Issue #4 and GH #39: column is optional only in nested resultmaps but not in the rest
      // column只有在嵌套结果时才是可选的，其他时候是必须的
      if (resultMapping.nestedResultMapId == null && resultMapping.column == null && resultMapping.composites.isEmpty()) {
        throw new IllegalStateException("Mapping is missing column attribute for property " + resultMapping.property);
      }
      // column和foreignColumn的列数应该相等
      if (resultMapping.getResultSet() != null) {
        int numColumns = 0;
        if (resultMapping.column != null) {
          numColumns = resultMapping.column.split(",").length;
        }
        int numForeignColumns = 0;
        if (resultMapping.foreignColumn != null) {
          numForeignColumns = resultMapping.foreignColumn.split(",").length;
        }
        if (numColumns != numForeignColumns) {
          throw new IllegalStateException("There should be the same number of columns and foreignColumns in property " + resultMapping.property);
        }
      }
    }

    // 根据javaType和jdbcType获取对应的TypeHandler
    private void resolveTypeHandler() {
      if (resultMapping.typeHandler == null && resultMapping.javaType != null) {
        Configuration configuration = resultMapping.configuration;
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, resultMapping.jdbcType);
      }
    }

    public Builder column(String column) {
      resultMapping.column = column;
      return this;
    }
  }

  public String getProperty() {
    return property;
  }

  public String getColumn() {
    return column;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public JdbcType getJdbcType() {
    return jdbcType;
  }

  public TypeHandler<?> getTypeHandler() {
    return typeHandler;
  }

  public String getNestedResultMapId() {
    return nestedResultMapId;
  }

  public String getNestedQueryId() {
    return nestedQueryId;
  }

  public Set<String> getNotNullColumns() {
    return notNullColumns;
  }

  public String getColumnPrefix() {
    return columnPrefix;
  }

  public List<ResultFlag> getFlags() {
    return flags;
  }

  public List<ResultMapping> getComposites() {
    return composites;
  }

  public boolean isCompositeResult() {
    return this.composites != null && !this.composites.isEmpty();
  }

  public String getResultSet() {
    return this.resultSet;
  }

  public String getForeignColumn() {
    return foreignColumn;
  }

  public void setForeignColumn(String foreignColumn) {
    this.foreignColumn = foreignColumn;
  }

  public boolean isLazy() {
    return lazy;
  }

  public void setLazy(boolean lazy) {
    this.lazy = lazy;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ResultMapping that = (ResultMapping) o;

    if (property == null || !property.equals(that.property)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    if (property != null) {
      return property.hashCode();
    } else if (column != null) {
      return column.hashCode();
    } else {
      return 0;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ResultMapping{");
    //sb.append("configuration=").append(configuration); // configuration doesn't have a useful .toString()
    sb.append("property='").append(property).append('\'');
    sb.append(", column='").append(column).append('\'');
    sb.append(", javaType=").append(javaType);
    sb.append(", jdbcType=").append(jdbcType);
    //sb.append(", typeHandler=").append(typeHandler); // typeHandler also doesn't have a useful .toString()
    sb.append(", nestedResultMapId='").append(nestedResultMapId).append('\'');
    sb.append(", nestedQueryId='").append(nestedQueryId).append('\'');
    sb.append(", notNullColumns=").append(notNullColumns);
    sb.append(", columnPrefix='").append(columnPrefix).append('\'');
    sb.append(", flags=").append(flags);
    sb.append(", composites=").append(composites);
    sb.append(", resultSet='").append(resultSet).append('\'');
    sb.append(", foreignColumn='").append(foreignColumn).append('\'');
    sb.append(", lazy=").append(lazy);
    sb.append('}');
    return sb.toString();
  }

}
