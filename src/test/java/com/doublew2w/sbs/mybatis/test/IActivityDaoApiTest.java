package com.doublew2w.sbs.mybatis.test;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.io.Resources;
import com.doublew2w.sbs.mybatis.session.SqlSession;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;
import com.doublew2w.sbs.mybatis.session.SqlSessionFactoryBuilder;
import com.doublew2w.sbs.mybatis.test.dao.IActivityDao;
import com.doublew2w.sbs.mybatis.test.po.Activity;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
