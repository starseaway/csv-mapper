package com.xinyi.csvmapper;

import com.xinyi.csvmapper.bind.AnnotationCsvReader;
import com.xinyi.csvmapper.bind.AnnotationCsvWriter;
import com.xinyi.csvmapper.bind.CsvTypeToken;
import com.xinyi.csvmapper.config.CsvConfig;
import com.xinyi.csvmapper.config.CsvWriteConfig;
import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.buffered.reader.BufferedCsvReader;
import com.xinyi.csvmapper.buffered.reader.CsvReader;
import com.xinyi.csvmapper.utils.FileIO;
import com.xinyi.csvmapper.buffered.writer.BufferedCsvWriter;
import com.xinyi.csvmapper.buffered.writer.CsvWriter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * CSV 对象映射框架统一入口门面类
 *
 * <p> 入口类主要提供创建 CSV 读取器、写入器及注解驱动对象映射器的工厂方法 </p>
 *
 * @author 新一
 * @date 2026/4/23 19:42
 */
public final class CsvMapper {

    private CsvMapper() { }

    /**
     * 使用泛型类型令牌解析 CSV 文件为对象列表（使用默认配置，自动启用表头解析）
     *
     * @param file CSV 文件
     * @param csvTypeToken 泛型类型令牌，如 {@code new CsvTypeToken<List<UserModel>>() {}}
     * @throws IOException 读取过程中发生 IO 错误
     * @throws CsvMappingException 映射过程中发生类型转换错误
     */
    @NotNull
    public static <T> T parse(@NotNull File file, @NotNull CsvTypeToken<T> csvTypeToken) throws IOException {
        return parse(file, csvTypeToken, new CsvConfig.Builder<>().skipHeader(true).build());
    }

    /**
     * 使用泛型类型令牌解析 CSV 文件为对象列表
     *
     * @param file CSV 文件
     * @param csvTypeToken 泛型类型令牌
     * @param config 解析配置
     * @throws IOException 读取过程中发生 IO 错误
     * @throws CsvMappingException 映射过程中发生类型转换错误
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T parse(@NotNull File file, @NotNull CsvTypeToken<T> csvTypeToken, @NotNull CsvConfig config) throws IOException {
        Class<?> elementClass = csvTypeToken.getListElementClass();
        if (elementClass == null) {
            throw new CsvMappingException("Cannot resolve list element type from CsvTypeToken, use parseFirst() for single object");
        }
        CsvReader csvReader = reader(FileIO.inputStream(file), config);
        AnnotationCsvReader<?> annotationReader = new AnnotationCsvReader<>(csvReader, elementClass);
        return (T) annotationReader.readAll();
    }

    /**
     * 解析 CSV 文件为指定类型的对象列表（使用默认配置，自动启用表头解析）
     *
     * @param file CSV 文件
     * @param targetClass 目标对象类型（需有 public 无参构造函数）
     * @throws IOException 读取过程中发生 IO 错误
     */
    @NotNull
    public static <T> List<T> parse(@NotNull File file, @NotNull Class<T> targetClass) throws IOException {
        return parse(file, targetClass, new CsvConfig.Builder<>().skipHeader(true).build());
    }

    /**
     * 解析 CSV 文件为指定类型的对象列表
     *
     * @param file CSV 文件
     * @param targetClass 目标对象类型
     * @param config 解析配置
     * @throws IOException 读取过程中发生 IO 错误
     */
    @NotNull
    public static <T> List<T> parse(@NotNull File file, @NotNull Class<T> targetClass, @NotNull CsvConfig config) throws IOException {
        CsvReader csvReader = reader(new FileInputStream(file), config);
        try (AnnotationCsvReader<T> annotationReader = new AnnotationCsvReader<>(csvReader, targetClass)) {
            return annotationReader.readAll();
        }
    }

