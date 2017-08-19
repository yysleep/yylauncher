package com.yanhuahealth.healthlauncher.ui.toolutil;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 * 通用的 WEB 展示页面
 */
public class BaseWebActivity extends YHBaseActivity {

    @Override
    protected String tag() {
        return BaseWebActivity.class.getName();
    }

    private Shortcut shortcut;

    public static final String PATH = "PATH";
    private String path;

    public static final String TITLE = "title";
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_web);

        shortcut = getIntent().getParcelableExtra(ShortcutConst.PARAM_SHORTCUT);
        if (shortcut == null) {
            title = getIntent().getStringExtra(TITLE);
            if (title == null || title.length() == 0) {
                YHLog.w(tag(), "onCreate, no title");
                finish();
                return;
            }

            path = getIntent().getStringExtra(PATH);
        } else {
            path = getPath();
            title = shortcut.title;
        }

        if (path == null || !path.startsWith("http")) {
            YHLog.w(tag(), "onCreate, invalid path");
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        // 导航栏设置
        NavBar navBar = new NavBar(this);
        navBar.setTitle(title);
        navBar.hideRight();

        WebView wv = (WebView) findViewById(R.id.base_web_wv);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        WebSettings webSettings = wv.getSettings();

        // 开启 JS 调用
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);

        wv.loadUrl(path);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 返回值是true的时候控制去 WebView 打开，为 false 调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
    }

    private String getPath() {
        if (getIntent() != null) {
            if (shortcut.type == ShortcutType.HELP) {
                return "http://dev.laoyou99.cn:28099/launcher/helpcenter/index.html";
            }
        }

        return null;
    }
}
