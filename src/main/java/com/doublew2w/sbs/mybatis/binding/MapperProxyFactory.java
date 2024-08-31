package com.doublew2w.sbs.mybatis.binding;

import com.doublew2w.sbs.mybatis.session.SqlSession;

import java.lang.reflect.Proxy;

/**
 * 映射器代理工厂：负责创建映射器代理类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 4:37
 * @project: sbs-mybatis
 */
public class MapperProxyFactory<T> {
  /** 映射器接口类型 */
  private final Class<T> mapperInterface;

  public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  /**
   * 创建映射器代理类
   *
   * @param sqlSession Sql会话
   * @return 映射器代理类
   */
  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface);
    return mapperInterface.cast(
        Proxy.newProxyInstance(
            mapperInterface.getClassLoader(), new Class[] {mapperInterface}, mapperProxy));
  }
}
