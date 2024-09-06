package com.doublew2w.sbs.mybatis.reflection.invoker;

/**
 * 调用者
 *
 * <p>1. 完成Field 字段的 get/set 还有普通的 Method 的调用
 *
 * <p>2. 调用者的实现包装成调用策略，统一接口不同策略不同的实现类。
 *
 * @author: DoubleW2w
 * @date: 2024/9/5 3:34
 * @project: sbs-mybatis
 */
public interface Invoker {
  Object invoke(Object target, Object[] args) throws Exception;

  Class<?> getType();
}
