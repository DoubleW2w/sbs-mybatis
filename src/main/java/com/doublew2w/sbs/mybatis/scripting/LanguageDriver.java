package com.doublew2w.sbs.mybatis.scripting;

import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * 语言驱动器
 *
 * <p>允许用户自定义 SQL 语言的处理方式。
 *
 * <p>负责将 MyBatis Mapper XML 文件中的 SQL 语句或者注解中的 SQL 字符串解析和处理成可执行的 SQL 对象（如 SqlSource）
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 2:40
 * @project: sbs-mybatis
 */
public interface LanguageDriver {
  SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);
}
