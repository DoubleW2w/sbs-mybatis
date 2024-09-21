package com.doublew2w.sbs.mybatis.executor.resultset;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.ResultMap;
import com.doublew2w.sbs.mybatis.mapping.ResultMapping;
import com.doublew2w.sbs.mybatis.reflection.MetaClass;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.reflection.factory.ObjectFactory;
import com.doublew2w.sbs.mybatis.session.*;
import com.doublew2w.sbs.mybatis.type.TypeHandler;
import com.doublew2w.sbs.mybatis.type.TypeHandlerRegistry;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认Map结果处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 4:11
 * @project: sbs-mybatis
 */
@Slf4j
public class DefaultResultSetHandler implements ResultSetHandler {
  private static final Object NO_VALUE = new Object();

  private final Configuration configuration;
  private final MappedStatement mappedStatement;
  private final RowBounds rowBounds;
  private final ResultHandler resultHandler;
  private final BoundSql boundSql;
  private final TypeHandlerRegistry typeHandlerRegistry;
  private final ObjectFactory objectFactory;

  public DefaultResultSetHandler(
      Executor executor,
      MappedStatement mappedStatement,
      ResultHandler resultHandler,
      RowBounds rowBounds,
      BoundSql boundSql) {
    this.configuration = mappedStatement.getConfiguration();
    this.rowBounds = rowBounds;
    this.boundSql = boundSql;
    this.mappedStatement = mappedStatement;
    this.resultHandler = resultHandler;
    this.objectFactory = configuration.getObjectFactory();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
  }

  @Override
  public List<Object> handleResultSets(Statement stmt) throws SQLException {
    log.info("正在处理结果集...");
    final List<Object> multipleResults = new ArrayList<>();
    int resultSetCount = 0;
    ResultSetWrapper rsw = new ResultSetWrapper(stmt.getResultSet(), configuration);
    List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    while (rsw != null && resultMaps.size() > resultSetCount) {
      ResultMap resultMap = resultMaps.get(resultSetCount);
      // 处理结果
      handleResultSet(rsw, resultMap, multipleResults, null);
      // 获取下一个结果集
      rsw = getNextResultSet(stmt);
      resultSetCount++;
    }
    return collapseSingleResultList(multipleResults);
  }

  @SuppressWarnings("unchecked")
  private List<Object> collapseSingleResultList(List<Object> multipleResults) {
    return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
  }

  private void handleResultSet(
      ResultSetWrapper rsw,
      ResultMap resultMap,
      List<Object> multipleResults,
      ResultMapping parentMapping)
      throws SQLException {
    try {
      if (resultHandler == null) {
        // 1. 新创建结果处理器
        DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
        // 2. 封装数据
        handleRowValuesForSimpleResultMap(rsw, resultMap, defaultResultHandler, rowBounds, null);
        // 3. 保存结果
        multipleResults.add(defaultResultHandler.getResultList());
      }
    } finally {
      // issue #228 (close resultsets)
      closeResultSet(rsw.getResultSet());
    }
  }

