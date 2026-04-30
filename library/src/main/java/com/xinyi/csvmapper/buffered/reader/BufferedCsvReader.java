package com.xinyi.csvmapper.buffered.reader;

import com.xinyi.csvmapper.config.CsvConfig;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 BufferedReader 的高性能 CSV 读取器
 *
 * <p> 内置采用有限状态机（FSM）逐字符解析 </p>
 *
 * @author 新一
 * @date 2026/4/27 9:29
 */
public class BufferedCsvReader implements CsvReader {

    /**
     * {@link #readNextChar} 返回值：标记行结束（换行符）
     */
    private static final int END_OF_ROW = -2;

    /**
     * {@link #readNextChar} 返回值：标记文件结束
     */
    private static final int END_OF_FILE = -1;

    /**
     * CSV 通用配置
     */
    private final CsvConfig mConfig;

    /**
     * 用于读取 CSV 内容的缓冲读取器
     */
    private final BufferedReader mBufferedReader;

    /**
     * 表头列名到索引的映射
     */
    private Map<String, Integer> mHeaderIndex;

    /**
     * 表头列名列表（保持顺序）
     */
    private List<String> mHeaderNames;

    /**
     * 当前已读取的行号
     */
    private int mCurrentLineNumber = 0;

    /**
     * 是否已完成表头初始化
     */
    private boolean mHeaderInitialized = false;

    /**
     * 构造函数
     *
     * @param inputStream 数据输入流，编码由 {@link CsvConfig#getCharset()} 决定
     * @param config CSV 解析配置
     */
    public BufferedCsvReader(@NotNull InputStream inputStream, @NotNull CsvConfig config) {
        this.mConfig = config;
        this.mBufferedReader = new BufferedReader(new InputStreamReader(inputStream, config.getCharset()), 8192);
    }

    @Nullable
    @Override
    public CsvRow readNextRow() throws IOException {
        // 首次读取时，若配置了 skipHeader，先消费表头行
        if (!mHeaderInitialized) {
            initializeHeader();
        }
        return readRow();
    }

    @NotNull
    @Override
    public List<CsvRow> readAllRows() throws IOException {
        List<CsvRow> rows = new ArrayList<>();
        CsvRow row;
        while ((row = readNextRow()) != null) {
            rows.add(row);
        }
        return rows;
    }

    @Nullable
    @Override
    public List<String> getHeader() {
        return mHeaderNames != null ? Collections.unmodifiableList(mHeaderNames) : null;
    }

    @Override
    public int getCurrentLineNumber() {
        return mCurrentLineNumber;
    }

    @NotNull
    @Override
    public CsvRowIterator iterator() {
        return new CsvRowIterator(this);
    }

    @Override
    public void close() throws IOException {
        mBufferedReader.close();
    }

    /**
     * 初始化表头
     *
     * <p> 若配置了 skipHeader，读取并解析第一行作为表头；否则标记为已初始化 </p>
     */
    private void initializeHeader() throws IOException {
        mHeaderInitialized = true;
        if (!mConfig.isSkipHeader()) {
            return;
        }
        List<String> headerFields = parseNextRowFields();
        if (headerFields == null) {
            return;
        }
        mHeaderNames = headerFields;
        mHeaderIndex = new HashMap<>(headerFields.size() * 2);
        for (int i = 0; i < headerFields.size(); i++) {
            mHeaderIndex.put(headerFields.get(i), i);
        }
    }

    /**
     * 读取下一个有效数据行
     *
     * <p> 自动跳过空行（若配置了 skipEmptyLines） </p>
     */
    @Nullable
    private CsvRow readRow() throws IOException {
        while (true) {
            List<String> fields = parseNextRowFields();
            if (fields == null) {
                return null;
            }
            int rowLineNumber = mCurrentLineNumber;
            CsvRow row = new CsvRow(rowLineNumber, fields, mHeaderIndex);
            if (mConfig.isSkipEmptyLines() && row.isEmpty()) {
                continue;
            }
            return row;
        }
    }

    /**
     * 核心解析方法，使用状态机解析下一行的所有字段
     *
     * <p> 支持跨行引号字段（字段内容包含换行符的情况） </p>
     *
     * @return 字段列表，到达文件末尾时返回 null
     */
    @Nullable
    private List<String> parseNextRowFields() throws IOException {
        int firstChar = mBufferedReader.read();
        if (firstChar == -1) {
            return null;
        }

        mCurrentLineNumber++;

        List<String> fields = new ArrayList<>();
        StringBuilder fieldBuffer = new StringBuilder(64);
        ParseState state = ParseState.FIELD_START;
        int currentChar = firstChar;

        while (currentChar != END_OF_FILE) {
            if (currentChar == END_OF_ROW) {
                // 引号字段内的换行属于字段内容，继续读取
                if (state == ParseState.IN_QUOTED_FIELD) {
                    fieldBuffer.append('\n');
                    mCurrentLineNumber++;
                    currentChar = readNextChar();
                    continue;
                }
                commitField(fields, fieldBuffer, state);
                return fields;
            }

            state = charStateTransfer((char) currentChar, state, fields, fieldBuffer);
            currentChar = readNextChar();
        }

        // 文件末尾无换行符：有字段内容或已提交过字段时，提交最后一个字段
        if (!fields.isEmpty() || fieldBuffer.length() > 0 || state == ParseState.QUOTE_IN_QUOTED_FIELD) {
            commitField(fields, fieldBuffer, state);
            return fields;
        }
        return null;
    }

