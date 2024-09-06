package com.doublew2w.sbs.mybatis.datasource.unpooled;

import com.doublew2w.sbs.mybatis.datasource.DataSourceFactory;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.reflection.SystemMetaObject;

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

  protected DataSource dataSource;

  public UnpooledDataSourceFactory() {
    this.dataSource = new UnpooledDataSource();
  }

  @Override
  public void setProperties(Properties props) {
    MetaObject metaObject = SystemMetaObject.forObject(dataSource);
    for (Object key : props.keySet()) {
      String propertyName = (String) key;
      if (metaObject.hasSetter(propertyName)) {
        String value = (String) props.get(propertyName);
        Object convertedValue = convertValue(metaObject, propertyName, value);
        metaObject.setValue(propertyName, convertedValue);
      }
    }
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * 根据setter的类型,将配置文件中的值强转成相应的类型
   */
  private Object convertValue(MetaObject metaObject, String propertyName, String value) {
    Object convertedValue = value;
    Class<?> targetType = metaObject.getSetterType(propertyName);
    if (targetType == Integer.class || targetType == int.class) {
      convertedValue = Integer.valueOf(value);
    } else if (targetType == Long.class || targetType == long.class) {
      convertedValue = Long.valueOf(value);
    } else if (targetType == Boolean.class || targetType == boolean.class) {
      convertedValue = Boolean.valueOf(value);
    }
    return convertedValue;
  }
}
