package com.xinyi.csvmapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CSV 字段忽略注解
 *
 * <p> 标注在 Java 对象的字段上，表示该字段在 CSV 读取和写入时均被忽略 </p>
 *
 * @author 新一
 * @date 2026/4/24 10:45
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvIgnore { }