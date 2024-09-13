package com.doublew2w.sbs.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author: DoubleW2w
 * @date: 2024/9/13 22:29
 * @project: sbs-mybatis
 */
public class StringTypeHandler extends BaseTypeHandler<String> {
  @Override
  protected void setNonNullParameter(
      PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter);
  }
}
