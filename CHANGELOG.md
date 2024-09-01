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

## S

在 MyBatis 的核心逻辑中，重要的一个逻辑就是为接口生成一个代理类，类中包含了对 Mapper XML 文件中的 SQL 信息进行解析和处理类型

- 入参
- 出参
- 条件

## T

目标是完成 XML 文件的解析和使用。

- 提供一个全局的配置类 Configuration，存放 XML 的解析内容。
- 通过 IO 流读取 XML 配置文件，然后解析 XML 节点进行「SQL 解析」、「Mapper 注册」。

## A

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

## R

### 建造者模式

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
