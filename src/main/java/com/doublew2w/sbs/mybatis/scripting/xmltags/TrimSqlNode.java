package com.doublew2w.sbs.mybatis.scripting.xmltags;

import com.doublew2w.sbs.mybatis.session.Configuration;
import java.util.*;

/**
 * @author: DoubleW2w
 * @date: 2024/9/22 23:54
 * @project: sbs-mybatis
 */
public class TrimSqlNode implements SqlNode {

  private SqlNode contents;
  private String prefix;
  private String suffix;
  private List<String> prefixesToOverride;
  private List<String> suffixesToOverride;
  private Configuration configuration;

  public TrimSqlNode(
      Configuration configuration,
      SqlNode contents,
      String prefix,
      String prefixesToOverride,
      String suffix,
      String suffixesToOverride) {
    this(
        configuration,
        contents,
        prefix,
        parseOverrides(prefixesToOverride),
        suffix,
        parseOverrides(suffixesToOverride));
  }

  protected TrimSqlNode(
      Configuration configuration,
      SqlNode contents,
      String prefix,
      List<String> prefixesToOverride,
      String suffix,
      List<String> suffixesToOverride) {
    this.contents = contents;
    this.prefix = prefix;
    this.prefixesToOverride = prefixesToOverride;
    this.suffix = suffix;
    this.suffixesToOverride = suffixesToOverride;
    this.configuration = configuration;
  }

  @Override
  public boolean apply(DynamicContext context) {
    FilteredDynamicContext filteredDynamicContext = new FilteredDynamicContext(context);
    boolean result = contents.apply(filteredDynamicContext);
    filteredDynamicContext.applyAll();
    return result;
  }

  private static List<String> parseOverrides(String overrides) {
    if (overrides != null) {
      final StringTokenizer parser = new StringTokenizer(overrides, "|", false);
      final List<String> list = new ArrayList<String>(parser.countTokens());
      while (parser.hasMoreTokens()) {
        list.add(parser.nextToken().toUpperCase(Locale.ENGLISH));
      }
      return list;
    }
    return Collections.emptyList();
  }

  private class FilteredDynamicContext extends DynamicContext {
    private DynamicContext delegate;
    private boolean prefixApplied;
    private boolean suffixApplied;
    private StringBuilder sqlBuffer;

    public FilteredDynamicContext(DynamicContext delegate) {
      super(configuration, null);
      this.delegate = delegate;
      this.prefixApplied = false;
      this.suffixApplied = false;
      this.sqlBuffer = new StringBuilder();
    }

    public void applyAll() {
      sqlBuffer = new StringBuilder(sqlBuffer.toString().trim());
      String trimmedUppercaseSql = sqlBuffer.toString().toUpperCase(Locale.ENGLISH);
      if (!trimmedUppercaseSql.isEmpty()) {
        applyPrefix(sqlBuffer, trimmedUppercaseSql);
        applySuffix(sqlBuffer, trimmedUppercaseSql);
      }
      delegate.appendSql(sqlBuffer.toString());
    }

    @Override
    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    @Override
    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }

    @Override
    public void appendSql(String sql) {
      sqlBuffer.append(sql);
    }

    @Override
    public String getSql() {
      return delegate.getSql();
    }

    /**
     * 检查并添加SQL语句的前缀
     *
     * @param sql 正在构建的SQL查询字符串
     * @param trimmedUppercaseSql 已去除两端空格并转为大写的SQL查询字符串，用于比较
     */
    private void applyPrefix(StringBuilder sql, String trimmedUppercaseSql) {
      if (!prefixApplied) {
        prefixApplied = true;
        // 如果有需要覆盖的前缀列表，则尝试移除对应的前缀
        if (prefixesToOverride != null) {
          for (String toRemove : prefixesToOverride) {
            if (trimmedUppercaseSql.startsWith(toRemove)) {
              sql.delete(0, toRemove.trim().length());
              break;
            }
          }
        }
        // 如果有特定的前缀需要添加，则追加到SQL字符串中
        if (prefix != null) {
          sql.insert(0, " ");
          sql.insert(0, prefix);
        }
      }
    }

    /**
     * 检查并添加SQL语句的后缀
     *
     * @param sql 正在构建的SQL查询字符串
     * @param trimmedUppercaseSql 已去除两端空格并转为大写的SQL查询字符串，用于比较
     */
    private void applySuffix(StringBuilder sql, String trimmedUppercaseSql) {
      if (!suffixApplied) {
        suffixApplied = true;
        // 如果有需要覆盖的后缀列表，则尝试移除对应的后缀
        if (suffixesToOverride != null) {
          for (String toRemove : suffixesToOverride) {
            if (trimmedUppercaseSql.endsWith(toRemove)
                || trimmedUppercaseSql.endsWith(toRemove.trim())) {
              int start = sql.length() - toRemove.trim().length();
              int end = sql.length();
              sql.delete(start, end);
              break;
            }
          }
        }
        // 如果有特定的后缀需要添加，则追加到SQL字符串中
        if (suffix != null) {
          sql.append(" ");
          sql.append(suffix);
        }
      }
    }
  }
}
