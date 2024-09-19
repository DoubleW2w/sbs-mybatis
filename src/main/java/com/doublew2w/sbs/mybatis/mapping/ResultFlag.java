package com.doublew2w.sbs.mybatis.mapping;

/**
 * 用于标识 ResultMap 中某些结果字段的特殊属性。
 *
 * @author: DoubleW2w
 * @date: 2024/9/19 10:51
 * @project: sbs-mybatis
 */
public enum ResultFlag {
  ID,           // 表示字段是主键
  CONSTRUCTOR   // 表示字段是构造函数参数
}
