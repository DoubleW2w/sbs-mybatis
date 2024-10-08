package com.doublew2w.sbs.mybatis.test;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactoryBuilder;
import com.doublew2w.sbs.mybatis.test.dao.IActivityDao;
import com.doublew2w.sbs.mybatis.test.po.Activity;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import ognl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: DoubleW2w
 * @date: 2024/9/19 11:34
 * @project: sbs-mybatis
 */
@Slf4j
public class IActivityDaoApiTest {

  private SqlSession sqlSession;

  @BeforeEach
  public void init() throws IOException {
    // 1. 从SqlSessionFactory中获取SqlSession
    SqlSessionFactory sqlSessionFactory =
        new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
    sqlSession = sqlSessionFactory.openSession();
  }

  @Test
  public void test_branch13_queryActivityById() {
    // 1. 获取映射器对象
    IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
    // 2. 测试验证
    Activity res = dao.queryActivityById(100001L);
    log.info("测试结果：{}", JSON.toJSONString(res));
  }

  @Test
  public void test_branch14_insert() {
    // 1. 获取映射器对象
    IActivityDao dao = sqlSession.getMapper(IActivityDao.class);

    Activity activity = new Activity();
    activity.setActivityId(10007L);
    activity.setActivityName("测试活动");
    activity.setActivityDesc("测试数据插入");
    activity.setCreator("xiaofuge");

    // 2. 测试验证
    Integer res = dao.insert(activity);
    sqlSession.commit();

    log.info("测试结果：count：{} idx：{}", res, JSON.toJSONString(activity.getId()));
  }

  @Test
  public void test_branch15_query() {
    // 2. 获取映射器对象
    IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
    // 3. 测试验证
    Activity req = new Activity();
    req.setActivityId(100001L);
    Activity res = dao.queryActivityByIdForDynamicSql(req);
    log.info("测试结果：{}", JSON.toJSONString(res));
  }

  @Test
  public void test_ognl() throws OgnlException {
    Activity req = new Activity();
    req.setActivityId(1L);
    req.setActivityName("测试活动");
    req.setActivityDesc("小傅哥的测试内容");

    OgnlContext context =
        new OgnlContext(
            new AbstractMemberAccess() {
              @Override
              public boolean isAccessible(
                  Map context, Object target, Member member, String propertyName) {
                int modifiers = member.getModifiers();
                return Modifier.isPublic(modifiers);
              }
            },
            null,
            null,
            null);
    context.setRoot(req);
    Object root = context.getRoot();

    Object activityName = Ognl.getValue("activityName", context, root);
    Object activityDesc = Ognl.getValue("activityDesc", context, root);
    Object value = Ognl.getValue("activityDesc.length()", context, root);

    System.out.println(activityName + "\t" + activityDesc + " length：" + value);
  }

  @Test
  public void test_branch16_plugin_parse() throws Exception {
    // 2. 获取映射器对象
    IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
    // 2. 测试验证
    Activity res = dao.queryActivityById(100001L);
    log.info("测试结果：{}", JSON.toJSONString(res));
  }

  @Test
  public void test_branch17_first_cache() {

    // 2. 获取映射器对象
    IActivityDao dao = sqlSession.getMapper(IActivityDao.class);

    // 3. 测试验证
    Activity req = new Activity();
    req.setActivityId(100001L);

    log.info("测试结果：{}", JSON.toJSONString(dao.queryActivityByIdForDynamicSql(req)));

    // sqlSession.commit();
    // sqlSession.clearCache();
    sqlSession.close();

    log.info("测试结果：{}", JSON.toJSONString(dao.queryActivityByIdForDynamicSql(req)));
  }

  @Test
  public void test_branch18_second_cache() throws IOException {
    // 1. 从SqlSessionFactory中获取SqlSession
    Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

    // 2. 请求对象
    Activity req = new Activity();
    req.setActivityId(100001L);

    // 3. 第一组：SqlSession
    // 3.1 开启 Session
    SqlSession sqlSession01 = sqlSessionFactory.openSession();
    // 3.2 获取映射器对象
    IActivityDao dao01 = sqlSession01.getMapper(IActivityDao.class);
    log.info("测试结果01：{}", JSON.toJSONString(dao01.queryActivityByIdUseSecondLevelCache(req)));
    sqlSession01.close();

    // 4. 第一组：SqlSession
    // 4.1 开启 Session
    SqlSession sqlSession02 = sqlSessionFactory.openSession();
    // 4.2 获取映射器对象
    IActivityDao dao02 = sqlSession02.getMapper(IActivityDao.class);
    log.info("测试结果02：{}", JSON.toJSONString(dao02.queryActivityByIdUseSecondLevelCache(req)));
    sqlSession02.close();
  }

  @Test
  public void test_branch19_ClassPathXmlApplicationContext() {
    BeanFactory beanFactory = new ClassPathXmlApplicationContext("spring-config.xml");
    IActivityDao dao = beanFactory.getBean("IActivityDao", IActivityDao.class);
    Activity res = dao.queryActivityByIdForDynamicSql(new Activity(100001L));
    log.info("测试结果：{}", JSON.toJSONString(res));
  }
}
