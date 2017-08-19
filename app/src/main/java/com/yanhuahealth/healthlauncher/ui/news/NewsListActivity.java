package com.yanhuahealth.healthlauncher.ui.news;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.api.ApiConst;
import com.yanhuahealth.healthlauncher.api.ApiResponseResult;
import com.yanhuahealth.healthlauncher.model.news.News;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.TaskType;
import com.yanhuahealth.healthlauncher.sys.task.LauncherTaskItem;
import com.yanhuahealth.healthlauncher.tool.XListView;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 新闻列表页
 */
public class NewsListActivity extends YHBaseActivity implements XListView.IXListViewListener {

    @Override
    protected String tag() {
        return NewsListActivity.class.getName();
    }

    // 默认的加载更多加载的数量
    private static final int DEFAULT_COUNT_PER_LOAD = 15;

    public static final String LOAD_LATEST = "latest";
    public static final String LOAD_MORE = "more";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);
        initView();

        // 加载最新资讯
        MainService.getInstance().getNews(tag(), 0, DEFAULT_COUNT_PER_LOAD, LOAD_LATEST);

        String newsUri = getIntent().getStringExtra(NewsDetailActivity.NEWS_URI);
        if (newsUri != null) {
            Intent intent = new Intent(this, NewsDetailActivity.class);
            intent.putExtra(NewsDetailActivity.NEWS_URI, newsUri);
            startActivity(intent);
        }
    }

    // 新闻列表
    private XListView lvNews;
    private NewsListAdapter adapterNews;

    private View viewFooter;

    // 加载进度
    private LinearLayout layoutLoading;

    // 加载更多的进度和提示
    private TextView tvLoadMoreTip;
    private ProgressBar pbarLoadMore;

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle("热点");
        navBar.setRight(R.drawable.ic_refresh);

        layoutLoading = (LinearLayout) findViewById(R.id.loading_layout);
        layoutLoading.setVisibility(View.VISIBLE);

        lvNews = (XListView) findViewById(R.id.news_lv);

        // 隐藏XListView底面加载更多
        lvNews.setPullLoadEnable(false);
        lvNews.setXListViewListener(this);

        viewFooter = LayoutInflater.from(this).inflate(R.layout.news_list_footer, lvNews, false);
        tvLoadMoreTip = (TextView) viewFooter.findViewById(R.id.load_more_tip_tv);
        tvLoadMoreTip.setText("点击加载更多新闻");

        pbarLoadMore = (ProgressBar) viewFooter.findViewById(R.id.load_progress);
        pbarLoadMore.setVisibility(View.GONE);

        ImageView refreshNewsList = (ImageView) findViewById(R.id.nav_right_iv);

        // 新闻列表底部加载更多的点击事件
        lvNews.addFooterView(viewFooter);
        viewFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 加载更多
                pbarLoadMore.setVisibility(View.VISIBLE);
                tvLoadMoreTip.setText("正在加载更多新闻");
                MainService.getInstance().getNews(tag(),
                        adapterNews.getCount(), DEFAULT_COUNT_PER_LOAD, LOAD_MORE);
            }
        });

        lvNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News news = (News) adapterNews.getItem(position - 1);
                if (news == null) {
                    return;
                }
                Intent intent = new Intent(NewsListActivity.this, NewsDetailActivity.class);
                intent.putExtra(NewsDetailActivity.NEWS_ID, news.id);
                intent.putExtra(NewsDetailActivity.NEWS_URI, news.uri);
                startActivity(intent);
            }
        });
        adapterNews = new NewsListAdapter(this);
        lvNews.setAdapter(adapterNews);
        lvNews.setVisibility(View.GONE);

        refreshNewsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainService.getInstance().getNews(tag(), 0, DEFAULT_COUNT_PER_LOAD, LOAD_LATEST);
            }
        });
    }

    // 下拉刷新
    @Override
    public void onRefresh() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        // 检测是否有网络连接
        if (!networkInfo.isConnectedOrConnecting() && !isWiFiActive()) {
            lvNews.stopDefaultRefresh();
            Toast.makeText(NewsListActivity.this, "请检查您的网络是否开启", Toast.LENGTH_LONG).show();
        } else {

            // 如果有网络就执行刷新操作并关闭刷新
            MainService.getInstance().getNews(tag(), 0, DEFAULT_COUNT_PER_LOAD, LOAD_LATEST);
            lvNews.stopRefresh();
        }
    }

    @Override
    public void onLoadMore() {

    }

    // 新闻列表项适配器
    class NewsListAdapter extends BaseAdapter {

        private Context context;
        private List<News> lstNews;

        public NewsListAdapter(Context context) {
            this.context = context;
            this.lstNews = new ArrayList<>();
        }

        public void addNewsToHead(News news) {
            if (news != null) {
                lstNews.add(0, news);
            }
        }

        public void addNewsToTail(News news) {
            if (news != null) {
                lstNews.add(news);
            }
        }

        // 清空所有资讯列表
        public void clearNews() {
            lstNews.clear();
        }

        @Override
        public int getCount() {
            return lstNews.size();
        }

        @Override
        public Object getItem(int position) {

            if (position >= 0) {
                return lstNews.get(position);
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.news_list_item, parent, false);
            }

            final News news = (News) getItem(position);
            if (news == null) {
                return convertView;
            }

            TextView tvTitle = (TextView) convertView.findViewById(R.id.title_tv);
            tvTitle.setText(news.title);

            if (news.createTime != null) {
                TextView tvTime = (TextView) convertView.findViewById(R.id.time_tv);
                String[] arrTime = news.createTime.split(" ");
                tvTime.setText(arrTime[0]);
            }

            int attachNum = 0;
            if (news.attach1 != null && news.attach1.startsWith("http")) {
                attachNum++;
            }

            if (news.attach2 != null && news.attach2.startsWith("http")) {
                attachNum++;
            }

            if (news.attach3 != null && news.attach3.startsWith("http")) {
                attachNum++;
            }

            LinearLayout layoutThumb1 = (LinearLayout) convertView.findViewById(R.id.thumb1_layout);
            LinearLayout layoutThumb2 = (LinearLayout) convertView.findViewById(R.id.thumb2_layout);
            LinearLayout layoutThumb3 = (LinearLayout) convertView.findViewById(R.id.thumb3_layout);
            FrameLayout layoutImage = (FrameLayout) convertView.findViewById(R.id.thumb_layout);

            switch (attachNum) {
                case 1:
                    layoutThumb1.setVisibility(View.VISIBLE);
                    layoutThumb2.setVisibility(View.GONE);
                    layoutThumb3.setVisibility(View.GONE);

                    ImageView ivThumb1First = (ImageView) convertView.findViewById(R.id.thumb1_first_iv);
                    ImageLoader.getInstance().displayImage(news.attach1, ivThumb1First);
                    break;

                case 2:
                    layoutThumb2.setVisibility(View.VISIBLE);
                    layoutThumb1.setVisibility(View.GONE);
                    layoutThumb3.setVisibility(View.GONE);

                    ImageView ivThumb2First = (ImageView) convertView.findViewById(R.id.thumb2_first_iv);
                    ImageLoader.getInstance().displayImage(news.attach1, ivThumb2First);

                    ImageView ivThumb2Second = (ImageView) convertView.findViewById(R.id.thumb2_second_iv);
                    ImageLoader.getInstance().displayImage(news.attach2, ivThumb2Second);
                    break;

                case 3:
                    layoutThumb3.setVisibility(View.VISIBLE);
                    layoutThumb1.setVisibility(View.GONE);
                    layoutThumb2.setVisibility(View.GONE);

                    ImageView ivThumb3First = (ImageView) convertView.findViewById(R.id.thumb3_first_iv);
                    ImageLoader.getInstance().displayImage(news.attach1, ivThumb3First);

                    ImageView ivThumb3Second = (ImageView) convertView.findViewById(R.id.thumb3_second_iv);
                    ImageLoader.getInstance().displayImage(news.attach2, ivThumb3Second);

                    ImageView ivThumb3Third = (ImageView) convertView.findViewById(R.id.thumb3_third_iv);
                    ImageLoader.getInstance().displayImage(news.attach3, ivThumb3Third);
                    break;
            }

            // 所有图片是放在一个frameLayout里面
            layoutImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewsImageActivity.startActivity(NewsListActivity.this, lstNews.get(position));
                }
            });

            return convertView;
        }
    }

    @Override
    public boolean refresh(int rt, Map<String, Object> params) {

        super.refresh(rt, params);
        switch (rt) {
            case TaskType.GET_NEWS:
                lvNews.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.GONE);

                pbarLoadMore.setVisibility(View.GONE);
                tvLoadMoreTip.setText("点击加载更多新闻");

                ApiResponseResult apr = (ApiResponseResult) params.get(MainService.MSG_PARAM_API_RESP);
                if (apr != null && apr.getResult() == 0 && apr.getData() != null
                        && apr.getData().containsKey(ApiConst.PARAM_NEWSES)) {

                    LauncherTaskItem taskItem = (LauncherTaskItem) params.get(MainService.MSG_PARAM_TASK);
                    String extraParam = taskItem.getExtraParam();

                    List<News> newses = gson.fromJson(gson.toJson(apr.getData().get(ApiConst.PARAM_NEWSES)),
                            new TypeToken<List<News>>() {
                            }.getType());

                    if (newses == null || newses.size() == 0) {
                        break;
                    }

                    boolean isLoadLatest = false;
                    if (extraParam.equals(LOAD_LATEST)) {
                        isLoadLatest = true;
                    }

                    if (isLoadLatest) {

                        // 按照降序排列
                        Collections.reverse(newses);
                        adapterNews.clearNews();
                    }

                    for (News news : newses) {
                        // 对 attach 列表进行处理
                        if (news.attach1 == null) {
                            if (news.attach2 != null) {
                                news.attach1 = news.attach2;
                                news.attach2 = null;
                                if (news.attach3 != null) {
                                    news.attach2 = news.attach3;
                                    news.attach3 = null;
                                }
                            } else if (news.attach3 != null) {
                                news.attach1 = news.attach3;
                                news.attach3 = null;
                            }
                        } else if (news.attach2 == null) {
                            if (news.attach3 != null) {
                                news.attach2 = news.attach3;
                                news.attach3 = null;
                            }
                        }

                        if (isLoadLatest) {
                            adapterNews.addNewsToHead(news);
                        } else {
                            adapterNews.addNewsToTail(news);
                        }
                    }

                    // 判断加载的条数小于15就隐藏底部的viewFooter
                    if (newses.size() < DEFAULT_COUNT_PER_LOAD) {
                       lvNews.removeFooterView(viewFooter);
                    }

                    adapterNews.notifyDataSetChanged();
                }
                break;
        }

        return true;
    }

    // 判断无线网
    public boolean isWiFiActive() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] infos = connectivity.getAllNetworkInfo();
            if (infos != null) {
                for (NetworkInfo ni : infos) {
                    if (ni.getTypeName().equals("WIFI") && ni.isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
