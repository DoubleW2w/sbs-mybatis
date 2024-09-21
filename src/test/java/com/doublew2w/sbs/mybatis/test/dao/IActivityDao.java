package com.doublew2w.sbs.mybatis.test.dao;

import com.doublew2w.sbs.mybatis.test.po.Activity;

/**
 * @author: DoubleW2w
 * @date: 2024/9/19 11:33
 * @project: sbs-mybatis
 */
public interface IActivityDao {
  Activity queryActivityById(Long activityId);

  Integer insert(Activity activity);
}
