
package com.yanhuahealth.healthlauncher.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.ebook.Ebook;
import com.yanhuahealth.healthlauncher.model.sms.ImSmsInfo;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.model.sys.MessageInfo;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutRemoveResult;
import com.yanhuahealth.healthlauncher.model.voicechannel.VoiceItem;
import com.yanhuahealth.healthlauncher.sys.EbookMgr;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.IEventHandler;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.sys.VoiceChannelMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.sys.verupd.VerDownloadService;
import com.yanhuahealth.healthlauncher.sys.verupd.VerDownloadTask;
import com.yanhuahealth.healthlauncher.sys.verupd.VersionUpgrade;
import com.yanhuahealth.healthlauncher.tool.HomeWatcher;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseFragment;
import com.yanhuahealth.healthlauncher.ui.fragment.CenterFragment;
import com.yanhuahealth.healthlauncher.ui.fragment.CommonShortcutPageFragment;
import com.yanhuahealth.healthlauncher.ui.fragment.ThirdFragment;
import com.yanhuahealth.healthlauncher.ui.note.FixContactSmsActivity;
import com.yanhuahealth.healthlauncher.ui.note.ImSmsActivity;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;
import com.yanhuahealth.healthlauncher.utils.DownloadManagerUtils;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

/**
 * 主页面
 */
public class MainActivity extends YHBaseActivity implements ViewPager.OnPageChangeListener, IEventHandler, HomeWatcher.OnHomePressedListener {

    private List<YHBaseFragment> fragmentList = new ArrayList<>();
    private LauncherPagerAdapter lcAdapter;

    private DialogUtil pbDialog;
    public static final String CAT_ID_TO_FRAGMENT_ACTION = "catIdToFragment";
    public static final String CAT_ID_TO_FRAGMENT_VOICE_ACTION = "catIdToVoiceFragment";
    public static final String CAT_ID_FOR_FRAGMENT = "catIdForFragment";
    public static final String EBOOK_ID_FOR_FRAGMENT = "ebookIdForFragment";

    // 开始下载
    private static final int DOWN_UPDATE = 100;

    ViewPager viewpagerFM;
    private HomeWatcher homeWatcher;
    SmsReceiver smsReceiver;
    ImSmsInfo imSmsInfo;

    // 用于标识是否为新创建的
    // 在 onStart 中根据此标志位来确定是否加载某些操作
    // 如果为 true，表示已经走过 onCreate，即本次进入页面非新创建
    private boolean isCreated = false;

