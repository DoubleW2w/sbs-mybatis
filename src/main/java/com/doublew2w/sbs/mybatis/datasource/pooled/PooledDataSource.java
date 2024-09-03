package com.doublew2w.sbs.mybatis.datasource.pooled;

import com.doublew2w.sbs.mybatis.datasource.unpooled.UnpooledDataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;

/**
 * 池化数据源
 *
 * @author: DoubleW2w
 * @date: 2024/9/3 17:13
 * @project: sbs-mybatis
 */
public class PooledDataSource implements DataSource {
  private org.slf4j.Logger logger = LoggerFactory.getLogger(PooledDataSource.class);

  /** 池状态 */
  @Getter private final PoolState state = new PoolState(this);

  /** 数据源 */
  @Getter private final UnpooledDataSource dataSource;

  /** 活跃连接数 */
  @Setter @Getter protected int poolMaximumActiveConnections = 10;

  /** 空闲连接数 */
  @Setter @Getter protected int poolMaximumIdleConnections = 5;

  /** 在被强制返回之前,池中连接被检查的时间 */
  @Setter @Getter protected int poolMaximumCheckoutTime = 20000;

  /** 这是给连接池一个打印日志状态机会的低层次设置,还有重新尝试获得连接, 这些情况下往往需要很长时间 为了避免连接池没有配置时静默失败)。 */
  @Setter @Getter protected int poolTimeToWait = 20000;

  /**
   * 发送到数据的侦测查询,用来验证连接是否正常工作,并且准备 接受请求。
   *
   * <p>默认是“NO PING QUERY SET” ,这会引起许多数据库驱动连接由一 个错误信息而导致失败
   */
  @Setter @Getter protected String poolPingQuery = "NO PING QUERY SET";

  /** 开启或禁用侦测查询 */
  @Setter @Getter protected boolean poolPingEnabled = false;

  /** 用来配置 poolPingQuery 多次时间被用一次 */
  @Setter @Getter protected int poolPingConnectionsNotUsedFor = 0;

  @Setter @Getter private int expectedConnectionTypeCode;

  public PooledDataSource() {
    this.dataSource = new UnpooledDataSource();
  }

  @Override
  public Connection getConnection() throws SQLException {
    return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return popConnection(username, password).getProxyConnection();
  }

  /**
   * 移除活跃连接
   *
   * <p>若当前空闲链接数量少于设定值，就实例化新的DB连接放入空闲列表中，并将原连接标记为无效
   *
   * <p>通知等待线程，有新链接可以使用
   *
   * <p>若空闲连接数量充足，则关闭当前接连，并标记为无效
   *
   * @param connection 连接
   * @throws SQLException
   */
  protected void pushConnection(PooledConnection connection) throws SQLException {
    synchronized (state) {
      state.activeConnections.remove(connection);
      // 判断链接是否有效
      if (connection.isValid()) {
        // 如果空闲链接小于设定数量，也就是太少时
        if (state.idleConnections.size() < poolMaximumIdleConnections
            && connection.getConnectionTypeCode() == expectedConnectionTypeCode) {
          state.accumulatedCheckoutTime += connection.getCheckoutTime();
          // 它首先检查数据库连接是否处于自动提交模式，如果不是，则调用rollback()方法执行回滚操作。
          // 在MyBatis中，如果没有开启自动提交模式，则需要手动提交或回滚事务。因此，这段代码可能是在确保操作完成后，如果没有开启自动提交模式，则执行回滚操作。
          // 总的来说，这段代码用于保证数据库的一致性，确保操作完成后，如果未开启自动提交模式，则执行回滚操作。
          if (!connection.getRealConnection().getAutoCommit()) {
            connection.getRealConnection().rollback();
          }
          // 实例化一个新的DB连接，加入到idle列表
          PooledConnection newConnection =
              new PooledConnection(connection.getRealConnection(), this);
          state.idleConnections.add(newConnection);
          newConnection.setCreatedTimestamp(connection.getCreatedTimestamp());
          newConnection.setLastUsedTimestamp(connection.getLastUsedTimestamp());
          connection.invalidate();
          logger.info("Returned connection " + newConnection.getRealHashCode() + " to pool.");
          // 通知其他线程可以来抢DB连接了
          state.notifyAll();
        } else { // 否则，空闲链接还比较充足
          state.accumulatedCheckoutTime += connection.getCheckoutTime();
          if (!connection.getRealConnection().getAutoCommit()) {
            connection.getRealConnection().rollback();
          }
          // 将connection关闭
          connection.getRealConnection().close();
          logger.info("Closed connection " + connection.getRealHashCode() + ".");
          connection.invalidate();
        }
      } else {
        logger.info(
            "A bad connection ("
                + connection.getRealHashCode()
                + ") attempted to return to the pool, discarding connection.");
        state.badConnectionCount++;
      }
    }
  }

