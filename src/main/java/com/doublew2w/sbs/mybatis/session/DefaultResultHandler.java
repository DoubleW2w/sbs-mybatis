package com.doublew2w.sbs.mybatis.session;

import com.doublew2w.sbs.mybatis.reflection.factory.ObjectFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认的结果处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/14 17:36
 * @project: sbs-mybatis
 */
public class DefaultResultHandler implements ResultHandler {

  private final List<Object> list;

  public DefaultResultHandler() {
    this.list = new ArrayList<>();
  }

  /** 通过 ObjectFactory 反射工具类，产生特定的 List */
  @SuppressWarnings("unchecked")
  public DefaultResultHandler(ObjectFactory objectFactory) {
    this.list = objectFactory.create(List.class);
  }

  @Override
  public void handleResult(ResultContext context) {
    list.add(context.getResultObject());
  }

  public List<Object> getResultList() {
    return list;
  }
}
