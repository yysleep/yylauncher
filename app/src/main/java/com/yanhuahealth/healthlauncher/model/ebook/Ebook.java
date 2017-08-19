package com.yanhuahealth.healthlauncher.model.ebook;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 表示单独的电子书记录
 */
public class Ebook implements Comparable<Ebook>,Serializable {

    // 电子书标志（对应于平台侧的唯一标识）
    @Expose
    @SerializedName(value = "id",alternate = {"MediaId","mediaId"})
    public int id;

    // 电子书的名称
    @Expose
    @SerializedName(value = "name",alternate = {"Title","title"})
    public String name;

    // 分类标志
    @Expose
    @SerializedName("cat_id")
    public int catId;

    // 电子书的下载路径
    @Expose
    @SerializedName(value = "downloadUrl",alternate = {"MediaUrl","mediaUrl"})
    public String downloadUrl;

    // 缩略图的远程下载路径
    @Expose
    @SerializedName(value="thumbUrl",alternate = {"CoverUrl"})
    public String thumbUrl;

    // 电子书在本地的路径
    public String localPath;

    // 文件大小(单位：字节）
    @Expose
    @SerializedName(value = "size",alternate = {"MediaSize","mediaSize"})
    public long size;

    // 作者
    @Expose
    @SerializedName(value = "author",alternate = {"Owner","owner"})
    public String author;

    // 发布(或 出版)时间
    @Expose
    @SerializedName(value = "publishTime",alternate = {"CreateTime","createTime"})
    public String publishTime;

    // 文件额外属性
    @Expose
    @SerializedName(value="extraAttr", alternate={"MediaAttr"})
    public String extraAttr;

    // 下载任务标识
    // 由 DownloadManagers 分配的唯一标识
    public long downloadId;

    // 下载状态
    public static final int STATUS_INIT = 0;
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_DOWNLOAD_FAIL = 2;
    public static final int STATUS_DOWNLOAD_FINISH = 3;
    public static final int STATUS_DOWNLOAD_UNKNOWN = 4;
    public static final int STATUS_READING = 10;
    public int status;

    @Override
    public String toString() {
        return "Ebook{" +
                "author='" + author + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", catId=" + catId +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                ", localPath='" + localPath + '\'' +
                ", size=" + size +
                ", publishTime='" + publishTime + '\'' +
                ", extraAttr='" + extraAttr + '\'' +
                ", downloadId=" + downloadId +
                ", status=" + status +
                '}';
    }

    @Override
    public int compareTo(@NonNull Ebook another) {
        if (another.id <= 0) {
            return 1;
        }

        return this.id - another.id;
    }
}
