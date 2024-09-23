package com.doublew2w.sbs.mybatis.session;

import java.util.List;

/**
 * SqlSession 用来执行SQL，获取映射器，管理事务。
 *
 * @author: DoubleW2w
 * @date: 2024/9/1 5:38
 * @project: sbs-mybatis
 */
public interface SqlSession {
  /**
   * 根据指定的SqlID获取一条记录的封装对象
   *
   * @param <T> the returned object type 封装之后的对象类型
   * @param statement sqlID
   * @return Mapped object 封装之后的对象
   */
  <T> T selectOne(String statement);

  /**
   * 根据指定的SqlID获取一条记录的封装对象，只不过这个方法容许我们可以给sql传递一些参数
   *
   * <p>一般在实际使用中，这个参数传递的是pojo，或者Map或者ImmutableMap
   */
  <T> T selectOne(String statement, Object parameter);

  /** 获取多条记录，这个方法容许我们可以传递一些参数 */
  <E> List<E> selectList(String statement, Object parameter);

  /**
   * 执行delete语句。将返回受影响的行数。
   *
   * @param statement 与要执行的语句匹配的唯一标识符
   * @return 删除所影响的行数
   */
  int delete(String statement);

  /**
   * 执行delete语句。将返回受影响的行数
   *
   * @param statement 与要执行的语句匹配的唯一标识符。
   * @param parameter 传入语句的参数
   * @return 删除所影响的行数
   */
  int delete(String statement, Object parameter);

  /**
   * 执行update语句。将返回受影响的行数
   *
   * @param statement 与要执行的语句匹配的唯一标识符
   * @return 删除所影响的行数
   */
  int update(String statement);

  /**
   * 执行update语句。将返回受影响的行数
   *
   * @param statement 与要执行的语句匹配的唯一标识符
   * @param parameter 传入语句的参数
   * @return 删除所影响的行数
   */
  int update(String statement, Object parameter);

  /**
   * 执行insert语句
   *
   * @param statement 与要执行的语句匹配的唯一标识符
   * @return 传入语句的参数
   */
  int insert(String statement);

  /**
   * 使用给定的参数对象执行insert语句。
   *
   * @param statement 与要执行的语句匹配的唯一标识符
   * @param parameter 传入语句的参数
   * @return 受插入操作影响的行数。
   */
  int insert(String statement, Object parameter);

  /**
   * 以下是事务控制方法 commit,rollback Flushes batch statements and commits database connection. Note that
   * database connection will not be committed if no updates/deletes/inserts were called.
   */
  void commit();

  /**
   * 获取映射器，这个巧妙的使用了泛型，使得类型安全
   *
   * @param <T> the mapper type
   * @param type Mapper interface class
   * @return a mapper bound to this SqlSession
   */
  <T> T getMapper(Class<T> type);

  /**
   * 获取配置
   *
   * @return Configuration
   */
  Configuration getConfiguration();


  /**
   * 关闭Session
   */
  void close();

  /**
   * 清理 Session 缓存
   */
  void clearCache();
}
