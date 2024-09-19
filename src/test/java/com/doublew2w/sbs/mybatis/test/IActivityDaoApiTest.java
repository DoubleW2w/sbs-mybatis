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
  public void test_queryActivityById() {
    // 1. 获取映射器对象
    IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
    // 2. 测试验证
    Activity res = dao.queryActivityById(100001L);
    log.info("测试结果：{}", JSON.toJSONString(res));
  }
}
