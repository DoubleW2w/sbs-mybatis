package com.doublew2w.sbs.mybatis.builder.annotation;

import com.doublew2w.sbs.mybatis.annotation.Delete;
import com.doublew2w.sbs.mybatis.annotation.Insert;
import com.doublew2w.sbs.mybatis.annotation.Select;
import com.doublew2w.sbs.mybatis.annotation.Update;
import com.doublew2w.sbs.mybatis.binding.MapperMethod;
import com.doublew2w.sbs.mybatis.builder.MapperBuilderAssistant;
import com.doublew2w.sbs.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.KeyGenerator;
import com.doublew2w.sbs.mybatis.executor.keygen.NoKeyGenerator;
import com.doublew2w.sbs.mybatis.mapping.SqlCommandType;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.scripting.LanguageDriver;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.ResultHandler;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * mapper注解构建器
 *
 * @author: DoubleW2w
 * @date: 2024/9/18 4:20
 * @project: sbs-mybatis
 */
public class MapperAnnotationBuilder {

  private static final Set<Class<? extends Annotation>> statementAnnotationTypes =
      Stream.of(Select.class, Update.class, Insert.class, Delete.class).collect(Collectors.toSet());

  private final Configuration configuration;
  private final MapperBuilderAssistant assistant;
  private final Class<?> type;

  public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
    String resource = type.getName().replace('.', '/') + ".java (best guess)";
    this.assistant = new MapperBuilderAssistant(configuration, resource);
    this.configuration = configuration;
    this.type = type;
  }

  public void parse() {
    String resource = type.toString();
    if (!configuration.isResourceLoaded(resource)) {
      assistant.setCurrentNamespace(type.getName());
      Method[] methods = type.getMethods();
      for (Method method : methods) {
        if (!method.isBridge()) {
          // 解析语句
          parseStatement(method);
        }
      }
    }
  }

  void parseStatement(Method method) {
    // 获取参数类型
    final Class<?> parameterTypeClass = getParameterType(method);
    // 获取脚本语言驱动器
    final LanguageDriver languageDriver = getLanguageDriver(method);
    // 构建SQL语言
    final SqlSource sqlSource = buildSqlSource(method, parameterTypeClass, languageDriver);

    if (sqlSource != null) {
      final String mappedStatementId = type.getName() + "." + method.getName();
      SqlCommandType sqlCommandType = getSqlCommandType(method);
      KeyGenerator keyGenerator;
      String keyProperty = "id";
      if (SqlCommandType.INSERT.equals(sqlCommandType)
          || SqlCommandType.UPDATE.equals(sqlCommandType)) {
        keyGenerator =
            configuration.isUseGeneratedKeys() ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
      } else {
        keyGenerator = new NoKeyGenerator();
      }

      boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
      String resultMapId = null;
      if (isSelect) {
        resultMapId = parseResultMap(method);
      }
      // 调用助手类
      assistant.addMappedStatement(
          mappedStatementId,
          sqlSource,
          sqlCommandType,
          parameterTypeClass,
          resultMapId,
          getReturnType(method),
          keyGenerator,
          keyProperty,
          languageDriver);
    }
  }

  /**
   * 获取方法的参数类型
   *
   * @param method 方法
   * @return 参数类型
   */
  private Class<?> getParameterType(Method method) {
    Class<?> parameterType = null;
    Class<?>[] parameterTypes = method.getParameterTypes();
    for (Class<?> currentParameterType : parameterTypes) {
      if (!RowBounds.class.isAssignableFrom(currentParameterType)
          && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
        if (parameterType == null) {
          parameterType = currentParameterType;
        } else {
          // issue #135
          parameterType = MapperMethod.ParamMap.class;
        }
      }
    }
    return parameterType;
  }

  /**
   * 获取脚本语言驱动器
   *
   * @param method 方法
   * @return 结果
   */
  private LanguageDriver getLanguageDriver(Method method) {
    // Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
    // return configuration.getDefaultScriptingLanguageInstance();
    return configuration.getDefaultScriptingLanguageInstance();
  }

  private SqlSource buildSqlSource(
      Method method, Class<?> parameterType, LanguageDriver languageDriver) {
    try {
      Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
      if (sqlAnnotationType != null) {
        Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
        final String[] strings =
            (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
        return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
      }
      return null;
    } catch (Exception e) {
      throw new RuntimeException("Could not find value method on SQL annotation.  Cause: " + e);
    }
  }

  private Class<? extends Annotation> getSqlAnnotationType(Method method) {
    for (Class<? extends Annotation> type : statementAnnotationTypes) {
      Annotation annotation = method.getAnnotation(type);
      if (annotation != null) return type;
    }
    return null;
  }

  private SqlSource buildSqlSourceFromStrings(
      String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
    return languageDriver.createSqlSource(
        configuration, String.join(" ", strings).trim(), parameterTypeClass);
  }

  private SqlCommandType getSqlCommandType(Method method) {
    Class<? extends Annotation> type = getSqlAnnotationType(method);
    if (type == null) {
      return SqlCommandType.UNKNOWN;
    }
    return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
  }

  private String parseResultMap(Method method) {
    // generateResultMapName
    StringBuilder suffix = new StringBuilder();
    for (Class<?> c : method.getParameterTypes()) {
      suffix.append("-");
      suffix.append(c.getSimpleName());
    }
    if (suffix.length() < 1) {
      suffix.append("-void");
    }
    String resultMapId = type.getName() + "." + method.getName() + suffix;

    // 添加 ResultMap
    Class<?> returnType = getReturnType(method);
    assistant.addResultMap(resultMapId, returnType, new ArrayList<>());
    return resultMapId;
  }

  /** 重点：DAO 方法的返回类型，如果为 List 则需要获取集合中的对象类型 */
  private Class<?> getReturnType(Method method) {
    // 获取方法的返回类型
    Class<?> returnType = method.getReturnType();
    if (Collection.class.isAssignableFrom(returnType)) {
      // 如果是list，尝试获取其泛型参数类型
      Type returnTypeParameter = method.getGenericReturnType();
      // 检查返回类型是否为泛型
      if (returnTypeParameter instanceof ParameterizedType) {
        // 获取泛型参数的实际类型
        Type[] actualTypeArguments =
            ((ParameterizedType) returnTypeParameter).getActualTypeArguments();
        // 确保泛型参数单一的情况
        if (actualTypeArguments != null && actualTypeArguments.length == 1) {
          returnTypeParameter = actualTypeArguments[0];
          if (returnTypeParameter instanceof Class) { // 如果泛型参数是一个类，则设置其为返回类型
            returnType = (Class<?>) returnTypeParameter;
          } else if (returnTypeParameter instanceof ParameterizedType) { // 如果泛型参数本身是另一个泛型，则获取其原始类型
            // (issue #443) actual type can be a also a parameterized type
            returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
          } else if (returnTypeParameter instanceof GenericArrayType) { // 如果泛型参数是数组类型，则获取数组的元素类型
            Class<?> componentType =
                (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
            // (issue #525) support List<byte[]>
            returnType = Array.newInstance(componentType, 0).getClass();
          }
        }
      }
    }
    return returnType;
  }
}
