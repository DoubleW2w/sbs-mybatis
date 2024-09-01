package com.doublew2w.sbs.mybatis.builder.xml;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.datasource.DataSourceFactory;
import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.Environment;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.transaction.TransactionFactory;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
      // resource属性值
      String resource = e.attributeValue("resource");
      // resource 输入流
      Reader reader = Resources.getResourceAsReader(resource);
      SAXReader saxReader = new SAXReader();
      Document document = saxReader.read(new InputSource(reader));
      Element root = document.getRootElement();
      // 命名空间
      String namespace = root.attributeValue("namespace");
      if (namespace == null || namespace.isEmpty()) {
        throw new RuntimeException("Mapper's namespace cannot be empty");
      }
      // SELECT
      List<Element> selectNodes = root.elements("select");
      for (Element node : selectNodes) {
        String id = node.attributeValue("id");
        String parameterType = node.attributeValue("parameterType");
        String resultType = node.attributeValue("resultType");
        String sql = node.getText();

        // ? 匹配
        Map<Integer, String> parameter = new HashMap<>();
        // 获取 #{} 里面的内容 1. (#{},#{}) 2. #{}
        Pattern pattern = Pattern.compile("(#\\{(.*?)})");
        Matcher matcher = pattern.matcher(sql);
        for (int i = 1; matcher.find(); i++) {
          String g1 = matcher.group(1);
          String g2 = matcher.group(2);
          parameter.put(i, g2);
          sql = sql.replace(g1, "?");
        }

        String msId = namespace + "." + id;
        String nodeName = node.getName();
        SqlCommandType sqlCommandType =
            SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        BoundSql boundSql = new BoundSql(sql, parameter, parameterType, resultType);
        MappedStatement mappedStatement =
            new MappedStatement.Builder(configuration, msId, sqlCommandType, boundSql).build();
        // 添加解析 SQL
        configuration.addMappedStatement(mappedStatement);
      }

      // 注册Mapper映射器
      configuration.addMapper(Resources.classForName(namespace));
    }
  }
}
