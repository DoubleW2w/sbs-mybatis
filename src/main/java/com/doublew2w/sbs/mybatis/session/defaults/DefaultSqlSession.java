package com.doublew2w.sbs.mybatis.session.defaults;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import java.sql.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 5:41
 * @project: sbs-mybatis
 */
@Slf4j
public class DefaultSqlSession implements SqlSession {
  private Configuration configuration;
  private Executor executor;

  public DefaultSqlSession(Configuration configuration, Executor executor) {
    this.configuration = configuration;
    this.executor = executor;
  }

  @Override
  public <T> T selectOne(String statement) {
    return (T) ("你被代理了！" + statement);
  }

  @Override
  public <T> T selectOne(String statement, Object parameter) {
    log.info("执行查询 statement：{} parameter：{}", statement, JSON.toJSONString(parameter));
    MappedStatement ms = configuration.getMappedStatement(statement);
    List<T> list = executor.query(ms, parameter, Executor.NO_RESULT_HANDLER, ms.getSqlSource().getBoundSql(parameter));
    return list.get(0);
  }

  @Override
  public <T> T getMapper(Class<T> type) {
    return configuration.getMapper(type, this);
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }
}
