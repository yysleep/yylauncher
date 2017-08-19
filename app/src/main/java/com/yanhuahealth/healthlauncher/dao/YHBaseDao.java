package com.yanhuahealth.healthlauncher.dao;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yanhuahealth.healthlauncher.common.YHLog;

/**
 * 所有 DAO 类的基类
 */
public abstract class YHBaseDao {

    // 标签 tag
    protected abstract String tag();

    // 提供统一的 gson 编解码
    protected Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    // 主要用于初始化
    public boolean init(SQLiteDatabase db) {
        YHLog.d(tag(), "init");
        return true;
    }

    // 用于数据库版本升级
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        YHLog.d(tag(), "upgrade - db:" + db.getPath() + "|old:" + oldVersion + "|new:" + newVersion);
    }
}
