package com.doublew2w.sbs.mybatis.session.defaults;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.Environment;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;
import com.doublew2w.sbs.mybatis.session.TransactionIsolationLevel;
import com.doublew2w.sbs.mybatis.transaction.Transaction;
import com.doublew2w.sbs.mybatis.transaction.TransactionFactory;
import java.sql.SQLException;

/**
 * 默认的Sql会话工厂
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 5:43
 * @project: sbs-mybatis
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

  private final Configuration configuration;

  public DefaultSqlSessionFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public SqlSession openSession() {
    Transaction tx = null;
    try {
      final Environment environment = configuration.getEnvironment();
      TransactionFactory transactionFactory = environment.getTransactionFactory();
      tx =
          transactionFactory.newTransaction(
              environment.getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);
      // 创建执行器
      final Executor executor = configuration.newExecutor(tx);
      // 创建DefaultSqlSession
      return new DefaultSqlSession(configuration, executor);
    } catch (Exception e) {
      try {
        assert tx != null;
        tx.close();
      } catch (SQLException ignore) {
      }
      throw new RuntimeException("Error opening session.  Cause: " + e);
    }
  }
}
