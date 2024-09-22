package com.doublew2w.sbs.mybatis.scripting.xmltags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ognl.Ognl;
import ognl.OgnlException;

/**
 * OGNL缓存：http://code.google.com/p/mybatis/issues/detail?id=342
 *
 * <p>OGNL 是 Object-Graph Navigation Language 的缩写，它是一种功能强大的表达式语言（Expression Language，简称为EL）
 *
 * <p>通过它简单一致的表达式语法，可以存取对象的任意属性，调用对象的方法，遍历整个对象的结构图，实现字段类型转化等功能。
 *
 * <p>它使用相同的表达式去存取对象的属性。
 *
 * @author: DoubleW2w
 * @date: 2024/9/23 0:15
 * @project: sbs-mybatis
 */
public class OgnlCache {
  private static final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

  private OgnlCache() {
    // Prevent Instantiation of Static Class
  }

  public static Object getValue(String expression, Object root) {
    try {
      Map<String,OgnlClassResolver> context = Ognl.createDefaultContext(root, new OgnlClassResolver());
      return Ognl.getValue(parseExpression(expression), context, root);
    } catch (OgnlException e) {
      throw new RuntimeException(
          "Error evaluating expression '" + expression + "'. Cause: " + e, e);
    }
  }

  private static Object parseExpression(String expression) throws OgnlException {
    Object node = expressionCache.get(expression);
    if (node == null) {
      // OgnlParser.topLevelExpression 操作耗时，加个缓存放到 ConcurrentHashMap 里面
      node = Ognl.parseExpression(expression);
      expressionCache.put(expression, node);
    }
    return node;
  }
}
