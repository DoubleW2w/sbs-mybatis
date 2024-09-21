package com.doublew2w.sbs.mybatis.executor.keygen;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import java.sql.Statement;

/**
 * @author: DoubleW2w
 * @date: 2024/9/19 23:02
 * @project: sbs-mybatis
 */
public interface KeyGenerator {

  void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

  void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
