package com.xinyi.csvmapper.mapper;

import com.xinyi.csvmapper.model.CsvRow;

import org.jetbrains.annotations.NotNull;

/**
 * CSV 行到对象的映射接口
 *
 * <p> 实现类负责读取 {@link CsvRow} 中的数据，并创建对应的目标对象。</p>
 *
 * <p>
 *   使用示例：
 *   <pre><code>
 *       CsvRowMapper<User> mapper = row -> {
 *           User user = new User();
 *           user.setName(row.get("name"));
 *           user.setAge(Integer.parseInt(row.get("age")));
 *           return user;
 *       };
 *   </code></pre>
 * </p>
 *
 * @param <T> 目标对象类型
 *
 * @author 新一
 * @date 2026/4/23 11:21
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