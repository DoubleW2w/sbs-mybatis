package com.doublew2w.sbs.mybatis.mapping;

import com.doublew2w.sbs.mybatis.session.Configuration;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

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

  /** 参数类型 */
  private String parameterType;

  /** 结果类型 */
  private String resultType;

  /** SQL语句 */
  private String sql;

  /** 参数位置映射 */
  private Map<Integer, String> parameter;

  MappedStatement() {}

  /** 建造者 */
  public static class Builder {

    private MappedStatement mappedStatement = new MappedStatement();

    public Builder(
        Configuration configuration,
        String id,
        SqlCommandType sqlCommandType,
        String parameterType,
        String resultType,
        String sql,
        Map<Integer, String> parameter) {
      mappedStatement.configuration = configuration;
      mappedStatement.id = id;
      mappedStatement.sqlCommandType = sqlCommandType;
      mappedStatement.parameterType = parameterType;
      mappedStatement.resultType = resultType;
      mappedStatement.sql = sql;
      mappedStatement.parameter = parameter;
    }

    public MappedStatement build() {
      assert mappedStatement.configuration != null;
      assert mappedStatement.id != null;
      return mappedStatement;
    }

    public String id() {
      return mappedStatement.id;
    }
  }
}
