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

  /** 绑定的SQL语句 */
  private BoundSql boundSql;

  MappedStatement() {}

  /** 建造者 */
  public static class Builder {

    private MappedStatement mappedStatement = new MappedStatement();

    public Builder(
        Configuration configuration, String id, SqlCommandType sqlCommandType, BoundSql boundSql) {
      mappedStatement.configuration = configuration;
      mappedStatement.id = id;
      mappedStatement.sqlCommandType = sqlCommandType;
      mappedStatement.boundSql = boundSql;
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

  public Configuration getConfiguration() {
    return configuration;
  }

  public String getId() {
    return id;
  }

  public SqlCommandType getSqlCommandType() {
    return sqlCommandType;
  }

  public BoundSql getBoundSql() {
    return boundSql;
  }
}
