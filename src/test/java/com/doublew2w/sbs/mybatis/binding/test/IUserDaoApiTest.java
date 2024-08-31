package com.doublew2w.sbs.mybatis.binding.test;

import com.doublew2w.sbs.mybatis.binding.MapperProxyFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 4:46
 * @project: sbs-mybatis
 */
public class IUserDaoApiTest {
  private final Logger logger = LoggerFactory.getLogger(IUserDaoApiTest.class);

  @Test
  public void test_MapperProxyFactory() {
    MapperProxyFactory<IUserDao> factory = new MapperProxyFactory<>(IUserDao.class);
    Map<String, String> sqlSession = new HashMap<>();
    sqlSession.put(
        "com.doublew2w.sbs.mybatis.binding.test.IUserDao.queryUserName",
        "模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户姓名");
    sqlSession.put(
        "com.doublew2w.sbs.mybatis.binding.test.IUserDao.queryUserAge",
        "模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户年龄");
    IUserDao userDao = factory.newInstance(sqlSession);
    String res = userDao.queryUserName("10001");
    logger.info("测试结果：{}", res);
  }

  @Test
  public void test_proxy_class() {
    IUserDao userDao =
        (IUserDao)
            Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {IUserDao.class},
                (proxy, method, args) -> "你被代理了！");
    String result = userDao.queryUserName("10001");
    logger.info(result);
  }
}
