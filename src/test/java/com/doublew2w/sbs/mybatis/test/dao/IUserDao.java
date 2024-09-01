package com.doublew2w.sbs.mybatis.test.dao;

import com.doublew2w.sbs.mybatis.test.po.User;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 4:46
 * @project: sbs-mybatis
 */
public interface IUserDao {
  String queryUserName(String uId);

  Integer queryUserAge(String uId);

  User queryUserInfoById(Long uId);
}
