package com.xinyi.csvmapper.bind.adapter;

import com.xinyi.csvmapper.mapper.CsvRowAdapter;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 单值行适配器
 *
 * <p> 一个值对应一行 CSV 数据，占用一个单元格。</p>
 * 
 * @author 新一
 * @date 2026/9/29 10:45
 */
final class ScalarRowAdapter<T> implements CsvRowAdapter<T> {

    /**
     * 值类型
     */
    private final Class<T> mValueType;

    /**
     * 构造函数
     *
     * @param valueType 值类型
     */
    ScalarRowAdapter(@NotNull Class<T> valueType) {
        this.mValueType = valueType;
    }

    @Nullable
    @Override
    public List<String> header() {
        return null;
    }

    @NotNull
    @Override
    public List<String> toCells(@NotNull T value) {
        return Collections.singletonList(CsvCells.format(value));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public T map(@NotNull CsvRow row) {
        Object value = CsvCells.parse(mValueType, row, 0, mValueType);
        // String 的空单元格可能转换为 null，单值行不返回 null
        return (T) (value == null ? "" : value);
    }
}