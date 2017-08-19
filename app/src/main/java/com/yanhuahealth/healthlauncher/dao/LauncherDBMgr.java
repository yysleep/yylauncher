package com.yanhuahealth.healthlauncher.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yanhuahealth.healthlauncher.common.YHLog;

/**
 * 数据访问层管理
 */
public class LauncherDBMgr {

    protected String tag() {
        return LauncherDBMgr.class.getName();
    }

    private static LauncherDBMgr instance = new LauncherDBMgr();
    public static LauncherDBMgr getInstance() {
        return instance;
    }

    public static final String DB_NAME = "launcher.db";
    public static final int DB_VER = 1;

    private SQLiteDatabase dbInstance;
    private SQLiteOpenHelper dbHelper;

    public boolean init(Context ctx) {
        YHLog.d(tag(), "init");
        dbHelper = new SQLiteOpenHelper(ctx, DB_NAME, null, DB_VER) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                ShortcutDao.getInstance().init(db);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                ShortcutDao.getInstance().upgrade(db, oldVersion, newVersion);
            }
        };

        return true;
    }

    public SQLiteDatabase getDBInstance() {

        if (dbInstance == null || !dbInstance.isOpen()) {
            dbInstance = dbHelper.getWritableDatabase();
        }

        return dbInstance;
    }
}
