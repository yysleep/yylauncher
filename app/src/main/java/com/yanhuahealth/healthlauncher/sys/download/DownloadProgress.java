package com.yanhuahealth.healthlauncher.sys.download;

/**
 * 下载进度信息
 */
public class DownloadProgress {

    // 下载进度百分比
    public int percent;

    // 已下载的字节数
    public long downloadedSize;

    // 已下载时长（单位：秒）
    public long downloadedTime;

    @Override
    public String toString() {
        return "DownloadProgress{" +
                "percent=" + percent +
                ", downloadedSize=" + downloadedSize +
                ", downloadedTime=" + downloadedTime +
                '}';
    }
}
