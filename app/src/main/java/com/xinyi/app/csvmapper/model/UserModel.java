package com.xinyi.app.csvmapper.model;

import androidx.annotation.NonNull;

import com.xinyi.csvmapper.annotation.CsvColumn;

/**
 * 用户数据模型（测试）
 *
 * <p> 展示框架的注解驱动对象的读写能力，通过 {@link CsvColumn} 注解声明字段与 CSV 列的映射关系 </p>
 *
 * @author 新一
 * @date 2026/4/23 17:39
 */
public class UserModel {

    /**
     * 用户 ID
     */
    @CsvColumn(name = "id", index = 0)
    private int id;

    /**
     * 用户名
     */
    @CsvColumn(name = "name", index = 1)
    private String name;

    /**
     * 年龄
     */
    @CsvColumn(name = "age", index = 2)
    private int age;

    /**
     * 座右铭
     *
     * <p> 可以选择设置最大长度和超出时的截断符号：maxLength = 20, truncateSuffix = "…" </p>
     */
    @CsvColumn(name = "motto", index = 4)
    private String motto;

    /**
     * 无参构造函数
     *
     * <p> 注解映射时框架通过反射调用此构造函数创建实例，必须保留 </p>
     */
    public UserModel() { }

    /**
     * 构造函数
     *
     * @param id 用户 ID
     * @param name 用户名
     * @param age 年龄
     * @param motto 座右铭
     */
    public UserModel(int id, String name, int age, String motto) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.motto = motto;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getMotto() {
        return motto;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + "id=" + id + ", name=" + name + ", age=" + age + ", motto=" + motto + "}";
    }
}