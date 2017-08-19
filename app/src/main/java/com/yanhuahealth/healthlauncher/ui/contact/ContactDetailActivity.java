package com.yanhuahealth.healthlauncher.ui.contact;

import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;
import com.yanhuahealth.healthlauncher.utils.Utilities;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * 联系人详情页面
 */
public class ContactDetailActivity extends YHBaseActivity {
    private Button callPhoneBtn;
    private Button sendMessageBtn;
    private TextView contactName;
    private TextView contactNumber;
    private TextView contactNumberTwo;
    private ImageView contactHeadImage;
    private DialogUtil dialogPhone;
    private DialogUtil dialogMessage;
    private DialogUtil dialogEdit;
    private View divider;
    private Shortcut shortcutDetail;

    private String phoneNumber = null;
    private String phoneNumberTwo = null;
    private String phoneName = null;
    private long contactId = 0;
    private long rawContactId;
    private Bitmap headImage;
    private int shortcutId;

    @Override
    protected String tag() {
        return ContactDetailActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        initView();

        if (getIntent().getIntExtra(LauncherConst.INTENT_PARAM_SHORTCUT_ID, -1) != -1) {
            shortcutId = getIntent().getIntExtra((LauncherConst.INTENT_PARAM_SHORTCUT_ID), -1);
            shortcutDetail = ShortcutMgr.getInstance().getShortcut(shortcutId);
            ArrayList<String> contactParam = ContactMgr.getInstance().parseContactParam(shortcutDetail.extra.param);
            if (contactParam == null || contactParam.size() < 2) {
                finish();
                return;
            }

            contactId = Long.valueOf(contactParam.get(0));
            rawContactId = Long.valueOf(contactParam.get(1));

            // todo
            Contact contact = ContactMgr.getInstance().getContactByContactId(contactId);
            if (contact != null && !contact.name.equals(shortcutDetail.title)) {
                ContactMgr.getInstance().updateShortcut(ContactDetailActivity.this,
                        contact.name, contactId, rawContactId, shortcutId);
            }
        } else {
            // 得到联系人信息（姓名,号码，头像） 传入一个空的shortcut 在下面的方法里已经初始化 下文可以直接使用
            contactId = getIntent().getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1);
            if (contactId <= 0) {
                finish();
                return;
            }

            rawContactId = getIntent().getLongExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1);
            if (rawContactId <= 0) {
                finish();
                return;
            }
        }

        // 删除或者编辑联系人
        deleteForDBorScv();

        // 打电话 发短信
        doublePhoneNumberCallOrSms();

        YHLog.i(tag(), "onCreate - contactId:" + contactId + "|rawContactId:" + rawContactId);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 给联系人塞上信息
        setContactInfo();
    }

    // 跳转到编辑界面
    public void editContact() {
        Intent intent = new Intent(this, EditContactActivity.class);
        intent.putExtra(LauncherConst.INTENT_PARAM_SHORTCUT_ID, shortcutId);
        intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contactId);
        intent.putExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, rawContactId);
        startActivityForResult(intent, ContactMgr.REQUEST_CODE_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ContactMgr.REQUEST_CODE_CONTACT && resultCode == ContactMgr.RESULT_CODE_CONTACT) {
            ContactMgr.getInstance().updateShortcut(ContactDetailActivity.this, data.getStringExtra(LauncherConst.INTENT_PARAM_CONTACT_NAME),
                    data.getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1),
                    data.getLongExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1), shortcutId);
            finish();
        }
    }

    // 初始化控件
    public void initView() {
        sendMessageBtn = (Button) findViewById(R.id.send_message_btn);
        callPhoneBtn = (Button) findViewById(R.id.call_phone_btn);
        contactName = (TextView) findViewById(R.id.contacts_name_tv);
        contactNumber = (TextView) findViewById(R.id.contacts_number_tv);
        contactNumberTwo = (TextView) findViewById(R.id.contacts_number_two_tv);
        contactHeadImage = (ImageView) findViewById(R.id.detail_head_image_iv);
        divider = findViewById(R.id.detail_divider_vw);
        NavBar navBar = new NavBar(this);
        navBar.setTitle("联系人详情");
        ImageView rightView = (ImageView) findViewById(R.id.nav_right_iv);
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEdit.showSecondStyleDialog("选择", "删除联系人", "编辑");
            }
        });
    }

    // 删除联系人和编辑联系人的Dialog
    public void deleteForDBorScv() {
        if (getIntent() != null && getIntent().getStringExtra(LauncherConst.INTENT_FROM_CONTACT_LIST) != null
                && getIntent().getStringExtra(LauncherConst.INTENT_FROM_CONTACT_LIST).equals("INTENT_FROM_CONTACT_LIST")) {
            dialogEdit = new DialogUtil(ContactDetailActivity.this, new DialogUtil.OnDialogUtilListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.dialog_btn_one_tv:
                            if (getIntent().getLongExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1) > 0) {
                                ContactMgr.getInstance().deleteContactForDB(ContactDetailActivity.this,
                                        getIntent().getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1),
                                        getIntent().getLongExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1));
                            }
                            dialogEdit.dismiss();
                            finish();
                            break;

                        case R.id.dialog_btn_two_tv:
                            editContact();
                            dialogEdit.dismiss();
                            break;
                    }
                }
            });
        } else {
            dialogEdit = new DialogUtil(ContactDetailActivity.this, new DialogUtil.OnDialogUtilListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.dialog_btn_one_tv:
                            // 是否在删除后直接跳转到主界面或者二级界面
                            boolean backToActivity = true;
                            ContactMgr.getInstance().deleteContactForScv(ContactDetailActivity.this, backToActivity);
                            dialogEdit.dismiss();
                            finish();
                            break;

                        case R.id.dialog_btn_two_tv:
                            editContact();
                            dialogEdit.dismiss();
                            break;
                    }
                }
            });
        }
    }

    // 2个号码的打电话和发短信Dialog
    public void doublePhoneNumberCallOrSms() {
        // 打电话Dialog
        dialogPhone = new DialogUtil(ContactDetailActivity.this, new DialogUtil.OnDialogUtilListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_btn_one_tv:
                        if (phoneNumber == null) {
                            return;
                        }
                        Intent intentPhone = new Intent("android.intent.action.CALL", Uri.parse("tel:" +
                                phoneNumber));
                        if (PhoneStatus.checkPhoneSimCard(ContactDetailActivity.this)) {
                            startActivity(intentPhone);
                        }
                        dialogPhone.dismiss();
                        break;

                    case R.id.dialog_btn_two_tv:
                        if (phoneNumberTwo == null) {
                            return;
                        }
                        Intent intentPhoneTwo = new Intent("android.intent.action.CALL", Uri.parse("tel:" +
                                phoneNumberTwo));
                        if (PhoneStatus.checkPhoneSimCard(ContactDetailActivity.this)) {
                            startActivity(intentPhoneTwo);
                        }
                        dialogPhone.dismiss();
                        break;
                }
            }
        });
        // 多个号码时弹出选择框选择号码，这是发短信按钮
        dialogMessage = new DialogUtil(ContactDetailActivity.this, new DialogUtil.OnDialogUtilListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_btn_one_tv:
                        SmsMgr.getInstance().sendSmsWithContact(ContactDetailActivity.this, phoneName, phoneNumber);
                        dialogMessage.dismiss();
                        break;

                    case R.id.dialog_btn_two_tv:
                        SmsMgr.getInstance().sendSmsWithContact(ContactDetailActivity.this, phoneName, phoneNumberTwo);
                        dialogMessage.dismiss();
                        break;
                }
            }
        });
    }

    public void yanhuaOnClick(View v) {
        switch (v.getId()) {

            case R.id.call_phone_btn:
                // 如果有2个号码 就弹出dialog
                if (phoneNumber != null && !phoneNumber.equals("") && (phoneNumberTwo != null && !phoneNumberTwo.equals(""))) {
                    dialogPhone.showSecondStyleDialog("打电话", phoneNumber, phoneNumberTwo);
                } else {

                    if ((phoneNumber == null || phoneNumber.equals("")) && (phoneNumberTwo != null && !phoneNumberTwo.equals(""))) {
                        // 如果第一个没有号码，第二个有号码就把第二个赋值给第一个
                        phoneNumber = phoneNumberTwo;
                        Intent intentPhone = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber));
                        if (PhoneStatus.checkPhoneSimCard(ContactDetailActivity.this)) {
                            startActivity(intentPhone);
                        }
                    } else if (phoneNumber != null && !phoneNumber.equals("")) {
                        Intent intentPhone = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber));
                        if (PhoneStatus.checkPhoneSimCard(ContactDetailActivity.this)) {
                            startActivity(intentPhone);
                        }
                    }
                }
                break;

            case R.id.send_message_btn:
                // 对号码个数进行判断 两个号码时弹出选择框，一个号码时直接响应发短信事件
                if (phoneNumber == null && phoneNumberTwo == null) {
                    return;
                }
                if (phoneNumber != null && !phoneNumber.equals("") && (phoneNumberTwo != null && !phoneNumberTwo.equals(""))) {
                    dialogMessage.showSecondStyleDialog("发短信", phoneNumber, phoneNumberTwo);
                } else if ((phoneNumber == null || phoneNumber.equals("")) && phoneNumberTwo != null && !phoneNumberTwo.equals("")) {
                    // 第一个号码为空，第二个不为空，就把第二个值赋值给第一个
                    phoneNumber = phoneNumberTwo;
                    SmsMgr.getInstance().sendSmsWithContact(ContactDetailActivity.this, phoneName, phoneNumber);
                    finish();
                } else if (phoneNumber != null && !phoneNumber.equals("") && (phoneNumberTwo == null || phoneNumberTwo.equals(""))) {
                    SmsMgr.getInstance().sendSmsWithContact(ContactDetailActivity.this, phoneName, phoneNumber);
                    finish();
                }
                break;

            default:
                break;
        }
    }

    // 给联系人塞上信息
    public void setContactInfo() {
        Contact contact = ContactMgr.getInstance().getContactByContactId(contactId);
        if (contact == null) {
            boolean backToActivity = false;
            Toast.makeText(ContactDetailActivity.this, "该联系人已经被第三方应用删除", Toast.LENGTH_LONG).show();
            ContactMgr.getInstance().deleteContactForScv(ContactDetailActivity.this, false);
            return;
        }

        phoneName = contact.name;
        phoneNumber = contact.phoneOne;
        phoneNumberTwo = contact.phoneTwo;
        if (contactId > 0) {
            contactName.setText(phoneName);
            if (phoneNumber != null && !phoneNumber.equals("null")) {
                contactNumber.setText(phoneNumber);
            }
            if (phoneNumberTwo != null && !phoneNumberTwo.equals("null")) {
                contactNumberTwo.setText(phoneNumberTwo);
            }
        }

        // 当从首页跳转时头像获取方式如下
        if (shortcutDetail != null) {
            if (shortcutDetail.icon == null) {
                headImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_head_image);
            } else {
                headImage = shortcutDetail.icon;
            }
        } else if (contactId > 0) {
            // 当从联系人列表选择跳转时头像获取方式如下
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri);
            if (input != null) {
                headImage = Utilities.createCircleBitmap(BitmapFactory.decodeStream(input));
            }
        }

        if (headImage != null) {
            contactHeadImage.setImageBitmap(headImage);
        } else {
            headImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_head_image);
            contactHeadImage.setImageResource(R.drawable.ic_head_image);
        }

        // 当联系人无号码时显示页面 todo
        if (phoneName == null && (phoneNumber == null || phoneNumberTwo == null)) {
            callPhoneBtn.setVisibility(View.INVISIBLE);
            sendMessageBtn.setVisibility(View.INVISIBLE);
            divider.setVisibility(View.INVISIBLE);
            contactHeadImage.setVisibility(View.INVISIBLE);
            Toast.makeText(ContactDetailActivity.this, "此联系人没有号码信息", Toast.LENGTH_LONG).show();
        }
    }

}
