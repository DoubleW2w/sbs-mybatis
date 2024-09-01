package com.doublew2w.sbs.mybatis.mapping;

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
  private String sql;
  private Map<Integer, String> parameterMappings;
  private String parameterType;
  private String resultType;

  public BoundSql(
      String sql, Map<Integer, String> parameterMappings, String parameterType, String resultType) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.parameterType = parameterType;
    this.resultType = resultType;
  }
}
