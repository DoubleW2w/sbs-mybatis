package com.doublew2w.sbs.mybatis.executor.statement;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 简单语句处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 4:18
 * @project: sbs-mybatis
 */
@Slf4j
public class SimpleStatementHandler extends BaseStatementHandler {
  public SimpleStatementHandler(
      Executor executor,
      MappedStatement mappedStatement,
      Object parameterObject,
      ResultHandler resultHandler,
      BoundSql boundSql) {
    super(executor, mappedStatement, parameterObject, resultHandler, boundSql);
  }

  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    return connection.createStatement();
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    //
  }

  @Override
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    log.info("执行查询");
    String sql = boundSql.getSql();
    statement.execute(sql);
    return resultSetHandler.handleResultSets(statement);
  }
}
