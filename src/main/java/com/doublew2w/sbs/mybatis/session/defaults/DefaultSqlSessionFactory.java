package com.doublew2w.sbs.mybatis.session.defaults;

import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
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
    // 创建执行器
    final Executor executor = configuration.newExecutor();
    // 创建DefaultSqlSession
    return new DefaultSqlSession(configuration, executor);
  }
}
