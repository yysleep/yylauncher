/**
 * @file MABTask.java
 */
package com.yanhuahealth.healthlauncher.sys.task;

import java.util.Map;

import android.util.Log;

/**
 * 任务调度层 中 每个任务的信息
 * 
 * @author steven
 */
public class LauncherTask
{
	// 日志 TAG
	private static String TAG = "LauncherTask";
	
	/**
	 * 任务ID
	 */
	private int taskId;
	
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
	 * 构造函数，只指定 taskId，无参数
	 */
	public LauncherTask(int taskId)
	{
		Log.d(TAG, "create task - " + taskId);
		this.taskId = taskId;
	}
	
	/**
	 * 构造函数
	 * 
	 * @param taskId
	 * @param taskParam
	 */
	public LauncherTask(int taskId, Map<String, Object> taskParam)
	{
		Log.d(TAG, "create task - " + taskId);
		
		this.taskId 	= taskId;
		this.taskParam	= taskParam;
		this.priority	= LauncherTaskPriority.NORMAL;
	}
	
	/**
	 * 构造函数
	 * 
	 * @param taskId
	 * @param taskParam
	 * @param priority
	 */
	public LauncherTask(int taskId, Map<String, Object> taskParam, LauncherTaskPriority priority, String activityName)
	{
		Log.d(TAG, "create task - " + taskId);
		
		this.taskId 	= taskId;
		this.taskParam	= taskParam;
		this.priority 	= priority;
		this.activityName = activityName;
	}

	/**
     * 
     * @return the taskId
     */
    public int getTaskId()
    {
    	return taskId;
    }

	/**
     * 
     * @param taskId the taskId to set
     */
    public void setTaskId(int taskId)
    {
    	this.taskId = taskId;
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
     * 
     * @return the taskParam
     */
    public Map<String, Object> getTaskParam()
    {
    	return taskParam;
    }

	/**
     * 
     * @param taskParam the taskParam to set
     */
    public void setTaskParam(Map<String, Object> taskParam)
    {
    	this.taskParam = taskParam;
    }

	/**
     * @return the activityName
     */
    public String getActivityName()
    {
    	return activityName;
    }

	/**
     * @param activityName the activityName to set
     */
    public void setActivityName(String activityName)
    {
    	this.activityName = activityName;
    }
}
