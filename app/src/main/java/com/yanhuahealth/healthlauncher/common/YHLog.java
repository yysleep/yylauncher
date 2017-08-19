package com.yanhuahealth.healthlauncher.common;

/**
 * 用于应用内的日志输出，替换 SDK 提供的 Log
 * 主要便于统一控制日志的输出和关闭
 */
public class YHLog {

    private static final boolean ENABLE_LOG = true;

    public static void v(String tag, String message) {
        if (ENABLE_LOG) {
            android.util.Log.v(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (ENABLE_LOG) {
            android.util.Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (ENABLE_LOG) {
            android.util.Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (ENABLE_LOG) {
            android.util.Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (ENABLE_LOG) {
            android.util.Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable tr) {
        if (ENABLE_LOG) {
            android.util.Log.e(tag, message, tr);
        }
    }
}
