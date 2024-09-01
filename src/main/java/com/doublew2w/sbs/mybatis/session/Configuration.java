package com.doublew2w.sbs.mybatis.session;

import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 16:34
 * @project: sbs-mybatis
 */
public class Configuration {
  /** 映射注册机 */
  protected MapperRegistry mapperRegistry = new MapperRegistry(this);

  /** 映射的语句，存在Map里 */
  protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

  public void addMappers(String packageName) {
    mapperRegistry.addMappers(packageName);
  }

  public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
  }

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }

  public boolean hasMapper(Class<?> type) {
    return mapperRegistry.hasMapper(type);
  }

  public void addMappedStatement(MappedStatement ms) {
    mappedStatements.put(ms.getId(), ms);
  }

  public MappedStatement getMappedStatement(String id) {
    return mappedStatements.get(id);
  }
}
