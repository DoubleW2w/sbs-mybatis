package com.doublew2w.sbs.mybatis.spring;

import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

/**
 * Mapper 工厂对象
 *
 * @author: DoubleW2w
 * @date: 2024/9/26 1:03
 * @project: sbs-mybatis
 */
@Slf4j
public class MapperFactoryBean<T> implements FactoryBean<T> {

  private Class<T> mapperInterface;
  private SqlSessionFactory sqlSessionFactory;

  public MapperFactoryBean(Class<T> mapperInterface, SqlSessionFactory sqlSessionFactory) {
    log.info("{},构造函数:执行", mapperInterface);
    this.mapperInterface = mapperInterface;
    this.sqlSessionFactory = sqlSessionFactory;
  }

  @Override
  public T getObject() throws Exception {
    return sqlSessionFactory.openSession().getMapper(mapperInterface);
  }

  @Override
  public Class<?> getObjectType() {
    return mapperInterface;
  }
}
