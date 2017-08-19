package com.yanhuahealth.healthlauncher.model.sms;

import com.yanhuahealth.healthlauncher.model.note.SmsInfo;

import java.util.ArrayList;

/**
 * 和指定用户的短信交互记录
 */
public class UserSmsRecord {

    // 存储的为本地库中的与某一用户的交互记录的唯一标识
    public int threadId;

    // 对方的姓名（display name)
    public String userName;

    // 对方的用户电话号码
    public String userNumber;

    // 当前未读短信数
    public int unreadCount;

    // 和该用户的短信交互记录列表
    // 按时间顺序倒序排列
    public ArrayList<SmsInfo> smsList;

    @Override
    public String toString() {
        return "UserSmsRecord{" +
                "threadId=" + threadId +
                ", userName='" + userName + '\'' +
                ", userNumber='" + userNumber + '\'' +
                ", unreadCount=" + unreadCount +
                ", smsList=" + smsList +
                '}';
    }
}
