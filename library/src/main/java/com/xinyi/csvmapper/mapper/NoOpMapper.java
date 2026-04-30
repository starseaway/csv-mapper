package com.xinyi.csvmapper.mapper;

import com.xinyi.csvmapper.annotation.CsvColumn;

import org.jetbrains.annotations.NotNull;

/**
 * 默认字段转换器（空操作、占位实现）
 *
 * <p> 不进行转换，直接返回原始字符串，用于 {@link CsvColumn } 注解中的默认 mapper 实现</p>
 *
 * @author 新一
 * @date 2026/4/27 17:03
 */
public final class NoOpMapper implements CsvFieldMapper<Object> {

    /**
     * 直接返回原始值
     *
     * @param rawValue CSV 原始字符串
     * @return 原样返回的字符串
     */
    @Override
    public Object convert(@NotNull String rawValue) {
        return rawValue;
    }
}