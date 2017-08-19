package com.yanhuahealth.healthlauncher.model.contact;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/4/5.
 */
public class ContactForIntent implements Serializable {

    public ContactForIntent(String name, long contactId, long rawContanctId, String phoneNumber) {
        this.name = name;
        this.contactId = contactId;
        this.rawContactId = rawContanctId;
        this.phoneNumber = phoneNumber;
    }

    // 用来传递参数的List 主要用于拨号界面的模糊查询
    public String name;

    public long contactId;

    public long rawContactId;

    public String phoneNumber;


    @Override
    public String toString() {
        return "ContactForIntent{" +
                "name='" + name + '\'' +
                ", contactId='" + contactId + '\'' +
                '}';
    }
}
