package com.doublew2w.sbs.mybatis.session;

import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.cache.Cache;
import com.doublew2w.sbs.mybatis.cache.decorators.FifoCache;
import com.doublew2w.sbs.mybatis.cache.impl.PerpetualCache;
import com.doublew2w.sbs.mybatis.datasource.druid.DruidDataSourceFactory;
import com.doublew2w.sbs.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.doublew2w.sbs.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.doublew2w.sbs.mybatis.executor.CachingExecutor;
import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.executor.SimpleExecutor;
import com.doublew2w.sbs.mybatis.executor.keygen.KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.parameter.ParameterHandler;
import com.doublew2w.sbs.mybatis.executor.resultset.DefaultResultSetHandler;
import com.doublew2w.sbs.mybatis.executor.resultset.ResultSetHandler;
import com.doublew2w.sbs.mybatis.executor.statement.PreparedStatementHandler;
import com.doublew2w.sbs.mybatis.executor.statement.StatementHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.Environment;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.ResultMap;
import com.doublew2w.sbs.mybatis.plugin.Interceptor;
import com.doublew2w.sbs.mybatis.plugin.InterceptorChain;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.reflection.factory.DefaultObjectFactory;
import com.doublew2w.sbs.mybatis.reflection.factory.ObjectFactory;
import com.doublew2w.sbs.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.doublew2w.sbs.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriverRegistry;
import com.doublew2w.sbs.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
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
  // 默认启用缓存，cacheEnabled = true/false
  protected boolean cacheEnabled = true;

  /** 映射注册机 */
  protected MapperRegistry mapperRegistry = new MapperRegistry(this);

  /** 映射的语句，存在Map里 */
  protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

  // 缓存,存在Map里
  protected final Map<String, Cache> caches = new HashMap<>();
  @Getter protected final Map<String, KeyGenerator> keyGenerators = new HashMap<>();

  // 结果映射，存在Map里
  protected final Map<String, ResultMap> resultMaps = new HashMap<>();
  // 类型别名注册机
  @Getter protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
  @Getter protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

  // 类型处理器注册机
  @Getter protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
  // 对象工厂和对象包装器工厂
  @Getter protected ObjectFactory objectFactory = new DefaultObjectFactory();
  protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
  // 插件拦截器链
  protected final InterceptorChain interceptorChain = new InterceptorChain();

  protected final Set<String> loadedResources = new HashSet<>();
  @Setter protected boolean useGeneratedKeys;
  @Getter protected String databaseId;
  // 缓存机制，默认不配置的情况是 SESSION
  @Getter @Setter protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;

  public Configuration() {
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
    typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
    typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

    typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
    typeAliasRegistry.registerAlias("FIFO", FifoCache.class);

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
      Executor executor,
      MappedStatement mappedStatement,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      BoundSql boundSql) {
    return new DefaultResultSetHandler(
        executor, mappedStatement, resultHandler, rowBounds, boundSql);
  }

  /** 生产执行器 */
  public Executor newExecutor(Transaction transaction) {
    Executor executor = new SimpleExecutor(this, transaction);
    // 配置开启缓存，创建 CachingExecutor(默认就是有缓存)装饰者模式
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    return executor;
  }

  /** 创建语句处理器 */
  public StatementHandler newStatementHandler(
      Executor executor,
      MappedStatement mappedStatement,
      Object parameterObject,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      BoundSql boundSql) {
    StatementHandler statementHandler =
        new PreparedStatementHandler(
            executor, mappedStatement, parameterObject, resultHandler, boundSql, rowBounds);
    // 嵌入插件，代理对象
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
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

  public ParameterHandler newParameterHandler(
      MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    // 创建参数处理器
    ParameterHandler parameterHandler =
        mappedStatement
            .getLang()
            .createParameterHandler(mappedStatement, parameterObject, boundSql);
    // 插件的一些参数，也是在这里处理，暂时不添加这部分内容 interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
  }

  public LanguageDriver getDefaultScriptingLanguageInstance() {
    return languageRegistry.getDefaultDriver();
  }

  public ResultMap getResultMap(String id) {
    return resultMaps.get(id);
  }

  public void addResultMap(ResultMap resultMap) {
    resultMaps.put(resultMap.getId(), resultMap);
  }

  public boolean isUseGeneratedKeys() {
    return useGeneratedKeys;
  }

  public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
    keyGenerators.put(id, keyGenerator);
  }

  public boolean hasKeyGenerator(String id) {
    return keyGenerators.containsKey(id);
  }

  public KeyGenerator getKeyGenerator(String id) {
    return keyGenerators.get(id);
  }

  public void addInterceptor(Interceptor interceptorInstance) {
    interceptorChain.addInterceptor(interceptorInstance);
  }

  public boolean isCacheEnabled() {
    return cacheEnabled;
  }

  public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  public void addCache(Cache cache) {
    caches.put(cache.getId(), cache);
  }

  public Cache getCache(String id) {
    return caches.get(id);
  }
}
