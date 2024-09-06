package com.doublew2w.sbs.mybatis.reflection.wrapper;

import com.doublew2w.sbs.mybatis.reflection.MetaObject;

/**
 * 对象包装器工厂：负责创建对象包装器
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 11:03
 * @project: sbs-mybatis
 */
public interface ObjectWrapperFactory {
  /** 判断有没有包装器 */
  boolean hasWrapperFor(Object object);

  /** 得到包装器 */
  ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
}
