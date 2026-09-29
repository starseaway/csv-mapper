package com.xinyi.csvmapper.bind.adapter;

import com.xinyi.csvmapper.annotation.CsvColumn;
import com.xinyi.csvmapper.annotation.CsvIgnore;
import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.mapper.CsvConverterRegistry;
import com.xinyi.csvmapper.mapper.CsvFieldMapper;
import com.xinyi.csvmapper.mapper.CsvRowAdapter;
import com.xinyi.csvmapper.mapper.NoOpMapper;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 注解对象行适配器
 *
 * <p> 根据 {@link CsvColumn} 和 {@link CsvIgnore} 将对象字段映射为 CSV 列。</p>
 *
 * <p> 字段顺序由 {@link CsvColumn#index()} 决定，未指定索引的字段追加在末尾。</p>
 *
 * @author 新一
 * @date 2026/9/29 10:01
 */
final class ReflectiveRowAdapter<T> implements CsvRowAdapter<T> {

    /**
     * 目标类型
     */
    private final Class<T> mTargetClass;

    /**
     * 字段绑定
     */
    private final List<FieldBinding> mBindings;

    /**
     * 表头列名
     */
    private final List<String> mHeader;

    /**
     * 构造函数
     *
     * @param targetClass 目标类型
     * @throws CsvMappingException 没有可映射字段或字段转换器无法创建时抛出
     */
    ReflectiveRowAdapter(@NotNull Class<T> targetClass) {
        this.mTargetClass = targetClass;
        this.mBindings = resolveBindings(targetClass);
        if (mBindings.isEmpty()) {
            throw new CsvMappingException("No mappable fields found", targetClass);
        }
        List<String> header = new ArrayList<>(mBindings.size());
        for (FieldBinding binding : mBindings) {
            header.add(binding.columnName);
        }
        this.mHeader = Collections.unmodifiableList(header);
    }

    @Nullable
    @Override
    public List<String> header() {
        return mHeader;
    }

    @NotNull
    @Override
    public List<String> toCells(@NotNull T value) {
        List<String> cells = new ArrayList<>(mBindings.size());
        for (FieldBinding binding : mBindings) {
            cells.add(readFieldValue(value, binding));
        }
        return cells;
    }

    @NotNull
    @Override
    public T map(@NotNull CsvRow row) {
        T instance = createInstance();
        for (FieldBinding binding : mBindings) {
            String rawValue = resolveRawValue(row, binding);
            Object convertedValue = convertValue(rawValue, binding, row.getLineNumber());
            setFieldValue(instance, binding.field, convertedValue, row.getLineNumber());
        }
        return instance;
    }

    /**
     * 读取字段值并应用列格式
     *
     * @param object 对象实例
     * @param binding 字段绑定
     * @return CSV 单元格文本
     * @throws CsvMappingException 字段读取失败时抛出
     */
    @NotNull
    private String readFieldValue(@NotNull T object, @NotNull FieldBinding binding) {
        try {
            Object value = binding.field.get(object);
            return applyColumnFormat(CsvCells.format(value), binding);
        } catch (IllegalAccessException exception) {
            throw new CsvMappingException("Cannot read field [" + binding.field.getName() + "]", mTargetClass, exception);
        }
    }

    /**
     * 应用列长度限制
     *
     * <p> 超过最大长度时截断原文本，并追加指定后缀。</p>
     *
     * @param raw 字段原始字符串值
     * @param binding 字段绑定
     */
    @NotNull
    private static String applyColumnFormat(@NotNull String raw, @NotNull FieldBinding binding) {
        if (binding.maxLength > 0 && raw.length() > binding.maxLength) {
            String suffix = binding.truncateSuffix;
            int cutLength = binding.maxLength - suffix.length();
            if (cutLength < 0) {
                cutLength = 0;
            }
            return raw.substring(0, cutLength) + suffix;
        }
        return raw;
    }

    /**
     * 定位字段对应的 CSV 单元格
     *
     * <p> 优先使用注解列名，其次使用列索引，最后使用字段名。</p>
     */
    @Nullable
    private static String resolveRawValue(@NotNull CsvRow row, @NotNull FieldBinding binding) {
        if (!binding.annotationName.isEmpty()) {
            return row.get(binding.annotationName);
        } else if (binding.columnIndex >= 0) {
            return row.get(binding.columnIndex);
        } else {
            return row.get(binding.field.getName());
        }
    }

    /**
     * 将单元格文本转换为字段类型
     *
     * @param rawValue 原始单元格文本
     * @param binding 字段绑定
     * @param lineNumber 行号
     * @throws CsvMappingException 转换失败时抛出
     */
    @Nullable
    private Object convertValue(@Nullable String rawValue, @NotNull FieldBinding binding, int lineNumber) {
        Class<?> type = binding.mapperClass != null ? binding.mapperClass : binding.field.getType();
        try {
            return CsvConverterRegistry.convert(type, rawValue);
        } catch (Exception exception) {
            throw new CsvMappingException("Line " + lineNumber + ", field [" + binding.field.getName()
                    + "] cannot convert \"" + rawValue + "\" to " + type.getSimpleName(), mTargetClass, exception);
        }
    }

    /**
     * 通过反射创建目标对象实例
     *
     * @throws CsvMappingException 对象类型没有无参构造函数时抛出
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
     * <p> 反射写入，值为 null 时跳过。</p>
     *
     * @param instance 对象实例
     * @param field 字段反射对象
     * @param value 字段值
     * @param lineNumber 行号
     * @throws CsvMappingException 字段写入失败时抛出
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
     * 解析字段绑定
     *
     * <p> 遍历目标类及其父类的实例字段，跳过静态字段、合成字段、{@link CsvIgnore} 标记的字段。</p>
     *
     * <p> 带 {@link CsvColumn#index()} 的字段按索引排序，其余字段追加在末尾。</p>
     *
     * @param targetClass 目标类型
     * @return 字段绑定列表
     */
    @NotNull
    private static List<FieldBinding> resolveBindings(@NotNull Class<?> targetClass) {
        List<FieldBinding> indexed = new ArrayList<>();
        List<FieldBinding> unindexed = new ArrayList<>();

        Class<?> current = targetClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                boolean isStatic = Modifier.isStatic(field.getModifiers());
                boolean isSynthetic = field.isSynthetic();
                boolean isIgnored = field.isAnnotationPresent(CsvIgnore.class);
                if (isStatic || isSynthetic || isIgnored) {
                    continue;
                }
                field.setAccessible(true);
                FieldBinding binding = buildBinding(field);
                if (binding.columnIndex >= 0) {
                    indexed.add(binding);
                } else {
                    unindexed.add(binding);
                }
            }
            current = current.getSuperclass();
        }

        Collections.sort(indexed, (left, right) ->
                Integer.compare(left.columnIndex, right.columnIndex));

        List<FieldBinding> result = new ArrayList<>(indexed.size() + unindexed.size());
        result.addAll(indexed);
        result.addAll(unindexed);

        return Collections.unmodifiableList(result);
    }

    /**
     * 构建单个字段的绑定信息
     *
     * @param field 字段
     * @throws CsvMappingException 字段转换器无法注册时抛出
     */
    @NotNull
    private static FieldBinding buildBinding(@NotNull Field field) {
        FieldBinding binding = new FieldBinding();
        binding.field = field;

        CsvColumn annotation = field.getAnnotation(CsvColumn.class);
        if (annotation == null) {
            binding.annotationName = "";
            binding.columnName = field.getName();
            binding.columnIndex = -1;
            return binding;
        }

        binding.annotationName = annotation.name();
        binding.columnName = annotation.name().isEmpty() ? field.getName() : annotation.name();
        binding.columnIndex = annotation.index();
        binding.maxLength = annotation.maxLength();
        binding.truncateSuffix = annotation.truncateSuffix();

        Class<? extends CsvFieldMapper<?>> mapperClass = annotation.mapper();
        if (mapperClass != NoOpMapper.class) {
            binding.mapperClass = mapperClass;
            try {
                CsvConverterRegistry.register(mapperClass);
            } catch (Exception exception) {
                throw new CsvMappingException("Cannot instantiate CsvFieldMapper: "
                        + mapperClass.getSimpleName(), field.getDeclaringClass(), exception);
            }
        }
        return binding;
    }

    /**
     * 字段绑定信息
     *
     * <p> 缓存单个字段的读写映射信息，避免每行重复解析注解。</p>
     */
    private static final class FieldBinding {

        /// 字段
        Field field;

        /// 注解中的列名，未配置时为空
        String annotationName;

        /// CSV 列名
        String columnName;

        /// CSV 列索引，未指定时为 -1
        int columnIndex;

        /// 最大长度，0 表示不限制
        int maxLength;

        /// 超长时追加的后缀
        String truncateSuffix = "";

        /// 字段转换器类型，未配置时为 null
        Class<? extends CsvFieldMapper<?>> mapperClass;
    }
}
