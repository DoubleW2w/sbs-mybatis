package com.doublew2w.sbs.mybatis.scripting.xmltags;

/**
 * @author: DoubleW2w
 * @date: 2024/9/22 23:48
 * @project: sbs-mybatis
 */
public class IfSqlNode implements SqlNode {

  private ExpressionEvaluator evaluator;
  private String test;
  private SqlNode contents;

  public IfSqlNode(SqlNode contents, String test) {
    this.test = test;
    this.contents = contents;
    this.evaluator = new ExpressionEvaluator();
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 如果满足条件，则apply，并返回true
    if (evaluator.evaluateBoolean(test, context.getBindings())) {
      contents.apply(context);
      return true;
    }
    return false;
  }
}
