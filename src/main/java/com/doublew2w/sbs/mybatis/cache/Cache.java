package com.doublew2w.sbs.mybatis.cache;

/**
 * SPI for cache providers.
 *
 * <p>将为每个命名空间创建一个cache实例.
 *
 * <p>缓存实现必须有一个构造函数，该构造函数接收缓存id作为字符串参数.
 *
 * <p>MyBatis will pass the namespace as id to the constructor.
 *
 * <pre>
 * public MyCache(final String id) {
 *   if (id == null) {
 *     throw new IllegalArgumentException("Cache instances require an ID");
 *   }
 *   this.id = id;
 *   initialize();
 * }
 * </pre>
 *
 * @author: DoubleW2w
 * @date: 2024/9/24 2:18
 * @project: sbs-mybatis
 */
public interface Cache {
  /**
   * @return The identifier of this cache
   */
  String getId();

  /**
   * @param key Can be any object but usually it is a {@link CacheKey}
   * @param value The result of a select.
   */
  void putObject(Object key, Object value);

  /**
   * @param key The key
   * @return The object stored in the cache.
   */
  Object getObject(Object key);

  /**
   * As of 3.3.0 this method is only called during a rollback for any previous value that was
   * missing in the cache. This lets any blocking cache to release the lock that may have previously
   * put on the key. A blocking cache puts a lock when a value is null and releases it when the
   * value is back again. This way other threads will wait for the value to be available instead of
   * hitting the database.
   *
   * @param key The key
   * @return Not used
   */
  Object removeObject(Object key);

  /** Clears this cache instance. */
  void clear();

  /**
   * Optional. This method is not called by the core.
   *
   * @return The number of elements stored in the cache (not its capacity).
   */
  int getSize();
}
