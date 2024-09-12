package com.doublew2w.sbs.mybatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 通过类加载器获得resource的辅助类
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 16:44
 * @project: sbs-mybatis
 */
public class Resources {

  public static Reader getResourceAsReader(String resource) throws IOException {
    return new InputStreamReader(getResourceAsStream(resource));
  }

  /**
   * 以流对象的形式返回classpath中的资源
   *
   * @param resource 需要查找的资源
   * @return The resource 流形式的资源
   */
  public static InputStream getResourceAsStream(String resource) throws IOException {
    ClassLoader[] classLoaders = getClassLoaders();
    for (ClassLoader classLoader : classLoaders) {
      InputStream inputStream = classLoader.getResourceAsStream(resource);
      if (null != inputStream) {
        return inputStream;
      }
    }
    throw new IOException("Could not find resource " + resource);
  }

  /**
   * 加载一个类
   *
   * @param className - 类的名称
   * @return 加载过的类
   */
  public static Class<?> classForName(String className) throws ClassNotFoundException {
    return Class.forName(className);
  }

  /**
   * 获取类加载器列表
   *
   * @return
   */
  private static ClassLoader[] getClassLoaders() {
    return new ClassLoader[] {
      ClassLoader.getSystemClassLoader(), Thread.currentThread().getContextClassLoader()
    };
  }
}
