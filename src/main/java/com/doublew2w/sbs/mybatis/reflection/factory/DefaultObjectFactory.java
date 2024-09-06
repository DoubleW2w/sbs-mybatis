package com.doublew2w.sbs.mybatis.reflection.factory;

import javax.management.ReflectionException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认对象工厂，所有的对象都有工厂来生成
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 10:54
 * @project: sbs-mybatis
 */
public class DefaultObjectFactory implements ObjectFactory, Serializable {
  private static final long serialVersionUID = -8855120656740914948L;

  @Override
  public <T> T create(Class<T> type) {
    return create(type, null, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T create(
      Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    Class<?> classToCreate = resolveInterface(type);
    // we know types are assignable
    return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
  }

  private <T> T instantiateClass(
      Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    try {
      Constructor<T> constructor;
      // 如果没有传入constructor，调用空构造函数，核心是调用Constructor.newInstance
      if (constructorArgTypes == null || constructorArgs == null) {
        constructor = type.getDeclaredConstructor();
        if (!constructor.canAccess(null)) {
          constructor.setAccessible(true);
        }
        return constructor.newInstance();
      }
      // 如果传入constructor，调用传入的构造函数，核心是调用Constructor.newInstance
      constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[0]));
      if (!constructor.canAccess(null)) {
        constructor.setAccessible(true);
      }
      return constructor.newInstance(constructorArgs.toArray(new Object[0]));
    } catch (Exception e) {
      // 如果出错，包装一下，重新抛出自己的异常
      StringBuilder argTypes = new StringBuilder();
      if (constructorArgTypes != null) {
        for (Class<?> argType : constructorArgTypes) {
          argTypes.append(argType.getSimpleName());
          argTypes.append(",");
        }
      }
      StringBuilder argValues = new StringBuilder();
      if (constructorArgs != null) {
        for (Object argValue : constructorArgs) {
          argValues.append(String.valueOf(argValue));
          argValues.append(",");
        }
      }
      throw new RuntimeException(
          "Error instantiating "
              + type
              + " with invalid types ("
              + argTypes
              + ") or values ("
              + argValues
              + "). Cause: "
              + e,
          e);
    }
  }

  protected Class<?> resolveInterface(Class<?> type) {
    Class<?> classToCreate;
    if (type == List.class || type == Collection.class || type == Iterable.class) {
      classToCreate = ArrayList.class;
    } else if (type == Map.class) {
      classToCreate = HashMap.class;
    } else if (type == SortedSet.class) { // issue #510 Collections Support
      classToCreate = TreeSet.class;
    } else if (type == Set.class) {
      classToCreate = HashSet.class;
    } else {
      classToCreate = type;
    }
    return classToCreate;
  }

  @Override
  public <T> boolean isCollection(Class<T> type) {
    return Collection.class.isAssignableFrom(type);
  }
}
