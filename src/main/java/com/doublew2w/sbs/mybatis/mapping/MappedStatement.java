package com.doublew2w.sbs.mybatis.mapping;

import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.session.Configuration;
import java.util.List;
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

  /** 脚本语言驱动器 */
  private LanguageDriver lang;

  /** 结果映射 */
  private List<ResultMap> resultMaps;

  MappedStatement() {}

  public BoundSql getBoundSql(Object parameterObject) {
    // 调用 SqlSource#getBoundSql
    return sqlSource.getBoundSql(parameterObject);
  }

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
      mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
    }

    public String id() {
      return mappedStatement.id;
    }

    public Builder resultMaps(List<ResultMap> resultMaps) {
      mappedStatement.resultMaps = resultMaps;
      return this;
    }

    public MappedStatement build() {
      assert mappedStatement.configuration != null;
      assert mappedStatement.id != null;
      return mappedStatement;
    }
  }
}
