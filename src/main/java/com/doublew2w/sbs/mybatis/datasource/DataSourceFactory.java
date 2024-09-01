package com.doublew2w.sbs.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 数据源工厂
 *
 * @author: DoubleW2w
 * @date: 2024/9/2 1:42
 * @project: sbs-mybatis
 */
public interface DataSourceFactory {
  /**
   * 设置数据源属性
   *
   * @param props 属性
   */
  void setProperties(Properties props);

  /**
   * 获取数据源
   *
   * @return 数据源
   */
  DataSource getDataSource();
}
