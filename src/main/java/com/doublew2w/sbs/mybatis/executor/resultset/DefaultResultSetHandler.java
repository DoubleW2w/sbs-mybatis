package com.doublew2w.sbs.mybatis.executor.resultset;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
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

  private final BoundSql boundSql;
  private final MappedStatement mappedStatement;

  public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
    this.boundSql = boundSql;
    this.mappedStatement = mappedStatement;
  }

  @Override
  public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
    ResultSet resultSet = stmt.getResultSet();
    return resultSet2Obj(resultSet, mappedStatement.getResultType());
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
