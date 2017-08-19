package com.yanhuahealth.healthlauncher.sys;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutExtra;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.MainActivity;
import com.yanhuahealth.healthlauncher.ui.contact.AddContactActivity;
import com.yanhuahealth.healthlauncher.ui.contact.ContactDetailActivity;
import com.yanhuahealth.healthlauncher.ui.toolutil.SecondActivity;
import com.yanhuahealth.healthlauncher.utils.PinYinUtil;
import com.yanhuahealth.healthlauncher.utils.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 联系人管理类
 */
public class ContactMgr {

    // 当外部应用改边联系人数据库时 才会触发onChange方法
    public boolean isChange = true;

    // startActivityForResult提供2个参数
    public static final int REQUEST_CODE_CONTACT = 100;
    public static final int RESULT_CODE_CONTACT = 101;
    public static final int ADD_CONTACT_FOR_LIST = 99;
    public static final String SOS_CHOOSE_CONTACT = "SOS_CHOOSE_CONTACT";
    public static final int SOS_CHOOSE_CONTACT_NUM = 2;

    public static final String ACTION_UPDATE_CONTACT = "com.yanhuahealth.healthlauncher.contact.loaded";

    public static final String UPDATE_CONTACTLIST = "com.yanhuahealth.update_contactlist";

    private static volatile ContactMgr instance;

    public static ContactMgr getInstance() {
        if (instance == null) {
            synchronized (ContactMgr.class) {
                if (instance == null) {
                    instance = new ContactMgr();
                }
            }
        }
        return instance;
    }

    private String tag() {
        return ContactMgr.class.getName();
    }

    private ArrayList<Contact> allContacts;
    private Lock lockContacts = new ReentrantLock();

    public ArrayList<Contact> getContactList() {

        lockContacts.lock();
        try {
            return allContacts;
        } finally {
            lockContacts.unlock();
        }
    }

    /**
     * 初始化加载联系人列表至内存
     * 比较耗时，不可以放到 UI 线程中执行
     *
     * @param context
     */
    public void init(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                lockContacts.lock();
                try {
                    allContacts = getContactsInApplication(context);
                } finally {
                    lockContacts.unlock();
                }
            }
        }).start();
    }

