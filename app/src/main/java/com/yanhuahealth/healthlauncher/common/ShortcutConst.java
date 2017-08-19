package com.yanhuahealth.healthlauncher.common;

/**
 * 应用/快捷方式的常用参数定义
 */
public class ShortcutConst {

    // 表示布局的页面和指定页面上的哪个 box 未知
    public static final int PAGE_UNKNOWN = -1;

    // 默认 shortcut 占用的页数，
    // 默认 shortcut 页是用于放置内置应用
    // 新增的应用只能从 DEFAULT_PAGE_NUM 之后开始添加
    public static final int DEFAULT_PAGE_NUM = 3;

    // 默认每页显示的格子数目
    public static final int DEFAULT_BOX_NUM_PER_PAGE = 8;

    // center页面 显示格子的数目
    public static final int CENTER_BOX_NUM_PER_PAGE = 6;

    /**
     * ======= intentType =======
     */

    public static final int INTENT_TYPE_NULL = 0;  // 未知

    public static final int INTENT_TYPE_EXTERNAL_APP = 1;  // 外部应用

    // 需要在 extra 中提供跳转的页面的 Activity 类名称
    // key: activity
    public static final int INTENT_TYPE_INTERNAL_ACTIVITY = 2;  // 内部页面跳转

    /**
     * ======== shortcut extra params =========
     **/

    // 所有的使用 ShortcutBoxView 默认 click 事件传递到下一个 activity
    // 都会携带相应的 Shortcut 实例
    public static final String PARAM_SHORTCUT = "shortcut";

    // 传递 shortcut folder 的 localId
    public static final String PARAM_SHORTCUT_FOLDER_ID = "shortcut-folder";

    // 用于作为 intent param 传入的参数
    // 也用于 activity 之间传递的通用参数
    public static final String PARAM_INTENT = "intent";

    /**======== 各内置应用的坐标 =========**/

    /**
     * ---- first page ----
     */

    // SOS
    public static final String SOS = "一键呼救";
    public static final int SOS_PAGE = 0;
    public static final int SOS_POS = 0;

    // 控制中心
    public static final int CONTROL_CENTER_PAGE = 0;
    public static final int CONTROL_CENTER_POS = 1;

    // 家人
    public static final int FAMILY_PAGE = 0;
    public static final int FAMILY_POS = 2;

    // 朋友
    public static final int FRIENDS_PAGE = 0;
    public static final int FRIENDS_POS = 3;

    // 常用联系人
    public static final int TOP_CONTACT_FIRST_PAGE = 0;
    public static final int TOP_CONTACT_FIRST_POS = 4;

    public static final int TOP_CONTACT_SECOND_PAGE = 0;
    public static final int TOP_CONTACT_SECOND_POS = 5;

    public static final int TOP_CONTACT_THIRD_PAGE = 0;
    public static final int TOP_CONTACT_THIRD_POS = 6;

    // 联系人
    public static final int CONTACT_PAGE = 0;
    public static final int CONTACT_POS = 7;

    /**
     * ---- second page ----
     */

    // 健康小秘书
    public static final int HEALTH_ASSISTANT_PAGE = 1;
    public static final int HEALTH_ASSISTANT_POS = 2;

    // 幸福小秘书
    public static final int HEALTH_MGR_PAGE = 1;
    public static final int HEALTH_MGR_POS = 3;
    public static final String HEALTH_MGR_APK_URL = "http://www.laoyou99.cn/app/3/1/Medical.apk";

    // 照相机
    public static final int CAMERA_PAGE = 1;
    public static final int CAMERA_POS = 4;

    // 微信
    public static final int WEIXIN_PAGE = 1;
    public static final int WEIXIN_POS = 5;

    // 电话
    public static final int PHONE_PAGE = 1;
    public static final int PHONE_POS = 6;

    // 语音助手
    public static final int VOICE_ASSISTANT_PAGE = 1;
    public static final int VOICE_ASSISTANT_POS = 7;

    /**
     * ---- third page ----
     */

    // 电子书
    public static final String EBOOK = "电子书";
    public static final int EBOOK_PAGE = 2;
    public static final int EBOOK_POS = 2;

    // 语音频道
    public static final String VOICE_CHANNEL = "语音频道";
    public static final int VOICE_CHANNEL_PAGE = 2;
    public static final int VOICE_CHANNEL_POS = 3;

    // 相册
    public static final int ALBUM_PAGE = 2;
    public static final int ALBUM_POS = 4;

    // 上网
    public static final int IE_PAGE = 2;
    public static final int IE_POS = 5;

    // 短信
    public static final int SMS_PAGE = 2;
    public static final int SMS_POS = 6;

    // 老黄历
    public static final String CALENDAR = "老黄历";
    public static final int CALENDAR_PAGE = 2;
    public static final int CALENDAR_POS = 7;
    public static final String CALENDAR_PKG_NAME = "com.fanyue.laohuangli";
    public static final String CALENDAR_APK_URL = "http://thirdsoft.oss-cn-qingdao.aliyuncs.com/com.fanyue.laohuangli_10.apk ";
    /**
     * ---- forth page ----
     */

    // 工具
    public static final int TOOLS_PAGE = 3;
    public static final int TOOLS_POS = 0;

    // 设置
    public static final int SETTING_PAGE = 3;
    public static final int SETTING_POS = 1;

    // 帮助中心
    public static final int HELP_CENTER_PAGE = 3;
    public static final int HELP_CENTER_POS = 2;

    // 所有应用
    public static final int ALL_APPS_PAGE = 3;
    public static final int ALL_APPS_POS = 3;

    // 百度医生
    public static final String BD_DOCTOR_NAME = "百度医生";
    public static final int BD_DOCTOR_PAGE = 3;
    public static final int BD_DOCTOR_POS = 4;
    public static final String BD_DOCTOR_PKG_NAME = "com.baidu.patient";
    public static final String BD_DOCTOR_APK_URL = "http://thirdsoft.oss-cn-qingdao.aliyuncs.com/e049d086fd_V1.8.1-20151231_bd-pc_Patient_bd-pc.apk";

    // 一键查话费
    public static final String QUERY_COST_NAME = "一键查话费";
    public static final int QUERY_COST_PAGE = 3;
    public static final int QUERY_COST_POS = 5;

    // 头条新闻
    public static final String NETEASE_NEWS_NAME = "头条新闻";
    public static final int NETEASE_NEWS_PAGE = 3;
    public static final int NETEASE_NEWS_POS = 6;
    public static final String NETEASE_NEWS_PKG_NAME = "com.netease.newsreader.activity";
    public static final String NETEASE_NEWS_APK_URL = "http://thirdsoft.oss-cn-qingdao.aliyuncs.com/com.netease.newsreader.activity_470.apk";

    /*------- 二级页面 -------*/

    public static final int DEFAULT_CHILD_PAGE = -2;
}
