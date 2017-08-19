package com.yanhuahealth.healthlauncher.ui.note;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 * Created by Administrator on 2016/5/3.
 */
public class ImSmsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_sms);
        if (getIntent() != null && getIntent().getStringExtra("url_data") != null) {
            WebView wbImSms;
            try {
                wbImSms = (WebView) findViewById(R.id.im_sms_wv);
                wbImSms.setWebViewClient(new WebViewClient() {
                    // 设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });
                // 设置WebView属性,运行执行js脚本
                wbImSms.getSettings().setJavaScriptEnabled(true);
                // 调用loadView方法为WebView加入链接
                wbImSms.loadUrl(getIntent().getStringExtra("url_data"));
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }
}
