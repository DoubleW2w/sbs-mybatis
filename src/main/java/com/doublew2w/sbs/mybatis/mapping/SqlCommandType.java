package com.doublew2w.sbs.mybatis.mapping;

/**
 * SQL 指令类型
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 17:41
 * @project: sbs-mybatis
 */
public enum SqlCommandType {
  /** 未知 */
  UNKNOWN,
  /** 插入 */
  INSERT,
  /** 更新 */
  UPDATE,
  /** 删除 */
  DELETE,
  /** 查找 */
  SELECT;
}
