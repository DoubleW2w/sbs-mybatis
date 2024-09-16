package com.doublew2w.sbs.mybatis.executor;

import com.doublew2w.sbs.mybatis.executor.statement.StatementHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
import java.sql.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 简单执行器
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 3:58
 * @project: sbs-mybatis
 */
@Slf4j
public class SimpleExecutor extends BaseExecutor {

  public SimpleExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }

  @Override
  protected int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Statement stmt = null;
    try {
      // 获取配置类
      Configuration configuration = ms.getConfiguration();
      // 新建一个 StatementHandler
      StatementHandler handler =
          configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
      // 准备语句
      stmt = prepareStatement(handler);
      // StatementHandler.update
      return handler.update(stmt);
    } finally {
      closeStatement(stmt);
    }
  }

  @Override
  protected <E> List<E> doQuery(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      BoundSql boundSql) {
    Statement stmt = null;
    try {
      // 获取配置类
      Configuration configuration = ms.getConfiguration();
      // 新建一个 StatementHandler
      StatementHandler handler =
          configuration.newStatementHandler(
              this, ms, parameter, rowBounds, resultHandler, boundSql);
      Connection connection = transaction.getConnection();
      // 准备语句
      stmt = prepareStatement(handler);
      // 返回结果
      return handler.query(stmt, resultHandler);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  private Statement prepareStatement(StatementHandler handler) throws SQLException {
    Statement stmt;
    Connection connection = transaction.getConnection();
    // 准备语句
    stmt = handler.prepare(connection);
    handler.parameterize(stmt);
    return stmt;
  }
}
