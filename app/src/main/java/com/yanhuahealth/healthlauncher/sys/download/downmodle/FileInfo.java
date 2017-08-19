package com.yanhuahealth.healthlauncher.sys.download.downmodle;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/17.
 */
public class FileInfo implements Serializable {

    public FileInfo() {
    }

    public FileInfo(int id, int type, String name, String downloadUrl, int fileLength, int finished) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.fileLength = fileLength;
        this.finished = finished;
    }

    // 暂时没啥用
    private int id;

    // 1 是apk  2是电子书 3是音乐
    private int type;

    // 文件名
    private String name;

    // 下载地址
    private String downloadUrl;

    // 文件的长度
    private int fileLength;

    // 完成进度
    private int finished;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileLength=" + fileLength +
                ", finished=" + finished +
                '}';
    }
}
