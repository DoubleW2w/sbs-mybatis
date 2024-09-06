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

定义一个执行器 `Executor` 接口，使用执行器基类`BaseExecutor`定义模板流程，具体的查询实现交给子类

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
- `prepare()` 准备语句:简单语句和预处理语句 
- `parameterize` 参数化语句,负责完成语句参数的设置
- `query() `执行查询，执行SQL语句执行
- `ResultSetHandler` 结果集处理，通过调用 set 方法来设置结果

### R

#### 代码结构

<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409041405364.png"/>

## MetaObject元对象与反射的运用

> 代码分支：[07-meta-object-reflection](https://github.com/DoubleW2w/sbs-mybatis/tree/07-meta-object-reflection)

### S

在 `DataSourceFacotory` 中获取数据源存在硬编码，约定的配置比如 username,password 等是可以使用硬编码，但如果要进行扩展就很难知道其他的属性配置。

### T

实现元对象反射工具类，可以完成一个对象的属性的反射填充，提取出统一的设置和获取属性值的操作，并封装成一个工具包。

### A

#### 工程目录
<img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409061555241.png"/>

- reflection.factory: 负责创建需要的对象，负责进行实例化
- reflection.invoker: 提供一个`Invoker`接口，将对象类中的属性值获取和设置可以分为 Field 字段的 get/set(「字段setter和getter」`GetFieldInvoker`和`SetFieldInvoker`),还有普通的 Method 的调用(「方法调用器」`MethodInvoker`)。
- reflection.property: 完成属性名称的分解和属性名称的转换，可以做一些方法转换属性名称。
- reflection.wrapper: 对象包装器，定义了标准的get/set方法处理以及属性操作等方法，进一步反射调用处理。

  <img src="https://doublew2w-note-resource.oss-cn-hangzhou.aliyuncs.com/img/202409061607600.png"/>

#### 反射器Reflector

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
每一个类都有对应的一个反射器，当通过 Reflector 的构造函数创建出来时，会进行解析clazz信息。

#### 元类 MetaClass

MetaClass 依赖于 Reflector，不仅有基础的对象拆解功能，还能获取到 get/set 的Invoker方法。
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
将元对象 MetaObject理解成一个反射服务，它简化了对对象属性的读取、修改和访问操作。
- 获取属性值：getValue(String name)：根据属性名获取对象的属性值。支持嵌套属性的访问，例如 user.address.city。
- 设置属性值：setValue(String name, Object value)：根据属性名设置对象的属性值，支持嵌套属性的修改。
- 检测属性的可读写性：判断是否有getter和setter
- 对象的包装：MetaObject 可以通过 ObjectWrapper 包装对象，提供对对象的统一操作接口。

### R

本次反射拆了几个功能：「属性」、「调用」、「实例化」、「对象包装」。 将这几个功能合起来变成 `MetaObject`、`MetaClass`。

在实现上基本采用的模式是 一个顶层接口定义规范，基类实现顶层接口可以完成默认实现，或者提供模板流程，具体的规范实现交给子类。

