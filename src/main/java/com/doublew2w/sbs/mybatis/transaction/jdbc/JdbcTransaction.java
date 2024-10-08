package com.doublew2w.sbs.mybatis.transaction.jdbc;

import com.doublew2w.sbs.mybatis.session.TransactionIsolationLevel;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * @author: DoubleW2w
 * @date: 2024/9/2 2:45
 * @project: sbs-mybatis
 */
public class JdbcTransaction implements Transaction {
  protected Connection connection;
  protected DataSource dataSource;
  protected TransactionIsolationLevel level = TransactionIsolationLevel.NONE;
  protected boolean autoCommit;

  public JdbcTransaction(
      DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
    this.dataSource = dataSource;
    this.level = level;
    this.autoCommit = autoCommit;
  }

  public JdbcTransaction(Connection connection) {
    this.connection = connection;
  }

  @Override
  public Connection getConnection() throws SQLException {
    if (null != connection) {
      return connection;
    }
    connection = dataSource.getConnection();
    connection.setTransactionIsolation(level.getLevel());
    connection.setAutoCommit(autoCommit);
    return connection;
  }

  @Override
  public void commit() throws SQLException {
    if (connection != null && !connection.getAutoCommit()) {
      connection.commit();
    }
  }

  @Override
  public void rollback() throws SQLException {
    if (connection != null && !connection.getAutoCommit()) {
      connection.rollback();
    }
  }

  @Override
  public void close() throws SQLException {
    if (connection != null && !connection.getAutoCommit()) {
      connection.close();
    }
  }
}
