package com.doublew2w.sbs.mybatis.binding;

import com.doublew2w.sbs.mybatis.session.SqlSession;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 映射器代理类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 4:33
 * @project: sbs-mybatis
 */
@Slf4j
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -8932997989399317814L;

  /** Sql会话 */
  private SqlSession sqlSession;

  /** 映射器接口类型 */
  private final Class<T> mapperInterface;

  /** */
  private final Map<Method, MapperMethod> methodCache;

  public MapperProxy(
      SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 如果是 Object 类
    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    } else {
      final MapperMethod mapperMethod = cachedMapperMethod(method);
      log.info(
          "into invoke(), mapperInterface: {}, cachedMapperMethod: {}",
          mapperInterface.getName(),
          method.getName());
      return mapperMethod.execute(sqlSession, args);
    }
  }

  /** 去缓存中找MapperMethod */
  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
      // 找不到才去new
      mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }
}
