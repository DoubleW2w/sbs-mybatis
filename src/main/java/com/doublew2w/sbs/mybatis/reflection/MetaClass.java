package com.doublew2w.sbs.mybatis.reflection;

import com.doublew2w.sbs.mybatis.reflection.invoker.GetFieldInvoker;
import com.doublew2w.sbs.mybatis.reflection.invoker.Invoker;
import com.doublew2w.sbs.mybatis.reflection.invoker.MethodInvoker;
import com.doublew2w.sbs.mybatis.reflection.property.PropertyTokenizer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * 元类：提供了对 Java 类的元数据的访问。这包括类的属性、方法、构造函数等信息。
 *
 * @author: DoubleW2w
 * @date: 2024/9/5 3:28
 * @project: sbs-mybatis
 */
public class MetaClass {

  private final Reflector reflector;

  private MetaClass(Class<?> type) {
    this.reflector = Reflector.forClass(type);
  }

  public static MetaClass forClass(Class<?> type) {
    return new MetaClass(type);
  }

  public static boolean isClassCacheEnabled() {
    return Reflector.isClassCacheEnabled();
  }

  public static void setClassCacheEnabled(boolean classCacheEnabled) {
    Reflector.setClassCacheEnabled(classCacheEnabled);
  }


  /**
   * 获取指定属性名称对应的元类（MetaClass）。
   *
   * @param name 属性名称
   * @return 属性对应的元类
   */
  public MetaClass metaClassForProperty(String name) {
    Class<?> propType = reflector.getGetterType(name);
    return MetaClass.forClass(propType);
  }

  /**
   * 根据名称查找属性
   *
   * @param name 属性名称
   * @return 如果找到了与给定名称对应的属性值，则返回该值的字符串表示；否则返回null
   */
  public String findProperty(String name) {
    StringBuilder prop = buildProperty(name, new StringBuilder());
    return prop.length() > 0 ? prop.toString() : null;
  }

  /**
   * 根据名称查找属性，并可选择是否使用驼峰式命名法映射。
   *
   * @param name 属性名称
   * @param useCamelCaseMapping 是否使用驼峰式命名法映射
   * @return 找到的属性值，如果未找到则返回null
   */
  public String findProperty(String name, boolean useCamelCaseMapping) {
    if (useCamelCaseMapping) {
      name = name.replace("_", "");
    }
    return findProperty(name);
  }

  /**
   * 获取到元类的getter方法名称列表
   *
   * @return getter方法名称列表
   */
  public String[] getGetterNames() {
    return reflector.getGetablePropertyNames();
  }

  /**
   * 获取到元类的setter方法名称列表
   *
   * @return setter方法名称列表
   */
  public String[] getSetterNames() {
    return reflector.getSetablePropertyNames();
  }

