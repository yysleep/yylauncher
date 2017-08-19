package com.yanhuahealth.healthlauncher.model.sys.appmgr;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 指定 shortcut 的背景，字体颜色等等样式属性
 */
public class ShortcutStyle implements Parcelable {

    // 标题字体颜色
    public int titleColor;

    // 背景色
    public int backgroundColor;

    public ShortcutStyle(int backgroundColor, int titleColor) {
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
    }

    public ShortcutStyle() {
    }

    public ShortcutStyle(Parcel source) {
        titleColor = source.readInt();
        backgroundColor = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(titleColor);
        dest.writeInt(backgroundColor);
    }

    public static final Parcelable.Creator<ShortcutStyle> CREATOR
            = new Parcelable.Creator<ShortcutStyle>() {

        @Override
        public ShortcutStyle createFromParcel(Parcel source) {
            return new ShortcutStyle(source);
        }

        @Override
        public ShortcutStyle[] newArray(int size) {
            return new ShortcutStyle[size];
        }
    };
}
