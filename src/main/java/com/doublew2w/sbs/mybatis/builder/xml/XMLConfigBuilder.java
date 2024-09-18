package com.doublew2w.sbs.mybatis.builder.xml;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.datasource.DataSourceFactory;
import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.mapping.Environment;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.transaction.TransactionFactory;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 16:35
 * @project: sbs-mybatis
 */
@Slf4j
public class XMLConfigBuilder extends BaseBuilder {
  /** XML根节点 */
  private Element root;

  public XMLConfigBuilder(Reader reader) {
    // 1. 调用父类初始化Configuration
    super(new Configuration());
    // 2. dom4j 处理 xml
    SAXReader saxReader = new SAXReader();
    try {
      Document document = saxReader.read(new InputSource(reader));
      root = document.getRootElement();
    } catch (DocumentException e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * 解析配置；类型别名、插件、对象工厂、对象包装工厂、设置、环境、类型转换、映射器
   *
   * @return Configuration
   */
  public Configuration parse() {
    try {
      // 环境
      environmentsElement(root.element("environments"));
      // 解析映射器
      mapperElement(root.element("mappers"));
    } catch (Exception e) {
      throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
    return configuration;
  }

  /**
   * <environments default="development"> <environment id="development"> <transactionManager
   * type="JDBC"> <property name="..." value="..."/> </transactionManager> <dataSource
   * type="POOLED"> <property name="driver" value="${driver}"/> <property name="url"
   * value="${url}"/> <property name="username" value="${username}"/> <property name="password"
   * value="${password}"/> </dataSource> </environment> </environments>
   */
  private void environmentsElement(Element environments) throws Exception {
    // 默认环境
    String environment = environments.attributeValue("default");
    List<Element> environmentList = environments.elements("environment");
    for (Element e : environmentList) {
      String id = e.attributeValue("id");
      if (environment.equals(id)) {
        // todo：事务管理器，缺少对应的测试
        TransactionFactory txFactory =
            (TransactionFactory)
                typeAliasRegistry
                    .resolveAlias(e.element("transactionManager").attributeValue("type"))
                    .newInstance();
        // 数据源
        Element dataSourceElement = e.element("dataSource");
        DataSourceFactory dataSourceFactory =
            (DataSourceFactory)
                typeAliasRegistry
                    .resolveAlias(dataSourceElement.attributeValue("type"))
                    .newInstance();
        // 注入数据源配置属性
        List<Element> propertyList = dataSourceElement.elements("property");
        Properties props = new Properties();
        for (Element property : propertyList) {
          props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
        }
        dataSourceFactory.setProperties(props);
        DataSource dataSource = dataSourceFactory.getDataSource();

        // 构建环境
        Environment.Builder environmentBuilder =
            new Environment.Builder(id).transactionFactory(txFactory).dataSource(dataSource);
        configuration.setEnvironment(environmentBuilder.build());
      }
    }
  }

  /**
   * 解析 mapper 资源
   *
   * <p>得到映射语句配置
   *
   * <p>注册映射器
   *
   * @param mappers mappers 根节点
   * @throws Exception
   */
  private void mapperElement(Element mappers) throws Exception {
    List<Element> mapperList = mappers.elements("mapper");
    for (Element e : mapperList) {
      // xml资源路径
      String resource = e.attributeValue("resource");
      // mapper接口
      String mapperClass = e.attributeValue("class");
      // XML 解析
      if (resource != null && mapperClass == null) {
        InputStream inputStream = Resources.getResourceAsStream(resource);
        // 在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
        mapperParser.parse();
      }
      // Annotation 注解解析
      else if (resource == null && mapperClass != null) {
        Class<?> mapperInterface = Resources.classForName(mapperClass);
        configuration.addMapper(mapperInterface);
      }
    }
  }
}
