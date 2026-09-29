package com.xinyi.csvmapper.config;

/**
 * 表头模式
 *
 * <p> 决定 CSV 文件的首行是否作为表头读取或写入。</p>
 *
 * @author 新一
 * @date 2026/9/29 16:20
 */
public enum HeaderMode {

    /**
     * 自动判断
     *
     * <p> 根据行类型是否提供表头确定模式。</p>
     */
    AUTO,

    /**
     * 存在表头
     *
     * <p> 首行作为表头处理。</p>
     */
    PRESENT,

    /**
     * 没有表头
     */
    ABSENT
}