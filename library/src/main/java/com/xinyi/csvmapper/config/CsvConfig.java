package com.xinyi.csvmapper.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * CSV 通用配置
 *
 * <p> 定义 CSV 文件内容的格式规范，包括分隔符、引号字符、转义字符、编码等 </p>
 *
 * @author 新一
 * @date 2026/4/24 13:12
 */
public class CsvConfig {

    /**
     * 字段分隔符，默认为逗号
     */
    private final char delimiter;

    /**
     * 引号字符，用于包裹含有特殊字符的字段，默认为双引号
     */
    private final char quoteChar;

    /**
     * 转义字符，默认为反斜杠
     */
    private final char escapeChar;

    /**
     * 文件编码，默认为 UTF-8
     */
    private final Charset charset;

    /**
     * 是否跳过首行（表头行），默认为 false
     */
    private final boolean skipHeader;

    /**
     * 是否忽略字段首尾空白字符，默认为 false
     */
    private final boolean trimWhitespace;

    /**
     * 是否跳过空行，默认为 true
     */
    private final boolean skipEmptyLines;

    protected CsvConfig(Builder<?> builder) {
        this.delimiter = builder.delimiter;
        this.quoteChar = builder.quoteChar;
        this.escapeChar = builder.escapeChar;
        this.charset = builder.charset;
        this.skipHeader = builder.skipHeader;
        this.trimWhitespace = builder.trimWhitespace;
        this.skipEmptyLines = builder.skipEmptyLines;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isSkipHeader() {
        return skipHeader;
    }

    public boolean isTrimWhitespace() {
        return trimWhitespace;
    }

    public boolean isSkipEmptyLines() {
        return skipEmptyLines;
    }

    /**
     * 创建默认配置
     */
    public static CsvConfig defaultConfig() {
        return new Builder<>().build();
    }

    public static class Builder<T extends Builder<T>> {

        private char delimiter = ',';
        private char quoteChar = '"';
        private char escapeChar = '\\';
        private Charset charset = StandardCharsets.UTF_8;
        private boolean skipHeader = false;
        private boolean trimWhitespace = false;
        private boolean skipEmptyLines = true;

        /**
         * 返回当前 Builder 实例（子类覆盖此方法以支持链式调用时返回正确的子类型）
         *
         * <p> 通过抽象 self() 方法代替强转，彻底消除泛型 Builder 中的 unchecked 警告 </p>
         */
        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T delimiter(char delimiter) {
            this.delimiter = delimiter;
            return self();
        }

        public T quoteChar(char quoteChar) {
            this.quoteChar = quoteChar;
            return self();
        }

        public T escapeChar(char escapeChar) {
            this.escapeChar = escapeChar;
            return self();
        }

        public T charset(Charset charset) {
            this.charset = charset;
            return self();
        }

        public T skipHeader(boolean skipHeader) {
            this.skipHeader = skipHeader;
            return self();
        }

        public T trimWhitespace(boolean trimWhitespace) {
            this.trimWhitespace = trimWhitespace;
            return self();
        }

        public T skipEmptyLines(boolean skipEmptyLines) {
            this.skipEmptyLines = skipEmptyLines;
            return self();
        }

        public CsvConfig build() {
            return new CsvConfig(this);
        }
    }
}