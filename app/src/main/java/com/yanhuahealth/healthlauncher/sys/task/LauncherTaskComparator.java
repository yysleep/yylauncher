/**
 * @file MABTaskComparator.java
 * @author steven
 */
package com.yanhuahealth.healthlauncher.sys.task;

import java.util.Comparator;

/**
 * <b>task比较器，用于task优先级队列</b></br>
 * 
 * 主要按照 MABTaskItem 中的优先级的高低进行比较
 * 
 * @author steven
 */
public class LauncherTaskComparator implements Comparator<LauncherTaskItem>
{
	@Override
    public int compare(LauncherTaskItem lhs, LauncherTaskItem rhs)
    {
		if (lhs.getPriority().getPriority() > rhs.getPriority().getPriority())
		{
			return -1;
		}
		
		return 0;
    }
}
