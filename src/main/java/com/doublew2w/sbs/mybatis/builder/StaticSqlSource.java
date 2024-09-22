package com.doublew2w.sbs.mybatis.builder;

import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.ParameterMapping;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.session.Configuration;
import java.util.List;

/**
 * 静态SQL源码
 *
 * <p>将一个静态的 SQL 语句（即在编译时确定的 SQL 语句）封装成一个 SqlSource 对象
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 3:00
 * @project: sbs-mybatis
 */
public class StaticSqlSource implements SqlSource {

  private String sql;
  private List<ParameterMapping> parameterMappings;
  private Configuration configuration;

  public StaticSqlSource(Configuration configuration, String sql) {
    this(configuration, sql, null);
  }

  public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.configuration = configuration;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    return new BoundSql(configuration, sql, parameterMappings, parameterObject);

  }
}
