package com.mx.hb.moon.entity;

import android.graphics.drawable.Drawable;

/**
 * Created by Moon on 2016/4/5.
 */
public class AppInfo {
    /**
     * 应用包名
     */
    private String packageName;
    /**
     * 应用名
     */
    private String appName;
    /**
     * 应用的图标
     */
    private Drawable appIcon;
    /**
     * 是否为系统应用
     */
    private boolean isSystem;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setIsSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }
}

