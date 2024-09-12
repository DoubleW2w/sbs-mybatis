package com.doublew2w.sbs.mybatis.mapping;

/**
 * SQL源码
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 2:38
 * @project: sbs-mybatis
 */
public interface SqlSource {
  BoundSql getBoundSql(Object parameterObject);
}
