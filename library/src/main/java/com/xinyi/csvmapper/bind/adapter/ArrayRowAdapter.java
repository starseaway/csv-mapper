package com.xinyi.csvmapper.bind.adapter;

import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.mapper.CsvRowAdapter;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 数组行适配器
 *
 * <p> 数组的每个元素对应一个 CSV 单元格。</p>
 *
 * @param <T> 数组类型
 *
 * @author 新一
 * @date 2026/9/29 9:28
 */
final class ArrayRowAdapter<T> implements CsvRowAdapter<T> {

    /**
     * 数组类型
     */
    private final Class<T> mArrayClass;

    /**
     * 数组元素类型
     */
    private final Class<?> mComponentType;

    /**
     * 构造函数
     *
     * @param arrayClass 数组类型
     * @throws CsvMappingException 元素类型不支持时抛出
     */
    ArrayRowAdapter(@NotNull Class<T> arrayClass) {
        this.mArrayClass = arrayClass;

        Class<?> componentType = arrayClass.getComponentType();
        if (componentType == null) {
            throw new CsvMappingException("Not an array type: " + arrayClass.getName());
        }
        this.mComponentType = CsvCells.requireSupported(componentType, arrayClass);
    }

    @Nullable
    @Override
    public List<String> header() {
        return null;
    }

    @NotNull
    @Override
    public List<String> toCells(@NotNull T value) {
        int length = Array.getLength(value);
        List<String> cells = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            cells.add(CsvCells.format(Array.get(value, i)));
        }
        return cells;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public T map(@NotNull CsvRow row) {
        Object array = Array.newInstance(mComponentType, row.size());
        for (int i = 0; i < row.size(); i++) {
            Object value = CsvCells.parse(mComponentType, row, i, mArrayClass);
            if (value != null) {
                Array.set(array, i, value);
            }
        }
        return (T) array;
    }
}