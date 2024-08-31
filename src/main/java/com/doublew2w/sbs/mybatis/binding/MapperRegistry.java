package com.doublew2w.sbs.mybatis.binding;

import cn.hutool.core.lang.ClassScanner;
import com.doublew2w.sbs.mybatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 映射器注册机
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 5:43
 * @project: sbs-mybatis
 */
public class MapperRegistry {

  /** 将已添加的映射器代理加入到 HashMap */
  private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
    }
  }

  public void addMappers(String packageName) {
    Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
    for (Class<?> mapperClass : mapperSet) {
      addMapper(mapperClass);
    }
  }

  /**
   * 注册映射器
   *
   * @param type 映射器
   * @param <T> 映射器类型
   */
  public <T> void addMapper(Class<?> type) {
    /* Mapper 必须是接口才会注册 */
    if (type.isInterface()) {
      if (hasMapper(type)) {
        // 如果重复添加了，报错
        throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
      }
      // 注册映射器代理工厂
      knownMappers.put(type, new MapperProxyFactory<>(type));
    }
  }

  /**
   * 是否扫描该Mapper
   *
   * @param type Mapper
   * @return true - 已注册，false - 未注册
   * @param <T> 映射器类型
   */
  public <T> boolean hasMapper(Class<T> type) {
    return knownMappers.containsKey(type);
  }
}
