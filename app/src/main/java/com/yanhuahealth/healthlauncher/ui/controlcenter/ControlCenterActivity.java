package com.yanhuahealth.healthlauncher.ui.controlcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.sys.NetMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 * 控制中心界面
 */
public class ControlCenterActivity extends YHBaseActivity implements View.OnClickListener {
    private LinearLayout mobileInternet;
    private LinearLayout wlanInternet;
    private LinearLayout silentVolume;
    private LinearLayout shockVolume;
    private SeekBar phoneVolumeBar;
    private SeekBar ringVolumeBar;
    private SeekBar lightBar;
    private ImageView mobileState;
    private ImageView wlanState;
    private ImageView silentState;
    private ImageView shakeState;
    private AudioManager audioManager;
    private int currPhoneVolume;
    private int maxPhoneVolume;
    private int maxRingVolume;
    private int currentRingVolume;
    private int currentLight;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ConnectivityManager connectivityManager;

    @Override
    protected String tag() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_center);
        preferences = getSharedPreferences("control", MODE_PRIVATE);
        editor = preferences.edit();
        registerBroadcastReceiver();
        init();
        mobileInternet.setOnClickListener(this);
        wlanInternet.setOnClickListener(this);
        silentVolume.setOnClickListener(this);
        shockVolume.setOnClickListener(this);

        //调整通话音量
        maxPhoneVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        currPhoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        phoneVolumeBar.setMax(maxPhoneVolume * 10);
        int proPhone = currPhoneVolume * 10;
        phoneVolumeBar.setProgress(proPhone);
        phoneVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 0 && progress < 5) {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);
                    currPhoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                } else if (progress >= 5 && progress < 15) {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, 0);
                    currPhoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                } else if (progress >= 15 && progress < 25) {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 2, 0);
                    currPhoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                } else if (progress >= 25 && progress < 35) {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 3, 0);
                    currPhoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                } else if (progress >= 35 && progress < 45) {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 4, 0);
                    currPhoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                } else if (progress >= 45 && progress < 55) {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 5, 0);
                    currPhoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                } else if (progress >= 55 && progress <= 60) {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 6, 0);
                    currPhoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //调整铃声音量
        maxRingVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        ringVolumeBar.setMax(maxRingVolume * 10);
        ringVolumeBar.setProgress(currentRingVolume * 10);
        ringVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 0 && progress < 5) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                } else if (progress >= 5 && progress < 15) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 15 && progress < 25) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 2, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 25 && progress < 35) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 3, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 35 && progress < 45) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 4, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 45 && progress < 55) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 5, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 55 && progress < 65) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 6, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 65 && progress < 75) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 7, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 75 && progress < 85) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 8, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 85 && progress < 95) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 9, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 95 && progress < 105) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 10, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 105 && progress < 115) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 11, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 115 && progress < 125) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 12, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 125 && progress <= 135) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 13, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 135 && progress <= 145) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 14, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                } else if (progress >= 145 && progress <= 155) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 15, 0);
                    currentRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    editor.putInt("volumeNum", progress);
                    editor.commit();
                }

                if (currentRingVolume == 0) {
                    silentState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                            R.drawable.ic_silent_on));
                    silentState.setBackgroundResource(R.drawable.bg_circle_on_control);
                } else if (currentRingVolume != 0) {
                    silentState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                            R.drawable.ic_silent_off));
                    silentState.setBackgroundResource(R.drawable.bg_circle_control_center);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //调整屏幕亮度
        currentLight = android.provider.Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 255);
        lightBar.setMax(255);
        lightBar.setProgress(currentLight);
        lightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int currLight = progress;

                //先关闭系统的亮度自动调节
                try {
                    if (android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                        android.provider.Settings.System.putInt(getContentResolver(),
                                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    }
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                android.provider.Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, currLight);
                currLight = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                if (currLight >= 0 && currLight <= 255) {
                    layoutParams.screenBrightness = currLight / 255f;
                }
                getWindow().setAttributes(layoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //元素初始化
    public void init() {
        // 导航栏设置
        NavBar navBar = new NavBar(this);
        navBar.setTitle("控制中心");

        // 隐藏右侧图标
        navBar.hideRight();

        mobileInternet = (LinearLayout) findViewById(R.id.mobile_net_state_ll);
        wlanInternet = (LinearLayout) findViewById(R.id.wlan_net_state_ll);
        silentVolume = (LinearLayout) findViewById(R.id.volume_silent_ll);
        shockVolume = (LinearLayout) findViewById(R.id.shake_state_ll);
        phoneVolumeBar = (SeekBar) findViewById(R.id.phone_volume_seekbar_sk);
        ringVolumeBar = (SeekBar) findViewById(R.id.ring_volume_seekbar_sk);
        lightBar = (SeekBar) findViewById(R.id.light_seekbar_sk);
        mobileState = (ImageView) findViewById(R.id.mobile_net_state_iv);
        wlanState = (ImageView) findViewById(R.id.wlan_net_state_iv);
        silentState = (ImageView) findViewById(R.id.volume_silent_iv);
        shakeState = (ImageView) findViewById(R.id.shake_state_iv);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        // 移动网络连接与否图标的变化
        if (NetMgr.getInstance().isNetConnect()) {
            mobileState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                    R.drawable.ic_mobile_net_on));
            mobileState.setBackgroundResource(R.drawable.bg_circle_on_control);
        } else if (!(NetMgr.getInstance().isMobileConnected())) {
            mobileState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                    R.drawable.ic_mobile_net_off));
            mobileState.setBackgroundResource(R.drawable.bg_circle_control_center);

        }

        // wlan网络连接与否图标的变化
        if (isWlanConnected(ControlCenterActivity.this)) {
            wlanState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                    R.drawable.ic_wlan_use));
            wlanState.setBackgroundResource(R.drawable.bg_circle_on_control);
        } else if (!(NetMgr.getInstance().isWifiConnected())) {
            wlanState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                    R.drawable.ic_wlan_no_use));
            wlanState.setBackgroundResource(R.drawable.bg_circle_control_center);
        }

        // 静音和振动图标的变化
        if (audioManager.getRingerMode() == audioManager.RINGER_MODE_SILENT) {
            silentState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                    R.drawable.ic_silent_on));
            silentState.setBackgroundResource(R.drawable.bg_circle_on_control);
        } else if (audioManager.getRingerMode() == audioManager.RINGER_MODE_VIBRATE) {
            shakeState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                    R.drawable.ic_shake_on));
            shakeState.setBackgroundResource(R.drawable.bg_circle_on_control);
        } else {
            shakeState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                    R.drawable.ic_shake_off));
            shakeState.setBackgroundResource(R.drawable.bg_circle_control_center);
            silentState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                    R.drawable.ic_silent_off));
            silentState.setBackgroundResource(R.drawable.bg_circle_control_center);
        }
    }

    // 判断当前wlan是否打开
    public boolean isWlanConnected(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == wifiManager.WIFI_STATE_ENABLED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //开启和关闭移动数据流量
            case R.id.mobile_net_state_ll:
                if (NetMgr.getInstance().isNetConnect()) {
                    NetMgr.getInstance().toggleMobileConnect(false);
                } else {
                    NetMgr.getInstance().toggleMobileConnect(true);
                }
                break;

            // 开启和关闭wifi
            case R.id.wlan_net_state_ll:
                Intent intentWlan = new Intent(ControlCenterActivity.this, WlanActivity.class);
                startActivity(intentWlan);
                finish();
                break;

            // 设置静音模式
            case R.id.volume_silent_ll:
                if (audioManager.getRingerMode() == audioManager.RINGER_MODE_SILENT) {
                    audioManager.setRingerMode(audioManager.RINGER_MODE_NORMAL);
                } else {
                    audioManager.setRingerMode(audioManager.RINGER_MODE_SILENT);
                }
                break;

            //设置振动模式
            case R.id.shake_state_ll:
                if (audioManager.getRingerMode() == audioManager.RINGER_MODE_VIBRATE) {
                    audioManager.setRingerMode(audioManager.RINGER_MODE_NORMAL);
                } else {
                    audioManager.setRingerMode(audioManager.RINGER_MODE_VIBRATE);
                }
                break;
        }
    }

    //按音量增减键时铃声音量seekBar的变化
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                currentRingVolume += 1;
                while (currentRingVolume > maxRingVolume) {
                    currentRingVolume = maxRingVolume;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_RING, currentRingVolume, 0);
                ringVolumeBar.setProgress(currentRingVolume * 10);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                currentRingVolume = currentRingVolume - 1;
                while (currentRingVolume < 0) {
                    currentRingVolume = 0;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_RING, currentRingVolume, 0);
                ringVolumeBar.setProgress(currentRingVolume * 10);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 广播接收，对action进行处理
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 监听网络状态变化
            if (action.equals(connectivityManager.CONNECTIVITY_ACTION)) {
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (NetMgr.getInstance().isNetConnect()) {
                    mobileState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                            R.drawable.ic_mobile_net_on));
                    mobileState.setBackgroundResource(R.drawable.bg_circle_on_control);
                } else {
                    mobileState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                            R.drawable.ic_mobile_net_off));
                    mobileState.setBackgroundResource(R.drawable.bg_circle_control_center);
                }
                if (NetMgr.getInstance().isWifiConnected()) {
                    wlanState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                            R.drawable.ic_wlan_use));
                    wlanState.setBackgroundResource(R.drawable.bg_circle_on_control);
                } else {
                    wlanState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                            R.drawable.ic_wlan_no_use));
                    wlanState.setBackgroundResource(R.drawable.bg_circle_control_center);
                }

            }

            // 监听静音、振动模式变化
            if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int ringMode = audioManager.getRingerMode();
                switch (ringMode) {
                    case AudioManager.RINGER_MODE_SILENT:
                        silentState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                                R.drawable.ic_silent_on));
                        silentState.setBackgroundResource(R.drawable.bg_circle_on_control);
                        shakeState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                                R.drawable.ic_shake_off));
                        shakeState.setBackgroundResource(R.drawable.bg_circle_control_center);
                        ringVolumeBar.setProgress(0);
                        ringVolumeBar.setEnabled(false);
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        shakeState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                                R.drawable.ic_shake_on));
                        shakeState.setBackgroundResource(R.drawable.bg_circle_on_control);
                        silentState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                                R.drawable.ic_silent_off));
                        silentState.setBackgroundResource(R.drawable.bg_circle_control_center);
                        break;
                    case AudioManager.RINGER_MODE_NORMAL:
                        silentState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                                R.drawable.ic_silent_off));
                        silentState.setBackgroundResource(R.drawable.bg_circle_control_center);
                        shakeState.setImageBitmap(BitmapFactory.decodeResource(ControlCenterActivity.this.getResources(),
                                R.drawable.ic_shake_off));
                        shakeState.setBackgroundResource(R.drawable.bg_circle_control_center);
                        ringVolumeBar.setEnabled(true);
                        ringVolumeBar.setProgress(preferences.getInt("volumeNum", 20));
                        break;
                }
            }
        }
    };

    // 注册广播监听网络状态及情景模式的变化，以改变图标
    public void registerBroadcastReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(connectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(audioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}

