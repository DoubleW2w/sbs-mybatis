package com.doublew2w.sbs.mybatis.plugin;

import java.lang.annotation.*;

/**
 * @author: DoubleW2w
 * @date: 2024/9/23 20:03
 * @project: sbs-mybatis
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {
  /**
   * Returns method signatures to intercept.
   *
   * @return method signatures
   */
  Signature[] value();
}
