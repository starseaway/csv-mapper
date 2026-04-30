package com.xinyi.csvmapper.mapper;

import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;

/**
 * CSV 行到对象的映射接口
 *
 * <p> 用于将一行 {@link CsvRow} 数据转换为指定类型的 Java 对象，
 * 可由调用方自定义实现，也可使用注解驱动的 {@link com.xinyi.csvmapper.bind.AnnotationCsvReader} </p>
 *
 * <p> 使用示例：
 * <pre>
 *     CsvRowMapper&lt;User&gt; mapper = row -> {
 *         User user = new User();
 *         user.setName(row.get("name"));
 *         user.setAge(Integer.parseInt(row.get("age")));
 *         return user;
 *     };
 * </pre>
 * </p>
 *
 * @param <T> 目标对象类型
 * @author 新一
 * @date 2026/4/23
 */
public interface CsvRowMapper<T> {

    /**
     * 将 CSV 行映射为目标对象
     *
     * @param row 当前行数据
     * @return 映射后的目标对象
     */
    @NotNull
    T map(@NotNull CsvRow row);
}
