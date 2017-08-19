package com.yanhuahealth.healthlauncher.sys.verupd;

import com.yanhuahealth.healthlauncher.model.sys.upgrade.VersionInfo;

import java.io.Serializable;

/**
 * 版本下载任务记录
 */
public class VerDownloadTask implements Serializable {

    public static final int WAITING = 0;
    public static final int RUNNING = 1;
    public static final int CANCELED = 2;
    public static final int FINISHED = 3;

    public int taskId;
    public String taskName;
    public int progress;
    public int status;
    public String apkLocalPath;
    public VersionInfo versionInfo;

    @Override
    public String toString() {
        return "VerDownloadTask{" +
                "apkLocalPath='" + apkLocalPath + '\'' +
                ", taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", progress=" + progress +
                ", status=" + status +
                ", versionInfo=" + versionInfo +
                '}';
    }
}
