package com.doublew2w.sbs.mybatis.builder;

import com.doublew2w.sbs.mybatis.mapping.ParameterMapping;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.mapping.StaticSqlSource;
import com.doublew2w.sbs.mybatis.parsing.GenericTokenParser;
import com.doublew2w.sbs.mybatis.parsing.TokenHandler;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL 源码构建器
 *
 * <p>1. 构建 SqlSource 对象：根据提供的 SQL 语句和参数映射构建一个 SqlSource 对象。
 *
 * <p>2. 解析 SQL 语句： 解析 SQL 语句中的占位符（如 #{param}）并将其与参数类型和映射进行配对。
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 3:09
 * @project: sbs-mybatis
 */
public class SqlSourceBuilder extends BaseBuilder {
  private static final String parameterProperties =
      "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

  public SqlSourceBuilder(Configuration configuration) {
    super(configuration);
  }

  public SqlSource parse(
      String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
    ParameterMappingTokenHandler handler =
        new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    String sql = parser.parse(originalSql);
    // 返回静态 SQL
    return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
  }

  private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

    private List<ParameterMapping> parameterMappings = new ArrayList<>();
    private Class<?> parameterType;
    private MetaObject metaParameters;

    public ParameterMappingTokenHandler(
        Configuration configuration,
        Class<?> parameterType,
        Map<String, Object> additionalParameters) {
      super(configuration);
      this.parameterType = parameterType;
      this.metaParameters = configuration.newMetaObject(additionalParameters);
    }

    public List<ParameterMapping> getParameterMappings() {
      return parameterMappings;
    }

    @Override
    public String handleToken(String content) {
      parameterMappings.add(buildParameterMapping(content));
      return "?";
    }

    // 构建参数映射
    private ParameterMapping buildParameterMapping(String content) {
      // 先解析参数映射,就是转化成一个 HashMap | #{favouriteSection,jdbcType=VARCHAR}
      Map<String, String> propertiesMap = new ParameterExpression(content);
      String property = propertiesMap.get("property");
      Class<?> propertyType = parameterType;
      ParameterMapping.Builder builder =
          new ParameterMapping.Builder(configuration, property, propertyType);
      return builder.build();
    }
  }
}
