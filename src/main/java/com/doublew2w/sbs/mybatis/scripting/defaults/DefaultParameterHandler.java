package com.doublew2w.sbs.mybatis.scripting.defaults;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.executor.parameter.ParameterHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.ParameterMapping;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.type.JdbcType;
import com.doublew2w.sbs.mybatis.type.TypeHandler;
import com.doublew2w.sbs.mybatis.type.TypeHandlerRegistry;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的参数处理器
 *
 * @author: DoubleW2w
 * @date: 2024/9/13 22:17
 * @project: sbs-mybatis
 */
@Slf4j
public class DefaultParameterHandler implements ParameterHandler {

  private Configuration configuration;
  private final TypeHandlerRegistry typeHandlerRegistry;
  private final MappedStatement mappedStatement;
  private final Object parameterObject;
  private BoundSql boundSql;

  public DefaultParameterHandler(
      MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    this.mappedStatement = mappedStatement;
    this.configuration = mappedStatement.getConfiguration();
    this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
    this.parameterObject = parameterObject;
    this.boundSql = boundSql;
  }

  @Override
  public Object getParameterObject() {
    return parameterObject;
  }

  @Override
  public void setParameters(PreparedStatement ps) throws SQLException {
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null && !parameterMappings.isEmpty()) {
      for (int parameterIndex = 0; parameterIndex < parameterMappings.size(); parameterIndex++) {
        ParameterMapping parameterMapping = parameterMappings.get(parameterIndex);
        String propertyName = parameterMapping.getProperty();
        Object value;
        if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
          value = parameterObject;
        } else {
          // 通过 MetaObject.getValue 反射取得值设进去
          MetaObject metaObject = configuration.newMetaObject(parameterObject);
          value = metaObject.getValue(propertyName);
        }
        JdbcType jdbcType = parameterMapping.getJdbcType();

        // 设置参数
        log.info("根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value：{}", JSON.toJSONString(value));
        TypeHandler typeHandler = parameterMapping.getTypeHandler();
        typeHandler.setParameter(ps, parameterIndex + 1, value, jdbcType);
      }
    }
  }
}
