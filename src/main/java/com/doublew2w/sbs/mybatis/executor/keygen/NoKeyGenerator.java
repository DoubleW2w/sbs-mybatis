package com.doublew2w.sbs.mybatis.executor.keygen;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import java.sql.Statement;

/**
 * @author: DoubleW2w
 * @date: 2024/9/19 23:03
 * @project: sbs-mybatis
 */
public class NoKeyGenerator implements KeyGenerator {

  public static final NoKeyGenerator INSTANCE = new NoKeyGenerator();

  @Override
  public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    // Do Nothing
  }

  @Override
  public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    // Do Nothing
  }
}
