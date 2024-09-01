package com.doublew2w.sbs.mybatis.type;

import com.doublew2w.sbs.mybatis.io.Resources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author: DoubleW2w
 * @date: 2024/9/2 2:06
 * @project: sbs-mybatis
 */
public class TypeAliasRegistry {
  private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();

  // 内置一些常用的类型
  public TypeAliasRegistry() {
    // 构造函数里注册系统内置的类型别名
    registerAlias("string", String.class);

    // 基本包装类型
    registerAlias("byte", Byte.class);
    registerAlias("long", Long.class);
    registerAlias("short", Short.class);
    registerAlias("int", Integer.class);
    registerAlias("integer", Integer.class);
    registerAlias("double", Double.class);
    registerAlias("float", Float.class);
    registerAlias("boolean", Boolean.class);
  }

  /**
   * 注册类型
   *
   * @param alias 别名
   * @param value 类型
   */
  public void registerAlias(String alias, Class<?> value) {
    if (alias == null) {
      throw new RuntimeException("The parameter alias cannot be null");
    }
    String key = alias.toLowerCase(Locale.ENGLISH);
    if (TYPE_ALIASES.containsKey(key)
        && TYPE_ALIASES.get(key) != null
        && !TYPE_ALIASES.get(key).equals(value)) {
      throw new RuntimeException(
          "The alias '"
              + alias
              + "' is already mapped to the value '"
              + TYPE_ALIASES.get(key).getName()
              + "'.");
    }
    TYPE_ALIASES.put(key, value);
  }

  /**
   * 注册类型
   *
   * @param alias 类型别名
   * @param value 类型
   */
  public void registerAlias(String alias, String value) {
    try {
      registerAlias(alias, Resources.classForName(value));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(
          "Error registering type alias " + alias + " for " + value + ". Cause: " + e, e);
    }
  }

  /** 根据名称解析出类型 */
  public <T> Class<T> resolveAlias(String string) {
    try {
      if (string == null) {
        return null;
      }
      // issue #748
      String key = string.toLowerCase(Locale.ENGLISH);
      Class<T> value;
      if (TYPE_ALIASES.containsKey(key)) {
        value = (Class<T>) TYPE_ALIASES.get(key);
      } else {
        value = (Class<T>) Resources.classForName(string);
      }
      return value;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not resolve type alias '" + string + "'.  Cause: " + e, e);
    }
  }
}
