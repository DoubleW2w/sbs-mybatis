package com.doublew2w.sbs.mybatis.builder.xml;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.builder.MapperBuilderAssistant;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.session.Configuration;
import java.util.Locale;
import org.dom4j.Element;

/**
 * XML语句构建器
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 2:34
 * @project: sbs-mybatis
 */
public class XMLStatementBuilder extends BaseBuilder {

  private MapperBuilderAssistant mapperBuilderAssistant;
  private Element element;

  public XMLStatementBuilder(
      Configuration configuration, MapperBuilderAssistant mapperBuilderAssistant, Element element) {
    super(configuration);
    this.mapperBuilderAssistant = mapperBuilderAssistant;
    this.element = element;
  }

  // 解析语句(select|insert|update|delete)
  // <select
  //  id="selectPerson"
  //  parameterType="int"
  //  parameterMap="deprecated"
  //  resultType="hashmap"
  //  resultMap="personResultMap"
  //  flushCache="false"
  //  useCache="true"
  //  timeout="10000"
  //  fetchSize="256"
  //  statementType="PREPARED"
  //  resultSetType="FORWARD_ONLY">
  //  SELECT * FROM PERSON WHERE ID = #{id}
  // </select>
  public void parseStatementNode() {
    String id = element.attributeValue("id");
    // 参数类型
    String parameterType = element.attributeValue("parameterType");
    Class<?> parameterTypeClass = resolveAlias(parameterType);
    // 外部应用 resultMap
    String resultMap = element.attributeValue("resultMap");
    // 结果类型
    String resultType = element.attributeValue("resultType");
    Class<?> resultTypeClass = resolveAlias(resultType);
    // 获取命令类型(select|insert|update|delete)
    String nodeName = element.getName();
    SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

    // 获取默认语言驱动器
    LanguageDriver langDriver = configuration.getLanguageRegistry().getDefaultDriver();
    // 构建SQL源码
    SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);
    // 调用助手类【本节新添加，便于统一处理参数的包装】
    mapperBuilderAssistant.addMappedStatement(
        id, sqlSource, sqlCommandType, parameterTypeClass, resultMap, resultTypeClass, langDriver);
  }
}
