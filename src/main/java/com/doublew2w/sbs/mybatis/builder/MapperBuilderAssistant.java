package com.doublew2w.sbs.mybatis.builder;

import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.ResultMap;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.session.Configuration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 映射构建器助手「建造者」：简化和协调 Mapper 文件的解析过程
 *
 * <p>1. 注册SQL语句： addMappedStatement()
 *
 * <p>2. 处理 ResultMap ：注册 ResultMap 配置，将数据库查询结果映射到 Java 对象。
 *
 * <p>3. 处理 处理 ParameterMap：将 Java 对象的属性映射到 SQL 语句的参数
 *
 * <p>4. 动态 SQL 处理：将动态 SQL 语句转化为执行时的 SQL 结构
 *
 * @author: DoubleW2w
 * @date: 2024/9/16 2:30
 * @project: sbs-mybatis
 */
@Getter
@Setter
public class MapperBuilderAssistant extends BaseBuilder {
  /** mapperXML文件命名空间 */
  private String currentNamespace;

  /** mapperXML路径资源 */
  private String resource;

  public MapperBuilderAssistant(Configuration configuration, String resource) {
    super(configuration);
    this.resource = resource;
  }

  public String applyCurrentNamespace(String base, boolean isReference) {
    if (base == null) {
      return null;
    }
    if (isReference) {
      if (base.contains(".")) return base;
    }
    return currentNamespace + "." + base;
  }

  /** 添加映射器语句 */
  public MappedStatement addMappedStatement(
      String id,
      SqlSource sqlSource,
      SqlCommandType sqlCommandType,
      Class<?> parameterType,
      String resultMap,
      Class<?> resultType,
      LanguageDriver lang) {
    // 给 id加上namespace前缀形成唯一标识
    id = applyCurrentNamespace(id, false);
    MappedStatement.Builder statementBuilder =
        new MappedStatement.Builder(
            configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, resultType);
    // 结果映射，给 MappedStatement#resultMaps
    setStatementResultMap(resultMap, resultType, statementBuilder);

    MappedStatement statement = statementBuilder.build();

    // 添加解析 SQL
    configuration.addMappedStatement(statementBuilder.build());
    return statement;
  }

  private void setStatementResultMap(
      String resultMap, Class<?> resultType, MappedStatement.Builder statementBuilder) {
    // 因为暂时还没有在 Mapper XML 中配置 Map 返回结果，所以这里返回的是 null
    resultMap = applyCurrentNamespace(resultMap, true);
    List<ResultMap> resultMaps = new ArrayList<>();
    if (resultMap != null) {
      // TODO：后续添加
    }
    /*
     * 通常使用 resultType 即可满足大部分场景
     * <select id="queryUserInfoById" resultType="cn.bugstack.mybatis.test.po.User">
     * 使用 resultType 的情况下，Mybatis 会自动创建一个 ResultMap，基于属性名称映射列到 JavaBean 的属性上。
     */
    else if (resultType != null) {
      ResultMap.Builder inlineResultMapBuilder =
          new ResultMap.Builder(
              configuration, statementBuilder.id() + "-Inline", resultType, new ArrayList<>());
      resultMaps.add(inlineResultMapBuilder.build());
    }
    statementBuilder.resultMaps(resultMaps);
  }
}
