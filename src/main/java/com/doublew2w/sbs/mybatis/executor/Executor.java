package com.doublew2w.sbs.mybatis.executor;

import com.doublew2w.sbs.mybatis.cache.CacheKey;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
import java.sql.SQLException;
import java.util.List;

/**
 * 执行器
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 3:55
 * @project: sbs-mybatis
 */
public interface Executor {
  ResultHandler NO_RESULT_HANDLER = null;

  // 查询，含缓存
  <E> List<E> query(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      CacheKey key,
      BoundSql boundSql)
      throws SQLException;

  // 查询
  <E> List<E> query(
      MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
      throws SQLException;

  /** 更新操作 */
  int update(MappedStatement ms, Object parameter) throws SQLException;

  Transaction getTransaction();

  void commit(boolean required) throws SQLException;

  void rollback(boolean required) throws SQLException;

  void close(boolean forceRollback);

  /** 清理Session缓存 */
  void clearLocalCache();

  /**
   * 创建缓存 Key
   *
   * @param ms 映射语句
   * @param parameterObject 映射语句参数
   * @param rowBounds 分页记录
   * @param boundSql 绑定SQL
   * @return 缓存Key
   */
  CacheKey createCacheKey(
      MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

  /**
   * 设置执行器包装器
   *
   * @param executor 执行器
   */
  void setExecutorWrapper(Executor executor);
}
