## 创建简单的映射器代理工厂

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

