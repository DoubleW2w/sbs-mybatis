package com.doublew2w.sbs.mybatis.test.dao;

import com.doublew2w.sbs.mybatis.annotation.Select;
import com.doublew2w.sbs.mybatis.test.po.User;

/**
 * @author: DoubleW2w
 * @date: 2024/9/18 4:05
 * @project: sbs-mybatis
 */
public interface IUserDaoAnno {
  @Select("SELECT id, userId, userName, userHead FROM user where id = #{id} and userId = #{userId}")
  User queryUserInfo(User req);
}
