package com.doublew2w.sbs.mybatis.datasource.unpooled;

import com.doublew2w.sbs.mybatis.io.Resources;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;

/**
 * 池化数据源实现
 *
 * @author: DoubleW2w
 * @date: 2024/9/3 16:33
 * @project: sbs-mybatis
 */
public class UnpooledDataSource implements DataSource {
  @Getter @Setter private ClassLoader driverClassLoader;

  /** 驱动配置，也可以扩展属性信息 driver.encoding=UTF8 */
  @Setter @Getter private Properties driverProperties;

  /** 驱动注册器 */
  @Setter @Getter private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

  /** 驱动 */
  @Setter @Getter private String driver;

  /** DB连接url */
  @Setter @Getter private String url;

  /** 账号 */
  @Setter @Getter private String username;

  /** 密码 */
  @Setter @Getter private String password;

  /** 是否自动提交 */
  @Setter @Getter private Boolean autoCommit;

  /** 事务级别 */
  @Setter @Getter private Integer defaultTransactionIsolationLevel;

  // 已注册的 JDBC 驱动程序在类的使用之前被缓存到 `registeredDrivers` 中，方便后续使用
  static {
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements()) {
      Driver driver = drivers.nextElement();
      registeredDrivers.put(driver.getClass().getName(), driver);
    }
  }

  public UnpooledDataSource() {}

  public UnpooledDataSource(String driver, String url, String username, String password) {
    this.driver = driver;
    this.url = url;
    this.username = username;
    this.password = password;
  }

  /**
   * 初始化数据库连接
   *
   * @param username 用户名
   * @param password 密码
   * @return 数据库连接
   */
  private Connection doGetConnection(String username, String password) throws SQLException {
    Properties props = new Properties();
    if (driverProperties != null) {
      props.putAll(driverProperties);
    }
    if (username != null) {
      props.setProperty("user", username);
    }
    if (password != null) {
      props.setProperty("password", password);
    }
    return doGetConnection(props);
  }

  /**
   * 根据给定的属性初始化数据库连接、自动提交设置、事务隔离级别
   *
   * @param properties ，包含数据库连接所需的属性，如用户名和密码
   * @return 初始化并配置好的数据库连接
   * @throws SQLException 如果在过程中发生错误，例如无法建立连接或配置连接属性
   */
  private Connection doGetConnection(Properties properties) throws SQLException {
    // 初始化数据库驱动
    initializeDriver();
    // 使用给定的URL和属性建立数据库连接
    Connection connection = DriverManager.getConnection(url, properties);
    // Configuration Connection
    if (autoCommit != null && autoCommit != connection.getAutoCommit()) {
      connection.setAutoCommit(autoCommit);
    }
    if (defaultTransactionIsolationLevel != null) {
      connection.setTransactionIsolation(defaultTransactionIsolationLevel);
    }
    return connection;
  }

  /**
   * 使用同步方法初始化数据库驱动程序，以确保多线程环境下的线程安全
   *
   * <p>如果驱动程序尚未注册，则根据给定的驱动程序名称和类加载器进行注册
   *
   * @throws SQLException 如果驱动程序初始化失败
   */
  private synchronized void initializeDriver() throws SQLException {
    if (!registeredDrivers.containsKey(driver)) {
      Class<?> driverType;
      try {
        // 尝试使用指定的类加载器加载驱动程序类
        if (driverClassLoader != null) {
          driverType = Class.forName(driver, true, driverClassLoader);
        } else {
          driverType = Resources.classForName(driver);
        }
        // DriverManager requires the driver to be loaded via the system ClassLoader.
        // https://www.kfu.com/~nsayer/Java/dyn-jdbc.html
        Driver driverInstance = (Driver) driverType.getDeclaredConstructor().newInstance();
        DriverManager.registerDriver(new DriverProxy(driverInstance));
        registeredDrivers.put(driver, driverInstance);
      } catch (Exception e) {
        throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
      }
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return doGetConnection(username, password);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return doGetConnection(username, password);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return DriverManager.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    DriverManager.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    DriverManager.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return DriverManager.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException(getClass().getName() + " is not a wrapper.");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  /** */
  private static class DriverProxy implements Driver {
    private Driver driver;

    DriverProxy(Driver d) {
      this.driver = d;
    }

    @Override
    public boolean acceptsURL(String u) throws SQLException {
      return this.driver.acceptsURL(u);
    }

    @Override
    public Connection connect(String u, Properties p) throws SQLException {
      return this.driver.connect(u, p);
    }

    @Override
    public int getMajorVersion() {
      return this.driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
      return this.driver.getMinorVersion();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
      return this.driver.getPropertyInfo(u, p);
    }

    @Override
    public boolean jdbcCompliant() {
      return this.driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() {
      return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
  }
}
