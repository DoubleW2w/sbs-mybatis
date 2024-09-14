package com.doublew2w.sbs.mybatis.session;

/**
 * 结果处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/4 3:54
 * @project: sbs-mybatis
 */
public interface ResultHandler {
  /**
   * 处理结果
   */
  void handleResult(ResultContext context);
}