  /**
   * 从连接池中获取一个可用的数据库连接。
   *
   * <p>优先从空闲连接队列中获取，如果没有空闲连接，则根据当前活跃连接数决定是否创建新连接或等待。 如
   *
   * <p>果获取到的连接无效，则尝试重新获取，超过一定次数后抛出异常。
   *
   * @param username 数据库用户名
   * @param password 数据库密码
   * @return 获取到的有效数据库连接
   * @throws SQLException 如果无法获取有效连接或发生其他数据库错误
   */
  private PooledConnection popConnection(String username, String password) throws SQLException {
    boolean countedWait = false;
    PooledConnection conn = null;
    long t = System.currentTimeMillis();
    int localBadConnectionCount = 0;

    while (conn == null) {
      synchronized (state) {
        // 如果有空闲链接：返回第一个
        if (!state.idleConnections.isEmpty()) {
          conn = state.idleConnections.remove(0);
          logger.info("Checked out connection " + conn.getRealHashCode() + " from pool.");
        } else { // 如果无空闲链接
          // 创建新的链接，判断活跃连接数的请苦情
          if (state.activeConnections.size() < poolMaximumActiveConnections) {
            // 活跃连接数不足，就创建一个
            conn = new PooledConnection(dataSource.getConnection(), this);
            logger.info("Created connection " + conn.getRealHashCode() + ".");
          } else {
            // 活跃连接数已满，判断最老的一个数据库链接情况，如果存在过期的情况就进行重新实例化
            PooledConnection oldestActiveConnection = state.activeConnections.get(0);
            long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
            // 如果 checkout 时间过长，则这个链接标记为过期
            if (longestCheckoutTime > poolMaximumCheckoutTime) {
              state.claimedOverdueConnectionCount++;
              state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
              state.accumulatedCheckoutTime += longestCheckoutTime;
              state.activeConnections.remove(oldestActiveConnection);
              if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                oldestActiveConnection.getRealConnection().rollback();
              }
              // 删掉最老的链接，然后重新实例化一个新的链接
              conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
              oldestActiveConnection.invalidate();
              logger.info("Claimed overdue connection " + conn.getRealHashCode() + ".");
            } else {
              // 如果checkout超时时间不够长，则等待
              try {
                if (!countedWait) {
                  state.hadToWaitCount++;
                  countedWait = true;
                }
                logger.info(
                    "Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                long wt = System.currentTimeMillis();
                // 等待连接池中连接中过期
                state.wait(poolTimeToWait);
                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
              } catch (InterruptedException e) {
                break;
              }
            }
          }
        }
        // 获得到链接
        if (conn != null) {
          if (conn.isValid()) {
            if (!conn.getRealConnection().getAutoCommit()) {
              conn.getRealConnection().rollback();
            }
            conn.setConnectionTypeCode(
                assembleConnectionTypeCode(dataSource.getUrl(), username, password));
            // 记录checkout时间
            conn.setCheckoutTimestamp(System.currentTimeMillis());
            conn.setLastUsedTimestamp(System.currentTimeMillis());
            state.activeConnections.add(conn);
            state.requestCount++;
            state.accumulatedRequestTime += System.currentTimeMillis() - t;
          } else {
            logger.info(
                "A bad connection ("
                    + conn.getRealHashCode()
                    + ") was returned from the pool, getting another connection.");
            // 如果没拿到，统计信息：失败链接 +1
            state.badConnectionCount++;
            localBadConnectionCount++;
            conn = null;
            // 失败次数较多，抛异常
            if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
              logger.debug("PooledDataSource: Could not get a good connection to the database.");
              throw new SQLException(
                  "PooledDataSource: Could not get a good connection to the database.");
            }
          }
        }
      }
    }

