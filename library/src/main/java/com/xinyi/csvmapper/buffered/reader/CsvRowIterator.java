package com.xinyi.csvmapper.buffered.reader;

import com.xinyi.csvmapper.exception.CsvParseException;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * CSV 行迭代器
 *
 * <p> 对 {@link CsvReader} 的逐行读取进行迭代器封装，支持 for-each 语法 </p>
 *
 * @author 新一
 * @date 2026/4/27 11:42
 */
public class CsvRowIterator implements Iterator<CsvRow>, Iterable<CsvRow>, Closeable {

    /**
     * CSV 读取器接口
     */
    private final CsvReader mCsvReader;

    /**
     * 预读的下一行，用于支持 hasNext() 判断
     */
    private CsvRow mNextRow;

    /**
     * 是否已到达文件末尾
     */
    private boolean mReachedEnd = false;

    /**
     * 构造函数
     *
     * <p> 创建后立即预读第一行，以支持 {@link #hasNext()} 的首次判断 </p>
     *
     * @param csvReader 底层 CSV 读取器
     */
    public CsvRowIterator(@NotNull CsvReader csvReader) {
        this.mCsvReader = csvReader;
        // 预读第一行
        advance();
    }

    @Override
    public boolean hasNext() {
        return mNextRow != null;
    }

    @Override
    public CsvRow next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more rows to read");
        }
        CsvRow current = mNextRow;
        advance();
        return current;
    }

    @NotNull
    @Override
    public Iterator<CsvRow> iterator() {
        return this;
    }

    @Override
    public void close() throws IOException {
        mCsvReader.close();
    }

    /**
     * 预读下一行，更新 mNextRow 状态
     */
    private void advance() {
        if (mReachedEnd) {
            mNextRow = null;
            return;
        }
        try {
            mNextRow = mCsvReader.readNextRow();
            if (mNextRow == null) {
                mReachedEnd = true;
            }
        } catch (IOException exception) {
            mReachedEnd = true;
            mNextRow = null;
            throw new CsvParseException("Failed to read next row: " + exception.getMessage(), -1);
        }
    }
}