package com.yanhuahealth.healthlauncher.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * 通用的 API 的响应结果的结构
 *
 * @author steven
 */
public class ApiResponseResult {

    // 响应结果码
    @Expose
    @SerializedName(value="result", alternate={"Ret", "ret"})
    private int result;

    // 响应内容
    @Expose
    @SerializedName(value = "data",alternate = {"Medias","medias","Informations"})
    private Map<String, Object> data;

    // 响应消息，主要在失败时返回
    @Expose
    @SerializedName("msg")
    private String msg;

    @Override
    public String toString() {
        return "ApiResponseResult{" +
                "result=" + result +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
