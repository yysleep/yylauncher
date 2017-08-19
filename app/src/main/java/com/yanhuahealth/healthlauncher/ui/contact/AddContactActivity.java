package com.yanhuahealth.healthlauncher.ui.contact;

import android.content.ContentUris;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.Utilities;

import java.io.InputStream;


/**
 * 添加联系人
 */
public class AddContactActivity extends YHBaseActivity {
    int shortcutId;

    @Override
    protected String tag() {
        return AddContactActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        NavBar navBar = new NavBar(this);
        navBar.setTitle("添加联系人");
        navBar.hideRight();
        if (getIntent().getIntExtra(LauncherConst.INTENT_PARAM_SHORTCUT_ID, -1) == -1) {
            finish();
            return;
        }

        shortcutId = getIntent().getIntExtra(LauncherConst.INTENT_PARAM_SHORTCUT_ID, -1);
    }

    public void yanhuaOnClick(View v) {
        switch (v.getId()) {
            case R.id.add_new_contact_btn:
                Intent intent = new Intent(this, NewContactActivity.class);
                intent.putExtra(LauncherConst.INTENT_PARAM_SHORTCUT_ID, shortcutId);
                startActivityForResult(intent, ContactMgr.REQUEST_CODE_CONTACT);
                break;

            case R.id.select_from_contacts_btn:
                Intent intentSelect = new Intent(this, ContactListActivity.class);
                intentSelect.putExtra(LauncherConst.INTENT_PARAM_SELECT_CONTACT, ContactMgr.ADD_CONTACT_FOR_LIST);
                startActivityForResult(intentSelect, ContactMgr.REQUEST_CODE_CONTACT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ContactMgr.REQUEST_CODE_CONTACT && resultCode == ContactMgr.RESULT_CODE_CONTACT) {
            ContactMgr.getInstance().updateShortcut(AddContactActivity.this, data.getStringExtra(LauncherConst.INTENT_PARAM_CONTACT_NAME),
                    data.getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID,-1),
                    data.getLongExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID,-1), shortcutId);
            finish();
        }
    }

}
