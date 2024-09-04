package com.doublew2w.sbs.mybatis.executor;

import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.ResultHandler;

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
      MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql);
}
