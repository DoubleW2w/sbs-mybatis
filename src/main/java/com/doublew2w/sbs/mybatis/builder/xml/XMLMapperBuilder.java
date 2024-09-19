package com.doublew2w.sbs.mybatis.builder.xml;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.builder.MapperBuilderAssistant;
import com.doublew2w.sbs.mybatis.builder.ResultMapResolver;
import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.mapping.ResultFlag;
import com.doublew2w.sbs.mybatis.mapping.ResultMap;
import com.doublew2w.sbs.mybatis.mapping.ResultMapping;
import com.doublew2w.sbs.mybatis.session.Configuration;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * XML映射构建器
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 2:31
 * @project: sbs-mybatis
 */
public class XMLMapperBuilder extends BaseBuilder {

  private Element element;

  /** 映射器构建助手 */
  private MapperBuilderAssistant builderAssistant;

  /** mapperXML资源路径 */
  private String resource;

  public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource)
      throws DocumentException {
    this(new SAXReader().read(inputStream), configuration, resource);
  }

  private XMLMapperBuilder(Document document, Configuration configuration, String resource) {
    super(configuration);
    this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
    this.element = document.getRootElement();
    this.resource = resource;
  }

  /** 解析 */
  public void parse() throws Exception {
    // 如果当前资源没有加载过再加载，防止重复加载
    if (!configuration.isResourceLoaded(resource)) {
      configurationElement(element);
      bindMapperForNamespace();
    }
  }

  // 配置mapper元素
  // <mapper namespace="org.mybatis.example.BlogMapper">
  //   <select id="selectBlog" parameterType="int" resultType="Blog">
  //    select * from Blog where id = #{id}
  //   </select>
  // </mapper>
  private void configurationElement(Element element) {
    // 1.配置namespace
    String namespace = element.attributeValue("namespace");
    if (namespace.isEmpty()) {
      throw new RuntimeException("Mapper's namespace cannot be empty");
    }
    builderAssistant.setCurrentNamespace(namespace);

    // 2. 解析resultMap
    resultMapElements(element.elements("resultMap"));

    // 2.配置select|insert|update|delete
    List<Element> list = new ArrayList<>();
    list.addAll(element.elements("select"));
    list.addAll(element.elements("insert"));
    list.addAll(element.elements("update"));
    list.addAll(element.elements("delete"));
    buildStatementFromContext(list);
  }

  /**
   * 循环解析每一个&lt;resultMap&gt;标签信息
   *
   * @param resultMaps &lt;resultMap&gt; 列表
   */
  private void resultMapElements(List<Element> resultMaps) {
    for (Element element : resultMaps) {
      try {
        resultMapElement(element, Collections.emptyList());
      } catch (Exception ignore) {
      }
    }
  }

  /**
   * &lt;resultMap id="activityMap" type="com.doublew2w.sbs.mybatis.test.po.Activity"&gt;
   *
   * <p>&lt;id column="id" property="id"/&gt;
   *
   * <p>&lt;result column="activity_id" property="activityId"/&gt;
   *
   * <p>&lt;result column="activity_name" property="activityName"/&gt;
   *
   * <p>&lt;result column="activity_desc" property="activityDesc"/&gt;
   *
   * <p>&lt;result column="create_time" property="createTime"/&gt;
   *
   * <p>&lt;result column="update_time" property="updateTime"/&gt;
   *
   * <p>&lt;/resultMap&gt;
   */
  private ResultMap resultMapElement(
      Element resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
    String id = resultMapNode.attributeValue("id");
    String type = resultMapNode.attributeValue("type");
    Class<?> typeClass = resolveClass(type);

    List<ResultMapping> resultMappings = new ArrayList<>(additionalResultMappings);

    List<Element> resultChildren = resultMapNode.elements();
    for (Element resultChild : resultChildren) {
      List<ResultFlag> flags = new ArrayList<>();
      if ("id".equals(resultChild.getName())) {
        flags.add(ResultFlag.ID);
      }
      // 构建 ResultMapping
      resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
    }

    // 创建结果映射解析器
    ResultMapResolver resultMapResolver =
        new ResultMapResolver(builderAssistant, id, typeClass, resultMappings);
    return resultMapResolver.resolve();
  }

  /**
   * 构建resultMap中的每一列属性
   *
   * <pre>
   * &lt;id column="id" property="id"/&gt;
   * &lt;result column="activity_id" property="activityId"/&gt;
   * </pre>
   */
  private ResultMapping buildResultMappingFromContext(
      Element context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
    String property = context.attributeValue("property");
    String column = context.attributeValue("column");
    return builderAssistant.buildResultMapping(resultType, property, column, flags);
  }

  // 配置select|insert|update|delete
  private void buildStatementFromContext(List<Element> list) {
    for (Element element : list) {
      final XMLStatementBuilder statementParser =
          new XMLStatementBuilder(configuration, builderAssistant, element);
      statementParser.parseStatementNode();
    }
  }

  private void bindMapperForNamespace() {
    String namespace = builderAssistant.getCurrentNamespace();
    if (namespace != null) {
      Class<?> boundType = null;
      try {
        boundType = Resources.classForName(namespace);
      } catch (ClassNotFoundException e) {
        // ignore, bound type is not required
      }
      if (boundType != null && !configuration.hasMapper(boundType)) {
        // Spring may not know the real resource name so we set a flag
        // to prevent loading again this resource from the mapper interface
        // look at MapperAnnotationBuilder#loadXmlResource
        configuration.addLoadedResource("namespace:" + namespace);
        configuration.addMapper(boundType);
      }
    }
  }
}
