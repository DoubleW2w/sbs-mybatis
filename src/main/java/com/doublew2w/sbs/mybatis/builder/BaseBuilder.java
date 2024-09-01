package com.doublew2w.sbs.mybatis.builder;

import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.type.TypeAliasRegistry;
import lombok.Getter;

/**
 * 建造者模式：建造者基类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 16:33
 * @project: sbs-mybatis
 */
public abstract class BaseBuilder {
  protected final Configuration configuration;
  protected final TypeAliasRegistry typeAliasRegistry;

  public BaseBuilder(Configuration configuration) {
    this.configuration = configuration;
    this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
  }

  public Configuration getConfiguration() {
    return configuration;
  }
}
