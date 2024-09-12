package com.doublew2w.sbs.mybatis.type;

import cn.hutool.db.meta.JdbcType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 类型处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 2:45
 * @project: sbs-mybatis
 */
public interface TypeHandler<T> {

  /** 设置参数 */
  void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
      throws SQLException;
}
