package com.doublew2w.sbs.mybatis.transaction;

import com.doublew2w.sbs.mybatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 事务工厂
 *
 * @author: DoubleW2w
 * @date: 2024/9/2 2:41
 * @project: sbs-mybatis
 */
public interface TransactionFactory {
  /**
   * 根据 Connection 创建 Transaction
   *
   * @param conn 现有的数据库连接
   * @return Transaction
   */
  Transaction newTransaction(Connection conn);

  /**
   * 根据数据源和事务隔离级别创建
   *
   * @param dataSource 数据源
   * @param level 事务隔离级别
   * @param autoCommit 自动提交
   * @return Transaction
   */
  Transaction newTransaction(
      DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
