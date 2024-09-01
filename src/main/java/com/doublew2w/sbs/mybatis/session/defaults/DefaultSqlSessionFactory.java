package com.doublew2w.sbs.mybatis.session.defaults;

import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 5:43
 * @project: sbs-mybatis
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

  private final Configuration configuration;

  public DefaultSqlSessionFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public SqlSession openSession() {
    return new DefaultSqlSession(configuration);
  }
}
