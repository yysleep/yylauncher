package com.yanhuahealth.healthlauncher.model.sys.appmgr;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 标识通用的 shortcut extra 结构
 */
public class ShortcutExtra implements Parcelable {

    // 主要用于内部应用页面之间跳转
    // 指向目标页面的 class name
    @Expose
    @SerializedName("activity")
    public String activity;

    // 主要用于跳转至目标页面时传递的参数
    // 具体内容由各个业务自定义
    @Expose
    @SerializedName("param")
    public String param;

    public ShortcutExtra(String activity, String param) {
        this.activity = activity;
        this.param = param;
    }

    public ShortcutExtra(Parcel source) {
        activity = source.readString();
        param = source.readString();
    }

    @Override
    public String toString() {
        return "ShortcutExtra{" +
                "param='" + param + '\'' +
                ", activity='" + activity + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(activity);
        dest.writeString(param);
    }

    public static final Parcelable.Creator<ShortcutExtra> CREATOR
            = new Parcelable.Creator<ShortcutExtra>() {

        @Override
        public ShortcutExtra createFromParcel(Parcel source) {
            return new ShortcutExtra(source);
        }

        @Override
        public ShortcutExtra[] newArray(int size) {
            return new ShortcutExtra[size];
        }
    };
}
