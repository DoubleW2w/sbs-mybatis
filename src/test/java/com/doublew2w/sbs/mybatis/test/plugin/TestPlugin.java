package com.doublew2w.sbs.mybatis.test.plugin;

import com.doublew2w.sbs.mybatis.executor.statement.StatementHandler;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.plugin.Interceptor;
import com.doublew2w.sbs.mybatis.plugin.Intercepts;
import com.doublew2w.sbs.mybatis.plugin.Invocation;
import com.doublew2w.sbs.mybatis.plugin.Signature;
import java.sql.Connection;
import java.util.Properties;

/**
 * @author: DoubleW2w
 * @date: 2024/9/23 20:43
 * @project: sbs-mybatis
 */
@Intercepts({
  @Signature(
      type = StatementHandler.class,
      method = "prepare",
      args = {Connection.class})
})
public class TestPlugin implements Interceptor {
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 获取StatementHandler
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    // 获取SQL信息
    BoundSql boundSql = statementHandler.getBoundSql();
    String sql = boundSql.getSql();
    // 输出SQL
    System.out.println("拦截SQL：" + sql);
    // 放行
    return invocation.proceed();
  }

  @Override
  public void setProperties(Properties properties) {
    System.out.println("参数输出：" + properties.getProperty("test00"));
  }
}
