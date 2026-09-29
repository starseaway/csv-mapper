package com.xinyi.csvmapper.mapper;

import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * CSV 行适配器
 *
 * <p> 定义对象与 CSV 行之间的双向转换规则。</p>
 *
 * <p> 一个列表元素对应一行 CSV，元素类型会决定该行的列结构：</p>
 * <ul>
 *     <li> 对象：字段对应列 </li>
 *     <li> 数组：元素对应列 </li>
 *     <li> 集合：元素对应列 </li>
 *     <li> 单值：占用一列< /li>
 * </ul>
 *
 * @param <T> 行对应的类型
 *
 * @author 新一
 * @date 2026/9/28 17:40
 */
public interface CsvRowAdapter<T> extends CsvRowMapper<T> {

    /**
     * 获取表头列名
     *
     * @return 表头列名，无表头时返回 null
     */
    @Nullable
    List<String> header();

    /**
     * 将对象转换为一行单元格
     *
     * @param value 源对象
     * @return 单元格字符串列表
     */
    @NotNull
    List<String> toCells(@NotNull T value);

    /**
     * 将一行 CSV 数据转换为对象
     *
     * @param row 当前行数据
     * @return 转换后的对象
     */
    @NotNull
    @Override
    T map(@NotNull CsvRow row);
}