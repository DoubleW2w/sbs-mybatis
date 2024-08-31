package com.doublew2w.sbs.mybatis.session;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 5:39
 * @project: sbs-mybatis
 */
public interface SqlSessionFactory {

  /**
   * 打开一个 session
   *
   * @return SqlSession
   */
  SqlSession openSession();
}
