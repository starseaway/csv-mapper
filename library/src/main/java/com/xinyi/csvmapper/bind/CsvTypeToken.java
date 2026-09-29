package com.xinyi.csvmapper.bind;

import com.xinyi.csvmapper.utils.Types;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 泛型类型令牌
 *
 * <p> 用于在运行时保留泛型类型信息，解决 Java 泛型擦除问题 </p>
 *
 * <p>
 *   必须以匿名子类的方式使用（{@code new CsvTypeToken<List<Model>>(){ }}），
 *   这样 JVM 才能在运行时通过 {@code getGenericSuperclass()} 拿到完整的泛型参数。
 * </p>
 *
 * <p>
 *   设计思路参考：
 *   <li> Gson - {@code com.google.gson.reflect.TypeToken} </li>
 *   <li> FastJson - {@code com.alibaba.fastjson2.TypeReference} </li>
 * </p>
 *
 * @param <T> 目标类型
 *
 * @author 新一
 * @date 2026/4/23 18:23
 */
public abstract class CsvTypeToken<T> {

    /**
     * 运行时保留的完整泛型类型
     */
    private final Type mType;

    /**
     * 原始类型
     */
    private final Class<T> mRawClass;

    /**
     * 构造函数
     *
     * <p> 必须以匿名子类方式调用：{@code new CsvTypeToken<List<Model>>(){ }}</p>
     *
     * @throws IllegalStateException 非匿名子类方式使用时抛出
     */
    @SuppressWarnings("unchecked")
    protected CsvTypeToken() {
        Type superClass = getClass().getGenericSuperclass();

        if (!(superClass instanceof ParameterizedType)) {
            throw new IllegalStateException("CsvTypeToken must use anonymous subclass: new CsvTypeToken<>(){}");
        }
        ParameterizedType parameterized = (ParameterizedType) superClass;
        if (parameterized.getRawType() != CsvTypeToken.class) {
            throw new IllegalStateException("CsvTypeToken must be directly subclassed");
        }

        this.mType = parameterized.getActualTypeArguments()[0];
        this.mRawClass = mType instanceof TypeVariable ? null : (Class<T>) Types.getRawType(mType);
    }

    /**
     * 获取完整类型
     *
     * @return 包含泛型参数的运行时类型
     */
    public Type getType() {
        return mType;
    }

    /**
     * 获取原始类型
     *
     * @return 类型对应的原始 Class，无法确定时返回 null
     */
    @Nullable
    public Class<T> getRawClass() {
        return mRawClass;
    }

    /**
     * 判断目标类型是否为 List
     *
     * @return 类型为 {@link List} 或 {@link ArrayList} 时返回 true
     */
    public boolean isList() {
        if (!(mType instanceof ParameterizedType)) {
            return false;
        }
        Type rawType = ((ParameterizedType) mType).getRawType();
        return rawType == List.class || rawType == ArrayList.class;
    }

    /**
     * 获取 List 元素类型
     *
     * <p> 返回完整 {@link Type}，保留元素类型中的泛型参数。</p>
     *
     * @return 元素类型，不是 List 或无法确定元素类型时返回 null
     */
    public Type getListElementType() {
        if (!isList()) {
            return null;
        }
        Type typeArgument = Types.getTypeArgument(mType, 0);
        if (typeArgument == null) {
            return null;
        }
        Type elementType = Types.unwrapWildcard(typeArgument);
        if (elementType instanceof TypeVariable) {
            return null;
        }
        return elementType;
    }

    /**
     * 获取 List 元素的原始类型
     *
     * @return 元素类型对应的 Class，不是 List 或无法确定时返回 null
     */
    public Class<?> getListElementClass() {
        Type elementType = getListElementType();
        return elementType == null ? null : Types.getRawType(elementType);
    }
}