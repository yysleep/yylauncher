/**
 * 
 */
package com.yanhuahealth.healthlauncher.sys;

import java.util.Map;

/**
 * 所有实现该接口的 activity 或 Fragment 等组件
 * 在 task 处理完后会回调该接口中的 refresh 实现来更新 UI
 * 
 * @author steven
 */
public interface ITaskCallback {
    
    /**
     * 通知相应的 Activity 或 Fragment 等 UI 组件进行刷新
     * 
     * @param       taskTypeId  为对应的任务类型
     * @param       params      当前支持的返回参数主要包括
     *
     */
    boolean refresh(int taskTypeId, Map<String, Object> params);
}
