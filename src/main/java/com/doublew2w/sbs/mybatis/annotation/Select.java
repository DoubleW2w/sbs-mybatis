package com.doublew2w.sbs.mybatis.annotation;

import java.lang.annotation.*;

/**
 * 指定用于检索记录的SQL的注释。
 *
 * <p><b>如何使用:</b>
 *
 * <pre>
 * public interface UserMapper {
 *   &#064;Select("SELECT id, name FROM users WHERE id = #{id}")
 *   User selectById(int id);
 * }
 * </pre>
 *
 * @author: DoubleW2w
 * @date: 2024/9/18 4:03
 * @project: sbs-mybatis
 */
@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {
  /** 查询SQL语句. */
  String[] value();
}