    /**
     * 将对象列表序列化为 CSV 文件（使用默认写入配置，自动写出表头）
     *
     * <p> 元素类型通过泛型类型令牌明确指定，与 {@link #parse(File, CsvTypeToken)} 完全对称 </p>
     *
     * @param file 目标 CSV 文件
     * @param objects 源对象列表
     * @param csvTypeToken 泛型类型令牌，用于明确指定元素类型
     * @throws IOException 写入过程中发生 IO 错误
     * @throws CsvMappingException 类型解析失败时抛出
     */
    public static <T> void serialize(@NotNull File file, @NotNull List<T> objects, @NotNull CsvTypeToken<List<T>> csvTypeToken) throws IOException {
        serialize(file, objects, csvTypeToken, CsvWriteConfig.defaultConfig());
    }

    /**
     * 将对象列表序列化为 CSV 文件
     *
     * @param file 目标 CSV 文件
     * @param objects 源对象列表
     * @param csvTypeToken 泛型类型令牌
     * @param config 写入配置
     * @throws IOException 写入过程中发生 IO 错误
     * @throws CsvMappingException 类型解析失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> void serialize(@NotNull File file, @NotNull List<T> objects, @NotNull CsvTypeToken<List<T>> csvTypeToken, @NotNull CsvWriteConfig config) throws IOException {
        Class<T> elementClass = (Class<T>) csvTypeToken.getListElementClass();
        if (elementClass == null) {
            throw new CsvMappingException("Cannot resolve list element type from CsvType");
        }
        CsvWriter csvWriter = writer(FileIO.outputStream(file), config);
        try (AnnotationCsvWriter<T> annotationWriter = new AnnotationCsvWriter<>(csvWriter, elementClass)) {
            annotationWriter.writeHeader();
            annotationWriter.writeAll(objects);
        }
    }

    /**
     * 将对象列表序列化为 CSV 文件（使用默认写入配置，自动写出表头）
     *
     * <p> 元素类型从列表第一个元素的运行时类型推断，列表不能为空。
     * 若存在多态场景（列表元素为子类实例但需按父类注解序列化），
     * 请改用 {@link #serialize(File, List, CsvTypeToken)} 显式指定类型 </p>
     *
     * @param file 目标 CSV 文件
     * @param objects 源对象列表，不能为空
     * @throws IOException 写入过程中发生 IO 错误
     * @throws CsvMappingException 列表为空时抛出
     */
    public static <T> void serialize(@NotNull File file, @NotNull List<T> objects) throws IOException {
        serialize(file, objects, CsvWriteConfig.defaultConfig());
    }

    /**
     * 将对象列表序列化为 CSV 文件
     *
     * @param file 目标 CSV 文件
     * @param objects 源对象列表，不能为空
     * @param config 写入配置
     * @throws IOException 写入过程中发生 IO 错误
     * @throws CsvMappingException 列表为空时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> void serialize(@NotNull File file, @NotNull List<T> objects, @NotNull CsvWriteConfig config) throws IOException {
        if (objects.isEmpty()) {
            throw new CsvMappingException("Cannot serialize empty list: element type is unknown");
        }
        // 从第一个元素的运行时类型推断
        Class<T> elementClass = (Class<T>) objects.get(0).getClass();
        CsvWriter csvWriter = writer(new FileOutputStream(file), config);
        try (AnnotationCsvWriter<T> annotationWriter = new AnnotationCsvWriter<>(csvWriter, elementClass)) {
            annotationWriter.writeHeader();
            annotationWriter.writeAll(objects);
        }
    }

    /**
     * 创建 CSV 读取器（使用默认配置）
     *
     * @param file CSV 文件
     * @throws IOException 文件不存在或无法读取时抛出
     */
    @NotNull
    public static CsvReader reader(@NotNull File file) throws IOException {
        return reader(file, CsvConfig.defaultConfig());
    }

