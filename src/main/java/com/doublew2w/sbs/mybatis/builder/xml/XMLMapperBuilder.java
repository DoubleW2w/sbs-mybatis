package com.doublew2w.sbs.mybatis.builder.xml;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.builder.MapperBuilderAssistant;
import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.session.Configuration;
import java.io.InputStream;
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
  /**
   * 映射器构建助手
   */
  private MapperBuilderAssistant builderAssistant;

  /**
   * mapperXML资源路径
   */
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
      // 标记一下，已经加载过了
      configuration.addLoadedResource(resource);
      // 绑定映射器到namespace
      configuration.addMapper(Resources.classForName(builderAssistant.getCurrentNamespace()));
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

    // 2.配置select|insert|update|delete
    buildStatementFromContext(element.elements("select"));
  }

  // 配置select|insert|update|delete
  private void buildStatementFromContext(List<Element> list) {
    for (Element element : list) {
      final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant,element);
      statementParser.parseStatementNode();
    }
  }
}
