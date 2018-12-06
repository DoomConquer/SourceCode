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
package org.apache.ibatis.executor.resultset;

import org.apache.ibatis.annotations.AutomapConstructor;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.cursor.defaults.DefaultCursor;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.loader.ResultLoader;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Constructor;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Iwao AVE!
 * @author Kazuki Shimizu
 */
public class DefaultResultSetHandler implements ResultSetHandler {

  private static final Object DEFERED = new Object(); // 标识使用延迟加载的返回

  private final Executor executor;
  private final Configuration configuration;
  private final MappedStatement mappedStatement;
  private final RowBounds rowBounds;
  private final ParameterHandler parameterHandler;
  private final ResultHandler<?> resultHandler;
  private final BoundSql boundSql;
  private final TypeHandlerRegistry typeHandlerRegistry;
  private final ObjectFactory objectFactory;
  private final ReflectorFactory reflectorFactory;

  // nested resultMaps
  private final Map<CacheKey, Object> nestedResultObjects = new HashMap<CacheKey, Object>(); // 保存嵌套结果
  private final Map<String, Object> ancestorObjects = new HashMap<String, Object>(); // 处理resultMap循环引用，用于记录上层的resultMap
  private Object previousRowValue;

  // multiple resultSets
  // 记录ResultMapping中resultSet设置对应的名称和ResultMapping，key为ResultMapping中resultSet名称，value为该ResultMapping
  private final Map<String, ResultMapping> nextResultMaps = new HashMap<String, ResultMapping>();
  // pendingRelations中保存了父resultMap中一条记录（cacheKey）对应的嵌套结果，例如父resultMap是一个对象（对应一个cacheKey），对象中
  // 包含了list<对象>，这里的List<PendingRelation>就保存着list<对象>的关系
  private final Map<CacheKey, List<PendingRelation>> pendingRelations = new HashMap<CacheKey, List<PendingRelation>>();

  // Cached AutoMappings
  private final Map<String, List<UnMappedColumnAutoMapping>> autoMappingsCache = new HashMap<String, List<UnMappedColumnAutoMapping>>();

  // temporary marking flag that indicate using constructor mapping (use field to reduce memory usage)
  // 标识是否使用constructor
  private boolean useConstructorMappings;

  private final PrimitiveTypes primitiveTypes; // java基本类型

  // 保存父ResultMapping和结果对象到pendingRelations
  private static class PendingRelation {
    public MetaObject metaObject;
    public ResultMapping propertyMapping;
  }

  // 未映射的列（静态内部类，相当于提供一个数据结构供外部类使用）
  private static class UnMappedColumnAutoMapping {
    private final String column;
    private final String property;
    private final TypeHandler<?> typeHandler;
    private final boolean primitive;

    public UnMappedColumnAutoMapping(String column, String property, TypeHandler<?> typeHandler, boolean primitive) {
      this.column = column;
      this.property = property;
      this.typeHandler = typeHandler;
      this.primitive = primitive;
    }
  }

