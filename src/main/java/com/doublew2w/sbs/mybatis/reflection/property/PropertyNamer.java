package com.doublew2w.sbs.mybatis.reflection.property;

import java.util.Locale;

/**
 * 属性命名器
 *
 * @author: DoubleW2w
 * @date: 2024/9/6 0:12
 * @project: sbs-mybatis
 */
public class PropertyNamer {
  private PropertyNamer() {}

  public static String methodToProperty(String name) {
    if (name.startsWith("is")) {
      name = name.substring(2);
    } else if (name.startsWith("get") || name.startsWith("set")) {
      name = name.substring(3);
    } else {
      throw new RuntimeException(
          "Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
    }
    /*
     * 如果只有1个字母，转换为小写 eg:isA setA getA => a
     * 如果大于1个字母，第二个字母非大写，转换为小写 eg:isApple setApple getApple apple
     */
    if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
      name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
    }

    return name;
  }

  /**
   * 判断给定的名字是否属于属性相关的 getter 或 setter 方法。
   *
   * @param name 需要判断的方法名称
   * @return 如果给定的名字属于 getter 或 setter 方法，则返回 true；否则返回 false
   */
  public static boolean isProperty(String name) {
    return isGetter(name) || isSetter(name);
  }

  /**
   * 判断给定的名字是否为 getter 方法。
   *
   * @param name 需要判断的方法名称
   * @return 如果给定的名字以 "get" 开头且长度大于 3，或者以 "is" 开头且长度大于 2，则返回 true；否则返回 false
   */
  public static boolean isGetter(String name) {
    return (name.startsWith("get") && name.length() > 3)
        || (name.startsWith("is") && name.length() > 2);
  }

  /**
   * 判断给定的名字是否为 setter 方法。
   *
   * @param name 需要判断的方法名称
   * @return 如果给定的名字以 "set" 开头且长度大于 3，则返回 true；否则返回 false
   */
  public static boolean isSetter(String name) {
    return name.startsWith("set") && name.length() > 3;
  }
}
