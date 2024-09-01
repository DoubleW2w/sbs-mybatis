package com.doublew2w.sbs.mybatis.binding;

import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * 映射器方法类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 22:56
 * @project: sbs-mybatis
 */
public class MapperMethod {

  private final SqlCommand command;

  public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
    this.command = new SqlCommand(configuration, mapperInterface, method);
  }

  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result = null;
    switch (command.getType()) {
      case INSERT:
        break;
      case DELETE:
        break;
      case UPDATE:
        break;
      case SELECT:
        result = sqlSession.selectOne(command.getName(), args);
        break;
      default:
        throw new RuntimeException("Unknown execution method for: " + command.getName());
    }
    return result;
  }

  /** SQL 指令 */
  public static class SqlCommand {
    /** SQL映射语句唯一标识 */
    private final String name;

    /** SQL映射语句类型 */
    private final SqlCommandType type;

    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
      String statementName = mapperInterface.getName() + "." + method.getName();
      MappedStatement ms = configuration.getMappedStatement(statementName);
      name = ms.getId();
      type = ms.getSqlCommandType();
    }

    public String getName() {
      return name;
    }

    public SqlCommandType getType() {
      return type;
    }
  }
}
