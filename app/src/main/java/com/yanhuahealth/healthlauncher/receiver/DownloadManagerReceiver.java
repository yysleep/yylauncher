package com.yanhuahealth.healthlauncher.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.ui.MainActivity;

import java.io.File;
import java.util.logging.Handler;

/**
 * Created by Administrator on 2016/3/23.
 */
public class DownloadManagerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            DownloadManager.Query query = new DownloadManager.Query();
            // 在广播中取出下载任务的id
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            query.setFilterById(id);
            Cursor c = downloadManager.query(query);
            if (c.moveToFirst()) {
                //获取文件下载路径
                String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                //如果文件名不为空，说明已经存在了，拿到文件名想干嘛都好
                if (filename != null) {
                    File apkfile = new File(filename);
                    if (!apkfile.exists()) {
                        Toast.makeText(context, "要安装的文件不存在，请检查路径", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (filename.endsWith(".apk")) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                                "application/vnd.android.package-archive");
                        context.startActivity(i);
                    }
                    if (filename.endsWith(".pdf")) {
                        Intent intentEbook=new Intent(LauncherConst.INTENT_ACTION_DOWN_EBOOK);
                        intentEbook.putExtra(LauncherConst.DOWNLOAD_PATH_EBOOK,filename);
                        context.sendBroadcast(intentEbook);
                    }
                }
            }
            c.close();
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
            long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            // 点击通知栏取消下载
            downloadManager.remove(ids);
            Toast.makeText(context, "已经取消下载", Toast.LENGTH_SHORT).show();
        }
    }
}