  private void closeResultSet(ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException e) {
      // ignore
    }
  }

  /** 处理行记录 */
  private void handleRowValuesForSimpleResultMap(
      ResultSetWrapper rsw,
      ResultMap resultMap,
      ResultHandler resultHandler,
      RowBounds rowBounds,
      ResultMapping parentMapping)
      throws SQLException {
    DefaultResultContext resultContext = new DefaultResultContext();
    ResultSet resultSet = rsw.getResultSet();
    while (resultContext.getResultCount() < rowBounds.getLimit() && resultSet.next()) {
      Object rowValue = getRowValue(rsw, resultMap);
      callResultHandler(resultHandler, resultContext, rowValue);
    }
  }

  /**
   * 获取一行的值
   *
   * @param rsw 结果集包装器
   * @param resultMap 结果
   * @return
   */
  private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException {
    // 根据返回类型，实例化对象
    Object resultObject = createResultObject(rsw, resultMap, null);
    if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
      final MetaObject metaObject = configuration.newMetaObject(resultObject);
      // 自动映射：把每列的值都赋到对应的字段上
      applyAutomaticMappings(rsw, resultMap, metaObject, null);
      // Map映射：根据映射类型赋值到字段
      applyPropertyMappings(rsw, resultMap, metaObject, null);
    }
    return resultObject;
  }

  private boolean applyAutomaticMappings(
      ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix)
      throws SQLException {
    final List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
    boolean foundValues = false;

    for (String columnName : unmappedColumnNames) {
      // 根据「列前缀」获取到正确的属性名称
      String propertyName = columnName;
      if (columnPrefix != null && !columnPrefix.isEmpty()) {
        // When columnPrefix is specified,ignore columns without the prefix.
        if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
          propertyName = columnName.substring(columnPrefix.length());
        } else {
          continue;
        }
      }
      final String property = metaObject.findProperty(propertyName, false);
      if (property != null && metaObject.hasSetter(property)) {
        // 获取到「属性列的类型」
        final Class<?> propertyType = metaObject.getSetterType(property);
        if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
          final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
          // 使用 TypeHandler 取得结果
          final Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
          if (value != null) {
            foundValues = true;
          }
          if (value != null || !propertyType.isPrimitive()) {
            // 通过反射工具类设置属性值
            metaObject.setValue(property, value);
          }
        }
      }
    }
    return foundValues;
  }

  private boolean applyPropertyMappings(
      ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix)
      throws SQLException {
    final List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
    boolean foundValues = false;
    final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
    for (ResultMapping propertyMapping : propertyMappings) {
      final String column = propertyMapping.getColumn();
      if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
        // 获取值
        final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
        Object value = typeHandler.getResult(rsw.getResultSet(), column);
        // 设置值
        final String property = propertyMapping.getProperty();
        if (value != NO_VALUE && property != null && value != null) {
          // 通过反射工具类设置属性值
          metaObject.setValue(property, value);
          foundValues = true;
        }
      }
    }
    return foundValues;
  }

  private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix)
      throws SQLException {
    final List<Class<?>> constructorArgTypes = new ArrayList<>();
    final List<Object> constructorArgs = new ArrayList<>();
    return createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
  }

  /** 创建结果 */
  private Object createResultObject(
      ResultSetWrapper rsw,
      ResultMap resultMap,
      List<Class<?>> constructorArgTypes,
      List<Object> constructorArgs,
      String columnPrefix)
      throws SQLException {
    // 通过结果类型创建「元类」对象
    final Class<?> resultType = resultMap.getType();
    final MetaClass metaType = MetaClass.forClass(resultType);
    if (typeHandlerRegistry.hasTypeHandler(resultType)) {
      // 基本类型
      return createPrimitiveResultObject(rsw, resultMap, columnPrefix);
    } else if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
      // 普通的Bean对象类型
      return objectFactory.create(resultType);
    }
    throw new RuntimeException("Do not know how to create an instance of " + resultType);
  }

  // 简单类型创建
  private Object createPrimitiveResultObject(
      ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final String columnName;
    if (!resultMap.getResultMappings().isEmpty()) {
      final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
      final ResultMapping mapping = resultMappingList.get(0);
      columnName = prependPrefix(mapping.getColumn(), columnPrefix);
    } else {
      columnName = rsw.getColumnNames().get(0);
    }
    final TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName);
    return typeHandler.getResult(rsw.getResultSet(), columnName);
  }

  private String prependPrefix(String columnName, String prefix) {
    if (columnName == null || columnName.length() == 0 || prefix == null || prefix.length() == 0) {
      return columnName;
    }
    return prefix + columnName;
  }

  /** 调用结果处理器 */
  private void callResultHandler(
      ResultHandler resultHandler, DefaultResultContext resultContext, Object rowValue) {
    resultContext.nextResultObject(rowValue);
    resultHandler.handleResult(resultContext);
  }

  private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
    // Making this method tolerant of bad JDBC drivers
    try {
      // 是否支持多结果集
      if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
        // stmt.getMoreResults() = false 表示 不存在更多的结果集
        // stmt.getUpdateCount() = -1 表示 当前结果集为不是更新计数
        //  if 为true表示，结果集只有一条
        if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
          ResultSet rs = stmt.getResultSet();
          return rs != null ? new ResultSetWrapper(rs, configuration) : null;
        }
      }
    } catch (Exception ignore) {
      // Intentionally ignored.
    }
    return null;
  }

  private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
    // 获取结果集
    ResultSet rs = stmt.getResultSet();
    while (rs == null) {
      // 尝试向前移动以获取第一个结果集，以防驱动程序没有将结果集作为第一个结果返回（例如HSQLDB 2.1）
      if (stmt.getMoreResults()) {
        rs = stmt.getResultSet();
      } else {
        if (stmt.getUpdateCount() == -1) {
          break;
        }
      }
    }
    // 如果存在结果集，就返回包装了结果集的 ResultSetWrapper 对象
    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
  }

  private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
    List<T> list = new ArrayList<>();
    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();
      // 每次遍历行值
      while (resultSet.next()) {
        T obj = (T) clazz.getDeclaredConstructor().newInstance();
        for (int i = 1; i <= columnCount; i++) {
          Object value = resultSet.getObject(i);
          String columnName = metaData.getColumnName(i);
          String setMethod =
              "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
          Method method;
          if (value instanceof Timestamp) {
            method = clazz.getMethod(setMethod, Date.class);
          } else {
            method = clazz.getMethod(setMethod, value.getClass());
          }
          method.invoke(obj, value);
        }
        list.add(obj);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }
}
