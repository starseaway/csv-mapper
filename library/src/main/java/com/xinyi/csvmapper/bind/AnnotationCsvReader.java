package com.xinyi.csvmapper.bind;

import com.xinyi.csvmapper.annotation.CsvColumn;
import com.xinyi.csvmapper.annotation.CsvIgnore;
import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.mapper.CsvConverterRegistry;
import com.xinyi.csvmapper.mapper.CsvFieldMapper;
import com.xinyi.csvmapper.mapper.NoOpMapper;
import com.xinyi.csvmapper.buffered.reader.CsvReader;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解驱动的 CSV 对象读取器
 *
 * <p> 基于 {@link CsvColumn} 和 {@link CsvIgnore} 注解，将 CSV 行数据自动映射为 Java 对象。</p>
 *
 * @param <T> 目标对象类型
 *
 * @author 新一
 * @date 2026/4/23 13:32
 */
public class AnnotationCsvReader<T> implements Closeable {

    /**
     * 字段元数据缓存
     *
     * <p> key = 目标类型，value = 字段元数据列表 </p>
     */
    private static final ConcurrentHashMap<Class<?>, List<FieldMeta>> sFieldMetaCache = new ConcurrentHashMap<>();

    /**
     * 底层 CSV 读取器，负责实际的 IO 读取
     */
    private final CsvReader mCsvReader;

    /**
     * 目标对象的 Class 类型，用于反射创建实例和异常信息
     */
    private final Class<T> mTargetClass;

    /**
     * 当前类型解析后的字段元数据列表，从缓存中获取
     */
    private final List<FieldMeta> mFieldMetas;

    /**
     * 构造函数
     *
     * @param csvReader 底层 CSV 读取器（需配置 skipHeader = true 以启用列名映射）
     * @param targetClass 目标对象类型（需有 public 无参构造函数）
     */
    public AnnotationCsvReader(@NotNull CsvReader csvReader, @NotNull Class<T> targetClass) {
        this.mCsvReader = csvReader;
        this.mTargetClass = targetClass;
        this.mFieldMetas = resolveFieldMetas(targetClass);
    }

    /**
     * 读取下一行并映射为目标对象
     *
     * @return 映射后的对象，到达文件末尾时返回 null
     * @throws IOException 读取过程中发生 IO 错误
     * @throws CsvMappingException 映射过程中发生类型转换错误
     */
    @Nullable
    public T readNext() throws IOException {
        CsvRow row = mCsvReader.readNextRow();
        if (row == null) {
            return null;
        }
        return mapRowToObject(row);
    }

