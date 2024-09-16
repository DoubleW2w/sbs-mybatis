package com.doublew2w.sbs.mybatis.executor;

import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
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

  private boolean closed;

  protected BaseExecutor(Configuration configuration, Transaction transaction) {
    this.configuration = configuration;
    this.transaction = transaction;
    this.wrapper = this;
  }

  @Override
  public <E> List<E> query(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      BoundSql boundSql) {
    log.info("executing an query");
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    return doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
  }

  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    log.info("executing an update");
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    return doUpdate(ms, parameter);
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
    if (required) {
      transaction.commit();
    }
  }

  @Override
  public void rollback(boolean required) throws SQLException {
    if (!closed) {
      if (required) {
        transaction.rollback();
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

  protected void closeStatement(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException ignore) {
      }
    }
  }
}
