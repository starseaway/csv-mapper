package com.xinyi.csvmapper.bind.adapter;

import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.mapper.CsvConverterRegistry;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CSV 单元格转换工具
 *
 * <p> 为行适配器提供统一的单元格格式化和类型转换。</p>
 *
 * @author 新一
 * @date 2026/9/29 9:32
 */
final class CsvCells {

    private CsvCells() { }

    /**
     * 将值转换为 CSV 单元格文本
     *
     * <p> null 转换为空字符串。</p>
     *
     * @param value 单元格值
     */
    @NotNull
    static String format(@Nullable Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * 校验单元格类型是否受支持
     *
     * @param cellType 单元格类型
     * @param ownerType 所属行类型
     * @throws CsvMappingException 类型不支持时抛出
     */
    @NotNull
    static Class<?> requireSupported(@NotNull Class<?> cellType, @NotNull Class<?> ownerType) {
        if (!CsvConverterRegistry.isSupported(cellType)) {
            throw new CsvMappingException("Unsupported cell type: " + cellType.getSimpleName(), ownerType);
        }
        return cellType;
    }

    /**
     * 读取并转换单元格
     *
     * @param cellType 目标类型
     * @param row 当前行
     * @param column 列索引
     * @param ownerType 所属行类型
     * @throws CsvMappingException 转换失败时抛出
     */
    @Nullable
    static Object parse(@NotNull Class<?> cellType, @NotNull CsvRow row, int column, @NotNull Class<?> ownerType) {
        String rawValue = row.get(column);
        try {
            return CsvConverterRegistry.convert(cellType, rawValue);
        } catch (Exception exception) {
            throw new CsvMappingException("Line " + row.getLineNumber() + ", column " + column
                    + " cannot convert \"" + rawValue + "\" to " + cellType.getSimpleName(), ownerType, exception);
        }
    }
}