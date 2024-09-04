package com.doublew2w.sbs.mybatis.executor;

import com.doublew2w.sbs.mybatis.executor.statement.StatementHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.Environment;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.List;

/**
 * 简单执行器
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 3:58
 * @project: sbs-mybatis
 */
@Slf4j
public class SimpleExecutor extends BaseExecutor {
  public SimpleExecutor(Configuration configuration) {
    super(configuration);
  }

  @Override
  protected <E> List<E> doQuery(
      MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
    try {
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler =
          configuration.newStatementHandler(this, ms, parameter, resultHandler, boundSql);
      Environment environment = configuration.getEnvironment();
      Connection connection = environment.getDataSource().getConnection();
      Statement stmt = handler.prepare(connection);
      handler.parameterize(stmt);
      return handler.query(stmt, resultHandler);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }
}
