package com.yanhuahealth.healthlauncher.ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.sys.CallMgr;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.contact.ContactListActivity;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;


/**
 * SOS 一键报警设置页面
 */
public class SosSettingActivity extends YHBaseActivity {

    private DialogUtil tvFirstDialog;
    private DialogUtil tvSecondDialog;
    private DialogUtil firstDeleteContactDialog;
    private DialogUtil secondDeleteSmsDialog;

    private TextView tvFirstAddContact;
    private ImageView ivRemoveCallContact;
    private TextView tvFirstCallContactName;
    private TextView tvFirstCallContactNumber;
    private LinearLayout firstLinearLayout;

    private TextView tvSecondAddContact;
    private ImageView ivRemoveSmsContact;
    private TextView tvSecondSmsContactName;
    private TextView tvSecondSmsContactNumber;
    private LinearLayout secondLinearLayout;

    private EditText etSaveSms;

    private ViewGroup vgFirst;
    private ViewGroup vgSecond;
    private ViewGroup vgThird;

    SharedPreferences sosPerson;
    SharedPreferences.Editor savePerson;

    @Override
    protected String tag() {
        return SosSettingActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_setting);
        sosPerson = getSharedPreferences("SosInfo", MODE_PRIVATE);
        savePerson = sosPerson.edit();

