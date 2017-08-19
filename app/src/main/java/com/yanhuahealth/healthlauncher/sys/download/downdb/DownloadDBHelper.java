package com.yanhuahealth.healthlauncher.sys.download.downdb;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/3/18.
 */
public class DownloadDBHelper extends SQLiteOpenHelper {

    private static DownloadDBHelper instance = null;

    public static DownloadDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DownloadDBHelper(context);
        }
        return instance;
    }

    private static final String DB_NAME = "yanhuadownload.db";
    private static final int VERSION = 1;
    private static final String SQL_CREATE = "create table  thread_info(_id integer primary key autoincrement," +
            "thread_id integer,url text,start integer,end integer,finished integer)";
    private static final String SQL_DROP = "drop table is exists thread_info)";

    private DownloadDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
    }


}
