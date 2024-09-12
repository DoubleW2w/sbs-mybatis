package com.doublew2w.sbs.mybatis.scripting.xmltags;

/**
 * 静态文本SQL节点
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 3:25
 * @project: sbs-mybatis
 */
public class StaticTextSqlNode implements SqlNode {
  /** 静态的SQL文本 */
  private String text;

  public StaticTextSqlNode(String text) {
    this.text = text;
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 将文本加入context
    context.appendSql(text);
    return true;
  }
}
