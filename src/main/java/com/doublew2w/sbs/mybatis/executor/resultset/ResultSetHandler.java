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
  <E> List<E> handleResultSets(Statement stmt) throws SQLException;
}
