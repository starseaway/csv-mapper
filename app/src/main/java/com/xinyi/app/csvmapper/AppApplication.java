package com.xinyi.app.csvmapper;

import android.app.Application;

import com.xinyi.androidbasic.app.ActivityManager;
import com.xinyi.androidbasic.app.AppContext;
import com.xinyi.beehive.TaskBeehive;
import com.xinyi.device.DeviceContext;

/**
 * 应用 AppApplication
 *
 * @author 杨耿雷
 * @date 2026/4/23 18:56
 */
public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppContext.init(this);
        ActivityManager.getInstance().init(this);
        DeviceContext.init(this);
        TaskBeehive.init(this);
    }
}
