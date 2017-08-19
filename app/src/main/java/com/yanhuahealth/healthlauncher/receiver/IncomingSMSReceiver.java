package com.yanhuahealth.healthlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.MainService;

import java.util.HashMap;
import java.util.Map;

/**
 * 新的 SMS 的接收器
 */
public class IncomingSMSReceiver extends BroadcastReceiver {

    public String tag() {
        return IncomingSMSReceiver.class.getName();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle smsBundle = intent.getExtras();
        if (smsBundle != null) {
            final Object[] pdusObj = (Object[]) smsBundle.get("pdus");
            if (pdusObj != null) {
                for (Object aPdusObj : pdusObj) {
                    SmsMessage curMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String phoneNumber = curMessage.getDisplayOriginatingAddress();
                    String message = curMessage.getDisplayMessageBody();
                    YHLog.d(tag(), "onReceive - " + phoneNumber + "|" + message);
                    Toast.makeText(context, "receive sms from " + phoneNumber + " with content: " + message,
                            Toast.LENGTH_LONG).show();

                    // 发送广播事件
                    Map<String, Object> eventInfo = new HashMap<>();
                    eventInfo.put(EventType.KEY_SYS_SMS_RECEIVED_SENDER_NUMBER, phoneNumber);
                    eventInfo.put(EventType.KEY_SYS_SMS_RECEIVED_CONTENT, message);
                    MainService.getInstance().sendBroadEvent(
                            new BroadEvent(EventType.SYS_SMS_RECEIVED, eventInfo));
                }
            }
        }
    }
}
