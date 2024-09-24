package com.doublew2w.sbs.mybatis.builder;

import com.doublew2w.sbs.mybatis.cache.Cache;
import com.doublew2w.sbs.mybatis.cache.decorators.FifoCache;
import com.doublew2w.sbs.mybatis.cache.impl.PerpetualCache;
import com.doublew2w.sbs.mybatis.executor.keygen.KeyGenerator;
import com.doublew2w.sbs.mybatis.mapping.*;
import com.doublew2w.sbs.mybatis.reflection.MetaClass;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.type.TypeHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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

  private Cache currentCache;

  public MapperBuilderAssistant(Configuration configuration, String resource) {
    super(configuration);
    this.resource = resource;
  }

  /**
   * 将当前 Mapper 的命名空间应用于给定的基础字符串（base），生成一个完整的 ID，用于注册 SQL 语句或其他映射信息。
   *
   * @param base 基础字符串
   * @param isReference 决定了该方法是否用于引用其他 Mapper 的 SQL 映射。 true - 在引用其他 Mapper 时使用的。MyBatis 需要保持原始 ID
   *     的格式，不添加当前 Mapper 的命名空间。 false - 返回的 ID 会包含当前 Mapper 的命名空间。
   * @return
   */
  public String applyCurrentNamespace(String base, boolean isReference) {
    if (base == null) {
      return null;
    }
    if (isReference) {
      if (base.contains(".")) return base;
    } else {
      if (base.startsWith(currentNamespace + ".")) {
        return base;
      }
      if (base.contains(".")) {
        throw new RuntimeException(
            "Dots are not allowed in element names, please remove it from " + base);
      }
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
      boolean flushCache,
      boolean useCache,
      KeyGenerator keyGenerator,
      String keyProperty,
      LanguageDriver lang) {
    // 给 id，resultMap加上namespace前缀形成唯一标识
    id = applyCurrentNamespace(id, false);
    // 是否是select语句
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
    MappedStatement.Builder statementBuilder =
        new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType)
            .resource(resource)
            .keyGenerator(keyGenerator)
            .keyProperty(keyProperty);

    // 结果映射，给 MappedStatement#resultMaps
    setStatementResultMap(resultMap, resultType, statementBuilder);
    setStatementCache(isSelect, flushCache, useCache, currentCache, statementBuilder);
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
      String[] resultMapNames = resultMap.split(",");
      for (String resultMapName : resultMapNames) {
        resultMaps.add(configuration.getResultMap(resultMapName.trim()));
      }
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

  /**
   * 注册&lt;ResultMap&gt;信息
   *
   * @param id resultId（不带命名空间）
   * @param type resultMap的type属性
   * @param resultMappings resultMap下的多个子节点
   * @return &lt;ResultMap&gt;信息
   */
  public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
    // resultMapId
    id = applyCurrentNamespace(id, false);
    ResultMap.Builder inlineResultMapBuilder =
        new ResultMap.Builder(configuration, id, type, resultMappings);
    ResultMap resultMap = inlineResultMapBuilder.build();
    configuration.addResultMap(resultMap);
    return resultMap;
  }

  /**
   * 创建ResultMapping对象
   *
   * @param resultType 结果类型
   * @param property 属性列
   * @param column 列名
   * @param flags ID，CONSTRUCTOR
   * @return 代表&lt;ResultMap&gt;中的一列信息
   */
  public ResultMapping buildResultMapping(
      Class<?> resultType, String property, String column, List<ResultFlag> flags) {

    Class<?> javaTypeClass = resolveResultJavaType(resultType, property, null);
    TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, null);

    ResultMapping.Builder builder =
        new ResultMapping.Builder(configuration, property, column, javaTypeClass)
            .typeHandler(typeHandlerInstance)
            .flags(flags);
    return builder.build();
  }

  /**
   * 解析返回类型的java类型
   *
   * @param resultType 结果类型
   * @param property 属性
   * @param javaType java类型
   * @return
   */
  private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
    if (javaType == null && property != null) {
      try {
        MetaClass metaResultType = MetaClass.forClass(resultType);
        javaType = metaResultType.getSetterType(property);
      } catch (Exception ignore) {
      }
    }
    if (javaType == null) {
      javaType = Object.class;
    }
    return javaType;
  }

  /**
   * Mapper构建助手
   * @param isSelect 是否是select语句
   * @param flushCache 刷新缓存标志
   * @param useCache 使用缓存
   * @param cache 缓存对象
   * @param statementBuilder builder
   */
  private void setStatementCache(
      boolean isSelect,
      boolean flushCache,
      boolean useCache,
      Cache cache,
      MappedStatement.Builder statementBuilder) {
    flushCache = valueOrDefault(flushCache, !isSelect);
    useCache = valueOrDefault(useCache, isSelect);
    statementBuilder.flushCacheRequired(flushCache);
    statementBuilder.useCache(useCache);
    statementBuilder.cache(cache);
  }

  /**
   * 使用建造者模式来构建一个缓存实例，然后将其添加到配置中
   * @param typeClass 缓存实现类的类类型如果为null，则默认使用PerpetualCache
   * @param evictionClass 缓存驱逐策略类类型如果为null，则默认使用FifoCache
   * @param flushInterval 缓存刷新间隔时间（毫秒）如果为null，则不自动刷新
   * @param size 缓存大小如果为null，则无大小限制
   * @param readWrite 是否为读写模式如果为true，则缓存将以读写模式运行；否则，将以只读模式运行
   * @param blocking 是否为阻塞模式如果为true，则缓存将以阻塞模式运行；否则，将以非阻塞模式运行
   * @param props 额外的属性配置可以为空
   * @return
   */
  public Cache useNewCache(
      Class<? extends Cache> typeClass,
      Class<? extends Cache> evictionClass,
      Long flushInterval,
      Integer size,
      boolean readWrite,
      boolean blocking,
      Properties props) {
    // 判断为null，则用默认值
    typeClass = valueOrDefault(typeClass, PerpetualCache.class);
    evictionClass = valueOrDefault(evictionClass, FifoCache.class);

    // 建造者模式构建 Cache [currentNamespace=cn.bugstack.mybatis.test.dao.IActivityDao]
    Cache cache =
        new CacheBuilder(currentNamespace)
            .implementation(typeClass)
            .addDecorator(evictionClass)
            .clearInterval(flushInterval)
            .size(size)
            .readWrite(readWrite)
            .blocking(blocking)
            .properties(props)
            .build();

    // 添加缓存
    configuration.addCache(cache);
    currentCache = cache;
    return cache;
  }

  private <T> T valueOrDefault(T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }
}