        String smsBody2 = sosPerson.getString("SmsBody", "");
        if (sosPerson.getLong(CallMgr.SOS_CONTACT_CALL_ID, -1) > 0 &&
                sosPerson.getLong(SmsMgr.SOS_CONTACT_SMS_ID, -1) > 0
                && getIntent().getBooleanExtra("changeSos", true) && sosPerson.getBoolean("changeSosPerson", true)) {
            Intent intent = new Intent(SosSettingActivity.this, SosAutoCallActivity.class);
            intent.putExtra(CallMgr.SOS_CONTACT_CALL_ID, sosPerson.getLong(CallMgr.SOS_CONTACT_CALL_ID, -1));
            intent.putExtra(SmsMgr.SOS_CONTACT_SMS_ID, sosPerson.getLong(SmsMgr.SOS_CONTACT_SMS_ID, -1));
            intent.putExtra("SosSmsBody", smsBody2);
            startActivity(intent);
            finish();
        } else {
            initView();
            if (getIntent().getIntExtra("MainOrNot", 0) != 0) {
                savePerson.putInt("MainOrNot", getIntent().getIntExtra("MainOrNot", 0));
                savePerson.apply();
            } else {
                savePerson.putInt("MainOrNot", getIntent().getIntExtra("MainOrNot", 0));
                savePerson.apply();
            }
            if (sosPerson.getString("SmsBody", "").equals("")) {
                etSaveSms.setText("我遇到麻烦了，快来帮我(从延华桌面紧急求助发出)");
            } else {
                etSaveSms.setText(sosPerson.getString("SmsBody", ""));
            }
            // 第一个界面删除联系人点击图标二次确认
            ivRemoveCallContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstDeleteContactDialog.showFirstStyleDialog("删除信息", "确认删除联系人?", "确认删除");
                }
            });

            firstDeleteContactDialog = new DialogUtil(SosSettingActivity.this, new DialogUtil.OnDialogUtilListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.dialog_btn_one_tv:
                            firstLinearLayout.setVisibility(View.GONE);
                            tvFirstAddContact.setVisibility(View.VISIBLE);
                            savePerson.remove(CallMgr.SOS_CONTACT_CALL_ID);
                            savePerson.putBoolean("changeSosPerson", false);
                            savePerson.apply();
                            firstDeleteContactDialog.dismiss();
                            break;

                        default:
                            break;
                    }
                }
            });

            // 第二个界面删除联系人点击图标二次确认
            ivRemoveSmsContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    secondDeleteSmsDialog.showFirstStyleDialog("删除信息", "确认删除联系人?", "确认删除");
                }
            });

            secondDeleteSmsDialog = new DialogUtil(SosSettingActivity.this, new DialogUtil.OnDialogUtilListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.dialog_btn_one_tv:
                            secondLinearLayout.setVisibility(View.GONE);
                            tvSecondAddContact.setVisibility(View.VISIBLE);
                            savePerson.remove(SmsMgr.SOS_CONTACT_SMS_ID);
                            savePerson.apply();
                            secondDeleteSmsDialog.dismiss();
                            break;

                        default:
                            break;
                    }
                }
            });
        }
    }

    private void initView() {
        // 第一个界面组件初始化
        tvFirstAddContact = (TextView) findViewById(R.id.add_call_contact_tv);
        tvFirstCallContactName = (TextView) findViewById(R.id.call_contact_name_tv);
        tvFirstCallContactNumber = (TextView) findViewById(R.id.call_contact_phone_tv);
        ivRemoveCallContact = (ImageView) findViewById(R.id.remove_call_contact_iv);
        firstLinearLayout = (LinearLayout) findViewById(R.id.first_contact_detail);

        // 第二个界面初始化
        tvSecondAddContact = (TextView) findViewById(R.id.add_sms_contact_tv);
        tvSecondSmsContactName = (TextView) findViewById(R.id.sms_contact_name_tv);
        tvSecondSmsContactNumber = (TextView) findViewById(R.id.sms_contact_phone_tv);
        ivRemoveSmsContact = (ImageView) findViewById(R.id.remove_sms_contact_iv);
        secondLinearLayout = (LinearLayout) findViewById(R.id.second_contact_detail);

        NavBar navBar = new NavBar(this);
        navBar.setTitle("一键呼救");
        navBar.hideRight();

        // first step
        vgFirst = (ViewGroup) findViewById(R.id.first_layout);
        vgFirst.setVisibility(View.VISIBLE);

        vgSecond = (ViewGroup) findViewById(R.id.second_layout);
        vgSecond.setVisibility(View.GONE);

        vgThird = (ViewGroup) findViewById(R.id.third_layout);
        vgThird.setVisibility(View.GONE);

        Button btnToSecond = (Button) findViewById(R.id.to_second_btn);
        btnToSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vgFirst.setVisibility(View.GONE);
                vgSecond.setVisibility(View.VISIBLE);
                if (!(sosPerson.getString("selectName2", "").equals(""))
                        && !(sosPerson.getString("contactId2", "").equals(""))) {
                    tvSecondAddContact.setVisibility(View.GONE);
                    secondLinearLayout.setVisibility(View.VISIBLE);
                    tvSecondSmsContactName.setText(sosPerson.getString("selectName2", ""));
                    tvSecondSmsContactNumber.setText(sosPerson.getString("contactId2", ""));
                }
            }
        });

        // 第一个界面添加紧急电话联系人的点击事件
        tvFirstAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvFirstDialog.showOneButtonDialog("选择联系人");
            }
        });

        // 第一个界面添加联系人的Dialog点击事件
        tvFirstDialog = new DialogUtil(SosSettingActivity.this, new DialogUtil.OnDialogUtilListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_btn_one_tv:
                        Intent intentChoseContact = new Intent(SosSettingActivity.this, ContactListActivity.class);
                        intentChoseContact.putExtra(ContactMgr.SOS_CHOOSE_CONTACT, ContactMgr.SOS_CHOOSE_CONTACT_NUM);
                        startActivity(intentChoseContact);
                        savePerson.putBoolean("changeSosPerson", false);
                        savePerson.apply();
                        tvFirstDialog.dismiss();
                        finish();
                        break;

                    default:
                        break;
                }
            }
        });

        // second step
        Button btnBackToFirst = (Button) findViewById(R.id.back_first_btn);
        Button btnToThird = (Button) findViewById(R.id.to_third_btn);
        btnBackToFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vgFirst.setVisibility(View.VISIBLE);
                vgSecond.setVisibility(View.GONE);
            }
        });

        btnToThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vgFirst.setVisibility(View.GONE);
                vgSecond.setVisibility(View.GONE);
                vgThird.setVisibility(View.VISIBLE);
            }
        });

        // 第二个界面添加紧急短信联系人的点击事件
        tvSecondAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSecondDialog.showOneButtonDialog("选择联系人");
            }
        });

        // 第二个界面添加联系人的Dialog点击事件
        tvSecondDialog = new DialogUtil(SosSettingActivity.this, new DialogUtil.OnDialogUtilListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()) {
                    case R.id.dialog_btn_one_tv:
                        Intent intent1 = new Intent(SosSettingActivity.this, ContactListActivity.class);
                        intent1.putExtra(ContactMgr.SOS_CHOOSE_CONTACT, 11);
                        startActivity(intent1);
                        savePerson.putBoolean("changeSosPerson", false);
                        savePerson.apply();
                        tvSecondDialog.dismiss();
                        finish();
                        break;

                    default:
                        break;
                }
            }
        });

        // third step
        Button btnBackSecond = (Button) findViewById(R.id.back_second_btn);
        Button btnFinish = (Button) findViewById(R.id.finish_btn);
        TextView tvSaveSms = (TextView) findViewById(R.id.save_sms_tv);
        etSaveSms = (EditText) findViewById(R.id.help_sms_tv);

        // 第三个界面返回第二个界面的点击事件
        btnBackSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vgFirst.setVisibility(View.GONE);
                vgSecond.setVisibility(View.VISIBLE);
                vgThird.setVisibility(View.GONE);
            }
        });

        // 第三个界面保存信息的点击事件
        tvSaveSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击完成可以保存信息
                String smsBody = etSaveSms.getText().toString().trim();
                // todo
                savePerson.putString("SmsBody", smsBody);
                savePerson.commit();
                Toast.makeText(SosSettingActivity.this, "信息保存成功", Toast.LENGTH_SHORT).show();
                YHLog.d("tag", smsBody);
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String smsBody = etSaveSms.getText().toString().trim();
                // todo
                savePerson.putString("SmsBody", smsBody);
                savePerson.commit();

                if (sosPerson.getInt("MainOrNot", 0) != 0) {
                    startActivity(new Intent(SosSettingActivity.this, SettingActivity.class));
                    savePerson.remove("MainOrNot");
                    savePerson.remove("changeSosPerson");
                    savePerson.apply();
                    finish();
                } else {
                    savePerson.remove("changeSosPerson");
                    savePerson.apply();
                    finish();
                }
            }
        });
    }

    // 第一个界面添加联系人（选择联系人）实现方法
    private void firstSelectContact() {
        Intent intent = getIntent();
        if (intent.getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1) > 0) {
            tvFirstAddContact.setVisibility(View.GONE);
            firstLinearLayout.setVisibility(View.VISIBLE);
            long contactCallId = intent.getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1);
            savePerson.putLong(CallMgr.SOS_CONTACT_CALL_ID, contactCallId);
            savePerson.apply();
            setContactInfoCall(contactCallId);
        }
    }

    // 第二个界面添加联系人（选择联系人）实现方法
    private void secondSelectContact() {
        Intent intent = getIntent();
        if (intent.getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1) > 0) {
            tvSecondAddContact.setVisibility(View.GONE);
            secondLinearLayout.setVisibility(View.VISIBLE);
            long contactSmsId = intent.getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1);
            savePerson.putLong(SmsMgr.SOS_CONTACT_SMS_ID, contactSmsId);
            savePerson.apply();
            setContactInfoSms(contactSmsId);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (sosPerson.getLong(CallMgr.SOS_CONTACT_CALL_ID, 0) > 0) {
            tvFirstAddContact.setVisibility(View.GONE);
            firstLinearLayout.setVisibility(View.VISIBLE);
            setContactInfoCall(sosPerson.getLong(CallMgr.SOS_CONTACT_CALL_ID, -1));
        }
        if (sosPerson.getLong(SmsMgr.SOS_CONTACT_SMS_ID, 0) > 0) {
            setContactInfoSms(sosPerson.getLong(SmsMgr.SOS_CONTACT_SMS_ID, -1));
            tvSecondAddContact.setVisibility(View.GONE);
            secondLinearLayout.setVisibility(View.VISIBLE);
        }

        switch (getIntent().getIntExtra("returnAddContact", 0)) {
            case 2:
                firstSelectContact();
                break;

            case 11:
                secondSelectContact();
                vgFirst.setVisibility(View.GONE);
                vgSecond.setVisibility(View.VISIBLE);
                vgThird.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    public void setContactInfoCall(long id) {
        Contact contact = ContactMgr.getInstance().getContactByContactId(id);
        if (contact != null) {
            tvFirstCallContactName.setText(contact.name);

            // 判断第一个号码为空就使用第二个号码
            if (!contact.phoneOne.equals("") && contact.phoneOne != null) {
                tvFirstCallContactNumber.setText(contact.phoneOne);
            } else if (contact.phoneOne.equals("") && !contact.phoneTwo.equals("")) {
                tvFirstCallContactNumber.setText(contact.phoneTwo);
            }
        }
    }

    public void setContactInfoSms(long id) {
        Contact contact = ContactMgr.getInstance().getContactByContactId(id);
        if (contact != null) {
            tvSecondSmsContactName.setText(contact.name);

            // 判断第一个号码为空就使用第二个号码
            if (!contact.phoneOne.equals("") && contact.phoneOne != null) {
                tvSecondSmsContactNumber.setText(contact.phoneOne);
            } else if (contact.phoneOne.equals("") && !contact.phoneTwo.equals("")) {
                tvSecondSmsContactNumber.setText(contact.phoneTwo);
            }
        }
    }

}
