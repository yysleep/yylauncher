package com.yanhuahealth.healthlauncher.ui.toolutil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutExtra;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.tool.HomeWatcher;
import com.yanhuahealth.healthlauncher.ui.base.ShortcutBoxView;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.contact.AddContactActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 供大家使用的二级桌面
 */
public class SecondActivity extends YHBaseActivity implements HomeWatcher.OnHomePressedListener {

    private EditText etTitle;
    private SharedPreferences.Editor saveTitle;
    private String title;
    private String secondTitle;
    private HomeWatcher homeWatcher;
    List<Integer> shortcuts;

    /**
     * 如果指定了页号，则在初始化时，会自动加载指定页面下的所有 shortcut 列表
     */
    private int pageNo = ShortcutMgr.getInstance().getShortcutPageNum() - 1;

    // 对应于 16 个 shortcut box
    private List<ShortcutBoxView> shortcutBoxViewList = new ArrayList<>();

    private Shortcut shortcutFolder;

    @Override
    protected String tag() {
        return SecondActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        loadShortcutBoxViews();
        String content;
        if (getIntent() != null) {
            shortcutFolder = getIntent().getParcelableExtra(ShortcutConst.PARAM_SHORTCUT);
            if (shortcutFolder != null) {
                content = shortcutFolder.title;
            } else {
                content = "";
            }
        } else {
            content = "new";
        }

        SharedPreferences oursTitle = getSharedPreferences(content + "Default", MODE_PRIVATE);
        saveTitle = oursTitle.edit();
        saveTitle.apply();

        View llAllView = findViewById(R.id.second_activity_ll);
        etTitle = (EditText) findViewById(R.id.second_activity_title_tv);
        title = oursTitle.getString("Title", "tag");
        etTitle.setCursorVisible(false);
        etTitle.clearFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        etTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTitle.setCursorVisible(true);
            }
        });

        llAllView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTitle.setCursorVisible(false);
            }
        });

        shortcutInit();
        getNewTitle();
    }

    // 重新加载 shortcut box
    public void reloadShortcuts() {
        if (shortcutFolder.localId <= 0) {
            YHLog.w(tag(), "reloadShortcuts - shortcutFolder.localId <= 0");
            return;
        }

        for (int pos = 0; pos < shortcutFolder.shortcuts.size(); ++pos) {
            ShortcutBoxView boxView = shortcutBoxViewList.get(pos);
            if (boxView != null) {
                boxView.hideShortcut();
                Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(
                        ShortcutConst.DEFAULT_CHILD_PAGE, pos, (int) shortcutFolder.localId);
                if (shortcut != null) {
                    boxView.setWithShortcut(shortcut);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        YHLog.d(tag(), "onStart");
        reloadShortcuts();
        homeWatcherStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        homeWatcherStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!(etTitle.getText().toString().equals(""))) {
            saveTitle.putString("Title", etTitle.getText().toString());
            saveTitle.commit();
            if (shortcutFolder != null) {
                shortcutFolder.title = etTitle.getText().toString();
                ShortcutMgr.getInstance().updateShortcut(shortcutFolder);
            }
        } else {
            saveTitle.remove(getNewTitle());
            saveTitle.commit();
        }
    }

    protected String getNewTitle() {
        if (title != null && !(title.equals("tag"))) {
            etTitle.setText(title);
        } else if (secondTitle != null && title != null && title.equals("tag")) {
            etTitle.setText(secondTitle);
        }

        return "";
    }

    // 加载所有的 shortcutBoxView 组件至 shortcutBoxViewList
    public void loadShortcutBoxViews() {
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_first_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_second_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_third_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_forth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_fifth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_sixth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_seventh_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_eighth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_ninth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_tenth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_eleventh_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_twelfth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_thirteeth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_fourteeth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_fifteenth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) findViewById(R.id.second_activity_sixteenth_shortcut));
    }

    protected ShortcutBoxView getShortcutBoxView(int pos) {
        return shortcutBoxViewList.get(pos);
    }

    /**
     * 新增 shortcut，主要有派生类调用
     * 将指定的 shortcut 添加到指定的 shortcutBoxView 上
     */
    protected boolean addShortcut(Shortcut shortcut) {

        if (shortcut == null || shortcut.posInPage < 0 || shortcut.posInPage >= shortcutBoxViewList.size()) {
            return false;
        }

        if (shortcut.extra != null && shortcut.extra.param != null && !shortcut.extra.param.equals("")) {
            ArrayList<String> contactParam = ContactMgr.getInstance().parseContactParam(shortcut.extra.param);
            if (contactParam != null && contactParam.size() >= 2) {
                Long contactId = Long.valueOf(contactParam.get(0));
                if (ContactMgr.getInstance().getContactByContactId(contactId) != null) {
                    shortcut.title = ContactMgr.getInstance().getContactByContactId(contactId).name;
                } else {
                    shortcut.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
                    shortcut.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
                    shortcut.setIcon(R.drawable.ic_add);
                    shortcut.title = "添加";
                }
            }
        }

        ShortcutBoxView shortcutBoxView = shortcutBoxViewList.get(shortcut.posInPage);
        if (shortcutBoxView != null) {
            shortcutBoxView.setWithShortcut(shortcut);
        }

        return true;
    }

    public void shortcutInit() {
        if (getIntent() != null) {
            int shortcutFolderLocalId = getIntent().getIntExtra(ShortcutConst.PARAM_SHORTCUT_FOLDER_ID, -1);
            if (shortcutFolderLocalId > 0) {
                shortcutFolder = ShortcutMgr.getInstance().getShortcut(shortcutFolderLocalId);
            }
        }

        if (shortcutFolder != null) {
            secondTitle = shortcutFolder.title;
            shortcuts = shortcutFolder.shortcuts;
            if (shortcuts != null && shortcuts.size() > 0) {
                for (int shortcutLocalId : shortcuts) {
                    addShortcut(ShortcutMgr.getInstance().getShortcut(shortcutLocalId));
                }
            }
        }
    }

    // 捕捉HOME键 短按
    @Override
    public void onHomePressed() {
        finish();
    }

    @Override
    public void onHomeLongPressed() {

    }

    private void homeWatcherStart() {
        homeWatcher = new HomeWatcher(this);
        homeWatcher.setOnHomePressedListener(this);
        homeWatcher.startWatch();
    }

    private void homeWatcherStop() {
        homeWatcher.setOnHomePressedListener(null);
        homeWatcher.stopWatch();
    }

}
