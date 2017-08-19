package com.yanhuahealth.healthlauncher.ui.base;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.bugtags.library.Bugtags;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.umeng.analytics.MobclickAgent;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.sys.ITaskCallback;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.tool.HomeWatcher;
import com.yanhuahealth.healthlauncher.ui.MainActivity;
import com.yanhuahealth.healthlauncher.utils.SystemBarTintManager;

import java.util.Map;

/**
 * 所有 Activity 的基类
 * 主要用于将一些通用的操作和属性封装出来，如：
 * <p/>
 * - 设置 Bugtags
 * - 友盟统计
 */
public abstract class YHBaseActivity extends AppCompatActivity implements ITaskCallback, HomeWatcher.OnHomePressedListener {

    // 正式发布时设置为 false
    public static final boolean needBugtags = false;

    // Home键监听
    private HomeWatcher homeWatcher;

    // 主要用于日志 tag，以及获取各个 Activity 所在的类路径
    // 要求所有的继承类都实现此方法
    protected abstract String tag();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        YHLog.d(tag(), "onCreate - " + (savedInstanceState != null));
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState != null) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        // 一下就是更改statusbar的颜色 代码实现 其中默认每个界面会全屏 导致与statusbar重叠
        // 为了修复这个问题 要在每个activity 的布局文件里加上 android:fitsSystemWindows="true"；
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.bg_black);//通知栏所需颜色
        }
    }

    @Override
    protected void onDestroy() {
        YHLog.d(tag(), "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        YHLog.d(tag(), "onStart");
        super.onStart();
        homeWatcherStart();
        MainService.getInstance().regServiceCallbackComponent(tag(), this);
    }

    private void homeWatcherStart() {
        homeWatcher = new HomeWatcher(this);
        homeWatcher.setOnHomePressedListener(this);
        homeWatcher.startWatch();
    }

    @Override
    protected void onStop() {
        YHLog.d(tag(), "onStop");
        super.onStop();
        homeWatcherStop();
        MainService.getInstance().unregServiceCallbackComponent(tag());
    }

    private void homeWatcherStop() {
        homeWatcher.setOnHomePressedListener(null);
        homeWatcher.stopWatch();
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (needBugtags) {
            Bugtags.onDispatchTouchEvent(this, ev);
        }

        return super.dispatchTouchEvent(ev);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(tag());
        MobclickAgent.onResume(this);
        if (needBugtags) {
            Bugtags.onResume(this);
        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(tag());
        MobclickAgent.onPause(this);
        if (needBugtags) {
            Bugtags.onPause(this);
        }
    }

    protected static Gson gson
            = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public boolean refresh(int rt, Map<String, Object> params) {
        Log.d(tag(), "refresh - " + rt + "|" + params);
        return true;
    }

    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void onHomePressed() {
        startActivity(new Intent(YHBaseActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onHomeLongPressed() {
        startActivity(new Intent(YHBaseActivity.this, MainActivity.class));
        finish();
    }
}
