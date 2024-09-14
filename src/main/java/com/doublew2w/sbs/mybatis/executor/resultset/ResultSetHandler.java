package com.doublew2w.sbs.mybatis.executor.resultset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 结果集处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 4:09
 * @project: sbs-mybatis
 */
public interface ResultSetHandler {
  /**
   * 处理SQL查询结果集
   *
   * @param stmt 执行SQL语句的语句对象
   * @return List结果集
   * @param <E> 结果类型
   * @throws SQLException SQL异常
   */
  <E> List<E> handleResultSets(Statement stmt) throws SQLException;
}
