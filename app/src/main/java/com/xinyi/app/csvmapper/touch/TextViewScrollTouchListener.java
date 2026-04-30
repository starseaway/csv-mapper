package com.xinyi.app.csvmapper.touch;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

/**
 * 解决 TextView 与父容器（ScrollView / NestedScrollView）滑动冲突
 *
 * <p> 适用于启用了 ScrollingMovementMethod 的 TextView </p>
 *
 * @author 新一
 * @date 2026/4/27 9:54
 */
public class TextViewScrollTouchListener implements View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        ViewParent parent = view.getParent();
        if (parent == null) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                parent.requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                parent.requestDisallowInterceptTouchEvent(false);
                view.performClick();
                break;
        }
        // 不消费事件，让 TextView 自己处理滚动
        return false;
    }
}