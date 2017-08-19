package com.yanhuahealth.healthlauncher.ui.note;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

import static com.yanhuahealth.healthlauncher.ui.note.SmsListActivity.*;


/**
 * 回复短信
 */
public class ReturnSmsActivity extends YHBaseActivity implements View.OnClickListener {
    private String phoneNumber;

    private EditText sms_detail_et;

    @Override
    protected String tag() {
        return ReturnSmsActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_sms);
        initView();
    }

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.hideRight();
        String name = getIntent().getStringExtra("name");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        if(name == null && phoneNumber != null ) {
            // 如果号码前面有+86 就取消掉（phoneNumber1 是去掉+86的号码）
            String phoneNumber1;
            if (phoneNumber.startsWith("+")) {
                phoneNumber1 = phoneNumber.substring(3);

            } else {
                phoneNumber1 = phoneNumber;
            }
            navBar.setTitle(phoneNumber1);
        } else{
            navBar.setTitle(name);
        }

        sms_detail_et= (EditText) findViewById(R.id.sms_return_detail_et);
        findViewById(R.id.send_sms_btn).setOnClickListener(this);
    }

    public static final String ACTION_SMS_SENT = "SMS_SENT";

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(smsSentReceiver, new IntentFilter(ACTION_SMS_SENT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsSentReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_sms_btn:
                senMessage();
                break;

            default:
                break;
        }
    }

    private void senMessage() {
        message = sms_detail_et.getText().toString().trim();

        // 判断有无sim卡
        if (!PhoneStatus.checkPhoneSimCard(ReturnSmsActivity.this)) {
            return;
        }
        if (message.equals("")) {
            Toast.makeText(ReturnSmsActivity.this, "信息内容不能为空", Toast.LENGTH_LONG).show();
        } else {
            SmsMgr.getInstance().sendSms(this, phoneNumber, message);
            finish();
        }
    }

    // 短信内容
    private String message;

    // 短信发送成功的接收器
    private BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case RESULT_OK:
                    YHLog.d(tag(), "send sms to " + phoneNumber + " ok");
                    ContentValues values = new ContentValues();
                    values.put("address", phoneNumber);
                    values.put("body", message);
                    context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                    ReturnSmsActivity.this.setResult(RESULT_SEND_SMS_SUCC);
                    finish();
                    break;

                default:
                    break;
            }
        }
    };
}
