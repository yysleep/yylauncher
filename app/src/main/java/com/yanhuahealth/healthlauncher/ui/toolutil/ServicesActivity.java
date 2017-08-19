package com.yanhuahealth.healthlauncher.ui.toolutil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutStyle;
import com.yanhuahealth.healthlauncher.model.sys.upgrade.VersionInfo;
import com.yanhuahealth.healthlauncher.ui.base.ShortcutBoxView;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 服务
 */
public class ServicesActivity extends SecondActivity {

    private DialogUtil dialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addShortcut(HealthManager());
        dialogManager = new DialogUtil(this, new DialogUtil.OnDialogUtilListener() {
            @Override
            public void onClick(View view) {

                // 执行下载安装健康管家包
                jumpToHealthManager();
                dialogManager.cancel();
            }
        });

        // 添加默认项
       /* for (int i = 1; i < 16; i++) {
            addShortcut(DefaultApp(i));
        }*/
    }

    // 服务列表健康管家项
    public Shortcut HealthManager() {
        Shortcut shortcutHealthManager = new Shortcut(
                1, 0, "健康管家", ShortcutType.YH_NEWS, false);
        shortcutHealthManager.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        ShortcutBoxView shortcutBoxView = getShortcutBoxView(0);
        shortcutBoxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentManager;
                if (ServicesActivity.this.getPackageManager().getLaunchIntentForPackage("net.uniontest.fhm") != null) {
                    intentManager = ServicesActivity.this.getPackageManager().getLaunchIntentForPackage("net.uniontest.fhm");
                    startActivity(intentManager);
                } else {
                    dialogManager.showFirstStyleDialog("下载应用", "下载健康管家", "确定");
                }
            }
        });
        shortcutHealthManager.icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_album);
        shortcutHealthManager.style = new ShortcutStyle(R.drawable.shortcut_bg_second, R.color.white);
        return shortcutHealthManager;
    }

    // 等待添加服务项
    public Shortcut DefaultApp(int pos){
        Shortcut shortcutDefaultApp = new Shortcut(
                1, pos, "敬请期待", ShortcutType.DEFAULT, false);
        shortcutDefaultApp.icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_add);
        shortcutDefaultApp.style = new ShortcutStyle(R.color.default_box_bg, R.color.white);
        return shortcutDefaultApp;
    }

    @Override
    public String getNewTitle() {

        return "服务";
    }

    // 下载健康管家
    private void jumpToHealthManager() {

        DownloadVerTask downloadVerTask = new DownloadVerTask(this);
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.appId = LauncherConst.APP_ID;
        versionInfo.pkgSize = 10737202;
        versionInfo.pkgUrl = "http://www.laoyou99.cn/app/3/36/Medical.apk";
        downloadVerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, versionInfo);
    }

    // 正在下载安装包的进度框
    private ProgressDialog progDlgDownloadApk;

    /**
     * 安装包文件下载任务
     *
     * @author steven
     */
    class DownloadVerTask extends AsyncTask<VersionInfo, Integer, Integer> {

        private VersionInfo versionInfo;
        private String apkPath;
        private int currDownSize = 0;
        private Context context;

        public DownloadVerTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progDlgDownloadApk = new ProgressDialog(context);
            progDlgDownloadApk.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progDlgDownloadApk.setTitle("正在下载安装包");
            progDlgDownloadApk.show();
        }

        @Override
        protected Integer doInBackground(VersionInfo... params) {

            versionInfo = params[0];
            progDlgDownloadApk.setMax(versionInfo.pkgSize);

            // 首先确定本地安装了 SDCARD 来存储下载文件
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return -1;
            }

            OkHttpClient httpClient = new OkHttpClient();

            Request request = new Request.Builder().url(versionInfo.pkgUrl).build();
            try {
                Response response = httpClient.newCall(request).execute();
                InputStream is = response.body().byteStream();
                BufferedInputStream inputStream = new BufferedInputStream(is);

                apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HealthManager.apk";
                OutputStream outputStream = new FileOutputStream(apkPath);

                byte[] data = new byte[4096];
                int len;
                while ((len = inputStream.read(data)) != -1) {
                    currDownSize += len;
                    outputStream.write(data, 0, len);
                    publishProgress(currDownSize);
                }

                YHLog.i(tag(), "DownloadVerTask - file size: " + currDownSize);

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                YHLog.w(tag(), "DownloadVerTask - exception execute http: " + e.getMessage());
                return -1;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (progDlgDownloadApk != null && progDlgDownloadApk.isShowing()) {
                progDlgDownloadApk.cancel();
            }

            if (result != 0) {
                Toast.makeText(context, "抱歉，下载安装包失败，请稍后重试", Toast.LENGTH_LONG).show();
                return ;
            }

            // 下载成功后自动安装
            installApk(context, apkPath);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progDlgDownloadApk.setProgress(values[0]);
        }
    }

    /**
     * 安装 APK
     */
    private void installApk(Context context, String apkPath) {
        File fileApk = new File(apkPath);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(fileApk), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
