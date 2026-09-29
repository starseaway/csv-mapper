package com.xinyi.csvmapper;

import com.xinyi.csvmapper.bind.CsvObjectReader;
import com.xinyi.csvmapper.bind.CsvObjectWriter;
import com.xinyi.csvmapper.bind.CsvTypeToken;
import com.xinyi.csvmapper.bind.adapter.CsvRowAdapters;
import com.xinyi.csvmapper.buffered.reader.BufferedCsvReader;
import com.xinyi.csvmapper.buffered.reader.CsvReader;
import com.xinyi.csvmapper.buffered.writer.BufferedCsvWriter;
import com.xinyi.csvmapper.buffered.writer.CsvWriter;
import com.xinyi.csvmapper.config.CsvConfig;
import com.xinyi.csvmapper.config.CsvWriteConfig;
import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.mapper.CsvRowAdapter;
import com.xinyi.csvmapper.utils.FileIO;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * CSV 映射入口
 *
 * <p> 提供 CSV 解析、序列化以及底层读写器的创建方法，数据来源支持文件和流。</p>
 *
 * <p> 列表中的每个元素对应一行，支持注解对象、数组、集合和单值，详见 {@link CsvRowAdapters}。</p>
 *
 * @author 新一
 * @date 2026/4/23 19:42
 */
public final class CsvMapper {

    private CsvMapper() { }

    /**
     * 解析 CSV 文件
     *
     * @param file CSV 文件
     * @param csvTypeToken 目标列表类型
     * @throws IOException 读取失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> List<T> parse(@NotNull File file, @NotNull CsvTypeToken<List<T>> csvTypeToken) throws IOException {
        return parse(file, csvTypeToken, defaultParseConfig(listElementAdapter(csvTypeToken)));
    }

    /**
     * 解析 CSV 文件
     *
     * @param file CSV 文件
     * @param csvTypeToken 目标列表类型
     * @param config 解析配置
     * @throws IOException 读取失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> List<T> parse(@NotNull File file, @NotNull CsvTypeToken<List<T>> csvTypeToken, @NotNull CsvConfig config) throws IOException {
        CsvRowAdapter<T> rowAdapter = (CsvRowAdapter<T>) listElementAdapter(csvTypeToken);
        return parseList(FileIO.inputStream(file), rowAdapter, config);
    }

    /**
     * 解析 CSV 文件
     * 
     * @param file CSV 文件
     * @param targetClass 目标类型
     * @throws IOException 读取失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> List<T> parse(@NotNull File file, @NotNull Class<T> targetClass) throws IOException {
        return parse(file, targetClass, defaultParseConfig(CsvRowAdapters.get(targetClass)));
    }

    /**
     * 解析 CSV 文件
     *
     * @param file CSV 文件
     * @param targetClass 目标类型
     * @param config 解析配置
     * @throws IOException 读取失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> List<T> parse(@NotNull File file, @NotNull Class<T> targetClass, @NotNull CsvConfig config) throws IOException {
        CsvRowAdapter<T> rowAdapter = CsvRowAdapters.get(targetClass);
        return parseList(FileIO.inputStream(file), rowAdapter, config);
    }

    /**
     * 解析 CSV 输入流
     *
     * @param inputStream 输入流
     * @param csvTypeToken 目标列表类型
     * @throws IOException 读取失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> List<T> parse(@NotNull InputStream inputStream, @NotNull CsvTypeToken<List<T>> csvTypeToken) throws IOException {
        return parse(inputStream, csvTypeToken, defaultParseConfig(listElementAdapter(csvTypeToken)));
    }

    /**
     * 解析 CSV 输入流
     *
     * @param inputStream 输入流
     * @param csvTypeToken 目标列表类型
     * @param config 解析配置
     * @throws IOException 读取失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> List<T> parse(@NotNull InputStream inputStream, @NotNull CsvTypeToken<List<T>> csvTypeToken, @NotNull CsvConfig config) throws IOException {
        return parseList(inputStream, (CsvRowAdapter<T>) listElementAdapter(csvTypeToken), config);
    }

    /**
     * 解析 CSV 输入流
     * 
     * @param inputStream 输入流
     * @param targetClass 目标类型
     * @throws IOException 读取失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> List<T> parse(@NotNull InputStream inputStream, @NotNull Class<T> targetClass) throws IOException {
        return parse(inputStream, targetClass, defaultParseConfig(CsvRowAdapters.get(targetClass)));
    }

    /**
     * 解析 CSV 输入流
     *
     * @param inputStream 输入流
     * @param targetClass 目标类型
     * @param config 解析配置
     * @throws IOException 读取失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> List<T> parse(@NotNull InputStream inputStream, @NotNull Class<T> targetClass, @NotNull CsvConfig config) throws IOException {
        return parseList(inputStream, CsvRowAdapters.get(targetClass), config);
    }

    /**
     * 将对象列表序列化为 CSV 文件
     *
     * @param file 目标 CSV 文件
     * @param objects 对象列表
     * @param csvTypeToken 目标列表类型
     * @throws IOException 写入失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    public static <T> void serialize(@NotNull File file, @NotNull List<T> objects, @NotNull CsvTypeToken<List<T>> csvTypeToken) throws IOException {
        serialize(file, objects, csvTypeToken, CsvWriteConfig.defaultConfig());
    }

    /**
     * 将对象列表序列化为 CSV 文件
     *
     * @param file 目标 CSV 文件
     * @param objects 对象列表
     * @param csvTypeToken 目标列表类型
     * @param config 写入配置
     * @throws IOException 写入失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> void serialize(@NotNull File file, @NotNull List<T> objects, @NotNull CsvTypeToken<List<T>> csvTypeToken, @NotNull CsvWriteConfig config) throws IOException {
        CsvRowAdapter<T> rowAdapter = (CsvRowAdapter<T>) listElementAdapter(csvTypeToken);
        serializeList(FileIO.outputStream(file), objects, rowAdapter, config);
    }

    /**
     * 将对象列表序列化为 CSV 文件
     *
     * <p> 元素类型从第一个元素的运行时类型推断，因此列表不能为空。</p>
     *
     * @param file 目标 CSV 文件
     * @param objects 对象列表
     * @throws IOException 写入失败时抛出
     * @throws CsvMappingException 列表为空或类型映射失败时抛出
     */
    public static <T> void serialize(@NotNull File file, @NotNull List<T> objects) throws IOException {
        serialize(file, objects, CsvWriteConfig.defaultConfig());
    }

