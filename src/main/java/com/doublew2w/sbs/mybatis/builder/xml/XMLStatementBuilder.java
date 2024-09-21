package com.doublew2w.sbs.mybatis.builder.xml;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.builder.MapperBuilderAssistant;
import com.doublew2w.sbs.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.NoKeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.SelectKeyGenerator;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.session.Configuration;
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

  private MapperBuilderAssistant builderAssistant;
  private Element element;

  public XMLStatementBuilder(
      Configuration configuration, MapperBuilderAssistant builderAssistant, Element element) {
    super(configuration);
    this.builderAssistant = builderAssistant;
    this.element = element;
  }

  /**
   * 解析mapperxml入口
   *
   * <p>解析语句(select|insert|update|delete)
   *
   * <p>&lt;select
   *
   * <p>id="selectPerson"
   *
   * <p>parameterType="int"
   *
   * <p>parameterMap="deprecated"
   *
   * <p>resultType="hashmap"
   *
   * <p>resultMap="personResultMap"
   *
   * <p>flushCache="false"
   *
   * <p>useCache="true"
   *
   * <p>timeout="10000"
   *
   * <p>fetchSize="256"
   *
   * <p>statementType="PREPARED"
   *
   * <p>resultSetType="FORWARD_ONLY"&gt;
   *
   * <p>SELECT * FROM PERSON WHERE ID = #{id}
   *
   * <p>&lt;/select&gt;
   */
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

    // Parse selectKey after includes and remove them.
    processSelectKeyNodes(id, parameterTypeClass, langDriver);

    // 属性标记【仅对 insert 有用】, MyBatis 会通过 getGeneratedKeys 或者通过 insert 语句的 selectKey 子元素设置它的值
    String keyProperty = element.attributeValue("keyProperty");

    KeyGenerator keyGenerator;
    String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
    keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
    // 获取主键生成器
    if (configuration.hasKeyGenerator(keyStatementId)) {
      keyGenerator = configuration.getKeyGenerator(keyStatementId);
    } else {
      keyGenerator =
          configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType)
              ? new Jdbc3KeyGenerator()
              : new NoKeyGenerator();
    }

    // 构建SQL源码
    SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);
    // 调用助手类【本节新添加，便于统一处理参数的包装】
    builderAssistant.addMappedStatement(
        id,
        sqlSource,
        sqlCommandType,
        parameterTypeClass,
        resultMap,
        resultTypeClass,
        keyGenerator,
        keyProperty,
        langDriver);
  }

  /** 循环处理多个selectKey节点 */
  private void processSelectKeyNodes(
      String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
    List<Element> selectKeyNodes = element.elements("selectKey");
    parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver);
  }

  private void parseSelectKeyNodes(
      String parentId,
      List<Element> list,
      Class<?> parameterTypeClass,
      LanguageDriver languageDriver) {
    for (Element nodeToHandle : list) {
      String id = parentId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
      parseSelectKeyNode(id, nodeToHandle, parameterTypeClass, languageDriver);
    }
  }

  private void parseSelectKeyNode(
      String id, Element nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver) {
    String resultType = nodeToHandle.attributeValue("resultType");
    Class<?> resultTypeClass = resolveClass(resultType);
    String keyProperty = nodeToHandle.attributeValue("keyProperty");
    String keyColumn = nodeToHandle.attributeValue("keyColumn");
    boolean executeBefore = "BEFORE".equals(nodeToHandle.attributeValue("order", "AFTER"));

    // defaults
    String resultMap = null;
    KeyGenerator keyGenerator = new NoKeyGenerator();

    // 解析成SqlSource，DynamicSqlSource/RawSqlSource
    SqlSource sqlSource =
        langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
    SqlCommandType sqlCommandType = SqlCommandType.SELECT;

    // 调用助手类
    builderAssistant.addMappedStatement(
        id,
        sqlSource,
        sqlCommandType,
        parameterTypeClass,
        resultMap,
        resultTypeClass,
        keyGenerator,
        keyProperty,
        langDriver);

    id = builderAssistant.applyCurrentNamespace(id, false);

    // 存放键值生成器配置
    MappedStatement keyStatement = configuration.getMappedStatement(id);
    configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
  }
}
