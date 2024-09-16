package com.doublew2w.sbs.mybatis.binding;

import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import java.lang.reflect.Method;
import java.util.*;
import lombok.Getter;

/**
 * 映射器方法类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 22:56
 * @project: sbs-mybatis
 */
public class MapperMethod {

  private final SqlCommand command;
  private final MethodSignature method;

  public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
    this.command = new SqlCommand(configuration, mapperInterface, method);
    this.method = new MethodSignature(configuration, method);
  }

  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result = null;
    switch (command.getType()) {
      case INSERT:
        {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.insert(command.getName(), param);
          break;
        }
      case DELETE:
        {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.delete(command.getName(), param);
          break;
        }
      case UPDATE:
        {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.update(command.getName(), param);
          break;
        }
      case SELECT:
        {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.selectOne(command.getName(), param);
          break;
        }
      default:
        throw new RuntimeException("Unknown execution method for: " + command.getName());
    }
    return result;
  }

  /** SQL 指令 */
  @Getter
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
  }

  /** 方法签名 */
  public static class MethodSignature {
    private final SortedMap<Integer, String> params;

    public MethodSignature(Configuration configuration, Method method) {
      this.params = Collections.unmodifiableSortedMap(getParams(method));
    }

    public Object convertArgsToSqlCommandParam(Object[] args) {
      final int paramCount = params.size();
      if (args == null || paramCount == 0) {
        // 五参数
        return null;
      } else if (paramCount == 1) {
        return args[0];
      } else {
        // 否则，返回一个ParamMap，修改参数名，参数名就是其位置
        final Map<String, Object> param = new ParamMap<>();
        int i = 0;
        for (Map.Entry<Integer, String> entry : params.entrySet()) {
          // 1.先加一个#{0},#{1},#{2}...参数
          param.put(entry.getValue(), args[entry.getKey()]);
          // issue #71, add param names as param1, param2...but ensure backward compatibility
          final String genericParamName = "param" + (i + 1);
          if (!param.containsKey(genericParamName)) {
            /*
             * 2.再加一个#{param1},#{param2}...参数
             * 你可以传递多个参数给一个映射器方法。如果你这样做了,
             * 默认情况下它们将会以它们在参数列表中的位置来命名,比如:#{param1},#{param2}等。
             * 如果你想改变参数的名称(只在多参数情况下) ,那么你可以在参数上使用@Param(“paramName”)注解。
             */
            param.put(genericParamName, args[entry.getKey()]);
          }
          i++;
        }
        return param;
      }
    }

    private SortedMap<Integer, String> getParams(Method method) {
      final SortedMap<Integer, String> params = new TreeMap<>();
      final Class<?>[] argTypes = method.getParameterTypes();
      for (int i = 0; i < argTypes.length; i++) {
        String paramName = String.valueOf(params.size());
        params.put(i, paramName);
      }
      return params;
    }
  }

  /** 参数map，静态内部类,更严格的get方法，如果没有相应的key，报错 */
  public static class ParamMap<V> extends HashMap<String, V> {
    private static final long serialVersionUID = 2766452075403611396L;

    @Override
    public V get(Object key) {
      if (!super.containsKey(key)) {
        throw new RuntimeException(
            "Parameter '" + key + "' not found. Available parameters are " + keySet());
      }
      return super.get(key);
    }
  }
}
