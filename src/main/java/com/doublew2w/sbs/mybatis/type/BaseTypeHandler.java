package com.doublew2w.sbs.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 类型处理器的积累
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 22:24
 * @project: sbs-mybatis
 */
public abstract class BaseTypeHandler<T> implements TypeHandler<T> {
  @Override
  public void setParameter(PreparedStatement ps, int parameterIndex, T parameter, JdbcType jdbcType)
      throws SQLException {
    // 定义抽象方法，由子类实现不同类型的属性设置
    setNonNullParameter(ps, parameterIndex, parameter, jdbcType);
  }

  @Override
  public T getResult(ResultSet rs, String columnName) throws SQLException {
    return getNullableResult(rs, columnName);
  }

  protected abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

  protected abstract void setNonNullParameter(
      PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;
}
