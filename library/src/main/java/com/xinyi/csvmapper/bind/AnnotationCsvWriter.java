package com.xinyi.csvmapper.bind;

import com.xinyi.csvmapper.annotation.CsvColumn;
import com.xinyi.csvmapper.annotation.CsvIgnore;
import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.buffered.writer.CsvWriter;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解驱动的 CSV 对象写入器
 *
 * <p> 基于 {@link CsvColumn} 和 {@link CsvIgnore} 注解，将 Java 对象自动序列化为 CSV 行 </p>
 *
 * <p> 字段写入顺序：优先按 {@link CsvColumn#index()} 升序排列，未指定 index 的字段追加在末尾 </p>
 *
 * @param <T> 源对象类型
 *
 * @author 新一
 * @date 2026/4/23 15:23
 */
public class AnnotationCsvWriter<T> implements Closeable {

    /**
     * 字段元数据缓存，key = 源类型
     */
    private static final ConcurrentHashMap<Class<?>, List<WriteFieldMeta>> sFieldMetaCache = new ConcurrentHashMap<>();

    /**
     * 底层 CSV 写入器，负责实际的 IO 写入
     */
    private final CsvWriter mCsvWriter;

    /**
     * 源对象的 Class 类型，用于反射读取字段和异常信息
     */
    private final Class<T> mSourceClass;

    /**
     * 当前类型解析后的字段元数据列表（已按列索引排序），从缓存中获取
     */
    private final List<WriteFieldMeta> mFieldMetas;

    /**
     * 构造函数
     *
     * @param csvWriter 底层 CSV 写入器
     * @param sourceClass 源对象类型
     */
    public AnnotationCsvWriter(@NotNull CsvWriter csvWriter, @NotNull Class<T> sourceClass) {
        this.mCsvWriter = csvWriter;
        this.mSourceClass = sourceClass;
        this.mFieldMetas = resolveFieldMetas(sourceClass);
    }

    /**
     * 写入表头行
     *
     * <p> 列名来源：优先使用 {@link CsvColumn#name()}，其次使用字段名 </p>
     *
     * @throws IOException 写入过程中发生 IO 错误
     */
    public void writeHeader() throws IOException {
        List<String> headerNames = new ArrayList<>(mFieldMetas.size());
        for (WriteFieldMeta meta : mFieldMetas) {
            headerNames.add(meta.columnName);
        }
        mCsvWriter.writeHeader(headerNames);
    }

    /**
     * 将单个对象序列化为一行 CSV 并写入
     *
     * @param object 源对象
     * @throws IOException 写入过程中发生 IO 错误
     * @throws CsvMappingException 字段读取失败时抛出
     */
    public void writeObject(@NotNull T object) throws IOException {
        List<String> fields = new ArrayList<>(mFieldMetas.size());
        for (WriteFieldMeta meta : mFieldMetas) {
            fields.add(readFieldValue(object, meta));
        }
        mCsvWriter.writeRow(fields);
    }

    /**
     * 批量写入对象列表
     *
     * @param objects 源对象列表
     * @throws IOException 写入过程中发生 IO 错误
     */
    public void writeAll(@NotNull List<T> objects) throws IOException {
        for (T object : objects) {
            writeObject(object);
        }
    }

    @Override
    public void close() throws IOException {
        mCsvWriter.close();
    }

    /**
     * 通过反射读取字段值，并应用列格式规则后返回字符串
     *
     * @param object 源对象实例
     * @param meta 字段元数据（含格式配置）
     * @return 格式化后的字段字符串值
     * @throws CsvMappingException 字段无法访问时抛出
     */
    @NotNull
    private String readFieldValue(@NotNull T object, @NotNull WriteFieldMeta meta) {
        try {
            Object value = meta.field.get(object);
            String raw = value == null ? "" : value.toString();
            return applyColumnFormat(raw, meta);
        } catch (IllegalAccessException exception) {
            throw new CsvMappingException("Cannot read field [" + meta.field.getName() + "]", mSourceClass, exception);
        }
    }

    /**
     * 对字段值应用列格式规则
     *
     * @param raw  字段原始字符串值
     * @param meta 字段元数据（含格式配置）
     * @return 格式化后的字段值
     */
    @NotNull
    private static String applyColumnFormat(@NotNull String raw, @NotNull WriteFieldMeta meta) {
        if (meta.maxLength > 0 && raw.length() > meta.maxLength) {
            String suffix = meta.truncateSuffix;
            int cutLength = meta.maxLength - suffix.length();
            if (cutLength < 0) cutLength = 0;
            return raw.substring(0, cutLength) + suffix;
        }
        return raw;
    }

    /**
     * 解析源类型的字段元数据，优先从缓存中读取
     *
     * <p> 遍历源类及其所有父类的字段，跳过标注了 {@link CsvIgnore} 的字段 </p>
     * <p> 有 {@link CsvColumn#index()} 的字段按索引升序排列，其余追加在末尾 </p>
     *
     * @param sourceClass 源对象类型
     * @return 排序后的字段元数据列表
     */
    @NotNull
    private static <E> List<WriteFieldMeta> resolveFieldMetas(@NotNull Class<E> sourceClass) {
        List<WriteFieldMeta> cached = sFieldMetaCache.get(sourceClass);
        if (cached != null) {
            return cached;
        }

        List<WriteFieldMeta> indexedMetas = new ArrayList<>();
        List<WriteFieldMeta> unindexedMetas = new ArrayList<>();

        Class<?> current = sourceClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvIgnore.class)) {
                    continue;
                }
                field.setAccessible(true);
                WriteFieldMeta meta = buildWriteFieldMeta(field);
                if (meta.columnIndex >= 0) {
                    indexedMetas.add(meta);
                } else {
                    unindexedMetas.add(meta);
                }
            }
            current = current.getSuperclass();
        }

        Collections.sort(indexedMetas, (left, right) ->
                Integer.compare(left.columnIndex, right.columnIndex));

        List<WriteFieldMeta> result = new ArrayList<>(indexedMetas.size() + unindexedMetas.size());
        result.addAll(indexedMetas);
        result.addAll(unindexedMetas);

        sFieldMetaCache.put(sourceClass, result);
        return result;
    }

    /**
     * 构建单个字段的写入元数据
     *
     * <p> 读取字段上的 {@link CsvColumn} 注解，提取所有属性 </p>
     *
     * @param field 源字段
     * @return 构建好的写入字段元数据
     */
    @NotNull
    private static WriteFieldMeta buildWriteFieldMeta(@NotNull Field field) {
        WriteFieldMeta meta = new WriteFieldMeta();
        meta.field = field;

        CsvColumn annotation = field.getAnnotation(CsvColumn.class);
        if (annotation != null && !annotation.name().isEmpty()) {
            meta.columnName = annotation.name();
        } else {
            meta.columnName = field.getName();
        }
        meta.columnIndex = (annotation != null) ? annotation.index() : -1;

        // 读取列格式属性
        if (annotation != null) {
            meta.maxLength = annotation.maxLength();
            meta.truncateSuffix = annotation.truncateSuffix();
        }

        return meta;
    }

    /**
     * 写入字段元数据
     *
     * <p> 缓存单个字段的所有写入信息，避免每次写入时重复解析注解 </p>
     */
    private static class WriteFieldMeta {

        /// 源字段的反射对象
        Field field;

        /// 写入 CSV 时使用的列名，来自 {@link CsvColumn#name()}
        String columnName;

        /// 列索引，来自 {@link CsvColumn#index()}
        int columnIndex;

        /// 最大长度，来自 {@link CsvColumn#maxLength()}，0 表示不限制
        int maxLength;

        /// 截断后缀，来自 {@link CsvColumn#truncateSuffix()}，超出 maxLength 时追加
        String truncateSuffix;
    }
}