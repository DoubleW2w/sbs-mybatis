package com.doublew2w.sbs.mybatis.mapping;

import com.doublew2w.sbs.mybatis.session.Configuration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

/**
 * 定义 SQL 查询结果如何映射到 Java 对象的配置。 1. 字段名的映射 2. 属性的映射 3. 复杂的映射关系（1对1,1对多）
 *
 * @author: DoubleW2w
 * @date: 2024/9/14 16:46
 * @project: sbs-mybatis
 */
@Getter
public class ResultMap {
  /** 唯一标识 */
  private String id;

  /** 结果类型 */
  private Class<?> type;

  /** 每一行的配置 */
  private List<ResultMapping> resultMappings;

  /** 映射的列名 */
  private Set<String> mappedColumns;

  private ResultMap() {}

  public static class Builder {
    private ResultMap resultMap = new ResultMap();

    public Builder(
        Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings) {
      resultMap.id = id;
      resultMap.type = type;
      resultMap.resultMappings = resultMappings;
    }

    public ResultMap build() {
      resultMap.mappedColumns = new HashSet<>();
      return resultMap;
    }
  }
}