    if (conn == null) {
      logger.debug(
          "PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
      throw new SQLException(
          "PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
    }
    return conn;
  }

  /**
   * 强制当前数据源关闭所有连接
   *
   * <p>1. 关闭活跃连接
   *
   * <p>2.关闭空闲连接
   *
   * <p>如果连接的autocommit 为false，则回滚事务，最后关闭连接
   */
  public void forceCloseAll() {
    synchronized (state) {
      expectedConnectionTypeCode =
          assembleConnectionTypeCode(
              dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
      // 关闭活跃链接
      for (int i = state.activeConnections.size(); i > 0; i--) {
        try {
          PooledConnection conn = state.activeConnections.remove(i - 1);
          conn.invalidate();

          Connection realConn = conn.getRealConnection();
          if (!realConn.getAutoCommit()) {
            realConn.rollback();
          }
          realConn.close();
        } catch (Exception ignore) {

        }
      }
      // 关闭空闲链接
      for (int i = state.idleConnections.size(); i > 0; i--) {
        try {
          PooledConnection conn = state.idleConnections.remove(i - 1);
          conn.invalidate();

          Connection realConn = conn.getRealConnection();
          if (!realConn.getAutoCommit()) {
            realConn.rollback();
          }
        } catch (Exception ignore) {
        }
      }
      logger.info("PooledDataSource forcefully closed/removed all connections.");
    }
  }

  /**
   * 检测数据库连接是否有效
   *
   * @param conn 数据库连接
   * @return true-有效，false-无效
   */
  protected boolean pingConnection(PooledConnection conn) {
    boolean result;
    // 尝试判断连接是否关闭，若未关闭则认为连接有效
    try {
      result = !conn.getRealConnection().isClosed();
    } catch (SQLException e) {
      logger.info("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
      result = false;
    }

    if (result) {
      if (poolPingEnabled) {
        if (poolPingConnectionsNotUsedFor >= 0
            && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
          try {
            logger.info("Testing connection " + conn.getRealHashCode() + " ...");
            Connection realConn = conn.getRealConnection();
            Statement statement = realConn.createStatement();
            ResultSet resultSet = statement.executeQuery(poolPingQuery);
            resultSet.close();
            if (!realConn.getAutoCommit()) {
              realConn.rollback();
            }
            logger.info("Connection " + conn.getRealHashCode() + " is GOOD!");
          } catch (Exception e) {
            logger.info(
                "Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
            try {
              conn.getRealConnection().close();
            } catch (SQLException ignore) {
              logger.info("Connection " + conn.getRealHashCode() + " close failed");
            }
            result = false;
            logger.info("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
          }
        }
      }
    }
    return result;
  }

  /**
   * 从代理连接获取真正的数据库连接
   *
   * @param conn 代理连接
   * @return 真正的数据库连接
   */
  public static Connection unwrapConnection(Connection conn) {
    // conn 是否代理类
    if (Proxy.isProxyClass(conn.getClass())) {
      // 获取代理类的调用处理器
      InvocationHandler handler = Proxy.getInvocationHandler(conn);
      if (handler instanceof PooledConnection) {
        // 返回真正数据库连接
        return ((PooledConnection) handler).getRealConnection();
      }
    }
    // 如果不是代理类，直接返回
    return conn;
  }

  private int assembleConnectionTypeCode(String url, String username, String password) {
    return ("" + url + username + password).hashCode();
  }

  protected void finalize() throws Throwable {
    forceCloseAll();
    super.finalize();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException(getClass().getName() + " is not a wrapper.");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return DriverManager.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter logWriter) throws SQLException {
    DriverManager.setLogWriter(logWriter);
  }

  @Override
  public void setLoginTimeout(int loginTimeout) throws SQLException {
    DriverManager.setLoginTimeout(loginTimeout);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return DriverManager.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  }

  public void setDriver(String driver) {
    dataSource.setDriver(driver);
    forceCloseAll();
  }

  public void setUrl(String url) {
    dataSource.setUrl(url);
    forceCloseAll();
  }

  public void setUsername(String username) {
    dataSource.setUsername(username);
    forceCloseAll();
  }

  public void setPassword(String password) {
    dataSource.setPassword(password);
    forceCloseAll();
  }
}
