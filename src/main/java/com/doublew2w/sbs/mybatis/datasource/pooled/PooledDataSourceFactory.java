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

  public PooledDataSourceFactory() {
    this.dataSource = new PooledDataSource();
  }
}
