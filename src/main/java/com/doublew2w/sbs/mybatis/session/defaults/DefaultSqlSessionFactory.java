package com.doublew2w.sbs.mybatis.session.defaults;

import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 5:43
 * @project: sbs-mybatis
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

  /** 映射器注册机 */
  private MapperRegistry mapperRegistry;

  public DefaultSqlSessionFactory(MapperRegistry mapperRegistry) {
    this.mapperRegistry = mapperRegistry;
  }

  @Override
  public SqlSession openSession() {
    return new DefaultSqlSession(mapperRegistry);
  }
}
