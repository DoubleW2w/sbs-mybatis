package com.doublew2w.sbs.mybatis.spring;

import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactoryBuilder;
import java.io.Reader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 会话工厂对象
 *
 * @author: DoubleW2w
 * @date: 2024/9/26 1:07
 * @project: sbs-mybatis
 */
@Slf4j
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean {
  private String resource;
  private SqlSessionFactory sqlSessionFactory;

  @Override
  public SqlSessionFactory getObject() throws Exception {
    return sqlSessionFactory;
  }

  @Override
  public Class<?> getObjectType() {
    return SqlSessionFactory.class;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    log.info("com.doublew2w.sbs.mybatis.spring.SqlSessionFactoryBean.afterPropertiesSet");
    try (Reader reader = Resources.getResourceAsReader(resource)) {
      this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void setResource(String resource) {
    this.resource = resource;
  }
}