  public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler<?> resultHandler, BoundSql boundSql,
                                 RowBounds rowBounds) {
    this.executor = executor;
    this.configuration = mappedStatement.getConfiguration();
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;
    this.parameterHandler = parameterHandler;
    this.boundSql = boundSql;
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();
    this.reflectorFactory = configuration.getReflectorFactory();
    this.resultHandler = resultHandler;
    this.primitiveTypes = new PrimitiveTypes();
  }

  // HANDLE OUTPUT PARAMETER
  // 处理存储过程输出参数
  @Override
  public void handleOutputParameters(CallableStatement cs) throws SQLException {
    final Object parameterObject = parameterHandler.getParameterObject(); // 入参
    final MetaObject metaParam = configuration.newMetaObject(parameterObject);
    final List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    for (int i = 0; i < parameterMappings.size(); i++) {
      final ParameterMapping parameterMapping = parameterMappings.get(i);
      if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
        if (ResultSet.class.equals(parameterMapping.getJavaType())) { // 出参类型是ResultSet
          // 处理存储过程输出参数
          handleRefCursorOutputParameter((ResultSet) cs.getObject(i + 1), parameterMapping, metaParam);
        } else { // 否则直接从resultSet取值设置到参数metaParam上
          final TypeHandler<?> typeHandler = parameterMapping.getTypeHandler();
          metaParam.setValue(parameterMapping.getProperty(), typeHandler.getResult(cs, i + 1)); // 通过反射从callableStatement中获取结果设置到入参上
        }
      }
    }
  }

  // 存储过程出参是ResultSet，处理成游标
  private void handleRefCursorOutputParameter(ResultSet rs, ParameterMapping parameterMapping, MetaObject metaParam) throws SQLException {
    if (rs == null) {
      return;
    }
    try {
      final String resultMapId = parameterMapping.getResultMapId(); // resultMapId即xml中的resultMap="xxx"
      final ResultMap resultMap = configuration.getResultMap(resultMapId); // 从configuration中取出ResultMap
      final ResultSetWrapper rsw = new ResultSetWrapper(rs, configuration);
      if (this.resultHandler == null) { // 不存在resultHandler，使用默认的DefaultResultHandler
        final DefaultResultHandler resultHandler = new DefaultResultHandler(objectFactory);
        handleRowValues(rsw, resultMap, resultHandler, new RowBounds(), null);
        metaParam.setValue(parameterMapping.getProperty(), resultHandler.getResultList()); // 设置参数属性，没有resultHandler默认结果为list
      } else {
        handleRowValues(rsw, resultMap, resultHandler, new RowBounds(), null);
      }
    } finally {
      // issue #228 (close resultsets)
      closeResultSet(rs);
    }
  }

  /*
    为了方便理解如何处理多结果集，假设有下面存储过程：
    SELECT * FROM BLOG WHERE ID = #{id}
    SELECT * FROM POST WHERE BLOG_ID = #{id}

    上面有两个查询结果，我们也必须给定一个指定的名称给对应的结果集合，做法是：增加一个“resultSets”属性，并使用逗号作为间隔。如下：

    <select id="selectBlog" resultSets="blogs,posts" resultMap="blogResult">
      {call getBlogsAndPosts(#{id,jdbcType=INTEGER,mode=IN})}
    </select>

   同样，我们也指定：数据填充的“posts”的集合包含在“posts”的结果集中
    <resultMap id="blogResult" type="Blog">
      <id property="id" column="id" />
      <result property="title" column="title"/>
      <collection property="posts" ofType="Post" resultSet="posts" column="id" foreignColumn="blog_id">
        <id property="id" column="blog_id"/>
        <result property="subject" column="subject"/>
        <result property="body" column="body"/>
      </collection>
    </resultMap>

    该查询中共有一个resultMap和两个resultSet，分别是blogResult、blogs和posts，mybatis会根据结果集首先映射到resultMap上，将剩下的映射到
    collection的resultSet上，即两个结果集，第一个结果集映射到blogResult，第二个结果集映射到posts上，resultSet blogs会被跳过，或者可以看
    成它已经被映射到resultMap blogResult上了。同理如果有多个resultMap，那么会先将结果集映射到resultMap上，剩下的从resultSet[resultMapCount]
    开始映射（即下面代码中用resultSetCount来计数）。注：这里的foreignColumn指向第二个结果集中的blog_id字段，column指向第一个结果集中的id字段，
    这样将blog_id=id的结果放在Blog对象中的list<Post>中
   */
  // HANDLE RESULT SETS
  // 处理ResultSets多结果集（大部分情况只有一个ResultSet），返回结果list
  @Override
  public List<Object> handleResultSets(Statement stmt) throws SQLException {
    ErrorContext.instance().activity("handling results").object(mappedStatement.getId());

    final List<Object> multipleResults = new ArrayList<Object>();

    int resultSetCount = 0; // 记录resultSet数量
    ResultSetWrapper rsw = getFirstResultSet(stmt); // 获取第一个ResultSet

    List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    int resultMapCount = resultMaps.size();
    validateResultMapsCount(rsw, resultMapCount); // 校验resultMap数量
    while (rsw != null && resultMapCount > resultSetCount) { // 获取mappedStatement中的ResultMap进行处理
      ResultMap resultMap = resultMaps.get(resultSetCount);
      handleResultSet(rsw, resultMap, multipleResults, null); // resultSet和resultMap怎么对应？（根据顺序位置）
      rsw = getNextResultSet(stmt); // 获取下一个resultSet
      cleanUpAfterHandlingResultSet(); // 处理完ResultSet清空nestedResultObjects
      resultSetCount++;
    }

    String[] resultSets = mappedStatement.getResultSets(); // 获取mappedStatement配置中resultSets
    if (resultSets != null) {
      // 从第resultSetCount个resultSet开始，将resultSet映射到association或collection的resultSet属性上，即上面例子中的posts
      while (rsw != null && resultSetCount < resultSets.length) {
        // nextResultMaps记录了ResultMapping中的resultSets，在上面处理resultMap时已经将包含resultSet的ResultMapping保存在
        // nextResultMaps中了，当时只保存了resultSet（String）-> ResultMapping没有处理，现在处理这个ResultMapping
        ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
        if (parentMapping != null) {
          String nestedResultMapId = parentMapping.getNestedResultMapId();
          // 该resultMap是嵌套结果对应的resultMap，例如association、collection、constructor等对应的resultMap
          ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
          // 和上面处理resultMap过程一样，但是该过程不用保存结果到multipleResults，因为该结果已经包含在上面的resultMap中，只要设置
          // 到上面的resultMap即可。这里的parentMapping不为空，为即将处理的resultMap的父ResultMapping，即上面例子中的collection，
          //（collection会被处理成一个ResultMapping，其下面的子元素会生成一个嵌套的resultMap，注：上面association中没有resultMap="xxx"
          // mybatis会根据其它属性生成一个resultMap，并将resultMapId保存在ResultMapping的nestedResultMapId字段）
          handleResultSet(rsw, resultMap, null, parentMapping);
        }
        rsw = getNextResultSet(stmt);
        cleanUpAfterHandlingResultSet();
        resultSetCount++;
      }
    }

    return collapseSingleResultList(multipleResults); // 如果只有一个结果，展开来不用再套一层list
  }

  // 处理游标ResultSet结果，直接从返回游标
  @Override
  public <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException {
    ErrorContext.instance().activity("handling cursor results").object(mappedStatement.getId());

    ResultSetWrapper rsw = getFirstResultSet(stmt);

    List<ResultMap> resultMaps = mappedStatement.getResultMaps();

    int resultMapCount = resultMaps.size();
    validateResultMapsCount(rsw, resultMapCount);
    if (resultMapCount != 1) { // Cursor结果不能被映射到多个resultMap上
      throw new ExecutorException("Cursor results cannot be mapped to multiple resultMaps");
    }

    ResultMap resultMap = resultMaps.get(0); // 返回DefaultCursor，到使用的时候才获取结果
    return new DefaultCursor<E>(this, resultMap, rsw, rowBounds); // 返回游标结果
  }

  // 获取第一个ResultSet，通过ResultSetWrapper包装返回
  private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
    ResultSet rs = stmt.getResultSet(); // getResultSet方法对于每个结果集只能调用一次
    while (rs == null) { // 获取结果中第一个ResultSet（针对一些数据库不返回第一个resultSet，移到第一个resultSet）
      // move forward to get the first resultset in case the driver
      // doesn't return the resultset as the first result (HSQLDB 2.1)
      if (stmt.getMoreResults()) {
        rs = stmt.getResultSet();
      } else {
        if (stmt.getUpdateCount() == -1) {
          // no more results. Must be no resultset
          break;
        }
      }
    }
    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
  }

  // 获取下一个ResultSet，通过ResultSetWrapper包装返回
  private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
    // Making this method tolerant of bad JDBC drivers
    try {
      if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) { // 数据库支持多结果集（MultipleResultSets）
        // Crazy Standard JDBC way of determining if there are more results
        if (!(!stmt.getMoreResults() && stmt.getUpdateCount() == -1)) {
          ResultSet rs = stmt.getResultSet();
            if (rs == null) {
            return getNextResultSet(stmt);
          } else {
            return new ResultSetWrapper(rs, configuration);
          }
        }
      }
    } catch (Exception e) {
      // Intentionally ignored.
    }
    return null;
  }

  // 关闭ResultSet
  private void closeResultSet(ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException e) {
      // ignore
    }
  }

  // 处理完ResultSet清空nestedResultObjects
  private void cleanUpAfterHandlingResultSet() {
    nestedResultObjects.clear();
  }

  // 校验resultMap数量（可能未指定resultType或resultMap）
  private void validateResultMapsCount(ResultSetWrapper rsw, int resultMapCount) {
    if (rsw != null && resultMapCount < 1) {
      throw new ExecutorException("A query was run and no Result Maps were found for the Mapped Statement '" + mappedStatement.getId()
          + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
    }
  }

  // 处理单个ResultSet结果
  private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
    try {
      if (parentMapping != null) { // 针对多结果集resultSet情况
        handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
      } else {
        if (resultHandler == null) { // resultHandler为空，使用默认的ResultHandler，并且保存结果
          DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
          handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
          multipleResults.add(defaultResultHandler.getResultList()); // 从ResultHandler中获取结果添加到multipleResults
        } else {
          handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
        }
      }
    } finally {
      // issue #228 (close resultsets)
      closeResultSet(rsw.getResultSet());
    }
  }

  // 处理单个元素结果
  @SuppressWarnings("unchecked")
  private List<Object> collapseSingleResultList(List<Object> multipleResults) {
    return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
  }

  // HANDLE ROWS FOR SIMPLE RESULTMAP
  // 处理单一ResultMap结果
  public void handleRowValues(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
    if (resultMap.hasNestedResultMaps()) { // 存在嵌套结果
      ensureNoRowBounds();  // 确保RowBounds没有越界
      checkResultHandler(); // 检查resultHandler
      // 处理嵌套结果的ResultMap
      handleRowValuesForNestedResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
    } else {
      // 处理非嵌套结果ResultMap
      handleRowValuesForSimpleResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
    }
  }

  // 确保RowBounds没有越界，对于嵌套结果映射不能使用rowBounds限制（默认的RowBounds除外），可以通过设置safeRowBoundsEnabled=false跳过该检查
  private void ensureNoRowBounds() {
    if (configuration.isSafeRowBoundsEnabled() && rowBounds != null && (rowBounds.getLimit() < RowBounds.NO_ROW_LIMIT || rowBounds.getOffset() > RowBounds.NO_ROW_OFFSET)) {
      throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely constrained by RowBounds. "
          + "Use safeRowBoundsEnabled=false setting to bypass this check.");
    }
  }

  // 检查resultHandler
  // resultOrdered，这个设置仅针对嵌套结果select语句适用：如果为true，就是假设包含了嵌套结果集或是分组了，这样的话当返回一个主结果行的时候，
  // 就不会发生有对前面结果集的引用的情况。这就使得在获取嵌套的结果集的时候不至于导致内存不够用。默认值：false。
  protected void checkResultHandler() {
    if (resultHandler != null && configuration.isSafeResultHandlerEnabled() && !mappedStatement.isResultOrdered()) {
      throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely used with a custom ResultHandler. "
          + "Use safeResultHandlerEnabled=false setting to bypass this check "
          + "or ensure your statement returns ordered data and set resultOrdered=true on it.");
    }
  }

  // 处理非嵌套结果ResultMap
  private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping)
      throws SQLException {
    DefaultResultContext<Object> resultContext = new DefaultResultContext<Object>();
    skipRows(rsw.getResultSet(), rowBounds); // 设置rowBounds offset
    while (shouldProcessMoreRows(resultContext, rowBounds) && rsw.getResultSet().next()) { // 处理resultSet中每行数据
      // 如果有鉴别器，找出discriminator对应的resultMap，否则返回传入的resultMap
      ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rsw.getResultSet(), resultMap, null);
      Object rowValue = getRowValue(rsw, discriminatedResultMap);
      storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
    }
  }

  // 保存rowValue，设置到对应的结果对象属性上
  private void storeObject(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue, ResultMapping parentMapping, ResultSet rs) throws SQLException {
    if (parentMapping != null) { // 如果存在父结果集，将rowValue结果设置到父结果集上
      linkToParents(rs, parentMapping, rowValue);
    } else { // 不存在父结果集，则调用resultHandler的handleResult方法将rowValue放入list中
      callResultHandler(resultHandler, resultContext, rowValue);
    }
  }

  // 执行handleResult
  @SuppressWarnings("unchecked" /* because ResultHandler<?> is always ResultHandler<Object>*/)
  private void callResultHandler(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue) {
    resultContext.nextResultObject(rowValue);
    ((ResultHandler<Object>) resultHandler).handleResult(resultContext);
  }

  // 是否还有需要处理的行
  private boolean shouldProcessMoreRows(ResultContext<?> context, RowBounds rowBounds) throws SQLException {
    return !context.isStopped() && context.getResultCount() < rowBounds.getLimit();
  }

  // 根据RowBounds设置ResultSet，跳过offset
  private void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
    if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
      if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
        rs.absolute(rowBounds.getOffset());
      }
    } else { // ResultSet类型为TYPE_FORWARD_ONLY
      for (int i = 0; i < rowBounds.getOffset(); i++) {
        rs.next();
      }
    }
  }

  // GET VALUE FROM ROW FOR SIMPLE RESULT MAP
  // 从非嵌套resultMap中获取value
  private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException {
    final ResultLoaderMap lazyLoader = new ResultLoaderMap(); // 延迟加载
    Object rowValue = createResultObject(rsw, resultMap, lazyLoader, null); // 构造结果对象
    if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) { // resultMap没有对应的typeHandler
      final MetaObject metaObject = configuration.newMetaObject(rowValue);
      boolean foundValues = this.useConstructorMappings;    // 是否需要使用构造器构造映射
      if (shouldApplyAutomaticMappings(resultMap, false)) { // 是否使用自动映射
        foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, null) || foundValues; // 使用自动映射处理resultMap
      }
      foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, null) || foundValues; // 使用属性映射处理resultMap
      foundValues = lazyLoader.size() > 0 || foundValues; // 需要延时加载的也算foundValues
      rowValue = foundValues || configuration.isReturnInstanceForEmptyRow() ? rowValue : null;
    }
    return rowValue;
  }

  // 是否使用自动映射，如果设置了autoMapping或根据全局设置autoMappingBehavior返回是否使用自动映射
  private boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean isNested) {
    if (resultMap.getAutoMapping() != null) {
      return resultMap.getAutoMapping();
    } else {
      if (isNested) { // 是否是嵌套结果
        return AutoMappingBehavior.FULL == configuration.getAutoMappingBehavior();
      } else {
        return AutoMappingBehavior.NONE != configuration.getAutoMappingBehavior();
      }
    }
  }

  // PROPERTY MAPPINGS
  // 使用属性映射resultMap
  private boolean applyPropertyMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, ResultLoaderMap lazyLoader, String columnPrefix)
      throws SQLException {
    final List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix); // 有映射的列
    boolean foundValues = false; // 结果中是否有对应值
    final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
    for (ResultMapping propertyMapping : propertyMappings) {
      String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
      if (propertyMapping.getNestedResultMapId() != null) {
        // the user added a column attribute to a nested result map, ignore it
        // 如果列属性被设置成嵌套resultMap，忽略这种情况
        column = null;
      }
      // 只有满足下面条件之一才处理
      // 1、该ResultMapping的column是复合主键（column包含多列，只在嵌套查询时使用）
      // 2、resultSet结果中包含该属性列column
      // 3、ResultMapping包含resultSet（xml配置中包含resultSet）
      if (propertyMapping.isCompositeResult()
          || (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH)))
          || propertyMapping.getResultSet() != null) {
        Object value = getPropertyMappingValue(rsw.getResultSet(), metaObject, propertyMapping, lazyLoader, columnPrefix);
        // issue #541 make property optional （property可选）
        final String property = propertyMapping.getProperty();
        if (property == null) { // 如果ResultMapping中property为空，忽略
          continue;
        } else if (value == DEFERED) { // ResultMapping中包含resultSet，暂不处理，后面处理
          foundValues = true;
          continue;
        }
        if (value != null) {
          foundValues = true;
        }
        // value不空设置到结果对象上，如果为空根据全局配置isCallSettersOnNulls=true且不是基本类型（基本类型有默认值了）则进行设置
        if (value != null || (configuration.isCallSettersOnNulls() && !metaObject.getSetterType(property).isPrimitive())) {
          // gcode issue #377, call setter on nulls (value is not 'found')
          metaObject.setValue(property, value);
        }
      }
    }
    return foundValues;
  }

  // 从resultSet中获取属性映射的结果
  private Object getPropertyMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix)
      throws SQLException {
    if (propertyMapping.getNestedQueryId() != null) {    // 如果该ResultMapping存在嵌套查询，处理嵌套查询
      return getNestedQueryMappingValue(rs, metaResultObject, propertyMapping, lazyLoader, columnPrefix);
    } else if (propertyMapping.getResultSet() != null) { // 如果该ResultMapping存在resultSet（xml中存在resultSet配置）
      // 添加ResultMapping中resultSet关联的metaResultObject和propertyMapping到pendingRelations
      addPendingChildRelation(rs, metaResultObject, propertyMapping);   // TODO is that OK?
      return DEFERED; // 表示该结果延迟加载
    } else { // 否则直接从ResultSet中获取column对应的值
      final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
      final String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
      return typeHandler.getResult(rs, column); // 从resultSet结果集中获取column的值
    }
  }

  // 创建自动映射UnMappedColumnAutoMapping列表
  private List<UnMappedColumnAutoMapping> createAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
    final String mapKey = resultMap.getId() + ":" + columnPrefix; // 缓存autoMappingsCache的key
    List<UnMappedColumnAutoMapping> autoMapping = autoMappingsCache.get(mapKey); // 从autoMappingsCache缓存中获取未映射的UnMappedColumnAutoMapping
    if (autoMapping == null) { // 如果存在直接返回
      autoMapping = new ArrayList<UnMappedColumnAutoMapping>();
      final List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix); // 获取未映射的列
      for (String columnName : unmappedColumnNames) {
        String propertyName = columnName;
        if (columnPrefix != null && !columnPrefix.isEmpty()) { // columnPrefix非空
          // When columnPrefix is specified,
          // ignore columns without the prefix.
          if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
            propertyName = columnName.substring(columnPrefix.length());
          } else { // 如果指定了columnPrefix，忽略没有columnPrefix前缀的列
            continue;
          }
        }
        // 找到结果对象中propertyName对应的属性名
        final String property = metaObject.findProperty(propertyName, configuration.isMapUnderscoreToCamelCase());
        if (property != null && metaObject.hasSetter(property)) {
          if (resultMap.getMappedProperties().contains(property)) { // 如果resultMap的mappedProperties中有属性property，则这里不做处理
            continue;
          }
          final Class<?> propertyType = metaObject.getSetterType(property);
          if (typeHandlerRegistry.hasTypeHandler(propertyType, rsw.getJdbcType(columnName))) { // 是否存在该属性的typeHandler
            final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
            // 生成一个UnMappedColumnAutoMapping放入autoMapping，下面applyAutomaticMappings方法中会根据typeHandler获取对应的值
            autoMapping.add(new UnMappedColumnAutoMapping(columnName, property, typeHandler, propertyType.isPrimitive()));
          } else { // 如果没有对应的typeHandler，不做处理
            configuration.getAutoMappingUnknownColumnBehavior()
                .doAction(mappedStatement, columnName, property, propertyType);
          }
        } else { // 结果对象中没找到propertyName对应属性，根据AutoMappingUnknownColumnBehavior（默认为NONE）做日志上的处理，对结果上不做处理
          configuration.getAutoMappingUnknownColumnBehavior()
              .doAction(mappedStatement, columnName, (property != null) ? property : propertyName, null);
        }
      }
      autoMappingsCache.put(mapKey, autoMapping); // 将该resultMap的未映射结果缓存
    }
    return autoMapping;
  }

  // 使用自动映射处理resultMap
  private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
    List<UnMappedColumnAutoMapping> autoMapping = createAutomaticMappings(rsw, resultMap, metaObject, columnPrefix);
    boolean foundValues = false;
    if (!autoMapping.isEmpty()) {
      for (UnMappedColumnAutoMapping mapping : autoMapping) {
        final Object value = mapping.typeHandler.getResult(rsw.getResultSet(), mapping.column); // 根据typeHandler获取resultSet的结果值
        if (value != null) {
          foundValues = true;
        }
        // 1、结果value不空设置到结果对象上
        // 2、如果设置了全局配置isCallSettersOnNulls为true且该属性不是基本类型，设置该值到结果对象上
        if (value != null || (configuration.isCallSettersOnNulls() && !mapping.primitive)) {
          // gcode issue #377, call setter on nulls (value is not 'found')
          metaObject.setValue(mapping.property, value);
        }
      }
    }
    return foundValues;
  }

  // MULTIPLE RESULT SETS
  // 将rowValue设置到父结果集上
  private void linkToParents(ResultSet rs, ResultMapping parentMapping, Object rowValue) throws SQLException {
    // 计算多结果集key值，createKeyForMultipleResults的columns是parentMapping.getForeignColumn()，可以看出foreignColumn对应的是
    // 嵌套结果里面（结果集）的字段，column对应的是父resultMap结果集中的字段
    CacheKey parentKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn(), parentMapping.getForeignColumn());
    // pendingRelations中保存了resultSet配置对应的MeteObject和ResultMapping
    List<PendingRelation> parents = pendingRelations.get(parentKey);
    if (parents != null) {
      for (PendingRelation parent : parents) {
        if (parent != null && rowValue != null) {
          linkObjects(parent.metaObject, parent.propertyMapping, rowValue); // 将嵌套结果关联到父结果集上
        }
      }
    }
  }

  // 添加ResultMapping对应resultSet到pendingRelations
  private void addPendingChildRelation(ResultSet rs, MetaObject metaResultObject, ResultMapping parentMapping) throws SQLException {
    // 生成多结果集key值，createKeyForMultipleResults方法对应的names和columns都是parentMapping.getColumn()
    CacheKey cacheKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn(), parentMapping.getColumn());
    PendingRelation deferLoad = new PendingRelation();
    deferLoad.metaObject = metaResultObject;
    deferLoad.propertyMapping = parentMapping;
    List<PendingRelation> relations = pendingRelations.get(cacheKey);
    // issue #255
    if (relations == null) {
      relations = new ArrayList<DefaultResultSetHandler.PendingRelation>();
      pendingRelations.put(cacheKey, relations);
    }
    relations.add(deferLoad);
    ResultMapping previous = nextResultMaps.get(parentMapping.getResultSet());
    if (previous == null) {
      nextResultMaps.put(parentMapping.getResultSet(), parentMapping);
    } else { // 保证不同属性不能使用同一resultSet
      if (!previous.equals(parentMapping)) {
        throw new ExecutorException("Two different properties are mapped to the same resultSet");
      }
    }
  }

  // 多结果集key值计算（如果column包含多个字段，每个字段都加入计算key值）。参数中names可以看成是column包含字段的名称，columns是对应的列，
  // 在处理resultMapping中resultSet配置时，需要column和foreignColumn字段对应。因为处理父resultMap时生成cacheKey取结果是在父resultMap
  // 对应的resultSet中根据column获取，处理含resultSet的resultMapping时resultSet是多结果集中另外的结果，它们根据column和foreignColumn
  // 对应字段。
  private CacheKey createKeyForMultipleResults(ResultSet rs, ResultMapping resultMapping, String names, String columns) throws SQLException {
    CacheKey cacheKey = new CacheKey();
    cacheKey.update(resultMapping);
    if (columns != null && names != null) {
      String[] columnsArray = columns.split(",");
      String[] namesArray = names.split(",");
      for (int i = 0; i < columnsArray.length; i++) {
        Object value = rs.getString(columnsArray[i]);
        if (value != null) {
          cacheKey.update(namesArray[i]);
          cacheKey.update(value);
        }
      }
    }
    return cacheKey;
  }

  // INSTANTIATION & CONSTRUCTOR MAPPING
  // 创建结果对象
  private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
    this.useConstructorMappings = false; // reset previous mapping result 重置useConstructorMappings标志
    final List<Class<?>> constructorArgTypes = new ArrayList<Class<?>>();
    final List<Object> constructorArgs = new ArrayList<Object>();
    Object resultObject = createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
    // 如果结果对象不空且没有resultMap对应的typeHandler，找出需要延迟加载的嵌套查询创建代理对象（返回代理对象，延迟加载在代理对象中处理）
    if (resultObject != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
      final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
      for (ResultMapping propertyMapping : propertyMappings) {
        // issue gcode #109 && issue #149
        // 如果resultMap中存在嵌套查询且延迟加载，则创建代理对象，获取属性时通过代理对象查询结果
        if (propertyMapping.getNestedQueryId() != null && propertyMapping.isLazy()) {
          resultObject = configuration.getProxyFactory().createProxy(resultObject, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
          break;
        }
      }
    }
    // 设置useConstructorMappings标志
    this.useConstructorMappings = resultObject != null && !constructorArgTypes.isEmpty(); // set current mapping result
    return resultObject;
  }

  // 创建结果对象，首先看是否是基本类型，再看是否存在构造器配置，再看是否能被反射，最后看是否能使用自动映射处理，如果这些都不行抛出异常
  private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix)
      throws SQLException {
    final Class<?> resultType = resultMap.getType(); // 获取resultMap类型
    final MetaClass metaType = MetaClass.forClass(resultType, reflectorFactory);
    final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
    if (hasTypeHandlerForResultObject(rsw, resultType)) { // resultType是否存在typeHandler（基本类型的typeHandler）
      return createPrimitiveResultObject(rsw, resultMap, columnPrefix); // 获取resultMap为基本类型的查询结果
    } else if (!constructorMappings.isEmpty()) { // resultMap包含构造器constructor
      return createParameterizedResultObject(rsw, resultType, constructorMappings, constructorArgTypes, constructorArgs, columnPrefix);
    } else if (resultType.isInterface() || metaType.hasDefaultConstructor()) { // 如果resultMap类型是接口或该类有默认构造函数
      return objectFactory.create(resultType); // 反射创建该类
    } else if (shouldApplyAutomaticMappings(resultMap, false)) { // 是否应用自动映射处理
      return createByConstructorSignature(rsw, resultType, constructorArgTypes, constructorArgs, columnPrefix);
    }
    throw new ExecutorException("Do not know how to create an instance of " + resultType);
  }

  // 创建resultMap包含构造器（constructor）的结果对象
  Object createParameterizedResultObject(ResultSetWrapper rsw, Class<?> resultType, List<ResultMapping> constructorMappings,
                                         List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) {
    boolean foundValues = false;
    for (ResultMapping constructorMapping : constructorMappings) {
      final Class<?> parameterType = constructorMapping.getJavaType(); // constructorMapping对应的JavaType类型
      final String column = constructorMapping.getColumn();
      final Object value;
      try {
        if (constructorMapping.getNestedQueryId() != null) { // constructorMapping中存在嵌套查询
          value = getNestedQueryConstructorValue(rsw.getResultSet(), constructorMapping, columnPrefix); // 获取嵌套查询结果
        } else if (constructorMapping.getNestedResultMapId() != null) { // constructorMapping中存在嵌套结果
          final ResultMap resultMap = configuration.getResultMap(constructorMapping.getNestedResultMapId()); // 获取嵌套的resultMap
          value = getRowValue(rsw, resultMap); // 获取嵌套resultMap对应的结果
        } else { // 普通resultMap（不包含嵌套查询和嵌套结果）
          final TypeHandler<?> typeHandler = constructorMapping.getTypeHandler();
          value = typeHandler.getResult(rsw.getResultSet(), prependPrefix(column, columnPrefix)); // 从resultSet中获取column对应的结果
        }
      } catch (ResultMapException e) {
        throw new ExecutorException("Could not process result for mapping: " + constructorMapping, e);
      } catch (SQLException e) {
        throw new ExecutorException("Could not process result for mapping: " + constructorMapping, e);
      }
      constructorArgTypes.add(parameterType);
      constructorArgs.add(value);
      foundValues = value != null || foundValues;
    }
    // 利用resultType和构造函数参数类型、参数值创建结果对象
    return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
  }

  // 根据构造函数创建结果对象
  private Object createByConstructorSignature(ResultSetWrapper rsw, Class<?> resultType, List<Class<?>> constructorArgTypes, List<Object> constructorArgs,
                                              String columnPrefix) throws SQLException {
    final Constructor<?>[] constructors = resultType.getDeclaredConstructors();
    final Constructor<?> annotatedConstructor = findAnnotatedConstructor(constructors);
    if (annotatedConstructor != null) { // 如果存在构造器自动映射注解，使用参数自动映射构造方法
      return createUsingConstructor(rsw, resultType, constructorArgTypes, constructorArgs, columnPrefix, annotatedConstructor);
    } else {
      for (Constructor<?> constructor : constructors) {
        if (allowedConstructor(constructor, rsw.getClassNames())) { // 找到一个参数和返回结果中列相匹配的构造函数
          return createUsingConstructor(rsw, resultType, constructorArgTypes, constructorArgs, columnPrefix, constructor);
        }
      }
    }
    throw new ExecutorException("No constructor found in " + resultType.getName() + " matching " + rsw.getClassNames());
  }

  // 使用构造函数constructor创建对象
  private Object createUsingConstructor(ResultSetWrapper rsw, Class<?> resultType, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix, Constructor<?> constructor) throws SQLException {
    boolean foundValues = false;
    for (int i = 0; i < constructor.getParameterTypes().length; i++) {
      Class<?> parameterType = constructor.getParameterTypes()[i];
      String columnName = rsw.getColumnNames().get(i);
      TypeHandler<?> typeHandler = rsw.getTypeHandler(parameterType, columnName);
      Object value = typeHandler.getResult(rsw.getResultSet(), prependPrefix(columnName, columnPrefix));
      constructorArgTypes.add(parameterType);
      constructorArgs.add(value);
      foundValues = value != null || foundValues;
    }
    return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
  }

  // 找到Constructor上是否有AutomapConstructor注解（标识该构造器可以使用参数自动映射进行构造）
  private Constructor<?> findAnnotatedConstructor(final Constructor<?>[] constructors) {
    for (final Constructor<?> constructor : constructors) {
      if (constructor.isAnnotationPresent(AutomapConstructor.class)) {
        return constructor;
      }
    }
    return null;
  }

  // 构造函数是否与classNames中参数类型匹配
  private boolean allowedConstructor(final Constructor<?> constructor, final List<String> classNames) {
    final Class<?>[] parameterTypes = constructor.getParameterTypes();
    if (typeNames(parameterTypes).equals(classNames)) return true; // 匹配
    if (parameterTypes.length != classNames.size()) return false;
    for (int i = 0; i < parameterTypes.length; i++) { // 检查包装类型是否匹配
      final Class<?> parameterType = parameterTypes[i];
      if (parameterType.isPrimitive() && !primitiveTypes.getWrapper(parameterType).getName().equals(classNames.get(i))) {
        return false;
      } else if (!parameterType.isPrimitive() && !parameterType.getName().equals(classNames.get(i))) { // 存在不是基本类型且名称不相等
        return false;
      }
    }
    return true;
  }

  // 返回parameterTypes类的全限定名
  private List<String> typeNames(Class<?>[] parameterTypes) {
    List<String> names = new ArrayList<String>();
    for (Class<?> type : parameterTypes) {
      names.add(type.getName());
    }
    return names;
  }

  // 获取resultMap为基本类型的查询结果
  private Object createPrimitiveResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final String columnName; // 列名
    if (!resultMap.getResultMappings().isEmpty()) { // resultMap的resultMappings不空，因为是基本类型，里面只可能有一个ResultMapping
      final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
      final ResultMapping mapping = resultMappingList.get(0); // 取出ResultMapping
      columnName = prependPrefix(mapping.getColumn(), columnPrefix);
    } else {
      columnName = rsw.getColumnNames().get(0);
    }
    final TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName); // 获取columnName对应列的typeHandler
    return typeHandler.getResult(rsw.getResultSet(), columnName);
  }

  // NESTED QUERY
  // 获取构造器中嵌套查询结果
  private Object getNestedQueryConstructorValue(ResultSet rs, ResultMapping constructorMapping, String columnPrefix) throws SQLException {
    final String nestedQueryId = constructorMapping.getNestedQueryId();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
    final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, constructorMapping, nestedQueryParameterType, columnPrefix);
    Object value = null;
    if (nestedQueryParameterObject != null) {
      final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
      final Class<?> targetType = constructorMapping.getJavaType();
      final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, targetType, key, nestedBoundSql);
      value = resultLoader.loadResult(); // 执行sql查询获取结果
    }
    return value;
  }

  // 获取嵌套查询映射结果
  private Object getNestedQueryMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix)
      throws SQLException {
    final String nestedQueryId = propertyMapping.getNestedQueryId(); // 嵌套查询id
    final String property = propertyMapping.getProperty();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId); // 获取嵌套查询的mappedStatement
    final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType(); // 获取嵌套查询参数类类型
    // 设置嵌套查询的查询参数（入参）
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, propertyMapping, nestedQueryParameterType, columnPrefix);
    Object value = null;
    if (nestedQueryParameterObject != null) {
      final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
      final Class<?> targetType = propertyMapping.getJavaType();
      if (executor.isCached(nestedQuery, key)) { // 如果缓存了该查询结果（一级缓存）
        // 执行延迟加载（如果能从缓存中加载则将结果设置到结果对象metaResultObject上，如果还不能则先放到延迟加载队列等查询执行完后再次执行该操作）
        executor.deferLoad(nestedQuery, metaResultObject, property, key, targetType);
        value = DEFERED;
      } else { // 缓存中不存在
        final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, targetType, key, nestedBoundSql);
        if (propertyMapping.isLazy()) { // 设置了延迟加载lazy
          lazyLoader.addLoader(property, metaResultObject, resultLoader); // 生成LoadPair加入loaderMap中，等到使用时才执行LoadPair.load方法查询结果
          value = DEFERED;
        } else { // 没有设置延迟加载，直接调用resultLoader.loadResult执行sql查询
          value = resultLoader.loadResult();
        }
      }
    }
    return value;
  }

  // 为嵌套查询准备参数
  private Object prepareParameterForNestedQuery(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
    if (resultMapping.isCompositeResult()) { // 如果是复合主键
      return prepareCompositeKeyParameter(rs, resultMapping, parameterType, columnPrefix);
    } else {
      return prepareSimpleKeyParameter(rs, resultMapping, parameterType, columnPrefix);
    }
  }

  // 设置简单类型参数
  private Object prepareSimpleKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
    final TypeHandler<?> typeHandler;
    if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
      typeHandler = typeHandlerRegistry.getTypeHandler(parameterType);
    } else {
      typeHandler = typeHandlerRegistry.getUnknownTypeHandler();
    }
    return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
  }

  // 设置嵌套查询复合主键（column包含多列，形如： column="{prop1=col1,prop2=col2}"）的参数
  private Object prepareCompositeKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
    final Object parameterObject = instantiateParameterObject(parameterType); // 实例化parameterType
    final MetaObject metaObject = configuration.newMetaObject(parameterObject);
    boolean foundValues = false;
    // ResultMapping的composites中每个RequestMapping对应column中一列数据
    for (ResultMapping innerResultMapping : resultMapping.getComposites()) {
      final Class<?> propType = metaObject.getSetterType(innerResultMapping.getProperty());
      final TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(propType);
      final Object propValue = typeHandler.getResult(rs, prependPrefix(innerResultMapping.getColumn(), columnPrefix));
      // issue #353 & #560 do not execute nested query if key is null
      if (propValue != null) {
        metaObject.setValue(innerResultMapping.getProperty(), propValue);
        foundValues = true;
      }
    }
    return foundValues ? parameterObject : null;
  }

  // 实例化入参parameterType，如果为空返回HashMap
  private Object instantiateParameterObject(Class<?> parameterType) {
    if (parameterType == null) {
      return new HashMap<Object, Object>();
    } else if (ParamMap.class.equals(parameterType)) {
      return new HashMap<Object, Object>(); // issue #649
    } else {
      return objectFactory.create(parameterType);
    }
  }

  // DISCRIMINATOR
  // 如果存在discriminator，根据discriminator中value值获取对应的ResultMap，否则返回传入的resultMap
  public ResultMap resolveDiscriminatedResultMap(ResultSet rs, ResultMap resultMap, String columnPrefix) throws SQLException {
    Set<String> pastDiscriminators = new HashSet<String>();
    Discriminator discriminator = resultMap.getDiscriminator();
    while (discriminator != null) { // 处理嵌套discriminator
      final Object value = getDiscriminatorValue(rs, discriminator, columnPrefix); // 获取discriminator对应列的值
      final String discriminatedMapId = discriminator.getMapIdFor(String.valueOf(value)); // 获取value值对应的resultMapId
      if (configuration.hasResultMap(discriminatedMapId)) { // 配置中获取discriminator对应的resultMap
        resultMap = configuration.getResultMap(discriminatedMapId);
        Discriminator lastDiscriminator = discriminator;
        discriminator = resultMap.getDiscriminator();
        if (discriminator == lastDiscriminator || !pastDiscriminators.add(discriminatedMapId)) { // 循环引用
          break;
        }
      } else { // 配置中没有找到discriminator对应的resultMap
        break;
      }
    }
    return resultMap;
  }

  // 从resultSet结果中获取discriminator对应列的值，如<discriminator javaType="int" column="draft">，draft列对应的结果值
  private Object getDiscriminatorValue(ResultSet rs, Discriminator discriminator, String columnPrefix) throws SQLException {
    final ResultMapping resultMapping = discriminator.getResultMapping();
    final TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
    return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
  }

  // 列名添加前缀
  private String prependPrefix(String columnName, String prefix) {
    if (columnName == null || columnName.length() == 0 || prefix == null || prefix.length() == 0) {
      return columnName;
    }
    return prefix + columnName;
  }

  // HANDLE NESTED RESULT MAPS
  // 处理嵌套结果的ResultMap
  private void handleRowValuesForNestedResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
    final DefaultResultContext<Object> resultContext = new DefaultResultContext<Object>();
    skipRows(rsw.getResultSet(), rowBounds); // 设置resultSet的rowBounds offset
    Object rowValue = previousRowValue; // previousRowValue保存上一个ResultSet结果集最后的rowValue，仅对isResultOrdered=true时有用
    while (shouldProcessMoreRows(resultContext, rowBounds) && rsw.getResultSet().next()) { // 是否还有需要处理的行
      // 获取discriminator对应的ResultMap
      final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rsw.getResultSet(), resultMap, null);
      // 生成discriminatedResultMap的rowKey，根据每行结果，如果resultMap类型的相应属性和结果值都相等，则rowKey相等，这样如果rowKey
      // 相等的行合并成一个对象，而且rowKey相等则已经存放在nestedResultObjects中了，partialObject也就不为空
      final CacheKey rowKey = createRowKey(discriminatedResultMap, rsw, null);
      // 在映射过程中所有生成的映射对象(包括嵌套映射对象)，都会生成一个key并保存在nestedResultObjects，这样对于有association关联的
      // resultMap进行记录合并，例如resultMap是一个java model，里面包含list<model>，但是resultMap对应的model只能有一个，需要将
      // 多条记录合并成一个对象
      Object partialObject = nestedResultObjects.get(rowKey);
      // issue #577 && #542
      // resultOrdered，这个设置仅针对嵌套结果select语句适用：如果为true，就是假设包含了嵌套结果集或是分组了，这样的话当返回一个主结果行的时候，
      // 就不会发生有对前面结果集的引用的情况。这就使得在获取嵌套的结果集的时候不至于导致内存不够用。默认值：false。
      if (mappedStatement.isResultOrdered()) {
        // partialObject为空表示是一个新的结果对象，需要添加到resultHandler，partialObject不为空表示是需要合并的结果对象，需要把部分结果合
        // 并到partialObject对象中，而partialObject已经在第一次生成的时候通过storeObject方法添加到resultHandler中了
        if (partialObject == null && rowValue != null) {
          nestedResultObjects.clear();
          // 把结果对象rowValue加入到resultHandler.resultList中
          storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
        }
        // 获取resultMap对应的结果，根据每行数据生成rowKey，rowKey不同就生成一个新的结果对象
        rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
      } else {
        rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
        if (partialObject == null) {
          storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
        }
      }
    }
    // 处理isResultOrdered为true的最后一次结果（因为处理isResultOrdered是先保存上一次结果再获取rowValue，getRowValue时会将rowKey添加到
    // nestedResultObjects中，而在isResultOrdered为true时可能会先清空nestedResultObjects）
    if (rowValue != null && mappedStatement.isResultOrdered() && shouldProcessMoreRows(resultContext, rowBounds)) {
      storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
      previousRowValue = null;
    } else if (rowValue != null) {
      previousRowValue = rowValue;
    }
  }

  // GET VALUE FROM ROW FOR NESTED RESULT MAP
  // 从嵌套结果中获取结果对象rowValue
  private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap, CacheKey combinedKey, String columnPrefix, Object partialObject) throws SQLException {
    final String resultMapId = resultMap.getId();
    Object rowValue = partialObject; // partialObject不空表示需要将结果合并到该结果对象，之前已经保存在nestedResultObjects中或是嵌套结果
    if (rowValue != null) { // partialObject不为空，需要将嵌套结果合并到rowValue
      final MetaObject metaObject = configuration.newMetaObject(rowValue);
      putAncestor(rowValue, resultMapId); // 将resultMapId先放入ancestorObjects，记录嵌套resultMap是否存在循环引用
      applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combinedKey, false);
      ancestorObjects.remove(resultMapId);
    } else {
      final ResultLoaderMap lazyLoader = new ResultLoaderMap(); // 延迟加载
      rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix); // 创建结果对象
      if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
        final MetaObject metaObject = configuration.newMetaObject(rowValue);
        boolean foundValues = this.useConstructorMappings;
        if (shouldApplyAutomaticMappings(resultMap, true)) { // 使用自动映射处理resultMap结果
          foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues;
        }
        foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix) || foundValues;
        putAncestor(rowValue, resultMapId);
        foundValues = applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combinedKey, true) || foundValues;
        ancestorObjects.remove(resultMapId);
        foundValues = lazyLoader.size() > 0 || foundValues;
        rowValue = foundValues || configuration.isReturnInstanceForEmptyRow() ? rowValue : null;
      }
      // 保存一行的结果对象rowKey到nestedResultObjects
      if (combinedKey != CacheKey.NULL_CACHE_KEY) {
        nestedResultObjects.put(combinedKey, rowValue);
      }
    }
    return rowValue;
  }

  // 将resultMapId先放入ancestorObjects，记录嵌套resultMap是否存在循环引用
  private void putAncestor(Object resultObject, String resultMapId) {
    ancestorObjects.put(resultMapId, resultObject);
  }

  // NESTED RESULT MAP (JOIN MAPPING)
  // 合并嵌套结果到结果对象metaObject中
  private boolean applyNestedResultMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String parentPrefix, CacheKey parentRowKey, boolean newObject) {
    boolean foundValues = false;
    for (ResultMapping resultMapping : resultMap.getPropertyResultMappings()) {
      final String nestedResultMapId = resultMapping.getNestedResultMapId(); // 嵌套结果id
      if (nestedResultMapId != null && resultMapping.getResultSet() == null) { // nestedResultMapId不空且该resultMap不存在resultSet设置
        try {
          final String columnPrefix = getColumnPrefix(parentPrefix, resultMapping); // 拼接columnPrefix
          final ResultMap nestedResultMap = getNestedResultMap(rsw.getResultSet(), nestedResultMapId, columnPrefix); // 获取嵌套ResultMap
          if (resultMapping.getColumnPrefix() == null) { // resultMapping不包含columnPrefix
            // try to fill circular reference only when columnPrefix
            // is not specified for the nested result map (issue #215)
            // 处理循环引用（只有在resultMap没有设置columnPrefix时处理，否则columnPrefix拼接后无法处理这种情况）
            Object ancestorObject = ancestorObjects.get(nestedResultMapId); // 已经存在nestedResultMapId对应的值
            if (ancestorObject != null) { // 如果已经存在（循环引用）
              if (newObject) { // 第一次需要设置该属性到结果对象上
                // 将nestedResultMapId对应的值ancestorObject设置到metaObject
                linkObjects(metaObject, resultMapping, ancestorObject); // issue #385
              }
              continue;
            }
          }
          final CacheKey rowKey = createRowKey(nestedResultMap, rsw, columnPrefix); // 嵌套resultMap的rowKey
          final CacheKey combinedKey = combineKeys(rowKey, parentRowKey); // resultMap的rowKey和嵌套resultMap的rowKey组合
          Object rowValue = nestedResultObjects.get(combinedKey); // nestedResultObjects是否已经存在该结果
          boolean knownValue = rowValue != null; // 已经存在的结果
          // 初始化resultMapping property集合属性
          instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject); // mandatory（强制的）
          // 如果notNullColumn属性有值或结果集包含columnPrefix或者没有notNullColumn属性和columnPrefix
          if (anyNotNullColumnHasValue(resultMapping, columnPrefix, rsw)) {
            rowValue = getRowValue(rsw, nestedResultMap, combinedKey, columnPrefix, rowValue);
            if (rowValue != null && !knownValue) { // 如果rowValue不空且不是partialObject，将结果rowValue设置到metaObject上
              linkObjects(metaObject, resultMapping, rowValue);
              foundValues = true;
            }
          }
        } catch (SQLException e) {
          throw new ExecutorException("Error getting nested result map values for '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
        }
      }
    }
    return foundValues;
  }

  // parentPrefix拼接resultMapping的columnPrefix
  private String getColumnPrefix(String parentPrefix, ResultMapping resultMapping) {
    final StringBuilder columnPrefixBuilder = new StringBuilder();
    if (parentPrefix != null) {
      columnPrefixBuilder.append(parentPrefix);
    }
    if (resultMapping.getColumnPrefix() != null) {
      columnPrefixBuilder.append(resultMapping.getColumnPrefix());
    }
    return columnPrefixBuilder.length() == 0 ? null : columnPrefixBuilder.toString().toUpperCase(Locale.ENGLISH);
  }

  // notNullColumn属性是否有值或结果集中是否有列名包含columnPrefix，否则返回true（如果没有notNullColumns和columnPrefix返回true）
  private boolean anyNotNullColumnHasValue(ResultMapping resultMapping, String columnPrefix, ResultSetWrapper rsw) throws SQLException {
    Set<String> notNullColumns = resultMapping.getNotNullColumns();
    if (notNullColumns != null && !notNullColumns.isEmpty()) {
      ResultSet rs = rsw.getResultSet();
      for (String column : notNullColumns) {
        rs.getObject(prependPrefix(column, columnPrefix));
        if (!rs.wasNull()) {
          return true;
        }
      }
      return false;
    } else if (columnPrefix != null) {
      for (String columnName : rsw.getColumnNames()) { // 列名中包含columnPrefix前缀（对应多个resultMap共用，以前缀区分）
        if (columnName.toUpperCase().startsWith(columnPrefix.toUpperCase())) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  // 根据nestedResultMapId获取嵌套结果
  private ResultMap getNestedResultMap(ResultSet rs, String nestedResultMapId, String columnPrefix) throws SQLException {
    ResultMap nestedResultMap = configuration.getResultMap(nestedResultMapId);
    return resolveDiscriminatedResultMap(rs, nestedResultMap, columnPrefix); // 如果nestedResultMap包含discriminator则处理鉴别器，否则返回nestedResultMap
  }

  // UNIQUE RESULT KEY
  // 生成resultMap对应rowKey
  private CacheKey createRowKey(ResultMap resultMap, ResultSetWrapper rsw, String columnPrefix) throws SQLException {
    final CacheKey cacheKey = new CacheKey();
    cacheKey.update(resultMap.getId());
    List<ResultMapping> resultMappings = getResultMappingsForRowKey(resultMap);
    if (resultMappings.isEmpty()) {
      if (Map.class.isAssignableFrom(resultMap.getType())) {
        createRowKeyForMap(rsw, cacheKey); // 如果resultMap是map类型，将ResultSet对应的列加入key值计算
      } else { // 使用未映射的列加入key值计算
        createRowKeyForUnmappedProperties(resultMap, rsw, cacheKey, columnPrefix);
      }
    } else { // 使用有映射的列加入key值计算
      createRowKeyForMappedProperties(resultMap, rsw, cacheKey, resultMappings, columnPrefix);
    }
    if (cacheKey.getUpdateCount() < 2) { // 如果少于两个属性加入key值计算，就用NULL_CACHE_KEY
      return CacheKey.NULL_CACHE_KEY;
    }
    return cacheKey;
  }

  // 联合key
  private CacheKey combineKeys(CacheKey rowKey, CacheKey parentRowKey) {
    if (rowKey.getUpdateCount() > 1 && parentRowKey.getUpdateCount() > 1) {
      CacheKey combinedKey;
      try {
        combinedKey = rowKey.clone();
      } catch (CloneNotSupportedException e) {
        throw new ExecutorException("Error cloning cache key.  Cause: " + e, e);
      }
      combinedKey.update(parentRowKey);
      return combinedKey;
    }
    return CacheKey.NULL_CACHE_KEY;
  }

  // 获取resultMap的存放有id或idArg的ResultMapping，如果没有返回普通的属性的ResultMapping
  private List<ResultMapping> getResultMappingsForRowKey(ResultMap resultMap) {
    // 如果有id或idArg的ResultMapping直接返回，否则返回propertyResultMappings。从这里可以看到如果设置了id属性，生成rowKey时只用这些id
    // 属性，否则使用全部的属性，性能会差一些。
    List<ResultMapping> resultMappings = resultMap.getIdResultMappings();
    if (resultMappings.isEmpty()) {
      resultMappings = resultMap.getPropertyResultMappings();
    }
    return resultMappings;
  }

  // 使用有映射的列和结果值加入key值计算。
  // 注意：id元素在嵌套结果映射中扮演着非 常重要的角色。你应该总是指定一个或多个可以唯一标识结果的属性。实际上如果你不指定它的话, MyBatis仍然
  // 可以工作,但是会有严重的性能问题。在可以唯一标识结果的情况下, 尽可能少的选择属性。主键是一个显而易见的选择（即使是复合主键）。
  private void createRowKeyForMappedProperties(ResultMap resultMap, ResultSetWrapper rsw, CacheKey cacheKey, List<ResultMapping> resultMappings, String columnPrefix) throws SQLException {
    for (ResultMapping resultMapping : resultMappings) {
      if (resultMapping.getNestedResultMapId() != null && resultMapping.getResultSet() == null) { // resultMap存在嵌套结果
        // Issue #392
        final ResultMap nestedResultMap = configuration.getResultMap(resultMapping.getNestedResultMapId());
        createRowKeyForMappedProperties(nestedResultMap, rsw, cacheKey, nestedResultMap.getConstructorResultMappings(),
            prependPrefix(resultMapping.getColumnPrefix(), columnPrefix)); // 递归计算嵌套结果的key值
      } else if (resultMapping.getNestedQueryId() == null) { // 没有嵌套查询
        final String column = prependPrefix(resultMapping.getColumn(), columnPrefix);
        final TypeHandler<?> th = resultMapping.getTypeHandler();
        List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix); // 有映射的列
        // Issue #114
        if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
          final Object value = th.getResult(rsw.getResultSet(), column); // 根据column获取结果
          if (value != null || configuration.isReturnInstanceForEmptyRow()) {
            cacheKey.update(column);
            cacheKey.update(value);
          }
        }
      }
    }
  }

  // 将未映射的列和结果值加入key值计算
  private void createRowKeyForUnmappedProperties(ResultMap resultMap, ResultSetWrapper rsw, CacheKey cacheKey, String columnPrefix) throws SQLException {
    final MetaClass metaType = MetaClass.forClass(resultMap.getType(), reflectorFactory);
    List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
    for (String column : unmappedColumnNames) {
      String property = column;
      if (columnPrefix != null && !columnPrefix.isEmpty()) {
        // When columnPrefix is specified, ignore columns without the prefix.
        if (column.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) { // 去掉前缀columnPrefix
          property = column.substring(columnPrefix.length());
        } else {
          continue;
        }
      }
      if (metaType.findProperty(property, configuration.isMapUnderscoreToCamelCase()) != null) {
        String value = rsw.getResultSet().getString(column);
        if (value != null) {
          cacheKey.update(column);
          cacheKey.update(value);
        }
      }
    }
  }

  // 如果resultMap类型是map，将ResultSet对应的列和结果值加入key值计算
  private void createRowKeyForMap(ResultSetWrapper rsw, CacheKey cacheKey) throws SQLException {
    List<String> columnNames = rsw.getColumnNames();
    for (String columnName : columnNames) {
      final String value = rsw.getResultSet().getString(columnName);
      if (value != null) {
        cacheKey.update(columnName);
        cacheKey.update(value);
      }
    }
  }

  // 将结果设置到对象metaObject的属性上
  private void linkObjects(MetaObject metaObject, ResultMapping resultMapping, Object rowValue) {
    final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject); // 是否集合属性
    if (collectionProperty != null) { // 如果是集合则添加rowValue，否则设置属性
      final MetaObject targetMetaObject = configuration.newMetaObject(collectionProperty);
      targetMetaObject.add(rowValue);
    } else {
      metaObject.setValue(resultMapping.getProperty(), rowValue);
    }
  }

  // 初始化resultMapping property集合属性
  private Object instantiateCollectionPropertyIfAppropriate(ResultMapping resultMapping, MetaObject metaObject) {
    final String propertyName = resultMapping.getProperty();
    Object propertyValue = metaObject.getValue(propertyName);
    if (propertyValue == null) { // 结果对象没有设置propertyName属性值
      Class<?> type = resultMapping.getJavaType();
      if (type == null) {
        type = metaObject.getSetterType(propertyName);
      }
      try {
        if (objectFactory.isCollection(type)) {
          propertyValue = objectFactory.create(type);
          metaObject.setValue(propertyName, propertyValue); // 根据type类型实例化对象设置到propertyName属性上
          return propertyValue;
        }
      } catch (Exception e) {
        throw new ExecutorException("Error instantiating collection property for result '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
      }
    } else if (objectFactory.isCollection(propertyValue.getClass())) {
      return propertyValue;
    }
    return null;
  }

  // 是否存在resultType类型对应的typeHandler
  private boolean hasTypeHandlerForResultObject(ResultSetWrapper rsw, Class<?> resultType) {
    if (rsw.getColumnNames().size() == 1) { // 如果只有一列数据，根据resultType和JdbcType去找有没有对应的typeHandler，否则直接根据resultType
      return typeHandlerRegistry.hasTypeHandler(resultType, rsw.getJdbcType(rsw.getColumnNames().get(0)));
    }
    return typeHandlerRegistry.hasTypeHandler(resultType);
  }

}
