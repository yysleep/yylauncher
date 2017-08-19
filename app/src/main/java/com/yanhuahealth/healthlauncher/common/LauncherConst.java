package com.yanhuahealth.healthlauncher.common;

import android.os.Environment;

/**
 * 放置整个应用各模块都会使用的常量
 */
public class LauncherConst {

    public static final int APP_ID = 100;
    public static final String APP_KEY = "4227295ad201412303d4527439f45b0df56dbe06";

    // 定义控制中心图标的几种状态
    public static final int CC_NET_STATE_WIFI_CONN_MOBILE_CONN = 0;
    public static final int CC_NET_STATE_WIFI_DISCONN_MOBILE_CONN = 1;
    public static final int CC_NET_STATE_WIFI_CONN_MOBILE_DISCONN = 2;
    public static final int CC_NET_STATE_WIFI_DISCONN_MOBILE_DISCONN = 3;

    // 短信的收发类型
    public static final String SMS_TYPE_RECV = "1";
    public static final String SMS_TYPE_SEND = "2";

    // intent 参数定义
    public static final String INTENT_ACTION_DOWN_APK = "yhlauncher.download";
    public static final String INTENT_ACTION_DOWN_EBOOK="yhlauncher.download.ebook";
    public static final String INTENT_ACTION_DOWN_VOICE = "yhlauncher.download.voice";

    // 从contactlist发来的信息
    public static final String INTENT_FROM_CONTACT_LIST="INTENT_FROM_CONTACT_LIST";

    // 待下载的 APK 安装包的 URL
    public static final String INTENT_PARAM_APK_URL = "ApkDownLoad";
    public static final String INTENT_PARAM_APK_TITLE = "ApkTitle";

    public static final String INTENT_PARAM_SELECT_CONTACT = "select_contact";
    public static final String INTENT_PARAM_SHORTCUT_ID = "shortcut_id";
    public static final String INTENT_PARAM_CONTACT_ID = "contact_id";
    public static final String INTENT_PARAM_RAW_CONTACT_ID = "raw_contact_id";
    public static final String INTENT_PARAM_CONTACT_NAME = "contact_name";
    public static final String INTENT_PARAM_PHONE_NUMBER = "phone_number";

    // 被用作DownlaodManagerUtils里面的位置
    public static final String DOWNLOAD_PATH_APK="apk";
    public static final String DOWNLOAD_PATH_EBOOK="ebook";
    public static final String DOWNLOAD_PATH_VOICE="voice";


    // 桌面的文件存放根路径
    public static String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/yhlauncher/";
    }

    // 安装包文件存放根路径
    public static String getApkRootPath() {
        return getRootPath() + "/apk/";
    }

    // 电子书根路径
    public static String getEbookRootPath() {
        return getRootPath() + "/ebook/";
    }

    // 语音频道路径
    public static String getVoiceRootPath() {
        return getRootPath() + "/voice/";
    }

    // 图片存放路径
    public static String getImageRootPath() {
        return getRootPath() + "/image/";
    }

    // 图标存放路径
    public static String getIconRootPath() {
        return getRootPath() + "/icon/";
    }

    // 定义针对各个运营商的话费查询的回复短信的匹配格式
    public static final String PATTERN_QUERY_FEE = "余额";

    // 查询余额的间隔时长（单位：秒）
    public static final long QUERY_BALANCE_TIMEOUT = 90;
}