    /**
     * 将对象列表序列化为 CSV 文件
     *
     * <p> 元素类型从第一个元素的运行时类型推断，因此列表不能为空。</p>
     *
     * @param file 目标 CSV 文件
     * @param objects 对象列表
     * @param config 写入配置
     * @throws IOException 写入失败时抛出
     * @throws CsvMappingException 列表为空或类型映射失败时抛出
     */
    public static <T> void serialize(@NotNull File file, @NotNull List<T> objects, @NotNull CsvWriteConfig config) throws IOException {
        CsvRowAdapter<T> rowAdapter = inferElementAdapter(objects);
        serializeList(FileIO.outputStream(file), objects, rowAdapter, config);
    }

    /**
     * 将对象列表序列化到输出流
     *
     * @param outputStream 输出流
     * @param objects 对象列表
     * @param csvTypeToken 目标列表类型
     * @throws IOException 写入失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    public static <T> void serialize(@NotNull OutputStream outputStream, @NotNull List<T> objects, @NotNull CsvTypeToken<List<T>> csvTypeToken) throws IOException {
        serialize(outputStream, objects, csvTypeToken, CsvWriteConfig.defaultConfig());
    }

    /**
     * 将对象列表序列化到输出流
     *
     * @param outputStream 输出流
     * @param objects 对象列表
     * @param csvTypeToken 目标列表类型
     * @param config 写入配置
     * @throws IOException 写入失败时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> void serialize(@NotNull OutputStream outputStream, @NotNull List<T> objects, @NotNull CsvTypeToken<List<T>> csvTypeToken, @NotNull CsvWriteConfig config) throws IOException {
        serializeList(outputStream, objects, (CsvRowAdapter<T>) listElementAdapter(csvTypeToken), config);
    }

    /**
     * 将对象列表序列化到输出流
     *
     * <p> 元素类型从第一个元素的运行时类型推断，因此列表不能为空。</p>
     *
     * @param outputStream 输出流
     * @param objects 对象列表
     * @throws IOException 写入失败时抛出
     * @throws CsvMappingException 列表为空或类型映射失败时抛出
     */
    public static <T> void serialize(@NotNull OutputStream outputStream, @NotNull List<T> objects) throws IOException {
        serialize(outputStream, objects, CsvWriteConfig.defaultConfig());
    }

