package com.doublew2w.sbs.mybatis.spring;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.ClassReader;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
import org.springframework.core.type.classreading.MetadataReader;

/**
 * 从给定的类文件资源中提取元数据信息，并为外部调用者提供这些信息。
 *
 * @author: DoubleW2w
 * @date: 2024/9/26 1:08
 * @project: sbs-mybatis
 */
@Slf4j
public class SimpleMetadataReader implements MetadataReader {

  private final Resource resource;

  private final ClassMetadata classMetadata;

  private final AnnotationMetadata annotationMetadata;

  public SimpleMetadataReader(Resource resource, ClassLoader classLoader) throws IOException {
    log.info("com.doublew2w.sbs.mybatis.spring.SimpleMetadataReader.SimpleMetadataReader");
    ClassReader classReader;
    try (InputStream is = new BufferedInputStream(resource.getInputStream())) {
      classReader = new ClassReader(is);
    } catch (IllegalArgumentException ex) {
      throw new NestedIOException(
          "ASM ClassReader failed to parse class file - "
              + "probably due to a new Java class file version that isn't supported yet: "
              + resource,
          ex);
    }
    AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor(classLoader);
    classReader.accept(visitor, ClassReader.SKIP_DEBUG);

    this.annotationMetadata = visitor;
    // (since AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor)
    this.classMetadata = visitor;
    this.resource = resource;
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  @Override
  public ClassMetadata getClassMetadata() {
    return this.classMetadata;
  }

  @Override
  public AnnotationMetadata getAnnotationMetadata() {
    return this.annotationMetadata;
  }
}