    /**
     * 创建 CSV 读取器
     *
     * @param file CSV 文件
     * @param config 解析配置
     * @throws IOException 文件不存在或无法读取时抛出
     */
    @NotNull
    public static CsvReader reader(@NotNull File file, @NotNull CsvConfig config) throws IOException {
        return reader(new FileInputStream(file), config);
    }

    /**
     * 创建 CSV 读取器
     *
     * @param inputStream 输入流
     * @param config 解析配置
     */
    @NotNull
    public static CsvReader reader(@NotNull InputStream inputStream, @NotNull CsvConfig config) {
        return new BufferedCsvReader(inputStream, config);
    }

    /**
     * 创建 CSV 写入器（使用默认写入配置）
     *
     * @param file 目标 CSV 文件
     * @throws IOException 文件无法创建或写入时抛出
     */
    @NotNull
    public static CsvWriter writer(@NotNull File file) throws IOException {
        return writer(file, CsvWriteConfig.defaultConfig());
    }

    /**
     * 创建 CSV 写入器
     *
     * @param file 目标 CSV 文件
     * @param config 写入配置
     * @throws IOException 文件无法创建或写入时抛出
     */
    @NotNull
    public static CsvWriter writer(@NotNull File file, @NotNull CsvWriteConfig config) throws IOException {
        return writer(FileIO.outputStream(file), config);
    }

    /**
     * 创建 CSV 写入器
     *
     * @param outputStream 输出流
     * @param config 写入配置
     */
    @NotNull
    public static CsvWriter writer(@NotNull OutputStream outputStream, @NotNull CsvWriteConfig config) {
        return new BufferedCsvWriter(outputStream, config);
    }

    /**
     * 创建注解驱动的对象读取器（使用默认配置，自动启用表头解析）
     *
     * @param file CSV 文件
     * @param targetClass 目标对象类型（需有 public 无参构造函数）
     * @throws IOException 文件不存在或无法读取时抛出
     */
    @NotNull
    public static <T> AnnotationCsvReader<T> objectReader(@NotNull File file, @NotNull Class<T> targetClass) throws IOException {
        CsvConfig config = new CsvConfig.Builder<>().skipHeader(true).build();
        return objectReader(file, targetClass, config);
    }

    /**
     * 创建注解驱动的对象读取器
     *
     * @param file CSV 文件
     * @param targetClass 目标对象类型
     * @param config 解析配置
     * @throws IOException 文件不存在或无法读取时抛出
     */
    @NotNull
    public static <T> AnnotationCsvReader<T> objectReader(@NotNull File file, @NotNull Class<T> targetClass, @NotNull CsvConfig config) throws IOException {
        CsvReader csvReader = reader(new FileInputStream(file), config);
        return new AnnotationCsvReader<>(csvReader, targetClass);
    }

    /**
     * 创建注解驱动的对象写入器（使用默认写入配置）
     *
     * @param file 目标 CSV 文件
     * @param sourceClass 源对象类型
     * @return {@link AnnotationCsvWriter}
     * @throws IOException 文件无法创建或写入时抛出
     */
    @NotNull
    public static <T> AnnotationCsvWriter<T> objectWriter(@NotNull File file, @NotNull Class<T> sourceClass) throws IOException {
        return objectWriter(file, sourceClass, CsvWriteConfig.defaultConfig());
    }

    /**
     * 创建注解驱动的对象写入器
     *
     * @param file 目标 CSV 文件
     * @param sourceClass 源对象类型
     * @param config 写入配置
     * @return {@link AnnotationCsvWriter}
     * @throws IOException 文件无法创建或写入时抛出
     */
    @NotNull
    public static <T> AnnotationCsvWriter<T> objectWriter(@NotNull File file, @NotNull Class<T> sourceClass, @NotNull CsvWriteConfig config) throws IOException {
        CsvWriter csvWriter = writer(FileIO.outputStream(file), config);
        return new AnnotationCsvWriter<>(csvWriter, sourceClass);
    }
}