package com.doublew2w.sbs.mybatis.executor.keygen;

import com.doublew2w.sbs.mybatis.executor.Executor;
import com.doublew2w.sbs.mybatis.mapping.MappedStatement;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.session.Configuration;
import com.doublew2w.sbs.mybatis.session.RowBounds;
import java.sql.Statement;
import java.util.List;

/**
 * @author: DoubleW2w
 * @date: 2024/9/19 23:02
 * @project: sbs-mybatis
 */
public class SelectKeyGenerator implements KeyGenerator {
  public static final String SELECT_KEY_SUFFIX = "!selectKey";
  private final boolean executeBefore;
  private final MappedStatement keyStatement;

  public SelectKeyGenerator(MappedStatement keyStatement, boolean executeBefore) {
    this.executeBefore = executeBefore;
    this.keyStatement = keyStatement;
  }

  @Override
  public void processBefore(
      Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    if (executeBefore) {
      processGeneratedKeys(executor, ms, parameter);
    }
  }

  @Override
  public void processAfter(
      Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    if (!executeBefore) {
      processGeneratedKeys(executor, ms, parameter);
    }
  }

  private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
    try {
      if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
        String[] keyProperties = keyStatement.getKeyProperties();
        final Configuration configuration = ms.getConfiguration();
        final MetaObject metaParam = configuration.newMetaObject(parameter);
        // Do not close keyExecutor.
        // The transaction will be closed by parent executor.
        Executor keyExecutor = configuration.newExecutor(executor.getTransaction());
        List<Object> values =
            keyExecutor.query(
                keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        if (values.size() == 0) {
          throw new RuntimeException("SelectKey returned no data.");
        } else if (values.size() > 1) {
          throw new RuntimeException("SelectKey returned more than one value.");
        } else {
          MetaObject metaResult = configuration.newMetaObject(values.get(0));
          if (keyProperties.length == 1) {
            if (metaResult.hasGetter(keyProperties[0])) {
              setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
            } else {
              // no getter for the property - maybe just a single value object
              // so try that
              setValue(metaParam, keyProperties[0], values.get(0));
            }
          } else {
            handleMultipleProperties(keyProperties, metaParam, metaResult);
          }
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(
          "Error selecting key or setting result to parameter object. Cause: " + e, e);
    }
  }

  /**
   * 处理selectKey是多个的情况
   * @param keyProperties
   * @param metaParam
   * @param metaResult
   */
  private void handleMultipleProperties(
      String[] keyProperties, MetaObject metaParam, MetaObject metaResult) {
    String[] keyColumns = keyStatement.getKeyColumns();

    if (keyColumns == null || keyColumns.length == 0) {
      // 不指定键列，只使用属性名
      for (String keyProperty : keyProperties) {
        setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
      }
    } else {
      if (keyColumns.length != keyProperties.length) {
        throw new RuntimeException(
            "If SelectKey has key columns, the number must match the number of key properties.");
      }
      for (int i = 0; i < keyProperties.length; i++) {
        setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
      }
    }
  }

  /**
   * 通过元对象来设置selectey列的值
   *
   * @param metaParam 参数的元对象
   * @param property 属性
   * @param value 值
   */
  private void setValue(MetaObject metaParam, String property, Object value) {
    if (metaParam.hasSetter(property)) {
      metaParam.setValue(property, value);
    } else {
      throw new RuntimeException(
          "No setter found for the keyProperty '"
              + property
              + "' in "
              + metaParam.getOriginalObject().getClass().getName()
              + ".");
    }
  }
}
