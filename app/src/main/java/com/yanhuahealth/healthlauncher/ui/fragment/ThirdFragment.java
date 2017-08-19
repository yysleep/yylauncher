package com.yanhuahealth.healthlauncher.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.google.gson.reflect.TypeToken;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.api.ApiConst;
import com.yanhuahealth.healthlauncher.api.ApiResponseResult;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.news.News;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.TaskType;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.base.ShortcutBoxView;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseFragment;
import com.yanhuahealth.healthlauncher.ui.news.NewsDetailActivity;
import com.yanhuahealth.healthlauncher.ui.news.NewsListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 第 3 屏
 */
public class ThirdFragment extends YHBaseFragment {

    @Override
    protected String tag() {
        return ThirdFragment.class.getName();
    }

    public ThirdFragment() {
        // Required empty public constructor
    }

    // 保存 parent view，其他组件使用此 find 和 加载
    private ViewGroup parentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (parentView == null) {
            parentView = (ViewGroup) inflater.inflate(R.layout.fragment_third, container, false);
            initView(parentView);
        } else {
            ViewGroup parent = (ViewGroup) parentView.getParent();
            if (parent != null) {
                parent.removeView(parentView);
            }
        }

        return parentView;
    }

    // 对应于 8 个 前2个是空的 shortcut box
    private List<ShortcutBoxView> shortcutBoxViewList = new ArrayList<>();

    // 只用于第 3 页
    public static final int PAGE_NO = 2;

    // 最新资讯滚动
    private SliderLayout sliderLatestNews;

    // 加载最新资讯的布局
    private LinearLayout layoutLoadingNews;

    // 是否为首次加载
    private boolean isLoadFirst = true;

    // 滚动资讯项
    public class LatestNewsSliderView extends BaseSliderView {

        private String title;
        private String summary;

        public LatestNewsSliderView(Context context, String title, String summary) {
            super(context);
            this.title = title;
            this.summary = summary;
        }

        @Override
        public View getView() {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.render_latest_news, parentView, false);

            TextView tvTitle = (TextView) v.findViewById(R.id.latest_news_title_tv);
            tvTitle.setText(title);

            TextView tvSummary = (TextView) v.findViewById(R.id.latest_news_summary_tv);
            tvSummary.setText(summary);

            bindEventAndShow(v, null);
            return v;
        }
    }

    private void initView(View rootView) {
        loadShortcutBoxViews(rootView);
        sliderLatestNews = (SliderLayout) rootView.findViewById(R.id.latest_news_slider);
        layoutLoadingNews = (LinearLayout) rootView.findViewById(R.id.loading_news_layout);
        layoutLoadingNews.setVisibility(View.VISIBLE);
        layoutLoadingNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NewsListActivity.class));
            }
        });
    }

    // 加载所有的 shortcutBoxView 组件至 shortcutBoxViewList
    public void loadShortcutBoxViews(View root) {
        YHLog.d(tag(), "loadShortcutBoxViews");
        if (root == null) {
            return;
        }

        shortcutBoxViewList.add(null);
        shortcutBoxViewList.add(null);
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.third_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.forth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.fifth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.sixth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.seventh_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.eighth_shortcut));
    }

    // 提示更新资讯列表
    private static final int MSG_REFRESH_NEWS = 100;

    private Timer timerNewsRefresh;
    private Handler newsRefreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REFRESH_NEWS:
                    // 加载最新资讯
                    MainService.getInstance().getNews(tag(), 0, 5, null);
                    break;
            }
        }
    };

    // 该定时任务当前仅用于通知 handler 来获取最新资讯列表
    private TimerTask newsRefreshTimerTask;

    @Override
    public void onStart() {
        super.onStart();
        YHLog.d(tag(), "onStart");
        reloadShortcuts();
        startNewsRefreshTimer();
    }

    // 启动 滚动新闻 定时器
    private void startNewsRefreshTimer() {
        if (timerNewsRefresh == null) {
            timerNewsRefresh = new Timer();
        }

        if (newsRefreshTimerTask == null) {
            newsRefreshTimerTask = new TimerTask() {
                @Override
                public void run() {
                    newsRefreshHandler.sendEmptyMessage(MSG_REFRESH_NEWS);
                }
            };
        }

        if (timerNewsRefresh != null) {
            isLoadFirst = true;
            timerNewsRefresh.schedule(newsRefreshTimerTask, 5 * 1000, 60 * 1000);
            sliderLatestNews.setVisibility(View.GONE);
            layoutLoadingNews.setVisibility(View.VISIBLE);
        }
    }

    // 停止 滚动新闻 定时器
    private void stopNewsRefreshTimer() {
        if (timerNewsRefresh != null) {
            timerNewsRefresh.cancel();
            timerNewsRefresh = null;
            sliderLatestNews.setVisibility(View.GONE);
            layoutLoadingNews.setVisibility(View.VISIBLE);
        }

        if (newsRefreshTimerTask != null) {
            newsRefreshTimerTask.cancel();
            newsRefreshTimerTask = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        YHLog.d(tag(), "onStop");
        stopNewsRefreshTimer();
    }

    // 重新加载 shortcut box
    public void reloadShortcuts() {
        //如果指定了页号，则在初始化时，会自动加载指定页面下的所有 shortcut 列表
        for (int pos = 0; pos < ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE; ++pos) {
            ShortcutBoxView boxView = shortcutBoxViewList.get(pos);
            if (boxView != null) {
                boxView.hideShortcut();
                Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(PAGE_NO, pos);
                if (shortcut != null) {
                    boxView.setWithShortcut(shortcut);
                }
            }
        }
    }

    @Override
    public boolean refresh(int taskTypeId, Map<String, Object> params) {
        super.refresh(taskTypeId, params);
        switch (taskTypeId) {
            case TaskType.GET_NEWS:
                ApiResponseResult apr = (ApiResponseResult) params.get(MainService.MSG_PARAM_API_RESP);
                if (apr != null && apr.getResult() == 0 && apr.getData() != null
                        && apr.getData().containsKey(ApiConst.PARAM_NEWSES)) {
                    List<News> newses = gson.fromJson(gson.toJson(apr.getData().get(ApiConst.PARAM_NEWSES)),
                            new TypeToken<List<News>>(){}.getType());
                    if (newses != null && newses.size() > 0 && isLoadFirst) {
                        layoutLoadingNews.setVisibility(View.GONE);
                        sliderLatestNews.setVisibility(View.VISIBLE);
                        isLoadFirst = false;
                    }

                    updateLatestNewsSlider(newses);
                }
                break;
        }

        return true;
    }

    // 更新滚动资讯列表
    private void updateLatestNewsSlider(List<News> newses) {
        if (newses == null || newses.size() == 0) {
            return;
        }

        sliderLatestNews.removeAllSliders();
        for (final News news : newses) {
            if (news != null && news.title != null && news.summary != null) {
                LatestNewsSliderView sliderView = new LatestNewsSliderView(getActivity(), news.title, news.summary);
                sliderView.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                    @Override
                    public void onSliderClick(BaseSliderView slider) {
                        Intent intent = new Intent(getActivity(), NewsListActivity.class);
//                        intent.putExtra(NewsDetailActivity.NEWS_ID, news.id);
                        intent.putExtra(NewsDetailActivity.NEWS_URI, news.uri);
                        startActivity(intent);
                    }
                });
                sliderLatestNews.addSlider(sliderView);
            }
        }
        sliderLatestNews.setDuration(10 * 1000);
    }
}
