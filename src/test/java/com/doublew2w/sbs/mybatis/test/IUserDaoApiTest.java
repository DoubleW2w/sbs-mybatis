package com.doublew2w.sbs.mybatis.test;

import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactoryBuilder;
import com.doublew2w.sbs.mybatis.test.dao.IUserDao;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 4:46
 * @project: sbs-mybatis
 */
public class IUserDaoApiTest {
  private final Logger logger = LoggerFactory.getLogger(IUserDaoApiTest.class);

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
