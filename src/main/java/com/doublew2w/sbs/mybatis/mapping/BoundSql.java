package com.doublew2w.sbs.mybatis.mapping;

import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.session.Configuration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * 绑定的SQL,是从 SqlSource 而来，将动态内容都处理完成得到的SQL语句字符串，其中包括?,还有绑定的参数
 *
 * @author: DoubleW2w
 * @date: 2024/9/2 2:58
 * @project: sbs-mybatis
 */
@Getter
public class BoundSql {
  /** sql语句 */
  private String sql;
  /**
   * 参数映射
   */
  private List<ParameterMapping> parameterMappings;
  /**
   * 参数对象
   */
  private Object parameterObject;
  /**
   * 附加参数
   */
  private Map<String, Object> additionalParameters;
  /**
   * 元对象
   */
  private MetaObject metaParameters;

  public BoundSql(
      Configuration configuration,
      String sql,
      List<ParameterMapping> parameterMappings,
      Object parameterObject) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.parameterObject = parameterObject;
    this.additionalParameters = new HashMap<>();
    this.metaParameters = configuration.newMetaObject(additionalParameters);
  }

  public boolean hasAdditionalParameter(String name) {
    return metaParameters.hasGetter(name);
  }

  public void setAdditionalParameter(String name, Object value) {
    metaParameters.setValue(name, value);
  }

  public Object getAdditionalParameter(String name) {
    return metaParameters.getValue(name);
  }
}
