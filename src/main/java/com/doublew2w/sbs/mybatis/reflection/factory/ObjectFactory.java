package com.doublew2w.sbs.mybatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * 对象工厂接口来创建所有需要的新对象。
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 10:53
 * @project: sbs-mybatis
 */
public interface ObjectFactory {
  /**
   * Sets configuration properties.
   *
   * @param properties configuration properties
   */
  default void setProperties(Properties properties) {
  }

  /**
   * Creates a new object with default constructor.
   *
   * @param <T> the generic type
   * @param type Object type
   * @return the t
   */
  <T> T create(Class<T> type);

  /**
   * Creates a new object with the specified constructor and params.
   *
   * @param <T> the generic type
   * @param type Object type
   * @param constructorArgTypes Constructor argument types
   * @param constructorArgs Constructor argument values
   * @return the t
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

  /**
   * Returns true if this object can have a set of other objects. It's main purpose is to support
   * non-java.util.Collection objects like Scala collections.
   *
   * @param <T> the generic type
   * @param type Object type
   * @return whether it is a collection or not
   * @since 3.1.0
   */
  <T> boolean isCollection(Class<T> type);
}
