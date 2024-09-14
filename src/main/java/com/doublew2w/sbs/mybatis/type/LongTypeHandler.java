package com.doublew2w.sbs.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Long类型处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 22:26
 * @project: sbs-mybatis
 */
public class LongTypeHandler extends BaseTypeHandler<Long> {

  @Override
  protected Long getNullableResult(ResultSet rs, String columnName) throws SQLException{
    return rs.getLong(columnName);
  }

  @Override
  protected void setNonNullParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setLong(i, parameter);
  }
}
