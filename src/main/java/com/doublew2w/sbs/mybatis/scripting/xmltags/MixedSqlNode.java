package com.doublew2w.sbs.mybatis.scripting.xmltags;

import java.util.List;

/**
 * 混合SQL节点
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 3:23
 * @project: sbs-mybatis
 */
public class MixedSqlNode implements SqlNode {
  // 组合模式，拥有一个SqlNode的List
  private List<SqlNode> contents;

  public MixedSqlNode(List<SqlNode> contents) {
    this.contents = contents;
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 依次调用list里每个元素的apply
    contents.forEach(node -> node.apply(context));
    return true;
  }
}
