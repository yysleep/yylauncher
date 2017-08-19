package com.yanhuahealth.healthlauncher.ui.setting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import android.os.Handler;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.sys.CallMgr;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

import java.lang.ref.WeakReference;


public class SosAutoCallActivity extends YHBaseActivity {
    private TextView tvShowTime;
    private TextView tvShowTimeMsg;
    private boolean cancelCall = true;
    int sTime = 5;
    String sosName;
    String sosNumber = "18756952585";
    String sosNumber2;
    boolean stop = true;
    String smsDetail;

    private boolean messageSended = true;

    private final TimeHandler handler = new TimeHandler(this);

    @Override
    protected String tag() {
        return SosAutoCallActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_auto_call);
        initView();
        new TimeThread().start();
    }

    public void initView() {
        tvShowTime = (TextView) findViewById(R.id.sos_auto_time_tv);
        tvShowTimeMsg = (TextView) findViewById(R.id.sos_auto_time_msg_tv);
        TextView tvName = (TextView) findViewById(R.id.sos_auto_name_tv);
        long numContactId = getIntent().getLongExtra(CallMgr.SOS_CONTACT_CALL_ID, -1);
        if (numContactId <= 0) {
            return;
        }
        Contact contact = ContactMgr.getInstance().getContactByContactId(numContactId);
        if (contact == null) {
            return;
        }
        tvName.setText(contact.name);

        // 判断第一个号码为空就使用第二个号码
        if (!contact.phoneOne.equals("") && contact.phoneOne != null) {
            sosNumber = contact.phoneOne;
        } else if (contact.phoneOne.equals("") && !contact.phoneTwo.equals("")) {
            sosNumber = contact.phoneTwo;
        }

        long smsContactId = getIntent().getLongExtra(SmsMgr.SOS_CONTACT_SMS_ID, -1);
        if (smsContactId <= 0 ) {
            return;
        }
        Contact contactSms = ContactMgr.getInstance().getContactByContactId(smsContactId);
        if (contactSms == null) {
            return;
        }

        // 判断第一个号码为空就使用第二个号码
        if (!contactSms.phoneOne.equals("") && contactSms.phoneOne != null) {
            sosNumber2 = contactSms.phoneOne;
        } else if (contactSms.phoneOne.equals("") && !contactSms.phoneTwo.equals("")) {
            sosNumber2 = contactSms.phoneTwo;
        }

        if (getIntent().getStringExtra("SosSmsBody") != null) {
            smsDetail = getIntent().getStringExtra("SosSmsBody");
        }
    }

    public void SosOnClick(View v) {
        switch (v.getId()) {
            case R.id.sos_auto_call_ll:
                if (sosNumber != null) {
                    Intent intentPhone = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + sosNumber));
                    if (ActivityCompat.checkSelfPermission(SosAutoCallActivity.this,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    stop = false;
                    messageSended = false;
                    if (PhoneStatus.checkPhoneSimCard(this)) {
                        startActivity(intentPhone);
                        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
                        smsManager.sendTextMessage(sosNumber2, null, smsDetail, null, null);
                    }
                    finish();
                }
                break;

            case R.id.sos_auto_call_close_ll:
                sTime = 5;
                cancelCall = false;
                finish();
                break;

            default:
                break;

        }
    }

    class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (sTime >= 0 && stop) {
                try {
                    sleep(1000);
                    sTime--;
                    Message msg = new Message();
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class TimeHandler extends Handler {
        private final WeakReference<SosAutoCallActivity> sosAutoActivity;

        public TimeHandler(SosAutoCallActivity activity) {
            sosAutoActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final SosAutoCallActivity activity = sosAutoActivity.get();
            if (activity != null && PhoneStatus.checkPhoneSimCard(activity)) {
                if (activity.sTime > 0) {
                    activity.tvShowTime.setText(activity.sTime + "");
                    activity.tvShowTimeMsg.setText(activity.sTime + "秒后拨打紧急电话");
                } else if (activity.sosNumber != null && activity.cancelCall) {
                    Intent intentPhone = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + activity.sosNumber));
                    if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    activity.startActivity(intentPhone);
                    activity.finish();
                }

                if (activity.sTime <= -1 && activity.messageSended &&
                        activity.sosNumber2 != null && activity.cancelCall && activity.smsDetail != null) {
                    String number = activity.sosNumber2;
                    String text = activity.smsDetail;

                    // 如果设置的信息为空，就不会发送
                    if (text.equals("")) {
                        return;
                    } else {
                        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
                        smsManager.sendTextMessage(number, null, text, null, null);
                        activity.messageSended = false;
                    }
                }
            } else if (activity != null && activity.sTime < 3) {
                activity.finish();
            }
        }
    }

}
