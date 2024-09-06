package com.doublew2w.sbs.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * getter方法调用者
 *
 * @author: DoubleW2w
 * @date: 2024/9/5 23:36
 * @project: sbs-mybatis
 */
public class GetFieldInvoker implements Invoker {
  private final Field field;

  public GetFieldInvoker(Field field) {
    this.field = field;
  }

  @Override
  public Object invoke(Object target, Object[] args) throws Exception {
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      field.setAccessible(true);
      return field.get(target);
    }
  }

  @Override
  public Class<?> getType() {
    return field.getType();
  }
}
