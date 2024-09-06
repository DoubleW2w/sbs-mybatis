package com.doublew2w.sbs.mybatis.reflection.property;

import lombok.Getter;

import java.util.Iterator;

/**
 * 属性分解标记
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 10:26
 * @project: sbs-mybatis
 */
@Getter
public class PropertyTokenizer implements Iterator<PropertyTokenizer>, Iterable<PropertyTokenizer> {
  /** 主属性名，例如 "班级" 或 "学生" */
  private String name;

  /** 包含索引的属性名，例如 "班级[0]" */
  private final String indexedName;

  /** 索引值，例如 "0" */
  private String index;

  /** 子属性名，例如 "成绩" */
  private final String children;

  public PropertyTokenizer(String fullname) {
    // 班级[0].学生.成绩
    // 找这个点 . 作为主属性和子属性的分界点
    int delim = fullname.indexOf('.');
    if (delim > -1) {
      name = fullname.substring(0, delim);
      children = fullname.substring(delim + 1);
    } else {
      // 找不到.的话，取全部部分
      name = fullname;
      children = null;
    }
    indexedName = name;
    // 把中括号里的数字给解析出来
    delim = name.indexOf('[');
    if (delim > -1) {
      index = name.substring(delim + 1, name.length() - 1);
      name = name.substring(0, delim);
    }
  }

  @Override
  public boolean hasNext() {
    return children != null;
  }

  @Override
  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException(
        "Remove is not supported, as it has no meaning in the context of properties.");
  }

  @Override
  public Iterator<PropertyTokenizer> iterator() {
    return this;
  }
}
