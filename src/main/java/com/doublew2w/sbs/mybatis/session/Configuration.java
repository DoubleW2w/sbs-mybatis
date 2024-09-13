package com.doublew2w.sbs.mybatis.session;

import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.datasource.druid.DruidDataSourceFactory;
import com.doublew2w.sbs.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.doublew2w.sbs.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.executor.SimpleExecutor;
import com.doublew2w.sbs.mybatis.executor.parameter.ParameterHandler;
import com.doublew2w.sbs.mybatis.executor.resultset.DefaultResultSetHandler;
import com.doublew2w.sbs.mybatis.executor.resultset.ResultSetHandler;
import com.doublew2w.sbs.mybatis.executor.statement.PreparedStatementHandler;
import com.doublew2w.sbs.mybatis.executor.statement.StatementHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.Environment;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.reflection.factory.DefaultObjectFactory;
import com.doublew2w.sbs.mybatis.reflection.factory.ObjectFactory;
import com.doublew2w.sbs.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.doublew2w.sbs.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriverRegistry;
import com.doublew2w.sbs.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.doublew2w.sbs.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.doublew2w.sbs.mybatis.type.TypeAliasRegistry;
import com.doublew2w.sbs.mybatis.type.TypeHandlerRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * 配置类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 16:34
 * @project: sbs-mybatis
 */
public class Configuration {
  /** 环境 */
  @Getter @Setter protected Environment environment;

  /** 映射注册机 */
  protected MapperRegistry mapperRegistry = new MapperRegistry(this);

  /** 映射的语句，存在Map里 */
  protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

  // 类型别名注册机
  @Getter protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
  @Getter protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

  // 类型处理器注册机
  @Getter protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
  // 对象工厂和对象包装器工厂
  protected ObjectFactory objectFactory = new DefaultObjectFactory();
  protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

  protected final Set<String> loadedResources = new HashSet<>();
  @Getter protected String databaseId;

  public Configuration() {
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
    typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
    typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

    languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
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

  public ResultSetHandler newResultSetHandler(
      Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
    return new DefaultResultSetHandler(executor, mappedStatement, boundSql);
  }

  /** 生产执行器 */
  public Executor newExecutor() {
    return new SimpleExecutor(this);
  }

  /** 创建语句处理器 */
  public StatementHandler newStatementHandler(
      Executor executor,
      MappedStatement mappedStatement,
      Object parameter,
      ResultHandler resultHandler,
      BoundSql boundSql) {
    return new PreparedStatementHandler(
        executor, mappedStatement, parameter, resultHandler, boundSql);
  }

  // 创建元对象
  public MetaObject newMetaObject(Object object) {
    return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
  }

  public boolean isResourceLoaded(String resource) {
    return loadedResources.contains(resource);
  }

  public void addLoadedResource(String resource) {
    loadedResources.add(resource);
  }

  public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    // 创建参数处理器
    ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
    // 插件的一些参数，也是在这里处理，暂时不添加这部分内容 interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
  }

  public LanguageDriver getDefaultScriptingLanguageInstance() {
    return  languageRegistry.getDefaultDriver();
  }
}
