package com.xinyi.csvmapper.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CSV 字段级类型转换接口
 *
 * <p> 负责将 CSV 中的原始字符串字段值转换为目标 Java 类型，可用于注解映射层的自定义类型转换 </p>
 *
 * @param <T> 目标字段类型
 *
 * @author 新一
 * @date 2026/4/27 9:07
 */
public interface CsvFieldMapper<T> {

    /**
     * 将原始字符串字段值转换为目标类型
     *
     * @param rawValue CSV 中的原始字符串值，可能为 null 或空字符串
     * @return 转换后的目标类型值
     */
    @Nullable
    T convert(@NotNull String rawValue);
}