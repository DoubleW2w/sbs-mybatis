package com.doublew2w.sbs.mybatis.datasource.pooled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

/**
 * 池化链接的代理
 *
 * @author: DoubleW2w
 * @date: 2024/9/3 17:17
 * @project: sbs-mybatis
 */
@Slf4j
public class PooledConnection implements InvocationHandler {
  /** 关闭方法名称 */
  private static final String CLOSE = "close";

  private static final Class<?>[] IFACES = new Class<?>[] {Connection.class};

  /** 池化链接的哈希码 */
  private int hashCode;

  /** 池化数据源 */
  private PooledDataSource dataSource;

  // 真实的连接
  private Connection realConnection;
  // 代理的连接
  private Connection proxyConnection;

  /** 连接被借用的时间戳（毫秒） */
  private long checkoutTimestamp;

  /** 连接被创建的时间戳（毫秒） */
  private long createdTimestamp;

  /** 最后被使用的时间戳（毫秒） */
  private long lastUsedTimestamp;

  /** 连接类型代码 */
  private int connectionTypeCode;

  /** 连接是否有效 */
  private boolean valid;

  public PooledConnection(Connection connection, PooledDataSource dataSource) {
    this.hashCode = connection.hashCode();
    this.realConnection = connection;
    this.dataSource = dataSource;
    this.createdTimestamp = System.currentTimeMillis();
    this.lastUsedTimestamp = System.currentTimeMillis();
    this.valid = true;
    this.proxyConnection =
        (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
  }

  private void checkConnection() throws SQLException {
    if (!valid) {
      throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    if (CLOSE.equals(methodName)) {
      dataSource.pushConnection(this);
      return null;
    } else {
      if (!Object.class.equals(method.getDeclaringClass())) {
        // issue #579 toString() should never fail
        // throw an SQLException instead of a Runtime
        checkConnection();
      }
      return method.invoke(realConnection, args);
    }
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PooledConnection) {
      return realConnection.hashCode() == (((PooledConnection) obj).realConnection.hashCode());
    } else if (obj instanceof Connection) {
      return hashCode == obj.hashCode();
    } else {
      return false;
    }
  }

  public void invalidate() {
    valid = false;
  }

  public boolean isValid() {
    return valid && realConnection != null && dataSource.pingConnection(this);
  }

  // getter 和 setter
  public Connection getRealConnection() {
    return realConnection;
  }

  public Connection getProxyConnection() {
    return proxyConnection;
  }

  public int getRealHashCode() {
    return realConnection == null ? 0 : realConnection.hashCode();
  }

  public int getConnectionTypeCode() {
    return connectionTypeCode;
  }

  public void setConnectionTypeCode(int connectionTypeCode) {
    this.connectionTypeCode = connectionTypeCode;
  }

  public long getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(long createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public long getLastUsedTimestamp() {
    return lastUsedTimestamp;
  }

  public void setLastUsedTimestamp(long lastUsedTimestamp) {
    this.lastUsedTimestamp = lastUsedTimestamp;
  }

  /** 获取上次使用以来经过的时间 ms */
  public long getTimeElapsedSinceLastUse() {
    return System.currentTimeMillis() - lastUsedTimestamp;
  }

  public long getAge() {
    return System.currentTimeMillis() - createdTimestamp;
  }

  public long getCheckoutTimestamp() {
    return checkoutTimestamp;
  }

  public void setCheckoutTimestamp(long timestamp) {
    this.checkoutTimestamp = timestamp;
  }

  public long getCheckoutTime() {
    return System.currentTimeMillis() - checkoutTimestamp;
  }
}
