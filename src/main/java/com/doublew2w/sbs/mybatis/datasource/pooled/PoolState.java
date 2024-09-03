package com.doublew2w.sbs.mybatis.datasource.pooled;

import java.util.ArrayList;
import java.util.List;

/**
 * 池状态
 *
 * @author: DoubleW2w
 * @date: 2024/9/3 17:17
 * @project: sbs-mybatis
 */
public class PoolState {
  protected PooledDataSource dataSource;

  // 空闲链接
  protected final List<PooledConnection> idleConnections = new ArrayList<>();
  // 活跃链接
  protected final List<PooledConnection> activeConnections = new ArrayList<>();

  // 请求次数
  protected long requestCount = 0;
  // 总请求时间
  protected long accumulatedRequestTime = 0;
  // 总连接时间
  protected long accumulatedCheckoutTime = 0;
  // 过期的连接数量
  protected long claimedOverdueConnectionCount = 0;
  // 过期连接的总时间
  protected long accumulatedCheckoutTimeOfOverdueConnections = 0;

  // 总等待时间
  protected long accumulatedWaitTime = 0;
  // 要等待的次数
  protected long hadToWaitCount = 0;
  // 失败连接次数
  protected long badConnectionCount = 0;

  public PoolState(PooledDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public synchronized long getRequestCount() {
    return requestCount;
  }

  public synchronized long getAverageRequestTime() {
    return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
  }

  /**
   * 获取平均等待时间
   *
   * @return 总的等待时间/要等待的次数
   */
  public synchronized long getAverageWaitTime() {
    return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
  }

  public synchronized long getHadToWaitCount() {
    return hadToWaitCount;
  }

  public synchronized long getBadConnectionCount() {
    return badConnectionCount;
  }

  public synchronized long getClaimedOverdueConnectionCount() {
    return claimedOverdueConnectionCount;
  }

  /**
   * 获取过期连接的平均连接时间
   *
   * @return 所有过期连接的连接时间 / 过期连接数量
   */
  public synchronized long getAverageOverdueCheckoutTime() {
    return claimedOverdueConnectionCount == 0
        ? 0
        : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
  }

  /** 获取平均连接时间 */
  public synchronized long getAverageCheckoutTime() {
    return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
  }

  public synchronized int getIdleConnectionCount() {
    return idleConnections.size();
  }

  public synchronized int getActiveConnectionCount() {
    return activeConnections.size();
  }
}
