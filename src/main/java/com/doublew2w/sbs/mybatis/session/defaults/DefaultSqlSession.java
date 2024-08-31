package com.doublew2w.sbs.mybatis.session.defaults;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.session.SqlSession;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 5:41
 * @project: sbs-mybatis
 */
public class DefaultSqlSession implements SqlSession {
  /** 映射器注册机 */
  private MapperRegistry mapperRegistry;

  public DefaultSqlSession(MapperRegistry mapperRegistry) {
    this.mapperRegistry = mapperRegistry;
  }

  @Override
  public <T> T selectOne(String statement) {
    return (T) ("你被代理了！" + statement);
  }

  @Override
  public <T> T selectOne(String statement, Object parameter) {
    return (T) ("你被代理了！" + "方法：" + statement + " 入参：" + JSON.toJSON(parameter));
  }

  @Override
  public <T> T getMapper(Class<T> type) {
    return mapperRegistry.getMapper(type, this);
  }
}
