package com.doublew2w.sbs.mybatis.executor.statement;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.executor.parameter.ParameterHandler;
import com.doublew2w.sbs.mybatis.executor.resultset.ResultSetHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
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
  /** 配置类 */
  protected final Configuration configuration;

  /** 结果集处理器 */
  protected final ResultSetHandler resultSetHandler;

  /** 参数处理器 */
  protected final ParameterHandler parameterHandler;

  /** 执行器 */
  protected final Executor executor;

  /** 映射语句 */
  protected final MappedStatement mappedStatement;

  /** 参数对象 */
  protected final Object parameterObject;

  /** 绑定的SQL对象 */
  protected BoundSql boundSql;

  protected final RowBounds rowBounds;

  public BaseStatementHandler(
      Executor executor,
      MappedStatement mappedStatement,
      Object parameterObject,
      ResultHandler resultHandler,
      BoundSql boundSql,
      RowBounds rowBounds) {
    this.configuration = mappedStatement.getConfiguration();
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    // 因为 update 不会传入 boundSql 参数，所以这里要做初始化处理
    if (boundSql == null) {
      boundSql = mappedStatement.getBoundSql(parameterObject);
    }
    this.boundSql = boundSql;
    this.parameterObject = parameterObject;
    this.rowBounds = rowBounds;
    this.parameterHandler =
        configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    this.resultSetHandler =
        configuration.newResultSetHandler(
            executor, mappedStatement, rowBounds, resultHandler, boundSql);
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
