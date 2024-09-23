package com.doublew2w.sbs.mybatis.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Getter;

/**
 * @author: DoubleW2w
 * @date: 2024/9/23 19:56
 * @project: sbs-mybatis
 */
@Getter
public class Invocation {
  /** 调用的对象 */
  private final Object target;

  /** 调用的方法 */
  private final Method method;

  /** 调用的方法参数列表 */
  private final Object[] args;

  public Invocation(Object target, Method method, Object[] args) {
    this.target = target;
    this.method = method;
    this.args = args;
  }

  public Object proceed() throws InvocationTargetException, IllegalAccessException {
    return method.invoke(target, args);
  }
}
