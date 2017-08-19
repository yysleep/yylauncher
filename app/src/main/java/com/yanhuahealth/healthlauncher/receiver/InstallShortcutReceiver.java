package com.yanhuahealth.healthlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Parcelable;

import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.utils.Utilities;
import com.yanhuahealth.healthlauncher.utils.FastBitmapDrawable;

/**
 * 该接收器主要负责 shortcut 的安装
 * 如：微信联系人添加至桌面
 */
public class InstallShortcutReceiver extends BroadcastReceiver {

    private static final String ACTION_INSTALL_SHORTCUT
            = "com.android.launcher.action.INSTALL_SHORTCUT";

    private String tag() {
        return InstallShortcutReceiver.class.getName();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        YHLog.d(tag(), "onReceive - " + intent.getAction());

        if (!ACTION_INSTALL_SHORTCUT.equals(intent.getAction())) {
            YHLog.i(tag(), "onReceive - not support action: " + intent.getAction());
            return ;
        }

        installShortcut(context, intent);
    }

    // 解析和安装 shortcut 至 launcher 中
    private boolean installShortcut(Context context, Intent intent) {

        Intent shortcutIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (shortcutIntent.getAction() == null) {
            shortcutIntent.setAction(Intent.ACTION_VIEW);
        }

        String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Bitmap shortcutIcon = null;
        Intent.ShortcutIconResource iconResource;

        if (bitmap != null) {
            shortcutIcon = Utilities.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);
        } else {
            Parcelable extra = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof Intent.ShortcutIconResource) {
                try {
                    iconResource = (Intent.ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    shortcutIcon = Utilities.createIconBitmap(resources.getDrawable(id), context);
                } catch (Exception e) {
                    YHLog.e(tag(), "Could not load shortcut icon: " + extra);
                }
            }
        }

        // 微信快捷方式
        if (shortcutIntent.getAction().contains("com.tencent.mm.action.BIZSHORTCUT")) {
            ShortcutMgr.getInstance().addWeixinShortcut(name, shortcutIcon, shortcutIntent);
        }

        return true;
    }
}
