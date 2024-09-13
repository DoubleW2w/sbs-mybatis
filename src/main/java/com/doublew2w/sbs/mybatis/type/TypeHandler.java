package com.doublew2w.sbs.mybatis.type;


import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 类型处理器：用于处理 Java 数据类型和 JDBC 数据类型之间的转换。
 *
 * <p>将 Java 对象的属性与数据库字段的值进行正确地映射和转换。
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 2:45
 * @project: sbs-mybatis
 */
public interface TypeHandler<T> {

  /** 设置参数 */
  void setParameter(PreparedStatement ps, int parameterIndex, T parameter, JdbcType jdbcType)
      throws SQLException;
}
