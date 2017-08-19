package com.yanhuahealth.healthlauncher.ui.toolutil;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutStyle;
import com.yanhuahealth.healthlauncher.ui.base.ShortcutBoxView;

/**
 * 工具详情界面
 */
public class ToolActivity extends SecondActivity {
    public static final int CLOCK_NEMUBER = -1;

    private boolean isopent = false;
    private Camera camera;

    private LinearLayout linearLayout;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addShortcut(Calculator());
        addShortcut(FlashLight());
        addShortcut(AlarmClock());
//        linearLayout = (LinearLayout)findViewById(R.id.chang_color);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        // 添加默认项
        for(int i = 3; i < 16; i++) {
            addShortcut(Default(i));
        }
    }

    // 计算器
    public Shortcut Calculator() {
        Shortcut shortcutCalculator = new Shortcut(
                1, 0, "计算器", ShortcutType.YH_NEWS, false);
        Intent intentCalculator = new Intent();
        intentCalculator.setClassName("com.android.calculator2",
                "com.android.calculator2.Calculator");
        shortcutCalculator.intent = intentCalculator;
        shortcutCalculator.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutCalculator.icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_album);
        shortcutCalculator.style = new ShortcutStyle(R.drawable.shortcut_bg_first, R.color.white);
        addShortcut(shortcutCalculator);
        return shortcutCalculator;
    }

    // 手电筒
    public Shortcut FlashLight() {
        Shortcut shortcutFlashLight = new Shortcut(
                1, 1, "手电筒", ShortcutType.YH_NEWS, false);
        shortcutFlashLight.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        ShortcutBoxView shortcutBoxView = getShortcutBoxView(1);
        shortcutBoxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFlashLight();
            }
        });
        shortcutFlashLight.icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_album);
        shortcutFlashLight.style = new ShortcutStyle(R.drawable.shortcut_bg_second, R.color.white);
        return shortcutFlashLight;
    }

    // 闹钟
    public Shortcut AlarmClock() {
        Shortcut shortcutAlarmClock = new Shortcut(
                1, 2, "闹钟", ShortcutType.YH_NEWS, false);
        shortcutAlarmClock.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        Intent alarms = new Intent(AlarmClock.ACTION_SET_ALARM);
        shortcutAlarmClock.intent = alarms;
        shortcutAlarmClock.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutAlarmClock.icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_album);
        shortcutAlarmClock.style = new ShortcutStyle(R.drawable.shortcut_bg_third, R.color.white);
        addShortcut(shortcutAlarmClock);
        return shortcutAlarmClock;
    }

    // 等待添加工具项
    public Shortcut Default(int pos){
        Shortcut shortcutDefault = new Shortcut(
                1, pos, "", ShortcutType.DEFAULT, false);
//        if(pos <= 5) {
//            scrollView.setVisibility(View.GONE);
//        }
        shortcutDefault.icon = BitmapFactory.decodeResource(getResources(), 0);
        shortcutDefault.style = new ShortcutStyle(R.color.default_box_bg, R.color.white);
        linearLayout.setBackgroundResource(R.color.default_box_bg);
        return shortcutDefault;
    }

    @Override
    public String getNewTitle() {

        return "工具";
    }

     // 退出当前界面关闭手电筒
    @Override
    public void onPause() {
        super.onPause();
        if (isopent) {
            startFlashLight();
        } else {
            return;
        }
    }

    /**
     * 启动系统手电筒
     */
    private void startFlashLight() {
        if (!isopent) {
//            Toast.makeText(getApplicationContext(), "您已经打开了手电筒", Toast.LENGTH_SHORT)
//                    .show();
            camera = Camera.open();
            Parameters params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);

            // 开始亮灯
            camera.startPreview();
            isopent = true;
        } else {
//            Toast.makeText(getApplicationContext(), "关闭了手电筒",
//                    Toast.LENGTH_SHORT).show();

            // 关掉手电筒
            camera.stopPreview();

            // 关掉照相机
            camera.release();
            isopent = false;
        }
    }

}
