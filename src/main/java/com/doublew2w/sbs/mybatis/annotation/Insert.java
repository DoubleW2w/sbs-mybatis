package com.doublew2w.sbs.mybatis.annotation;

import java.lang.annotation.*;

/**
 * @author: DoubleW2w
 * @date: 2024/9/18 4:25
 * @project: sbs-mybatis
 */
@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Insert {
  /** 查询SQL语句. */
  String[] value();
}
