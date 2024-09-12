package com.doublew2w.sbs.mybatis.mapping;

import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.type.JdbcType;
import lombok.Getter;

/**
 * 参数映射 #{property,javaType=int,jdbcType=NUMERIC}
 *
 * <p>将 Java 方法参数中的值映射到 SQL 语句中的占位符（如 #{parameter}）
 *
 * <p>1. 参数映射：将 Java 方法的参数（可能是一个对象或多个参数）映射到 SQL 语句中的占位符。
 *
 * <p>2. 参数类型处理：确保 SQL 语句中的占位符与传入参数的数据类型相匹配。
 *
 * <p>3. 动态 SQL 处理：动态生成的 SQL 语句中的参数映射，使得 SQL 语句能够根据不同的条件动态生成。
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 3:03
 * @project: sbs-mybatis
 */
@Getter
public class ParameterMapping {
  private Configuration configuration;

  // property
  private String property;
  // javaType = int
  private Class<?> javaType = Object.class;
  // jdbcType=NUMERIC
  private JdbcType jdbcType;

  private ParameterMapping() {}

  public static class Builder {

    private ParameterMapping parameterMapping = new ParameterMapping();

    public Builder(Configuration configuration, String property, Class<?> javaType) {
      parameterMapping.configuration = configuration;
      parameterMapping.property = property;
      parameterMapping.javaType = javaType;
    }

    public Builder javaType(Class<?> javaType) {
      parameterMapping.javaType = javaType;
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      parameterMapping.jdbcType = jdbcType;
      return this;
    }

    public ParameterMapping build() {
      return parameterMapping;
    }
  }
}
