package com.xinyi.csvmapper.buffered.writer;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.List;

/**
 * CSV 写入器接口
 *
 * <p> 定义 CSV 数据写入的核心行为，支持逐行写入和批量写入 </p>
 *
 * @author 新一
 * @date 2026/4/23 15:32
 */
public interface CsvWriter extends Closeable, Flushable {

    /**
     * 写入一行数据
     *
     * @param fields 字段列表，不可为 null
     * @throws IOException 写入过程中发生 IO 错误
     */
    void writeRow(@NotNull List<String> fields) throws IOException;

    /**
     * 写入一行数据（可变参数形式）
     *
     * @param fields 字段值，不可为 null
     * @throws IOException 写入过程中发生 IO 错误
     */
    void writeRow(@NotNull String... fields) throws IOException;

    /**
     * 写入表头行
     *
     * <p> 与 {@link #writeRow(List)} 行为相同，语义上用于写入列名 </p>
     *
     * @param headerNames 列名列表
     * @throws IOException 写入过程中发生 IO 错误
     */
    void writeHeader(@NotNull List<String> headerNames) throws IOException;

    /**
     * 批量写入多行数据
     *
     * @param rows 多行字段数据
     * @throws IOException 写入过程中发生 IO 错误
     */
    void writeAllRows(@NotNull List<List<String>> rows) throws IOException;

    /**
     * 获取已写入的行数（不含表头）
     */
    int getWrittenRowCount();
}