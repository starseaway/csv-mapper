package com.xinyi.csvmapper.config;

/**
 * CSV 写入专用配置
 *
 * <p> 在 {@link CsvConfig} 基础上扩展了写入行为相关的配置项 </p>
 *
 * @author 新一
 * @date 2026/4/25 10:17
 */
public class CsvWriteConfig extends CsvConfig {

    /**
     * 行分隔符
     */
    private final String lineSeparator;

    /**
     * 是否对所有字段强制加引号
     */
    private final boolean forceQuoteAll;

    /**
     * 是否在写入前自动写出表头行
     */
    private final boolean writeHeader;

    /**
     * 构造函数
     *
     * @param builder 写入构建器
     */
    private CsvWriteConfig(WriteBuilder builder) {
        super(builder);
        this.lineSeparator = builder.lineSeparator;
        this.forceQuoteAll = builder.forceQuoteAll;
        this.writeHeader = builder.writeHeader;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public boolean isForceQuoteAll() {
        return forceQuoteAll;
    }

    public boolean isWriteHeader() {
        return writeHeader;
    }

    /**
     * 创建默认写入配置
     */
    public static CsvWriteConfig defaultConfig() {
        return new WriteBuilder().build();
    }

    /**
     * 写入配置构建器
     */
    public static class WriteBuilder extends CsvConfig.Builder<WriteBuilder> {

        /// 行分隔符（默认：系统换行符）
        private String lineSeparator = System.lineSeparator();
        /// 是否强制所有字段加引号（默认：false）
        private boolean forceQuoteAll = false;
        /// 是否写入表头（默认：false）
        private boolean writeHeader = false;

        /**
         * 设置行分隔符
         */
        public WriteBuilder lineSeparator(String lineSeparator) {
            this.lineSeparator = lineSeparator;
            return this;
        }

        /**
         * 设置是否强制所有字段加引号
         */
        public WriteBuilder forceQuoteAll(boolean forceQuoteAll) {
            this.forceQuoteAll = forceQuoteAll;
            return this;
        }

        /**
         * 设置是否写入表头
         */
        public WriteBuilder writeHeader(boolean writeHeader) {
            this.writeHeader = writeHeader;
            return this;
        }

        /**
         * 构建 {@link CsvWriteConfig}
         */
        @Override
        public CsvWriteConfig build() {
            return new CsvWriteConfig(this);
        }
    }
}