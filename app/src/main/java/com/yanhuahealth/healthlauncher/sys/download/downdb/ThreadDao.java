package com.yanhuahealth.healthlauncher.sys.download.downdb;

import com.yanhuahealth.healthlauncher.sys.download.downmodle.ThreadInfo;

import java.util.List;

/**
 * 数据访问接口
 */
public interface ThreadDao {

    // 插入线程信息
    public void insertThread(ThreadInfo threadInfo);

    // 删除线程信息
    public void deleteThread(String url);

    // 更新线程下载信息
    public void updateThread(String url, int thread_id, int finished);


    // 查询文件的线程信息
    public List<ThreadInfo> getThreads(String fileurl);

    // 线程信息是否存在
    public boolean isExists(String url,int thread_id);


}
