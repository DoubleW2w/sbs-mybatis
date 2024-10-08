## 创建简单的映射器代理工厂

>
代码分支：[01-mapper-proxy-factory](https://github.com/DoubleW2w/sbs-mybatis/tree/01-mapper-proxy-factory)

所谓的代理就是提供一种手段来控制对目标对象的访问。「代理」就像一个”经纪人“，”中介“一样的角色作用，避免其他对象直接访问目标对象。

映射器代理类包装「对数据库的操作」。

映射器代理工厂提供映射器代理类的实例化操作。

```java
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -8932997989399317814L;

  /** Sql会话 */
  private Map<String, String> sqlSession;

  /** 映射器接口类型 */
  private final Class<T> mapperInterface;

  public MapperProxy(Map<String, String> sqlSession, Class<T> mapperInterface) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 如果是 Object 类
    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    } else {
      return "你的映射器被代理了！" + sqlSession.get(mapperInterface.getName() + "." + method.getName());
    }
  }
}
```

- 实现 InvocationHandler 接口，将具体的操作逻辑封装在 `invoke()` 方法中
- 针对 `Object` 的方法，是不需要代理执行的

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409010515991.png"/>

## 映射器的注册和使用

> 代码分支：[02-mapper-registry](https://github.com/DoubleW2w/sbs-mybatis/tree/02-mapper-registry)

### S

为了解决以下问题：

1. 需要编码告知 `MapperProxyFactory` 要对哪个接口进行代理
2. 编写一个假的 `SqlSession` 处理实际调用接口时的返回结果

### T

所以，提供「注册机」负责注册映射器，比如完成包路径下的扫描注册。完善 `SqlSession`，将 `SqlSession`
定义数据库操作接口和获取 Mapper 的操作。
目的是为了提供一种功能服务，那么相应也要存在对应的功能服务工厂。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409011616508.png"/>

### A

```java
public class MapperRegistry {

  /** 将已添加的映射器代理加入到 HashMap */
  private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
    }
  }

  public void addMappers(String packageName) {
    Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
    for (Class<?> mapperClass : mapperSet) {
      addMapper(mapperClass);
    }
  }

  /**
   * 注册映射器
   *
   * @param type 映射器
   * @param <T> 映射器类型
   */
  public <T> void addMapper(Class<?> type) {
    /* Mapper 必须是接口才会注册 */
    if (type.isInterface()) {
      if (hasMapper(type)) {
        // 如果重复添加了，报错
        throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
      }
      // 注册映射器代理工厂
      knownMappers.put(type, new MapperProxyFactory<>(type));
    }
  }

  /**
   * 是否扫描该Mapper
   *
   * @param type Mapper
   * @return true - 已注册，false - 未注册
   * @param <T> 映射器类型
   */
  public <T> boolean hasMapper(Class<T> type) {
    return knownMappers.containsKey(type);
  }
}
```

测试方法：

```java

@Test
public void test_MapperProxyFactory() {
  // 1. 注册 Mapper
  MapperRegistry registry = new MapperRegistry();
  // 2. 扫描包路径
  registry.addMappers("com.doublew2w.sbs.mybatis.binding.test.dao");

  // 创建SqlSession工厂，创建SqlSession
  SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(registry);
  SqlSession sqlSession = sqlSessionFactory.openSession();

  // 3. 获取映射器对象
  IUserDao userDao = sqlSession.getMapper(IUserDao.class);

  // 4. 测试验证
  String res = userDao.queryUserName("10001");
  logger.info(res);
}
```

```
06:04:47.052 [main] INFO com.doublew2w.sbs.mybatis.test.binding.dao.IUserDaoApiTest -- 你被代理了！方法：com.doublew2w.sbs.mybatis.binding.test.dao.IUserDao 入参：["10001"]
```

### R

- `MapperRegistry` 提供包路径的扫描和注册映射器的代理类
- `SqlSession` 用于定义执行 SQL 标准、获取映射器等方面操作。
- `SqlSessionFactory` 是一个简单工厂模式，用于提供 `SqlSession` 服务，屏蔽创建细节，延迟创建过程。
- 涉及概念「映射器」、「代理类」、「接口标准」、「工厂模式」

#### 简单工厂模式实现

在简单工厂模式中，工厂类提供一个公开方法负责完成对象的创建。实现的逻辑相对简单，适用于只有少数的产品需要创建。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409010611050.png"/>

## Mapper XML 解析和使用

> 代码分支：[03-mapper-xml-parse](https://github.com/DoubleW2w/sbs-mybatis/tree/03-mapper-xml-parse)

### S

在 MyBatis 的核心逻辑中，重要的一个逻辑就是为接口生成一个代理类，类中包含了对 Mapper XML 文件中的 SQL 信息进行解析和处理类型

- 入参
- 出参
- 条件

### T

目标是完成 XML 文件的解析和使用。

- 提供一个全局的配置类 Configuration，存放 XML 的解析内容。
- 通过 IO 流读取 XML 配置文件，然后解析 XML 节点进行「SQL 解析」、「Mapper 注册」。

### A

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409012241359.png"/>

- `MappedStatement` 映射语句类封装了 Mapper XML 的 SQL 语句
- `MapperMehtod` 映射器方法类
  文件中映射语句，比如「映射语句唯一标识 id」、「SELECT|UPDATE|DELETE|INSERT 指令」、「出参、入参」等信息
- `XMLConfigBuilder` 继承 `BaseBuilder` 具有解析 XML 文件的内容
- `SqlSessionFactoryBuilder` 可以理解为 `SqlSessionFactory` 的工厂类，通过指定解析 XML 的 IO，启动流程

```java
  @Test
  public void test_SqlSessionFactory() throws IOException {
    // 1. 从SqlSessionFactory中获取SqlSession
    Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
    // 解析xml 文件，并将xml信息加载到 Configuration 类
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    SqlSession sqlSession = sqlSessionFactory.openSession();

    // 2. 获取映射器对象
    IUserDao userDao = sqlSession.getMapper(IUserDao.class);

    // 3. 测试验证
    String res = userDao.queryUserInfoById("10001");
    logger.info("测试结果：{}", res);
  }
}
```

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409012348732.png"/>

### R

#### 建造者模式

定义：一种「创建型设计模式」，使你能够分步骤创建复杂对象。 该模式允许你使用相同的创建代码生成不同类型和形式的对象。



虽然所谓的 <u> *分步骤创建* </u> 跟直接调用分步调用 setter 是一样的，但是在代码体现上，setter 方法返回 `void`，而 Builder 每一步都返回自身。

该模式可以避免“多个构造函数”的出现，它的重点主要是 **如何分步创建一个复杂对象**



<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409012347901.png"/>







例子 1 在 `MappedStatement` 类中。

```java
public class MappedStatement {

  public static class Builder {

    private MappedStatement mappedStatement = new MappedStatement();

    public Builder(){
			// 省略...
    }
    public MappedStatement build() {
      // 省略..
      return mappedStatement;
    }
  }
}
```

例子 2 在 `SqlSessionFactoryBuilder` 类中体现

```java
public class SqlSessionFactoryBuilder {

  public SqlSessionFactory build(Reader reader) {
    XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
    return build(xmlConfigBuilder.parse());
  }

  public SqlSessionFactory build(Configuration config) {
    return new DefaultSqlSessionFactory(config);
  }
}
```

## 数据源的解析、创建和使用

>
代码分支: [04-datasource-parse-create-use](https://github.com/DoubleW2w/sbs-mybatis/tree/04-datasource-parse-create-use)

### S

在解决映射器的代理，Mapper 映射，SQL 的简单解析，接下来应该连接「数据库」和执行 SQL 语句返回执行结果

### T

解析数据源的配置信息，并创建数据源，并使用数据源完成对数据库操作。

### A

- 关于数据源配置和创建数据源，在 `parse()` 方法添加对连接池，数据库配置的解析

- 关于执行 SQL，则需要从数据库连接池中获取数据库连接，并调用数据库操作方法

```java
public interface DataSourceFactory {
  /**
   * 设置数据源属性
   *
   * @param props 属性
   */
  void setProperties(Properties props);

  /**
   * 获取数据源
   *
   * @return 数据源
   */
  DataSource getDataSource();
}
```

```java
public class DruidDataSourceFactory implements DataSourceFactory {
  private Properties props;

  @Override
  public void setProperties(Properties props) {
    this.props = props;
  }

  @Override
  public DataSource getDataSource() {
    try (DruidDataSource dataSource = new DruidDataSource()) {
      dataSource.setDriverClassName(props.getProperty("driver"));
      dataSource.setUrl(props.getProperty("url"));
      dataSource.setUsername(props.getProperty("username"));
      dataSource.setPassword(props.getProperty("password"));
      return dataSource;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
```

- 数据源连接池引入 Druid
- 创建 DruidDataSourceFactory 来包装数据源功能
- 在 XMLConfigBuilder 解析 XML 配置操作中，对数据源的配置进行解析以及创建出相应的服务，存放到
  Configuration 的环境配置中。
- DefaultSqlSession#selectOne 方法中完成 SQL 的执行和结果封装

测试类如下：

```java

@Test
public void test_SqlSessionFactory() throws IOException {
  // 1. 从SqlSessionFactory中获取SqlSession
  SqlSessionFactory sqlSessionFactory =
          new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
  SqlSession sqlSession = sqlSessionFactory.openSession();

  // 2. 获取映射器对象
  IUserDao userDao = sqlSession.getMapper(IUserDao.class);

  // 3. 测试验证
  User user = userDao.queryUserInfoById(1L);
  logger.info("测试结果：{}", JSON.toJSONString(user));
}
```

测试流程：

1. 创建库表
2. 配置数据源
3. 配置 Mapper
4. 执行 SQL

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409020349928.png"/>

### R

真正的执行逻辑是在 `MapperProxy#invoke()` 中, 在创建映射器接口代理对象的时候，就会缓存对应的 MapperMethod 对象

```java
public class MapperProxy<T> implements InvocationHandler, Serializable {

  //省略...

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 如果是 Object 类
    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    } else {
      final MapperMethod mapperMethod = cachedMapperMethod(method);
      return mapperMethod.execute(sqlSession, args);
    }
  }
  // 省略...
}
```

在 `MapperProxy#invoke()` 会去调用 `mapperMethod.execute(sqlSession, args)` 方法，从而去执行真正的 `selectOne()`

```java
  public Object execute(SqlSession sqlSession, Object[] args) {
  Object result = null;
  switch (command.getType()) {
    case INSERT:
      break;
    case DELETE:
      break;
    case UPDATE:
      break;
    case SELECT:
      result = sqlSession.selectOne(command.getName(), args);
      break;
    default:
      throw new RuntimeException("Unknown execution method for: " + command.getName());
  }
  return result;
}
```

- 创建 `PreparedStatement` 类
- 设置参数
- 执行查询 `preparedStatement.executeQuery()`
- 封装结果，并返回



##  数据源池化的实现

> 代码分支: [05-pool-datasource](https://github.com/DoubleW2w/sbs-mybatis/tree/05-pool-datasource)


### S

MyBatis 有自己数据源实现，包括 UnpooledDataSource 和 PooledDataSource 的实现。对于创建成本高且高频使用的资源，需要将这些资源创建出来存放到一个「池子」中，需要时直接从「池子」中获取，使用完再进行使用，目的是有效提供资源的利用率。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409032232942.png"/>

### T

提供连接池中心，存放连接资源，并根据配置信息获取连接，包括活跃连接的最大数量和空闲连接的最大数据量。

- 当外部从连接池获取链接时，如果链接已满则会进行循环等待。
- 当关闭连接时，移除链接并标记连接状态。
- 在配置数据源时，会强制关闭数据库所有连接。

### A

注册 池化 和 无池化类型

```java
  public Configuration() {
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
    typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
    typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
  }
```

建立对应的「数据源工厂类」和「数据源类型」，池化数据连接需要被放入池子进行管理，单独创建一个类 `PooledConnection` ，`PoolState` 负责管理池子状态，包括请求时间，等待时间，总的等待次数，失败连接次数等信息。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409032250095.png"/>

池化数据源获取连接和关闭连接



<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/Mybatis%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90-05-%E6%95%B0%E6%8D%AE%E6%BA%90%E6%B1%A0%E5%8C%96%E6%8A%80%E6%9C%AF%E5%AE%9E%E7%8E%B0.drawio-1.png"/>



无池化数据源获取连接

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/Mybatis%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90-05-%E6%95%B0%E6%8D%AE%E6%BA%90%E6%B1%A0%E5%8C%96%E6%8A%80%E6%9C%AF%E5%AE%9E%E7%8E%B0.drawio-2.png"/>

代码流程

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/Mybatis%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90-05-%E6%95%B0%E6%8D%AE%E6%BA%90%E6%B1%A0%E5%8C%96%E6%8A%80%E6%9C%AF%E5%AE%9E%E7%8E%B0.drawio-3.png"/>



测试类

```java
  @Test
  public void test_PooledDataSource() throws Exception {
    // 1. 从SqlSessionFactory中获取SqlSession
    SqlSessionFactory sqlSessionFactory =
        new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
    SqlSession sqlSession = sqlSessionFactory.openSession();

    // 2. 获取映射器对象
    IUserDao userDao = sqlSession.getMapper(IUserDao.class);

    // 3. 测试验证
    for (int i = 0; i < 10; i++) {
      User user = userDao.queryUserInfoById(1L);
      logger.info("测试结果：{}", JSON.toJSONString(user));
    }
  }
```

```xml
<dataSource type="POOLED"></dataSource>
<dataSource type="UNPOOLED"></dataSource>
```

### R

这种池化思想可以理解为享元模式

#### 享元模式

一种结构型设计模式，通过共享多个对象「相同的内部状态」，减少内存使用，来支持大量细粒度的对象，提高访问效率。

涉及角色：

- 享元工厂（Flyweight Factory）：管理和创建享元对象，通常实现了对象池来存储和管理具体享元对象。工厂类确保返回现有的享元对象或者创建新的享元对象。
- 享元对象（Flyweight）：包含内部状态和外部状态。内部状态是享元对象共享的，外部状态，每个享元对象是不同的。游戏中的子弹对象，每个子弹有相同的形状和大小，但位置和速度是不同的。

```java
// 享元接口
public interface Bullet {
    void fire(int x, int y);  // 外部状态：位置
}

// 具体享元类
public class ConcreteBullet implements Bullet {
    private String shape;  // 内部状态：形状

    public ConcreteBullet(String shape) {
        this.shape = shape;
    }

    @Override
    public void fire(int x, int y) {
        System.out.println("Firing a " + shape + " bullet at (" + x + ", " + y + ")");
    }
}
```

```java
// 享元工厂
public class BulletFactory {
    private Map<String, Bullet> bullets = new HashMap<>();

    public Bullet getBullet(String shape) {
        Bullet bullet = bullets.get(shape);
        if (bullet == null) {
            bullet = new ConcreteBullet(shape);
            bullets.put(shape, bullet);
        }
        return bullet;
    }
}
```

```java
public class Client {
    public static void main(String[] args) {
        BulletFactory factory = new BulletFactory();

        Bullet bullet1 = factory.getBullet("small");  // 创建一个小型子弹对象
        bullet1.fire(10, 20);

        Bullet bullet2 = factory.getBullet("small");  // 从工厂获取同一个小型子弹对象
        bullet2.fire(15, 25);

        Bullet bullet3 = factory.getBullet("large");  // 创建一个大型子弹对象
        bullet3.fire(30, 40);
    }
}
```

在 Java 中 利用缓存来加速大量小对象的访问时间。

- `java.lang.Integer#valueOf(int)`

- `java.lang.Boolean#valueOf(boolean)`

## SQL 执行器的定义和使用

> 代码分支：[06-define-sql-executor-use](https://github.com/DoubleW2w/sbs-mybatis/tree/06-define-sql-executor-use)

### S

在 `SqlSession#selectOne()`，并把执行的查询逻辑放进里面，为了解耦 `SqlSession`，我们需要提供新的功能 Executor 来替代这部分的代码处理

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409040026081.png"/>

### A

- 提取出执行器的接口，定义出执行方法、事务获取和相应提交、回滚、关闭的定义。
- 执行器定义成抽象类，对过程内容进行模板模式的过程包装，具体过程交由子类实现。
- 对 SQL 的处理分为：简单处理和预处理，预处理中包括准备语句、参数化传递、执行查询，以及最后的结果封装和返回。

### T

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409041346992.png"/>

变成

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409041346880.png"/>

定义一个执行器 `Executor` 接口，使用执行器基类 `BaseExecutor` 定义模板流程，具体的查询实现交给子类

```java
public interface Executor {
  // todo:暂时无用
  ResultHandler NO_RESULT_HANDLER = null;

  /** 查询 */
  <E> List<E> query(
      MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql);
}


@Slf4j
public abstract class BaseExecutor implements Executor {

  protected Configuration configuration;
  protected Executor wrapper;

  private boolean closed;

  protected BaseExecutor(Configuration configuration) {
    this.configuration = configuration;
    this.wrapper = this;
  }

  @Override
  public <E> List<E> query(
      MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    log.info("执行器执行查询");
    return doQuery(ms, parameter, resultHandler, boundSql);
  }

  protected abstract <E> List<E> doQuery(
      MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql);
}
```

```java
@Slf4j
public class SimpleExecutor extends BaseExecutor {
  public SimpleExecutor(Configuration configuration) {
    super(configuration);
  }

  @Override
  protected <E> List<E> doQuery(
      MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
    try {
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, resultHandler, boundSql);
      Environment environment = configuration.getEnvironment();
      Connection connection = environment.getDataSource().getConnection();
      Statement stmt = handler.prepare(connection);
      handler.parameterize(stmt);
      return handler.query(stmt, resultHandler);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }
}
```

- `newStatementHandler()` 负责实例化语句处理器，方便后续对语句的处理
- `prepare()` 准备语句: 简单语句和预处理语句 
- `parameterize` 参数化语句, 负责完成语句参数的设置
- `query() ` 执行查询，执行 SQL 语句执行
- `ResultSetHandler` 结果集处理，通过调用 set 方法来设置结果

### R

#### 代码结构

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409041405364.png"/>

## MetaObject 元对象与反射的运用

> 代码分支：[07-meta-object-reflection](https://github.com/DoubleW2w/sbs-mybatis/tree/07-meta-object-reflection)

### S

在 `DataSourceFacotory` 中获取数据源存在硬编码，约定的配置比如 username, password 等是可以使用硬编码，但如果要进行扩展就很难知道其他的属性配置。

### T

实现元对象反射工具类，可以完成一个对象的属性的反射填充，提取出统一的设置和获取属性值的操作，并封装成一个工具包。

### A

#### 工程目录
<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409061555241.png"/>

- reflection.factory: 负责创建需要的对象，负责进行实例化
- reflection.invoker: 提供一个 `Invoker` 接口，将对象类中的属性值获取和设置可以分为 Field 字段的 get/set(「字段 setter 和 getter」`GetFieldInvoker` 和 `SetFieldInvoker`), 还有普通的 Method 的调用(「方法调用器」`MethodInvoker`)。
- reflection.property: 完成属性名称的分解和属性名称的转换，可以做一些方法转换属性名称。
- reflection.wrapper: 对象包装器，定义了标准的 get/set 方法处理以及属性操作等方法，进一步反射调用处理。

  <img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409061607600.png"/>

#### 反射器 Reflector

针对一个类的信息进行解耦，完成属性与方法之间的映射，并做缓存。

```java
public class Reflector {

  private static boolean classCacheEnabled = true;

  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  // 线程安全的缓存
  private static final Map<Class<?>, Reflector> REFLECTOR_MAP = new ConcurrentHashMap<>();

  private Class<?> type;
  // get 属性列表
  private String[] readablePropertyNames = EMPTY_STRING_ARRAY;
  // set 属性列表
  private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;
  // set 方法列表
  private Map<String, Invoker> setMethods = new HashMap<>();
  // get 方法列表
  private Map<String, Invoker> getMethods = new HashMap<>();
  // set 类型列表
  private Map<String, Class<?>> setTypes = new HashMap<>();
  // get 类型列表
  private Map<String, Class<?>> getTypes = new HashMap<>();
  // 构造函数
  private Constructor<?> defaultConstructor;

  private Map<String, String> caseInsensitivePropertyMap = new HashMap<String, String>();

  public Reflector(Class<?> clazz) {
    this.type = clazz;
    // 加入构造函数
    addDefaultConstructor(clazz);
    // 加入 getter
    addGetMethods(clazz);
    // 加入 setter
    addSetMethods(clazz);
    // 加入字段
    addFields(clazz);
    readablePropertyNames = getMethods.keySet().toArray(new String[0]);
    writeablePropertyNames = setMethods.keySet().toArray(new String[0]);
    for (String propName : readablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
    for (String propName : writeablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
  }
  //....省略
}
```
每一个类都有对应的一个反射器，当通过 Reflector 的构造函数创建出来时，会进行解析 clazz 信息。

#### 元类 MetaClass

MetaClass 依赖于 Reflector，不仅有基础的对象拆解功能，还能获取到 get/set 的 Invoker 方法。
```java
public class MetaClass {

  private final Reflector reflector;

  private MetaClass(Class<?> type) {
    this.reflector = Reflector.forClass(type);
  }

  public static MetaClass forClass(Class<?> type) {
    return new MetaClass(type);
  }
  //...省略
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

  //... 省略

  public boolean hasSetter(String name) {}

  public boolean hasGetter(String name) {}

  public Invoker getGetInvoker(String name) {}

  public Invoker getSetInvoker(String name) {}
  // ...省略
}
```
MetaClass 元类相当于是对我们需要处理对象的包装，解耦一个原对象，包装出一个元类。

#### 元对象 MetaObject
将元对象 MetaObject 理解成一个反射服务，它简化了对对象属性的读取、修改和访问操作。
- 获取属性值：getValue(String name)：根据属性名获取对象的属性值。支持嵌套属性的访问，例如 user.address.city。
- 设置属性值：setValue(String name, Object value)：根据属性名设置对象的属性值，支持嵌套属性的修改。
- 检测属性的可读写性：判断是否有 getter 和 setter
- 对象的包装：MetaObject 可以通过 ObjectWrapper 包装对象，提供对对象的统一操作接口。

### R

本次反射拆了几个功能：「属性」、「调用」、「实例化」、「对象包装」。 将这几个功能合起来变成 `MetaObject`、`MetaClass`。

在实现上基本采用的模式是 一个顶层接口定义规范，基类实现顶层接口可以完成默认实现，或者提供模板流程，具体的规范实现交给子类。





## 细化 XML 语句构建器，完善解析流程

> 代码分支: [08-xml-config-builder-parse-detail](https://github.com/DoubleW2w/sbs-mybatis/tree/08-xml-config-builder-parse-detail)

### S

在 XML 解析中，存在硬编码的问题，并且在预处理语句处理器 `PreparedStatementHandler#parameterize` 中，解析参数时，是写死的。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409120243359.png"/>



```java
  public void parameterize(Statement statement) throws SQLException {
    log.info(" 参数化处理：{}", JSON.toJSONString(parameterObject));
    PreparedStatement ps = (PreparedStatement) statement;
    ps.setLong(1, Long.parseLong(((Object[]) parameterObject)[0].toString()));
  }
```



### T

- 引入 XMLMapperBuilder 处理「映射」
- 引入 XMLStatementBudiler 处理「语句」
- 引入脚本语言驱动器，具体操作静态和动态 SQL 语句节点的解析，参考 Mybatis 源码中 OGNL 的处理方式。

### A

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/Mybatis%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90-08-%E7%BB%86%E5%8C%96xml%E8%AF%AD%E5%8F%A5%E6%9E%84%E5%BB%BA%E5%99%A8.drawio.svg"/>

在解析 SQL 源码的处理方式上，

### R



## 使用策略模式，调用参数处理器

> 代码分支: [09-type-handler-use](https://github.com/DoubleW2w/sbs-mybatis/tree/09-type-handler-use)

本节内容是解决下面的参数处理硬编码问题，应该在解析 XML 文件的时候就已经确定好类型，调用「某个方法」就可以完成参数处理的操作。

```java
  public void parameterize(Statement statement) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    ps.setLong(1, Long.parseLong(((Object[]) parameterObject)[0].toString()));
  }
```



策略模式允许我们将参数处理的逻辑封装在不同的策略类中，并在运行时动态选择合适的策略进行参数设置。

1. 定义参数处理策略接口 `ParameterHandler`
2. 实现不同的参数处理策略 
3. 修改 `PreparedStatementHandler` 使用策略
4. 配置策略

但是参数的相关信息，比如对应的「jdbcType」、「javaType」等信息存放在哪里呢，这个时候就在 `ParameterMapping` 类上。所以 `ParameterHandler` 应该会依赖 `ParameterMapping`；





类型处理

- TypeHandler
- BaseTypeHandler
- 继承 BaseTypeHandler 的各种实现

参数设置

- ParameterMapping
- ParameterMappingTokenHandler

参数使用



## 结果集处理器

> 代码分支：[10-result-set-handler](https://github.com/DoubleW2w/sbs-mybatis/tree/10-result-set-handler)



对于结果集的封装处理，流程大致如下：

1. 从 MapperXML 文件得到返回类型
2. 根据数据库查询结果
3. 通过反射类型进行实例化



*DefaultResultSetHandler.java*

```java
  private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
    List<T> list = new ArrayList<>();
    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();
      // 每次遍历行值
      while (resultSet.next()) {
        T obj = (T) clazz.getDeclaredConstructor().newInstance();
        for (int i = 1; i <= columnCount; i++) {
          Object value = resultSet.getObject(i);
          String columnName = metaData.getColumnName(i);
          String setMethod =
              "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
          Method method;
          if (value instanceof Timestamp) {
            method = clazz.getMethod(setMethod, Date.class);
          } else {
            method = clazz.getMethod(setMethod, value.getClass());
          }
          method.invoke(obj, value);
        }
        list.add(obj);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }
```

- ResultSetMetaData 存放着 「列的类型和属性」、「总共有多少列」，「第一列是否可以用于 where 语句」中等信息
- 遍历结果集
- 根据「结果类型」创建一个「结果对象」
- 获取到「列的值」，找到对应的「setter」，并进行调用
- 将「结果对象」放入到「结果集合」
- 最终返回



```java
  public List<Object> handleResultSets(Statement stmt) throws SQLException {
    final List<Object> multipleResults = new ArrayList<>();
    int resultSetCount = 0;
    // 1.结果集包装处理
    ResultSetWrapper rsw = new ResultSetWrapper(stmt.getResultSet(), configuration);
    List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    while (rsw != null && resultMaps.size() > resultSetCount) {
      ResultMap resultMap = resultMaps.get(resultSetCount);
      // 2.处理结果
      handleResultSet(rsw, resultMap, multipleResults, null);
      // 3.获取下一个结果集
      rsw = getNextResultSet(stmt);
      resultSetCount++;
    }
    return collapseSingleResultList(multipleResults);
  }
```

- 1: 结果集包装器设置「列名」、「类名」、「jdbc 类型」、「对应的结果处理器」等
- 2: 处理结果中，会先创建「结果处理器 DefaultResultHandler」。
  - 实例化「结果对象」
  - 根据「结果对象」创建「元对象」
  - 处理列名，并处理「结果上下文」
- 3: 获取下一个结果集

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/DefaultResultSetHandler_handleResultSets.svg"/>



### ResultSetMetaData

他包含 `ResultSet` 对象的信息，并提供了关于结果集中列的「数量」、「类型」、「属性」以及「其他元数据」的访问方法。

```java
public interface ResultSetMetaData extends Wrapper {
  int getColumnCount() throws SQLException;
  boolean isAutoIncrement(int column) throws SQLException;
  boolean isCaseSensitive(int column) throws SQLException;
  boolean isSearchable(int column) throws SQLException;
  boolean isCurrency(int column) throws SQLException;
  //...
  String getColumnClassName(int column) throws SQLException;
}
```

### ResultSet

当一个数据库查询被执行后，它会返回一系列的数据行，这些数据行会被封装在一个 `ResultSet` 对象中。

- 游标：内部有一个游标指向当前的数据行。初始时，游标位于第一行之前。每当调用 `next()` 并且返回 `true` 时，游标就移动到了下一行。
- 列访问：内部可以通过多种方式获取列值
- 可滚动性：默认情况下，`ResultSet` 是向前的，只读的（`TYPE_FORWARD_ONLY`），这意味着只能从前往后读取数据。但是，也可以创建可滚动的 `ResultSet`（`TYPE_SCROLL_INSENSITIVE` 或 `TYPE_SCROLL_SENSITIVE`），这样就可以在结果集中前后移动。
- 元数据：除了数据本身，`ResultSet` 还提供了关于结果集结构的信息，如列的数量和类型。
- 生命周期：`ResultSet` 和其关联的 `Statement` 对象有相同的生命周期。

```java
public interface ResultSet extends Wrapper, AutoCloseable {
    next()：移动游标到下一行，如果还有数据则返回 true，否则返回 false。
    close()：关闭 ResultSet，释放它占用的资源。
    getString(int columnIndex)：根据列索引获取该列的字符串值。
    getInt(int columnIndex)：根据列索引获取该列的整数值。
    getBoolean(int columnIndex)：根据列索引获取该列的布尔值。
    getFloat(int columnIndex)：根据列索引获取该列的浮点数值。
    getDouble(int columnIndex)：根据列索引获取该列的双精度浮点数值。
    getBigDecimal(int columnIndex)：根据列索引获取该列的 BigDecimal 值。
    getDate(int columnIndex)：根据列索引获取该列的日期值。
    getTime(int columnIndex)：根据列索引获取该列的时间值。
    getTimestamp(int columnIndex)：根据列索引获取该列的时间戳值。
    getObject(int columnIndex)：根据列索引获取该列的Java对象值。
    getBlob(int columnIndex)：根据列索引获取该列的 Blob 值。
    getCharacterStream(int columnIndex)：根据列索引获取该列的字符流。
    getAsciiStream(int columnIndex)：根据列索引获取该列的ASCII流。
    getUnicodeStream(int columnIndex)：根据列索引获取该列的Unicode流。
    getBinaryStream(int columnIndex)：根据列索引获取该列的二进制流。
    getWarnings()：获取有关 ResultSet 的警告信息。
    clearWarnings()：清除所有警告信息。
}
```



## 增删改操作补充

> 代码分支：[11-insert-update-delete-dao](https://github.com/DoubleW2w/sbs-mybatis/tree/11-insert-update-delete-dao)

扩展 XMLMapperBuilder#configurationElement 方法，添加对 insert/update/delete 的解析操作, 所有的语句解析都会放置在 Configuration 类中。

```java
  private void configurationElement(Element element) {
    // 1.配置namespace
    String namespace = element.attributeValue("namespace");
    if (namespace.isEmpty()) {
      throw new RuntimeException("Mapper's namespace cannot be empty");
    }
    builderAssistant.setCurrentNamespace(namespace);

    // 2.配置select|insert|update|delete
    List<Element> list = new ArrayList<>();
    list.addAll(element.elements("select"));
    list.addAll(element.elements("insert"));
    list.addAll(element.elements("update"));
    list.addAll(element.elements("delete"));
    buildStatementFromContext(list);
  }
```



在 MapperMethod 中，添加「增删改」的指令执行。

```java
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result = null;
    switch (command.getType()) {
      case INSERT:
        {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.insert(command.getName(), param);
          break;
        }
      case DELETE:
        {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.delete(command.getName(), param);
          break;
        }
      case UPDATE:
        {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.update(command.getName(), param);
          break;
        }
      case SELECT:
        {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.selectOne(command.getName(), param);
          break;
        }
      default:
        throw new RuntimeException("Unknown execution method for: " + command.getName());
    }
    return result;
  }
```

本质上，都是对 update 方法的调用。

*SqlSession.java* 

```java
public interface SqlSession {
  //省略...
  int delete(String statement);

  int delete(String statement, Object parameter);

  int update(String statement) ;

  int update(String statement, Object parameter);

  int insert(String statement) ;

  int insert(String statement, Object parameter);
  //省略...
}
```

*DefaultSqlSession.java*

```java
public class DefaultSqlSession implements SqlSession {
  //省略...
  @Override
  public int delete(String statement) {
    return update(statement, null);
  }

  @Override
  public int delete(String statement, Object parameter) {
    return update(statement, parameter);
  }

  @Override
  public int update(String statement) {
    return update(statement, null);
  }

  @Override
  public int update(String statement, Object parameter) {
    MappedStatement ms = configuration.getMappedStatement(statement);
    try {
      return executor.update(ms, parameter);
    } catch (SQLException e) {
      throw new RuntimeException("Error updating database.  Cause: " + e);
    }
  }

  @Override
  public int insert(String statement) {
    return update(statement, null);
  }

  @Override
  public int insert(String statement, Object parameter) {
    return update(statement, parameter);
  }
  //省略...
}
```

同理，最终的 update 实现交给执行器 Executor 去实现，采用模板方法的模式。

```java
public abstract class BaseExecutor implements Executor {
  //...省略
  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    log.info("executing an update");
    if (closed) {
      throw new RuntimeException("Executor was closed.");
    }
    return doUpdate(ms, parameter);
  }

  /** 真正的具体实现交给子类 */
  protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;
  //...省略
}
```

- 获取配置类
- 创建 StatementHandler
- 准备语句 Statement
- 执行操作，返回结果

```java
public class SimpleExecutor extends BaseExecutor {  
  //...省略
  protected <E> List<E> doQuery(
    MappedStatement ms,
    Object parameter,
    RowBounds rowBounds,
    ResultHandler resultHandler,
    BoundSql boundSql) {
    Statement stmt = null;
    try {
      // 获取配置类
      Configuration configuration = ms.getConfiguration();
      // 新建一个 StatementHandler
      StatementHandler handler =
        configuration.newStatementHandler(
        this, ms, parameter, rowBounds, resultHandler, boundSql);
      Connection connection = transaction.getConnection();
      // 准备语句
      stmt = prepareStatement(handler);
      // 返回结果
      return handler.query(stmt, resultHandler);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }
  //...省略
}
```

## 初始化方式-XML 配置总结

```java
public void test_SqlSessionFactory() throws IOException {
  // 1. 从SqlSessionFactory中获取SqlSession
  SqlSessionFactory sqlSessionFactory =
    new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
  SqlSession sqlSession = sqlSessionFactory.openSession();

  // 2. 获取映射器对象
  IUserDao userDao = sqlSession.getMapper(IUserDao.class);

  // 3. 测试验证
  User user = userDao.queryUserInfoById(1L);
  logger.info("测试结果：{}", JSON.toJSONString(user));
}
```

从上面的例子我们可以知道基本的流程：

1. 创建 `SqlSessionFactoryBuilder` 加载 MyBatis 配置文件
1. 创建 `SqlSession`
1. 获取 `Mapper`
1. 执行 SQL 语句

### 创建 SqlSessionFactory 的基本过程

SqlSessionFactoryBuilder 根据传入的数据流生成 Configuration 对象，然后根据 Configuration 对象创建默认的 SqlSessionFactory 实例。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409170835897.png"/>

- `Resources` 读取配置文件进入程序，变成一个输入流。
- 调用 `SqlSessionFactoryBuilder` 对象的 `build(inputStream)` 方法；
- `SqlSessionFactoryBuilder` 调用 `XMLConfigBuilder` 对象的 `parse()` 方法；
- `XMLConfigBuilder` 对象返回 `Configuration` 对象；
- `SqlSessionFactoryBuilder` 根据 `Configuration` 对象创建一个 `DefaultSessionFactory` 对象；
- `SqlSessionFactoryBuilder` 返回 `DefaultSessionFactory` 对象给 Client。

```java
public class SqlSessionFactoryBuilder {

  public SqlSessionFactory build(Reader reader) {
    XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
    return build(xmlConfigBuilder.parse());
  }

  public SqlSessionFactory build(Configuration config) {
    return new DefaultSqlSessionFactory(config);
  }
}
```

- Configuration ：该对象是 mybatis-config.xml 文件中所有 mybatis 配置信息

### 建造者模式和工厂模式及类说明

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409170845610.png"/>

- `Configuration` ：该对象是 *mybatis-config.xml* 文件中所有 mybatis 配置信息
- `SqlSessionFactoryBuilder` 是通过建造者模式来创建 `SqlSessionFactory` 
- `SqlSessionFactory` 是通过工厂模式来创建 `SqlSession`

建造者模式和工厂模式都是「创建型设计模式」，建造者模式适用于那些构建过程复杂或者需要多个步骤来完成的对象，强调一个「分步创建」。而工厂模式是通过定义一个创建的接口来创建出子类，强调「如何创建一个产品对象，并不是强调过程和复杂性」

- **建造者模式** 通常用于构建需要多个步骤的复杂对象
- **工厂模式** 则适用于创建不同类型的对象，通常不涉及对象的复杂构建过程



### 创建 Configuration 对象的过程

*XMLConfigBuilder#parse()*

```java
  public Configuration parse() {
    try {
      // 环境
      environmentsElement(root.element("environments"));
      // 解析映射器
      mapperElement(root.element("mappers"));
    } catch (Exception e) {
      throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
    return configuration;
  }
```

本质上就是解析 Configuration 节点下所有子节点，比如 environments, mappers 等。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="UNPOOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url"
                          value="jdbc:mysql://localhost:13306/sbs_mybatis?useUnicode=true"/>
                <property name="username" value="root"/>
                <property name="password" value="123456"/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="mapper/UserMapper.xml"/>
    </mappers>
</configuration>
```

在简单版的解析过程中，通过「获取节点信息」，「获取节点属性值」来创建各种对象并放置在 Configuration 对象中

```java
private void environmentsElement(Element environments) throws Exception {
  // 默认环境
  String environment = environments.attributeValue("default");
  List<Element> environmentList = environments.elements("environment");
  for (Element e : environmentList) {
    String id = e.attributeValue("id");
    if (environment.equals(id)) {
      // todo：事务管理器，缺少对应的测试
      TransactionFactory txFactory =
        (TransactionFactory)
        typeAliasRegistry
        .resolveAlias(e.element("transactionManager").attributeValue("type"))
        .newInstance();
      // 数据源
      Element dataSourceElement = e.element("dataSource");
      DataSourceFactory dataSourceFactory =
        (DataSourceFactory)
        typeAliasRegistry
        .resolveAlias(dataSourceElement.attributeValue("type"))
        .newInstance();
      // 注入数据源配置属性
      List<Element> propertyList = dataSourceElement.elements("property");
      Properties props = new Properties();
      for (Element property : propertyList) {
        props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
      }
      dataSourceFactory.setProperties(props);
      DataSource dataSource = dataSourceFactory.getDataSource();

      // 构建环境
      Environment.Builder environmentBuilder =
        new Environment.Builder(id).transactionFactory(txFactory).dataSource(dataSource);
      configuration.setEnvironment(environmentBuilder.build());
    }
  }
}
```

在解析 mappers 节点时，会利用 XMLMapperBuilder。

```java
  private void mapperElement(Element mappers) throws Exception {
    List<Element> mapperList = mappers.elements("mapper");
    for (Element e : mapperList) {
      String resource = e.attributeValue("resource");
      InputStream inputStream = Resources.getResourceAsStream(resource);

      // 在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
      XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
      mapperParser.parse();
    }
  }
```

### Mapper 映射文件配置

解析每一个类型的标签

```java
  // 配置mapper元素
  // <mapper namespace="org.mybatis.example.BlogMapper">
  //   <select id="selectBlog" parameterType="int" resultType="Blog">
  //    select * from Blog where id = #{id}
  //   </select>
  // </mapper>
  private void configurationElement(Element element) {
    // 1.配置namespace
    String namespace = element.attributeValue("namespace");
    if (namespace.isEmpty()) {
      throw new RuntimeException("Mapper's namespace cannot be empty");
    }
    builderAssistant.setCurrentNamespace(namespace);

    // 2.配置select|insert|update|delete
    List<Element> list = new ArrayList<>();
    list.addAll(element.elements("select"));
    list.addAll(element.elements("insert"));
    list.addAll(element.elements("update"));
    list.addAll(element.elements("delete"));
    buildStatementFromContext(list);
  }
```

解析每个标签下的属性信息

```java
  // 解析语句(select|insert|update|delete)
  // <select
  //  id="selectPerson"
  //  parameterType="int"
  //  parameterMap="deprecated"
  //  resultType="hashmap"
  //  resultMap="personResultMap"
  //  flushCache="false"
  //  useCache="true"
  //  timeout="10000"
  //  fetchSize="256"
  //  statementType="PREPARED"
  //  resultSetType="FORWARD_ONLY">
  //  SELECT * FROM PERSON WHERE ID = #{id}
  // </select>
  public void parseStatementNode() {
    String id = element.attributeValue("id");
    // 参数类型
    String parameterType = element.attributeValue("parameterType");
    Class<?> parameterTypeClass = resolveAlias(parameterType);
    // 外部应用 resultMap
    String resultMap = element.attributeValue("resultMap");
    // 结果类型
    String resultType = element.attributeValue("resultType");
    Class<?> resultTypeClass = resolveAlias(resultType);
    // 获取命令类型(select|insert|update|delete)
    String nodeName = element.getName();
    SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

    // 获取默认语言驱动器
    LanguageDriver langDriver = configuration.getLanguageRegistry().getDefaultDriver();
    // 构建SQL源码
    SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);
    // 调用助手类【本节新添加，便于统一处理参数的包装】
    mapperBuilderAssistant.addMappedStatement(
        id, sqlSource, sqlCommandType, parameterTypeClass, resultMap, resultTypeClass, langDriver);
  }
```



## 通过注解配置

> 代码分支：[12-annotation-config-sql](https://github.com/DoubleW2w/sbs-mybatis/tree/12-annotation-config-sql)

> 如果你想写一个 `@Select("select * from user")`，你需要解决什么问题？

1. 定义注解类

2. 在注册映射器的地方添加对映射器的解析工作。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409180500020.png"/>

| **注解方式**          | **xml 方式**           |
| --------------------- | --------------------- |
| @Select               | `<select>`            |
| @Select("SQL 语句")    | SQL 语句               |
| 方法名                | 标签唯一标识          |
| 返回类型              | 返回类型              |
| 入参类型              | 入参类型              |
| 接口全路径名          | 命名空间              |
| 接口全路径名称.方法名 | 命名空间.标签唯一标识 |



将在 xml 的解析逻辑挪出来放到一个新的类中 `MapperAnnotationBuilder` 注解解析方式。

```java
public class MapperRegistry {
  //...省略
  public <T> void addMapper(Class<?> type) {
    /* Mapper 必须是接口才会注册 */
    if (type.isInterface()) {
      if (hasMapper(type)) {
        // 如果重复添加了，报错
        throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try{
        // 注册映射器代理工厂
        knownMappers.put(type, new MapperProxyFactory<>(type));
        // 注解解析，如果因为解析失败的情况，就会删除加载状态。
        MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }
  // 省略
}
```

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/Mybatis%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90-11-%E6%B3%A8%E8%A7%A3%E9%85%8D%E7%BD%AE%E8%A7%A3%E6%9E%90SQL.drawio.svg"/>



添加 mapper 接口的扫描，和 xml 文件的配置

```java
public class XMLConfigBuilder extends BaseBuilder {  
  //省略...
  private void mapperElement(Element mappers) throws Exception {
    List<Element> mapperList = mappers.elements("mapper");
    for (Element e : mapperList) {
      // xml资源路径
      String resource = e.attributeValue("resource");
      // mapper接口
      String mapperClass = e.attributeValue("class");
      // XML 解析
      if (resource != null && mapperClass == null) {
        InputStream inputStream = Resources.getResourceAsStream(resource);
        // 在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
        mapperParser.parse();
      }
      // Annotation 注解解析
      else if (resource == null && mapperClass != null) {
        Class<?> mapperInterface = Resources.classForName(mapperClass);
        configuration.addMapper(mapperInterface);
      }
    }
    // 省略..
  }
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
		<!--省略...-->
    <mappers>
        <!--注解配置-->
        <mapper class="com.doublew2w.sbs.mybatis.test.dao.IUserDaoAnno"/>
    </mappers>
</configuration>
```

## ResultMap 映射参数配置

> 代码分支：[13-result-map-parse-use](https://github.com/DoubleW2w/sbs-mybatis/tree/13-result-map-parse-use)

### S

ResultMap 的作用是什么呢？

1. 字段与属性的映射：数据库字段与 Java 对象属性的对应关系, 比如 把数据库表中的下划线的字段名称，映射成 Java 代码中的驼峰字段。当然你可以用在查询语句上使用 `as` 语句
2. 支持复杂关系映射：一对多，和一对一的关系

对应的就是 `<resultMap>` 标签，那里面有什么东西呢？

```xml
<!-- 
        resultMap –结果映射，用来描述如何从数据库结果集映射到你想要的对象。

        1.type 对应类型，可以是javabean, 也可以是其它
        2.id 必须唯一， 用于标示这个resultMap的唯一性，在使用resultMap的时候，就是通过id指定
     -->
<resultMap type="" id="">
  <!-- id, 唯一性，注意啦，这个id用于标示这个javabean对象的唯一性， 不一定会是数据库的主键（不要把它理解为数据库对应表的主键） 
            property属性对应javabean的属性名，column对应数据库表的列名
            （这样，当javabean的属性与数据库对应表的列名不一致的时候，就能通过指定这个保持正常映射了）
  -->
  <id property="" column=""/>
  <!-- result与id相比， 对应普通属性 -->    
  <result property="" column=""/>
  <!-- 
            聚集元素用来处理“一对多”的关系。需要指定映射的Java实体类的属性，属性的javaType（一般为ArrayList）；列表中对象的类型ofType（Java实体类）；对应的数据库表的列名称；

            collection，对应javabean中容器类型, 是实现一对多的关键 
            property 为javabean中容器对应字段名
            column 为体现在数据库中列名
            ofType 就是指定javabean中容器指定的类型

            不同情况需要告诉 MyBatis 如何加载一个聚集。MyBatis 可以用两种方式加载：
                1. select: 执行一个其它映射的SQL 语句返回一个Java实体类型。较灵活；
                2. resultMap: 使用一个嵌套的结果映射来处理通过join查询结果集，映射成Java实体类型。
        -->
  <collection property="" column="" ofType=""></collection>
  <!-- 
            联合元素用来处理“一对一”的关系。需要指定映射的Java实体类的属性，属性的javaType（通常MyBatis 自己会识别）。对应的数据库表的列名称。如果想覆写的话返回结果的值，需要指定typeHandler。

            association 为关联关系，是实现N对一的关键。
            property 为javabean中容器对应字段名
            column 为体现在数据库中列名
            javaType 指定关联的类型

            不同情况需要告诉MyBatis 如何加载一个联合。MyBatis可以用两种方式加载：
                1. select: 执行一个其它映射的SQL 语句返回一个Java实体类型。较灵活；
                2. resultMap: 使用一个嵌套的结果映射来处理，通过join查询结果集，映射成Java实体类型。
         -->
  <association property="" column="" javaType=""></association>
</resultMap>
```

### T

映射参数的解析过程，主要以循环解析 `resultMap` 的标签集合，摘取核心的 property、column 字段构建出 `ResultMapping` 结果映射类，将解析出来的 `ResultMapping` 集合放进 `Configuration` 配置项的 `Map<String, ResultMap> resultMaps` 的结果映射中。

最后在利用 `SqlSession` 进行结果封装时，从 `Configuration` 中取出。

### A

找到解析语句的地方

```java
public class XMLMapperBuilder extends BaseBuilder {
  // 省略...
  private void configurationElement(Element element) {
    // 1.配置namespace
    String namespace = element.attributeValue("namespace");
    if (namespace.isEmpty()) {
      throw new RuntimeException("Mapper's namespace cannot be empty");
    }
    builderAssistant.setCurrentNamespace(namespace);

    // 2. 解析resultMap(添加部分)
    resultMapElements(element.elements("resultMap"));

    // 2.配置select|insert|update|delete
    List<Element> list = new ArrayList<>();
    list.addAll(element.elements("select"));
    list.addAll(element.elements("insert"));
    list.addAll(element.elements("update"));
    list.addAll(element.elements("delete"));
    buildStatementFromContext(list);
  }
  // 省略...
}
```

我们知道一个 MapperXML 文件中可能存在多个 `<resultMap></resultMap>` 标签，所以循环去解析。

```java
  private void resultMapElements(List<Element> resultMaps) {
    for (Element element : resultMaps) {
      try {
        resultMapElement(element, Collections.emptyList());
      } catch (Exception ignore) {
      }
    }
  }
```

在解析上：

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409192031798.png"/>



在使用上，进行结果封装时

```java
public class DefaultResultSetHandler implements ResultSetHandler {
  // 省略...
  private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException {
    // 根据返回类型，实例化对象
    Object resultObject = createResultObject(rsw, resultMap, null);
    if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
      final MetaObject metaObject = configuration.newMetaObject(resultObject);
      // 自动映射：把每列的值都赋到对应的字段上
      applyAutomaticMappings(rsw, resultMap, metaObject, null);
      // Map映射：根据映射类型赋值到字段
      applyPropertyMappings(rsw, resultMap, metaObject, null);
    }
    return resultObject;
  }
  // 省略...

  private boolean applyPropertyMappings(
    ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix)
    throws SQLException {
    final List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
    boolean foundValues = false;
    final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
    for (ResultMapping propertyMapping : propertyMappings) {
      final String column = propertyMapping.getColumn();
      if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
        // 获取值
        final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
        Object value = typeHandler.getResult(rsw.getResultSet(), column);
        // 设置值
        final String property = propertyMapping.getProperty();
        if (value != NO_VALUE && property != null && value != null) {
          // 通过反射工具类设置属性值
          metaObject.setValue(property, value);
          foundValues = true;
        }
      }
    }
    return foundValues;
  }
  // 省略...
}
```

- 根据 ResultMap 获取到每一列的属性 `List<ResultMapping> propertyMappings`
- 根据列名 `column`、类型处理器 `typeHandler` 获取到对应的数据库查询值
- 通过反射进行调用 setter 进行设置属性值 `metaObject.setValue(property, value);`

### R

在整个解析的过程中，一个 ResultMap 对应多个 ResultMapping 的关系，把每一条映射都存处理成 ResultMapping 信息，都存放到配置项中。

在整个使用上，普通的对象默认按照对象字段即可封装，而存在「下划线」的情况，则要找到映射关系进行匹配处理，最终返回统一的封装结果处理。



## selectKey 自增索引配置

> 代码分支：[14-select-key-parse-use](https://github.com/DoubleW2w/sbs-mybatis/tree/14-select-key-parse-use)

### S

当我们插入一条数据后，想要获取到该记录的自增主键值，在某些情况需要用到这个自增主键值来做业务上的处理。

如果没有这个需求的话，当我们执行插入操作后，还要进行一次额外的查询操作来获取自增主键，这样会使代码变的冗长。

### T

本节要实现的是在执行插入 SQL 后要返回此条插入语句后的自增索引。

```xml
<selectKey>
  keyProperty="id"
  resultType="int"
  order="BEFORE"
  <!-- 与前面相同，MyBatis 支持 STATEMENT，PREPARED 和 CALLABLE 语句的映射类型，分别代表 PreparedStatement 和 CallableStatement 类型。 -->
  statementType="PREPARED">
</selectKey>
```

- keyProperty: selectKey 语句结果应该被设置的目标属性。如果希望得到多个生成的列，也可以是逗号分隔的属性名称列表。
- resultType: 结果的类型。MyBatis 通常可以推算出来，但是为了更加确定写上也不会有什么问题。
- order: 可以被设置为 BEFORE 或 AFTER。
  - 如果设置为 BEFORE，那么它会首先选择主键，设置 keyProperty 然后执行插入语句。
  - 如果设置为 AFTER，那么先执行插入语句，然后是 selectKey 元素
- statementType: 语句的映射类型，在 Mybatis 中支持 STATEMENT、PREPARED、CALLABLE。

### A

#### 解析 SelectKey

```java
public void parseStatementNode() {
  //省略...
  // 获取默认语言驱动器
    LanguageDriver langDriver = configuration.getLanguageRegistry().getDefaultDriver();
  
  // Parse selectKey after includes and remove them.
  processSelectKeyNodes(id, parameterTypeClass, langDriver);

  // 属性标记【仅对 insert 有用】, MyBatis 会通过 getGeneratedKeys 或者通过 insert 语句的 selectKey 子元素设置它的值
  String keyProperty = element.attributeValue("keyProperty");

  KeyGenerator keyGenerator;
  String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
  keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
  // 获取主键生成器
  if (configuration.hasKeyGenerator(keyStatementId)) {
    keyGenerator = configuration.getKeyGenerator(keyStatementId);
  } else {
    keyGenerator =
      configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType)
      ? new Jdbc3KeyGenerator()
      : new NoKeyGenerator();
  }
  //省略...
}
```

真正解析 SelectKey 内容的方法在如下 parseSelectKeyNode

```java
private void parseSelectKeyNode(
  String id, Element nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver) {
  String resultType = nodeToHandle.attributeValue("resultType");
  Class<?> resultTypeClass = resolveClass(resultType);
  String keyProperty = nodeToHandle.attributeValue("keyProperty");
  String keyColumn = nodeToHandle.attributeValue("keyColumn");
  boolean executeBefore = "BEFORE".equals(nodeToHandle.attributeValue("order", "AFTER"));

  // defaults
  String resultMap = null;
  KeyGenerator keyGenerator = new NoKeyGenerator();

  // 解析成SqlSource，DynamicSqlSource/RawSqlSource
  SqlSource sqlSource =
    langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
  SqlCommandType sqlCommandType = SqlCommandType.SELECT;

  // 调用助手类
  builderAssistant.addMappedStatement(
    id,
    sqlSource,
    sqlCommandType,
    parameterTypeClass,
    resultMap,
    resultTypeClass,
    keyGenerator,
    keyProperty,
    langDriver);

  id = builderAssistant.applyCurrentNamespace(id, false);

  // 存放键值生成器配置
  MappedStatement keyStatement = configuration.getMappedStatement(id);
  configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
}
```

- 解析 `selectKey` 标签的内容
- 默认 KeyGenerator 为 NoKeyGenerator 类型
- 通过助手类注册映射语句，不过与之前的不同的是携带了「keyGenerator」和「keyProperty」信息。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/Mybatis%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90-13-%E8%A7%A3%E6%9E%90%E5%B9%B6%E4%BD%BF%E7%94%A8selectKey%E6%A0%87%E7%AD%BE.drawio-1.svg"/>



#### 使用 SelectKey

```java
public class Configuration {  
  //..省略...
  public StatementHandler newStatementHandler(
    Executor executor,
    MappedStatement mappedStatement,
    Object parameterObject,
    RowBounds rowBounds,
    ResultHandler resultHandler,
    BoundSql boundSql) {
    return new PreparedStatementHandler(
      executor, mappedStatement, parameterObject, resultHandler, boundSql, rowBounds);
  }
  //..省略...
}

public class PreparedStatementHandler extends BaseStatementHandler {
  //..省略...
  public int update(Statement statement) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    int rows = ps.getUpdateCount();
    Object parameterObject = boundSql.getParameterObject();
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
    return rows;
  }
  //省略
}
```

- 执行 update 语句
- 获取到 update 语句绑定的参数对象 `parameterObject` 和 主键生成器 `keyGenerator`

```java
private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
    try {
      if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
        String[] keyProperties = keyStatement.getKeyProperties();
        final Configuration configuration = ms.getConfiguration();
        final MetaObject metaParam = configuration.newMetaObject(parameter);
        // Do not close keyExecutor.
        // The transaction will be closed by parent executor.
        Executor keyExecutor = configuration.newExecutor(executor.getTransaction());
        List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        if (values.size() == 0) {
          throw new RuntimeException("SelectKey returned no data.");
        } else if (values.size() > 1) {
          throw new RuntimeException("SelectKey returned more than one value.");
        } else {
          MetaObject metaResult = configuration.newMetaObject(values.get(0));
          if (keyProperties.length == 1) {
            if (metaResult.hasGetter(keyProperties[0])) {
              setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
            } else {
              // no getter for the property - maybe just a single value object
              // so try that
              setValue(metaParam, keyProperties[0], values.get(0));
            }
          } else {
            handleMultipleProperties(keyProperties, metaParam, metaResult);
          }
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(
          "Error selecting key or setting result to parameter object. Cause: " + e, e);
    }
```

- `SELECT LAST_INSERT_ID()` 语句执行，并将结果设置到结果的属性列上。

### R

1. 由于是在同一个操作下，处理两条 SQL，分别是插入和返回索引值。那么这两条 SQL 其实要在同一个链接下才能正确的获取到结果，也就是保证了一个事务的特性。

*JdbcTransaction.java*

```java
public Connection getConnection() throws SQLException {
    // 多个SQL在同一个JDBC连接下，才能完成事务特性
    if (null != connection) {
        return connection;
    }
    connection = dataSource.getConnection();
    connection.setTransactionIsolation(level.getLevel());
    connection.setAutoCommit(autoCommit);
    return connection;
}
```

2. 在原有的 Mapper XML 对各类标签语句的解析中，对 insert 操作进行扩展，添加 selectKey 标签的解析、执行、结果封装。把最终的插入索引结果返回到入参对象的对应属性字段上。

```java
2024-09-21 21:24:01 INFO  c.d.s.m.builder.SqlSourceBuilder - 构建参数映射 property：activityId propertyType：class java.lang.Long
2024-09-21 21:24:01 INFO  c.d.s.m.builder.SqlSourceBuilder - 构建参数映射 property：activityId propertyType：class java.lang.Long
2024-09-21 21:24:01 INFO  c.d.s.m.builder.SqlSourceBuilder - 构建参数映射 property：activityName propertyType：class java.lang.String
2024-09-21 21:24:01 INFO  c.d.s.m.builder.SqlSourceBuilder - 构建参数映射 property：activityDesc propertyType：class java.lang.String
2024-09-21 21:24:01 INFO  c.d.sbs.mybatis.binding.MapperProxy - into invoke(), mapperInterface: com.doublew2w.sbs.mybatis.test.dao.IActivityDao, cachedMapperMethod: insert
2024-09-21 21:24:01 INFO  c.d.s.mybatis.executor.BaseExecutor - executing an update
2024-09-21 21:24:02 INFO  c.d.s.m.s.d.DefaultParameterHandler - 根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value：10007
2024-09-21 21:24:02 INFO  c.d.s.m.s.d.DefaultParameterHandler - 根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value："测试活动"
2024-09-21 21:24:02 INFO  c.d.s.m.s.d.DefaultParameterHandler - 根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value："测试数据插入"
2024-09-21 21:24:02 INFO  c.d.s.mybatis.executor.BaseExecutor - executing an query
2024-09-21 21:24:02 INFO  c.d.s.m.e.s.PreparedStatementHandler - 执行查询 query 
2024-09-21 21:24:02 INFO  c.d.s.m.e.r.DefaultResultSetHandler - 正在处理结果集...
2024-09-21 21:24:02 INFO  c.d.s.m.test.IActivityDaoApiTest - 测试结果：count：1 idx：20
```

## 解析含标签的动态 SQL 语句

> 代码分支：[15-dynamic-label-sql-parse](https://github.com/DoubleW2w/sbs-mybatis/tree/15-dynamic-label-sql-parse)

### S

我们知道在 Mybatis 中存在很多动态语句标签，`if`, `foreach`, `choose`, `where` 等，不仅如此还要根据参数对象的字段「判断情况」才能决定是否要拼接 SQL 语句。

### T

扩充对动态 SQL 的处理，最终让 ORM 框架可以配置拼装 SQL 语句。

### A

在前面其实完成的是静态 SQL 语句 `RawSqlSource` 的解析，由此创建出对应的 SqlSource 对象。因此需要实现一个 `DynamicSqlSource` 来完成动态 SQL 的解析。

结合上节 `<selectKey>` 标签的思路，也是完成识别标签，然后解析标签属性，最后完成 sql 的运行，只不过 `<selectKey>` 标签是存在于 `<insert>` 标签内部，所以在解析 `<insert>` 标签以后，进一步解析 `<selectKey>` 标签。而本节的动态 Sql 标签，则会存在于 `<insert>`, `<update>`, `<delete>`, `<select>` 中，所以要独立出来。

- 判断动态标签类型
- 解析文本和标签内容
- 由 `DynamicSqlSource` 去生成 `SqlSource` 语句



<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409230216957.png"/>

<p style="text-align:center"> 图片来自：小傅哥 </p>



创建 SqlSource 的入口在于 LanguageDriver 类中

```java
public interface LanguageDriver {
  /**
   * 创建SQL源码
   *
   * <p>mapper xml方式
   */
  SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

  /** 创建SQL源码(annotation 注解方式) */
  SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

  /** 创建参数处理器 */
  ParameterHandler createParameterHandler(
      MappedStatement ms, Object parameterObject, BoundSql boundSql);
}
```

本质上还是走的 XMLScriptBuilder#parseScriptNode

```java
public class XMLScriptBuilder extends BaseBuilder {
  //省略...
  public SqlSource parseScriptNode() {
    List<SqlNode> contents = parseDynamicTags(element);
    MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
    SqlSource sqlSource = null;
    // 动态还是静态
    if (isDynamic) {
      sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
    } else {
      sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
    }
    return sqlSource;
  }
  //省略...
}
```

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409230218058.png"/>

实现对应节点的处理逻辑

```java
public class DynamicSqlSource implements SqlSource {

  private Configuration configuration;
  private SqlNode rootSqlNode;

  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // 生成一个 DynamicContext 动态上下文
    DynamicContext context = new DynamicContext(configuration, parameterObject);
    // SqlNode.apply 将 ${} 参数替换掉，不替换 #{} 这种参数
    rootSqlNode.apply(context);

    // 调用 SqlSourceBuilder
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();

    // SqlSourceBuilder.parse 这里返回的是 StaticSqlSource，解析过程就把那些参数都替换成?了，也就是最基本的JDBC的SQL语句。
    SqlSource sqlSource =
        sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());

    // SqlSource.getBoundSql，非递归调用，而是调用 StaticSqlSource 实现类
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
      boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
    }
    return boundSql;
  }
}

```

### R

通过 OGNL(Object-Graph Navigation Language)来完成「动态属性访问」、「方法调用」、「条件判断和逻辑运算」等。

## 插件功能实现

> 代码分支：[16-plugin-impl](https://github.com/DoubleW2w/sbs-mybatis/tree/16-plugin-impl)

### S

### T

通过代理来实现对类的扩展，并在代理中调用自定义的逻辑行为。

按照 Mybatis 框架提供的拦截器接口，实现自己的功能实现类，并把这个类配置到 Mybatis 的 XML 配置中

```xml
<plugins>
	<plugin interceptor="com.doublew2w.mybatis.test.plugin.MyPlugin">
    <property name="user" value="zzz"/>
  </plugin>
</plugins>
```

### A

在 Mybatis Plugin 插件的具体实现落地中，由框架提供拦截器接口，交由使用方实现，并通过配置的方式把实现添加到 Mybatis 框架中。

- `ParameterHandler`
- `ResultSetHandler`
- `StatementHandler`
- `Executor`

都可以为它们创建对面的插件。当调用任意类对应的接口方法时，都能调用到用户实现拦截器接口的插件内容，也就是实现了自定义扩展的效果。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409231953241.png"/>



通过实现 `InvocationHandler` 接口，你可以定义如何处理方法调用。MyBatis 中的 `Plugin` 类通常实现 `InvocationHandler`，用于创建动态代理以拦截方法调用。当代理对象上的方法被调用时，`invoke` 方法就会被执行，你可以在 `invoke` 方法中添加自定义逻辑，完成拦截行为。



#### 解析 plugins

```java
  private void pluginElement(Element parent) throws Exception {
    if (parent != null) {
      List<Element> elements = parent.elements();
      for (Element element : elements) {
        String interceptor = element.attributeValue("interceptor");
        // 参数配置
        Properties properties = new Properties();
        List<Element> propertyElementList = element.elements("property");
        for (Element property : propertyElementList) {
          properties.setProperty(property.attributeValue("name"), property.attributeValue("value"));
        }
        // 获取插件实现类并实例化：
        Interceptor interceptorInstance =
            (Interceptor) resolveClass(interceptor).getDeclaredConstructor().newInstance();
        interceptorInstance.setProperties(properties);
        configuration.addInterceptor(interceptorInstance);
      }
    }
  }
```

- `interceptor` 插件类的全限定名名称
- `Properties properties` 插件的属性
- 实例化拦截器，并放进拦截器链进行管理

#### 生成代理对象并使用自定义逻辑

```java
public interface Interceptor {
  Object intercept(Invocation invocation) throws Throwable;
  default Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  default void setProperties(Properties properties) {
    // NOP
  }
}
```

```java
public class Plugin implements InvocationHandler {
  //省略..
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
   //省略..
}
```

- 获取到自定义插件上的注解信息，并放进 set 进行管理
- 获取到目标对象实现的所有接口
- 并根据目标对象生成对应的代理对象

```java
@Intercepts({
  @Signature(
      type = StatementHandler.class,
      method = "prepare",
      args = {Connection.class})
})
public class TestPlugin implements Interceptor {
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 获取StatementHandler
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    // 获取SQL信息
    BoundSql boundSql = statementHandler.getBoundSql();
    String sql = boundSql.getSql();
    // 输出SQL
    System.out.println("拦截SQL：" + sql);
    // 放行
    return invocation.proceed();
  }

  @Override
  public void setProperties(Properties properties) {
    System.out.println("参数输出：" + properties.getProperty("test00"));
  }
}
```

在插件中完成自定义逻辑



<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409232153522.png"/>

<p style="text-align:center"> 图片来自：小傅哥 </p>

### R

通过 Proxy 类来生成对目标对象的代理对象。代理对象的拦截是通过实现 `InvocationHandler` 接口来完成的

```java
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
```

每一个代理对象（拦截器）都会有自己的自定义逻辑

```java
	  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 获取StatementHandler
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    // 获取SQL信息
    BoundSql boundSql = statementHandler.getBoundSql();
    String sql = boundSql.getSql();	
    // 输出SQL
    System.out.println("拦截SQL：" + sql);
    // 放行
    return invocation.proceed();
  }
```

也就是说，根据依赖倒置原则，面向抽象编程的具体实现。



## 一级缓存

> 代码分支：[17-first-level-cache](https://github.com/DoubleW2w/sbs-mybatis/tree/17-first-level-cache)



### S

在 MyBatis 中，缓存指的是用于存储查询结果以提高性能的机制。通过缓存，MyBatis 可以减少对数据库的访问次数，从而加快数据读取速度。有「一级缓存」和「二级缓存」

一级缓存是与 `SqlSession` 绑定的缓存，每个 `SqlSession` 拥有自己的缓存。

**作用**：在同一个 `SqlSession` 中，执行相同的查询时，MyBatis 会先检查缓存，如果缓存中存在结果，则直接返回，不再查询数据库。

**生命周期**：一级缓存的生命周期与 `SqlSession` 相同，当 `SqlSession` 被关闭或清除时，缓存也随之失效。

### T

### A

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409240420439.png"/>

<p style="text-align:center"> 图片来自：小傅哥 </p>



默认情况下使用一级缓存，也就是 Session 级别的。

SqlSession 的工作主要交给 Executor 执行器完成，负责数据库的各种操作。当创建一个 SqlSession 对象时，Mybatis 会为这个 SqlSession 创建一个新的 Executor 执行器，而缓存的工具包也是在执行器的创建时构建出来的。

```xml
<settings>
    <!--缓存级别：SESSION/STATEMENT-->
    <setting name="localCacheScope" value="SESSION"/>
</settings>
```

#### 解析

```java
  private void settingsElement(Element settings) {
    if (settings == null) return;
    List<Element> elements = settings.elements();
    Properties props = new Properties();
    for (Element element : elements) {
      props.setProperty(element.attributeValue("name"), element.attributeValue("value"));
    }
    configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope")));
  }
```

#### 使用

```java
  // 查询，含缓存
  <E> List<E> query(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      CacheKey key,
      BoundSql boundSql)
      throws SQLException;

  // 查询
  <E> List<E> query(
      MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
      throws SQLException;
```

```java
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    // 1. 获取绑定SQL
    BoundSql boundSql = ms.getBoundSql(parameter);
    // 2. 创建缓存Key
    CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
    return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
}
```

缓存 key 的创建依赖于 update 方法，MyBatis 对于其 Key 的生成采取规则为：[mappedStatementId + offset + limit + SQL + queryParams + environment] 生成一个哈希码作为 Key 使用。

```java
public class CacheKey implements Cloneable, Serializable {
  // 省略..
  private static final int DEFAULT_MULTIPLIER = 37;
  private static final int DEFAULT_HASHCODE = 17;

  private final int multiplier;
  private int hashcode;
  private long checksum;
  private int count;
  // 8/21/2017 - Sonarlint flags this as needing to be marked transient. While true if content is
  // not serializable, this
  // is not always true and thus should not be marked transient.
  private List<Object> updateList;

  public CacheKey() {
    this.hashcode = DEFAULT_HASHCODE;
    this.multiplier = DEFAULT_MULTIPLIER;
    this.count = 0;
    this.updateList = new ArrayList<>();
  }

  public void update(Object object) {
    int baseHashCode = object == null ? 1 : ArrayUtil.hashCode(object);

    count++;
    checksum += baseHashCode;
    baseHashCode *= count;
    hashcode = multiplier * hashcode + baseHashCode;

    updateList.add(object);
  }
  // 省略..
}
```



测试类

```java
  public void test_branch17_first_cache() {
    // 2. 获取映射器对象
    IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
    // 3. 测试验证
    Activity req = new Activity();
    req.setActivityId(100001L);
    log.info("测试结果：{}", JSON.toJSONString(dao.queryActivityByIdForDynamicSql(req)));
    sqlSession.close();

    log.info("测试结果：{}", JSON.toJSONString(dao.queryActivityByIdForDynamicSql(req)));
  }
```



<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409240426269.png"/>

### R

Mybatis 中的一级缓存时基于 PerpetualCache 的 HashMap 本地缓存，其存储作用域为 Session，当 Session flush 或 close 之后，该 Session 中的所有 Cache 就将清空，默认打开一级缓存。

对于缓存数据更新机制，当某一个作用域(一级缓存 Session/二级缓存 Namespaces)的进行了 C/U/D 操作后，默认该作用域下所有 select 中的缓存将被 clear 掉并重新更新，如果开启了二级缓存，则只根据配置判断是否刷新。

## 二级缓存的实现

> 代码分支：[18-second-level-cache](https://github.com/DoubleW2w/sbs-mybatis/tree/18-second-level-cache)

### S

一级缓存的功能实现，它对数据的缓存操作主要作用于一次 Session 会话的生命周期内，从查询开始保存数据，到执行有可能变更数据库的操作为止清空一级缓存数据。

Mybatis 框架中的二级缓存，以一个 Mapper 为生命周期，在这个 Mapper 内的同一个操作，无论发起几次会话都可以使用缓存来处理数据。

### T

二级缓存在一级缓存会话层上，添加的额外缓存操作，当会话发生 close、commit 操作时则把数据刷新到二级缓存中进行保存，直到执行器发生 update 操作时清空缓存。

### A

缓存类 `FifoCache `

```java
public class FifoCache implements Cache {

  private final Cache delegate;
  private final Deque<Object> keyList;
  private int size;

  public FifoCache(Cache delegate) {
    this.delegate = delegate;
    this.keyList = new LinkedList<>();
    this.size = 1024;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    cycleKeyList(key);
    delegate.putObject(key, value);
  }

  @Override
  public Object getObject(Object key) {
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyList.clear();
  }

  private void cycleKeyList(Object key) {
    keyList.addLast(key);
    if (keyList.size() > size) {
      Object oldestKey = keyList.removeFirst();
      delegate.removeObject(oldestKey);
    }
  }
}
```

- 通过 cycleKeyList 方法的作用是在增加记录时判断记录是否超过 size 值，超过的时候移除头元素

`TransactionalCache` 负责存储会话期间内的缓存数据，当会话结束后则把缓存 **刷新到二级缓存** 中。如果是回滚操作则清空缓存。

```java
public class CachingExecutor implements Executor {

  private Executor delegate;
  private TransactionalCacheManager tcm = new TransactionalCacheManager();

	//省略...

  @Override
  public <E> List<E> query(
      MappedStatement ms,
      Object parameter,
      RowBounds rowBounds,
      ResultHandler resultHandler,
      CacheKey key,
      BoundSql boundSql)
      throws SQLException {
    Cache cache = ms.getCache();
    if (cache != null) {
      flushCacheIfRequired(ms);
      if (ms.isUseCache() && resultHandler == null) {
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) tcm.getObject(cache, key);
        if (list == null) {
          list = delegate.<E>query(ms, parameter, rowBounds, resultHandler, key, boundSql);
          // cache：缓存队列实现类，FIFO
          // key：哈希值 [mappedStatementId + offset + limit + SQL + queryParams + environment]
          // list：查询的数据
          tcm.putObject(cache, key, list);
        }
        // 打印调试日志，记录二级缓存获取数据
        if (log.isDebugEnabled() && cache.getSize() > 0) {
          log.debug("二级缓存：{}", JSON.toJSONString(list));
        }
        return list;
      }
    }
    return delegate.<E>query(ms, parameter, rowBounds, resultHandler, key, boundSql);
  }

  @Override
  public <E> List<E> query(
      MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
      throws SQLException {
    // 1. 获取绑定SQL
    BoundSql boundSql = ms.getBoundSql(parameter);
    // 2. 创建缓存Key
    CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
    return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
  }

  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    return delegate.update(ms, parameter);
  }

  // 省略....
  @Override
  public void commit(boolean required) throws SQLException {
    delegate.commit(required);
    tcm.commit();
  }
  @Override
  public void close(boolean forceRollback) {
    try {
      if (forceRollback) {
        tcm.rollback();
      } else {
        tcm.commit();
      }
    } finally {
      delegate.close(forceRollback);
    }
  }

 	//省略....

  private void flushCacheIfRequired(MappedStatement ms) {
    Cache cache = ms.getCache();
    if (cache != null && ms.isFlushCacheRequired()) {
      tcm.clear(cache);
    }
  }
}
```

- 当缓存数据随着会话周期处理完后，则存放到 MappedStatement 所提供的 Cache 缓存队列中，也就是本章节所实现的 FiflCache 先进先出缓存实现类。
- 另外关于缓存的流转会调用 TransactionalCacheManager 事务缓存管理器进行操作，从会话作用域范围，通过会话的结束，刷新提交到二级缓存或者清空处理。

### R

装饰器: 可以再不破坏原有逻辑的前提下，完成功能通过配置开关的自由开启使用。

```java
  public Cache build() {
    // 装饰器进行装饰
    setDefaultImplementations();
    // 实例化一个缓存类
    Cache cache = newBaseCacheInstance(implementation, id);
    // 设置缓存喜属性
    setCacheProperties(cache);
    if (PerpetualCache.class.equals(cache.getClass())) {
      for (Class<? extends Cache> decorator : decorators) {
        // 使用装饰者模式包装
        cache = newCacheDecoratorInstance(decorator, cache);
        // 额外属性设置
        setCacheProperties(cache);
      }
    }
    return cache;
  }
```

## Spring 整合 Myabtis

`org.springframework.beans.factory.FactoryBean` 是 Spring 框架中的一个接口，主要用于创建和管理 bean 实例，可以自定义 Bean 的创建逻辑。

```java
public class MapperFactoryBean<T> implements FactoryBean<T> {

  private Class<T> mapperInterface;
  private SqlSessionFactory sqlSessionFactory;

  public MapperFactoryBean(Class<T> mapperInterface, SqlSessionFactory sqlSessionFactory) {
    log.info("{},构造函数:执行", mapperInterface);
    this.mapperInterface = mapperInterface;
    this.sqlSessionFactory = sqlSessionFactory;
  }

  @Override
  public T getObject() throws Exception {
    return sqlSessionFactory.openSession().getMapper(mapperInterface);
  }

  @Override
  public Class<?> getObjectType() {
    return mapperInterface;
  }
}

```

`org.springframework.beans.factory.InitializingBean` 是 Spring 框架中的一个接口，用于定义一个特定的回调方法，该方法在 Spring 容器实例化和填充完 bean 之后被调用。

- 自定义初始化逻辑，即这个逻辑是在 Spring 完成 bean 的属性设置后执行

```java
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean {
  private String resource;
  private SqlSessionFactory sqlSessionFactory;

  @Override
  public SqlSessionFactory getObject() throws Exception {
    return sqlSessionFactory;
  }

  @Override
  public Class<?> getObjectType() {
    return SqlSessionFactory.class;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    log.info("com.doublew2w.sbs.mybatis.spring.SqlSessionFactoryBean.afterPropertiesSet");
    try (Reader reader = Resources.getResourceAsReader(resource)) {
      this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void setResource(String resource) {
    this.resource = resource;
  }
}
```





`org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor` 是 Spring 框架中的一个接口，用于在 Spring 容器的 bean 定义注册中心中执行一些后处理逻辑。它允许我们自定义和修改 bean 定义的过程。

```java
public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor {
  private String basePackage;
  private SqlSessionFactory sqlSessionFactory;

  /**
   * 在标准初始化之后修改应用程序上下文的内部bean定义注册中心。将加载所有常规bean定义，但还没有实例化任何bean。这允许在下一个后处理阶段开始之前添加进一步的bean定义。
   *
   * @param registry 应用程序上下文使用的bean定义注册表
   */
  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    try {
      log.info("com.doublew2w.sbs.mybatis.spring.MapperScannerConfigurer.postProcessBeanDefinitionRegistry");
      // classpath*:cn/bugstack/**/dao/**/*.class
      String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";

      ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

      for (Resource resource : resources) {
        // 通过resource和默认类加载器，来读取类文件的元数据
        MetadataReader metadataReader =
            new SimpleMetadataReader(resource, ClassUtils.getDefaultClassLoader());

        ScannedGenericBeanDefinition beanDefinition =
            new ScannedGenericBeanDefinition(metadataReader);
        String beanName =
            Introspector.decapitalize(ClassUtils.getShortName(beanDefinition.getBeanClassName()));
        //设置资源和源信息
        beanDefinition.setResource(resource);
        beanDefinition.setSource(resource);
        beanDefinition.setScope("singleton");
        // 为beanDefinition的构造函数参数添加两个值：
        // 第一个参数是Bean的类名。
        // 第二个参数是sqlSessionFactory对象
        beanDefinition
            .getConstructorArgumentValues()
            .addGenericArgumentValue(beanDefinition.getBeanClassName());
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(sqlSessionFactory);
        beanDefinition.setBeanClass(MapperFactoryBean.class);

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
        registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 在标准初始化之后修改应用程序上下文的内部bean工厂。所有的bean定义都已加载，但还没有实例化任何bean。这允许覆盖或添加属性，甚至是对急于初始化的bean
   *
   * @param beanFactory 应用程序上下文使用的bean工厂
   */
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    log.info("com.doublew2w.sbs.mybatis.spring.MapperScannerConfigurer#postProcessBeanFactory()");
  }

  public void setBasePackage(String basePackage) {
    this.basePackage = basePackage;
  }

  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }
}
```

- 通过 resource 和默认类加载器，来读取类文件的元数据
- 创建 bean 定义信息
- 

`org.springframework.core.type.classreading.MetadataReader` 是 Spring 框架中用于读取类元数据的接口。它提供了一种访问类的注解、类信息和其他元数据的方式，通常在类扫描和处理过程中使用。

```java
public class SimpleMetadataReader implements MetadataReader {

  private final Resource resource;

  private final ClassMetadata classMetadata;

  private final AnnotationMetadata annotationMetadata;

  public SimpleMetadataReader(Resource resource, ClassLoader classLoader) throws IOException {
    log.info("com.doublew2w.sbs.mybatis.spring.SimpleMetadataReader.SimpleMetadataReader");
    ClassReader classReader;
    try (InputStream is = new BufferedInputStream(resource.getInputStream())) {
      classReader = new ClassReader(is);
    } catch (IllegalArgumentException ex) {
      throw new NestedIOException(
          "ASM ClassReader failed to parse class file - "
              + "probably due to a new Java class file version that isn't supported yet: "
              + resource,
          ex);
    }
    AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor(classLoader);
    classReader.accept(visitor, ClassReader.SKIP_DEBUG);

    this.annotationMetadata = visitor;
    // (since AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor)
    this.classMetadata = visitor;
    this.resource = resource;
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  @Override
  public ClassMetadata getClassMetadata() {
    return this.classMetadata;
  }

  @Override
  public AnnotationMetadata getAnnotationMetadata() {
    return this.annotationMetadata;
  }
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd     http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.0.xsd"
       default-autowire="byName">
    <context:component-scan base-package="com.doublew2w.sbs.mybatis"/>

    <aop:aspectj-autoproxy/>

    <bean id="sqlSessionFactory" class="com.doublew2w.sbs.mybatis.spring.SqlSessionFactoryBean">
        <property name="resource" value="mybatis-config.xml"/>
    </bean>

    <bean class="com.doublew2w.sbs.mybatis.spring.MapperScannerConfigurer">
        <!-- 注入sqlSessionFactory -->
        <property name="sqlSessionFactory" ref="sqlSessionFactory"/>
        <!-- 给出需要扫描Dao接口包 -->
        <property name="basePackage" value="com.doublew2w.sbs.mybatis.test.dao"/>
    </bean>

</beans>
```

首先通过 name 的方式进行自动注入，扫描的包路径是 `com.doublew2w.sbs.mybatis`。

```xml
   <bean id="sqlSessionFactory" class="com.doublew2w.sbs.mybatis.spring.SqlSessionFactoryBean">
        <property name="resource" value="mybatis-config.xml"/>
    </bean>
```

创建一个 `sqlSessionFactory` bean，bean 的类型是 `SqlSessionFactory`。通过构造函数来设置 `resources` 属性值。

由于实现了 `InitializingBean` ，因此会调用 `afterPropertiesSet()` 来完成 `this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);`  逻辑



```xml
    <bean class="com.doublew2w.sbs.mybatis.spring.MapperScannerConfigurer">
        <!-- 注入sqlSessionFactory -->
        <property name="sqlSessionFactory" ref="sqlSessionFactory"/>
        <!-- 给出需要扫描Dao接口包 -->
        <property name="basePackage" value="com.doublew2w.sbs.mybatis.test.dao"/>
    </bean>
```

创建一个 bean 对象 `MapperScannerConfigurer`，并完成 属性值 `sqlSessionFactory` 和 `basePackage` 的设置

紧接着执行 `postProcessBeanDefinitionRegistry()` 方法，完成 mapper 接口的 bean 注入。

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409260222544.png"/>