//    // 先讲联系人的信息移除 在根据根据联系人标识删除指定的联系人
//    public void deleteContactWithId(Context context, String contactId) {
//        if (contactId == null || contactId.length() == 0) {
//            YHLog.w(tag(), "deleteContactWithId - contactId invalid");
//            return;
//        }
//        context.getContentResolver().delete(ContactsContract.Contacts.CONTENT_URI,
//                "_id = ?", new String[]{contactId});
//    }

    // 删除联系人 从数据库删
    public void deleteContactForDB(Context context, long contactId, long rawContactId) {
        Contact contact = getContactByRawContactId(rawContactId);
        context.getContentResolver().delete(ContentUris.withAppendedId
                (ContactsContract.RawContacts.CONTENT_URI, rawContactId), null, null);

        lockContacts.lock();
        try {
            allContacts.remove(contact);
            // 更新shortcut界面
            Bundle bundle = new Bundle();
            bundle.putLong(LauncherConst.INTENT_PARAM_CONTACT_ID, contactId);
            bundle.putLong(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, rawContactId);
            ShortcutMgr.getInstance().updateShortcutofTopContact(bundle);
            ShortcutMgr.getInstance().updateShortcutToSecond(bundle);
        } finally {
            lockContacts.unlock();
        }
    }

    // 删除联系人 删除快捷方式
    public void deleteContactForScv(Activity activity, boolean backToActivity) {
        Shortcut shortcut = activity.getIntent().getParcelableExtra(ShortcutConst.PARAM_SHORTCUT);
        if (shortcut != null) {
            Shortcut srcShortcut = ShortcutMgr.getInstance().getShortcut(shortcut.localId);
            if (srcShortcut != null) {
                srcShortcut.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
                srcShortcut.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
                srcShortcut.setIcon(R.drawable.ic_add);
                srcShortcut.title = "添加";
            }

            if (backToActivity) {
                if (shortcut.parentLocalId > 0) {
                    ShortcutMgr.getInstance().updateShortcutOfChild(srcShortcut);
                    // 跳转到家人二级页面
                    Intent intent = new Intent(activity, SecondActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(ShortcutConst.PARAM_SHORTCUT_FOLDER_ID, shortcut.parentLocalId);
                    activity.startActivity(intent);
                } else {
                    ShortcutMgr.getInstance().updateShortcut(srcShortcut);
                    // 跳转到首页添加联系人
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.putExtra("FragmentPageNumber", 100);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                }
            } else {
                if (shortcut.parentLocalId > 0) {
                    ShortcutMgr.getInstance().updateShortcutOfChild(srcShortcut);
                } else {
                    ShortcutMgr.getInstance().updateShortcut(srcShortcut);
                }
            }
        }
    }

    /**
     * 解析联系人参数
     *
     * @param param 传入的参数格式为 contactId|rawContactId|rawContactId|...
     * @return 返回字符串数组
     */
    public ArrayList<String> parseContactParam(String param) {
        if (param == null || param.length() == 0) {
            return null;
        }

        ArrayList<String> contactParams = new ArrayList<>();
        String[] arrParams = param.split("\\|");
        Collections.addAll(contactParams, arrParams);

        return contactParams;
    }

    /**
     * 根据 rawContactId 获取联系人信息
     *
     * @param rawContactId 联系人的 raw contact id
     * @return 返回对应的联系人信息
     */
    public Contact getContactByRawContactId(long rawContactId) {

        if (rawContactId <= 0) {
            return null;
        }

        lockContacts.lock();
        try {
            for (Contact contact : allContacts) {
                if (contact != null && contact.rawContactId != 0 && contact.rawContactId == rawContactId) {
                    return contact;
                }
            }
        } finally {
            lockContacts.unlock();
        }

        return null;
    }

    /**
     * 根据 contactId 获取联系人信息
     *
     * @param contactId 联系人信息
     * @return 返回对应的联系人信息
     */
    public Contact getContactByContactId(long contactId) {

        if (contactId <= 0) {
            return null;
        }

        lockContacts.lock();
        try {
            for (Contact contact : allContacts) {
                if (contact != null && contact.contactId != 0 && contact.contactId == contactId) {
                    return contact;
                }
            }
        } finally {
            lockContacts.unlock();
        }

        return null;
    }

    /*
    *   通过contactId等到联系人的姓名和号码 返回的是list 长度可能为2或者3 第一个是name 第二个是号码1 第三个是号码2（可能没有）
    *   查询联系人信息
    *
    *   Map<String, String>
    *   put("name", "");
    *   put("phone1",
    */
    public ArrayList<String> getContactInfo(Context context, long contactId) {
        ArrayList<String> contact = new ArrayList<>();
        String name = null;
        String phoneNumber = null;
        String phoneNumberTwo;
        ContentResolver resolver = context.getContentResolver();
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, " sort_key asc");
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                if (name == null) {
                    name = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (name == null) {
                        name = "";
                    }

                    contact.add(name);
                }

                if (phoneNumber == null || phoneNumber.equals("")) {
                    phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex
                            (ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (phoneNumber != null && !phoneNumber.equals("")) {
                        contact.add(phoneNumber);
                    }
                } else {
                    phoneNumberTwo = phoneCursor.getString(phoneCursor.getColumnIndex
                            (ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (phoneNumberTwo != null && !phoneNumberTwo.equals("")) {
                        contact.add(phoneNumberTwo);
                    }
                }
            }

            phoneCursor.close();
        }

        return contact;
    }

    // 获取联系人列表
    public ArrayList<Contact> getContactsInApplication(Context context) {
        ArrayList<Contact> contacts = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        // "sort_key asc" 按照拼音和英文排序
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name;
                String pinyinName = null;
                String firstNumber = null;
                String secondNumber = null;
                Bitmap photo = null;
                long rawContactId = 0;
                long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (name != null && !name.equals("") && !name.equals("null")) {
                    pinyinName = PinYinUtil.getInstance().getPingYin(name);
                }
                Cursor rawContactCur = resolver.query(ContactsContract.RawContacts.CONTENT_URI,

                        new String[]{ContactsContract.RawContacts._ID},
                        ContactsContract.RawContacts.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(contactId)},
                        null);
                if (rawContactCur != null && rawContactCur.moveToFirst()) {
                    rawContactId = rawContactCur.getLong(rawContactCur
                            .getColumnIndex(ContactsContract.RawContacts._ID));
                    Cursor numberCurcor = resolver.query(ContactsContract.Data.CONTENT_URI, null,
                            ContactsContract.Data.RAW_CONTACT_ID + " = ?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                            new String[]{String.valueOf(rawContactId), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE}, null);
                    if (numberCurcor != null) {
                        while (numberCurcor.moveToNext()) {
                            if (firstNumber == null || firstNumber.equals("")) {
                                firstNumber = numberCurcor.getString(numberCurcor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            } else if (secondNumber == null || secondNumber.equals("")) {
                                secondNumber = numberCurcor.getString(numberCurcor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }
                        }
                        numberCurcor.close();
                    }

                    // 当从联系人列表选择跳转时头像获取方式如下
                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
                    if (input != null) {
                        photo = Utilities.createCircleBitmap(BitmapFactory.decodeStream(input));
                    }
                }

                if (rawContactCur != null) {
                    rawContactCur.close();
                }

                if (name != null && !name.equals("") && ((firstNumber != null &&
                        !firstNumber.equals("")) || (secondNumber != null && !secondNumber.equals("")))) {
                    contacts.add(new Contact(contactId, rawContactId, name, pinyinName, firstNumber, secondNumber, photo));
                }
            }
            cursor.close();
        }

        return contacts;
    }

    // 添加联系人到数据库 并且返回一个rawContactId
    public long addContactToDB(Context context, String contactName,
                               String contactNumberOne, String contactNumberTwo, Bitmap headImage) {

        String pinyinName = null;

        if ((contactNumberOne == null || contactNumberOne.equals("")) && (contactNumberTwo != null && !contactNumberTwo.equals(""))) {
            contactNumberOne = contactNumberTwo;
            contactNumberTwo = null;
        }
        if (contactNumberOne != null && contactNumberTwo != null && contactNumberOne.equals(contactNumberTwo)) {
            contactNumberTwo = null;
        }
        try {
            if ((contactName == null || contactName.equals("")) && (contactNumberOne == null || contactNumberOne.equals(""))
                    && (contactNumberTwo == null || contactNumberTwo.equals(""))) {

                Toast.makeText(context, "姓名和号码没有填写完整", Toast.LENGTH_SHORT).show();
                return -1l;
            }

            ContentValues values = new ContentValues();
            Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);
            if (rawContactId <= 0) {
                YHLog.e(tag(), "add raw contact failed!!!");
                return -1;
            }

            if (contactName != null && !(contactName.equals(""))) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
                pinyinName = PinYinUtil.getInstance().getPingYin(contactName);
            }

            if (contactNumberOne != null && !contactNumberOne.equals("")) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumberOne);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }

            if (contactNumberTwo != null && !contactNumberTwo.equals("")) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumberTwo);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }


            if (headImage != null) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                headImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArrayOutputStream.toByteArray());
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }

            // 获取分配的 contactId
            Cursor cursorRawContact = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{ContactsContract.RawContacts.CONTACT_ID},
                    ContactsContract.RawContacts._ID + " = ?",
                    new String[]{String.valueOf(rawContactId)},
                    null);
            if (cursorRawContact != null) {
                if (cursorRawContact.moveToFirst()) {
                    long contactId = cursorRawContact.getLong(0);
                    Contact contact = new Contact(contactId, rawContactId,
                            contactName, pinyinName, contactNumberOne, contactNumberTwo, headImage);
                    lockContacts.lock();
                    try {
                        allContacts.add(0, contact);
                        Intent intent = new Intent(ACTION_UPDATE_CONTACT);
                        context.sendBroadcast(intent);
                    } finally {
                        lockContacts.unlock();
                    }
                }
                cursorRawContact.close();

            }

            return rawContactId;
        } catch (Exception e) {
            YHLog.e(tag(), "addContactToDB - exception: " + e.getMessage());
            return -1l;
        }
    }

    // 刷新联系人对应的shortcut
    public void updateShortcut(final Context context, final String name, final long contactId, final long rawContactId, final int shortcutId) {

        // 刷新对应位置的shortcut块
        Shortcut srcShortcut = ShortcutMgr.getInstance().getShortcut(shortcutId);
        if (srcShortcut == null) {
            return;
        }

        srcShortcut.title = name;
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
        if (input != null) {
            srcShortcut.setIcon(Utilities.createCircleBitmap(BitmapFactory.decodeStream(input)));
        } else {
            srcShortcut.setIcon(R.drawable.ic_head_image);
        }
        srcShortcut.type = ShortcutType.CONTACT_INFO;
        srcShortcut.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        srcShortcut.extra.activity = ContactDetailActivity.class.getName();
        srcShortcut.extra.param = contactId + "|" + rawContactId;
        YHLog.w(tag(), "updateShortcut - extra.param: " + srcShortcut.extra.param);
        ShortcutMgr.getInstance().updateShortcut(srcShortcut);

        // 更新shortcut界面
        Bundle bundle = new Bundle();
        bundle.putLong(LauncherConst.INTENT_PARAM_CONTACT_ID, contactId);
        bundle.putLong(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, rawContactId);
        ShortcutMgr.getInstance().updateShortcutofTopContact(bundle);
        ShortcutMgr.getInstance().updateShortcutToSecond(bundle);
    }

    // 联系人的每一条 data 记录
    // 一个联系人有多个号码，则会有多条 ContactPhoneData 记录
    class ContactPhoneData {
        // 对应于 DATA 表中的主键标识
        public long id;

        // 用户手机号码
        public String phoneNumber;

        public ContactPhoneData(long id, String phoneNumber) {
            this.id = id;
            this.phoneNumber = phoneNumber;
        }

        @Override
        public String toString() {
            return "ContactPhoneData{" +
                    "id=" + id +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    '}';
        }
    }

    /**
     * 更新联系人
     *
     * @param context
     * @param name
     * @param numberOne
     * @param numberTwo
     * @param headImage
     * @param rawContactId
     * @return true 更新成功，false 更新失败
     */
    public boolean updateContact(Context context, String name,
                                 String numberOne, String numberTwo,
                                 boolean isDefaultHead, Bitmap headImage,
                                 long rawContactId, long contactId) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        lockContacts.lock();
        try {
            // 未更新前的 联系人信息
            Contact currContact = getContactByRawContactId(rawContactId);

            // 更新姓名
            if (name == null || name.length() == 0) {
                lockContacts.unlock();
                return false;
            } else if (currContact.name != null && !currContact.name.equals(name)) {
                currContact.name = name;
                contentValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
                contentResolver.update(ContactsContract.Data.CONTENT_URI,
                        contentValues,
                        ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{String.valueOf(rawContactId), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE});
            }

            // 更新号码
            List<ContactPhoneData> lstData = new ArrayList<>();
            Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.LABEL}, ContactsContract.Data.RAW_CONTACT_ID + " = ? " +
                            " AND " +
                            ContactsContract.Data.MIMETYPE + "='" +
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                    new String[]{String.valueOf(rawContactId)}, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long contactDataId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID));
                    String number = cursor.getString(cursor.getColumnIndex
                            (ContactsContract.CommonDataKinds.Phone.NUMBER));
                    lstData.add(new ContactPhoneData(contactDataId, number));
                }
                cursor.close();
            }

            // 更新号码 1
            if (currContact.phoneOne != null && !currContact.phoneOne.equals(numberOne)
                    && lstData.size() >= 1) {
                contentValues.clear();
                contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numberOne);
                contentResolver.update(ContactsContract.Data.CONTENT_URI,
                        contentValues,
                        ContactsContract.Data._ID + "=? and " + ContactsContract.Data.MIMETYPE + "=? and "
                                + ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                        new String[]{String.valueOf(lstData.get(0).id),
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, currContact.phoneOne});
                currContact.phoneOne = numberOne;
            } else if (currContact.phoneOne == null || currContact.phoneOne.equals("") || currContact.phoneOne.equals("null")) {
                // 新增号码 1
                contentValues.clear();
                contentValues.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numberOne);
                contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
                currContact.phoneOne = numberOne;
            }

            // 更新号码 2
            if (currContact.phoneTwo != null && lstData.size() >= 2) {
                contentValues.clear();
                contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numberTwo);
                contentResolver.update(ContactsContract.Data.CONTENT_URI,
                        contentValues,
                        ContactsContract.Data._ID + "=? and " + ContactsContract.Data.MIMETYPE + "=? and "
                                + ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                        new String[]{String.valueOf(lstData.get(1).id),
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, currContact.phoneTwo});
                currContact.phoneTwo = numberTwo;
            } else {
                if (currContact.phoneTwo == null ||
                        currContact.phoneTwo.equals("") || currContact.phoneTwo.equals("null")) {
                    contentValues.clear();
                    contentValues.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                    contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                    contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numberTwo);
                    contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                    contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
                    currContact.phoneTwo = numberTwo;
                }
            }

            // 更新头像
            if (headImage != null && isDefaultHead) {
                contentValues.clear();
                contentValues.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                headImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                contentValues.put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArrayOutputStream.toByteArray());
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
                currContact.bitmap = headImage;
            } else if (headImage != null) {
                contentValues.clear();
                contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                headImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                contentValues.put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArrayOutputStream.toByteArray());
                contentResolver.update(ContactsContract.Data.CONTENT_URI,
                        contentValues,
                        ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{String.valueOf(rawContactId),
                                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE});
                currContact.bitmap = headImage;
            }
        } finally {
            // 给通话记录 通过模糊查询 选择出来的List 更新现实的数据
            Intent intent = new Intent(ACTION_UPDATE_CONTACT);
            intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contactId);
            intent.putExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, rawContactId);
            context.sendBroadcast(intent);
            // 更新shortcut界面
            Bundle bundle = new Bundle();
            bundle.putLong(LauncherConst.INTENT_PARAM_CONTACT_ID, contactId);
            bundle.putLong(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, rawContactId);
            ShortcutMgr.getInstance().updateShortcutofTopContact(bundle);
            ShortcutMgr.getInstance().updateShortcutToSecond(bundle);
        }
        lockContacts.unlock();
        return true;
    }

    // 用于同步手机的通讯录
    public void updateContactList(Context context) {
        Log.i("TAG00048", "布尔值为" + isChange);
        if (isChange) {
            synchronized (this) {
                ArrayList<Contact> newContacts = getContactsInApplication(context);
                if (newContacts != null && newContacts.size() > 0 && allContacts != null) {
                    allContacts = newContacts;
                    Log.i("TAG00048", "我被启用了");
                    updateContactForSystem();
                }
            }
        }
    }

    public void updateContactForSystem() {
        Shortcut shortcutFirst = ShortcutMgr.getInstance().
                getShortcut(ShortcutConst.TOP_CONTACT_FIRST_PAGE, ShortcutConst.TOP_CONTACT_FIRST_POS);
        Shortcut shortcutSecond = ShortcutMgr.getInstance().
                getShortcut(ShortcutConst.TOP_CONTACT_SECOND_PAGE, ShortcutConst.TOP_CONTACT_SECOND_POS);
        Shortcut shortcutThird = ShortcutMgr.getInstance().
                getShortcut(ShortcutConst.TOP_CONTACT_THIRD_PAGE, ShortcutConst.TOP_CONTACT_THIRD_POS);
        updateShortcutWithLocation(shortcutFirst);
        updateShortcutWithLocation(shortcutSecond);
        updateShortcutWithLocation(shortcutThird);

        Shortcut shortcutFolderFM = ShortcutMgr.getInstance().getShortcut(ShortcutConst.FAMILY_PAGE, ShortcutConst.FAMILY_POS);
        if (shortcutFolderFM != null) {
            ArrayList<Integer> shortcutFolderFMs = shortcutFolderFM.shortcuts;
            for (int shortcutId : shortcutFolderFMs) {
                Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(shortcutId);
                if (shortcut != null) {
                    if (shortcut.extra.param == null) {
                        continue;
                    }
                    updateShortcutWithLocation(shortcut);
                }
            }
        }

        Shortcut shortcutFolderFD = ShortcutMgr.getInstance().getShortcut(ShortcutConst.FRIENDS_PAGE, ShortcutConst.FRIENDS_POS);
        if (shortcutFolderFD != null) {
            ArrayList<Integer> shortcutFolderFDs = shortcutFolderFD.shortcuts;
            for (int shortcutId : shortcutFolderFDs) {
                Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(shortcutId);
                if (shortcut != null) {
                    if (shortcut.extra.param == null) {
                        continue;
                    }
                    updateShortcutWithLocation(shortcut);
                }
            }
        }

    }

    public void updateShortcutWithLocation(Shortcut shortcut) {
        if (shortcut == null) {
            return;
        }
        String param = shortcut.extra.param;
        if (param == null || param.length() < 1) {
            return;
        }
        ArrayList<String> contactParam = parseContactParam(param);
        Long contactId = Long.valueOf(contactParam.get(0));
        if (contactId <= 0) {
            return;
        }
        Contact contact = getContactByContactId(contactId);
        if (contact == null && shortcut.title != null && !shortcut.title.equals("添加") && !shortcut.title.equals("")) {
            shortcut.extra.param = null;
            shortcut.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
            shortcut.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
            shortcut.setIcon(R.drawable.ic_add);
            shortcut.title = "添加";
            ShortcutMgr.getInstance().updateShortcut(shortcut);
        } else if (contact != null) {
            shortcut.title = contact.name;
            if (contact.bitmap == null) {
                shortcut.setIcon(R.drawable.ic_head_image);
            } else {
                shortcut.setIcon(Utilities.createCircleBitmap(contact.bitmap));
            }
            ShortcutMgr.getInstance().updateShortcut(shortcut);
        }

    }
}