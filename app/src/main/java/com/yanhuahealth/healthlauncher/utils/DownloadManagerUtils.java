package com.yanhuahealth.healthlauncher.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import com.yanhuahealth.healthlauncher.common.LauncherConst;

import java.net.URLDecoder;
import java.net.URLEncoder;


/**
 * Created by Administrator on 2016/3/23.
 */
public class DownloadManagerUtils {

    long downloadId = 0;
    public long currDownloadId;

    private DownloadManagerUtils() {

    }

    private static volatile DownloadManagerUtils instance ;

    public static DownloadManagerUtils getInstance() {
        if(instance==null){
            synchronized (DownloadManagerUtils.class){
                if (instance == null) {
                    instance = new DownloadManagerUtils();
                }
            }
        }
        return instance;
    }

    /**
     * path 指定为yhlauncher 下的某个文件夹
     *
     * @return 返回下载 ID
     *          -1   表示已经在下载队列中
     */
    public long startDownloadApk(Context context, String fileName, Handler handler, String url, String path) {


            return download(context, handler, url, fileName, path);

    }

    private long download(Context context, Handler handler, String url, String fileName, String path) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = null;
        try {
            String urlss = Uri.encode(url, "-![.:/,%?&=]");
            request = new DownloadManager.Request(Uri.parse(urlss));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (request == null) {
            return 0;
        }

        Toast.makeText(context, "已经开始下载", Toast.LENGTH_SHORT).show();

        // 仅允许在WIFI连接情况下下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

        // 在通知栏中显示
        request.setVisibleInDownloadsUi(true);

        // 通知栏中将出现的内容
        if (fileName != null) {
            request.setTitle(fileName);
            request.setDescription("正在下载: " + fileName);
        } else {
            request.setTitle("下载文件中");
            request.setDescription("正在下载文件");
        }

        // 下载过程和下载完成后通知栏有通知消息。
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 下载完成后的存放路径
        request.setDestinationInExternalPublicDir("yhlauncher/" + path, fileName);

        // enqueue 开始启动下载...
        currDownloadId = downloadManager.enqueue(request);
        if (currDownloadId > 0 && handler != null) {
            query(context, downloadManager, handler);
        } else {
            // 如果没有指定跟踪下载进度的 handler
            // 则重置下载标识为 0
            downloadId = 0;
        }

        downloadId = currDownloadId;

        return currDownloadId;
    }

//    public long getDownloadId() {
//        return downloadId;
//    }

    // 查询下载进度，文件总大小多少，已经下载多少
    private void query(Context context, final DownloadManager downloadManager, final Handler handler) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stopThread = false;
                while (downloadId > 0 && !stopThread) {
                    DownloadManager.Query downloadQuery = new DownloadManager.Query();
                    downloadQuery.setFilterById(downloadId);
                    Cursor cursor = downloadManager.query(downloadQuery);
                    if (cursor != null && cursor.moveToFirst()) {
                        int fileName = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        int fileUri = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
                        String fn = cursor.getString(fileName);
                        String fu = cursor.getString(fileUri);

                        int totalSizeBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                        int bytesDownloadSoFarIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);

                        // 下载的文件总大小
                        int allSize = cursor.getInt(totalSizeBytesIndex);
                        int downSize = cursor.getInt(bytesDownloadSoFarIndex);

                        int pro = (int) (((float) downSize / allSize) * 100);
                        Log.d(this.getClass().getName(),
                                "from " + fu + " 下载到本地 " + fn + " 文件总大小:" + allSize + " 已经下载:" + downSize);
                        Message message = new Message();
                        message.arg1 = pro;
                        message.what = 100;
                        handler.sendMessage(message);
                        if (pro == 100) {
                            stopThread = true;
                            downloadId = 0;
                        }
                        Log.i("tagnew", "截止目前已经下载的文件百分比" + pro + "%");

                        cursor.close();
                    }
                }
            }
        }).start();
    }

    public void cancelDownload(Context context) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadId > 0) {
            Cursor c = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
            if (c != null && c.moveToFirst()) {
                int state = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (state != DownloadManager.STATUS_FAILED && state != DownloadManager.STATUS_SUCCESSFUL) {
                    // 如果文件已经下载完成，remove命令并不会删除文件
                    downloadManager.remove(downloadId);
                    downloadId = 0;
                }
                c.close();
            }
        }
    }
}
