package com.yanhuahealth.healthlauncher.ui.base;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;

/**
 * 用于测试的类
 */
public class TestBaseActivity extends YHBaseActivity {

    @Override
    protected String tag() {
        return TestBaseActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_base);

        Shortcut shortcut = getIntent().getParcelableExtra(ShortcutConst.PARAM_SHORTCUT);



        shortcut.title = "老黄历";
        ShortcutBoxView firstShortcutBoxView = (ShortcutBoxView) findViewById(R.id.first_shortcut);
        firstShortcutBoxView.setWithShortcut(shortcut);

        shortcut = new Shortcut();
        shortcut.title = "工具";
        shortcut.iconUrl = "http://www.iconpng.com/png/stickers/tools.png";
        ShortcutBoxView secondShortcutBoxView = (ShortcutBoxView) findViewById(R.id.second_shortcut);
        secondShortcutBoxView.setWithShortcut(shortcut);
    }
}
