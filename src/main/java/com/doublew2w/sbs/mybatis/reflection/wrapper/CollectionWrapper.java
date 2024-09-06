package com.doublew2w.sbs.mybatis.reflection.wrapper;

import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.reflection.factory.ObjectFactory;
import com.doublew2w.sbs.mybatis.reflection.property.PropertyTokenizer;

import java.util.Collection;
import java.util.List;

/**
 * Collection 包装器
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 11:22
 * @project: sbs-mybatis
 */
public class CollectionWrapper implements ObjectWrapper {

  private final Collection<Object> object;

  public CollectionWrapper(MetaObject metaObject, Collection<Object> object) {
    this.object = object;
  }

  @Override
  public Object get(PropertyTokenizer prop) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void set(PropertyTokenizer prop, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String findProperty(String name, boolean useCamelCaseMapping) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getGetterNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getSetterNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> getSetterType(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> getGetterType(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSetter(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasGetter(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MetaObject instantiatePropertyValue(
      String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCollection() {
    return true;
  }

  @Override
  public void add(Object element) {
    object.add(element);
  }

  @Override
  public <E> void addAll(List<E> element) {
    object.addAll(element);
  }
}