    BroadcastReceiver apkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String apkurl = intent.getStringExtra(LauncherConst.INTENT_PARAM_APK_URL) + "";
            final String apkTitle = intent.getStringExtra(LauncherConst.INTENT_PARAM_APK_TITLE) + ".apk";
            final File apkfile = new File(LauncherConst.getApkRootPath() + apkTitle);
            pbDialog = new DialogUtil(MainActivity.this, new DialogUtil.OnDialogUtilListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.dialog_btn_one_tv:
                            if (apkfile.exists() && apkfile.length() > 1024 * 1000) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                                        "application/vnd.android.package-archive");
                                context.startActivity(i);
                                pbDialog.dismiss();
                            } else if (PhoneStatus.getInstance().isWiFi(context)) {
                                if (apkfile.exists()) {
                                    apkfile.delete();
                                }
                                DownloadManagerUtils.getInstance().startDownloadApk
                                        (context, apkTitle, new ApkDownloadHandler(
                                                MainActivity.this), apkurl, LauncherConst.DOWNLOAD_PATH_APK);
                            } else {
                                Toast.makeText(context, "仅支持wifi状态下下载", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.dialog_btn_two_tv:
                            DownloadManagerUtils.getInstance().cancelDownload(context);
                            pbDialog.dismiss();
                            break;
                    }

                }
            });

            if (apkfile.exists() && apkfile.length() > 1024 * 1000 && pbDialog != null) {
                pbDialog.showProgessDialog("是否下载该应用", "安装", "取消");
            } else {
                pbDialog.showProgessDialog("是否下载该应用", "下载", "取消");
            }
        }
    };

    @Override
    protected String tag() {
        return MainActivity.class.getName();
    }

    private LinearLayout layoutLoading;

    /**
     * 下载完成的广播接收器
     */
    // 接收和处理 下载完成 后的广播通知
    BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                YHLog.i(tag(), "receive download complete: " + downloadId);

                // 判断是否为电子书下载
                Ebook ebook = EbookMgr.getInstance().getDownloadingEbookWithDownloadId(downloadId);
                if (ebook != null) {
                    Toast.makeText(MainActivity.this, ebook.name + "已下载完成", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(CAT_ID_TO_FRAGMENT_ACTION);
                    sendBroadcast(intent1);
                    EbookMgr.getInstance().updateEbookWithDownloadFinish(context, downloadId);
                }
            }
        }
    };

    /**
     * 语音频道下载完成的广播接收器
     */
    // 接收和处理下载完成后的广播通知
    BroadcastReceiver downloadVoiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                YHLog.i(tag(), "receive download complete: " + downloadId);

                // 判断是否为语音下载
                VoiceItem voice = VoiceChannelMgr.getInstance().getDownloadingVoiceWithDownloadId(downloadId);
                if (voice != null) {
                    Intent intentVoice = new Intent(CAT_ID_TO_FRAGMENT_VOICE_ACTION);
                    sendBroadcast(intentVoice);
                    VoiceChannelMgr.getInstance().updateVoiceItemWithDownloadFinish(context, downloadId);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 设置系统最大字体
        Configuration configuration = new Configuration();
        configuration.fontScale = 1.1f;
        getResources().updateConfiguration(configuration, null);

        layoutLoading = (LinearLayout) findViewById(R.id.loading_layout);
        viewpagerFM = (ViewPager) findViewById(R.id.pager);

        registerReceiver();
        new LoadingTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class LoadingTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            viewpagerFM.setVisibility(View.GONE);
            layoutLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            YHLog.i(tag(), "begin LoadingTask");
            ShortcutMgr.getInstance().init(getApplicationContext());
            SmsMgr.getInstance().init(getApplication());
            YHLog.i(tag(), "end LoadingTask");
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            layoutLoading.setVisibility(View.GONE);
            viewpagerFM.setVisibility(View.VISIBLE);

            initFragment();
            reloadNonDefaultShortcutPage();
            phoneChange();
            startVerCheck();
            isCreated = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        YHLog.i(tag(), "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        YHLog.i(tag(), "onRestoreInstanceState");
    }

    private void initFragment() {

        // 添加第 1 页
        CommonShortcutPageFragment oneFragment = new CommonShortcutPageFragment();
        Bundle bundleOne = new Bundle();
        bundleOne.putInt(CommonShortcutPageFragment.PARAM_PAGE, 0);
        oneFragment.setArguments(bundleOne);
        fragmentList.add(oneFragment);

        // 第 2 页
        CenterFragment centerFragment = new CenterFragment();
        Bundle bundleCenter = new Bundle();
        bundleCenter.putInt(CommonShortcutPageFragment.PARAM_PAGE, 1);
        centerFragment.setArguments(bundleCenter);
        fragmentList.add(centerFragment);

        // 第 3 页
        ThirdFragment threeFragment = new ThirdFragment();
        fragmentList.add(threeFragment);

        viewpagerFM.addOnPageChangeListener(this);
        lcAdapter = new LauncherPagerAdapter(getSupportFragmentManager(), fragmentList);
        viewpagerFM.setAdapter(lcAdapter);
        viewpagerFM.setCurrentItem(whitchFrament());
    }

    private void reloadNonDefaultShortcutPage() {
        int pageNum = ShortcutMgr.getInstance().getShortcutPageNum();
        if (pageNum > fragmentList.size()) {
            YHLog.i(tag(), "reloadNonDefaultShortcutPage - before:" + fragmentList.size() + "|after:" + pageNum);
            for (int idxPage = fragmentList.size(); idxPage < pageNum; ++idxPage) {
                CommonShortcutPageFragment newFragment = new CommonShortcutPageFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(CommonShortcutPageFragment.PARAM_PAGE, idxPage);
                newFragment.setArguments(bundle);
                fragmentList.add(newFragment);
            }

            if (lcAdapter != null) {
                lcAdapter.notifyDataSetChanged();
            }
        }
    }

    // 启动版本更新检查的任务
    private void startVerCheck() {
        // 版本更新的启动
        VersionUpgrade.getInstance().startCheckTimer(MainActivity.this, 3000, 4 * 3600 * 1000);
    }

    private BroadcastReceiver receiverVerDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(VerDownloadService.ACTION_FINISHED)) {
                VerDownloadTask task = (VerDownloadTask) intent.getSerializableExtra(VerDownloadService.PARAM_TASK);
                if (task == null) {
                    return;
                }

                YHLog.i(tag(), "receiverVerDownload - " + task);
                if (task.status == VerDownloadTask.FINISHED) {
                    // 下载成功后自动安装
                    VersionUpgrade.installApk(context, task.apkLocalPath);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiverVerDownload != null) {
            unregisterReceiver(receiverVerDownload);
        }

        if (apkReceiver != null) {
            unregisterReceiver(apkReceiver);
        }

        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }

        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
        }

        if (downloadVoiceReceiver != null) {
            unregisterReceiver(downloadVoiceReceiver);
        }

        if (runnableForQueryBalance != null) {
            handlerForQueryBalance.removeCallbacks(runnableForQueryBalance);
        }
    }

    // 滑动的三个方法
    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        MainService.getInstance().regEventHandler(EventType.SVC_APP_UNINSTALL, this);
        MainService.getInstance().regEventHandler(EventType.SVC_QUERY_BALANCE, this);
        homeWatcherStart();

        if (isCreated) {
            reloadNonDefaultShortcutPage();
            phoneChange();
        }
    }

    private void homeWatcherStart() {
        homeWatcher = new HomeWatcher(this);
        homeWatcher.setOnHomePressedListener(this);
        homeWatcher.startWatch();

    }

    @Override
    protected void onStop() {
        super.onStop();
        MainService.getInstance().unregEventHandler(EventType.SVC_QUERY_BALANCE, this);
        MainService.getInstance().unregEventHandler(EventType.SVC_APP_UNINSTALL, this);
        homeWatcherStop();
    }

    private void homeWatcherStop() {
        homeWatcher.setOnHomePressedListener(null);
        homeWatcher.stopWatch();
    }

    @Override
    public void notify(BroadEvent event) {

        switch (event.eventType) {
            case EventType.SVC_APP_UNINSTALL:
                if (event.eventInfo != null
                        && event.eventInfo.containsKey(EventType.KEY_SVC_APP_UNINSTALL_REMOVE_RESULT)) {
                    ShortcutRemoveResult removeResult = (ShortcutRemoveResult) event.eventInfo.get(
                            EventType.KEY_SVC_APP_UNINSTALL_REMOVE_RESULT);
                    if (removeResult != null
                            && removeResult.result && removeResult.isRemovedPage
                            && removeResult.shortcut != null
                            && fragmentList.size() > removeResult.shortcut.page) {
                        fragmentList.remove(removeResult.shortcut.page);
                        lcAdapter.notifyDataSetChanged();
                    }
                }
                break;

            case EventType.SVC_QUERY_BALANCE:
                if (event.eventInfo != null
                        && event.eventInfo.containsKey(EventType.KEY_SVC_QUERY_BALANCE_SHORTCUT_ID)) {
                    int shortcutId = (int) event.eventInfo.get(EventType.KEY_SVC_QUERY_BALANCE_SHORTCUT_ID);
                    Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(shortcutId);
                    if (shortcut != null) {
                        handleQueryBalance(shortcut);
                    }
                }
                break;
        }
    }

    // 查询余额的 shortcut 更新
    public static final int MSG_TYPE_QUERY_BALANCE_UPD = 100;

    // 用于定时器到期后通知更新 shortcut box
    private Handler handlerForQueryBalance = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TYPE_QUERY_BALANCE_UPD:
                    // 通知相应的 shortcut box view 更新 UI
                    MainService.getInstance().sendBroadEvent(
                            new BroadEvent(EventType.SVC_QUERY_BALANCE_UPD, null));
                    if (runnableForQueryBalance != null) {
                        removeCallbacks(runnableForQueryBalance);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    class RunnableForQueryBalance implements Runnable {

        private int shortcutId;

        public RunnableForQueryBalance(int shortcutId) {
            this.shortcutId = shortcutId;
        }

        @Override
        public void run() {
            Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(shortcutId);
            if (shortcut == null) {
                return;
            }

            shortcut.isEnable = true;
            shortcut.style.backgroundColor = R.drawable.shortcut_box_fifth_bg;
            handlerForQueryBalance.sendEmptyMessage(MSG_TYPE_QUERY_BALANCE_UPD);
        }
    }

    private RunnableForQueryBalance runnableForQueryBalance;

    // 处理一键查询话费
    private void handleQueryBalance(Shortcut shortcut) {
        YHLog.i(tag(), "handleQueryBalance - handle shortcut - " + shortcut);
        boolean result = SmsMgr.getInstance().sendSmsForBalance(this);
        if (!result) {
            YHLog.i(tag(), "handleQueryBalance - send sms for balance failed!");
            return;
        }

        shortcut.isEnable = false;
        shortcut.style.backgroundColor = R.drawable.shortcut_box_unuse_bg;
        MainService.getInstance().sendBroadEvent(new BroadEvent(EventType.SVC_QUERY_BALANCE_UPD, null));
    }

    @Override
    public void onHomePressed() {
        viewpagerFM.setCurrentItem(whitchFrament());
    }

    @Override
    public void onHomeLongPressed() {

    }

    // FRAGMENT 适配器
    private class LauncherPagerAdapter extends FragmentPagerAdapter {

        private List<YHBaseFragment> fragmentList;

        public LauncherPagerAdapter(FragmentManager fm, List<YHBaseFragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    public int whitchFrament() {
        if (getIntent() == null) {
            return 1;
        }

        if (getIntent().getIntExtra("FragmentPageNumber", 2) == 100) {
            return 0;
        }

        if (getIntent().getIntExtra("FragmentPageNumber", 2) == 200) {
            return 1;
        }

        if (getIntent().getIntExtra("FragmentPageNumber", 2) == 300) {
            return 2;
        }

        return 1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == 100) {
            if (data.getIntegerArrayListExtra("PageNo") != null) {
                ArrayList<Integer> lstShortcutPageNo = data.getIntegerArrayListExtra("PageNo");

                // 先更新现有的 fragment 的 PageNo
                int idxFragment = 0;
                for (YHBaseFragment fragment : fragmentList) {
                    if (idxFragment != 1 && idxFragment != 2) {
                        CommonShortcutPageFragment pageFragment = (CommonShortcutPageFragment) fragment;
                        if (pageFragment != null) {
                            pageFragment.setPageNo(lstShortcutPageNo.get(idxFragment));
                        }
                    }

                    ++idxFragment;
                }

                Collections.reverse(lstShortcutPageNo);

                int idxPage = 0;
                for (int pageNo : lstShortcutPageNo) {
                    if (pageNo == -1) {
                        fragmentList.remove(lstShortcutPageNo.size() - idxPage - 1);
                        lcAdapter.notifyDataSetChanged();
                    }

                    ++idxPage;
                }
            }
        }
    }

    private static class ApkDownloadHandler extends Handler {

        private final WeakReference<MainActivity> thisActivity;

        public ApkDownloadHandler(MainActivity activity) {
            this.thisActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = thisActivity.get();
            if (activity != null && activity.pbDialog != null) {
                switch (msg.what) {
                    case DOWN_UPDATE:
                        activity.pbDialog.updateProgress(msg.arg1);
                        if (msg.arg1 == 100) {
                            activity.pbDialog.dismiss();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void phoneChange() {
        Shortcut msg = ShortcutMgr.getInstance().getShortcut(ShortcutConst.SMS_PAGE, ShortcutConst.SMS_POS);
        if (msg != null && msg.numSign != getSMSCount()) {
            Intent intent = new Intent();
            intent.setAction("SMS.is.changed");
            sendBroadcast(intent);
        }

        Shortcut call = ShortcutMgr.getInstance().getShortcut(ShortcutConst.PHONE_PAGE, ShortcutConst.PHONE_POS);
        if (call != null && call.numSign != readMissCall()) {
            Intent intent = new Intent();
            intent.setAction("Call.is.changed");
            sendBroadcast(intent);
        }
    }

    public int getSMSCount() {
        Uri uri = Uri.parse("content://sms");
        ContentResolver cr = MainActivity.this.getContentResolver();
        int smsCount = 0;
        Cursor cursor = cr.query(uri, null, "type =1 and read=0", null, null);
        if (cursor != null) {
            smsCount = cursor.getCount();
            cursor.close();
        }
        return smsCount;
    }

    private int readMissCall() {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        Cursor cursor = MainActivity.this.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{
                CallLog.Calls.TYPE
        }, " type=? and new=?", new String[]{
                CallLog.Calls.MISSED_TYPE + "", "1"
        }, "date desc");

        if (cursor != null) {
            result = cursor.getCount();
            cursor.close();
        }
        return result;
    }

    public void registerReceiver() {
        // APP 下载接收器
        IntentFilter filter = new IntentFilter(LauncherConst.INTENT_ACTION_DOWN_APK);
        registerReceiver(apkReceiver, filter);

        // 接收版本更新和下载的广播通知
        IntentFilter filterVerDownload = new IntentFilter();
        filterVerDownload.addAction(VerDownloadService.ACTION_FINISHED);
        filterVerDownload.addAction(VerDownloadService.ACTION_UPDATE);
        registerReceiver(receiverVerDownload, filterVerDownload);

        // 注册接收系统回复短信广播
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        smsReceiver = new SmsReceiver();
        intentFilter.addAction("com.yanhuaim.message.NOTIFY");
        intentFilter.addAction("DeleteShortcut");
        registerReceiver(smsReceiver, intentFilter);

        // 下载完成的接收器
        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        registerReceiver(downloadVoiceReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    // 接收系统广播 和IM信息
    private class SmsReceiver extends BroadcastReceiver {
        // 当收到短信时被触发
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                // 接收由SMS传来的数据
                Bundle bundle = intent.getExtras();

                // 判断是否有数据
                if (bundle != null) {
                    // 通过pdus可以获得接收到的所有短信消息
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus == null) {
                        return;
                    }

                    // 构建短信对象array，并依据收到对象长度来创建array大小
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }

                    String phoneNumber = null;
                    StringBuilder sbufMessageBody = new StringBuilder();

                    // 将发来的信息合并自定义信息于StringBuilder当中
                    for (SmsMessage message : messages) {
                        // 接收短信的号码
                        phoneNumber = message.getDisplayOriginatingAddress();
                        sbufMessageBody.append(message.getDisplayMessageBody());
                    }

                    if (phoneNumber != null) {
                        YHLog.d(tag(), "SmsReceiver:onReceive sms");
                        String messageBody = sbufMessageBody.toString();
                        if (phoneNumber.equals("10086") ||
                                phoneNumber.equals("10010") ||
                                phoneNumber.equals("10001")
                                        && messageBody.contains(LauncherConst.PATTERN_QUERY_FEE)) {
                            smsShowMessageDialog(phoneNumber, messageBody);
                            Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(3, 5);
                            shortcut.isEnable = true;
                            shortcut.style.backgroundColor = R.drawable.shortcut_box_fifth_bg;
                            MainService.getInstance().sendBroadEvent(new BroadEvent(EventType.SVC_QUERY_BALANCE_UPD, null));
                        }
                    }
                }
            }

            // todo 当收到IM信息触发
            if (intent.getAction().equals("com.yanhuaim.message.NOTIFY")) {
                String imMessage = intent.getStringExtra("CONTENT");
                Log.i("tagjson", "json : " + imMessage);
                String imBody = intent.getStringExtra("msg");
                if (imMessage != null && imBody != null) {
                    try {
                        Gson gson = new Gson();
                        Type type = new TypeToken<ImSmsInfo>() {}.getType();
                        imSmsInfo = gson.fromJson(imMessage, type);
                        if (imSmsInfo != null && imSmsInfo.from != null && imSmsInfo.from.equals("0")) {
                            smsShowMessageDialog("系统消息", imBody);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "消息转换异常", Toast.LENGTH_LONG).show();
                    }
                }
            }

            if (intent.getAction().equals("DeleteShortcut") && viewpagerFM != null) {
                viewpagerFM.setCurrentItem(1);
                if(intent.getIntExtra("page",0)>3) {
                    fragmentList.remove(intent.getIntExtra("page",0));
                    lcAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    // 一键查话费收到短信回复弹出的dialog
    private DialogUtil queryBalanceMessageDialog;

    private void smsShowMessageDialog(final String phoneNumber, String messageBody) {

        if (queryBalanceMessageDialog != null || (phoneNumber == null || messageBody == null)) {
            return;
        }

        DialogUtil.OnDialogUtilListenerWithMessageDialog leftListener = new DialogUtil.OnDialogUtilListenerWithMessageDialog() {
            @Override
            public void onClick(View view, AlertDialog messageDialog) {
                if (phoneNumber.equals("10086") || phoneNumber.equals("10000")
                        || phoneNumber.equals("10010") || phoneNumber.equals("10011")) {
                    Intent intentToSmsList = new Intent(MainActivity.this, FixContactSmsActivity.class);
                    intentToSmsList.putExtra(LauncherConst.INTENT_PARAM_PHONE_NUMBER, phoneNumber);
                    startActivity(intentToSmsList);
                    messageDialog.dismiss();
                    queryBalanceMessageDialog = null;
                } else if (phoneNumber.equals("系统消息")) {
                    if (imSmsInfo != null && imSmsInfo.url != null && !imSmsInfo.url.equals("")) {
                        Intent intent = new Intent(MainActivity.this, ImSmsActivity.class);
                        intent.putExtra("url_data", imSmsInfo.url);
                        startActivity(intent);
                        messageDialog.dismiss();
                        queryBalanceMessageDialog = null;
                        Toast.makeText(MainActivity.this, "没有详情查看", Toast.LENGTH_LONG).show();
                    } else {
                        messageDialog.dismiss();
                        queryBalanceMessageDialog = null;
                        Toast.makeText(MainActivity.this, "没有具体的详情", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        DialogUtil.OnDialogUtilListenerWithMessageDialog rightListener = new DialogUtil.OnDialogUtilListenerWithMessageDialog() {
            @Override
            public void onClick(View view, AlertDialog messageDialog) {
                messageDialog.dismiss();
                queryBalanceMessageDialog = null;

            }
        };
        DialogUtil.MessageDialogProp messageDialogProp = new DialogUtil.MessageDialogProp(
                R.drawable.ic_sms, leftListener, "查看", rightListener, "取消");
        queryBalanceMessageDialog = new DialogUtil(MainActivity.this, messageDialogProp);
        MessageInfo messageInfo = new MessageInfo(messageBody, phoneNumber);
        queryBalanceMessageDialog.showMessageDialog(messageInfo);
    }

}
