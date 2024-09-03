package com.doublew2w.sbs.mybatis.session;

import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.datasource.druid.DruidDataSourceFactory;
import com.doublew2w.sbs.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.doublew2w.sbs.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.doublew2w.sbs.mybatis.mapping.Environment;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.doublew2w.sbs.mybatis.type.TypeAliasRegistry;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 16:34
 * @project: sbs-mybatis
 */
public class Configuration {
  /** 环境 */
  protected Environment environment;

  /** 映射注册机 */
  protected MapperRegistry mapperRegistry = new MapperRegistry(this);

  /** 映射的语句，存在Map里 */
  protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

  // 类型别名注册机
  protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

  public Configuration() {
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
    typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
    typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
  }

  public void addMappers(String packageName) {
    mapperRegistry.addMappers(packageName);
  }

  public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
  }

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }

  public boolean hasMapper(Class<?> type) {
    return mapperRegistry.hasMapper(type);
  }

  public void addMappedStatement(MappedStatement ms) {
    mappedStatements.put(ms.getId(), ms);
  }

  public MappedStatement getMappedStatement(String id) {
    return mappedStatements.get(id);
  }

  public TypeAliasRegistry getTypeAliasRegistry() {
    return typeAliasRegistry;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public Environment getEnvironment() {
    return environment;
  }
}
