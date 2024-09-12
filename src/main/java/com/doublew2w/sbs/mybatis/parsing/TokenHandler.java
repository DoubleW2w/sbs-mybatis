package com.doublew2w.sbs.mybatis.parsing;

/**
 * 记号处理器：负责处理和替换 SQL 语句中的占位符。它的主要作用是提供一个接口，用于定义如何处理在 SQL 语句中找到的特定标记（token）
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 3:11
 * @project: sbs-mybatis
 */
public interface TokenHandler {
  String handleToken(String content);
}
