package com.yanhuahealth.healthlauncher.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.umeng.analytics.MobclickAgent;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.sys.ITaskCallback;
import com.yanhuahealth.healthlauncher.sys.MainService;

import java.util.Map;

/**
 * 本应用中所有 Fragment 的基类
 * 提供了 注册 和 撤销注册 至 MainService 的机制
 * 以及相关的友盟注册事件
 */
public abstract class YHBaseFragment extends Fragment implements ITaskCallback {

    /**
     * 主要用于注册 register service 时使用对应的 Activity 路径名
     */
    protected abstract String tag();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        YHLog.d(tag(), "onCreate");
        super.onCreate(savedInstanceState);

        MainService.getInstance().regServiceCallbackComponent(tag(), this);
    }

    @Override
    public void onDestroy() {
        YHLog.d(tag(), "onDestroy");
        super.onDestroy();
        MainService.getInstance().unregServiceCallbackComponent(tag());
    }

    protected static Gson gson
            = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public boolean refresh(int taskTypeId, Map<String, Object> params) {
        YHLog.d(tag(), "refresh - " + taskTypeId + "|" + params);
        return false;
    }

    @Override
    public void onResume() {
        YHLog.d(tag(), "onResume");
        super.onResume();
        MobclickAgent.onPageStart(tag());
    }

    @Override
    public void onPause() {
        YHLog.d(tag(), "onPause");
        super.onPause();
        MobclickAgent.onPageEnd(tag());
    }

    /**
     * 主要用于需要由其他页面来通知该 fragment 的子类来刷新页面
     */
    public void notifyUpdate() {
        YHLog.d(tag(), "notifyUpdate");
    }
}
