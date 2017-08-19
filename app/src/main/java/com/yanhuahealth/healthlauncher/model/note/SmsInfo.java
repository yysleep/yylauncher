package com.yanhuahealth.healthlauncher.model.note;

/**
 * 短信信息
 */
public class SmsInfo {
    // id
    public int id;

    // thread id
    public int threadId;

    // 短信内容
    public String smsbody;

    // 发送短信的号码
    public String phoneNumber;

    // 发送短信的日期和时间
    public String date;

    // 发送短信人的姓名
    public String name;

    // 短信类型（1是接收，2是已发出）
    public String type;
}
