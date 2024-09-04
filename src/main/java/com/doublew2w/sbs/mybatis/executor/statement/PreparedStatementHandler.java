package com.doublew2w.sbs.mybatis.executor.statement;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
      BoundSql boundSql) {
    super(executor, mappedStatement, parameterObject, resultHandler, boundSql);
  }

  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    String sql = boundSql.getSql();
    // 尝试创建预编译语句，并捕获异常
    return connection.prepareStatement(sql);
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    log.info(" 参数化处理：{}", JSON.toJSONString(parameterObject));
    PreparedStatement ps = (PreparedStatement) statement;
    ps.setLong(1, Long.parseLong(((Object[]) parameterObject)[0].toString()));
  }

  @Override
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    log.info("执行查询");
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    return resultSetHandler.<E>handleResultSets(ps);
  }
}
