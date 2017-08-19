package com.yanhuahealth.healthlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.model.note.SmsInfo;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.utils.DateTimeUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/2/24.
 */
public class SMSChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Shortcut shortcutSms = ShortcutMgr.getInstance().getShortcut(
                ShortcutConst.SMS_PAGE, ShortcutConst.SMS_POS);
        if (shortcutSms == null) {
            return;
        }
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            shortcutSms.numSign = shortcutSms.numSign + 1;

            // 接收由SMS传来的数据
            Bundle bundle = intent.getExtras();

            // 判断是否有数据
            if (bundle != null) {
                // 通过pdus可以获得接收到的所有短信消息
                Object[] pdus = (Object[]) bundle.get("pdus");

                // 构建短信对象array，并依据收到对象长度来创建array大小
                if (pdus == null) {
                    return;
                }
                SmsMessage[] messages = new SmsMessage[pdus.length];

                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                // 将发来的信息合并自定义信息于StringBuilder当中
                String phoneNumber = null;
                StringBuilder sb = new StringBuilder();
                for (SmsMessage message : messages) {
                    // 接收短信的号码
                    phoneNumber = message.getDisplayOriginatingAddress();
                    sb.append(message.getDisplayMessageBody());
                }

                // 添加到缓存中
                SmsInfo smsInfo = new SmsInfo();
                smsInfo.phoneNumber = phoneNumber;
                smsInfo.smsbody = sb.toString();
                smsInfo.date = DateTimeUtils.getTimeStr(new Date());
                smsInfo.type = LauncherConst.SMS_TYPE_RECV;
                SmsMgr.getInstance().addNewSms(context, smsInfo);
            }
        } else {
            shortcutSms.numSign = getSMSCount(context);
        }

        ShortcutMgr.getInstance().updateShortcut(shortcutSms);
        Map<String, Object> eventInfo = new HashMap<>();
        eventInfo.put(EventType.SYS_SMS_RECEIVED + "", getSMSCount(context));
        MainService.getInstance().sendBroadEvent(new BroadEvent(EventType.SYS_SMS_RECEIVED, eventInfo));
    }

    public int getSMSCount(Context context) {
        Uri uri = Uri.parse("content://sms");
        ContentResolver cr = context.getContentResolver();
        int smsCount = 0;
        Cursor cursor = cr.query(uri, null, "type =1 and read=0", null, null);
        if (cursor != null) {
            smsCount = cursor.getCount();
            cursor.close();
        }
        return smsCount;
    }
}
