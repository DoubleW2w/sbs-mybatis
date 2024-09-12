package com.doublew2w.sbs.mybatis.builder;

import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.type.TypeAliasRegistry;
import com.doublew2w.sbs.mybatis.type.TypeHandlerRegistry;
import lombok.Getter;

/**
 * 建造者模式：建造者基类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 16:33
 * @project: sbs-mybatis
 */
public abstract class BaseBuilder {
  /** 配置类 */
  protected final Configuration configuration;
  /**
   * 类型别名注册机
   */
  protected final TypeAliasRegistry typeAliasRegistry;
  /**
   * 类型处理器注册机
   */
  protected final TypeHandlerRegistry typeHandlerRegistry;

  public BaseBuilder(Configuration configuration) {
    this.configuration = configuration;
    this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  protected Class<?> resolveAlias(String alias) {
    return typeAliasRegistry.resolveAlias(alias);
  }
}
