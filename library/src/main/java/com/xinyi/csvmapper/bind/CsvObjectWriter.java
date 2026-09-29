package com.xinyi.csvmapper.bind;

import com.xinyi.csvmapper.buffered.writer.CsvWriter;
import com.xinyi.csvmapper.mapper.CsvRowAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * CSV 对象写入器
 *
 * <p> 通过 {@link CsvRowAdapter} 将行对象逐个序列化为 CSV 行。</p>
 *
 * @param <T> 源对象类型
 *
 * @author 新一
 * @date 2026/4/23 15:23
 */
public class CsvObjectWriter<T> implements Closeable {

    /**
     * CSV 写入器
     */
    private final CsvWriter mCsvWriter;

    /**
     * 行适配器
     */
    private final CsvRowAdapter<T> mRowAdapter;

    /**
     * 构造函数
     *
     * @param csvWriter CSV 写入器
     * @param rowAdapter 行适配器
     */
    public CsvObjectWriter(@NotNull CsvWriter csvWriter, @NotNull CsvRowAdapter<T> rowAdapter) {
        this.mCsvWriter = csvWriter;
        this.mRowAdapter = rowAdapter;
    }

    /**
     * 写入表头
     *
     * <p> 行类型没有表头时不写入。</p>
     *
     * @throws IOException 写入失败时抛出
     */
    public void writeHeader() throws IOException {
        List<String> header = mRowAdapter.header();
        if (header != null) {
            mCsvWriter.writeHeader(header);
        }
    }

    /**
     * 写入一个对象
     *
     * @param object 源对象
     * @throws IOException 写入失败时抛出
     */
    public void writeObject(@NotNull T object) throws IOException {
        mCsvWriter.writeRow(mRowAdapter.toCells(object));
    }

    /**
     * 写入对象列表
     *
     * @param objects 源对象列表
     * @throws IOException 写入失败时抛出
     */
    public void writeAll(@NotNull List<T> objects) throws IOException {
        for (T object : objects) {
            writeObject(object);
        }
    }

    @Override
    public void close() throws IOException {
        mCsvWriter.close();
    }
}