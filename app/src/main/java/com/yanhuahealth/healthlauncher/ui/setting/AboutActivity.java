package com.yanhuahealth.healthlauncher.ui.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.upgrade.VersionInfo;
import com.yanhuahealth.healthlauncher.sys.NetMgr;
import com.yanhuahealth.healthlauncher.sys.verupd.VersionUpgrade;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 * 关于 页面
 */
public class AboutActivity extends YHBaseActivity implements VersionUpgrade.INewVerNotify {

    @Override
    protected String tag() {
        return AboutActivity.class.getName();
    }

    // 检测新版本
    private Button btnCheckVer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle("关于");
        navBar.hideRight();

        // 当前版本
        TextView tvCurrVer = (TextView) findViewById(R.id.curr_ver_tv);
        VersionInfo currVersion = VersionUpgrade.getCurrVer(this);
        if (currVersion != null) {
            tvCurrVer.setText(currVersion.verName);
        } else {
            tvCurrVer.setText("");
        }

        // 检测新版本
        btnCheckVer = (Button) findViewById(R.id.check_version_btn);
        btnCheckVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCheckVer.setEnabled(false);
                if (NetMgr.getInstance().isWifiConnected() ||
                        NetMgr.getInstance().isMobileConnected()) {
                    btnCheckVer.setText("正在检测中");
                    VersionUpgrade.getInstance().checkVersion(
                            AboutActivity.this, false, AboutActivity.this);
                } else {
                    Toast.makeText(AboutActivity.this,"当前无网络连接",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void alreadyLatest() {
        Toast.makeText(this, "当前已经为最新版本", Toast.LENGTH_SHORT).show();
        btnCheckVer.setEnabled(true);
        btnCheckVer.setText("检测新版本");
    }

    @Override
    public void newVerNotify(VersionInfo newVer) {
        YHLog.d(tag(), "newVerNotify - " + newVer);
        btnCheckVer.setEnabled(true);
        btnCheckVer.setText("检测新版本");
    }

    @Override
    public void cancelUpdate(VersionInfo newVer) {
        YHLog.d(tag(), "cancelUpdate - " + newVer);
        btnCheckVer.setEnabled(true);
        btnCheckVer.setText("检测新版本");
    }
}
