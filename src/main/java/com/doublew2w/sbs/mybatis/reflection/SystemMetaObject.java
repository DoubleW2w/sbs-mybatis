package com.doublew2w.sbs.mybatis.reflection;

import com.doublew2w.sbs.mybatis.reflection.factory.DefaultObjectFactory;
import com.doublew2w.sbs.mybatis.reflection.factory.ObjectFactory;
import com.doublew2w.sbs.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.doublew2w.sbs.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * 一些系统级别的元对象
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 11:16
 * @project: sbs-mybatis
 */
public class SystemMetaObject {
  public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
  public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY =
      new DefaultObjectWrapperFactory();
  public static final MetaObject NULL_META_OBJECT =
      MetaObject.forObject(
          NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);

  private SystemMetaObject() {
    // Prevent Instantiation of Static Class
  }

  /** 空对象 */
  private static class NullObject {}

  public static MetaObject forObject(Object object) {
    return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
  }
}
