package com.doublew2w.sbs.mybatis.session.defaults;

import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.Environment;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 5:41
 * @project: sbs-mybatis
 */
public class DefaultSqlSession implements SqlSession {
  private final Configuration configuration;

  public DefaultSqlSession(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public <T> T selectOne(String statement) {
    return (T) ("你被代理了！" + statement);
  }

  @Override
  public <T> T selectOne(String statement, Object parameter) {
    try {
      MappedStatement mappedStatement = configuration.getMappedStatement(statement);
      Environment environment = configuration.getEnvironment();

      Connection connection = environment.getDataSource().getConnection();

      BoundSql boundSql = mappedStatement.getBoundSql();
      PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSql());
      preparedStatement.setLong(1, Long.parseLong(((Object[]) parameter)[0].toString()));
      ResultSet resultSet = preparedStatement.executeQuery();

      List<T> objList = resultSet2Obj(resultSet, Class.forName(boundSql.getResultType()));
      return objList.get(0);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public <T> T getMapper(Class<T> type) {
    return configuration.getMapper(type, this);
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
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
