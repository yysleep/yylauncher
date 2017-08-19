package com.yanhuahealth.healthlauncher.model.sys.appmgr;

import android.graphics.Bitmap;

/**
 * shortcut 状态图标
 */
public class ShortcutStateIcon {

    // shortcut 标识
    public int shortcutLocalId;

    // 主键标识
    public int localId;

    // 状态值，不同业务不同状态值
    public int state;

    // 对应的状态图标
    public Bitmap icon;

    // 状态图标路径
    public String iconUrl;

    @Override
    public String toString() {
        return "ShortcutStateIcon{" +
                "shortcutLocalId=" + shortcutLocalId +
                ", localId=" + localId +
                ", state=" + state +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }
}
