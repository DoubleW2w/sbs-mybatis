package com.doublew2w.sbs.mybatis.plugin;

import java.util.Properties;

/**
 * 插件接口
 *
 * @author: DoubleW2w
 * @date: 2024/9/23 19:54
 * @project: sbs-mybatis
 */
public interface Interceptor {
  Object intercept(Invocation invocation) throws Throwable;
  default Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  default void setProperties(Properties properties) {
    // NOP
  }
}
