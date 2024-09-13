package com.doublew2w.sbs.mybatis.type;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: DoubleW2w
 * @date: 2024/9/13 2:45
 * @project: sbs-mybatis
 */
public final class TypeHandlerRegistry {
  private final Map<JdbcType, TypeHandler<?>> JDBC_TYPE_HANDLER_MAP = new EnumMap<>(JdbcType.class);
  private final Map<Type, Map<JdbcType, TypeHandler<?>>> TYPE_HANDLER_MAP = new HashMap<>();
  private final Map<Class<?>, TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<>();

  /** 初始化内置类型处理器 */
  public TypeHandlerRegistry() {
    register(Long.class, new LongTypeHandler());
    register(long.class, new LongTypeHandler());

    register(String.class, new StringTypeHandler());
    register(String.class, JdbcType.CHAR, new StringTypeHandler());
    register(String.class, JdbcType.VARCHAR, new StringTypeHandler());
  }

  private void register(Type javaType, TypeHandler<?> handler) {
    register(javaType, null, handler);
  }

  private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
    if (null != javaType) {
      Map<JdbcType, TypeHandler<?>> map =
          TYPE_HANDLER_MAP.computeIfAbsent(javaType, k -> new HashMap<>());
      map.put(jdbcType, handler);
    }
    ALL_TYPE_HANDLERS_MAP.put(handler.getClass(), handler);
  }

  public boolean hasTypeHandler(Class<?> javaType) {
    return hasTypeHandler(javaType, null);
  }

  public boolean hasTypeHandler(Class<?> javaType, JdbcType jdbcType) {
    return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
  }

  @SuppressWarnings("unchecked")
  public <T> TypeHandler<T> getTypeHandler(Class<T> type, JdbcType jdbcType) {
    return getTypeHandler((Type) type, jdbcType);
  }

  private <T> TypeHandler<T> getTypeHandler(Type type, JdbcType jdbcType) {
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(type);

    if (jdbcHandlerMap == null) {
      return null; // 或者返回一个默认值
    }
    TypeHandler<?> handler = jdbcHandlerMap.get(jdbcType);
    if (handler == null) {
      handler = jdbcHandlerMap.get(null); // 尝试获取默认处理器
    }
    // type drives generics here
    return (TypeHandler<T>) handler;
  }
}
