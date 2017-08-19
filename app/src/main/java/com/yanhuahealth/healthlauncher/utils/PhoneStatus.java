package com.yanhuahealth.healthlauncher.utils;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class PhoneStatus {

    private static volatile PhoneStatus instance;

    public static PhoneStatus getInstance() {
        if (instance == null) {
            synchronized (PhoneStatus.class) {
                if (instance == null) {
                    instance = new PhoneStatus();
                }
            }
        }
        return instance;
    }


    // 检查是否存在 SIM 卡
    public static boolean checkPhoneSimCard(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

        // SIM卡没有就绪
        if (mTelephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY) {
            Toast.makeText(context, "没有检测到 SIM 卡", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // 检测是否在wifi 状态下
    public boolean isWiFi(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] infos = connectivity.getAllNetworkInfo();
            if (infos != null) {
                for (NetworkInfo ni : infos) {
                    if (ni.getTypeName().equals("WIFI") && ni.isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
