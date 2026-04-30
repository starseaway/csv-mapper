package com.xinyi.app.csvmapper.touch;

import android.annotation.SuppressLint;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

/**
 * 触摸相关兼容工具
 *
 * @author 新一
 * @date 2026/4/27 9:45
 */
public final class TouchCompat {

    private TouchCompat() { }

    /**
     * 解决 TextView 与父容器的滑动冲突
     *
     * @param view 目标 TextView
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void fixTextViewScroll(TextView view) {
        // 让 TextView 支持滚动
        view.setMovementMethod(new ScrollingMovementMethod());
        view.setVerticalScrollBarEnabled(true);
        view.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        view.setOnTouchListener(new TextViewScrollTouchListener());
    }
}