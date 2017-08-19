package com.yanhuahealth.healthlauncher.model.voicechannel;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 语音项记录
 */
public class VoiceItem implements Comparable<VoiceItem> {

    // 标识
    @Expose
    @SerializedName(value = "id",alternate = {"MediaId"})
    public int id;

    // 所属分类(2是讲座, 3是评书)
    @Expose
    @SerializedName(value = "cat_id")
    public int catId;

    @Override
    public String toString() {
        return "VoiceItem{" +
                "author='" + author + '\'' +
                ", id=" + id +
                ", catId=" + catId +
                ", extraAttr='" + extraAttr + '\'' +
                ", name='" + name + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                ", localPath='" + localPath + '\'' +
                ", size=" + size +
                ", timelen=" + timelen +
                ", publishTime='" + publishTime + '\'' +
                ", downloadId=" + downloadId +
                ", status=" + status +
                '}';
    }

    @Expose
    @SerializedName(value = "extraAttr", alternate = {"MediaAttr"})
    public String extraAttr;

    // 名称（一般取文件名）
    @Expose
    @SerializedName(value = "name",alternate = {"Title", "title"})
    public String name;

    // 下载路径
    @Expose
    @SerializedName(value = "downloadUrl", alternate = {"MediaUrl", "mediaUrl"})
    public String downloadUrl;

    // 缩略图下载路径
    @Expose
    @SerializedName(value = "thumbUrl", alternate = {"CoverUrl"})
    public String thumbUrl;

    // 本地路径
    @Expose
    @SerializedName("local_path")
    public String localPath;

    // 文件大小（单位：字节）
    @Expose
    @SerializedName(value = "size", alternate = {"MediaSize"})
    public long size;

    // 作者
    @Expose
    @SerializedName(value = "author",alternate = {"Owner","owner"})
    public String author;

    // 时长（单位：秒）
    @Expose
    @SerializedName("timelen")
    public long timelen;

    // 发布时间
    @Expose
    @SerializedName("publish_time")
    public String publishTime;

    // 下载任务标识
    // 由 DownloadManagers 分配的唯一标识
    public long downloadId;

    // 下载状态
    public static final int STATUS_INIT = 0;
    public static final int STATUS_DOWNLOADING = 1;

    // 下载失败
    public static final int STATUS_DOWNLOAD_FAIL = 2;

    // 下载完成
    public static final int STATUS_DOWNLOAD_FINISH = 3;

    // 下载状态未知
    public static final int STATUS_DOWNLOAD_UNKNOWN = 4;

    public static final int STATUS_READING = 10;
    public int status;

    @Override
    public int compareTo(@NonNull VoiceItem another) {
        if (another.id <= 0) {
            return 1;
        }
        return this.id - another.id;
    }
}
