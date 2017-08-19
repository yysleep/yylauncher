package com.yanhuahealth.healthlauncher.sys;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.os.Handler;

import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.weather.TodayWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 提供了检查 wifi，GPRS 等网络状态的工具
 */
public class NetMgr {

    private String tag() {
        return NetMgr.class.getName();
    }

    private final Lock lock = new ReentrantLock();

    private static volatile NetMgr instance;

    public static NetMgr getInstance() {
        if (instance == null) {
            synchronized (NetMgr.class) {
                if (instance == null) {
                    instance = new NetMgr();
                }
            }
        }
        return instance;
    }

    private Context context;

    public void init(Context context) {
        YHLog.i(tag(), "init");
        this.context = context;
    }

    // 判断 wifi 是否连接可用
    public boolean isWifiConnected() {

        ConnectivityManager connMgr
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetInfo != null && wifiNetInfo.isConnected();
    }

    // 移动网络是否连接可用（在WiFi连接状态下无效）
    public boolean isMobileConnected() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }

        ConnectivityManager connMgr
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (mobile == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

    // 判断移动网络连接是否可用（在WiFi已连接条件下也可使用）
    public boolean isNetConnect() {
        ConnectivityManager connManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class cmClass = connManager.getClass();
        Class[] argClasses = null;
        Object[] argObject = null;
        Boolean isOpen = false;
        try {
            Method method = cmClass.getMethod("getMobileDataEnabled", argClasses);

            isOpen = (Boolean) method.invoke(connManager, argObject);
            if (isOpen == true) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 打开或关闭移动网络连接
    public void toggleMobileConnect(boolean enableMobile) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // todo
//            toggleMobileConnectForLollipop(enableMobile);
            return;
        }

        ConnectivityManager connMgr
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class<?> cmClass = connMgr.getClass();
        Class<?>[] argClasses = new Class[1];
        argClasses[0] = boolean.class;

        Method method;
        try {
            method = cmClass.getMethod("setMobileDataEnabled", argClasses);
            method.invoke(connMgr, enableMobile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 针对 Android 5.x 以上版本的 移动网络开关
    private void toggleMobileConnectForLollipop(boolean enableMobile) {

        String transactionCode = getTransactionCode(context);
        if (transactionCode == null || transactionCode.length() == 0) {
            YHLog.w(tag(), "toggleMobileConnectForLollipop - invalid transaction");
            return;
        }

        String command;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            SubscriptionManager subscriptionManager
                    = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            for (int idx = 0; idx < subscriptionManager.getActiveSubscriptionInfoList().size(); ++idx) {
                // Get the active subscription ID for a given SIM card.
                int subscriptionId = subscriptionManager.getActiveSubscriptionInfoList().get(idx).getSubscriptionId();

                // Execute the command via `su` to turn off
                // mobile network for a subscription service.
                command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + (enableMobile ? 0 : 1);
                executeCommandViaSu("-c", command);
            }
        }
    }

    private String getTransactionCode(Context context) {
        try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        } catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            YHLog.e(tag(), "getTransactionCode exception: " + e.getMessage());
            return null;
        }
    }

    private void executeCommandViaSu(String option, String command) {
        boolean success;
        String su = "su";
        try {
            Runtime.getRuntime().exec(new String[]{su, option, command});
            success = true;
        } catch (IOException e) {
            success = false;
            YHLog.e(tag(), "executeCommandViaSu - can not execute su: " + e.getMessage());
        }

        if (success) {
            YHLog.i(tag(), "executeCommandViaSu - execute success - " + command);
            return;
        }

        su = "/system/xbin/su";
        try {
            Runtime.getRuntime().exec(new String[]{su, option, command});
            success = true;
        } catch (IOException e) {
            success = false;
            YHLog.e(tag(), "executeCommandViaSu - can not execute su: " + e.getMessage());
        }

        if (success) {
            YHLog.i(tag(), "executeCommandViaSu - execute success - " + command);
            return;
        }

        su = "/system/xbin/su";
        try {
            Runtime.getRuntime().exec(new String[]{su, option, command});
            success = true;
        } catch (IOException e) {
            success = false;
            YHLog.e(tag(), "executeCommandViaSu - can not execute su: " + e.getMessage());
        }

        if (success) {
            YHLog.i(tag(), "executeCommandViaSu - execute success - " + command);
            return;
        }

        su = "/system/bin/su";
        try {
            Runtime.getRuntime().exec(new String[]{su, option, command});
        } catch (IOException e) {
            YHLog.e(tag(), "executeCommandViaSu - can not execute su: " + e.getMessage());
        }
    }

    public void weaHttp(Handler handler){
        lock.lock();
        try {
            URL httpUrl = new URL(getUrl(System.currentTimeMillis()));
            HttpURLConnection con = (HttpURLConnection) httpUrl.openConnection();
            con.setReadTimeout(5000);
            con.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer sb = new StringBuffer();

            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }

            reader.close();
            final TodayWeather wdata = parseWeatherJson(sb.toString());

            if (wdata != null) {
                Message msg = new Message();
                msg.obj = wdata;
                msg.what = 50;
                handler.sendMessage(msg);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Message msg = new Message();
            msg.what = 51;
            handler.sendMessage(msg);
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private String getUrl(long date) {
        return "http://www.laoyou99.cn/201/weather?date=" + date + "&lang=2&area=%E5%8D%97%E4%BA%AC&prov=%E6%B1%9F%E8%8B%8F&reqtype=1";
    }

    // 讲解析出来的数据塞进weather_data里
    private TodayWeather parseWeatherJson(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONObject resultsObject = object.getJSONObject("result");
            JSONObject fObject = resultsObject.getJSONObject("f");
            JSONArray jsonArray = fObject.getJSONArray("f1");
            List<TodayWeather> list = new ArrayList<>();
            for (int i = 0; i < 1; i++) {
                TodayWeather todayWeather = new TodayWeather();
                JSONObject resultsObjcets = jsonArray.getJSONObject(i);
                String wea = resultsObjcets.getString("fa");
                String weaNight = resultsObjcets.getString("fb");
                String hTemp = resultsObjcets.getString("fc");
                String lTemp = resultsObjcets.getString("fd");
                String wind = resultsObjcets.getString("fe");
                todayWeather.setFa(wea);
                todayWeather.setFb(weaNight);
                todayWeather.setFc(hTemp);
                todayWeather.setFd(lTemp);
                todayWeather.setFe(wind);
                list.add(todayWeather);
            }
            return list.get(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
