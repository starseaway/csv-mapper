package com.xinyi.csvmapper.utils;

import android.os.Build;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * File IO 工具类（兼容全 Android 版本）
 *
 * <p> 提供统一的文件输入/输出流创建方式，屏蔽 NIO 在低版本或部分 ROM 上的兼容问题 </p>
 *
 * @author 新一
 * @date 2026/4/27 8:58
 */
public final class FileIO {

    private FileIO() { }

    /**
     * 打开文件输入流
     *
     * @param file 目标文件
     * @return InputStream
     * @throws IOException 打开失败时抛出
     */
    @NotNull
    public static InputStream inputStream(@NotNull File file) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return Files.newInputStream(file.toPath());
            } catch (Throwable ignore) {
                return new FileInputStream(file);
            }
        } else {
            return new FileInputStream(file);
        }
    }

    /**
     * 打开文件输出流（覆盖写）
     *
     * @param file 目标文件
     * @return OutputStream
     * @throws IOException 打开失败时抛出
     */
    @NotNull
    public static OutputStream outputStream(@NotNull File file) throws IOException {
        return outputStream(file, false);
    }

    /**
     * 打开文件输出流
     *
     * @param file 目标文件
     * @param append true=追加，false=覆盖
     * @return OutputStream
     * @throws IOException 打开失败时抛出
     */
    @NotNull
    public static OutputStream outputStream(@NotNull File file, boolean append) throws IOException {
        ensureParent(file);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (append) {
                    return Files.newOutputStream(
                            file.toPath(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND
                    );
                } else {
                    return Files.newOutputStream(
                            file.toPath(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                }
            } catch (Throwable ignore) {
                return new FileOutputStream(file, append);
            }
        } else {
            return new FileOutputStream(file, append);
        }
    }

    /**
     * 确保父目录存在
     *
     * @param file 目标文件
     */
    private static void ensureParent(@NotNull File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs() && !parent.exists()) {
                throw new IOException("Cannot create parent dir: " + parent);
            }
        }
    }
}