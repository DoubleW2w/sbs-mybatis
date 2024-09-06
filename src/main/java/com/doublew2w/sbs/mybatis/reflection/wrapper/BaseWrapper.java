package com.doublew2w.sbs.mybatis.reflection.wrapper;

import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.reflection.property.PropertyTokenizer;

import javax.management.ReflectionException;
import java.util.List;
import java.util.Map;

/**
 * 对象包装器基类，提供一些工具方法
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 11:04
 * @project: sbs-mybatis
 */
public abstract class BaseWrapper implements ObjectWrapper {
  protected static final Object[] NO_ARGUMENTS = new Object[0];
  protected final MetaObject metaObject;

  protected BaseWrapper(MetaObject metaObject) {
    this.metaObject = metaObject;
  }

  /**
   * 解析集合属性值。
   *
   * <p>此方法用于解析给定属性名对应的集合属性值。
   *
   * <ul>
   *   <li>如果属性名为空字符串，则直接返回传入的对象。
   *   <li>否则，通过 MetaObject 获取并返回属性值。
   * </ul>
   *
   * @param prop 属性解析器，包含属性名等信息
   * @param object 当前对象
   * @return 解析后的属性值
   */
  protected Object resolveCollection(PropertyTokenizer prop, Object object) {
    if ("".equals(prop.getName())) {
      return object;
    } else {
      return metaObject.getValue(prop.getName());
    }
  }

  /**
   * 取集合的值
   *
   * <p>中括号有2个意思，一个是Map，一个是List或数组
   */
  protected Object getCollectionValue(PropertyTokenizer prop, Object collection) {
    if (collection instanceof Map) {
      return ((Map) collection).get(prop.getIndex());
    } else {
      int i = Integer.parseInt(prop.getIndex());
      if (collection instanceof List) {
        return ((List) collection).get(i);
      } else if (collection instanceof Object[]) {
        return ((Object[]) collection)[i];
      } else if (collection instanceof char[]) {
        return ((char[]) collection)[i];
      } else if (collection instanceof boolean[]) {
        return ((boolean[]) collection)[i];
      } else if (collection instanceof byte[]) {
        return ((byte[]) collection)[i];
      } else if (collection instanceof double[]) {
        return ((double[]) collection)[i];
      } else if (collection instanceof float[]) {
        return ((float[]) collection)[i];
      } else if (collection instanceof int[]) {
        return ((int[]) collection)[i];
      } else if (collection instanceof long[]) {
        return ((long[]) collection)[i];
      } else if (collection instanceof short[]) {
        return ((short[]) collection)[i];
      } else {
        throw new RuntimeException(
            "The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
      }
    }
  }

  /**
   * 设集合的值
   *
   * <p>中括号有2个意思，一个是Map，一个是List或数组
   */
  protected void setCollectionValue(PropertyTokenizer prop, Object collection, Object value) {
    if (collection instanceof Map) {
      ((Map) collection).put(prop.getIndex(), value);
    } else {
      int i = Integer.parseInt(prop.getIndex());
      if (collection instanceof List) {
        ((List) collection).set(i, value);
      } else if (collection instanceof Object[]) {
        ((Object[]) collection)[i] = value;
      } else if (collection instanceof char[]) {
        ((char[]) collection)[i] = (Character) value;
      } else if (collection instanceof boolean[]) {
        ((boolean[]) collection)[i] = (Boolean) value;
      } else if (collection instanceof byte[]) {
        ((byte[]) collection)[i] = (Byte) value;
      } else if (collection instanceof double[]) {
        ((double[]) collection)[i] = (Double) value;
      } else if (collection instanceof float[]) {
        ((float[]) collection)[i] = (Float) value;
      } else if (collection instanceof int[]) {
        ((int[]) collection)[i] = (Integer) value;
      } else if (collection instanceof long[]) {
        ((long[]) collection)[i] = (Long) value;
      } else if (collection instanceof short[]) {
        ((short[]) collection)[i] = (Short) value;
      } else {
        throw new RuntimeException(
            "The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
      }
    }
  }
}