    /**
     * 将对象列表序列化到输出流
     *
     * <p> 元素类型从第一个元素的运行时类型推断，因此列表不能为空。</p>
     *
     * @param outputStream 输出流
     * @param objects 对象列表
     * @param config 写入配置
     * @throws IOException 写入失败时抛出
     * @throws CsvMappingException 列表为空或类型映射失败时抛出
     */
    public static <T> void serialize(@NotNull OutputStream outputStream, @NotNull List<T> objects, @NotNull CsvWriteConfig config) throws IOException {
        serializeList(outputStream, objects, inferElementAdapter(objects), config);
    }

    /**
     * 创建对象读取器
     * 
     * @param file CSV 文件
     * @param targetClass 目标类型
     * @throws IOException 文件无法读取时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> CsvObjectReader<T> objectReader(@NotNull File file, @NotNull Class<T> targetClass) throws IOException {
        return objectReader(file, targetClass, defaultParseConfig(CsvRowAdapters.get(targetClass)));
    }

    /**
     * 创建对象读取器
     *
     * @param file CSV 文件
     * @param targetClass 目标类型
     * @param config 解析配置
     * @throws IOException 文件无法读取时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> CsvObjectReader<T> objectReader(@NotNull File file, @NotNull Class<T> targetClass, @NotNull CsvConfig config) throws IOException {
        CsvRowAdapter<T> rowAdapter = CsvRowAdapters.get(targetClass);
        return new CsvObjectReader<>(reader(FileIO.inputStream(file), config), rowAdapter);
    }

    /**
     * 创建对象读取器
     * 
     * @param inputStream 输入流
     * @param targetClass 目标类型
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> CsvObjectReader<T> objectReader(@NotNull InputStream inputStream, @NotNull Class<T> targetClass) {
        return objectReader(inputStream, targetClass, defaultParseConfig(CsvRowAdapters.get(targetClass)));
    }

    /**
     * 创建对象读取器
     *
     * @param inputStream 输入流
     * @param targetClass 目标类型
     * @param config 解析配置
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> CsvObjectReader<T> objectReader(@NotNull InputStream inputStream, @NotNull Class<T> targetClass, @NotNull CsvConfig config) {
        return new CsvObjectReader<>(reader(inputStream, config), CsvRowAdapters.get(targetClass));
    }

    /**
     * 创建对象写入器
     *
     * @param file 目标文件
     * @param sourceClass 源对象类型
     * @throws IOException 文件无法创建时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> CsvObjectWriter<T> objectWriter(@NotNull File file, @NotNull Class<T> sourceClass) throws IOException {
        return objectWriter(file, sourceClass, CsvWriteConfig.defaultConfig());
    }

    /**
     * 创建对象写入器
     *
     * @param file 目标文件
     * @param sourceClass 源对象类型
     * @param config 写入配置
     * @throws IOException 文件无法创建时抛出
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> CsvObjectWriter<T> objectWriter(@NotNull File file, @NotNull Class<T> sourceClass, @NotNull CsvWriteConfig config) throws IOException {
        CsvRowAdapter<T> rowAdapter = CsvRowAdapters.get(sourceClass);
        return new CsvObjectWriter<>(writer(FileIO.outputStream(file), config), rowAdapter);
    }

    /**
     * 创建对象写入器
     *
     * @param outputStream 输出流
     * @param sourceClass 源对象类型
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> CsvObjectWriter<T> objectWriter(@NotNull OutputStream outputStream, @NotNull Class<T> sourceClass) {
        return objectWriter(outputStream, sourceClass, CsvWriteConfig.defaultConfig());
    }

    /**
     * 创建对象写入器
     *
     * @param outputStream 输出流
     * @param sourceClass 源对象类型
     * @param config 写入配置
     * @throws CsvMappingException 类型映射失败时抛出
     */
    @NotNull
    public static <T> CsvObjectWriter<T> objectWriter(@NotNull OutputStream outputStream, @NotNull Class<T> sourceClass, @NotNull CsvWriteConfig config) {
        return new CsvObjectWriter<>(writer(outputStream, config), CsvRowAdapters.get(sourceClass));
    }