    /**
     * 读取下一个字符，统一处理 \r\n
     *
     * <p>
     *   \r\n 视为一个换行，\r 单独也视为换行，均返回 {@link #END_OF_ROW}；
     *   文件末尾返回 {@link #END_OF_FILE}；其他字符原样返回。
     * </p>
     */
    private int readNextChar() throws IOException {
        int currentChar = mBufferedReader.read();
        if (currentChar == -1) {
            return END_OF_FILE;
        }
        if (currentChar == '\r') {
            mBufferedReader.mark(2);
            if (mBufferedReader.read() != '\n') {
                mBufferedReader.reset();
            }
            return END_OF_ROW;
        }
        if (currentChar == '\n') {
            return END_OF_ROW;
        }
        return currentChar;
    }

    /**
     * 处理单个字符的状态转移
     *
     * @param currentChar 当前字符
     * @param state 当前解析状态
     * @param fields 字段列表
     * @param fieldBuffer 当前字段内容缓冲区
     */
    @NotNull
    private ParseState charStateTransfer(char currentChar, ParseState state, List<String> fields, StringBuilder fieldBuffer) throws IOException {
        final char delimiter = mConfig.getDelimiter();
        final char quoteChar = mConfig.getQuoteChar();
        final char escapeChar = mConfig.getEscapeChar();

        switch (state) {
            case FIELD_START:
                if (currentChar == quoteChar) {
                    return ParseState.IN_QUOTED_FIELD;
                } else if (currentChar == delimiter) {
                    commitField(fields, fieldBuffer, state);
                    return ParseState.FIELD_START;
                } else if (currentChar == escapeChar) {
                    readEscapedChar(fieldBuffer);
                    return ParseState.IN_PLAIN_FIELD;
                } else {
                    fieldBuffer.append(currentChar);
                    return ParseState.IN_PLAIN_FIELD;
                }
            case IN_PLAIN_FIELD:
                if (currentChar == delimiter) {
                    commitField(fields, fieldBuffer, state);
                    return ParseState.FIELD_START;
                } else if (currentChar == escapeChar && escapeChar != quoteChar) {
                    readEscapedChar(fieldBuffer);
                    return ParseState.IN_PLAIN_FIELD;
                } else {
                    fieldBuffer.append(currentChar);
                    return ParseState.IN_PLAIN_FIELD;
                }
            case IN_QUOTED_FIELD:
                if (currentChar == escapeChar && escapeChar != quoteChar) {
                    readEscapedChar(fieldBuffer);
                    return ParseState.IN_QUOTED_FIELD;
                } else if (currentChar == quoteChar) {
                    return ParseState.QUOTE_IN_QUOTED_FIELD;
                } else {
                    fieldBuffer.append(currentChar);
                    return ParseState.IN_QUOTED_FIELD;
                }
            case QUOTE_IN_QUOTED_FIELD:
                if (currentChar == quoteChar) {
                    fieldBuffer.append(quoteChar);
                    return ParseState.IN_QUOTED_FIELD;
                } else if (currentChar == delimiter) {
                    commitField(fields, fieldBuffer, state);
                    return ParseState.FIELD_START;
                } else {
                    fieldBuffer.append(currentChar);
                    return ParseState.IN_PLAIN_FIELD;
                }
            default:
                return state;
        }
    }

    /**
     * 读取转义字符后的下一个字符并写入缓冲区
     *
     * @param fieldBuffer 当前字段内容缓冲区
     */
    private void readEscapedChar(StringBuilder fieldBuffer) throws IOException {
        int escaped = mBufferedReader.read();
        if (escaped != -1) {
            fieldBuffer.append((char) escaped);
        }
    }

    /**
     * 将当前字段缓冲区的内容提交到字段列表
     *
     * @param fields 字段列表，提交结果追加至此
     * @param fieldBuffer 当前字段内容缓冲区，提交后会被清空
     * @param state 当前解析状态，用于判断是否需要 trim
     */
    private void commitField(List<String> fields, StringBuilder fieldBuffer, ParseState state) {
        String value = fieldBuffer.toString();
        if (mConfig.isTrimWhitespace() && state != ParseState.IN_QUOTED_FIELD && state != ParseState.QUOTE_IN_QUOTED_FIELD) {
            value = value.trim();
        }
        fields.add(value);
        fieldBuffer.setLength(0);
    }

    /**
     * 字段解析状态
     */
    private enum ParseState {

        /// 字段起始，尚未读入任何字符
        FIELD_START,

        /// 正在读取普通（未引号包裹）字段
        IN_PLAIN_FIELD,

        /// 正在读取引号包裹的字段
        IN_QUOTED_FIELD,

        /// 在引号字段内遇到了引号，等待判断是转义引号还是字段结束
        QUOTE_IN_QUOTED_FIELD
    }
}