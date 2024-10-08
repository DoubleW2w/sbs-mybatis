package com.doublew2w.sbs.mybatis.datasource.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.doublew2w.sbs.mybatis.datasource.DataSourceFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Druid 数据源工厂
 *
 * @author: DoubleW2w
 * @date: 2024/9/2 1:42
 * @project: sbs-mybatis
 */
public class DruidDataSourceFactory implements DataSourceFactory {
  private final Logger logger = LoggerFactory.getLogger(DruidDataSourceFactory.class);
  private Properties props;

  @Override
  public void setProperties(Properties props) {
    this.props = props;
  }

  @Override
  public DataSource getDataSource() {
    try (DruidDataSource dataSource = new DruidDataSource()) {
      dataSource.setDriverClassName(props.getProperty("driver"));
      dataSource.setUrl(props.getProperty("url"));
      dataSource.setUsername(props.getProperty("username"));
      dataSource.setPassword(props.getProperty("password"));
      return dataSource;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }
}
