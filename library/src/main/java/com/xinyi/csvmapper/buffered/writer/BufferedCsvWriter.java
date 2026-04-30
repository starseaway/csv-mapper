package com.xinyi.csvmapper.buffered.writer;

import com.xinyi.csvmapper.config.CsvWriteConfig;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

/**
 * 基于 BufferedWriter 的高性能 CSV 写入器
 *
 * <p> 自动处理字段引号转义、特殊字符转义，符合 RFC 4180 规范 </p>
 *
 * @author 新一
 * @date 2026/4/27 13:54
 */
public class BufferedCsvWriter implements CsvWriter {

    /**
     * CSV 写入专用配置
     */
    private final CsvWriteConfig mConfig;

    /**
     * 用于写入 CSV 内容的缓冲写入器
     */
    private final BufferedWriter mBufferedWriter;

    /**
     * 已写入的数据行数（不含表头）
     */
    private int mWrittenRowCount = 0;

    /**
     * 构造函数
     *
     * @param outputStream 数据输出流，编码由 {@link CsvWriteConfig#getCharset()} 决定
     * @param config  CSV 写入配置
     */
    public BufferedCsvWriter(@NotNull OutputStream outputStream, @NotNull CsvWriteConfig config) {
        this.mConfig = config;
        this.mBufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, config.getCharset()), 8192);
    }

    /**
     * 写入一行数据，写入后行计数器加一
     *
     * @param fields 字段列表，不可为 null
     * @throws IOException 写入过程中发生 IO 错误
     */
    @Override
    public void writeRow(@NotNull List<String> fields) throws IOException {
        writeFields(fields);
        mWrittenRowCount++;
    }

    /**
     * 写入一行数据（可变参数形式）
     *
     * @param fields 字段值，不可为 null
     * @throws IOException 写入过程中发生 IO 错误
     */
    @Override
    public void writeRow(@NotNull String... fields) throws IOException {
        writeRow(Arrays.asList(fields));
    }

    /**
     * 写入表头行，不计入行计数器
     *
     * @param headerNames 列名列表
     * @throws IOException 写入过程中发生 IO 错误
     */
    @Override
    public void writeHeader(@NotNull List<String> headerNames) throws IOException {
        writeFields(headerNames);
    }

    /**
     * 批量写入多行数据
     *
     * @param rows 多行字段数据
     * @throws IOException 写入过程中发生 IO 错误
     */
    @Override
    public void writeAllRows(@NotNull List<List<String>> rows) throws IOException {
        for (List<String> row : rows) {
            writeRow(row);
        }
    }

    /**
     * 获取已写入的数据行数（不含表头）
     */
    @Override
    public int getWrittenRowCount() {
        return mWrittenRowCount;
    }

    /**
     * 将缓冲区内容强制刷入底层输出流
     *
     * @throws IOException 刷入过程中发生 IO 错误
     */
    @Override
    public void flush() throws IOException {
        mBufferedWriter.flush();
    }

    /**
     * 关闭写入器，自动刷入并释放底层资源
     *
     * @throws IOException 关闭过程中发生 IO 错误
     */
    @Override
    public void close() throws IOException {
        mBufferedWriter.close();
    }

    /**
     * 将字段列表序列化为一行 CSV 并写入缓冲区
     *
     * @param fields 字段列表
     * @throws IOException 写入过程中发生 IO 错误
     */
    private void writeFields(@NotNull List<String> fields) throws IOException {
        final char delimiter = mConfig.getDelimiter();
        final String lineSeparator = mConfig.getLineSeparator();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                mBufferedWriter.write(delimiter);
            }
            writeField(fields.get(i));
        }
        mBufferedWriter.write(lineSeparator);
    }

    /**
     * 序列化单个字段
     *
     * <p> 字段包含分隔符、引号字符、换行符时自动加引号；
     * 配置了 forceQuoteAll 时所有字段均加引号 </p>
     */
    private void writeField(@NotNull String field) throws IOException {
        final char quoteChar = mConfig.getQuoteChar();
        final char delimiter = mConfig.getDelimiter();

        boolean needsQuoting = mConfig.isForceQuoteAll()
                || field.indexOf(delimiter) >= 0
                || field.indexOf(quoteChar) >= 0
                || field.indexOf('\n') >= 0
                || field.indexOf('\r') >= 0;

        if (!needsQuoting) {
            mBufferedWriter.write(field);
            return;
        }

        // 引号字符通过双写转义
        mBufferedWriter.write(quoteChar);
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c == quoteChar) {
                mBufferedWriter.write(quoteChar);
            }
            mBufferedWriter.write(c);
        }
        mBufferedWriter.write(quoteChar);
    }
}