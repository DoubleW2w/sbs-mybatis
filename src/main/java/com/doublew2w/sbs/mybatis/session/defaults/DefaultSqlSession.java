package com.doublew2w.sbs.mybatis.session.defaults;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.SqlSession;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 5:41
 * @project: sbs-mybatis
 */
public class DefaultSqlSession implements SqlSession {
  private final Configuration configuration;

  public DefaultSqlSession(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public <T> T selectOne(String statement) {
    return (T) ("你被代理了！" + statement);
  }

  @Override
  public <T> T selectOne(String statement, Object parameter) {
    MappedStatement mappedStatement = configuration.getMappedStatement(statement);
    return (T)
        ("你被代理了！"
            + "\n方法："
            + statement
            + "\n入参："
            + JSON.toJSON(parameter)
            + "\n待执行SQL："
            + mappedStatement.getSql());
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
