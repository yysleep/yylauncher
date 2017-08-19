package com.yanhuahealth.healthlauncher.model.sys;

/**
 * 接收到的消息信息，主要用于推送消息，也可以用于接收到短信并展现
 */
public class MessageInfo {

    // 标题
    public String title;

    // 内容
    public String content;

    public MessageInfo(String content, String title) {
        this.content = content;
        this.title = title;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "content='" + content + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
