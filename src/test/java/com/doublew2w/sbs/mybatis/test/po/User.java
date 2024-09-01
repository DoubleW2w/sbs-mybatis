package com.doublew2w.sbs.mybatis.test.po;

import lombok.Data;
import lombok.Setter;

import java.util.Date;

/**
 * @author: DoubleW2w
 * @date: 2024/9/1 18:06
 * @project: sbs-mybatis
 */
@Data
public class User {
  private Long id;
  private String userId; // 用户ID
  private String userHead; // 头像
  private Date createTime; // 创建时间
  private Date updateTime; // 更新时间
}
