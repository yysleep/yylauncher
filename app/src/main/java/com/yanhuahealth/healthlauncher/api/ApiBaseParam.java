/**
 * 
 */
package com.yanhuahealth.healthlauncher.api;


/**
 * API 接口的基本参数信息
 * 
 * @author steven
 */
public class ApiBaseParam {

    private static final String TAG = ApiBaseParam.class.getName();

    // 分配的应用 ID
    private int appId;
    
    // 分配的应用 token
    private String appToken;
    
    // 分配的会话 token
    private String sessionToken;
    
    // 用户标识
    private int userId;

    /**
     * @return the appId
     */
    public int getAppId() {
        return appId;
    }
    
    /**
     * @param appId the appId to set
     */
    public void setAppId(int appId) {
        this.appId = appId;
    }

    /**
     * @return the appToken
     */
    public String getAppToken() {
        return appToken;
    }

    /**
     * @param appToken the appToken to set
     */
    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    /**
     * @return the sessionToken
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * @param sessionToken the sessionToken to set
     */
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    /**
     * @return the userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ApiBaseParam{" +
                "appId=" + appId +
                ", appToken='" + appToken + '\'' +
                ", sessionToken='" + sessionToken + '\'' +
                ", userId=" + userId +
                '}';
    }
}
