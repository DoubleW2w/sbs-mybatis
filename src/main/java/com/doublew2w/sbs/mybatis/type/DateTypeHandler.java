package com.doublew2w.sbs.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Date类型处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/19 10:20
 * @project: sbs-mybatis
 */
public class DateTypeHandler extends BaseTypeHandler<Date> {
  @Override
  protected Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Timestamp sqlTimestamp = rs.getTimestamp(columnName);
    if (sqlTimestamp != null) {
      return new Date(sqlTimestamp.getTime());
    }
    return null;
  }

  @Override
  protected void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTimestamp(i, new Timestamp((parameter).getTime()));
  }
}
