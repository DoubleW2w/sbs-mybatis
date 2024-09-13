package com.doublew2w.sbs.mybatis.test.builder;

import com.doublew2w.sbs.mybatis.builder.SqlSourceBuilder;
import com.doublew2w.sbs.mybatis.mapping.BoundSql;
import com.doublew2w.sbs.mybatis.mapping.SqlSource;
import com.doublew2w.sbs.mybatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author: DoubleW2w
 * @date: 2024/9/14 1:47
 * @project: sbs-mybatis
 */
public class SqlSourceBuilderTest {
  private static Configuration configuration;
  private static SqlSourceBuilder sqlSourceBuilder;
  private final String sqlFromXml =
      "\t\n\n  SELECT * \n        FROM user\n \t        WHERE user_id = 1\n\t  ";
  private final String sqlFromXml2 = "\t\n\n  SELECT * \n        FROM user\n \t        WHERE user_id = #{userId}\n\t  ";
  @BeforeEach
  void setUp() {
    configuration = new Configuration();
    sqlSourceBuilder = new SqlSourceBuilder(configuration);
  }

  @Test
  void testShrinkWhitespacesInSqlIsFalse() {
    SqlSource sqlSource = sqlSourceBuilder.parse(sqlFromXml, null, null);
    BoundSql boundSql = sqlSource.getBoundSql(null);
    String actual = boundSql.getSql();
    Assertions.assertEquals(sqlFromXml, actual);
  }

  @Test
  void testShrinkWhitespacesInSqlIsFalse2() {
    SqlSource sqlSource = sqlSourceBuilder.parse(sqlFromXml2, null, null);
    BoundSql boundSql = sqlSource.getBoundSql(null);
    String actual = boundSql.getSql();
    Assertions.assertEquals(sqlFromXml, actual);
  }

}
