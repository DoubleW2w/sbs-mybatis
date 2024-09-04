package com.doublew2w.sbs.mybatis.executor.statement;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.executor.resultset.ResultSetHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 语句处理器基类
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 4:17
 * @project: sbs-mybatis
 */
public abstract class BaseStatementHandler implements StatementHandler {
  protected final Configuration configuration;
  protected final ResultSetHandler resultSetHandler;

  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final Object parameterObject;
  protected BoundSql boundSql;

  public BaseStatementHandler(
      Executor executor,
      MappedStatement mappedStatement,
      Object parameterObject,
      ResultHandler resultHandler,
      BoundSql boundSql) {
    this.configuration = mappedStatement.getConfiguration();
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.boundSql = boundSql;

    this.parameterObject = parameterObject;
    this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, boundSql);
  }

  protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

  @Override
  public Statement prepare(Connection connection) throws SQLException {
    Statement statement = null;
    try {
      // 实例化 Statement
      statement = instantiateStatement(connection);
      // 参数设置，可以被抽取，提供配置
      statement.setQueryTimeout(350);
      statement.setFetchSize(10000);
      return statement;
    } catch (Exception e) {
      throw new RuntimeException("Error preparing statement.  Cause: " + e, e);
    }
  }
}
