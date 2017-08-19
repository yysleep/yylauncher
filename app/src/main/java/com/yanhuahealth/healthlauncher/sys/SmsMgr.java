package com.yanhuahealth.healthlauncher.sys;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.note.SmsInfo;
import com.yanhuahealth.healthlauncher.model.sms.UserSmsRecord;
import com.yanhuahealth.healthlauncher.ui.note.SendSmsActivity;
import com.yanhuahealth.healthlauncher.utils.DateTimeUtils;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 手机上的短信管理
 */
public class SmsMgr {
    public static final String SOS_CONTACT_SMS_ID = "contactSmsId";
    public static final String TAG = SmsMgr.class.getName();

    private static volatile SmsMgr instance;

    public static SmsMgr getInstance() {
        if (instance == null) {
            synchronized (SmsMgr.class) {
                if (instance == null) {
                    instance = new SmsMgr();
                }
            }
        }
        return instance;
    }

    // 存储所有的用户之间的短信交互记录
    private List<UserSmsRecord> allUserSmsRecords;
    private Lock lockAllUserSmsRecords = new ReentrantLock();

    // 获取所有的用户交互短信记录
    public List<UserSmsRecord> getAllUserSmsRecords() {
        lockAllUserSmsRecords.lock();
        try {
            return allUserSmsRecords;
        } finally {
            lockAllUserSmsRecords.unlock();
        }
    }

