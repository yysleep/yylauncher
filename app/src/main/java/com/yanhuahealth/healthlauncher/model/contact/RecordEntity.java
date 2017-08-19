package com.yanhuahealth.healthlauncher.model.contact;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/2/25.
 */
public class RecordEntity {

    public String name;

    public String number;
    // 1打进来 2打出去 3未接
    public int type;
    public long lDate;
    public long duration;
    // 联系人标识
    public long contactId;

    public String photoId;

    public Bitmap bitmap;

    public String _id;

    public String photoUri;

    // 1代表未看 0代表已看
    public String callNew;

    // 次数
    public int numTime;

    @Override
    public String toString() {
        return "RecordEntity{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", type=" + type +
                ", lDate='" + lDate + '\'' +
                ", duration=" + duration +
                ", contactId=" + contactId +
                ", photoId='" + photoId + '\'' +
                ", bitmap=" + bitmap +
                ", _id='" + _id + '\'' +
                ", photoUri='" + photoUri + '\'' +
                ", callNew='" + callNew + '\'' +
                ", numTime=" + numTime +
                '}';
    }
}
