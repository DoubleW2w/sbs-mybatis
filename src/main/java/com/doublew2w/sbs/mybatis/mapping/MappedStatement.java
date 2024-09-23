package com.doublew2w.sbs.mybatis.mapping;

import com.doublew2w.sbs.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.NoKeyGenerator;
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

  /** 资源路径 */
  private String resource;

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

  private boolean flushCacheRequired;

  /** 主键生成器 */
  private KeyGenerator keyGenerator;

  /** 主键属性 */
  private String[] keyProperties;

  /** 主键列 */
  private String[] keyColumns;

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
      mappedStatement.keyGenerator =
          configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType)
              ? Jdbc3KeyGenerator.INSTANCE
              : NoKeyGenerator.INSTANCE;
    }

    public String id() {
      return mappedStatement.id;
    }

    public Builder resultMaps(List<ResultMap> resultMaps) {
      mappedStatement.resultMaps = resultMaps;
      return this;
    }

    public Builder resource(String resource) {
      mappedStatement.resource = resource;
      return this;
    }

    public Builder keyGenerator(KeyGenerator keyGenerator) {
      mappedStatement.keyGenerator = keyGenerator;
      return this;
    }

    public Builder keyProperty(String keyProperty) {
      mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
      return this;
    }

    public Builder keyColumn(String keyColumn) {
      mappedStatement.keyColumns = delimitedStringToArray(keyColumn);
      return this;
    }

    public MappedStatement build() {
      assert mappedStatement.configuration != null;
      assert mappedStatement.id != null;
      return mappedStatement;
    }
  }

  public boolean isFlushCacheRequired() {
    return flushCacheRequired;
  }

  /** 分割字符串 */
  private static String[] delimitedStringToArray(String in) {
    if (in == null || in.trim().length() == 0) {
      return null;
    } else {
      return in.split(",");
    }
  }
}
