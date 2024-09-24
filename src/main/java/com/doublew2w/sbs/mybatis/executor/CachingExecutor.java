package com.doublew2w.sbs.mybatis.executor;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.cache.Cache;
import com.doublew2w.sbs.mybatis.cache.CacheKey;
import com.doublew2w.sbs.mybatis.cache.TransactionalCacheManager;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 二级缓存执行器
 *
 * @author: DoubleW2w
 * @date: 2024/9/25 3:23
 * @project: sbs-mybatis
 */
@Slf4j
public class CachingExecutor implements Executor {

  private Executor delegate;
  private TransactionalCacheManager tcm = new TransactionalCacheManager();

  public CachingExecutor(Executor delegate) {
    this.delegate = delegate;
    delegate.setExecutorWrapper(this);
  }

  @Override
  public <E> List<E> query(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      CacheKey key,
      BoundSql boundSql)
      throws SQLException {
    Cache cache = ms.getCache();
    if (cache != null) {
      flushCacheIfRequired(ms);
      if (ms.isUseCache() && resultHandler == null) {
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) tcm.getObject(cache, key);
        if (list == null) {
          list = delegate.<E>query(ms, parameter, rowBounds, resultHandler, key, boundSql);
          // cache：缓存队列实现类，FIFO
          // key：哈希值 [mappedStatementId + offset + limit + SQL + queryParams + environment]
          // list：查询的数据
          tcm.putObject(cache, key, list);
        }
        // 打印调试日志，记录二级缓存获取数据
        if (log.isDebugEnabled() && cache.getSize() > 0) {
          log.debug("二级缓存：{}", JSON.toJSONString(list));
        }
        return list;
      }
    }
    return delegate.<E>query(ms, parameter, rowBounds, resultHandler, key, boundSql);
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

  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    return delegate.update(ms, parameter);
  }

  @Override
  public Transaction getTransaction() {
    return delegate.getTransaction();
  }

  @Override
  public void commit(boolean required) throws SQLException {
    delegate.commit(required);
    tcm.commit();
  }

  @Override
  public void rollback(boolean required) throws SQLException {
    try {
      delegate.rollback(required);
    } finally {
      if (required) {
        tcm.rollback();
      }
    }
  }

  @Override
  public void close(boolean forceRollback) {
    try {
      if (forceRollback) {
        tcm.rollback();
      } else {
        tcm.commit();
      }
    } finally {
      delegate.close(forceRollback);
    }
  }

  @Override
  public void clearLocalCache() {
    delegate.clearLocalCache();
  }

  @Override
  public CacheKey createCacheKey(
      MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
  }

  @Override
  public void setExecutorWrapper(Executor executor) {
    throw new UnsupportedOperationException("This method should not be called");
  }

  private void flushCacheIfRequired(MappedStatement ms) {
    Cache cache = ms.getCache();
    if (cache != null && ms.isFlushCacheRequired()) {
      tcm.clear(cache);
    }
  }
}
