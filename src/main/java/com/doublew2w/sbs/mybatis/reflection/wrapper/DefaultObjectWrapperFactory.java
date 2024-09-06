package com.doublew2w.sbs.mybatis.reflection.wrapper;

import com.doublew2w.sbs.mybatis.reflection.MetaObject;

/**
 * 默认对象包装工厂
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 11:17
 * @project: sbs-mybatis
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {
  @Override
  public boolean hasWrapperFor(Object object) {
    return false;
  }

  @Override
  public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
    throw new RuntimeException(
        "The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
  }
}
