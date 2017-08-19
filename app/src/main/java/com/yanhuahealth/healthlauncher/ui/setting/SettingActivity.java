package com.yanhuahealth.healthlauncher.ui.setting;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.sys.download.DownloadActivity;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 * 设置首页
 */
public class SettingActivity extends YHBaseActivity {

    public final int SOS_NUM_SET = 1;

    @Override
    protected String tag() {
        return SettingActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle("设置");
        navBar.hideRight();

        // 进入 SOS 告警设置
        ViewGroup vgSos = (ViewGroup) findViewById(R.id.sos_layout);
        vgSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, SosSettingActivity.class);
                intent.putExtra("changeSos", false);
                intent.putExtra("MainOrNot", SOS_NUM_SET);
                startActivity(intent);
            }
        });

        // 进入系统设置
        ViewGroup vgSysSetting = (ViewGroup) findViewById(R.id.sys_setting_layout);
        vgSysSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });

        // 进入关于页面
        ViewGroup vgAbout = (ViewGroup) findViewById(R.id.about_layout);
        vgAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, AboutActivity.class));
            }
        });

        // 进入下载页面
        ViewGroup vgDownload = (ViewGroup) findViewById(R.id.download_layout);
        vgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
            }
        });
    }
}
