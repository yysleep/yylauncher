package com.yanhuahealth.healthlauncher.dao;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.reflect.TypeToken;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutExtra;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutStateIcon;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutStyle;
import com.yanhuahealth.healthlauncher.utils.Utilities;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 负责 shortcut 的存储访问
 */
public class ShortcutDao extends YHBaseDao {

    @Override
    protected String tag() {
        return ShortcutDao.class.getName();
    }

    // shortcut
    public static final String TABLE_SHORTCUT = "shortcut";
    public static final String COL_ID = "_id";
    public static final String COL_PARENT_ID = "parent_id";
    public static final String COL_TITLE = "title";
    public static final String COL_TYPE = "type";
    public static final String COL_APK_URL = "apk_url";
    public static final String COL_APP_PKG_NAME = "app_pkg_name";
    public static final String COL_ICON = "icon";
    public static final String COL_ICON_RES_ID = "icon_res_id";
    public static final String COL_ICON_URL = "icon_url";
    public static final String COL_PAGE = "page";
    public static final String COL_POS = "pos";
    public static final String COL_FOLDER = "folder";
    public static final String COL_INTENT_URI = "intent_uri";
    public static final String COL_INTENT_TYPE = "intent_type";
    public static final String COL_EXTRA = "extra";
    public static final String COL_STYLE_BG_COLOR = "style_bg_color";
    public static final String COL_EVENTS = "events";
    public static final String COL_NUM_SIGN = "num_sign";

    public static final String SQL_TABLE_SHORTCUT = "create table "
            + TABLE_SHORTCUT + " ("
            + COL_ID + " integer primary key autoincrement, "
            + COL_PARENT_ID + " int,"
            + COL_TITLE + " varchar(128) not null, "
            + COL_TYPE + " int,"
            + COL_APK_URL + " varchar(1024), "
            + COL_APP_PKG_NAME + " varchar(1024), "
            + COL_NUM_SIGN + " int,"
            + COL_ICON + " blob,"
            + COL_ICON_RES_ID + " int,"
            + COL_ICON_URL + " varchar(256), "
            + COL_PAGE + " int,"
            + COL_POS + " int,"
            + COL_FOLDER + " int,"
            + COL_INTENT_URI + " varchar(1024),"
            + COL_INTENT_TYPE + " int,"
            + COL_EXTRA + " varchar(1024),"
            + COL_EVENTS + " varchar(1024),"
            + COL_STYLE_BG_COLOR + " int);";

    // shortcut state icons
    public static final String TABLE_SHORTCUT_STATE_ICON = "shortcut_state_icon";
    public static final String COL_SHORTCUT_ID = "shortcut_id";
    public static final String COL_STATE = "state";

    public static final String SQL_TABLE_SHORTCUT_STATE_ICON = "create table "
            + TABLE_SHORTCUT_STATE_ICON + " ("
            + COL_ID + " integer primary key autoincrement, "
            + COL_SHORTCUT_ID + " int not null,"
            + COL_STATE + " int not null,"
            + COL_ICON + " blob,"
            + COL_ICON_URL + " varchar(256));";

    private static ShortcutDao instance = new ShortcutDao();

    public static ShortcutDao getInstance() {
        return instance;
    }

