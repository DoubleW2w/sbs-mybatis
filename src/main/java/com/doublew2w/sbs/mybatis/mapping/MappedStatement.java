package com.doublew2w.sbs.mybatis.mapping;

import com.doublew2w.sbs.mybatis.session.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * 映射语句类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 17:42
 * @project: sbs-mybatis
 */
@Getter
@Setter
public class MappedStatement {
  private Configuration configuration;

  /** 映射语句id */
  private String id;

  /** SQL指令类型 */
  private SqlCommandType sqlCommandType;

  /** SQL源码 */
  private SqlSource sqlSource;

  /** 结果类型 */
  private Class<?> resultType;

  MappedStatement() {}

  /** 建造者 */
  public static class Builder {

    private MappedStatement mappedStatement = new MappedStatement();

    public Builder(
        Configuration configuration,
        String id,
        SqlCommandType sqlCommandType,
        SqlSource sqlSource,
        Class<?> resultType) {
      mappedStatement.configuration = configuration;
      mappedStatement.id = id;
      mappedStatement.sqlCommandType = sqlCommandType;
      mappedStatement.sqlSource = sqlSource;
      mappedStatement.resultType = resultType;
    }

    public MappedStatement build() {
      assert mappedStatement.configuration != null;
      assert mappedStatement.id != null;
      return mappedStatement;
    }
  }
}
