package com.yanhuahealth.healthlauncher.model.app;

import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;

/**
 * 用于所有应用列表中的应用信息
 */
public class AppInfo {
    public String appName = "";
    public String packageName = "";
    public String versionName = "";
    public int versionCode = 0;
    public Drawable appIcon = null;
    public PackageInfo packageInfo;
    public Shortcut shortcut;

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", shortcut=" + shortcut +
                '}';
    }
}
