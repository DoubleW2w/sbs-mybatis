package com.doublew2w.sbs.mybatis.datasource.unpooled;

import com.doublew2w.sbs.mybatis.datasource.DataSourceFactory;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * 无池化数据源工厂
 *
 * @author: DoubleW2w
 * @date: 2024/9/3 16:46
 * @project: sbs-mybatis
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {
  protected Properties props;

  @Override
  public void setProperties(Properties props) {
    this.props = props;
  }

  @Override
  public DataSource getDataSource() {
    UnpooledDataSource unpooledDataSource = new UnpooledDataSource();
    unpooledDataSource.setDriver(props.getProperty("driver"));
    unpooledDataSource.setUrl(props.getProperty("url"));
    unpooledDataSource.setUsername(props.getProperty("username"));
    unpooledDataSource.setPassword(props.getProperty("password"));
    return unpooledDataSource;
  }
}
