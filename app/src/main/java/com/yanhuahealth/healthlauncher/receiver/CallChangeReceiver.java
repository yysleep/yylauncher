package com.yanhuahealth.healthlauncher.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/2/26.
 */
public class CallChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Shortcut shortcutCall = ShortcutMgr.getInstance().getShortcut(ShortcutConst.PHONE_PAGE, ShortcutConst.PHONE_POS);
        if (shortcutCall == null) {
            return;
        }
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            shortcutCall.numSign = shortcutCall.numSign + 1;
        } else {
            shortcutCall.numSign = readMissCall(context);
        }
        if (intent.getAction().equals("yhlauncher.delete.callrecord") && shortcutCall.numSign > 0) {
            shortcutCall.numSign--;
        }
        ShortcutMgr.getInstance().updateShortcut(shortcutCall);
        Map<String, Object> eventInfo = new HashMap<>();
        eventInfo.put(EventType.SYS_CALL_RECEIVED + "", "test");
        MainService.getInstance().sendBroadEvent(new BroadEvent(EventType.SYS_CALL_RECEIVED, eventInfo));
    }

    private int readMissCall(Context context) {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{
                CallLog.Calls.TYPE
        }, " type=? and new=?", new String[]{
                CallLog.Calls.MISSED_TYPE + "", "1"
        }, "date desc");

        if (cursor != null) {
            result = cursor.getCount();
            cursor.close();
        }
        return result;
    }
}
