package com.yanhuahealth.healthlauncher.model.sys.appmgr;


/**
 * 工具、服务、家人、朋友
 */
public class ServerInfo {

    public int serverAppIcon;
    public String serverAppName;

    public ServerInfo(int bitmap, String serverAppName) {
        this.serverAppIcon = bitmap;
        this.serverAppName = serverAppName;
    }

}
