package com.xinyi.csvmapper.bind;

import com.xinyi.csvmapper.buffered.reader.CsvReader;
import com.xinyi.csvmapper.mapper.CsvRowAdapter;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 对象读取器
 *
 * <p> 逐行读取 CSV，并通过 {@link CsvRowAdapter} 将每行数据反序列化为对象。</p>
 *
 * @param <T> 目标对象类型
 *
 * @author 新一
 * @date 2026/4/23 13:32
 */
public class CsvObjectReader<T> implements Closeable {

    /**
     * CSV 读取器
     */
    private final CsvReader mCsvReader;

    /**
     * 行适配器
     */
    private final CsvRowAdapter<T> mRowAdapter;

    /**
     * 构造函数
     *
     * @param csvReader CSV 读取器
     * @param rowAdapter 行适配器
     */
    public CsvObjectReader(@NotNull CsvReader csvReader, @NotNull CsvRowAdapter<T> rowAdapter) {
        this.mCsvReader = csvReader;
        this.mRowAdapter = rowAdapter;
    }

    /**
     * 读取下一行
     *
     * @return 下一行映射后的对象，到达文件末尾时返回 null
     * @throws IOException 读取失败时抛出
     */
    @Nullable
    public T readNext() throws IOException {
        CsvRow row = mCsvReader.readNextRow();
        if (row == null) {
            return null;
        }
        return mRowAdapter.map(row);
    }

    /**
     * 读取全部行数据并映射为对象列表
     *
     * @return 对象列表，文件为空时返回空列表
     * @throws IOException 读取失败时抛出
     */
    @NotNull
    public List<T> readAll() throws IOException {
        List<T> result = new ArrayList<>();
        CsvRow row;
        while ((row = mCsvReader.readNextRow()) != null) {
            result.add(mRowAdapter.map(row));
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        mCsvReader.close();
    }
}