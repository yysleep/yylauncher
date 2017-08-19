package com.yanhuahealth.healthlauncher.model.contact;

import android.graphics.Bitmap;
import android.os.Parcel;

/**
 * 联系人信息
 */
public class Contact {

    // 姓名
    public String name;

    // 手机号码
    public String phoneOne;

    // 手机号码
    public String phoneTwo;

    // 联系人标识 查询联系人信息时使用
    public long contactId;

    // 联系人头像
    public Bitmap bitmap;

    // 删除联系人时 使用的标识
    public long rawContactId;

    public String pinyinName;

    public Contact(long contactId, String name) {
        this.contactId = contactId;
        this.name = name;

    }

    public Contact(long contactId, String name, String phoneOne, String phoneTwo, Bitmap bitmap) {
        this.contactId = contactId;
        this.name = name;
        this.phoneOne = phoneOne;
        this.phoneTwo = phoneTwo;
        this.bitmap = bitmap;
    }

    protected Contact(Parcel in) {
        name = in.readString();
        contactId = in.readLong();
        rawContactId = in.readLong();
        phoneOne = in.readString();
        phoneTwo = in.readString();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }


    public Contact() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        Contact contact = (Contact) o;
        return name.equals(contact.name);
    }

    @Override
    public int hashCode() {
        String in = name;
        return in.hashCode();
    }


    public Contact(long contactId, long rawContactId, String name) {
        this.contactId = contactId;
        this.rawContactId = rawContactId;
        this.name = name;
    }

    public Contact(long contactId, long rawContactId, String name, String pinyinName, String phoneOne, String phoneTwo, Bitmap bitmap) {
        this.contactId = contactId;
        this.rawContactId = rawContactId;
        this.name = name;
        this.pinyinName = pinyinName;
        this.phoneOne = phoneOne;
        this.phoneTwo = phoneTwo;
        this.bitmap = bitmap;
    }

    public Contact(long contactId, long rawContactId, String name, String number) {
        this.contactId = contactId;
        this.rawContactId = rawContactId;
        this.name = name;
        this.phoneOne = number;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", phoneOne='" + phoneOne + '\'' +
                ", phoneTwo='" + phoneTwo + '\'' +
                ", contactId='" + contactId + '\'' +
                ", bitmap=" + bitmap +
                ", rawContactId='" + rawContactId + '\'' +
                '}';
    }
}
