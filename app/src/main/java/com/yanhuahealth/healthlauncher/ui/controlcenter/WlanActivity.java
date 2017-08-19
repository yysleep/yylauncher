package com.yanhuahealth.healthlauncher.ui.controlcenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 *  无线网设置页面
 */

public class WlanActivity extends YHBaseActivity implements NavBar.ClickLeftListener {
    private RelativeLayout selectWlan;
    private ImageView wlanState;
    WifiManager wifiManager;

    @Override
    protected String tag() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlan);
        NavBar navBar = new NavBar(this);
        navBar.setTitle("免费上网");
        navBar.hideRight();
        selectWlan = (RelativeLayout) findViewById(R.id.select_wlan_rl);
        wlanState = (ImageView) findViewById(R.id.wlan_state_iv);
        wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);

        if (isWlanConnected(WlanActivity.this)) {
            wlanState.setImageBitmap(BitmapFactory.decodeResource(WlanActivity.this.getResources(),
                    R.drawable.ic_wlan_on));
        } else {
            wlanState.setImageBitmap(BitmapFactory.decodeResource(WlanActivity.this.getResources(),
                    R.drawable.ic_wlan_off));
        }

        // 点击选取网络，打开系统wlan选取设置界面
        selectWlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWlanConnected(WlanActivity.this)) {
                    Intent intent = new Intent();
//                    intent.setAction(android.provider.Settings.ACTION_WIFI_SETTINGS);
//                    intent.setData(Uri.parse("content://settings/system"));
                    intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                    if (intent != null) {
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(WlanActivity.this,"无线网络未启用",Toast.LENGTH_LONG).show();
                }


                }
        });

        // 点击打开或关闭wlan
        wlanState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWlanConnected(WlanActivity.this)) {
                    wifiManager.setWifiEnabled(false);
                    wlanState.setImageBitmap(BitmapFactory.decodeResource(WlanActivity.this.getResources(),
                            R.drawable.ic_wlan_off));
                } else {
                    wifiManager.setWifiEnabled(true);
                    wlanState.setImageBitmap(BitmapFactory.decodeResource(WlanActivity.this.getResources(),
                            R.drawable.ic_wlan_on));
                }
            }


        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isWlanConnected(WlanActivity.this)) {
            wlanState.setImageBitmap(BitmapFactory.decodeResource(WlanActivity.this.getResources(),
                    R.drawable.ic_wlan_on));
        } else {
            wlanState.setImageBitmap(BitmapFactory.decodeResource(WlanActivity.this.getResources(),
                    R.drawable.ic_wlan_off));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(WlanActivity.this,ControlCenterActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // 判断当前wlan是否打开
    public boolean isWlanConnected(Context context) {
        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == wifiManager.WIFI_STATE_ENABLED) {
            return true;
        } else {
            return false;
        }
    }

    // 处理标题栏左侧按钮点击事件
    @Override
    public void onClick() {
        Intent intent = new Intent(WlanActivity.this,ControlCenterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