    /**
     * 创建 CSV 读取器
     *
     * @param file CSV 文件
     * @throws IOException 文件无法读取时抛出
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
     * @throws IOException 文件无法读取时抛出
     */
    @NotNull
    public static CsvReader reader(@NotNull File file, @NotNull CsvConfig config) throws IOException {
        return reader(FileIO.inputStream(file), config);
    }

    /**
     * 创建 CSV 读取器
     *
     * @param inputStream 输入流
     */
    @NotNull
    public static CsvReader reader(@NotNull InputStream inputStream) {
        return reader(inputStream, CsvConfig.defaultConfig());
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
     * 创建 CSV 写入器
     *
     * @param file 目标文件
     * @throws IOException 文件无法创建时抛出
     */
    @NotNull
    public static CsvWriter writer(@NotNull File file) throws IOException {
        return writer(file, CsvWriteConfig.defaultConfig());
    }

    /**
     * 创建 CSV 写入器
     *
     * @param file 目标文件
     * @param config 写入配置
     * @throws IOException 文件无法创建时抛出
     */
    @NotNull
    public static CsvWriter writer(@NotNull File file, @NotNull CsvWriteConfig config) throws IOException {
        return writer(FileIO.outputStream(file), config);
    }

    /**
     * 创建 CSV 写入器
     *
     * @param outputStream 输出流
     */
    @NotNull
    public static CsvWriter writer(@NotNull OutputStream outputStream) {
        return writer(outputStream, CsvWriteConfig.defaultConfig());
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
     * 获取列表元素的行适配器
     *
     * @param csvTypeToken 列表类型令牌
     * @throws CsvMappingException 不是 List 或无法确定元素类型时抛出
     */
    @NotNull
    private static CsvRowAdapter<?> listElementAdapter(@NotNull CsvTypeToken<?> csvTypeToken) {
        Type elementType = csvTypeToken.getListElementType();
        if (elementType == null) {
            throw new CsvMappingException("Cannot resolve list element type from CsvTypeToken: " + csvTypeToken.getType());
        }
        return CsvRowAdapters.get(elementType);
    }

    /**
     * 按第一个元素的运行时类型获取行适配器
     *
     * @param objects 对象列表
     * @throws CsvMappingException 列表为空、首个元素为 null 或类型映射失败时抛出
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private static <T> CsvRowAdapter<T> inferElementAdapter(@NotNull List<T> objects) {
        if (objects.isEmpty()) {
            throw new CsvMappingException("Cannot serialize empty list: element type is unknown");
        }
        T first = objects.get(0);
        if (first == null) {
            throw new CsvMappingException("Cannot serialize list whose first element is null: element type is unknown");
        }
        return CsvRowAdapters.get((Class<T>) first.getClass());
    }

    /**
     * 创建默认解析配置
     *
     * <p> 根据行适配器是否提供表头决定是否跳过首行。</p>
     *
     * @param rowAdapter 行适配器
     * @return 默认解析配置
     */
    @NotNull
    private static CsvConfig defaultParseConfig(@NotNull CsvRowAdapter<?> rowAdapter) {
        return new CsvConfig.Builder<>().skipHeader(rowAdapter.header() != null).build();
    }

    /**
     * 解析输入流并关闭
     *
     * @param inputStream 输入流
     * @param rowAdapter 行适配器
     * @param config 解析配置
     * @throws IOException 读取失败时抛出
     */
    @NotNull
    private static <T> List<T> parseList(@NotNull InputStream inputStream, @NotNull CsvRowAdapter<T> rowAdapter, @NotNull CsvConfig config) throws IOException {
        try (CsvObjectReader<T> objectReader = new CsvObjectReader<>(reader(inputStream, config), rowAdapter)) {
            return objectReader.readAll();
        }
    }

    /**
     * 写入对象列表并关闭输出流
     *
     * @param outputStream 输出流
     * @param objects 对象列表
     * @param rowAdapter 行适配器
     * @param config 写入配置
     * @throws IOException 写入失败时抛出
     */
    private static <T> void serializeList(@NotNull OutputStream outputStream, @NotNull List<T> objects, @NotNull CsvRowAdapter<T> rowAdapter, @NotNull CsvWriteConfig config) throws IOException {
        try (CsvObjectWriter<T> objectWriter = new CsvObjectWriter<>(writer(outputStream, config), rowAdapter)) {
            // 行适配器如果提供了表头，则先写入表头
            objectWriter.writeHeader();
            objectWriter.writeAll(objects);
        }
    }
}