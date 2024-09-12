package com.doublew2w.sbs.mybatis.scripting.xmltags;

import com.doublew2w.sbs.mybatis.builder.BaseBuilder;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.defaults.RawSqlSource;
import com.doublew2w.sbs.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: DoubleW2w
 * @date: 2024/9/13 3:25
 * @project: sbs-mybatis
 */
public class XMLScriptBuilder extends BaseBuilder {
  private Element element;
  private boolean isDynamic;
  private Class<?> parameterType;

  public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
    super(configuration);
    this.element = element;
    this.parameterType = parameterType;
  }

  public SqlSource parseScriptNode() {
    List<SqlNode> contents = parseDynamicTags(element);
    MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
    return new RawSqlSource(configuration, rootSqlNode, parameterType);
  }

  List<SqlNode> parseDynamicTags(Element element) {
    List<SqlNode> contents = new ArrayList<>();
    // element.getText 拿到 SQL
    String data = element.getText();
    contents.add(new StaticTextSqlNode(data));
    return contents;
  }
}
