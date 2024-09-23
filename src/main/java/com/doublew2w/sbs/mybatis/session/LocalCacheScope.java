package com.doublew2w.sbs.mybatis.session;

/**
 * 本地缓存机制；
 *
 * <p>SESSION 默认值，缓存一个会话中执行的所有查询
 *
 * <p>STATEMENT 本地会话仅用在语句执行上，对相同 SqlSession 的不同调用将不做数据共享
 *
 * @author: DoubleW2w
 * @date: 2024/9/24 3:54
 * @project: sbs-mybatis
 */
public enum LocalCacheScope {
  SESSION,
  STATEMENT
}
