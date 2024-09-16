package com.doublew2w.sbs.mybatis.test.po;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 18:06
 * @project: sbs-mybatis
 */
@Data
@NoArgsConstructor
public class User {
  private Long id;
  // 用户ID
  private String userId;
  // 用户名称
  private String userName;
  // 头像
  private String userHead;
  // 创建时间
  private Date createTime;
  // 更新时间
  private Date updateTime;

  public User(Long id, String userId) {
    this.id = id;
    this.userId = userId;
  }

  public User(Long id, String userId, String userName) {
    this.id = id;
    this.userId = userId;
    this.userName = userName;
  }
}
