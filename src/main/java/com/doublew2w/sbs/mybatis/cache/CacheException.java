package com.doublew2w.sbs.mybatis.cache;

/**
 * @author: DoubleW2w
 * @date: 2024/9/24 2:20
 * @project: sbs-mybatis
 */
public class CacheException extends RuntimeException {
  private static final long serialVersionUID = -193202262468464650L;

  public CacheException() {
    super();
  }

  public CacheException(String message) {
    super(message);
  }

  public CacheException(String message, Throwable cause) {
    super(message, cause);
  }

  public CacheException(Throwable cause) {
    super(cause);
  }
}
