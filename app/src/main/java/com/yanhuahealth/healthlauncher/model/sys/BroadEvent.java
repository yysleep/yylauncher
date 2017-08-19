package com.yanhuahealth.healthlauncher.model.sys;

import java.util.Map;

/**
 * 内部通信的广播事件
 */
public class BroadEvent {

    /**
     * 事件类型，类型定义在 EventType 中
     */
    public int eventType;

    /**
     * 不同事件类型携带的事件信息不同
     * 事件的 KEY 定义在 EventType 中
     */
    public Map<String, Object> eventInfo;

    public BroadEvent(int eventType, Map<String, Object> eventInfo) {
        this.eventInfo = eventInfo;
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "BroadEvent{" +
                "eventType=" + eventType +
                '}';
    }
}
