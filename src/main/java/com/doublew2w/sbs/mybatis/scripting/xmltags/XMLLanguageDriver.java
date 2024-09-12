package com.doublew2w.sbs.mybatis.scripting.xmltags;

import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * XML语言驱动器
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 3:25
 * @project: sbs-mybatis
 */
public class XMLLanguageDriver implements LanguageDriver {
  @Override
  public SqlSource createSqlSource(
      Configuration configuration, Element script, Class<?> parameterType) {
    // 用XML脚本构建器解析
    XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
    return builder.parseScriptNode();
  }
}
