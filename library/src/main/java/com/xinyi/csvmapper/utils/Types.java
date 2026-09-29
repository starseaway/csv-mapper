package com.xinyi.csvmapper.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Java {@link Type} 类型处理工具
 *
 * <p> 提供泛型类型擦除、通配符解析和参数类型获取等基础操作。</p>
 *
 * @author 新一
 * @date 2026/9/29 14:50
 */
public final class Types {

    private Types() { }

    /**
     * 获取 {@link Type} 对应的原始 Class
     *
     * <p> 支持普通类型、参数化类型、泛型数组、通配符和类型变量。</p>
     *
     * @param type 类型
     * @return 类型对应的原始 Class
     * @throws IllegalArgumentException 不支持的 Type 实现时抛出
     */
    @NotNull
    public static Class<?> getRawType(@NotNull Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if (type instanceof GenericArrayType) {
            Class<?> componentClass = getRawType(((GenericArrayType) type).getGenericComponentType());
            return Array.newInstance(componentClass, 0).getClass();
        }
        if (type instanceof WildcardType) {
            return getRawType(unwrapWildcard(type));
        }
        if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return bounds.length > 0 ? getRawType(bounds[0]) : Object.class;
        }
        throw new IllegalArgumentException("Unknown Type implementation: " + type.getClass().getName());
    }

    /**
     * 解析通配符类型的上界
     *
     * <p>
     *   对于 {@code ? extends T} 返回 {@code T}，
     *   对于 {@code ?} 和 {@code ? super T} 返回 {@link Object}，
     *   其他类型直接返回自身。
     * </p>
     *
     * @param type 类型
     * @return 通配符上界或原类型
     */
    @NotNull
    public static Type unwrapWildcard(@NotNull Type type) {
        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            return upperBounds.length > 0 ? upperBounds[0] : Object.class;
        }
        return type;
    }

    /**
     * 获取参数化类型的指定类型参数
     *
     * <p>
     *   例如 {@code Map<String, Integer>}：
     *   <li> 第 0 个参数为 {@code String} </li>
     *   <li> 第 1 个参数为 {@code Integer} </li>
     * </p>
     *
     * @param type 参数化类型
     * @param index 参数索引，从 0 开始
     * @return 类型参数，不是参数化类型或索引无效时返回 null
     */
    @Nullable
    public static Type getTypeArgument(@NotNull Type type, int index) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
        if (index < 0 || index >= typeArgs.length) {
            return null;
        }
        return typeArgs[index];
    }
}