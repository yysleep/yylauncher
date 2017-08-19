package com.yanhuahealth.healthlauncher.sys.verupd;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.YHLog;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 版本下载服务
 */
public class VerDownloadService extends Service {

    protected String tag() {
        return VerDownloadService.class.getName();
    }

    // 用于广播 下载进度 以及 下载完成 的通知
    public final static String ACTION_UPDATE = "com.yanhuahealth.healthlauncher.service.UPDATE";
    public final static String ACTION_FINISHED = "com.yanhuahealth.healthlauncher.service.FINISHED";

    // 广播通知的参数
    // 传入整个任务信息
    public static final String PARAM_TASK = "task";

    private ServiceBinder binder;
    private List<VerDownloadTask> downloadTaskQueue;
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        YHLog.i(tag(), "onBind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        YHLog.i(tag(), "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        YHLog.i(tag(), "onCreate");

        binder = new ServiceBinder();
        downloadTaskQueue = new ArrayList<>();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setProgress(100, 0, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        YHLog.i(tag(), "onDestroy");

        binder = null;
        downloadTaskQueue = null;
        notificationManager = null;
        notificationBuilder = null;
    }

    public class ServiceBinder extends Binder {

        public VerDownloadService getService() {
            return VerDownloadService.this;
        }
    }

    public void addTask(VerDownloadTask task) {
        if (task == null) {
            return ;
        }

        if (downloadTaskQueue != null) {
            downloadTaskQueue.add(task);
            YHLog.d(tag(), "add task - " + task);

            if (!isRunning && downloadTaskQueue.size() > 0) {
                startDownload();
            }
        }
    }

    private void startDownload() {
        if (isRunning) {
            return ;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (downloadTaskQueue != null && downloadTaskQueue.size() > 0) {
                    isRunning = true;

                    // 首先确定本地安装了 SDCARD 来存储下载文件
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        YHLog.e(tag(), "no have External Storage");
                        return ;
                    }

                    VerDownloadTask task = downloadTaskQueue.get(0);

                    // 获取 安装包 的文件名
                    String [] pkgSeg = task.versionInfo.pkgUrl.split("/");
                    if (pkgSeg.length < 1) {
                        continue;
                    }

                    String apkFileName = pkgSeg[pkgSeg.length - 1];

                    // 开始下载，边下载边发布进度
                    OkHttpClient httpClient = new OkHttpClient();
                    Request request = new Request.Builder().url(task.versionInfo.pkgUrl).build();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        InputStream is = response.body().byteStream();
                        BufferedInputStream inputStream = new BufferedInputStream(is);

                        task.apkLocalPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/" + apkFileName;
                        OutputStream outputStream = new FileOutputStream(task.apkLocalPath);

                        byte[] data = new byte[4096];
                        int len;
                        int currDownSize = 0;
                        int publishProgressCount = 1;
                        while ((len = inputStream.read(data)) != -1 && task.status != VerDownloadTask.CANCELED) {
                            currDownSize += len;
                            outputStream.write(data, 0, len);
                            task.status = VerDownloadTask.RUNNING;

                            if (currDownSize >= (task.versionInfo.pkgSize * 0.01 * publishProgressCount)) {
                                ++publishProgressCount;
                                task.progress += 1;
                                Message message = handler.obtainMessage(DOWNLOAD_STATUS_UPDATE, task);
                                handler.sendMessage(message);
                            }
                        }

                        YHLog.i(tag(), "DownloadTask - file size: " + currDownSize);

                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();

                        if (task.status == VerDownloadTask.CANCELED) {
                            YHLog.w(tag(), "DownloadTask - cancel task - " + task);
                        } else {
                            Message message = handler.obtainMessage(DOWNLOAD_STATUS_SUCCESS, task);
                            handler.sendMessage(message);
                        }
                    } catch (IOException e) {
                        YHLog.w(tag(), "DownloadTask - exception execute http: " + e.getMessage());
                        Message message = handler.obtainMessage(DOWNLOAD_STATUS_FAILED, task);
                        handler.sendMessage(message);
                    }

                    if (downloadTaskQueue != null) {
                        downloadTaskQueue.remove(task);
                    }
                }
            }
        }).start();
    }

    // 发送下载完成的广播通知
    private void sendNotify(VerDownloadTask task) {
        if (task == null) {
            return ;
        }

        Intent intent;
        if (task.progress >= 100) {
            intent = new Intent(ACTION_FINISHED);
        } else {
            intent = new Intent(ACTION_UPDATE);
        }

        intent.putExtra(PARAM_TASK, task);
        sendBroadcast(intent);

        if (task.progress >= 100) {
            notificationManager.cancel(NOTIFY_ID);
        }
    }

    public static final int NOTIFY_ID = 1001;

    private static final int DOWNLOAD_STATUS_UPDATE = 0;
    private static final int DOWNLOAD_STATUS_SUCCESS = 1;
    private static final int DOWNLOAD_STATUS_FAILED = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DOWNLOAD_STATUS_UPDATE:
                {
                    VerDownloadTask task = (VerDownloadTask) msg.obj;
                    notificationBuilder.setContentText(String.format("正在下载中(%d%%)", task.progress));
                    notificationBuilder.setProgress(100, task.progress, false);
                    notificationManager.notify(NOTIFY_ID, notificationBuilder.build());
                    task.status = VerDownloadTask.RUNNING;
                    sendNotify(task);
                    break;
                }

                case DOWNLOAD_STATUS_SUCCESS:
                {
                    VerDownloadTask task = (VerDownloadTask) msg.obj;
                    notificationBuilder.setContentText("下载完成");
                    notificationBuilder.setProgress(100, 100, false);
                    notificationManager.notify(NOTIFY_ID, notificationBuilder.build());
                    task.status = VerDownloadTask.FINISHED;
                    sendNotify(task);
                    break;
                }

                case DOWNLOAD_STATUS_FAILED:
                {
                    VerDownloadTask task = (VerDownloadTask) msg.obj;
                    notificationBuilder.setContentText("下载失败");
                    notificationBuilder.setProgress(0, 0, false);
                    notificationManager.notify(NOTIFY_ID, notificationBuilder.build());
                    task.status = VerDownloadTask.CANCELED;
                    sendNotify(task);
                    break;
                }
            }

            super.handleMessage(msg);
        }
    };
}