    // 删除指定短信标识的单条短信
    public boolean deleteSms(Context context, SmsInfo smsInfo) {

        if (context == null || smsInfo == null || smsInfo.id <= 0) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String uri = "content://sms/";
        int result = contentResolver.delete(Uri.parse(uri), "_id=?", new String[]{String.valueOf(smsInfo.id)});
        YHLog.d(TAG, "deleteSms - result: " + result);

        // 同时从内存中移除该条短信记录
        lockAllUserSmsRecords.lock();
        try {
            if (allUserSmsRecords != null && allUserSmsRecords.size() > 0) {
                for (UserSmsRecord userSmsRecord : allUserSmsRecords) {
                    if (userSmsRecord != null && userSmsRecord.smsList != null && userSmsRecord.smsList.size() > 0) {
                        for (SmsInfo idxSms : userSmsRecord.smsList) {
                            if (idxSms != null && idxSms.id == smsInfo.id) {
                                userSmsRecord.smsList.remove(idxSms);

                                // 如果被删除的短信记录为 UserSmsRecord（会话记录）中的最后一条短信
                                // 则在删除之后，同时移除本条 UserSmsRecord
                                if (userSmsRecord.smsList == null || userSmsRecord.smsList.size() == 0) {
                                    allUserSmsRecords.remove(userSmsRecord);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        } finally {
            lockAllUserSmsRecords.unlock();
        }

        return false;
    }

    // 删除指定会话的所有短信
    public boolean deleteSmsSession(Context context, UserSmsRecord userSmsRecord) {

        if (context == null || userSmsRecord == null || userSmsRecord.threadId <= 0) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String uri = "content://sms/";
        int result = contentResolver.delete(Uri.parse(uri), "thread_id=?", new String[]{String.valueOf(userSmsRecord.threadId)});
        YHLog.d(TAG, "deleteSmsSession - result: " + result);

        // 同时从内存中移除对应的会话记录
        lockAllUserSmsRecords.lock();
        try {
            if (allUserSmsRecords != null && allUserSmsRecords.size() > 0) {
                for (UserSmsRecord idxUserSmsRecord : allUserSmsRecords) {
                    if (idxUserSmsRecord != null && idxUserSmsRecord.threadId == userSmsRecord.threadId) {
                        allUserSmsRecords.remove(idxUserSmsRecord);
                        return true;
                    }
                }
            }
        } finally {
            lockAllUserSmsRecords.unlock();
        }

        return false;
    }

    // 发送余额查询短信
    public boolean sendSmsForBalance(Context context) {

        // 检测手机是否有sim卡
        if (!PhoneStatus.checkPhoneSimCard(context)) {
            return false;
        }

        YHLog.d(TAG, "sendSmsForBalance");

        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telManager != null && telManager.getSimOperator() != null) {
            String operator = telManager.getSimOperator();
            switch (operator) {
                case "46000":
                case "46002":
                case "46007":
                    sendSms(context, "10086", "CXYE");
                    break;

                case "46001":
                    sendSms(context, "10010", "102");
                    break;

                case "46003":
                    sendSms(context, "10001", "102");
                    break;

                default:
                    return false;
            }

            Toast.makeText(context, R.string.query_balance_tip, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    // 发送短信
    public void sendSms(Context context, String phoneNumber, String message) {
        if (phoneNumber == null || message == null) {
            return;
        }

        YHLog.d(TAG, "sendSms - " + phoneNumber + "|" + message);
        SmsManager smsManager = SmsManager.getDefault();
        if (smsManager == null) {
            return;
        }

        // 如果信息过长就分多条发送
        ArrayList<String> list = smsManager.divideMessage(message);
        if (list == null) {
            return;
        }
        for (String text : list) {
            if (text == null) {
                return;
            }
            smsManager.sendTextMessage(phoneNumber, null, text, null, null);
        }

        SmsInfo smsInfo = new SmsInfo();
        smsInfo.smsbody = message;
        smsInfo.phoneNumber = phoneNumber;
        smsInfo.date = DateTimeUtils.getTimeStr(new Date());
        smsInfo.type = LauncherConst.SMS_TYPE_SEND;

        lockAllUserSmsRecords.lock();
        try {
            UserSmsRecord userSmsRecord = getSmsByNumber(phoneNumber);
            if (userSmsRecord != null) {
                if (userSmsRecord.smsList == null) {
                    userSmsRecord.smsList = new ArrayList<>();
                }

                userSmsRecord.smsList.add(0, smsInfo);

                // 并且将该记录移动到最开始的位置
                allUserSmsRecords.remove(userSmsRecord);
                allUserSmsRecords.add(0, userSmsRecord);
            } else {
                userSmsRecord = new UserSmsRecord();
                userSmsRecord.smsList = new ArrayList<>();
                userSmsRecord.smsList.add(smsInfo);
                userSmsRecord.userNumber = smsInfo.phoneNumber;
                userSmsRecord.userName = getContactName(context, userSmsRecord.userNumber);
                allUserSmsRecords.add(0, userSmsRecord);
            }
        } finally {
            lockAllUserSmsRecords.unlock();
        }
    }

    // 新增短信记录
    public int addNewSms(Context context, SmsInfo smsInfo) {
        if (smsInfo == null || smsInfo.phoneNumber == null) {
            return -1;
        }

        lockAllUserSmsRecords.lock();
        try {
            if (allUserSmsRecords == null) {
                allUserSmsRecords = new ArrayList<>();
            }

            for (UserSmsRecord userSmsRecord : allUserSmsRecords) {
                if (userSmsRecord.userNumber != null
                        && userSmsRecord.userNumber.equals(smsInfo.phoneNumber)) {
                    userSmsRecord.smsList.add(0, smsInfo);

                    // 并且将该记录移动到最开始的位置
                    allUserSmsRecords.remove(userSmsRecord);
                    allUserSmsRecords.add(0, userSmsRecord);
                    return 0;
                }
            }

            UserSmsRecord userSmsRecord = new UserSmsRecord();
            userSmsRecord.smsList = new ArrayList<>();
            userSmsRecord.smsList.add(smsInfo);
            userSmsRecord.userNumber = smsInfo.phoneNumber;
            userSmsRecord.userName = getContactName(context, userSmsRecord.userNumber);
            allUserSmsRecords.add(0, userSmsRecord);
        } finally {
            lockAllUserSmsRecords.unlock();
        }

        return 0;
    }

    /**
     * 查询与指定用户的所有短信交互记录
     *
     * @param phoneNumber 对方手机号码
     * @return 与指定的用户的短信交互记录
     */
    public UserSmsRecord getSmsByNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() == 0) {
            return null;
        }

        lockAllUserSmsRecords.lock();
        try {
            if (allUserSmsRecords != null) {
                for (UserSmsRecord smsRecord : allUserSmsRecords) {
                    if (smsRecord != null && smsRecord.userNumber.equals(phoneNumber)) {
                        return smsRecord;
                    }
                }
            }
        } finally {
            lockAllUserSmsRecords.unlock();
        }

        return null;
    }

    // 根据 手机号码 查找用户名
    public String getContactName(Context ctx, String phoneNum) {
        String contactName = null;
        Cursor pCur = ctx.getContentResolver().query(Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNum), new String[]{
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.TYPE, ContactsContract.PhoneLookup.LABEL}, null, null, null);
        if (pCur == null) {
            return null;
        }

        if (pCur.moveToFirst()) {
            contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        }
        pCur.close();

        if (contactName == null || contactName.equals("")) {
            contactName = phoneNum;
        }

        return contactName;
    }

    /**
     * 初始化短信管理
     */
    public void init(final Context ctx) {
        YHLog.i(TAG, "init");

        new Thread(new Runnable() {
            @Override
            public void run() {
                YHLog.i(TAG, "begin load sms");
                ArrayList<SmsInfo> infos = new ArrayList<>();

                // 收件箱信息
                final String SMS_URI_INBOX = "content://sms/";
                ContentResolver cr = ctx.getContentResolver();
                try {
                    String[] projection = new String[]{"_id", "thread_id", "address", "person", "body", "date", "type"};
                    Uri uri = Uri.parse(SMS_URI_INBOX);
                    Cursor cursor = cr.query(uri, projection, null, null, "date desc");
                    if (cursor == null) {
                        YHLog.w(TAG, "load sms from SMS_INBOX failed!");
                        return;
                    }

                    int nameColumn = cursor.getColumnIndex("person");
                    int phoneNumberColumn = cursor.getColumnIndex("address");
                    int smsBodyColumn = cursor.getColumnIndex("body");
                    int dateColumn = cursor.getColumnIndex("date");
                    int typeColumn = cursor.getColumnIndex("type");

                    while (cursor.moveToNext()) {
                        SmsInfo smsInfo = new SmsInfo();
                        smsInfo.name = cursor.getString(nameColumn);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date d = new Date(Long.parseLong(cursor.getString(dateColumn)));
                        smsInfo.date = dateFormat.format(d);
                        smsInfo.phoneNumber = cursor.getString(phoneNumberColumn);
                        smsInfo.smsbody = cursor.getString(smsBodyColumn);
                        smsInfo.id = cursor.getInt(cursor.getColumnIndex("_id"));
                        smsInfo.threadId = cursor.getInt(cursor.getColumnIndex("thread_id"));
                        smsInfo.type = cursor.getString(typeColumn);
                        infos.add(smsInfo);
                    }
                    cursor.close();
                } catch (SQLiteException e) {
                    e.printStackTrace();
                    return;
                }

                // 遍历所有短信记录，整理出对应每一个用户的交互短信列表
                lockAllUserSmsRecords.lock();
                try {
                    allUserSmsRecords = new ArrayList<>();
                    for (SmsInfo smsInfo : infos) {
                        if (smsInfo == null || smsInfo.phoneNumber == null) {
                            continue;
                        }

                        // 用于检查是否 smsInfo 中的用户号码是否已经在 allUserSmsRecords 中存在
                        boolean isExists = false;
                        for (UserSmsRecord userSmsRecord : allUserSmsRecords) {
                            if (smsInfo.threadId == userSmsRecord.threadId) {
                                // 在 allUserSmsRecords 中已经存在对应用户号码的 短信交互记录
                                // 则只需要将该短信 smsInfo 加入与该用户号码的 短信交互记录 列表中
                                userSmsRecord.smsList.add(smsInfo);
                                isExists = true;
                            }
                        }

                        if (!isExists) {
                            // 如果在 allUserSmsRecords 列表中还不存在该用户的短信记录
                            // 则新增，并加入到 allUserSmsRecords 中
                            UserSmsRecord userSmsRecord = new UserSmsRecord();
                            userSmsRecord.threadId = smsInfo.threadId;
                            if (smsInfo.phoneNumber.startsWith("+")) {
                                // 过滤国家码前缀
                                userSmsRecord.userNumber = smsInfo.phoneNumber.substring(3);
                            } else {
                                userSmsRecord.userNumber = smsInfo.phoneNumber;
                            }

                            userSmsRecord.userName = getContactName(ctx, userSmsRecord.userNumber);
                            userSmsRecord.smsList = new ArrayList<>();
                            userSmsRecord.smsList.add(smsInfo);
                            allUserSmsRecords.add(userSmsRecord);
                        }
                    }
                } finally {
                    lockAllUserSmsRecords.unlock();
                }

                YHLog.i(TAG, "end load sms");
            }
        }).start();
    }

    public void sendSmsWithContact(Context context, String contactName, String phoneNumber) {
        if (contactName == null || phoneNumber == null) {
            return;
        }
        Intent sendIntent = new Intent(context, SendSmsActivity.class);
        sendIntent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_NAME, contactName);
        sendIntent.putExtra(LauncherConst.INTENT_PARAM_PHONE_NUMBER, phoneNumber);
        context.startActivity(sendIntent);
    }
}
