package com.xinyi.csvmapper.exception;

/**
 * CSV 映射异常
 *
 * <p> 在将 CSV 行数据映射到 Java 对象，或将 Java 对象序列化为 CSV 行时发生错误时抛出 </p>
 *
 * @author 新一
 * @date 2026/4/27 16:25
 */
public class CsvMappingException extends RuntimeException {

    /**
     * 发生映射错误的目标类型
     */
    private final Class<?> targetClass;

    /**
     * 构造异常
     *
     * @param message 异常信息
     */
    public CsvMappingException(String message) {
        super(message);
        this.targetClass = null;
    }

    /**
     * 构造异常
     *
     * @param message 异常信息
     * @param cause 原始异常
     */
    public CsvMappingException(String message, Throwable cause) {
        super(message, cause);
        this.targetClass = null;
    }

    /**
     * 构造异常
     *
     * @param message 异常信息
     * @param targetClass 映射目标类型
     */
    public CsvMappingException(String message, Class<?> targetClass) {
        super("Mapping to [" + targetClass.getSimpleName() + "] failed: " + message);
        this.targetClass = targetClass;
    }

    /**
     * 构造异常
     *
     * @param message 异常信息
     * @param targetClass 映射目标类型
     * @param cause 原始异常
     */
    public CsvMappingException(String message, Class<?> targetClass, Throwable cause) {
        super("Mapping to [" + targetClass.getSimpleName() + "] failed: " + message, cause);
        this.targetClass = targetClass;
    }

    /**
     * 获取映射目标类型
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }
}