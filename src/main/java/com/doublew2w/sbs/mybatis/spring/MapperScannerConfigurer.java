package com.doublew2w.sbs.mybatis.spring;

import com.doublew2w.sbs.mybatis.session.SqlSessionFactory;
import java.beans.Introspector;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

/**
 * @author: DoubleW2w
 * @date: 2024/9/26 1:22
 * @project: sbs-mybatis
 */
@Slf4j
public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor {
  private String basePackage;
  private SqlSessionFactory sqlSessionFactory;

  /**
   * 在标准初始化之后修改应用程序上下文的内部bean定义注册中心。将加载所有常规bean定义，但还没有实例化任何bean。这允许在下一个后处理阶段开始之前添加进一步的bean定义。
   *
   * @param registry 应用程序上下文使用的bean定义注册表
   */
  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    try {
      log.info("com.doublew2w.sbs.mybatis.spring.MapperScannerConfigurer.postProcessBeanDefinitionRegistry");
      // classpath*:cn/bugstack/**/dao/**/*.class
      String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";

      ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

      for (Resource resource : resources) {
        // 通过resource和默认类加载器，来读取类文件的元数据
        MetadataReader metadataReader =
            new SimpleMetadataReader(resource, ClassUtils.getDefaultClassLoader());

        ScannedGenericBeanDefinition beanDefinition =
            new ScannedGenericBeanDefinition(metadataReader);
        String beanName =
            Introspector.decapitalize(ClassUtils.getShortName(beanDefinition.getBeanClassName()));
        //设置资源和源信息
        beanDefinition.setResource(resource);
        beanDefinition.setSource(resource);
        beanDefinition.setScope("singleton");
        // 为beanDefinition的构造函数参数添加两个值：
        // 第一个参数是Bean的类名。
        // 第二个参数是sqlSessionFactory对象
        beanDefinition
            .getConstructorArgumentValues()
            .addGenericArgumentValue(beanDefinition.getBeanClassName());
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(sqlSessionFactory);
        beanDefinition.setBeanClass(MapperFactoryBean.class);

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
        registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 在标准初始化之后修改应用程序上下文的内部bean工厂。所有的bean定义都已加载，但还没有实例化任何bean。这允许覆盖或添加属性，甚至是对急于初始化的bean
   *
   * @param beanFactory 应用程序上下文使用的bean工厂
   */
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    log.info("com.doublew2w.sbs.mybatis.spring.MapperScannerConfigurer#postProcessBeanFactory()");
  }

  public void setBasePackage(String basePackage) {
    log.info("setBasePackage");
    this.basePackage = basePackage;
  }

  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    log.info("setSqlSessionFactory");
    this.sqlSessionFactory = sqlSessionFactory;
  }
}
