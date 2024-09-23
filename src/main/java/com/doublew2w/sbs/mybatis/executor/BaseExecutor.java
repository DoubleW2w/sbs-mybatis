package com.doublew2w.sbs.mybatis.executor;

import com.doublew2w.sbs.mybatis.cache.CacheKey;
import com.doublew2w.sbs.mybatis.cache.impl.PerpetualCache;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.ParameterMapping;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.LocalCacheScope;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
import com.doublew2w.sbs.mybatis.type.TypeHandlerRegistry;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 执行器抽象基类
 *
 * <p>默认实现步骤、定义模板模式的过程包装
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 3:53
 * @project: sbs-mybatis
 */
@Slf4j
public abstract class BaseExecutor implements Executor {

  protected Configuration configuration;
  protected Transaction transaction;
  protected Executor wrapper;
  // 本地缓存
  protected PerpetualCache localCache;
  // 查询堆栈
  protected int queryStack = 0;
  private boolean closed;

  protected BaseExecutor(Configuration configuration, Transaction transaction) {
    this.configuration = configuration;
    this.transaction = transaction;
    this.wrapper = this;
    this.localCache = new PerpetualCache("LocalCache");
  }

  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    // 进行 CUD都会清理缓存
    clearLocalCache();
    return doUpdate(ms, parameter);
  }

  @Override
  public <E> List<E> query(
      MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
      throws SQLException {
    // 1. 获取绑定SQL
    BoundSql boundSql = ms.getBoundSql(parameter);
    // 2. 创建缓存Key
    CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
    return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
  }

  /** 执行查询操作，首先尝试从本地缓存中获取结果，如果本地缓存中没有所需数据， 则从数据库中查询并缓存结果供后续使用。 */
  @Override
  public <E> List<E> query(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      CacheKey key,
      BoundSql boundSql)
      throws SQLException {
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    // 清理局部缓存，查询堆栈为0则清理。queryStack 避免递归调用清理
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      clearLocalCache();
    }
    List<E> list;
    try {
      queryStack++;
      // 根据cacheKey从localCache中查询数据
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
      if (list == null) {
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
      }
    } finally {
      queryStack--;
    }
    // 再次检查查询堆栈，如果为0且本地缓存作用域为STATEMENT，则清理本地缓存
    if (queryStack == 0) {
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        clearLocalCache();
      }
    }
    return list;
  }

  @Override
  public Transaction getTransaction() {
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    return transaction;
  }

  @Override
  public void commit(boolean required) throws SQLException {
    if (closed) {
      throw new RuntimeException("Cannot commit, transaction is already closed");
    }
    clearLocalCache();
    if (required) {
      transaction.commit();
    }
  }

  @Override
  public void rollback(boolean required) throws SQLException {
    if (!closed) {
      try {
        clearLocalCache();
      } finally {
        if (required) {
          transaction.rollback();
        }
      }
    }
  }

  @Override
  public void close(boolean forceRollback) {
    try {
      try {
        rollback(forceRollback);
      } finally {
        transaction.close();
      }
    } catch (SQLException e) {
      log.warn("Unexpected exception on closing transaction.  Cause: " + e);
    } finally {
      transaction = null;
      closed = true;
    }
  }

  @Override
  public void clearLocalCache() {
    if (!closed) {
      localCache.clear();
    }
  }

  @Override
  public CacheKey createCacheKey(
      MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    CacheKey cacheKey = new CacheKey();
    cacheKey.update(ms.getId());
    cacheKey.update(rowBounds.getOffset());
    cacheKey.update(rowBounds.getLimit());
    cacheKey.update(boundSql.getSql());
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
    for (ParameterMapping parameterMapping : parameterMappings) {
      Object value;
      String propertyName = parameterMapping.getProperty();
      if (boundSql.hasAdditionalParameter(propertyName)) {
        value = boundSql.getAdditionalParameter(propertyName);
      } else if (parameterObject == null) {
        value = null;
      } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
        value = parameterObject;
      } else {
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        value = metaObject.getValue(propertyName);
      }
      cacheKey.update(value);
    }
    if (configuration.getEnvironment() != null) {
      cacheKey.update(configuration.getEnvironment().getId());
    }
    return cacheKey;
  }

  /** 真正的具体实现交给子类 */
  protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;

  /**
   * 真正查询的逻辑实现
   *
   * @param ms 映射语句
   * @param parameter 参数
   * @param resultHandler 结果处理器
   * @param boundSql 绑定SQL
   * @return 结果集合
   * @param <E> 结果类型
   */
  protected abstract <E> List<E> doQuery(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      BoundSql boundSql);

  protected void closeStatement(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException ignore) {
      }
    }
  }

  private <E> List<E> queryFromDatabase(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      CacheKey key,
      BoundSql boundSql)
      throws SQLException {
    List<E> list;
    localCache.putObject(key, ExecutionPlaceholder.EXECUTION_PLACEHOLDER);
    try {
      list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
      localCache.removeObject(key);
    }
    // 存入缓存
    localCache.putObject(key, list);
    return list;
  }
}
