package com.yanhuahealth.healthlauncher.sys.verupd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.api.ApiConst;
import com.yanhuahealth.healthlauncher.api.ApiResponseResult;
import com.yanhuahealth.healthlauncher.api.HealthApi;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.upgrade.VersionInfo;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.utils.DownloadManagerUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 负责版本检测升级和安装
 *
 * @author steven
 */
public class VersionUpgrade {

    protected String tag() {
        return VersionUpgrade.class.getName();
    }

    private static VersionUpgrade instance = new VersionUpgrade();

    public static VersionUpgrade getInstance() {
        return instance;
    }

    private static Gson gson
            = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    // 系统当前版本信息
    public static VersionInfo CURR_VER = new VersionInfo();

    static {
        CURR_VER.appId = LauncherConst.APP_ID;
        CURR_VER.osId = ApiConst.OS_ANDROID;
    }

    /**
     * 返回应用当前版本信息
     */
    public static VersionInfo getCurrVer(Context context) {

        PackageInfo packInfo;
        try {
            packInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        CURR_VER.verCode = packInfo.versionCode;
        CURR_VER.verName = packInfo.versionName;
        CURR_VER.sdkVer = Build.VERSION.SDK_INT;
        return CURR_VER;
    }

    // 用于定时获取最新版本的定时器
    private Handler handlerSyncTimer = new Handler();

    class CheckVerTimerRunnable implements Runnable {

        private Activity context;
        private long period;

        public CheckVerTimerRunnable(Activity context, long period) {
            this.context = context;
            this.period = period;
        }

        @Override
        public void run() {
            // 默认晚上 10 点 ~ 早上 5 点不自动提示
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            if (hour >= 22 || hour < 5) {
                handlerSyncTimer.postDelayed(this, period);
                return;
            }

            if (VersionUpgrade.getInstance().checkVersion(context, false, null)) {
                handlerSyncTimer.postDelayed(this, period);
            }
        }
    }

    /**
     * 启动线程来定期检查是否有更新
     */
    public void startCheckTimer(final Activity context, long delay, long period) {
        handlerSyncTimer.postDelayed(new CheckVerTimerRunnable(context, period), delay);
    }

    /**
     * 通知回调接口
     */
    public interface INewVerNotify {
        /**
         * 当前已经为最新版本
         */
        void alreadyLatest();

        /**
         * 新版本通知回调接口
         * 如果有最新版本，则 newVer 为最新版本信息
         * 否则为 null
         */
        void newVerNotify(VersionInfo newVer);

        /**
         * 用户取消升级后的回调接口
         */
        void cancelUpdate(VersionInfo newVer);
    }

    // 当前版本已经为最新版本
    private static final int CHECK_VER_ALREADY_LATEST = 100;

    // 有最新版本
    private static final int CHECK_VER_HAS_NEW = 101;

    /***
     * 用于在检测到新版本后通知给主线程
     */
    private static class HandlerCheckVer extends Handler {

        private final WeakReference<Activity> activityWeakReference;
        private INewVerNotify newVerNotify;
        private boolean dontAlert;

        public HandlerCheckVer(Activity activity, boolean dontAlert, INewVerNotify notify) {
            activityWeakReference = new WeakReference<>(activity);
            this.newVerNotify = notify;
            this.dontAlert = dontAlert;
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = activityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                switch (msg.what) {
                    case CHECK_VER_ALREADY_LATEST:
                        if (newVerNotify != null) {
                            newVerNotify.alreadyLatest();
                        }
                        break;

                    case CHECK_VER_HAS_NEW:
                        VersionInfo newVer = (VersionInfo) msg.obj;
                        if (newVer != null) {
                            // 显示最新版本信息的对话框
                            // 并提示是否需要升级
                            VersionUpgrade.getInstance().showVersionDlg(activity, newVer, dontAlert);
                            if (newVerNotify != null) {
                                newVerNotify.newVerNotify(newVer);
                            }
                        }
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    // 检测版本的线程
    private class CheckVerRunnable implements Runnable {

        private VersionInfo currVer;
        private HandlerCheckVer handlerCheckVer;

        public CheckVerRunnable(VersionInfo currVer, HandlerCheckVer handlerCheckVer) {
            this.currVer = currVer;
            this.handlerCheckVer = handlerCheckVer;
        }

        @Override
        public void run() {
            YHLog.d(tag(), "CheckVerRunnable - 正在获取最新版本 - 当前版本: " + currVer);
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put(ApiConst.PARAM_OS_ID, String.valueOf(currVer.osId));
            reqParams.put(ApiConst.PARAM_SDK_VER, String.valueOf(currVer.sdkVer));
            reqParams.put(ApiConst.PARAM_SITE_ID, "1");
            reqParams.put(ApiConst.PARAM_VER_CODE, String.valueOf(currVer.verCode));
            ApiResponseResult apr = HealthApi.checkVer(MainService.getInstance().apiBaseParam, reqParams);
            if (apr == null || apr.getResult() != 0) {
                YHLog.w(tag(), "CheckVerRunnable - check ver api failed");
                return;
            } else if (apr.getData() == null) {
                YHLog.w(tag(), "CheckVerRunnable - already latest version");
                handlerCheckVer.sendEmptyMessage(CHECK_VER_ALREADY_LATEST);
                return;
            }

            String jsonVerInfo = gson.toJson(apr.getData());
            VersionInfo newVer = gson.fromJson(jsonVerInfo,
                    new TypeToken<VersionInfo>() {
                    }.getType());

            if (newVer == null
                    || newVer.pkgUrl == null || newVer.pkgUrl.length() == 0 || !newVer.pkgUrl.startsWith("http")
                    || newVer.verDesc == null || newVer.verDesc.length() == 0
                    || newVer.verCode <= 0
                    || newVer.pkgSize <= 0) {
                YHLog.w(tag(), "CheckVerRunnable - new version param null");
                handlerCheckVer.sendEmptyMessage(CHECK_VER_ALREADY_LATEST);
                return;
            }

            handlerCheckVer.sendMessage(handlerCheckVer.obtainMessage(CHECK_VER_HAS_NEW, newVer));
        }
    }

    /**
     * 启用 独立线程 来检测版本更新，以及使用 handler 机制来通知 UI
     *
     * @return true 表示启动检测线程成功，false 表示启动检测线程失败
     */
    public boolean checkVersion(final Activity activity,
                                final boolean dontAlert,
                                final INewVerNotify notify) {

        if (activity == null || activity.isFinishing()) {
            return false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                return false;
            }
        }

        HandlerCheckVer handlerCheckVer = new HandlerCheckVer(activity, dontAlert, notify);

        // 启用独立线程去检测版本更新
        new Thread(new CheckVerRunnable(getCurrVer(activity), handlerCheckVer)).start();

        return true;
    }

    // 版本提示对话框
    private AlertDialog dlgVersion;

    /**
     * 显示最新版本提示对话框
     */
    private void showVersionDlg(final Activity activity,
                                final VersionInfo newVer,
                                final boolean dontAlert) {
        LayoutInflater inflater = activity.getLayoutInflater();
        ViewGroup versionDlgView = (ViewGroup) inflater.inflate(R.layout.ver_update, null, false);
        TextView tvDesc = (TextView) versionDlgView.findViewById(R.id.ver_desc_tv);
        tvDesc.setText(newVer.verDesc);

        TextView tvConfirm = (TextView) versionDlgView.findViewById(R.id.confirm_tv);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgVersion.cancel();
                DownloadManagerUtils.getInstance().startDownloadApk(activity,
                        activity.getString(R.string.app_name), null, newVer.pkgUrl, LauncherConst.DOWNLOAD_PATH_APK);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light_Dialog));
        dlgVersion = builder.setView(versionDlgView).create();
        dlgVersion.setCanceledOnTouchOutside(false);
        dlgVersion.show();
    }

    // 记录任务序号
    private AtomicInteger taskCnt = new AtomicInteger(0);

    private void startBackgroundService(final Context context,
                                        final VersionInfo newVer) {
        // 启动后台版本下载服务
        context.startService(new Intent(context, VerDownloadService.class));
        Intent intent = new Intent(context, VerDownloadService.class);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                YHLog.i(tag(), "startBackgroundService - onServiceConnected, component:"
                        + name.getClassName());

                // 连接上了 版本下载服务 后，开始添加 下载任务
                VerDownloadTask task = new VerDownloadTask();
                task.versionInfo = newVer;
                task.progress = 0;
                task.taskId = taskCnt.incrementAndGet();

                VerDownloadService verDownloadService
                        = ((VerDownloadService.ServiceBinder) service).getService();
                if (verDownloadService != null) {
                    verDownloadService.addTask(task);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                YHLog.i(tag(), "startBackgroundService - onServiceDisconnected, component:"
                        + name.getClassName());
            }
        }, Context.BIND_AUTO_CREATE);
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
            progDlgDownloadApk.setTitle(R.string.dlg_title_downloading_apk);
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

                apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HealthLauncher.apk";
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
                return;
            }

            // 下载成功后自动安装
            installApk(context, apkPath);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progDlgDownloadApk.setProgress(values[0]);
        }
    }

    private String downloadApk(VersionInfo versionInfo) {

        if (versionInfo == null || versionInfo.pkgUrl == null) {
            return null;
        }

        // 首先确定本地安装了 SDCARD 来存储下载文件
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        OkHttpClient httpClient = new OkHttpClient();

        Request request = new Request.Builder().url(versionInfo.pkgUrl).build();
        try {
            Response response = httpClient.newCall(request).execute();
            InputStream is = response.body().byteStream();
            BufferedInputStream inputStream = new BufferedInputStream(is);

            int currDownSize = 0;
            String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HealthLauncher.apk";
            OutputStream outputStream = new FileOutputStream(apkPath);

            byte[] data = new byte[4096];
            int len;
            while ((len = inputStream.read(data)) != -1) {
                currDownSize += len;
                outputStream.write(data, 0, len);
            }

            YHLog.i(tag(), "downloadApk - file size: " + currDownSize);

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return apkPath;
        } catch (IOException e) {
            YHLog.w(tag(), "downloadApk - exception execute http: " + e.getMessage());
            return null;
        }
    }

    /**
     * 安装 APK
     */
    public static void installApk(Context context, String apkPath) {
        File fileApk = new File(apkPath);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(fileApk), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
