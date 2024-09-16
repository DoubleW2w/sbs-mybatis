package com.doublew2w.sbs.mybatis.test.dao;

import com.doublew2w.sbs.mybatis.test.po.User;
import java.util.List;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 4:46
 * @project: sbs-mybatis
 */
public interface IUserDao {
  String queryUserName(String uId);

  User queryUserInfo(User req);

  User queryUserInfoById(Long id);

  int updateUserName(User req);

  void insertUserInfo(User req);

  int deleteUserInfoByUserId(String userId);

  List<User> queryUserInfoList();
}
