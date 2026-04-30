package com.xinyi.csvmapper.annotation;

import com.xinyi.csvmapper.mapper.CsvFieldMapper;
import com.xinyi.csvmapper.mapper.NoOpMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CSV 列绑定注解
 *
 * <p> 标注在 Java 对象的字段上，用于声明该字段与 CSV 列的映射关系 </p>
 *
 * @author 新一
 * @date 2026/4/23 14:52
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvColumn {

    /**
     * 绑定的列名
     *
     * <p> 与 CSV 表头中的列名对应，优先级高于 {@link #index()} </p>
     */
    String name() default "";

    /**
     * 绑定的列索引（从 0 开始）
     *
     * <p> 当 {@link #name()} 为空时生效 </p>
     */
    int index() default -1;

    /**
     * 自定义字段类型转换器
     *
     * <p> 指定一个 {@link CsvFieldMapper} 实现类，用于将原始字符串转换为目标类型 </p>
     */
    Class<? extends CsvFieldMapper<?>> mapper() default NoOpMapper.class;

    /**
     * 最大长度（字符数，默认为 0，表示不限制长度）
     *
     * <p> 大于 0 时生效：字段值超出时截断，并在末尾追加 {@link #truncateSuffix()} </p>
     */
    int maxLength() default 0;

    /**
     * 截断后缀
     *
     * <p> 字段值超出 {@link #maxLength()} 时，截断后在末尾追加此字符串 </p>
     */
    String truncateSuffix() default "…";
}