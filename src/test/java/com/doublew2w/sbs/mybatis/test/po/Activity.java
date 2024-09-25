package com.doublew2w.sbs.mybatis.test.po;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: DoubleW2w
 * @date: 2024/9/19 11:33
 * @project: sbs-mybatis
 */
@Data
@NoArgsConstructor
public class Activity {
  /** 自增ID */
  private Long id;

  /** 活动ID */
  private Long activityId;

  /** 活动名称 */
  private String activityName;

  /** 活动描述 */
  private String activityDesc;

  /** 创建人 */
  private String creator;

  /** 创建时间 */
  private Date createTime;

  /** 修改时间 */
  private Date updateTime;

  public Activity(Long activityId) {
    this.activityId = activityId;
  }
}
