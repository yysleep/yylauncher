package com.yanhuahealth.healthlauncher.model.sys.upgrade;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Launcher 的版本信息
 */
public class VersionInfo implements Serializable {

    // 本次是否需要升级
    @Expose
    @SerializedName("needUpdate")
    public int needUpdate;

    // 应用标识
    @Expose
    @SerializedName("appId")
    public int appId;

    // SDK 版本号
    public int sdkVer;

    // 系统标识
    @Expose
    @SerializedName("os_id")
    public int osId;

    // 版本代码
    @Expose
    @SerializedName("versionCode")
    public int verCode;

    // 版本名称，如：1.4.1
    @Expose
    @SerializedName("versionName")
    public String verName;

    // 版本描述
    @Expose
    @SerializedName("versionDesc")
    public String verDesc;

    // 安装包的大小
    @Expose
    @SerializedName("pkgSize")
    public int pkgSize;

    // 安装包的下载路径
    @Expose
    @SerializedName("pkgUrl")
    public String pkgUrl;

    // 安装包的本地存储路径
    public String packageLocalPath;

    @Override
    public String toString() {
        return "VersionInfo{" +
                "appId=" + appId +
                ", needUpdate=" + needUpdate +
                ", sdkVer=" + sdkVer +
                ", osId=" + osId +
                ", verCode=" + verCode +
                ", verName='" + verName + '\'' +
                ", verDesc='" + verDesc + '\'' +
                ", pkgSize=" + pkgSize +
                ", pkgUrl='" + pkgUrl + '\'' +
                ", packageLocalPath='" + packageLocalPath + '\'' +
                '}';
    }
}
