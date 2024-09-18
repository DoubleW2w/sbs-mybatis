package com.doublew2w.sbs.mybatis.scripting.xmltags;

import com.doublew2w.sbs.mybatis.executor.parameter.ParameterHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.scripting.defaults.DefaultParameterHandler;
import com.doublew2w.sbs.mybatis.scripting.defaults.RawSqlSource;
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

  /** 用于处理注解配置 SQL 语句 */
  @Override
  public SqlSource createSqlSource(
      Configuration configuration, String script, Class<?> parameterType) {
    // 暂时不解析动态 SQL
    return new RawSqlSource(configuration, script, parameterType);
  }

  @Override
  public ParameterHandler createParameterHandler(
      MappedStatement ms, Object parameterObject, BoundSql boundSql) {
    return new DefaultParameterHandler(ms, parameterObject, boundSql);
  }
}
