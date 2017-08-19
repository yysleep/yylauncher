package com.yanhuahealth.healthlauncher.sys.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.sys.download.dbservice.DownloadManagers;
import com.yanhuahealth.healthlauncher.sys.download.downmodle.FileInfo;
import com.yanhuahealth.healthlauncher.tool.FileDownloadAdapter;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/21.
 */
public class DownloadActivity extends YHBaseActivity {

    private ListView lvFile;
    private List<FileInfo> fileList;
    private FileDownloadAdapter fileAdapter;

    @Override
    protected String tag() {
        return DownloadActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downlaod);
        NavBar navBar = new NavBar(this);
        navBar.setTitle("下载管理");
        navBar.hideRight();
        RegisterReceeiver();
        lvFile = (ListView) findViewById(R.id.download_lv);
        fileList = new ArrayList<>();
        FileInfo fileInfo0 = new FileInfo(0, 1, "语音助手.apk", "http://61.155.220.192/dd.myapp.com/16891/8C8D2ADD86EBEB61CD88CFA920C12328.apk?mkey=56d52fc168854541&f=b110&fsname=com.wowenwen.yy_1.5.5_1122.apk&p=.apk", 0, 0);
        FileInfo fileInfo1 = new FileInfo(1, 1, "微信.apk", "http://apk.r1.market.hiapk.com/data/upload/apkres/2016/2_3/17/com.tencent.mm_054825.apk", 0, 0);
        FileInfo fileInfo2 = new FileInfo(2, 1, "健康管家.apk", "http://www.laoyou99.cn/app/3/1/Medical-4.2.4.apk", 0, 0);
        FileInfo fileInfo3 = new FileInfo(3, 1, "QQ.apk", "http://apk.r1.market.hiapk.com/data/upload/apkres/2016/2_4/15/com.tencent.mobileqq_031014.apk", 0, 0);
        fileList.add(fileInfo0);
        fileList.add(fileInfo1);
        fileList.add(fileInfo2);
        fileList.add(fileInfo3);
        fileAdapter = new FileDownloadAdapter(DownloadActivity.this, fileList);
        lvFile.setAdapter(fileAdapter);

    }

    // 注册下载广播
    public void RegisterReceeiver() {
        // 新的下载广播
        IntentFilter filterDown = new IntentFilter();
        filterDown.addAction(DownloadManagers.DOWNLOAD_ACTION_UPDATE);
        filterDown.addAction(DownloadManagers.DOWNLOAD_ACTION_STOP);
        filterDown.addAction(DownloadManagers.DOWNLOAD_ACTION_FINISHED);
        registerReceiver(receiverDownManager, filterDown);
    }

    BroadcastReceiver receiverDownManager = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManagers.DOWNLOAD_ACTION_UPDATE)) {
                // 更新进度条
                int finished = intent.getIntExtra("finishedProgress", 0);
                int fileInfoId = intent.getIntExtra("fileInfoId", 0);
                fileAdapter.updateProgress(fileInfoId, finished);
            }
            if (intent.getAction().equals(DownloadManagers.DOWNLOAD_ACTION_FINISHED)) {
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                // 更新进度条为100
                fileAdapter.updateProgress(fileInfo.getId(), 100);
                Toast.makeText(context, fileList.get(fileInfo.getId()).getName() + "下载完毕", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverDownManager);
    }
}
