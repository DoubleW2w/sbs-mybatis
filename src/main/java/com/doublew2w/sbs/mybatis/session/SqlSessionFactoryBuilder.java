package com.doublew2w.sbs.mybatis.session;

import com.doublew2w.sbs.mybatis.builder.xml.XMLConfigBuilder;
import com.doublew2w.sbs.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 18:08
 * @project: sbs-mybatis
 */
public class SqlSessionFactoryBuilder {

  public SqlSessionFactory build(Reader reader) {
    XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
    return build(xmlConfigBuilder.parse());
  }

  public SqlSessionFactory build(Configuration config) {
    return new DefaultSqlSessionFactory(config);
  }
}
