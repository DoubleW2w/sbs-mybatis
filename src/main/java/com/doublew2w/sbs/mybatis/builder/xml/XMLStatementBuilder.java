package com.doublew2w.sbs.mybatis.builder.xml;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriverRegistry;
import com.doublew2w.sbs.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.Locale;

/**
 * XML语句构建器
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 2:34
 * @project: sbs-mybatis
 */
public class XMLStatementBuilder extends BaseBuilder {

  private String currentNamespace;
  private Element element;

  public XMLStatementBuilder(
      Configuration configuration, Element element, String currentNamespace) {
    super(configuration);
    this.element = element;
    this.currentNamespace = currentNamespace;
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
    // 构建映射语句
    MappedStatement mappedStatement =
        new MappedStatement.Builder(
                configuration,
                currentNamespace + "." + id,
                sqlCommandType,
                sqlSource,
                resultTypeClass)
            .build();

    // 添加解析 SQL
    configuration.addMappedStatement(mappedStatement);
  }
}
