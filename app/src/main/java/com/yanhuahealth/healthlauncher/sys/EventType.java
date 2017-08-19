package com.yanhuahealth.healthlauncher.sys;

/**
 * 事件类型定义
 */
public class EventType {

    /**===== 系统事件 [100, 1000) ====**/

    /**
     * 网络更新事件
     */
    public static final int SYS_NET_CHANGE = 101;

    // wifi 网络状态，对应的 value 为 Integer 类型
    public static final String KEY_SYS_NET_CHANGE_WIFI = "wifi";

    // mobile 网络状态，对应的 value 为 Integer 类型
    public static final String KEY_SYS_NET_CHANGE_MOBILE = "mobile";

    /**
     * 新的消息数量
     */
    public static final int SYS_SMS_RECEIVED = 102;

    // 有电话进来
    public static final int SYS_CALL_RECEIVED = 103;

    // 更新未读短信数量
    public static final int SYS_SMS_CHANGE=104;

    // 发送方号码，对应的 value 为 String 类型
    public static final String KEY_SYS_SMS_RECEIVED_SENDER_NUMBER = "send-number";

    // 消息内容，对应的 value 为 String 类型
    public static final String KEY_SYS_SMS_RECEIVED_CONTENT = "content";

    /**===== 业务处理事件 [1000, 2000) ====**/

    /**
     * 桌面通知刷新事件
     * 主要用于需要刷新桌面的场景，如：
     */
    public static final int SVC_NOTIFY_REFRESH = 1000;

    /**
     * 应用被卸载
     */
    public static final int SVC_APP_UNINSTALL = 1001;

    // 被卸载的应用包名称，类型为 String
    public static final String KEY_SVC_APP_UNINSTALL_PKG_NAME = "package-name";

    // 被卸载的应用对应的 shortcut 移除结果信息
    public static final String KEY_SVC_APP_UNINSTALL_REMOVE_RESULT = "remove-result";

    /**
     * 一键查话费
     */
    public static final int SVC_QUERY_BALANCE = 1002;

    // 查话费对应的 shortcut localId，类型为 int
    public static final String KEY_SVC_QUERY_BALANCE_SHORTCUT_ID = "shortcut-id";

    /**
     * 一键查话费的结果更新事件
     * 主要用于通知相应的 ShortcutBoxView 更新 UI
     * 如：90 秒内不可重复查询
     */
    public static final int SVC_QUERY_BALANCE_UPD = 1003;
}
