package com.yanhuahealth.healthlauncher.model.sys.appmgr;

/**
 * 存储 shortcut 被移除的信息，如：
 *
 *  - 是否移除成功
 *  - 是否同时移除了对应的 shortcut 所在页（如：该 shortcut 为对应页面的最后一个时）
 */
public class ShortcutRemoveResult {

    // 被移除前的 shortcut
    public Shortcut shortcut;

    // true 表示移除成功，false 为移除失败
    public boolean result;

    // 是否移除所在的那一页
    public boolean isRemovedPage;

    @Override
    public String toString() {
        return "ShortcutRemoveInfo{" +
                "isRemovedPage=" + isRemovedPage +
                ", shortcut=" + shortcut +
                ", result=" + result +
                '}';
    }
}
