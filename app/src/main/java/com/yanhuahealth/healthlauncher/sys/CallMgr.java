package com.yanhuahealth.healthlauncher.sys;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telecom.Call;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.model.contact.RecordEntity;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.MainActivity;
import com.yanhuahealth.healthlauncher.ui.contact.ContactDetailActivity;
import com.yanhuahealth.healthlauncher.ui.toolutil.SecondActivity;
import com.yanhuahealth.healthlauncher.utils.Utilities;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/22.
 */
public class CallMgr {
    // 一键呼救电话的ID
    public static final String SOS_CONTACT_CALL_ID = "contactId";
    private static volatile CallMgr instance ;

    public static CallMgr getInstance() {
        if (instance == null) {
            synchronized (CallMgr.class) {
                if (instance == null) {
                    instance = new CallMgr();
                }
            }
        }
        return instance;
    }

    public ArrayList<RecordEntity> RecordCalls(Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            cursor = contentResolver.query(
                    CallLog.Calls.CONTENT_URI, null, null, null,
                    CallLog.Calls.DATE + " desc");

            if (cursor == null) {
                return null;
            }
            ArrayList<RecordEntity> recordList = new ArrayList<>();
            while (cursor.moveToNext()) {
                RecordEntity record = new RecordEntity();
                record._id = cursor.getString(cursor.getColumnIndex("_id"));
                record.name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                record.number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                record.lDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                record.type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                record.callNew = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NEW));
                record.numTime = 1;
                recordList.add(record);
            }

            return getNewList(recordList);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public ArrayList<RecordEntity> getNewList(ArrayList<RecordEntity> lis) {
        if (lis == null) {
            return null;
        }
        ArrayList<RecordEntity> newList = lis;
        ArrayList<RecordEntity> testList = new ArrayList<>();
        //遍历数据集合
        for (int i = 1; i < newList.size(); i++) {

            if (newList.get(i - 1).name != null && newList.get(i).name != null) {
                if (newList.get(i - 1).name.equals(newList.get(i).name) &&
                        ((newList.get(i - 1).type != 3 && newList.get(i).type != 3) || (newList.get(i - 1).type == newList.get(i).type))) {
                    newList.get(i - 1).numTime++;
                    testList.add(newList.get(i));
                    newList.remove(newList.get(i));
                    i--;
                }
            } else if (newList.get(i - 1).number.equals(newList.get(i).number) &&
                    ((newList.get(i - 1).type != 3 && newList.get(i).type != 3) || (newList.get(i - 1).type == newList.get(i).type))) {
                newList.get(i - 1).numTime++;
                newList.remove(newList.get(i));
                i--;
            }
        }

        return newList;
    }

    public ArrayList<RecordEntity> CallLogCalls(Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            cursor = contentResolver.query(
                    CallLog.Calls.CONTENT_URI, null, null, null,
                    CallLog.Calls.DATE + " desc");

            if (cursor == null) {
                return null;
            }
            ArrayList<RecordEntity> recordList = new ArrayList<>();
            while (cursor.moveToNext()) {
                RecordEntity record = new RecordEntity();
                record._id = cursor.getString(cursor.getColumnIndex("_id"));
                record.name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                record.number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                record.lDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                record.type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                record.callNew = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NEW));
                record.numTime = 1;
                recordList.add(record);
            }

            return recordList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
