package com.yanhuahealth.healthlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutRemoveResult;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;

import java.util.HashMap;
import java.util.Map;

/**
 * 安装，卸载应用的接收器
 */
public class InstallAppReceiver extends BroadcastReceiver {

    private String tag() {
        return InstallAppReceiver.class.getName();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getData().toString();
            YHLog.d(tag(), "package added - " + packageName);
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            YHLog.d(tag(), "package removed - " + packageName);

            ShortcutRemoveResult removeResult = ShortcutMgr.getInstance().removeExAppShortcut(packageName);
            if (removeResult != null && removeResult.result) {
                if (!removeResult.isRemovedPage) {
                    // 通知桌面更新
                    MainService.getInstance().sendBroadEvent(
                            new BroadEvent(EventType.SVC_NOTIFY_REFRESH, null));
                } else {
                    // 表示有页面被移除
                    // 则需要广播删除应用事件
                    Map<String, Object> eventInfo = new HashMap<>();
                    eventInfo.put(EventType.KEY_SVC_APP_UNINSTALL_PKG_NAME, packageName);
                    eventInfo.put(EventType.KEY_SVC_APP_UNINSTALL_REMOVE_RESULT, removeResult);
                    MainService.getInstance().sendBroadEvent(
                            new BroadEvent(EventType.SVC_APP_UNINSTALL, eventInfo));
                }
            }
        }
    }
}
