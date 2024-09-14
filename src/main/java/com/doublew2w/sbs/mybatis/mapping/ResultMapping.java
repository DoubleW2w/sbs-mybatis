package com.doublew2w.sbs.mybatis.mapping;

import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.type.JdbcType;
import com.doublew2w.sbs.mybatis.type.TypeHandler;
import lombok.Getter;

/**
 * ResultMap 的核心部分 1. 字段到属性的映射：<result> 或 <id> 2. 复杂的映射关系：<association> <collection> 3. 类型处理：
 * typeHandler
 *
 * @author: DoubleW2w
 * @date: 2024/9/14 16:48
 * @project: sbs-mybatis
 */
@Getter
public class ResultMapping {
  /** 配置类 */
  private Configuration configuration;

  /** 属性 */
  private String property;

  /** 列名 */
  private String column;

  /** java类型 */
  private Class<?> javaType;

  /** jdbc类型 */
  private JdbcType jdbcType;

  /** 类型处理器 */
  private TypeHandler<?> typeHandler;

  ResultMapping() {}

  public static class Builder {
    private ResultMapping resultMapping = new ResultMapping();

    public Builder(
        Configuration configuration, String property, String column, TypeHandler<?> typeHandler) {
      this(configuration, property);
      resultMapping.column = column;
      resultMapping.typeHandler = typeHandler;
    }

    public Builder(Configuration configuration, String property, String column, Class<?> javaType) {
      this(configuration, property);
      resultMapping.column = column;
      resultMapping.javaType = javaType;
    }

    public Builder(Configuration configuration, String property) {
      resultMapping.configuration = configuration;
      resultMapping.property = property;
    }

    public Builder javaType(Class<?> javaType) {
      resultMapping.javaType = javaType;
      return this;
    }

    public Builder column(String column) {
      resultMapping.column = column;
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      resultMapping.jdbcType = jdbcType;
      return this;
    }

    public ResultMapping build() {
      return resultMapping;
    }

    public Builder typeHandler(TypeHandler<?> typeHandler) {
      resultMapping.typeHandler = typeHandler;
      return this;
    }
  }
}
