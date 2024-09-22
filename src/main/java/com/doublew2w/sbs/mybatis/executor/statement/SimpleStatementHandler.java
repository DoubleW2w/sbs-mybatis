package com.doublew2w.sbs.mybatis.executor.statement;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.SelectKeyGenerator;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

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
      BoundSql boundSql,
      RowBounds rowBounds) {
    super(executor, mappedStatement, parameterObject, resultHandler, boundSql, rowBounds);
  }

  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    return connection.createStatement();
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    // empty
  }

  @Override
  public int update(Statement statement) throws SQLException {
    String sql = boundSql.getSql();
    Object parameterObject = boundSql.getParameterObject();
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    int rows;
    if (keyGenerator instanceof Jdbc3KeyGenerator) {
      statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
      rows = statement.getUpdateCount();
      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
    } else if (keyGenerator instanceof SelectKeyGenerator) {
      statement.execute(sql);
      rows = statement.getUpdateCount();
      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
    } else {
      statement.execute(sql);
      rows = statement.getUpdateCount();
    }
    return rows;
  }

  @Override
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    String sql = boundSql.getSql();
    log.info("sql:{}", sql);
    statement.execute(sql);
    return resultSetHandler.handleResultSets(statement);
  }
}
