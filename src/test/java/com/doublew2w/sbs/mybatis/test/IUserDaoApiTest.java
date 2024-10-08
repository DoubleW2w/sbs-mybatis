package com.doublew2w.sbs.mybatis.test;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.session.*;
import com.doublew2w.sbs.mybatis.test.dao.IUserDao;
import com.doublew2w.sbs.mybatis.test.dao.IUserDaoAnno;
import com.doublew2w.sbs.mybatis.test.po.User;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

  SqlSession sqlSession;

  @BeforeEach
  public void init() throws IOException {
    // 1. 从SqlSessionFactory中获取SqlSession
    SqlSessionFactory sqlSessionFactory =
        new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
    sqlSession = sqlSessionFactory.openSession();
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

  @Test
  public void test_Executor() throws Exception {
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

  @Test
  public void test_branch09() throws Exception {
    // 1. 获取映射器对象
    IUserDao userDao = sqlSession.getMapper(IUserDao.class);

    // 2. 测试验证：基本参数
    User user = userDao.queryUserInfoById(1L);
    logger.info("测试结果：{}", JSON.toJSONString(user));

    // 1. 获取映射器对象
    IUserDao userDao2 = sqlSession.getMapper(IUserDao.class);

    // 2. 测试验证：对象参数
    User user2 = userDao2.queryUserInfo(new User(1L, "10001"));
    logger.info("测试结果：{}", JSON.toJSONString(user2));
  }

  @Test
  public void test_branch10() throws Exception {
    // 1. 获取映射器对象
    IUserDao userDao = sqlSession.getMapper(IUserDao.class);
    // 2. 测试验证：基本参数
    User user = userDao.queryUserInfoById(1L);
    logger.info("测试结果：{}", JSON.toJSONString(user));
  }

  @Test
  public void test_branch11_insertUserInfo() {
    // 1. 获取映射器对象
    IUserDao userDao = sqlSession.getMapper(IUserDao.class);
    // 2. 测试验证
    User user = new User();
    user.setUserId("10001");
    user.setUserName("小白");
    user.setUserHead("1_05");
    userDao.insertUserInfo(user);
    logger.info("测试结果：{}", "Insert OK");

    // 3. 提交事务
    sqlSession.commit();
  }

  @Test
  public void test_branch11_deleteUserInfoByUserId() {
    // 1. 获取映射器对象
    IUserDao userDao = sqlSession.getMapper(IUserDao.class);

    // 2. 测试验证
    int count = userDao.deleteUserInfoByUserId("10001");
    logger.info("测试结果：{}", count == 1);

    // 3. 提交事务
    sqlSession.commit();
  }

  @Test
  public void test_branch11_updateUserName() {
    // 1. 获取映射器对象
    IUserDao userDao = sqlSession.getMapper(IUserDao.class);

    // 2. 测试验证
    int count = userDao.updateUserName(new User(1L, "10001", "叮当猫"));
    logger.info("测试结果：{}", count);

    // 3. 提交事务
    sqlSession.commit();
  }

  @Test
  public void test_branch11_queryUserInfoList() {
    // 1. 获取映射器对象
    IUserDao userDao = sqlSession.getMapper(IUserDao.class);
    // 2. 测试验证：基本参数
    List<User> list = userDao.queryUserInfoList();
    logger.info("测试结果：{}", JSON.toJSONString(list));
  }

  @Test
  public void test_branch12_annotationConfigSql(){
    // 1. 获取映射器对象
    IUserDaoAnno dao = sqlSession.getMapper(IUserDaoAnno.class);
    // 2. 测试验证：基本参数
    User user = dao.queryUserInfo(new User(2L, "10001"));
    logger.info("测试结果：{}", JSON.toJSONString(user));
  }
}
