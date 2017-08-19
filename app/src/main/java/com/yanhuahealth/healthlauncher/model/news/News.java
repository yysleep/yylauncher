package com.yanhuahealth.healthlauncher.model.news;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 单条新闻纪录
 */
public class News implements Serializable{
    // 新闻详细内容的URI
    @Expose
    @SerializedName(value = "uri",alternate = "HtmlUrl")
    public String uri;

    @Expose
    @SerializedName(value = "id", alternate = "InfoId")
    public int id;

    // 新闻标题
    @Expose
    @SerializedName(value = "title",alternate = {"Title"})
    public String title;

    // 摘要
    @Expose
    @SerializedName(value = "summary",alternate = {"Digest"})
    public String summary;

    // 新闻创建时间
    @Expose
    @SerializedName(value = "createTime",alternate = {"CreateTime"})
    public String createTime;

    // 小屋名字
    @Expose
    @SerializedName("houseName")
    public String houseName;

    // 显示的图片，对应的1,2,3
    @Expose
    @SerializedName(value = "attach1",alternate = {"DigestUrl1"})
    public String attach1;

    @Expose
    @SerializedName(value = "attach2",alternate = {"DigestUrl2"})
    public String attach2;

    @Expose
    @SerializedName(value = "attach3",alternate = {"DigestUrl3"})
    public String attach3;

    @Override
    public String toString() {
        return "News{" +
                "attach1='" + attach1 + '\'' +
                ", uri='" + uri + '\'' +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", createTime='" + createTime + '\'' +
                ", houseName='" + houseName + '\'' +
                ", attach2='" + attach2 + '\'' +
                ", attach3='" + attach3 + '\'' +
                '}';
    }

    public String getAttach1() {
        return attach1;
    }

    public void setAttach1(String attach1) {
        this.attach1 = attach1;
    }

    public String getAttach2() {
        return attach2;
    }

    public void setAttach2(String attach2) {
        this.attach2 = attach2;
    }

    public String getAttach3() {
        return attach3;
    }

    public void setAttach3(String attach3) {
        this.attach3 = attach3;
    }
}
