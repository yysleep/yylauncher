package com.yanhuahealth.healthlauncher.model.sms;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/4/29.
 */
public class ImSmsInfo {

    // IM短信内容
    @Expose
    @SerializedName(value = "version",alternate = "Version")
    // im信息版本
    public String version;

    @Expose
    @SerializedName(value = "from",alternate = "From")
    public String from;

    public String extra;

    @Expose
    @SerializedName(value = "url",alternate = "Url")
    // im信息图片或者视频地址
    public String url;

    @Expose
    @SerializedName(value = "is_group",alternate = "Is_group")
    public String is_group;

    @Expose
    @SerializedName(value = "to",alternate = "To")
    public String to;

    @Expose
    @SerializedName(value = "sendTime",alternate = "Sendtime")
    public String sendTime;

    @Expose
    @SerializedName(value = "messageid",alternate = "Messageid")
    public String messageid;

    @Expose
    @SerializedName(value = "type",alternate = "Type")
    // im信息类型
    public String type;

    @Expose
    @SerializedName(value = "body",alternate = "Body")
    // im信息内容
    public String body;

    @Expose
    @SerializedName(value = "time",alternate = "Time")
    // im信息时间
    public String time;

    @Override
    public String toString() {
        return "ImSmsInfo{" +
                "version='" + version + '\'' +
                ", from='" + from + '\'' +
                ", extra='" + extra + '\'' +
                ", url='" + url + '\'' +
                ", is_group='" + is_group + '\'' +
                ", to='" + to + '\'' +
                ", sendTime='" + sendTime + '\'' +
                ", messageid='" + messageid + '\'' +
                ", type='" + type + '\'' +
                ", body='" + body + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
