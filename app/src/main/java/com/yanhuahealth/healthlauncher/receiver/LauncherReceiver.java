package com.yanhuahealth.healthlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.MainService;

import java.util.HashMap;
import java.util.Map;

/**
 * launcher 接收器，主要负责:
 *
 * - 接收外部应用的生效和失效事件的处理
 */
public class LauncherReceiver extends BroadcastReceiver {

    private String tag() {
        return LauncherReceiver.class.getName();
    }

    private NetworkInfo.State currWifiState;
    private NetworkInfo.State currMobileState;

    public LauncherReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            YHLog.i(tag(), "onReceive - network changed");
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            NetworkInfo.State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

            if (wifiState != null && mobileState != null
                    && NetworkInfo.State.DISCONNECTED == wifiState
                    && NetworkInfo.State.DISCONNECTED == mobileState) {
                if ((currMobileState == null && currWifiState == null)
                        || (currMobileState != mobileState && currWifiState != wifiState)) {
                    Toast.makeText(context, "网络已断开，请检查网络设置", Toast.LENGTH_LONG).show();
                }
            }

            currWifiState = wifiState;
            currMobileState = mobileState;

            // 发送广播事件
            Map<String, Object> eventInfo = new HashMap<>();
            eventInfo.put(EventType.KEY_SYS_NET_CHANGE_WIFI, currWifiState);
            eventInfo.put(EventType.KEY_SYS_NET_CHANGE_MOBILE, currMobileState);
            MainService.getInstance().sendBroadEvent(new BroadEvent(EventType.SYS_NET_CHANGE, eventInfo));
        }
    }
}
