package com.yanhuahealth.healthlauncher.model.sys.appmgr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 桌面图标信息
 */
public class Shortcut implements Parcelable {

    // 本地 DB 中的唯一标识
    public int localId;

    // 对于属于某一文件夹下的 shortcut 还需要指定其上一级的 shortcut
    public int parentLocalId;

    // 快捷方式展示标题
    public String title;

    // 快捷方式的图标
    public Bitmap icon;
    public String iconUrl;

    // 如果指定了 iconResId，则优先采用 iconResId
    // 否则先使用 icon 对象，再使用 iconUrl
    public int iconResId;

    // 支持不同状态不同图标
    public HashMap<Integer, Bitmap> stateIcons = new HashMap<>();

    // 关注的事件列表
    public List<Integer> events = new ArrayList<>();

    // shortcut 类型
    public int type;

    // 如果为外部应用，可以提供相应的安装包下载路径
    public String apkUrl;

    // 对于外部应用，还需要提供相应的 package name
    public String appPackageName;

    // 角标
    public int numSign;

    // 是否为 Folder
    // 如果为 true，则实际类型为 ShortcutFolder
    public boolean isFolder;

    // 该 shortcut 所在的坐标位置
    public int page;
    public int posInPage;

    // shortcut 的展示样式
    public ShortcutStyle style;

    // 跳转类型，如：跳转至外部应用 or 内部页面
    public int intentType;

    // 用于启动该应用或快捷方式的 intent
    public Intent intent;

    // 其他参数
    // json 格式表示，由各不同类型的 shortcut 自定义
    public ShortcutExtra extra;

    // 快捷方式列表
    public ArrayList<Integer> shortcuts = new ArrayList<>();

    // shortcut 的上次更新时间
    // 当前主要用于 主页 的 ShortcutBoxView 来判断是否当前 shortcut 被更新过
    // 可避免不停的设置 ShortcutBox
    public long lastUpdatedTime = System.currentTimeMillis();

    // 是否允许强制更新本地库
    // 如：更改了 shortcut 的背景色，跳转类型等等属性
    // 有些 shortcut 是不允许更新的，需要用户设置的 shortcut（如：添加联系人）
    // true：表示允许，一般都是针对内置不可更改的 Shortcut
    // false: 表示不允许
    public boolean enableUpdate;

    // 用于表示当前的 shortcut 是否可点击
    public boolean isEnable = true;

    public Shortcut() {
        this.type = ShortcutType.NULL;
        this.page = ShortcutConst.PAGE_UNKNOWN;
        this.intentType = ShortcutConst.INTENT_TYPE_NULL;
    }

    public Shortcut(int page, int posInPage, String title, int type, boolean isFolder) {
        this.page = page;
        this.posInPage = posInPage;
        this.title = title;
        this.type = type;
        this.isFolder = isFolder;
        this.intentType = ShortcutConst.INTENT_TYPE_NULL;
    }

    public void setIcon(int iconResId) {
        if (iconResId <= 0) {
            return;
        }

        this.iconResId = iconResId;
        this.iconUrl = null;
        this.icon = null;
    }

    public void setIcon(String iconUrl) {
        if (iconUrl == null || iconUrl.length() == 0) {
            return;
        }

        this.iconResId = 0;
        this.iconUrl = iconUrl;
        this.icon = null;
    }

    public void setIcon(Bitmap bmpIcon) {
        if (bmpIcon == null) {
            return;
        }

        this.iconResId = 0;
        this.iconUrl = null;
        this.icon = bmpIcon;
    }

    public Shortcut(Parcel source) {
        localId = source.readInt();
        parentLocalId = source.readInt();
        title = source.readString();
        iconUrl = source.readString();
        type = source.readInt();
        apkUrl = source.readString();
        appPackageName = source.readString();
        isFolder = source.readInt() != 0;
        page = source.readInt();
        posInPage = source.readInt();
        stateIcons = source.readHashMap(HashMap.class.getClassLoader());
        style = source.readParcelable(ShortcutStyle.class.getClassLoader());
        intentType = source.readInt();
        extra = source.readParcelable(ShortcutExtra.class.getClassLoader());
        shortcuts = source.readArrayList(Integer.class.getClassLoader());
        numSign=source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(localId);
        dest.writeInt(parentLocalId);
        dest.writeString(title);
        dest.writeString(iconUrl);
        dest.writeInt(type);
        dest.writeString(apkUrl);
        dest.writeString(appPackageName);
        dest.writeInt(isFolder ? 1 : 0);
        dest.writeInt(page);
        dest.writeInt(posInPage);
        dest.writeMap(stateIcons);
        dest.writeParcelable(style, 0);
        dest.writeInt(intentType);
        dest.writeParcelable(extra, 0);
        dest.writeList(shortcuts);
        dest.writeInt(numSign);
    }

    public static final Parcelable.Creator<Shortcut> CREATOR
            = new Parcelable.Creator<Shortcut>() {

        @Override
        public Shortcut createFromParcel(Parcel source) {
            return new Shortcut(source);
        }

        @Override
        public Shortcut[] newArray(int size) {
            return new Shortcut[size];
        }
    };

    @Override
    public String toString() {
        return "Shortcut{" +
                "localId=" + localId +
                ", parentLocalId=" + parentLocalId +
                ", title='" + title + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", stateIcons=" + stateIcons +
                ", events=" + events +
                ", type=" + type +
                ", apkUrl='" + apkUrl + '\'' +
                ", appPackageName='" + appPackageName + '\'' +
                ", numSign=" + numSign +
                ", isFolder=" + isFolder +
                ", page=" + page +
                ", posInPage=" + posInPage +
                ", style=" + style +
                ", intentType=" + intentType +
                ", intent=" + intent +
                ", extra=" + extra +
                ", shortcuts=" + shortcuts +
                ", lastUpdatedTime=" + lastUpdatedTime +
                '}';
    }
}
