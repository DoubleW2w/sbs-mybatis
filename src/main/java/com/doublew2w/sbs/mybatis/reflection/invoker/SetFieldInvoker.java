package com.doublew2w.sbs.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * setter方法调用者
 *
 * @author: DoubleW2w
 * @date: 2024/9/5 23:37
 * @project: sbs-mybatis
 */
public class SetFieldInvoker implements Invoker {
  private final Field field;

  public SetFieldInvoker(Field field) {
    this.field = field;
  }

  @Override
  public Object invoke(Object target, Object[] args) throws Exception {
    try {
      field.set(target, args[0]);
    } catch (IllegalAccessException e) {
      field.setAccessible(true);
      field.set(target, args[0]);
    }
    return null;
  }

  @Override
  public Class<?> getType() {
    return field.getType();
  }
}
