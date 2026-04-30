package com.xinyi.csvmapper.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CSV 字段级类型转换器注册中心（线程安全）
 *
 * <p> 复用 {@link CsvFieldMapper} 作为唯一转换抽象，避免创建重复接口 </p>
 *
 * @author 新一
 * @date 2026/4/28 17:02
 */
public final class CsvConverterRegistry {

    private CsvConverterRegistry() { }

    /**
     * 内置类型转换器表
     *
     * <p> 不可变，类加载时初始化，读取零锁开销 </p>
     */
    private static final Map<Class<?>, CsvFieldMapper<?>> sBuiltinConverters;

    /**
     * 自定义类型转换器表
     */
    private static final ConcurrentHashMap<Class<?>, CsvFieldMapper<?>> sCustomConverters = new ConcurrentHashMap<>();

    static {
        Map<Class<?>, CsvFieldMapper<?>> map = new HashMap<>();

        // String
        map.put(String.class, raw -> raw);

        // Byte
        CsvFieldMapper<Byte> byteMapper = raw -> Byte.parseByte(raw.trim());
        map.put(Byte.class, byteMapper);
        map.put(byte.class, byteMapper);

        // Short
        CsvFieldMapper<Short> shortMapper = raw -> Short.parseShort(raw.trim());
        map.put(Short.class, shortMapper);
        map.put(short.class, shortMapper);

        // Integer
        CsvFieldMapper<Integer> intMapper = raw -> Integer.parseInt(raw.trim());
        map.put(Integer.class, intMapper);
        map.put(int.class, intMapper);

        // Long
        CsvFieldMapper<Long> longMapper = raw -> Long.parseLong(raw.trim());
        map.put(Long.class, longMapper);
        map.put(long.class, longMapper);

        // Double
        CsvFieldMapper<Double> doubleMapper = raw -> Double.parseDouble(raw.trim());
        map.put(Double.class, doubleMapper);
        map.put(double.class, doubleMapper);

        // Float
        CsvFieldMapper<Float> floatMapper = raw -> Float.parseFloat(raw.trim());
        map.put(Float.class, floatMapper);
        map.put(float.class, floatMapper);

        // Character
        CsvFieldMapper<Character> charMapper = raw -> raw.charAt(0);
        map.put(Character.class, charMapper);
        map.put(char.class, charMapper);

        // Boolean
        CsvFieldMapper<Boolean> booleanMapper = raw -> Boolean.parseBoolean(raw.trim());
        map.put(Boolean.class, booleanMapper);
        map.put(boolean.class, booleanMapper);

        sBuiltinConverters = Collections.unmodifiableMap(map);
    }

    /**
     * 执行类型转换
     *
     * <p> 查找顺序：自定义转换器优先，其次内置转换器 </p>
     *
     * @param type 目标字段类型
     * @param rawValue 原始字符串值
     *
     * @throws NumberFormatException 数值解析失败时抛出
     * @throws IllegalArgumentException char 类型长度非法时抛出
     */
    @Nullable
    public static Object convert(@NotNull Class<?> type, @Nullable String rawValue) {
        // 自定义表优先
        CsvFieldMapper<?> mapper = sCustomConverters.get(type);
        // 获取内置类型转换器
        if (mapper == null) {
            mapper = sBuiltinConverters.get(type);
        }
        if (mapper == null) {
            return null;
        }
        if (rawValue == null || rawValue.isEmpty()) {
            return defaultValue(type);
        }
        return mapper.convert(rawValue);
    }

    /**
     * 获取类型默认值（基本类型返回零值，引用类型返回 null）
     *
     * <p> 用于处理 CSV 空字段，避免基本类型出现空指针异常 </p>
     *
     * @param type 字段类型
     */
    @Nullable
    public static Object defaultValue(@NotNull Class<?> type) {
        if (type == byte.class || type == Byte.class) {
            return 0;
        }
        if (type == short.class || type == Short.class) {
            return 0;
        }
        if (type == int.class || type == Integer.class) {
            return 0;
        }
        if (type == long.class || type == Long.class) {
            return 0L;
        }
        if (type == double.class || type == Double.class) {
            return 0.0D;
        }
        if (type == float.class || type == Float.class) {
            return 0.0F;
        }
        if (type == char.class || type == Character.class) {
            return '\0';
        }
        if (type == boolean.class || type == Boolean.class) {
            return false;
        }
        return null;
    }

    /**
     * 注册自定义类型转换器
     *
     * @param type {@link CsvFieldMapper} 的 Class 对象
     * @throws Exception 实例化失败时抛出
     */
    public static void register(@NotNull Class<? extends CsvFieldMapper<?>> type) throws Exception {
        if (sCustomConverters.containsKey(type)) {
            return;
        }
        sCustomConverters.putIfAbsent(type, type.getDeclaredConstructor().newInstance());
    }
}