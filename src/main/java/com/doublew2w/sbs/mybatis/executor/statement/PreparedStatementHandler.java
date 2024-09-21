package com.doublew2w.sbs.mybatis.executor.statement;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.executor.keygen.KeyGenerator;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 预处理语句处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 4:18
 * @project: sbs-mybatis
 */
@Slf4j
public class PreparedStatementHandler extends BaseStatementHandler {

  public PreparedStatementHandler(
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
    String sql = boundSql.getSql();
    return connection.prepareStatement(sql);
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    parameterHandler.setParameters(ps);
  }

  @Override
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    log.info("执行查询 query ");
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    return resultSetHandler.<E>handleResultSets(ps);
  }

  @Override
  public int update(Statement statement) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    int rows = ps.getUpdateCount();
    Object parameterObject = boundSql.getParameterObject();
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
    return rows;
  }
}
