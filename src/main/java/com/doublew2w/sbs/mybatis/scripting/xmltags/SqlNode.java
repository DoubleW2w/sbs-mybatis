package com.doublew2w.sbs.mybatis.scripting.xmltags;

/**
 * SQL 节点
 * @author: DoubleW2w
 * @date: 2024/9/13 3:19
 * @project: sbs-mybatis
 */
public interface SqlNode {
  boolean apply(DynamicContext context);
}
