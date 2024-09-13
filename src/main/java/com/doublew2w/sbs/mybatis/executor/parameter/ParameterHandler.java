package com.doublew2w.sbs.mybatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 参数处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 16:59
 * @project: sbs-mybatis
 */
public interface ParameterHandler {
  /**
   * 获取参数
   */
  Object getParameterObject();

  /**
   * 设置参数
   */
  void setParameters(PreparedStatement ps) throws SQLException;
}
