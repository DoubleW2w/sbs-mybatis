package com.doublew2w.sbs.mybatis.scripting.xmltags;

import com.doublew2w.sbs.mybatis.io.Resources;
import java.util.HashMap;
import java.util.Map;
import ognl.ClassResolver;

/**
 * OGNL表达式中负责解析和查找指定名称的 Java 类
 *
 * @author: DoubleW2w
 * @date: 2024/9/23 0:20
 * @project: sbs-mybatis
 */
public class OgnlClassResolver implements ClassResolver {
  private Map<String, Class<?>> classes = new HashMap<String, Class<?>>(101);

  @Override
  public Class classForName(String className, Map context) throws ClassNotFoundException {
    Class<?> result;
    if ((result = classes.get(className)) == null) {
      try {
        result = Resources.classForName(className);
      } catch (ClassNotFoundException e1) {
        if (className.indexOf('.') == -1) {
          result = Resources.classForName("java.lang." + className);
          classes.put("java.lang." + className, result);
        }
      }
      classes.put(className, result);
    }
    return result;
  }
}
