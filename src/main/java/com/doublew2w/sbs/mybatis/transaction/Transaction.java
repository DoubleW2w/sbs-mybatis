package com.doublew2w.sbs.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务接口
 *
 * @author: DoubleW2w
 * @date: 2024/9/2 2:44
 * @project: sbs-mybatis
 */
public interface Transaction {

  Connection getConnection() throws SQLException;

  void commit() throws SQLException;

  void rollback() throws SQLException;

  void close() throws SQLException;
}
