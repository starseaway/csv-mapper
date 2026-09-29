package com.xinyi.csvmapper.bind.adapter;

import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.mapper.CsvConverterRegistry;
import com.xinyi.csvmapper.mapper.CsvRowAdapter;
import com.xinyi.csvmapper.utils.Types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CSV 行适配器工厂
 *
 * <p> 根据行类型选择对应的 {@link CsvRowAdapter}，结果按类型缓存。</p>
 *
 * @author 新一
 * @date 2026/9/29 9:12
 */
public final class CsvRowAdapters {

    private CsvRowAdapters() { }

    /**
     * 自定义适配器表
     */
    private static final ConcurrentHashMap<Type, CsvRowAdapter<?>> sCustomAdapters = new ConcurrentHashMap<>();

    /**
     * 内置适配器缓存
     *
     * <p> key = 行类型；value = 行适配器。</p>
     */
    private static final ConcurrentHashMap<Type, CsvRowAdapter<?>> sAdapterCache = new ConcurrentHashMap<>();

    /**
     * 获取指定类型的行适配器
     *
     * @param rowType 行类型
     * @throws CsvMappingException 类型无法映射时抛出
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> CsvRowAdapter<T> get(@NotNull Class<T> rowType) {
        return (CsvRowAdapter<T>) get((Type) rowType);
    }

    /**
     * 获取指定类型的行适配器
     *
     * @param rowType 行类型，支持参数化类型（如 {@code List<Integer>}）
     * @throws CsvMappingException 类型无法映射时抛出
     */
    @NotNull
    public static CsvRowAdapter<?> get(@NotNull Type rowType) {
        Type type = Types.unwrapWildcard(rowType);
        CsvRowAdapter<?> custom = sCustomAdapters.get(type);
        if (custom != null) {
            return custom;
        }
        CsvRowAdapter<?> cached = sAdapterCache.get(type);
        if (cached != null) {
            return cached;
        }
        CsvRowAdapter<?> created = create(type);
        CsvRowAdapter<?> previous = sAdapterCache.putIfAbsent(type, created);
        return previous != null ? previous : created;
    }

    /**
     * 注册自定义行适配器
     *
     * <p> 自定义适配器的查找优先级，高于内置适配器。</p>
     *
     * @param rowType 行类型
     * @param adapter 行适配器
     */
    public static <T> void register(@NotNull Class<T> rowType, @NotNull CsvRowAdapter<T> adapter) {
        sCustomAdapters.put(rowType, adapter);
    }

    /**
     * 创建内置行适配器
     *
     * @param type 行类型
     * @throws CsvMappingException 类型不支持时抛出
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    private static CsvRowAdapter<?> create(@NotNull Type type) {
        Class<?> raw = Types.getRawType(type);
        if (raw.isArray()) {
            return new ArrayRowAdapter<>(raw);
        }
        if (Collection.class.isAssignableFrom(raw)) {
            return new CollectionRowAdapter(raw, collectionElementClass(type));
        }
        if (CsvConverterRegistry.isSupported(raw)) {
            return new ScalarRowAdapter<>(raw);
        }
        if (raw.isPrimitive() || Map.class.isAssignableFrom(raw)) {
            throw new CsvMappingException("Unsupported row type, register a CsvRowAdapter for it", raw);
        }
        return new ReflectiveRowAdapter<>(raw);
    }

    /**
     * 获取集合元素类型
     *
     * @param collectionType 集合类型
     * @return 元素类型，无法确定时返回 null
     */
    @Nullable
    private static Class<?> collectionElementClass(@NotNull Type collectionType) {
        Type typeArgument = Types.getTypeArgument(collectionType, 0);
        if (typeArgument == null) {
            return null;
        }
        Type elementType = Types.unwrapWildcard(typeArgument);
        if (elementType instanceof TypeVariable) {
            return null;
        }
        return Types.getRawType(elementType);
    }
}