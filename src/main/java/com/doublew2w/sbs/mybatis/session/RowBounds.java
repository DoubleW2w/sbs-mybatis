package com.doublew2w.sbs.mybatis.session;

import lombok.Getter;

/**
 * 分页记录限制
 *
 * @author: DoubleW2w
 * @date: 2024/9/14 17:00
 * @project: sbs-mybatis
 */
public class RowBounds {
  public static final int NO_ROW_OFFSET = 0;
  public static final int NO_ROW_LIMIT = Integer.MAX_VALUE;
  public static final RowBounds DEFAULT = new RowBounds();
  @Getter private final int offset;
  @Getter private final int limit;

  public RowBounds() {
    this.offset = NO_ROW_OFFSET;
    this.limit = NO_ROW_LIMIT;
  }

  public RowBounds(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }
}
