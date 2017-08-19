package com.yanhuahealth.healthlauncher.ui.news;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

import java.util.Timer;
import java.util.TimerTask;


/**
 * 资讯详情页
 */
public class NewsDetailActivity extends YHBaseActivity {

    @Override
    protected String tag() {
        return NewsDetailActivity.class.getName();
    }

    public static final String NEWS_ID = "news_id";
    public static final String NEWS_URI = "news_uri";

    // 资讯标识
    private String newsUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        // 热点界面传递过来的URI
        newsUri = getIntent().getStringExtra(NEWS_URI);
        if (newsUri == null) {
            finish();
            return;
        }

        initView();
    }

    // 断网刷新新闻
    private TextView tvRefreshNews;

    // 加载进度
    private LinearLayout layoutLoading;

    // 加载超时
    private LinearLayout layoutLoadFailed;

    // web view
    private WebView wvNews;

    private Timer timerNews;
    private static final long LOAD_TIMEOUT = 10 * 1000;
    private static final int MSG_LOAD_TIMEOUT = 100;
    private Handler handlerLoadNews = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_TIMEOUT:
                    layoutLoadFailed.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.GONE);
                    wvNews.setVisibility(View.GONE);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle("详情");
        navBar.hideRight();

        // 断网刷新
        tvRefreshNews = (TextView)findViewById(R.id.tv_refresh_news);

        layoutLoading = (LinearLayout) findViewById(R.id.loading_layout);
        layoutLoading.setVisibility(View.VISIBLE);

        layoutLoadFailed = (LinearLayout) findViewById(R.id.load_failed_layout);
        layoutLoadFailed.setVisibility(View.GONE);

        wvNews = (WebView) findViewById(R.id.news_wv);
        wvNews.setVisibility(View.GONE);
        wvNews.addJavascriptInterface(new JINewsDetail(this), "newsDetail");

        WebSettings webSettings = wvNews.getSettings();
        webSettings.setUserAgentString(webSettings.getUserAgentString() + " YanHua");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        wvNews.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                YHLog.d(tag(), "onPageStarted - " + url);

                timerNews = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        timerNews.cancel();
                        timerNews.purge();
                        handlerLoadNews.sendEmptyMessage(MSG_LOAD_TIMEOUT);
                    }
                };
                timerNews.schedule(timerTask, LOAD_TIMEOUT);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                YHLog.d(tag(), "onReceivedError - " + error);
                super.onReceivedError(view, request, error);
                layoutLoadFailed.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.GONE);
                wvNews.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                YHLog.d(tag(), "onPageFinished - " + url);
                super.onPageFinished(view, url);
                wvNews.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.GONE);
                layoutLoadFailed.setVisibility(View.GONE);
                timerNews.cancel();
                timerNews.purge();
            }
        });

        // 断网后点击重新加载
        tvRefreshNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initView();
            }
        });

        // 根据传递过来的URI得到详细内容
        wvNews.loadUrl(newsUri);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (timerNews != null) {
            timerNews.cancel();
            timerNews.purge();
        }
    }

    public class JINewsDetail {
        private Context context;

        public JINewsDetail(Context ctx) {
            this.context = ctx;
        }

        @JavascriptInterface
        public void loadFailed() {
            YHLog.d(tag(), "JINewsDetail - loadFailed");
            timerNews.cancel();
            timerNews.purge();
            handlerLoadNews.sendEmptyMessage(MSG_LOAD_TIMEOUT);
        }
    }
}
