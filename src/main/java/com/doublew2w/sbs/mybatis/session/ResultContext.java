package com.doublew2w.sbs.mybatis.session;

/**
 * 结果上下文
 *
 * @author: DoubleW2w
 * @date: 2024/9/14 17:43
 * @project: sbs-mybatis
 */
public interface ResultContext {
  /** 获取结果 */
  Object getResultObject();

  /** 获取记录数 */
  int getResultCount();
}
