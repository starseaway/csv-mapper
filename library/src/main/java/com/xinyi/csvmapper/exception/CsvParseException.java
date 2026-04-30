package com.xinyi.csvmapper.exception;

/**
 * CSV 解析异常
 *
 * <p> 在 CSV 数据读取或字段解析过程中发生格式错误时抛出 </p>
 *
 * @author 新一
 * @date 2026/4/27 13:31
 */
public class CsvParseException extends RuntimeException {

    /**
     * 发生异常的行号（从 1 开始，-1 表示未知）
     */
    private final int lineNumber;

    /**
     * 构造异常
     *
     * @param message 异常信息
     */
    public CsvParseException(String message) {
        super(message);
        this.lineNumber = -1;
    }

    /**
     * 构造异常
     *
     * @param message 异常信息
     * @param lineNumber 行号（从 1 开始）
     */
    public CsvParseException(String message, int lineNumber) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }

    /**
     * 构造异常
     *
     * @param message 异常信息
     * @param lineNumber 行号（从 1 开始）
     * @param cause 原始异常
     */
    public CsvParseException(String message, int lineNumber, Throwable cause) {
        super("Line " + lineNumber + ": " + message, cause);
        this.lineNumber = lineNumber;
    }

    /**
     * 获取发生异常的行号
     *
     * @return 行号（从 1 开始），-1 表示未知
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
