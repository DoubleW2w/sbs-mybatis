package com.doublew2w.sbs.mybatis.builder;

import com.doublew2w.sbs.mybatis.mapping.ResultMap;
import com.doublew2w.sbs.mybatis.mapping.ResultMapping;
import java.util.List;

/**
 * 结果映射解析器：帮助 MyBatis 处理 ResultMap 的解析和注册
 *
 * @author: DoubleW2w
 * @date: 2024/9/19 10:59
 * @project: sbs-mybatis
 */
public class ResultMapResolver {
  private final MapperBuilderAssistant assistant;
  private String id;
  private Class<?> type;
  private List<ResultMapping> resultMappings;

  public ResultMapResolver(
      MapperBuilderAssistant assistant,
      String id,
      Class<?> type,
      List<ResultMapping> resultMappings) {
    this.assistant = assistant;
    this.id = id;
    this.type = type;
    this.resultMappings = resultMappings;
  }

  public ResultMap resolve() {
    return assistant.addResultMap(this.id, this.type, this.resultMappings);
  }
}
