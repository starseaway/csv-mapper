package com.xinyi.csvmapper.buffered.reader;

import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * CSV 读取器接口
 *
 * <p> 定义 CSV 数据读取的核心行为，支持逐行读取和全量读取两种模式 </p>
 *
 * <p> 实现类需保证线程安全性由调用方负责，单个实例不应在多线程中并发使用 </p>
 *
 * @author 新一
 * @date 2026/4/23 13:58
 */
public interface CsvReader extends Closeable {

    /**
     * 读取下一行数据
     *
     * @return 下一行的 {@link CsvRow}，若已到达文件末尾则返回 null
     * @throws IOException 读取过程中发生 IO 错误
     */
    @Nullable
    CsvRow readNextRow() throws IOException;

    /**
     * 读取所有行数据
     *
     * <p> 该方法会将文件中所有行一次性加载到内存，大文件场景请使用 {@link #readNextRow()} 逐行读取 </p>
     *
     * @return 所有行的列表，若文件为空则返回空列表
     * @throws IOException 读取过程中发生 IO 错误
     */
    @NotNull
    List<CsvRow> readAllRows() throws IOException;

    /**
     * 获取表头行的列名列表
     *
     * <p> 仅当配置中启用了表头解析时有效，否则返回 null </p>
     *
     * @return 列名列表，未启用表头解析时返回 null
     */
    @Nullable
    List<String> getHeader();

    /**
     * 获取当前已读取的行号
     *
     * @return 当前行号（从 1 开始）
     */
    int getCurrentLineNumber();

    /**
     * 获取行迭代器，支持 for-each 遍历
     *
     * @return {@link CsvRowIterator}
     */
    @NotNull
    CsvRowIterator iterator();
}