package com.doublew2w.sbs.mybatis.test;

import com.alibaba.fastjson2.JSON;
import com.doublew2w.sbs.mybatis.reflection.MetaObject;
import com.doublew2w.sbs.mybatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: DoubleW2w
 * @date: 2024/9/5 3:24
 * @project: sbs-mybatis
 */
public class ReflectionTest {

  @Test
  public void test_metaObject() {
    // 第一次读取
    Teca teca = new Teca();

    List<Teca.Stu> objects = new ArrayList<>();
    objects.add(new Teca.Stu());
    teca.setName("lili");
    teca.setStus(objects);

    MetaObject metaObject = SystemMetaObject.forObject(teca);
    System.out.println("getGetterNames：" + JSON.toJSONString(metaObject.getGetterNames()));
    System.out.println("getSetterNames：" + JSON.toJSONString(metaObject.getSetterNames()));
    System.out.println("name的get方法返回值：" + JSON.toJSONString(metaObject.getGetterType("name")));
    System.out.println("stus的set方法参数值：" + JSON.toJSONString(metaObject.getGetterType("stus")));
    System.out.println("name的hasGetter：" + metaObject.hasGetter("name"));
    // 出现：UnsupportedOperationException异常
    // System.out.println("stus.id(属性为集合)的hasGetter：" + metaObject.hasGetter("stus.id"));
    System.out.println("stu.id（属性为对象）的hasGetter：" + metaObject.hasGetter("stu.id"));
    // System.out.println("stu.id（属性为对象）的hasGetter：" + metaObject.hasGetter("stu"));
    // System.out.println("获取name的属性值：" + metaObject.getValue("name"));
    // 重新设置属性值
    metaObject.setValue("name", "huahua");
    System.out.println("设置name的属性值：" + metaObject.getValue("name"));
    // 设置属性（集合）的元素值
    metaObject.setValue("stus[0].id", "001");
    System.out.println(
        "获取stus集合的第一个元素的属性值：" + JSON.toJSONString(metaObject.getValue("stus[0].id")));
    System.out.println("对象的序列化：" + JSON.toJSONString(teca));
  }

 public static class Teca {

    private String name;

    private double price;

    private List<Stu> stus;

    private Stu stu;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public double getPrice() {
      return price;
    }

    public void setPrice(double price) {
      this.price = price;
    }

    public List<Stu> getStus() {
      return stus;
    }

    public void setStus(List<Stu> stus) {
      this.stus = stus;
    }

    public Stu getStu() {
      return stu;
    }

    public void setStu(Stu stu) {
      this.stu = stu;
    }


    public static class Stu {
      private String id;

      public String getId() {
        return id;
      }

      public void setId(String id) {
        this.id = id;
      }
    }
  }
}