    /**
     * 读取所有行并映射为目标对象列表
     *
     * @return 对象列表，文件为空时返回空列表
     * @throws IOException 读取过程中发生 IO 错误
     * @throws CsvMappingException 映射过程中发生类型转换错误
     */
    @NotNull
    public List<T> readAll() throws IOException {
        List<T> result = new ArrayList<>();
        T item;
        while ((item = readNext()) != null) {
            result.add(item);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        mCsvReader.close();
    }

    /**
     * 将一行 CSV 数据映射为目标对象
     *
     * @param row 当前行数据
     * @return 映射后的目标对象实例
     */
    @NotNull
    private T mapRowToObject(@NotNull CsvRow row) {
        T instance = createInstance();
        for (FieldMeta meta : mFieldMetas) {
            String rawValue = resolveFieldValue(row, meta);
            Object convertedValue = convertValue(rawValue, meta, row.getLineNumber());
            setFieldValue(instance, meta.field, convertedValue, row.getLineNumber());
        }
        return instance;
    }

    /**
     * 从行数据中解析字段原始值
     *
     * <p> 优先按列名查找，其次按列索引查找，最后按字段名匹配列名 </p>
     */
    @Nullable
    private String resolveFieldValue(@NotNull CsvRow row, @NotNull FieldMeta meta) {
        if (meta.columnName != null && !meta.columnName.isEmpty()) {
            return row.get(meta.columnName);
        } else if (meta.columnIndex >= 0) {
            return row.get(meta.columnIndex);
        } else {
            return row.get(meta.field.getName());
        }
    }

    /**
     * 将原始字符串值转换为字段目标类型
     *
     * <p> 优先使用自定义 {@link CsvFieldMapper}，其次使用内置类型转换规则 </p>
     *
     * @param rawValue 字段原始字符串值，可为 null
     * @param meta 字段元数据
     * @param lineNumber 当前行号，用于异常信息
     * @return 转换后的目标类型值，无法转换时返回 null
     */
    @Nullable
    private Object convertValue(@Nullable String rawValue, @NotNull FieldMeta meta, int lineNumber) {
        @NotNull Class<?> type;
        if (meta.mapperClass != null) {
            type = meta.mapperClass;
        } else {
            type = meta.field.getType();
        }
        try {
            return CsvConverterRegistry.convert(type, rawValue);
        } catch (Exception exception) {
            throw new CsvMappingException("Line " + lineNumber + ", field [" + meta.field.getName()
                    + "] cannot convert \"" + rawValue + "\" to " + type.getSimpleName(), mTargetClass, exception);
        }
    }

    /**
     * 通过反射创建目标类型的实例
     *
     * @return 新创建的目标对象实例
     * @throws CsvMappingException 目标类没有 public 无参构造函数时抛出
     */
    @NotNull
    private T createInstance() {
        try {
            return mTargetClass.getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            throw new CsvMappingException("Cannot instantiate class, ensure it has a public no-arg constructor",
                    mTargetClass, exception);
        }
    }

    /**
     * 通过反射将值写入目标对象的字段
     *
     * @param instance 目标对象实例
     * @param field 目标字段
     * @param value 要写入的值，为 null 时跳过
     * @param lineNumber 当前行号，用于异常信息
     */
    private void setFieldValue(@NotNull Object instance, @NotNull Field field, @Nullable Object value, int lineNumber) {
        if (value == null) {
            return;
        }
        try {
            field.set(instance, value);
        } catch (IllegalAccessException exception) {
            throw new CsvMappingException("Line " + lineNumber + ", cannot set field ["
                    + field.getName() + "]", mTargetClass, exception);
        }
    }

    /**
     * 解析目标类型的字段元数据，优先从缓存中读取
     *
     * <p> 遍历目标类及其所有父类的字段，跳过标注了 {@link CsvIgnore} 的字段 </p>
     *
     * @param targetClass 目标类型
     * @return 字段元数据列表
     */
    @NotNull
    private static <E> List<FieldMeta> resolveFieldMetas(@NotNull Class<E> targetClass) {
        List<FieldMeta> cached = sFieldMetaCache.get(targetClass);
        if (cached != null) {
            return cached;
        }

        List<FieldMeta> metas = new ArrayList<>();
        Class<?> current = targetClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvIgnore.class)) {
                    continue;
                }
                field.setAccessible(true);
                metas.add(buildFieldMeta(field));
            }
            current = current.getSuperclass();
        }

        sFieldMetaCache.put(targetClass, metas);
        return metas;
    }

    /**
     * 构建单个字段的元数据
     *
     * <p> 读取字段上的 {@link CsvColumn} 注解，提取列名、列索引、格式属性及自定义 mapper </p>
     *
     * @param field 目标字段
     * @return 构建好的字段元数据
     */
    @NotNull
    private static FieldMeta buildFieldMeta(@NotNull Field field) {
        FieldMeta meta = new FieldMeta();
        meta.field = field;

        CsvColumn annotation = field.getAnnotation(CsvColumn.class);
        if (annotation != null) {
            meta.columnName = annotation.name();
            meta.columnIndex = annotation.index();

            Class<? extends CsvFieldMapper<?>> mapperClass = annotation.mapper();
            boolean isDefaultMapper = mapperClass == NoOpMapper.class;
            if (!isDefaultMapper) {
                meta.mapperClass = mapperClass;
                try {
                    CsvConverterRegistry.register(mapperClass);
                } catch (Exception exception) {
                    throw new CsvMappingException("Cannot instantiate CsvFieldMapper: "
                            + mapperClass.getSimpleName(), field.getDeclaringClass(), exception);
                }
            }
        } else {
            meta.columnName = "";
            meta.columnIndex = -1;
        }

        return meta;
    }

    /**
     * 字段映射元数据
     *
     * <p> 缓存单个字段的所有映射信息，避免每次读取时重复解析注解 </p>
     */
    private static class FieldMeta {

        /** 目标字段的反射对象 */
        Field field;

        /** 绑定的列名，来自 {@link CsvColumn#name()}，未配置时为空字符串 */
        String columnName;

        /** 绑定的列索引，来自 {@link CsvColumn#index()}，未配置时为 -1 */
        int columnIndex = -1;

        /** 自定义字段类型转换器，未配置时为 null */
        Class<? extends CsvFieldMapper<?>> mapperClass;
    }
}