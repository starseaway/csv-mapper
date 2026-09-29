package com.xinyi.csvmapper.bind.adapter;

import com.xinyi.csvmapper.exception.CsvMappingException;
import com.xinyi.csvmapper.mapper.CsvRowAdapter;
import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 集合行适配器
 *
 * <p> 集合的每个元素对应一个 CSV 单元格。</p>
 *
 * @param <T> 集合类型
 *
 * @author 新一
 * @date 2026/9/29 9:39
 */
final class CollectionRowAdapter<T> implements CsvRowAdapter<T> {

    /**
     * 集合类型
     */
    private final Class<T> mCollectionClass;

    /**
     * 集合元素类型
     */
    private final Class<?> mElementType;

    /**
     * 构造函数
     *
     * @param collectionClass 集合类型
     * @param elementType 元素类型，无法确定时使用 String
     * @throws CsvMappingException 元素类型不支持或集合无法实例化时抛出
     */
    CollectionRowAdapter(@NotNull Class<T> collectionClass, @Nullable Class<?> elementType) {
        this.mCollectionClass = collectionClass;

        if (elementType == null || elementType == Object.class) {
            // 无法确定元素类型时使用 String
            this.mElementType = String.class;
        } else {
            this.mElementType = CsvCells.requireSupported(elementType, collectionClass);
        }

        // 提前校验，避免读取到一半才发现无法实例化
        newCollection(0);
    }

    @Nullable
    @Override
    public List<String> header() {
        return null;
    }

    @NotNull
    @Override
    public List<String> toCells(@NotNull T value) {
        Collection<?> collection = (Collection<?>) value;
        List<String> cells = new ArrayList<>(collection.size());
        for (Object element : collection) {
            cells.add(CsvCells.format(element));
        }
        return cells;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public T map(@NotNull CsvRow row) {
        Collection<Object> collection = newCollection(row.size());
        for (int i = 0; i < row.size(); i++) {
            collection.add(CsvCells.parse(mElementType, row, i, mCollectionClass));
        }
        return (T) collection;
    }

    /**
     * 创建集合实例
     *
     * <p>
     *   List 接口使用 {@link ArrayList}，Set 接口使用 {@link LinkedHashSet}，
     *   具体集合类型通过无参构造函数创建。
     * </p>
     *
     * @param expectedSize 预期元素数量
     * @throws CsvMappingException 集合类型无法实例化时抛出
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private Collection<Object> newCollection(int expectedSize) {
        if (mCollectionClass.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<>(expectedSize);
        }
        if (mCollectionClass.isAssignableFrom(LinkedHashSet.class)) {
            return new LinkedHashSet<>();
        }
        if (mCollectionClass.isInterface() || Modifier.isAbstract(mCollectionClass.getModifiers())) {
            throw new CsvMappingException("Cannot instantiate abstract collection type", mCollectionClass);
        }
        try {
            return (Collection<Object>) mCollectionClass.getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            throw new CsvMappingException("Cannot instantiate collection, ensure it has a public no-arg constructor",
                    mCollectionClass, exception);
        }
    }
}