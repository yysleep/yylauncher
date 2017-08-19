package com.yanhuahealth.healthlauncher.sys;

import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;

/**
 * 事件处理器
 */
public interface IEventHandler {

    // 触发指定事件后的通知处理
    void notify(BroadEvent event);
}
