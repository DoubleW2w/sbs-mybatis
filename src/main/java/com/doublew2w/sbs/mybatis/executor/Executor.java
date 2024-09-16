package com.doublew2w.sbs.mybatis.executor;

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

  /** 查询 */
  <E> List<E> query(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      BoundSql boundSql);

  /** 更新操作 */
  int update(MappedStatement ms, Object parameter) throws SQLException;

  Transaction getTransaction();

  void commit(boolean required) throws SQLException;

  void rollback(boolean required) throws SQLException;

  void close(boolean forceRollback);
}
