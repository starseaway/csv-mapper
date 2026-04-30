package com.xinyi.csvmapper.bind;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
 * @author 新一
 * @date 2026/4/23 18:23
 */
public abstract class CsvTypeToken<T> {

    /**
     * 运行时保留的完整泛型类型
     */
    private final Type mType;

    /**
     * 若泛型参数为具体类（非参数化类型），则直接持有该 Class
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
        // 通过匿名子类的父类泛型参数拿到完整 Type
        Type superClass = getClass().getGenericSuperclass();

        if (!(superClass instanceof ParameterizedType)) {
            throw new IllegalStateException("CsvTypeToken must use anonymous subclass: new CsvTypeToken<>(){}");
        }
        ParameterizedType parameterized = (ParameterizedType) superClass;
        if (parameterized.getRawType() != CsvTypeToken.class) {
            throw new IllegalStateException("CsvTypeToken must be directly subclassed");
        }

        this.mType = parameterized.getActualTypeArguments()[0];
        // 提取 rawClass
        if (mType instanceof Class) {
            this.mRawClass = (Class<T>) mType;
        } else if (mType instanceof ParameterizedType) {
            this.mRawClass = (Class<T>) ((ParameterizedType) mType).getRawType();
        } else {
            this.mRawClass = null;
        }
    }

    /**
     * 获取完整的泛型 Type（包含泛型参数信息）
     */
    public Type getType() {
        return mType;
    }

    /**
     * 获取泛型擦除后的原始 Class
     *
     * <p> 例如 {@code CsvType<List<User>>} 的 rawClass 为 {@code List.class} </p>
     */
    public Class<T> getRawClass() {
        return mRawClass;
    }

    /**
     * 判断目标类型是否为 {@link List}
     */
    public boolean isList() {
        if (!(mType instanceof ParameterizedType)) {
            return false;
        }
        Type rawType = ((ParameterizedType) mType).getRawType();
        return rawType == List.class || rawType == ArrayList.class;
    }

    /**
     * 若目标类型为 {@code List<E>}，返回元素类型 E 的 Class
     *
     * @return 列表元素类型，若不是 List 或元素类型非具体 Class 则返回 null
     */
    public Class<?> getListElementClass() {
        if (!isList()) {
            return null;
        }
        Type[] typeArgs = ((ParameterizedType) mType).getActualTypeArguments();
        if (typeArgs.length == 0) {
            return null;
        }
        Type elementType = typeArgs[0];
        if (elementType instanceof Class) {
            return (Class<?>) elementType;
        }
        return null;
    }
}