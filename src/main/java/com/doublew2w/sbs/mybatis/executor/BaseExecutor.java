package com.doublew2w.sbs.mybatis.executor;

import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
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
  protected Executor wrapper;

  private boolean closed;

  protected BaseExecutor(Configuration configuration) {
    this.configuration = configuration;
    this.wrapper = this;
  }

  @Override
  public <E> List<E> query(
      MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    return doQuery(ms, parameter, resultHandler, boundSql);
  }

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
      MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql);
}