    @Override
    public boolean init(SQLiteDatabase db) {
        super.init(db);

        db.execSQL("drop table if exists " + TABLE_SHORTCUT + ";");
        db.execSQL(SQL_TABLE_SHORTCUT);

        db.execSQL("drop table if exists " + TABLE_SHORTCUT_STATE_ICON + ";");
        db.execSQL(SQL_TABLE_SHORTCUT_STATE_ICON);
        return true;
    }

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.upgrade(db, oldVersion, newVersion);
    }

    // 加载 shortcut
    public List<Shortcut> loadAllShortcuts() {
        SQLiteDatabase dbInstance = LauncherDBMgr.getInstance().getDBInstance();
        String sql = "select * from " + TABLE_SHORTCUT;
        Cursor cursor = dbInstance.rawQuery(sql, null);
        YHLog.d(tag(), "loadAllShortcuts - sql:" + sql);
        List<Shortcut> shortcuts = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            Shortcut shortcut = new Shortcut();
            shortcut.title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            shortcut.localId = cursor.getInt(cursor.getColumnIndex(COL_ID));
            shortcut.parentLocalId = cursor.getInt(cursor.getColumnIndex(COL_PARENT_ID));
            shortcut.type = cursor.getInt(cursor.getColumnIndex(COL_TYPE));
            shortcut.page = cursor.getInt(cursor.getColumnIndex(COL_PAGE));
            shortcut.posInPage = cursor.getInt(cursor.getColumnIndex(COL_POS));
            shortcut.iconResId = cursor.getInt(cursor.getColumnIndex(COL_ICON_RES_ID));
            shortcut.iconUrl = cursor.getString(cursor.getColumnIndex(COL_ICON_URL));
            shortcut.apkUrl = cursor.getString(cursor.getColumnIndex(COL_APK_URL));
            shortcut.appPackageName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
            shortcut.intentType = cursor.getInt(cursor.getColumnIndex(COL_INTENT_TYPE));
            shortcut.numSign = cursor.getInt(cursor.getColumnIndex(COL_NUM_SIGN));

            String strEvents = cursor.getString(cursor.getColumnIndex(COL_EVENTS));
            if (strEvents != null && strEvents.length() > 0) {
                String[] events = strEvents.split(",");
                for (String event : events) {
                    if (event != null && event.length() > 0) {
                        shortcut.events.add(Integer.valueOf(event));
                    }
                }
            }

            String jsonExtra = cursor.getString(cursor.getColumnIndex(COL_EXTRA));
            if (jsonExtra != null && jsonExtra.length() > 0) {
                try {
                    shortcut.extra = gson.fromJson(jsonExtra, new TypeToken<ShortcutExtra>() {
                    }.getType());
                } catch (Exception e) {
                    YHLog.w(tag(), "loadAllShortcuts exception: " + e.getMessage());
                    continue;
                }
            }

            int folder = cursor.getInt(cursor.getColumnIndex(COL_FOLDER));
            shortcut.isFolder = (folder > 0);

            if (shortcut.iconResId <= 0) {
                byte[] iconData = cursor.getBlob(cursor.getColumnIndex(COL_ICON));
                try {
                    shortcut.icon = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
                } catch (Exception e) {
                    YHLog.w(tag(), "loadAllShortcuts exception - " + e.getMessage());
                }
            }

            String intentUri = cursor.getString(cursor.getColumnIndex(COL_INTENT_URI));
            if (intentUri != null && intentUri.length() > 0) {
                try {
                    shortcut.intent = Intent.parseUri(intentUri, 0);
                } catch (URISyntaxException e) {
                    YHLog.w(tag(), "loadAllShortcuts exception - " + e.getMessage());
                }
            }

            shortcut.style = new ShortcutStyle();
            shortcut.style.backgroundColor = cursor.getInt(cursor.getColumnIndex(COL_STYLE_BG_COLOR));

            // 获取状态图标列表
            List<ShortcutStateIcon> lstStateIcon = getStateIconOfShortcut(shortcut.localId);
            if (lstStateIcon != null && lstStateIcon.size() > 0) {
                for (ShortcutStateIcon stateIcon : lstStateIcon) {
                    if (stateIcon != null && stateIcon.icon != null) {
                        shortcut.stateIcons.put(stateIcon.state, stateIcon.icon);
                    }
                }
            }

            shortcuts.add(shortcut);
        }

        if (cursor != null) {
            cursor.close();
        }

        // 处理二级页面
        for (Shortcut shortcut : shortcuts) {
            if (shortcut != null && shortcut.isFolder && shortcut.localId > 0) {
                for (Shortcut shortcutChild : shortcuts) {
                    if (shortcutChild != null && !shortcutChild.isFolder
                            && shortcutChild.parentLocalId == shortcut.localId) {
                        shortcut.shortcuts.add(shortcutChild.localId);
                    }
                }
            }
        }

        return shortcuts;
    }

    // 新增 shortcut
    public void addShortcut(Shortcut shortcut) {

        if (shortcut == null || shortcut.title == null) {
            YHLog.w(tag(), "addShortcut - invalid param");
            return;
        }

        YHLog.d(tag(), "addShortcut - " + shortcut);

        SQLiteDatabase db = LauncherDBMgr.getInstance().getDBInstance();
        ContentValues newValues = new ContentValues();
        newValues.put(COL_PARENT_ID, shortcut.parentLocalId);
        newValues.put(COL_TITLE, shortcut.title);
        newValues.put(COL_TYPE, shortcut.type);
        newValues.put(COL_APK_URL, shortcut.apkUrl);
        newValues.put(COL_APP_PKG_NAME, shortcut.appPackageName);
        newValues.put(COL_FOLDER, shortcut.isFolder ? 1 : 0);
        newValues.put(COL_PAGE, shortcut.page);
        newValues.put(COL_POS, shortcut.posInPage);
        newValues.put(COL_INTENT_TYPE, shortcut.intentType);
        newValues.put(COL_NUM_SIGN, shortcut.numSign);

        StringBuilder sbufEvents = new StringBuilder();
        for (int event : shortcut.events) {
            sbufEvents.append(event).append(",");
        }
        newValues.put(COL_EVENTS, sbufEvents.toString());

        if (shortcut.extra != null) {
            String strExtra;
            try {
                strExtra = gson.toJson(shortcut.extra);
                newValues.put(COL_EXTRA, strExtra);
            } catch (Exception e) {
                YHLog.w(tag(), "addShortcut exception: " + e.getMessage());
            }
        }

        if (shortcut.intent != null) {
            newValues.put(COL_INTENT_URI, shortcut.intent.toUri(0));
        }

        if (shortcut.style != null) {
            newValues.put(COL_STYLE_BG_COLOR, shortcut.style.backgroundColor);
        }

        if (shortcut.icon != null) {
            byte[] data = Utilities.flattenBitmap(shortcut.icon);
            newValues.put(COL_ICON, data);
        }

        newValues.put(COL_ICON_URL, shortcut.iconUrl);
        newValues.put(COL_ICON_RES_ID, shortcut.iconResId);
        shortcut.localId = (int) db.insert(TABLE_SHORTCUT, null, newValues);

        // 添加 状态图标
        if (shortcut.stateIcons != null && shortcut.stateIcons.size() > 0) {
            for (Map.Entry<Integer, Bitmap> entryStateIcon : shortcut.stateIcons.entrySet()) {
                int state = entryStateIcon.getKey();
                Bitmap stateIcon = entryStateIcon.getValue();
                addShortcutStateIcon(shortcut.localId, state, stateIcon);
            }
        }
    }

    /**
     * 新增 shortcut 指定 state 的 icon
     *
     * @param shortcutId 对应的 shortuct
     * @param state      状态
     * @param icon       图标
     * @return 主键 ID，小于等于 0 表示添加失败
     */
    public int addShortcutStateIcon(int shortcutId, int state, Bitmap icon) {

        if (shortcutId <= 0 || icon == null) {
            return -1;
        }

        YHLog.d(tag(), "addShortcutStateIcon - " + shortcutId + "|" + state);

        SQLiteDatabase db = LauncherDBMgr.getInstance().getDBInstance();
        ContentValues newValues = new ContentValues();
        newValues.put(COL_SHORTCUT_ID, shortcutId);
        newValues.put(COL_STATE, state);
        byte[] data = Utilities.flattenBitmap(icon);
        newValues.put(COL_ICON, data);

        return (int) db.insert(TABLE_SHORTCUT_STATE_ICON, null, newValues);
    }

    /**
     * 更新 shortcut
     */
    public void updateShortcut(Shortcut shortcut) {

        if (shortcut == null || shortcut.localId <= 0) {
            return;
        }

        YHLog.d(tag(), "updateShortcut - " + shortcut);

        ContentValues newValues = new ContentValues();
        newValues.put(COL_PARENT_ID, shortcut.parentLocalId);
        newValues.put(COL_TITLE, shortcut.title);
        newValues.put(COL_TYPE, shortcut.type);
        newValues.put(COL_PAGE, shortcut.page);
        newValues.put(COL_POS, shortcut.posInPage);
        newValues.put(COL_FOLDER, shortcut.isFolder ? 1 : 0);
        newValues.put(COL_ICON_URL, shortcut.iconUrl);
        newValues.put(COL_ICON_RES_ID, shortcut.iconResId);
        newValues.put(COL_APK_URL, shortcut.apkUrl);
        newValues.put(COL_APP_PKG_NAME, shortcut.appPackageName);
        newValues.put(COL_INTENT_TYPE, shortcut.intentType);
        newValues.put(COL_NUM_SIGN, shortcut.numSign);

        StringBuilder sbufEvents = new StringBuilder();
        for (int event : shortcut.events) {
            sbufEvents.append(event).append(",");
        }
        newValues.put(COL_EVENTS, sbufEvents.toString());

        if (shortcut.extra != null) {
            String strExtra;
            try {
                strExtra = gson.toJson(shortcut.extra);
                newValues.put(COL_EXTRA, strExtra);
            } catch (Exception e) {
                YHLog.w(tag(), "updateShortcut exception: " + e.getMessage());
            }
        }

        if (shortcut.style != null) {
            newValues.put(COL_STYLE_BG_COLOR, shortcut.style.backgroundColor);
        }

        if (shortcut.icon != null) {
            byte[] data = Utilities.flattenBitmap(shortcut.icon);
            newValues.put(COL_ICON, data);
        }

        if (shortcut.intent != null) {
            newValues.put(COL_INTENT_URI, shortcut.intent.toUri(0));
        } else {
            newValues.put(COL_INTENT_URI, "");
        }

        SQLiteDatabase db = LauncherDBMgr.getInstance().getDBInstance();

        String sqlWhere = COL_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(shortcut.localId)};
        db.update(TABLE_SHORTCUT, newValues, sqlWhere, whereArgs);
    }

    /**
     * 获取指定 shortcut 下的所有 state icon
     */
    public List<ShortcutStateIcon> getStateIconOfShortcut(int shortcutLocalId) {

        if (shortcutLocalId <= 0) {
            return null;
        }

        SQLiteDatabase dbInstance = LauncherDBMgr.getInstance().getDBInstance();
        String sql = "select * from " + TABLE_SHORTCUT_STATE_ICON
                + " where " + COL_SHORTCUT_ID + " = " + shortcutLocalId;
        Cursor cursor = dbInstance.rawQuery(sql, null);
        YHLog.d(tag(), "getStateIconOfShortcut - sql:" + sql);
        List<ShortcutStateIcon> lstStateIcon = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            ShortcutStateIcon stateIcon = new ShortcutStateIcon();
            stateIcon.state = cursor.getInt(cursor.getColumnIndex(COL_STATE));
            byte[] iconData = cursor.getBlob(cursor.getColumnIndex(COL_ICON));
            try {
                stateIcon.icon = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
            } catch (Exception e) {
                YHLog.w(tag(), "getStateIconOfShortcut exception - " + e.getMessage());
            }

            lstStateIcon.add(stateIcon);
        }

        if (cursor != null) {
            cursor.close();
        }

        return lstStateIcon;
    }

    /**
     * 获取指定 parent 的指定位置的 shortcut
     */
    public Shortcut getShortcutOfChild(int parentLocalId, int page, int pos) {

        String sql = "select * from " + TABLE_SHORTCUT
                + " where " + COL_PAGE + "=" + page
                + " and " + COL_POS + " = " + pos
                + " and " + COL_PARENT_ID + " = " + parentLocalId;

        SQLiteDatabase db = LauncherDBMgr.getInstance().getDBInstance();
        Cursor cursor = db.rawQuery(sql, null);
        YHLog.d(tag(), "getShortcutOfChild - sql:" + sql);
        Shortcut shortcut = null;
        while (cursor != null && cursor.moveToNext()) {
            shortcut = new Shortcut();
            shortcut.title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            shortcut.localId = cursor.getInt(cursor.getColumnIndex(COL_ID));
            shortcut.parentLocalId = cursor.getInt(cursor.getColumnIndex(COL_PARENT_ID));
            shortcut.type = cursor.getInt(cursor.getColumnIndex(COL_TYPE));
            shortcut.page = cursor.getInt(cursor.getColumnIndex(COL_PAGE));
            shortcut.posInPage = cursor.getInt(cursor.getColumnIndex(COL_POS));
            shortcut.iconResId = cursor.getInt(cursor.getColumnIndex(COL_ICON_RES_ID));
            shortcut.iconUrl = cursor.getString(cursor.getColumnIndex(COL_ICON_URL));
            shortcut.apkUrl = cursor.getString(cursor.getColumnIndex(COL_APK_URL));
            shortcut.appPackageName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
            shortcut.intentType = cursor.getInt(cursor.getColumnIndex(COL_INTENT_TYPE));
            shortcut.numSign = cursor.getInt(cursor.getColumnIndex(COL_NUM_SIGN));

            String strEvents = cursor.getString(cursor.getColumnIndex(COL_EVENTS));
            if (strEvents != null && strEvents.length() > 0) {
                String[] events = strEvents.split(",");
                for (String event : events) {
                    if (event != null && event.length() > 0) {
                        shortcut.events.add(Integer.valueOf(event));
                    }
                }
            }

            String jsonExtra = cursor.getString(cursor.getColumnIndex(COL_EXTRA));
            if (jsonExtra != null && jsonExtra.length() > 0) {
                try {
                    shortcut.extra = gson.fromJson(jsonExtra, new TypeToken<ShortcutExtra>() {
                    }.getType());
                } catch (Exception e) {
                    YHLog.w(tag(), "loadAllShortcuts exception: " + e.getMessage());
                    continue;
                }
            }

            int folder = cursor.getInt(cursor.getColumnIndex(COL_FOLDER));
            shortcut.isFolder = (folder > 0);

            if (shortcut.iconResId <= 0) {
                byte[] iconData = cursor.getBlob(cursor.getColumnIndex(COL_ICON));
                try {
                    shortcut.icon = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
                } catch (Exception e) {
                    YHLog.w(tag(), "loadAllShortcuts exception - " + e.getMessage());
                }
            }

            String intentUri = cursor.getString(cursor.getColumnIndex(COL_INTENT_URI));
            if (intentUri != null && intentUri.length() > 0) {
                try {
                    shortcut.intent = Intent.parseUri(intentUri, 0);
                } catch (URISyntaxException e) {
                    YHLog.w(tag(), "loadAllShortcuts exception - " + e.getMessage());
                }
            }

            shortcut.style = new ShortcutStyle();
            shortcut.style.backgroundColor = cursor.getInt(cursor.getColumnIndex(COL_STYLE_BG_COLOR));
        }

        if (cursor != null) {
            cursor.close();
        }

        return shortcut;
    }

    /**
     * 获取指定位置的 shortcut
     */
    public Shortcut getShortcutWithPos(int page, int pos) {

        String sql = "select * from " + TABLE_SHORTCUT
                + " where " + COL_PAGE + "=" + page
                + " and " + COL_POS + " = " + pos;

        SQLiteDatabase db = LauncherDBMgr.getInstance().getDBInstance();
        Cursor cursor = db.rawQuery(sql, null);
        YHLog.d(tag(), "getShortcutWithPos - sql:" + sql);
        Shortcut shortcut = null;
        while (cursor != null && cursor.moveToNext()) {
            shortcut = new Shortcut();
            shortcut.title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            shortcut.localId = cursor.getInt(cursor.getColumnIndex(COL_ID));
            shortcut.parentLocalId = cursor.getInt(cursor.getColumnIndex(COL_PARENT_ID));
            shortcut.type = cursor.getInt(cursor.getColumnIndex(COL_TYPE));
            shortcut.page = cursor.getInt(cursor.getColumnIndex(COL_PAGE));
            shortcut.posInPage = cursor.getInt(cursor.getColumnIndex(COL_POS));
            shortcut.iconResId = cursor.getInt(cursor.getColumnIndex(COL_ICON_RES_ID));
            shortcut.iconUrl = cursor.getString(cursor.getColumnIndex(COL_ICON_URL));
            shortcut.apkUrl = cursor.getString(cursor.getColumnIndex(COL_APK_URL));
            shortcut.appPackageName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
            shortcut.intentType = cursor.getInt(cursor.getColumnIndex(COL_INTENT_TYPE));
            shortcut.numSign = cursor.getInt(cursor.getColumnIndex(COL_NUM_SIGN));

            String jsonExtra = cursor.getString(cursor.getColumnIndex(COL_EXTRA));
            if (jsonExtra != null && jsonExtra.length() > 0) {
                try {
                    shortcut.extra = gson.fromJson(jsonExtra, new TypeToken<ShortcutExtra>() {
                    }.getType());
                } catch (Exception e) {
                    YHLog.w(tag(), "getShortcutWithPos exception: " + e.getMessage());
                    continue;
                }
            }

            int folder = cursor.getInt(cursor.getColumnIndex(COL_FOLDER));
            shortcut.isFolder = (folder > 0);

            if (shortcut.iconResId <= 0) {
                byte[] iconData = cursor.getBlob(cursor.getColumnIndex(COL_ICON));
                try {
                    shortcut.icon = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
                } catch (Exception e) {
                    YHLog.w(tag(), "getShortcutWithPos exception - " + e.getMessage());
                }
            }

            String intentUri = cursor.getString(cursor.getColumnIndex(COL_INTENT_URI));
            if (intentUri != null && intentUri.length() > 0) {
                try {
                    shortcut.intent = Intent.parseUri(intentUri, 0);
                } catch (URISyntaxException e) {
                    YHLog.w(tag(), "getShortcutWithPos exception - " + e.getMessage());
                }
            }

            shortcut.style = new ShortcutStyle();
            shortcut.style.backgroundColor = cursor.getInt(cursor.getColumnIndex(COL_STYLE_BG_COLOR));
        }

        if (cursor != null) {
            cursor.close();
        }

        return shortcut;
    }

    /**
     * 删除指定位置的 shortcut
     */
    public boolean deleteShortcutWithPos(int page, int posInPage) {

        YHLog.d(tag(), "deleteShortcutWithPos - page:" + page + "|pos:" + posInPage);
        SQLiteDatabase db = LauncherDBMgr.getInstance().getDBInstance();
        String where = COL_PAGE + " = ? and " + COL_POS + " = ?";
        String[] whereArgs = new String[]{String.valueOf(page), String.valueOf(posInPage)};
        db.delete(TABLE_SHORTCUT, where, whereArgs);
        return true;
    }
}
