package com.doublew2w.sbs.mybatis.transaction.jdbc;

import com.doublew2w.sbs.mybatis.session.TransactionIsolationLevel;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
import com.doublew2w.sbs.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author: DoubleW2w
 * @date: 2024/9/2 2:45
 * @project: sbs-mybatis
 */
public class JdbcTransactionFactory implements TransactionFactory {
  @Override
  public Transaction newTransaction(Connection conn) {
    return new JdbcTransaction(conn);
  }

  @Override
  public Transaction newTransaction(
      DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
    return new JdbcTransaction(dataSource, level, autoCommit);
  }
}
