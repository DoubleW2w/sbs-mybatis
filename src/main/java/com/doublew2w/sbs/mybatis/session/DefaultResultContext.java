package com.doublew2w.sbs.mybatis.session;


/**
 * 默认的结果集上下文
 *
 * @author: DoubleW2w
 * @date: 2024/9/14 17:49
 * @project: sbs-mybatis
 */
public class DefaultResultContext implements ResultContext {
  private Object resultObject;
  private int resultCount;

  public DefaultResultContext() {
    this.resultObject = null;
    this.resultCount = 0;
  }

  @Override
  public Object getResultObject() {
    return resultObject;
  }

  @Override
  public int getResultCount() {
    return resultCount;
  }

  public void nextResultObject(Object resultObject) {
    resultCount++;
    this.resultObject = resultObject;
  }
}
