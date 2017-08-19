package com.yanhuahealth.healthlauncher.sys.download.dbservice;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.sys.download.downthread.DownTask;
import com.yanhuahealth.healthlauncher.sys.download.downmodle.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/3/17.
 */
public class DownloadManagers extends Service {

    public final static String DOWNLOAD_ACTION__START = "DOWNLOAD_START";
    public final static String DOWNLOAD_ACTION_STOP = "DOWNLOAD_STOP";
    public final static String DOWNLOAD_ACTION_UPDATE = "DOWNLOAD_ACTION_UPDATE";
    public final static String DOWNLOAD_ACTION_FINISHED = "DOWNLOAD_ACTION_FINISHED";
    final int APK = 1;
    final int EBOOK = 2;
    final int MUSIC = 3;
    final int MSG_INIT = 111;
    // 下载任务的集合
    private Map<Integer, DownTask> mpTaks = new LinkedHashMap<Integer, DownTask>();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    // 获得将要下载的任务文件 准备下载
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    // 启动下载任务 3 代码三个线程下载一个任务
                    DownTask downTask = new DownTask(DownloadManagers.this, fileInfo, 3);
                    downTask.downLoad();
                    // 把下载任务添加到集合中
                    mpTaks.put(fileInfo.getId(), downTask);

                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(DOWNLOAD_ACTION__START)) {
            // 接收到下载命令 初始化下载线程
            FileInfo startfileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            if (startfileInfo != null) {
                new InitThread(startfileInfo).start();
            }

        } else if (intent.getAction().equals(DOWNLOAD_ACTION_STOP)) {
            // 暂停下载
            FileInfo stopfileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            // 从集合中取出下载任务
            DownTask task = mpTaks.get(stopfileInfo.getId());
            if (task != null) {
                // 停止下载
                task.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class InitThread extends Thread {

        private FileInfo fileInfo;

        private InitThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection con = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(fileInfo.getDownloadUrl());
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(3000);
                con.setRequestMethod("GET");
                int length = -1;
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // 得到文件长度
                    length = con.getContentLength();
                }
                if (length < 0) {
                    return;
                }
                File file = new File(getPath(fileInfo.getType()), fileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                fileInfo.setFileLength(length);
                handler.obtainMessage(MSG_INIT, fileInfo).sendToTarget();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                try {
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public String getPath(int type) {
        String path;
        switch (type) {
            case APK:
                path = LauncherConst.getApkRootPath();
                break;

            case EBOOK:
                path = LauncherConst.getEbookRootPath();
                break;

            case MUSIC:
                path = LauncherConst.getVoiceRootPath();
                break;

            default:
                path = Environment.getExternalStorageDirectory().getAbsolutePath();
                break;
        }
        return path;
    }

}
