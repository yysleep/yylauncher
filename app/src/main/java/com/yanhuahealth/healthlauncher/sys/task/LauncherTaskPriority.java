/**
 * @file MABTaskPriority.java
 * @author steven
 */
package com.yanhuahealth.healthlauncher.sys.task;

/**
 * 表示每个任务的优先级
 * 
 * @author steven
 */
public enum LauncherTaskPriority
{
	/**
	 * 当前只定义了5个优先级，分别对应：
	 * 
	 * 最高，高于正常，正常，低于正常，最低
	 */
	MAX(12), ABOVE_NORMAL(10), NORMAL(8), BELOW_NORMAL(6), MIN(4);
	
	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}
	
	private LauncherTaskPriority(int val)
	{
		priority = val;
	}
	
	/**
	 * <b>优先级调整</b></br>
	 * 
	 * 这里的 niceval 对应于需要调整的级别数，而不是具体的值
	 * 
	 * @param 	niceval		调整的级别值
	 * @return
	 */
	public boolean adjust(int niceval)
	{
		if (0 == niceval)
		{
			return true;
		}
		
		if ((this.priority == LauncherTaskPriority.MAX.getPriority() && niceval > 0)
			|| (this.priority == LauncherTaskPriority.MIN.getPriority() && niceval < 0))
		{
			return false;
		}
		
		this.priority += 2 * niceval;
		if (this.priority > LauncherTaskPriority.MAX.getPriority())
		{
			this.priority = LauncherTaskPriority.MAX.getPriority();
		}
		else if (this.priority < LauncherTaskPriority.MIN.getPriority())
		{
			this.priority = LauncherTaskPriority.MIN.getPriority();
		}
		
		return true;
	}

	private int priority = 0;
}
