package com.doublew2w.sbs.mybatis.executor.statement;

import com.doublew2w.sbs.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 语句处理器：1.准备语句 2. 参数化 3.执行查询
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 4:16
 * @project: sbs-mybatis
 */
public interface StatementHandler {
  /** 准备语句 */
  Statement prepare(Connection connection) throws SQLException;

  /** 参数化 */
  void parameterize(Statement statement) throws SQLException;

  /** 执行查询 */
  <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;
}
