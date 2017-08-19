package com.yanhuahealth.healthlauncher.sys.appmgr;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.sys.ImageManager;
import com.yanhuahealth.healthlauncher.model.app.AppInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 应用管理，主要维护所有应用的基本信息的缓存
 */
public class AppMgr {

    private static volatile AppMgr instance;

    public static AppMgr getInstance() {
        if (instance == null) {
            synchronized (AppMgr.class) {
                if (instance == null) {
                    instance = new AppMgr();
                }
            }
        }
        return instance;
    }

    // 主要维护所有第三方应用的
    private List<AppInfo> allApps = new CopyOnWriteArrayList<>();
    private Lock lockAllApps = new ReentrantLock();

    // 初始化加载
    // 放到独立的线程中加载
    public void init(final Context ctx) {
        lockAllApps.lock();
        try {
            allApps.clear();
        } finally {
            lockAllApps.unlock();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<PackageInfo> packages = ctx.getPackageManager()
                        .getInstalledPackages(0);
                lockAllApps.lock();

                try {
                    for (int i = 0; i < packages.size(); i++) {
                        PackageInfo packageInfo = packages.get(i);
                        AppInfo tmpInfo = new AppInfo();
                        tmpInfo.appName = packageInfo.applicationInfo.loadLabel(
                                ctx.getPackageManager()).toString();
                        tmpInfo.packageName = packageInfo.packageName;
                        tmpInfo.versionName = packageInfo.versionName;
                        tmpInfo.versionCode = packageInfo.versionCode;
                        tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(ctx.getPackageManager());
                        if (!ImageManager.getInstance().existsAppIconWithPkgName(
                                packageInfo.applicationInfo.packageName)) {
                            ImageManager.getInstance().saveAppIcon(ctx, packageInfo.applicationInfo);
                        }

                        // 如果属于非系统程序，以及非默认应用，则添加到列表显示
                        if (ctx.getPackageManager().getLaunchIntentForPackage(packageInfo.packageName) != null
                                && !packageInfo.packageName.startsWith("com.tencent.mm")
                                && !packageInfo.packageName.startsWith("com.yanhuahealth.healthlauncher")) {
                            tmpInfo.packageInfo = packageInfo;
                            tmpInfo.shortcut = ShortcutMgr.getInstance().getShortcutOfExApp(packageInfo);
                            if (!(tmpInfo.shortcut != null && tmpInfo.shortcut.type < ShortcutType.EXTERNAL_APP)) {
                                allApps.add(tmpInfo);
                            }
                        }
                    }
                } finally {
                    lockAllApps.unlock();
                }
            }
        }).start();
    }

    public List<AppInfo> getAllApps() {
        lockAllApps.lock();
        try {
            return allApps;
        } finally {
            lockAllApps.unlock();
        }
    }

    /**
     * 新增应用
     *
     * @param appInfo 应用信息
     * @return 0 表示成功，其他表示失败
     */
    public int addApp(AppInfo appInfo) {

        if (appInfo == null || appInfo.appName == null || appInfo.packageName == null) {
            return -1;
        }

        lockAllApps.lock();
        try {
            for (AppInfo a : allApps) {
                if (a.packageName.equals(appInfo.packageName)) {
                    return 1;
                }
            }

            allApps.add(appInfo);
        } finally {
            lockAllApps.unlock();
        }

        return 0;
    }

    /**
     * 移除应用
     */
    public int removeApp(String packageName) {

        if (packageName == null || packageName.equals("")) {
            return -1;
        }

        lockAllApps.lock();
        try {
            for (AppInfo a : allApps) {
                if (a != null && a.packageName.equals(packageName)) {
                    allApps.remove(a);
                    return 0;
                }
            }
        } finally {
            lockAllApps.unlock();
        }

        return 1;
    }

    public int updateApp(Context context, String packageName) {
        if (packageName == null || packageName.equals("")) {
            return -1;
        }

        lockAllApps.lock();
        try {
            for (AppInfo a : allApps) {
                if (a != null && a.packageName.equals(packageName)) {
                    a.shortcut = null;
                    return 0;
                }
            }
        } finally {
            lockAllApps.unlock();
        }

        return 1;
    }

    /**
     * 判断是否存在对应的应用
     */
    public boolean isExistsApp(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName) != null;
    }
}
