package com.doublew2w.sbs.mybatis.builder.xml;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.ResultMap;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.session.Configuration;
import java.util.ArrayList;
import java.util.List;
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
    MappedStatement.Builder mappedStatementBuilder =
        new MappedStatement.Builder(
            configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, resultTypeClass);
    // 结果映射，给 MappedStatement#resultMaps
    setMappedStatementResultMaps(resultTypeClass, mappedStatementBuilder);

    // 添加解析 SQL
    configuration.addMappedStatement(mappedStatementBuilder.build());
  }

  private void setMappedStatementResultMaps(
      Class<?> resultTypeClass, MappedStatement.Builder mappedStatementBuilder) {
    /*
     * 通常使用 resultType 即可满足大部分场景
     * <select id="queryUserInfoById" resultType="cn.bugstack.mybatis.test.po.User">
     * 使用 resultType 的情况下，Mybatis 会自动创建一个 ResultMap，基于属性名称映射列到 JavaBean 的属性上。
     */
    List<ResultMap> resultMaps = new ArrayList<>();
    if (resultTypeClass != null) {
      ResultMap.Builder inlineResultMapBuilder =
          new ResultMap.Builder(
              configuration,
              mappedStatementBuilder.id() + "-Inline",
              resultTypeClass,
              new ArrayList<>());
      resultMaps.add(inlineResultMapBuilder.build());
    }
    mappedStatementBuilder.resultMaps(resultMaps);
  }
}
