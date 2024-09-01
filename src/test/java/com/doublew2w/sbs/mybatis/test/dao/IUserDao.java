package com.doublew2w.sbs.mybatis.test.dao;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 4:46
 * @project: sbs-mybatis
 */
public interface IUserDao {
  String queryUserName(String uId);

  Integer queryUserAge(String uId);

  String queryUserInfoById(String uId);
}
