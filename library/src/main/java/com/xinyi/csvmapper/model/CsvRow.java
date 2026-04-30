package com.xinyi.csvmapper.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CSV 单行数据模型
 *
 * <p> 封装一行 CSV 解析后的字段列表，支持按索引和按列名两种方式访问字段值 </p>
 *
 * <p>
 *   当 CSV 文件包含表头时，可通过 {@link #get(String)} 按列名访问；
 *   否则只能通过 {@link #get(int)} 按索引访问。
 * </p>
 *
 * @author 新一
 * @date 2026/4/23 15:03
 */
public class CsvRow {

    /**
     * 当前行的行号（从 1 开始）
     */
    private final int lineNumber;

    /**
     * 当前行解析后的字段列表（不可变）
     */
    private final List<String> fields;

    /**
     * 列名到索引的映射（来自表头行，可为 null）
     */
    private final Map<String, Integer> headerIndex;

    /**
     * 构造函数
     *
     * @param lineNumber 行号（从 1 开始）
     * @param fields 解析后的字段列表
     * @param headerIndex 列名索引映射，无表头时传 null
     */
    public CsvRow(int lineNumber, @NotNull List<String> fields, @Nullable Map<String, Integer> headerIndex) {
        this.lineNumber = lineNumber;
        this.fields = Collections.unmodifiableList(fields);
        this.headerIndex = headerIndex;
    }

    /**
     * 按索引获取字段值
     *
     * @param index 列索引（从 0 开始）
     * @return 字段值，若索引越界则返回 null
     */
    @Nullable
    public String get(int index) {
        if (index < 0 || index >= fields.size()) {
            return null;
        }
        return fields.get(index);
    }

    /**
     * 按列名获取字段值
     *
     * <p> 需要 CSV 文件包含表头行，且解析时启用了表头解析 </p>
     *
     * @param columnName 列名
     * @return 字段值，若列名不存在或未启用表头则返回 null
     */
    @Nullable
    public String get(@NotNull String columnName) {
        if (headerIndex == null) {
            return null;
        }
        Integer index = headerIndex.get(columnName);
        if (index == null) {
            return null;
        }
        return get(index);
    }

    /**
     * 获取当前行的字段数量
     */
    public int size() {
        return fields.size();
    }

    /**
     * 获取当前行的行号（从 1 开始）
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * 获取当前行所有字段（不可变列表）
     */
    @NotNull
    public List<String> getFields() {
        return fields;
    }

    /**
     * 判断当前行是否为空行（所有字段均为空字符串）
     */
    public boolean isEmpty() {
        for (String field : fields) {
            if (field != null && !field.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    @Override
    public String toString() {
        return "CsvRow{lineNumber=" + lineNumber + ", fields=" + fields + "}";
    }
}