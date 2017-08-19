/**
 * @file MABTaskItem.java
 */
package com.yanhuahealth.healthlauncher.sys.task;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import android.util.Log;

/**
 * 任务调度层 中 每个任务的信息
 * 
 * @author steven
 */
public class LauncherTaskItem implements Serializable {
    
	private static String TAG = "LauncherTaskItem";

    /**
     * 任务类型ID
     */
    private int taskTypeId;
	
	/**
	 * 任务优先级
	 */
	private LauncherTaskPriority priority;
	
	/**
	 * 发起任务的 activity，如果非 activity，则为 null
	 */
	private String activityName;
	
	/**
	 * Task 任务参数
	 */
	private Map<String, Object> taskParam;
	
	/**
	 * 任务创建时间
	 */
	private Date createTime = new Date();
	
	/**
	 * 超时时长
	 */
	private long timeout = 60;
	
	/**
	 * 任务开始执行时间
	 */
	private Date startTime;
	
	/**
	 * 额外的需要重新发送给 refresh 的值列表
	 */
	private String extraParam;
	
	/**
	 * 构造函数，只指定 taskTypeId，无参数
	 */
	public LauncherTaskItem(int taskTypeId) {
		Log.d(TAG, "create task - " + taskTypeId);
		this.taskTypeId = taskTypeId;
	}
	
	/**
	 * 构造函数
	 * 
	 * @param taskTypeId
	 * @param taskParam
	 */
	public LauncherTaskItem(int taskTypeId, Map<String, Object> taskParam) {
		Log.d(TAG, "create task - " + taskTypeId);
		this.taskTypeId 	= taskTypeId;
		this.taskParam	= taskParam;
		this.priority	= LauncherTaskPriority.NORMAL;
	}
	
	/**
	 * 构造函数
	 * 
	 * @param taskTypeId
	 * @param taskParam
	 * @param priority
	 */
	public LauncherTaskItem(int taskTypeId, Map<String, Object> taskParam, LauncherTaskPriority priority, String activityName) {
		Log.d(TAG, "create task - " + taskTypeId);
		this.taskTypeId 	= taskTypeId;
		this.taskParam	= taskParam;
		this.priority 	= priority;
		this.activityName = activityName;
	}
	
	/**
     * 构造函数
     * 
     * @param taskTypeId
     * @param taskParam
     * @param priority
     */
    public LauncherTaskItem(int taskTypeId, Map<String, Object> taskParam,
                            String extraParam,
                            LauncherTaskPriority priority, String activityName) {
        Log.d(TAG, "create task - " + taskTypeId);
        this.taskTypeId     = taskTypeId;
        this.taskParam  = taskParam;
        this.extraParam = extraParam;
        this.priority   = priority;
        this.activityName = activityName;
    }

    public int getTaskTypeId() {
        return taskTypeId;
    }

    public void setTaskTypeId(int taskTypeId) {
        this.taskTypeId = taskTypeId;
    }

    /**
	 * @return the priority
	 */
	public LauncherTaskPriority getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(LauncherTaskPriority priority) {
		this.priority = priority;
	}

	/**
     * @return the taskParam
     */
    public Map<String, Object> getTaskParam() {
    	return taskParam;
    }

	/**
     * @param taskParam the taskParam to set
     */
    public void setTaskParam(Map<String, Object> taskParam) {
    	this.taskParam = taskParam;
    }

    /**
     * @return the extraParam
     */
    public String getExtraParam() {
        return extraParam;
    }

    /**
     * @param extraParam the extraParam to set
     */
    public void setExtraParam(String extraParam) {
        this.extraParam = extraParam;
    }

    /**
     * @return the activityName
     */
    public String getActivityName() {
    	return activityName;
    }

	/**
     * @param activityName the activityName to set
     */
    public void setActivityName(String activityName) {
    	this.activityName = activityName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LauncherTaskItem [taskTypeId=");
        builder.append(taskTypeId);
        builder.append(", priority=");
        builder.append(priority);
        builder.append(", activityName=");
        builder.append(activityName);
        builder.append(", taskParam=");
        builder.append(taskParam);
        builder.append(", createTime=");
        builder.append(createTime);
        builder.append(", timeout=");
        builder.append(timeout);
        builder.append(", startTime=");
        builder.append(startTime);
        builder.append(", extraParam=");
        builder.append(extraParam);
        builder.append("]");
        return builder.toString();
    }
}
