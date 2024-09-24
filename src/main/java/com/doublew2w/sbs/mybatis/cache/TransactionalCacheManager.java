package com.doublew2w.sbs.mybatis.cache;

import com.doublew2w.sbs.mybatis.cache.decorators.TransactionalCache;
import java.util.HashMap;
import java.util.Map;

/**
 * 事务缓存，管理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/25 3:20
 * @project: sbs-mybatis
 */
public class TransactionalCacheManager {
  private Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();

  public void clear(Cache cache) {
    getTransactionalCache(cache).clear();
  }

  /** 得到某个TransactionalCache的值 */
  public Object getObject(Cache cache, CacheKey key) {
    return getTransactionalCache(cache).getObject(key);
  }

  public void putObject(Cache cache, CacheKey key, Object value) {
    getTransactionalCache(cache).putObject(key, value);
  }

  /** 提交时全部提交 */
  public void commit() {
    for (TransactionalCache txCache : transactionalCaches.values()) {
      txCache.commit();
    }
  }

  /** 回滚时全部回滚 */
  public void rollback() {
    for (TransactionalCache txCache : transactionalCaches.values()) {
      txCache.rollback();
    }
  }

  private TransactionalCache getTransactionalCache(Cache cache) {
    TransactionalCache txCache = transactionalCaches.get(cache);
    if (txCache == null) {
      txCache = new TransactionalCache(cache);
      transactionalCaches.put(cache, txCache);
    }
    return txCache;
  }
}