  /**
   * 根据属性名称获取到setter方法的参数类型
   *
   * @param name 属性名称
   */
  public Class<?> getSetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop.getName());
      return metaProp.getSetterType(prop.getChildren());
    } else {
      return reflector.getSetterType(prop.getName());
    }
  }

  /**
   * 根据属性名称获取getter方法的返回类型
   *
   * @param name 属性名称，用于获取getter方法类型
   * @return getter方法的返回类型，如果无法确定类型，则返回null
   */
  public Class<?> getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop);
      return metaProp.getGetterType(prop.getChildren());
    }
    // issue #506. Resolve the type inside a Collection Object
    return getGetterType(prop);
  }

  private MetaClass metaClassForProperty(PropertyTokenizer prop) {
    Class<?> propType = getGetterType(prop);
    return MetaClass.forClass(propType);
  }

  /**
   * 根据属性信息获取getter方法的返回类型
   *
   * <p>首先尝试通过反射获取getter方法的返回类型， 并检查是否为集合类型
   *
   * <p>如果属性对应一个特定的索引且其getter方法的返回类型为集合， 则进一步解析泛型类型信息以获取集合中元素的确切类型
   *
   * @param prop 属性信息的令牌，包含属性名和索引等信息
   * @return getter方法的返回类型，或经过泛型解析后的类型
   */
  private Class<?> getGetterType(PropertyTokenizer prop) {
    // 通过反射获取getter方法的返回类型
    Class<?> type = reflector.getGetterType(prop.getName());

    // 如果属性有指定索引且返回类型为集合，则进一步解析泛型类型信息
    if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
      // 获取getter方法的泛型返回类型
      Type returnType = getGenericGetterType(prop.getName());

      // 如果返回类型是参数化类型（即带有泛型参数的类型）
      if (returnType instanceof ParameterizedType) {
        // 获取泛型参数的实际类型参数数组
        Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();

        // 如果泛型参数只有一个且不为null，则重新设置返回类型为泛型参数类型
        if (actualTypeArguments != null && actualTypeArguments.length == 1) {
          returnType = actualTypeArguments[0];

          // 如果泛型参数是一个具体类，则更新返回类型
          if (returnType instanceof Class) {
            type = (Class<?>) returnType;
          } else if (returnType instanceof ParameterizedType) {
            // 如果泛型参数是参数化类型，则更新返回类型为该类型的原始类型
            type = (Class<?>) ((ParameterizedType) returnType).getRawType();
          }
        }
      }
    }

    // 返回getter方法的返回类型
    return type;
  }

  /**
   * 通过属性名称获取泛型的getter方法返回类型
   *
   * @param propertyName 属性名称
   * @return getter方法返回类型
   */
  private Type getGenericGetterType(String propertyName) {
    try {
      Invoker invoker = reflector.getGetInvoker(propertyName);
      if (invoker instanceof MethodInvoker) {
        Field declaredMethod = MethodInvoker.class.getDeclaredField("method");
        declaredMethod.setAccessible(true);
        Method method = (Method) declaredMethod.get(invoker);
        // 返回Method对象的泛型返回类型
        return method.getGenericReturnType();
      } else if (invoker instanceof GetFieldInvoker) {
        Field declaredField = GetFieldInvoker.class.getDeclaredField("field");
        declaredField.setAccessible(true);
        Field field = (Field) declaredField.get(invoker);
        // 返回Field对象的泛型类型
        return field.getGenericType();
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // Ignored
    }
    return null;
  }

  public boolean hasSetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (reflector.hasSetter(prop.getName())) {
        MetaClass metaProp = metaClassForProperty(prop.getName());
        return metaProp.hasSetter(prop.getChildren());
      } else {
        return false;
      }
    } else {
      return reflector.hasSetter(prop.getName());
    }
  }

  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (reflector.hasGetter(prop.getName())) {
        MetaClass metaProp = metaClassForProperty(prop);
        return metaProp.hasGetter(prop.getChildren());
      } else {
        return false;
      }
    } else {
      return reflector.hasGetter(prop.getName());
    }
  }

  public Invoker getGetInvoker(String name) {
    return reflector.getGetInvoker(name);
  }

  public Invoker getSetInvoker(String name) {
    return reflector.getSetInvoker(name);
  }

  /**
   * 构建属性字符串
   *
   * <p>该方法用于构建一个属性的字符串表示形式，包括处理嵌套属性
   *
   * @param name 属性名称，可以是嵌套属性的路径形式
   * @param builder 字符串构建器，用于高效构建属性字符串
   * @return 返回构建后的字符串构建器
   */
  private StringBuilder buildProperty(String name, StringBuilder builder) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      String propertyName = reflector.findPropertyName(prop.getName());
      if (propertyName != null) {
        builder.append(propertyName);
        builder.append(".");
        MetaClass metaProp = metaClassForProperty(propertyName);
        metaProp.buildProperty(prop.getChildren(), builder);
      }
    } else {
      String propertyName = reflector.findPropertyName(name);
      if (propertyName != null) {
        builder.append(propertyName);
      }
    }
    return builder;
  }

  public boolean hasDefaultConstructor() {
    return reflector.hasDefaultConstructor();
  }
}
