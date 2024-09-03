package com.doublew2w.sbs.mybatis.datasource.pooled;

import com.doublew2w.sbs.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import javax.sql.DataSource;

/**
 * 池化数据源工厂
 *
 * @author: DoubleW2w
 * @date: 2024/9/3 17:37
 * @project: sbs-mybatis
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

  @Override
  public DataSource getDataSource() {
    PooledDataSource pooledDataSource = new PooledDataSource();
    pooledDataSource.setDriver(props.getProperty("driver"));
    pooledDataSource.setUrl(props.getProperty("url"));
    pooledDataSource.setUsername(props.getProperty("username"));
    pooledDataSource.setPassword(props.getProperty("password"));
    return pooledDataSource;
  }
}
