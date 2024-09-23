package com.doublew2w.sbs.mybatis.plugin;

import cn.hutool.core.map.MapUtil;
import com.doublew2w.sbs.mybatis.reflection.ExceptionUtil;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 代理模式插件
 *
 * @author: DoubleW2w
 * @date: 2024/9/23 19:57
 * @project: sbs-mybatis
 */
public class Plugin implements InvocationHandler {
  /** 调用对象 */
  private final Object target;

  /** 插件 */
  private final Interceptor interceptor;

  /** 类上的方法签名 */
  private final Map<Class<?>, Set<Method>> signatureMap;

  private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }

  /**
   * 根据目标对象和拦截器，返回一个代理对象，该代理对象在调用方法时会经过拦截器的处理
   *
   * <p>如果目标对象的类没有实现任何接口，则直接返回目标对象
   *
   * @param target 被代理的目标对象
   * @param interceptor 拦截器
   * @return 代理对象或原始目标对象，视情况而定
   */
  public static Object wrap(Object target, Interceptor interceptor) {
    // 从拦截器中获取签名映射，用于后续判断哪些方法需要被拦截
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    //  获取目标对象类实现的所有接口，并且这些接口中的方法需要在拦截器中处理
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      return Proxy.newProxyInstance(
          type.getClassLoader(), interfaces, new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        return interceptor.intercept(new Invocation(target, method, args));
      }
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }

  /**
   * 获取插件上
   *
   * @param interceptor
   * @return
   */
  private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
    // issue #251
    if (interceptsAnnotation == null) {
      throw new RuntimeException(
          "No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
    }
    Signature[] sigs = interceptsAnnotation.value();
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
    for (Signature sig : sigs) {
      Set<Method> methods = MapUtil.computeIfAbsent(signatureMap, sig.type(), k -> new HashSet<>());
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(
            "Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e,
            e);
      }
    }
    return signatureMap;
  }

  /**
   * 获取到类及其超类的，所有接口实现的对象
   *
   * @param type 类
   * @param signatureMap 包含已知接口及其方法集映射的Map
   * @return 所有找到的接口类型的数组
   */
  private static Class<?>[] getAllInterfaces(
      Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<>();
    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[0]);
  }
}
