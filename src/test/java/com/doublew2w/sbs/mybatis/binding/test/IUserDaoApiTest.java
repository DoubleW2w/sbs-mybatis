package com.doublew2w.sbs.mybatis.binding.test;

import com.doublew2w.sbs.mybatis.binding.MapperRegistry;
import com.doublew2w.sbs.mybatis.binding.test.dao.IUserDao;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;
import com.doublew2w.sbs.mybatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 4:46
 * @project: sbs-mybatis
 */
public class IUserDaoApiTest {
  private final Logger logger = LoggerFactory.getLogger(IUserDaoApiTest.class);

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
    logger.info( res);
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
