

package com.yanhuahealth.healthlauncher.sys.appmgr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.dao.ShortcutDao;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutExtra;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutRemoveResult;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.ShortcutStyle;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.ImageManager;
import com.yanhuahealth.healthlauncher.ui.contact.AddContactActivity;
import com.yanhuahealth.healthlauncher.ui.contact.ContactListActivity;
import com.yanhuahealth.healthlauncher.ui.controlcenter.ControlCenterActivity;
import com.yanhuahealth.healthlauncher.ui.ebook.EbookListActivity;
import com.yanhuahealth.healthlauncher.ui.note.SmsListActivity;
import com.yanhuahealth.healthlauncher.ui.player.MusicPlayerActivity;
import com.yanhuahealth.healthlauncher.ui.setting.SettingActivity;
import com.yanhuahealth.healthlauncher.ui.setting.SosSettingActivity;
import com.yanhuahealth.healthlauncher.ui.toolutil.AllAppActivity;
import com.yanhuahealth.healthlauncher.model.app.AppInfo;
import com.yanhuahealth.healthlauncher.ui.toolutil.BaseWebActivity;
import com.yanhuahealth.healthlauncher.ui.toolutil.SecondActivity;
import com.yanhuahealth.healthlauncher.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 所有桌面图标的管理器，负责：
 * <p/>
 * - 缓存所有桌面图标的名称，图标，以及 intent 信息
 */
public class ShortcutMgr {

    protected String tag() {
        return ShortcutMgr.class.getName();
    }

    private static ShortcutMgr instance = new ShortcutMgr();

    public static ShortcutMgr getInstance() {
        return instance;
    }

    // 保存所有添加到桌面的 shortcut 记录
    private List<List<Shortcut>> allShortcutsOnDesktop = new ArrayList<>();

    // 保存所有二级页面的 shortcut 列表
    private Map<Integer, List<Shortcut>> allShortcutsOfChild = new HashMap<>();

    // 维护所有的 shortcut
    private Map<Integer, Shortcut> allShortcuts = new HashMap<>();

    private Context context;

    /**
     * 初始化
     */
    public void init(Context context) {
        YHLog.d(tag(), "init");
        this.context = context;

        loadShortcuts();

        // 必须在 shortcuts 加载完成后再加载应用列表
        AppMgr.getInstance().init(context);
    }

    /**
     * 获取当前 shortcut 页面数
     */
    public int getShortcutPageNum() {
        return allShortcutsOnDesktop.size();
    }

    /**
     * 获取指定位置的 shortcut
     */
    public Shortcut getShortcut(int page, int posInPage) {

        int currentPages = allShortcutsOnDesktop.size();
        if (currentPages <= page) {
            YHLog.w(tag(), "getShortcut - page too long - " + page);
            return null;
        } else if (posInPage >= ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE) {
            YHLog.w(tag(), "getShortcut - box num too large - " + posInPage);
            return null;
        }

        return allShortcutsOnDesktop.get(page).get(posInPage);
    }

    /**
     * 获取指定位置的 shortcut
     */
    public Shortcut getShortcut(int page, int posInPage, int parentLocalId) {

        if (parentLocalId <= 0) {
            return getShortcut(page, posInPage);
        } else if (page != ShortcutConst.DEFAULT_CHILD_PAGE) {
            YHLog.w(tag(), "getShortcut - page not equal " + ShortcutConst.DEFAULT_CHILD_PAGE);
            return null;
        } else if (!allShortcutsOfChild.containsKey(parentLocalId)) {
            YHLog.w(tag(), "getShortcut - not contains child page of " + parentLocalId);
            return null;
        }

        List<Shortcut> shortcuts = allShortcutsOfChild.get(parentLocalId);
        if (shortcuts != null && shortcuts.size() > posInPage) {
            return shortcuts.get(posInPage);
        }

        return null;
    }

    /**
     * 获取指定 localId 对应的 shortcut
     */
    public Shortcut getShortcut(int localId) {
        if (localId <= 0) {
            return null;
        }

        if (allShortcuts.containsKey(localId)) {
            return allShortcuts.get(localId);
        }

        return null;
    }

    /**
     * 新增微信 shortcut
     */
    public boolean addWeixinShortcut(String title, Bitmap icon, Intent intent) {

        Shortcut newShortcut = new Shortcut();
        newShortcut.title = title;
        newShortcut.intent = intent;
        newShortcut.icon = icon;
        newShortcut.type = ShortcutType.WEIXIN_SHORTCUT;

        boolean isAssistant = false;
        if (title.equals(context.getString(R.string.health_assistant))) {
            // 小秘书
            newShortcut.page = ShortcutConst.HEALTH_ASSISTANT_PAGE;
            newShortcut.posInPage = ShortcutConst.HEALTH_ASSISTANT_POS;
            newShortcut.type = ShortcutType.HEALTH_ASSISTANT;
            newShortcut.icon = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_second_health);
            isAssistant = true;
        } else if (title.equals(context.getString(R.string.xf_assistant))) {
            // 幸福小秘书
            newShortcut.page = ShortcutConst.HEALTH_MGR_PAGE;
            newShortcut.posInPage = ShortcutConst.HEALTH_MGR_POS;
            newShortcut.type = ShortcutType.HEALTH_MGR;
            newShortcut.icon = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_second_happiness);
            isAssistant = true;
        }

        if (isAssistant) {
            Shortcut oldShortcut = getShortcut(newShortcut.page, newShortcut.posInPage);
            if (oldShortcut != null) {
                newShortcut.style = oldShortcut.style;
                newShortcut.localId = oldShortcut.localId;
                updateShortcut(newShortcut);
            } else {
                addShortcut(newShortcut);
            }
        }

        return true;
    }

    /**
     * 新增一个外部应用安装包
     */
    public Shortcut addExAppShortcut(PackageInfo packageInfo) {

        if (packageInfo == null) {
            return null;
        }

        if (getShortcutOfExApp(packageInfo) != null) {
            YHLog.w(tag(), "addExAppShortcut - already exists");
            return null;
        }

        ApplicationInfo appInfo = packageInfo.applicationInfo;
        PackageManager pm = context.getPackageManager();

        Shortcut newShortcut = new Shortcut();
        newShortcut.title = appInfo.loadLabel(pm).toString();
        newShortcut.intent = pm.getLaunchIntentForPackage(appInfo.packageName);
        newShortcut.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        if (ImageManager.getInstance().existsAppIconWithPkgName(appInfo.packageName)) {
            newShortcut.iconUrl = "file://" + ImageManager.getInstance().getAppIconPathWithPkgName(appInfo.packageName);
        } else {
            newShortcut.iconUrl = ImageManager.getInstance().saveAppIcon(context, appInfo);
        }

        newShortcut.type = ShortcutType.EXTERNAL_APP;
        newShortcut.appPackageName = appInfo.packageName;
        newShortcut.isFolder = false;

        if (addShortcut(newShortcut) > 0) {
            // 添加到应用缓存中
            AppInfo tmpInfo = new AppInfo();
            tmpInfo.appName = newShortcut.title;
            tmpInfo.packageName = packageInfo.packageName;
            tmpInfo.versionName = packageInfo.versionName;
            tmpInfo.versionCode = packageInfo.versionCode;
            tmpInfo.appIcon = appInfo.loadIcon(pm);
            tmpInfo.packageInfo = packageInfo;
            AppMgr.getInstance().addApp(tmpInfo);

            return newShortcut;
        }

        return null;
    }

    /**
     * 获取给定的安装包是否已添加至 shortcut 桌面
     * 如果已添加，则返回对应的 Shortcut 实例
     */
    public Shortcut getShortcutOfExApp(PackageInfo packageInfo) {

        if (packageInfo == null) {
            return null;
        }

        return getShortcutOfExApp(packageInfo.applicationInfo.packageName);
    }

    /**
     * 获取指定安装包名的 shortcut
     */
    public Shortcut getShortcutOfExApp(String packageName) {

        if (packageName == null || packageName.equals("")) {
            return null;
        }

        for (List<Shortcut> shortcuts : allShortcutsOnDesktop) {
            for (Shortcut shortcut : shortcuts) {
                if (shortcut != null && shortcut.intent != null
                        && shortcut.intent.getPackage() != null
                        && shortcut.intent.getPackage().equals(packageName)) {
                    return shortcut;
                }
            }
        }

        return null;
    }

    /**
     * 移除一个外部应用安装包
     */
    public boolean removeExAppShortcut(PackageInfo packageInfo) {

        if (packageInfo == null) {
            return false;
        }

        Shortcut shortcut = getShortcutOfExApp(packageInfo);
        if (shortcut == null) {
            YHLog.w(tag(), "removeExAppShortcut - not exists");
            return false;
        }

        // 同时从 应用列表 中移除该应用
        AppMgr.getInstance().removeApp(packageInfo.packageName);

        removeShortcut(shortcut, false);
        return true;
    }

    /**
     * 移除一个外部应用安装包 只用在桌面快速删除
     */
    public boolean removeShortcutApp(String packageName, Shortcut shortcut) {

        if (packageName == null) {
            return false;
        }

        if (shortcut == null) {
            YHLog.w(tag(), "removeExAppShortcut - not exists");
            return false;
        }

        // 移除该应用的快捷方式
        AppMgr.getInstance().updateApp(context, shortcut.appPackageName);

        removeShortcut(shortcut, true);
        return true;
    }

    /**
     * 移除指定安装包名的外部应用
     */
    public ShortcutRemoveResult removeExAppShortcut(String packageName) {

        if (packageName == null || packageName.equals("")) {
            return null;
        }

        Shortcut shortcut = getShortcutOfExApp(packageName);
        if (shortcut == null) {
            YHLog.w(tag(), "removeExAppShortcut - not exists");
            return null;
        }

        if (shortcut.type < ShortcutType.EXTERNAL_APP) {
            // 设置对应的 intent 为 null
            shortcut.intent = null;

            ShortcutRemoveResult removeResult = new ShortcutRemoveResult();
            removeResult.shortcut = shortcut;
            removeResult.isRemovedPage = false;
            removeResult.result = true;
            return removeResult;
        }

        // 同时从 应用列表 中移除该应用
        AppMgr.getInstance().removeApp(packageName);

        return removeShortcut(shortcut, true);
    }

    /**
     * 新增一个 shortcut
     * <p/>
     * - 新增的 shortcut 只能添加到比当前页数多一个页
     * - 如果没有指定 shortcut 应该布局的 box 位置,
     * 则会同时设置改快捷方式摆放的 box 的位置，
     * 在 shortcut 实例中设置相应的 page 和 posOfPage
     *
     * @return 返回分配的 localId，<= 0 表示添加失败
     */
    public int addShortcut(Shortcut shortcut) {

        if (shortcut == null) {
            YHLog.w(tag(), "addShortcut - invalid param");
            return -1;
        }

        if (shortcut.parentLocalId > 0 && shortcut.page == ShortcutConst.DEFAULT_CHILD_PAGE) {
            return addShortcutToChild(shortcut);
        }

        if (shortcut.page == ShortcutConst.PAGE_UNKNOWN) {
            // 不知道摆放的页号
            // 先从当前最大页号中，找一个剩余的未被设置的 box
            int pageSize = allShortcutsOnDesktop.size();
            if (pageSize == ShortcutConst.DEFAULT_PAGE_NUM) {
                // 新增第一个新的页面
                List<Shortcut> lstShortcut = new CopyOnWriteArrayList<>();
                allShortcutsOnDesktop.add(lstShortcut);
                for (int idx = 0; idx < ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE; ++idx) {
                    lstShortcut.add(null);
                }

                pageSize += 1;
            }

            // 从第 DEFAULT_PAGE_NUM+1 页往后找到一个空的 box
            boolean isFindPos = false;
            for (int pageNum = ShortcutConst.DEFAULT_PAGE_NUM; pageNum < pageSize; ++pageNum) {
                List<Shortcut> lstShortcutsOfPage = allShortcutsOnDesktop.get(pageNum);
                int idxPosOfPage = 0;
                for (Shortcut shortcutInPage : lstShortcutsOfPage) {
                    if (shortcutInPage == null || shortcutInPage.page == ShortcutConst.PAGE_UNKNOWN) {
                        shortcut.page = pageNum;
                        shortcut.posInPage = idxPosOfPage;
                        lstShortcutsOfPage.set(idxPosOfPage, shortcut);
                        isFindPos = true;
                        break;
                    }

                    idxPosOfPage++;
                }

                if (isFindPos) {
                    YHLog.i(tag(), "addShortcut - already find pos: " + shortcut);
                    break;
                }
            }

            if (isFindPos) {
                // 找到 box 位置，不需要再遍历
                YHLog.i(tag(), "addShortcut - find pos: " + shortcut);
                int localId = addShortcutToDB(shortcut);
                if (localId > 0) {
                    allShortcuts.put(shortcut.localId, shortcut);
                }
                return localId;
            } else {
                // 没有找到，说明之前的页面都已经被占满
                // 此时需要新增一页
                // 并将该 shortcut 放到第一个位置
                List<Shortcut> lstShortcut = new CopyOnWriteArrayList<>();
                for (int idx = 0; idx < ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE; ++idx) {
                    lstShortcut.add(null);
                }

                shortcut.page = allShortcutsOnDesktop.size();
                shortcut.posInPage = 0;
                lstShortcut.set(0, shortcut);
                allShortcutsOnDesktop.add(lstShortcut);
                int localId = addShortcutToDB(shortcut);
                if (localId > 0) {
                    allShortcuts.put(localId, shortcut);
                }

                return localId;
            }
        }

        int currentPages = allShortcutsOnDesktop.size();
        if (currentPages < shortcut.page) {
            YHLog.w(tag(), "addShortcut - page too long - " + shortcut);
            return -1;
        } else if (shortcut.posInPage >= ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE) {
            YHLog.w(tag(), "addShortcut - box num too large - " + shortcut);
            return -1;
        }

        if (currentPages == shortcut.page) {
            // 如果为新页面
            // 则同时初始化对应页面下的 BOX
            List<Shortcut> lstShortcut = new CopyOnWriteArrayList<>();
            allShortcutsOnDesktop.add(lstShortcut);
            for (int idx = 0; idx < ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE; ++idx) {
                lstShortcut.add(null);
            }
        }

        List<Shortcut> lstShortcut = allShortcutsOnDesktop.get(shortcut.page);
        lstShortcut.set(shortcut.posInPage, shortcut);
        int localId = addShortcutToDB(shortcut);
        if (localId > 0) {
            allShortcuts.put(localId, shortcut);
        }

        return localId;
    }

    /**
     * 添加 shortcut 至子页面
     */
    private int addShortcutToChild(Shortcut shortcut) {

        if (shortcut == null || shortcut.parentLocalId <= 0) {
            YHLog.e(tag(), "addShortcutToChild - invalid param");
            return -1;
        }

        int parentLocalId = shortcut.parentLocalId;
        List<Shortcut> lstShortcuts = null;
        if (allShortcutsOfChild.containsKey(parentLocalId)) {
            lstShortcuts = allShortcutsOfChild.get(parentLocalId);
        }

        if (lstShortcuts == null) {
            lstShortcuts = new CopyOnWriteArrayList<>();
            allShortcutsOfChild.put(parentLocalId, lstShortcuts);
        }

        // 如果已存在相同的 localId，先移除再添加
        for (Shortcut s : lstShortcuts) {
            if (s != null && s.localId == shortcut.localId) {
                lstShortcuts.remove(s);
                break;
            }
        }

        lstShortcuts.add(shortcut);
        int localId = addShortcutOfChildToDB(shortcut);
        if (localId > 0) {
            allShortcuts.put(localId, shortcut);
        }

        return localId;
    }

    /**
     * 更新 shortcut
     */
    public boolean updateShortcut(Shortcut shortcut) {

        if (shortcut == null || shortcut.posInPage < 0) {
            YHLog.w(tag(), "updateShortcut - invalid param");
            return false;
        }

        shortcut.lastUpdatedTime = System.currentTimeMillis();

        if (shortcut.page == ShortcutConst.DEFAULT_CHILD_PAGE) {
            return updateShortcutOfChild(shortcut);
        }

        if (shortcut.page < 0 || shortcut.posInPage >= ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE) {
            return false;
        }

        if (getShortcut(shortcut.page, shortcut.posInPage) != null) {
            allShortcutsOnDesktop.get(shortcut.page).set(shortcut.posInPage, shortcut);
            allShortcuts.put(shortcut.localId, shortcut);
            ShortcutDao.getInstance().updateShortcut(shortcut);
            return true;
        }

        return false;
    }

    /**
     * 更新子页面中的 shortcut 信息
     */
    public boolean updateShortcutOfChild(Shortcut shortcut) {

        if (shortcut == null || shortcut.parentLocalId <= 0) {
            YHLog.w(tag(), "updateShortcutOfChild - invalid param");
            return false;
        }

        int parentLocalId = shortcut.parentLocalId;
        if (getShortcut(shortcut.page, shortcut.posInPage, parentLocalId) != null) {
            allShortcutsOfChild.get(parentLocalId).set(shortcut.posInPage, shortcut);
            allShortcuts.put(shortcut.localId, shortcut);
            ShortcutDao.getInstance().updateShortcut(shortcut);
            return true;
        }

        return false;
    }

    // 首界面更新shortcut
    public void updateShortcutofTopContact(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        if (bundle.getLong(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1) <= 0) {
            return;
        }
        Long contactId = bundle.getLong(LauncherConst.INTENT_PARAM_CONTACT_ID, -1);
        Long rawContactId = bundle.getLong(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1);
        String param = contactId + "|" + rawContactId;

        Shortcut shortcutAddFirst = ShortcutMgr.getInstance().
                getShortcut(ShortcutConst.TOP_CONTACT_FIRST_PAGE, ShortcutConst.TOP_CONTACT_FIRST_POS);
        updateContactShortcut(shortcutAddFirst, param, contactId);

        Shortcut shortcutAddSecond = ShortcutMgr.getInstance().
                getShortcut(ShortcutConst.TOP_CONTACT_SECOND_PAGE, ShortcutConst.TOP_CONTACT_SECOND_POS);
        updateContactShortcut(shortcutAddSecond, param, contactId);

        Shortcut shortcutAddThird = ShortcutMgr.getInstance().
                getShortcut(ShortcutConst.TOP_CONTACT_THIRD_PAGE, ShortcutConst.TOP_CONTACT_THIRD_POS);
        updateContactShortcut(shortcutAddThird, param, contactId);

        Shortcut shortcutFamily = ShortcutMgr.getInstance().getShortcut
                (ShortcutConst.FAMILY_PAGE, ShortcutConst.FAMILY_POS);
        if (shortcutFamily != null) {
            List<Integer> shortcuts = shortcutFamily.shortcuts;
            ArrayList<Shortcut> shortcutChilds = new ArrayList<>();
            if (shortcuts.size() > 0) {
                for (int shortcutLocalId : shortcuts) {
                    Shortcut shortcutChild = ShortcutMgr.getInstance().getShortcut(shortcutLocalId);
                    if (shortcutChild == null) {
                        continue;
                    }
                    if (!shortcutChild.title.equals("添加") && shortcutChild.extra.param != null) {
                        shortcutChilds.add(shortcutChild);
                    }
                }
            }
        }
    }

    // 二级界面更新shortcut
    public void updateShortcutToSecond(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        if (bundle.getLong(LauncherConst.INTENT_PARAM_CONTACT_ID, -1) <= 0) {
            return;
        }
        if (bundle.getLong(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1) <= 0) {
            return;
        }
        Long contactId = bundle.getLong(LauncherConst.INTENT_PARAM_CONTACT_ID, -1);
        Long rawContactId = bundle.getLong(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1);
        String param = contactId + "|" + rawContactId;
        Shortcut shortcutFamily = getShortcut(ShortcutConst.FAMILY_PAGE, ShortcutConst.FAMILY_POS);
        updateShortcutforS(shortcutFamily.shortcuts, param, contactId);
        Shortcut shortcutFriend = getShortcut(ShortcutConst.FRIENDS_PAGE, ShortcutConst.FRIENDS_POS);
        updateShortcutforS(shortcutFriend.shortcuts, param, contactId);

    }

    // 针对2级界面更新shortcut的其中一个
    private void updateShortcutforS(ArrayList<Integer> shortcuts, String param, long contactId) {
        for (int shortcutId : shortcuts) {
            Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(shortcutId);
            if (shortcut != null && shortcut.extra.param != null &&
                    shortcut.extra.param.equals(param)) {
                Contact contact = ContactMgr.getInstance().getContactByContactId(contactId);
                if (contact != null) {
                    if (contact.bitmap != null) {
                        shortcut.setIcon(contact.bitmap);
                    } else {
                        shortcut.setIcon(R.drawable.ic_head_image);
                    }
                } else {
                    shortcut.extra.param = null;
                    shortcut.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
                    shortcut.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
                    shortcut.setIcon(R.drawable.ic_add);
                    shortcut.title = "添加";
                }
                ShortcutMgr.getInstance().updateShortcutOfChild(shortcut);
            }
        }
    }


    /**
     * 移除 shortcut
     * 从 cache 中移除 shortcut 信息
     * 并从本地 DB 中移除 shortcut 记录
     */
    public ShortcutRemoveResult removeShortcut(Shortcut shortcut, boolean removePage) {

        if (shortcut == null) {
            return null;
        }

        ShortcutRemoveResult removeResult = new ShortcutRemoveResult();
        removeResult.shortcut = shortcut;
        removeResult.isRemovedPage = false;

        if (allShortcutsOnDesktop.size() > shortcut.page) {
            List<Shortcut> shortcuts = allShortcutsOnDesktop.get(shortcut.page);
            if (shortcuts != null && shortcuts.size() > shortcut.posInPage) {
                shortcuts.set(shortcut.posInPage, null);

                // 如果当前页面的所有 shortcut 都为 null
                // 表明该页面已经没有 shortcut
                // 则可以移除该页面
                boolean hasNonNullShortcut = false;
                for (Shortcut s : shortcuts) {
                    if (s != null) {
                        hasNonNullShortcut = true;
                        break;
                    }
                }

                if (!hasNonNullShortcut) {
                    if (removePage) {
                        Intent intent = new Intent("DeleteShortcut");
                        intent.putExtra("page", shortcut.page);
                        context.sendBroadcast(intent);
                    }
                    removeResult.isRemovedPage = true;
                    allShortcutsOnDesktop.remove(shortcut.page);
                    // 同时需要更新该页后续的所有 shortcut 的 页号（-1）
                    if (allShortcutsOnDesktop.size() > shortcut.page) {
                        for (int idxPageNo = shortcut.page; idxPageNo < allShortcutsOnDesktop.size(); ++idxPageNo) {
                            List<Shortcut> shortcutsNeedToUpdate = allShortcutsOnDesktop.get(idxPageNo);
                            for (Shortcut shortcutNeedToUpdatePage : shortcutsNeedToUpdate) {
                                if (shortcutNeedToUpdatePage != null) {
                                    shortcutNeedToUpdatePage.page -= 1;
                                    shortcutNeedToUpdatePage.lastUpdatedTime = System.currentTimeMillis();
                                    updateShortcut(shortcutNeedToUpdatePage);
                                }
                            }
                        }
                    }

                    // 从所有 shortcut 记录中移除
                    allShortcuts.remove(shortcut.localId);
                }
            }
        }

        // 从本地 DB 中移除对应的 shortcut 信息
        ShortcutDao.getInstance().deleteShortcutWithPos(shortcut.page, shortcut.posInPage);

        removeResult.result = true;
        return removeResult;
    }

    // 新增 shortcut 至 DB
    private int addShortcutToDB(Shortcut shortcut) {

        if (shortcut == null) {
            return -1;
        }

        // 如果不存在，则新增
        Shortcut shortcutInDB = ShortcutDao.getInstance().getShortcutWithPos(shortcut.page, shortcut.posInPage);
        if (shortcutInDB == null) {
            YHLog.d(tag(), "addShortcutToDB - not exists in db, will add to db - " + shortcut);
            ShortcutDao.getInstance().addShortcut(shortcut);
        } else if (shortcut.enableUpdate || !shortcut.title.equals(shortcutInDB.title)) {
            // 如果允许更新，或者前后的标题不一致，则强制替换老的 shortcut 记录
            ShortcutDao.getInstance().deleteShortcutWithPos(shortcut.page, shortcut.posInPage);
            ShortcutDao.getInstance().addShortcut(shortcut);
        } else {
            YHLog.d(tag(), "addShortcutToDB - already exists of shortcut in db - " + shortcut);
            return shortcutInDB.localId;
        }

        return shortcut.localId;
    }

    // 新增子页面的 shortcut 至 DB
    private int addShortcutOfChildToDB(Shortcut shortcut) {

        if (shortcut == null || shortcut.parentLocalId <= 0) {
            return -1;
        }

        // 如果不存在，则新增
        Shortcut shortcutInDB = ShortcutDao.getInstance().getShortcutOfChild(
                shortcut.parentLocalId, shortcut.page, shortcut.posInPage);
        if (shortcutInDB == null) {
            YHLog.d(tag(), "addShortcutOfChildToDB - not exists in db, will add to db - " + shortcut);
            ShortcutDao.getInstance().addShortcut(shortcut);
        } else if (shortcut.enableUpdate || !shortcut.title.equals(shortcutInDB.title)) {
            // 如果允许更新，或者前后的标题不一致，则强制替换老的 shortcut 记录
            ShortcutDao.getInstance().deleteShortcutWithPos(shortcut.page, shortcut.posInPage);
            ShortcutDao.getInstance().addShortcut(shortcut);
        } else {
            YHLog.d(tag(), "addShortcutOfChildToDB - already exists of shortcut in db - " + shortcut);
        }

        return shortcut.localId;
    }

    //--------------------------------

    /**
     * 桌面快捷方式位置初始化
     */
    private void loadShortcuts() {
        YHLog.i(tag(), "begin load shortcuts");

        allShortcuts.clear();
        allShortcutsOnDesktop.clear();
        allShortcutsOfChild.clear();

        // 先初始化前面的内置应用的 box
        for (int pageIdx = 0; pageIdx < ShortcutConst.DEFAULT_PAGE_NUM; ++pageIdx) {
            List<Shortcut> shortcuts = new CopyOnWriteArrayList<>();
            allShortcutsOnDesktop.add(shortcuts);

            for (int posIdx = 0; posIdx < ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE; ++posIdx) {
                shortcuts.add(null);
            }
        }

        loadDefaultShortcuts();

        // 再根据 DB 中的 shortcut 更新
        YHLog.i(tag(), "begin load shortcuts from db");
        List<Shortcut> shortcutsFromDB = ShortcutDao.getInstance().loadAllShortcuts();
        if (shortcutsFromDB == null || shortcutsFromDB.size() == 0) {
            return;
        }
        YHLog.i(tag(), "end load shortcuts from db");

        for (Shortcut shortcut : shortcutsFromDB) {
            if (shortcut == null || shortcut.posInPage < 0) {
                continue;
            }

            // 先判断是否对应页面和位置 已经存在 shortcut，如果存在则更新
            // 如果不存在，则新增 shortcut
            if (shortcut.page >= 0 && shortcut.posInPage < ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE) {
                Shortcut existsShortcut = getShortcut(shortcut.page, shortcut.posInPage);
                if (existsShortcut != null) {
                    updateShortcut(shortcut);
                } else {
                    addShortcut(shortcut);
                }
            } else if (shortcut.page == ShortcutConst.DEFAULT_CHILD_PAGE && shortcut.parentLocalId > 0) {
                addShortcutToChild(shortcut);
            }

            // 对于外部应用，如果已经卸载的还需要移除对应的记录
            if (shortcut.type >= ShortcutType.EXTERNAL_APP) {
                if (!AppMgr.getInstance().isExistsApp(context, shortcut.appPackageName)) {
                    removeShortcut(shortcut, true);
                }
            }
        }

        YHLog.i(tag(), "end load shortcuts");
    }

    // 加载第一页的 shortcuts
    private void loadFirstPageShortcuts() {

        // SOS
        Shortcut shortcutSOS = new Shortcut(ShortcutConst.SOS_PAGE,
                ShortcutConst.SOS_POS, ShortcutConst.SOS, ShortcutType.SOS, false);
        shortcutSOS.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutSOS.extra = new ShortcutExtra(SosSettingActivity.class.getName(), null);
        shortcutSOS.iconResId = R.drawable.ic_sos;
        shortcutSOS.style = new ShortcutStyle(R.drawable.shortcut_box_fifth_bg, R.color.white);
        shortcutSOS.enableUpdate = true;
        addShortcut(shortcutSOS);

        // 控制中心
        Shortcut shortcutControl = new Shortcut(ShortcutConst.CONTROL_CENTER_PAGE,
                ShortcutConst.CONTROL_CENTER_POS,
                "控制中心", ShortcutType.CONTROL_CENTER, false);
        shortcutControl.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutControl.extra = new ShortcutExtra(ControlCenterActivity.class.getName(), null);
        shortcutControl.iconResId = R.drawable.ic_wlan_control_on;
        shortcutControl.style = new ShortcutStyle(R.drawable.shortcut_box_third_bg, R.color.white);
        shortcutControl.events.add(EventType.SYS_NET_CHANGE);
        shortcutControl.stateIcons.put(LauncherConst.CC_NET_STATE_WIFI_CONN_MOBILE_CONN,
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cc_net_wifi_conn));
        shortcutControl.stateIcons.put(LauncherConst.CC_NET_STATE_WIFI_DISCONN_MOBILE_CONN,
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cc_net_mobile_conn));
        shortcutControl.stateIcons.put(LauncherConst.CC_NET_STATE_WIFI_CONN_MOBILE_DISCONN,
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cc_net_wifi_conn));
        shortcutControl.stateIcons.put(LauncherConst.CC_NET_STATE_WIFI_DISCONN_MOBILE_DISCONN,
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cc_net_mobile_disconn));
        shortcutControl.enableUpdate = true;
        addShortcut(shortcutControl);

        // 家人及其二级页面
        // 先判断是否已经存在
        Shortcut shortcutFamily = new Shortcut(
                ShortcutConst.FAMILY_PAGE, ShortcutConst.FAMILY_POS,
                "家人", ShortcutType.FAMILY, true);
        shortcutFamily.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutFamily.extra = new ShortcutExtra(SecondActivity.class.getName(), null);
        shortcutFamily.iconResId = R.drawable.ic_album;
        shortcutFamily.style = new ShortcutStyle(R.drawable.shortcut_box_first_bg, R.color.white);
        shortcutFamily.enableUpdate = true;
        shortcutFamily.localId = addShortcut(shortcutFamily);

        final int DEFAULT_FAMILY_CONTACT_NUM = 16;
        for (int idxFamily = 0; idxFamily < DEFAULT_FAMILY_CONTACT_NUM; ++idxFamily) {
            Shortcut shortcutContact = ShortcutDao.getInstance().getShortcutOfChild(shortcutFamily.localId,
                    ShortcutConst.DEFAULT_CHILD_PAGE, idxFamily);
            if (shortcutContact == null) {
                shortcutContact = new Shortcut(
                        ShortcutConst.DEFAULT_CHILD_PAGE, idxFamily,
                        "添加", ShortcutType.ADD_CONTACT, false);
                shortcutContact.parentLocalId = shortcutFamily.localId;
                shortcutContact.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
                shortcutContact.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
                shortcutContact.iconResId = R.drawable.ic_add;
                shortcutContact.style = new ShortcutStyle(R.drawable.shortcut_box_second_bg, R.color.white);
                shortcutContact.localId = addShortcut(shortcutContact);
                shortcutFamily.shortcuts.add(shortcutContact.localId);
            }
        }

        // 朋友及其二级页面
        Shortcut shortcutFriends = new Shortcut(
                ShortcutConst.FRIENDS_PAGE, ShortcutConst.FRIENDS_POS,
                "朋友", ShortcutType.FRIENDS, true);
        shortcutFriends.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutFriends.extra = new ShortcutExtra(SecondActivity.class.getName(), null);
        shortcutFriends.iconResId = R.drawable.ic_album;
        shortcutFriends.style = new ShortcutStyle(R.drawable.shortcut_box_sixth_bg, R.color.white);
        shortcutFriends.enableUpdate = true;
        shortcutFriends.localId = addShortcut(shortcutFriends);

        final int DEFAULT_FRIEND_CONTACT_NUM = 16;
        for (int idxFriend = 0; idxFriend < DEFAULT_FRIEND_CONTACT_NUM; ++idxFriend) {
            Shortcut shortcutContact = ShortcutDao.getInstance().getShortcutOfChild(shortcutFriends.localId,
                    ShortcutConst.DEFAULT_CHILD_PAGE, idxFriend);
            if (shortcutContact == null) {
                shortcutContact = new Shortcut(
                        ShortcutConst.DEFAULT_CHILD_PAGE, idxFriend,
                        "添加", ShortcutType.ADD_CONTACT, false);
                shortcutContact.parentLocalId = shortcutFriends.localId;
                shortcutContact.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
                shortcutContact.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
                shortcutContact.iconResId = R.drawable.ic_add;
                shortcutContact.style = new ShortcutStyle(R.drawable.shortcut_box_second_bg, R.color.white);
                shortcutContact.localId = addShortcut(shortcutContact);
                shortcutFriends.shortcuts.add(shortcutContact.localId);
            }
        }

        // 常用联系人
        Shortcut shortcutTopContactFirst = ShortcutDao.getInstance().getShortcutWithPos(
                ShortcutConst.TOP_CONTACT_FIRST_PAGE, ShortcutConst.TOP_CONTACT_FIRST_POS);
        if (shortcutTopContactFirst == null) {
            shortcutTopContactFirst = new Shortcut(
                    ShortcutConst.TOP_CONTACT_FIRST_PAGE, ShortcutConst.TOP_CONTACT_FIRST_POS,
                    "添加", ShortcutType.ADD_CONTACT, false);
            shortcutTopContactFirst.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
            shortcutTopContactFirst.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
            shortcutTopContactFirst.iconResId = R.drawable.ic_add;
            shortcutTopContactFirst.style = new ShortcutStyle(R.drawable.shortcut_box_second_bg, R.color.white);
            shortcutTopContactFirst.enableUpdate = true;
        }
        addShortcut(shortcutTopContactFirst);

        Shortcut shortcutTopContactSecond = ShortcutDao.getInstance().getShortcutWithPos(
                ShortcutConst.TOP_CONTACT_SECOND_PAGE, ShortcutConst.TOP_CONTACT_SECOND_POS);
        if (shortcutTopContactSecond == null) {
            shortcutTopContactSecond = new Shortcut(
                    ShortcutConst.TOP_CONTACT_SECOND_PAGE, ShortcutConst.TOP_CONTACT_SECOND_POS,
                    "添加", ShortcutType.ADD_CONTACT, false);
            shortcutTopContactSecond.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
            shortcutTopContactSecond.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
            shortcutTopContactSecond.iconResId = R.drawable.ic_add;
            shortcutTopContactSecond.style = new ShortcutStyle(R.drawable.shortcut_box_forth_bg, R.color.white);
            shortcutTopContactSecond.enableUpdate = true;
        }
        addShortcut(shortcutTopContactSecond);

        Shortcut shortcutTopContactThird = ShortcutDao.getInstance().getShortcutWithPos(
                ShortcutConst.TOP_CONTACT_THIRD_PAGE, ShortcutConst.TOP_CONTACT_THIRD_POS);
        if (shortcutTopContactThird == null) {
            shortcutTopContactThird = new Shortcut(
                    ShortcutConst.TOP_CONTACT_THIRD_PAGE, ShortcutConst.TOP_CONTACT_THIRD_POS,
                    "添加", ShortcutType.ADD_CONTACT, false);
            shortcutTopContactThird.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
            shortcutTopContactThird.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
            shortcutTopContactThird.iconResId = R.drawable.ic_add;
            shortcutTopContactThird.style = new ShortcutStyle(R.drawable.shortcut_box_third_bg, R.color.white);
            shortcutTopContactThird.enableUpdate = true;
        }
        addShortcut(shortcutTopContactThird);

        // 通讯录
        Shortcut shortcutContact = new Shortcut(
                ShortcutConst.CONTACT_PAGE, ShortcutConst.CONTACT_POS,
                "联系人", ShortcutType.CONTACTS, false);
        Intent intentContact = new Intent();
        shortcutContact.extra = new ShortcutExtra(ContactListActivity.class.getName(), null);
        shortcutContact.intent = intentContact;
        shortcutContact.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutContact.iconResId = R.drawable.ic_contacts;
        shortcutContact.style = new ShortcutStyle(R.drawable.shortcut_box_fifth_bg, R.color.white);
        shortcutContact.enableUpdate = true;
        addShortcut(shortcutContact);
    }

    // 加载第 2 页的 shortcuts
    private void loadSecondPageShortcuts() {

        // 小秘书
        Shortcut shortHealth = new Shortcut(ShortcutConst.HEALTH_ASSISTANT_PAGE, ShortcutConst.HEALTH_ASSISTANT_POS,
                "小秘书", ShortcutType.HEALTH_ASSISTANT, false);
        shortHealth.iconResId = R.drawable.ic_second_health;
        shortHealth.style = new ShortcutStyle(R.drawable.shortcut_box_forth_bg, R.color.white);
        shortHealth.enableUpdate = true;
        addShortcut(shortHealth);

        // 健康管家
        Shortcut shortHappiness = new Shortcut(ShortcutConst.HEALTH_MGR_PAGE, ShortcutConst.HEALTH_MGR_POS,
                "健康管家", ShortcutType.HEALTH_MGR, false);
        shortHappiness.iconResId = R.drawable.ic_second_happiness;
        shortHappiness.style = new ShortcutStyle(R.drawable.shortcut_box_first_bg, R.color.white);
        shortHappiness.apkUrl = ShortcutConst.HEALTH_MGR_APK_URL;
        shortHappiness.enableUpdate = true;
        addShortcut(shortHappiness);

        // 照相机
        Shortcut shortCamrea = new Shortcut(ShortcutConst.CAMERA_PAGE, ShortcutConst.CAMERA_POS,
                "照相机", ShortcutType.CAMERA, false);
        shortCamrea.iconResId = R.drawable.ic_center_camera;
        shortCamrea.style = new ShortcutStyle(R.drawable.shortcut_box_third_bg, R.color.white);
        shortCamrea.enableUpdate = true;
        addShortcut(shortCamrea);

        // 微信
        Shortcut shortWeiXin = new Shortcut(ShortcutConst.WEIXIN_PAGE, ShortcutConst.WEIXIN_POS,
                "微信", ShortcutType.WEIXIN, false);
        shortWeiXin.iconResId = R.drawable.ic_center_weixin;
        shortWeiXin.style = new ShortcutStyle(R.drawable.shortcut_box_second_bg, R.color.white);
        shortWeiXin.enableUpdate = true;
        addShortcut(shortWeiXin);

        // 电话
        Shortcut shortPhone = new Shortcut(ShortcutConst.PHONE_PAGE, ShortcutConst.PHONE_POS,
                "电话", ShortcutType.CALL, false);
        shortPhone.iconResId = R.drawable.ic_center_phone;
        shortPhone.style = new ShortcutStyle(R.drawable.shortcut_box_fifth_bg, R.color.white);
        shortPhone.numSign = readMissCall();
        shortPhone.events.add(EventType.SYS_CALL_RECEIVED);
        shortPhone.enableUpdate = true;
        addShortcut(shortPhone);

        // 语音助手
        Shortcut shortVoiceAssistant = new Shortcut(ShortcutConst.VOICE_ASSISTANT_PAGE, ShortcutConst.VOICE_ASSISTANT_POS,
                "语音助手", ShortcutType.VOICE_ASSISTANT, false);
        shortVoiceAssistant.iconResId = R.drawable.ic_voice_assistant;
        shortVoiceAssistant.style = new ShortcutStyle(R.drawable.shortcut_box_sixth_bg, R.color.white);
        shortVoiceAssistant.enableUpdate = true;
        addShortcut(shortVoiceAssistant);
    }

    // 加载第 3 页的 shortcuts
    private void loadThirdPageShortcuts() {

        // 电子书
        Shortcut shortcutEbook = new Shortcut(
                ShortcutConst.EBOOK_PAGE, ShortcutConst.EBOOK_POS,
                ShortcutConst.EBOOK, ShortcutType.EBOOK, false);
        shortcutEbook.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutEbook.extra = new ShortcutExtra(EbookListActivity.class.getName(), null);
        shortcutEbook.iconResId = R.drawable.ic_ebook;
        shortcutEbook.style = new ShortcutStyle(R.drawable.shortcut_box_second_bg, R.color.white);
        shortcutEbook.enableUpdate = true;
        addShortcut(shortcutEbook);

        // 语音频道
        Shortcut shortMusicPlayer = new Shortcut(
                ShortcutConst.VOICE_CHANNEL_PAGE, ShortcutConst.VOICE_CHANNEL_POS,
                ShortcutConst.VOICE_CHANNEL, ShortcutType.VOICE_CHANNEL, false);
        shortMusicPlayer.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortMusicPlayer.iconResId = R.drawable.ic_voice_channel;
        shortMusicPlayer.style = new ShortcutStyle(R.drawable.shortcut_box_forth_bg, R.color.white);
        shortMusicPlayer.extra = new ShortcutExtra(MusicPlayerActivity.class.getName(), null);
        shortMusicPlayer.enableUpdate = true;
        addShortcut(shortMusicPlayer);

        // 相册
        Shortcut shortcutAlbum = new Shortcut(
                ShortcutConst.ALBUM_PAGE, ShortcutConst.ALBUM_POS,
                "相册", ShortcutType.ALBUM, false);
        Intent intentAlbum = new Intent();
        if (choosePhoto() != null) {
            intentAlbum = context.getPackageManager().getLaunchIntentForPackage(choosePhoto());
        }
        shortcutAlbum.intent = intentAlbum;
        shortcutAlbum.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutAlbum.iconResId = R.drawable.ic_album;
        shortcutAlbum.style = new ShortcutStyle(R.drawable.shortcut_box_third_bg, R.color.white);
        shortcutAlbum.enableUpdate = true;
        addShortcut(shortcutAlbum);

        // 上网
        Shortcut shortcutExplorer = new Shortcut(
                ShortcutConst.IE_PAGE, ShortcutConst.IE_POS,
                "上网", ShortcutType.INTERNET_EXPLORER, false);
        Intent intentExplorer = new Intent(Intent.ACTION_VIEW);
        intentExplorer.setData(Uri.parse("http://www.baidu.com"));
        shortcutExplorer.intent = intentExplorer;
        shortcutExplorer.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutExplorer.iconResId = R.drawable.ic_explorer;
        shortcutExplorer.style = new ShortcutStyle(R.drawable.shortcut_box_first_bg, R.color.white);
        shortcutExplorer.enableUpdate = true;
        addShortcut(shortcutExplorer);

        // SMS
        Shortcut shortcutSMS = new Shortcut(
                ShortcutConst.SMS_PAGE, ShortcutConst.SMS_POS,
                "短信", ShortcutType.SMS, false);
        shortcutSMS.extra = new ShortcutExtra(SmsListActivity.class.getName(), null);
        shortcutSMS.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutSMS.iconResId = R.drawable.ic_sms;
        shortcutSMS.style = new ShortcutStyle(R.drawable.shortcut_box_sixth_bg, R.color.white);
        shortcutSMS.events.add(EventType.SYS_SMS_RECEIVED);
        shortcutSMS.enableUpdate = true;
        addShortcut(shortcutSMS);

        // 老黄历
        Shortcut shortcutCalendar = new Shortcut(
                ShortcutConst.CALENDAR_PAGE, ShortcutConst.CALENDAR_POS,
                ShortcutConst.CALENDAR, ShortcutType.CALENDAR, false);
        Intent intentCalendar = null;
        if (context.getPackageManager().getLaunchIntentForPackage(ShortcutConst.CALENDAR_PKG_NAME) != null) {
            intentCalendar = context.getPackageManager().getLaunchIntentForPackage(ShortcutConst.CALENDAR_PKG_NAME);
        }
        shortcutCalendar.appPackageName = ShortcutConst.CALENDAR_PKG_NAME;
        shortcutCalendar.intent = intentCalendar;
        shortcutCalendar.apkUrl = ShortcutConst.CALENDAR_APK_URL;
        shortcutCalendar.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutCalendar.iconResId = R.drawable.ic_calendar;
        shortcutCalendar.style = new ShortcutStyle(R.drawable.shortcut_box_second_bg, R.color.white);
        shortcutCalendar.enableUpdate = true;
        addShortcut(shortcutCalendar);
    }

    // 加载第 4 页
    private void loadForthPageShortcuts() {
        // 工具
        Shortcut shortcutTools = new Shortcut(
                ShortcutConst.TOOLS_PAGE, ShortcutConst.TOOLS_POS,
                "工具", ShortcutType.TOOLS, true);
        shortcutTools.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutTools.extra = new ShortcutExtra(SecondActivity.class.getName(), null);
        shortcutTools.iconResId = R.drawable.ic_album;
        shortcutTools.style = new ShortcutStyle(R.drawable.shortcut_box_fifth_bg, R.color.white);
        shortcutTools.enableUpdate = true;
        shortcutTools.localId = addShortcut(shortcutTools);

        // 工具 - 计算器
        Shortcut shortcutCalculator = new Shortcut(
                ShortcutConst.DEFAULT_CHILD_PAGE, 0,
                "计算器", ShortcutType.CALCULATOR, false);
        shortcutCalculator.parentLocalId = shortcutTools.localId;
        Intent intentCalculator = new Intent();
        intentCalculator.setClassName("com.android.calculator2",
                "com.android.calculator2.Calculator");
        shortcutCalculator.intent = intentCalculator;
        shortcutCalculator.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutCalculator.iconResId = R.drawable.ic_album;
        shortcutCalculator.style = new ShortcutStyle(R.drawable.shortcut_box_first_bg, R.color.white);
        shortcutCalculator.enableUpdate = true;
        shortcutCalculator.localId = addShortcut(shortcutCalculator);
        shortcutTools.shortcuts.add(shortcutCalculator.localId);

        // 工具 - 闹钟
        Shortcut shortcutAlarmClock = new Shortcut(
                ShortcutConst.DEFAULT_CHILD_PAGE, 1,
                "闹钟", ShortcutType.ALARM_CLOCK, false);
        shortcutAlarmClock.parentLocalId = shortcutTools.localId;
        Intent alarms = new Intent();
        if (chooseAlarmClock() != null) {
            alarms = context.getPackageManager().getLaunchIntentForPackage(chooseAlarmClock());
        }
        shortcutAlarmClock.intent = alarms;
        shortcutAlarmClock.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutAlarmClock.iconResId = R.drawable.ic_album;
        shortcutAlarmClock.style = new ShortcutStyle(R.drawable.shortcut_box_third_bg, R.color.white);
        shortcutAlarmClock.enableUpdate = true;
        shortcutAlarmClock.localId = addShortcut(shortcutAlarmClock);
        shortcutTools.shortcuts.add(shortcutAlarmClock.localId);

        // 设置
        Shortcut shortcutSetting = new Shortcut(
                ShortcutConst.SETTING_PAGE, ShortcutConst.SETTING_POS,
                "设置", ShortcutType.SETTING, false);
        shortcutSetting.extra = new ShortcutExtra(SettingActivity.class.getName(), null);
        shortcutSetting.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutSetting.iconResId = R.drawable.ic_setting;
        shortcutSetting.style = new ShortcutStyle(R.drawable.shortcut_box_sixth_bg, R.color.white);
        shortcutSetting.enableUpdate = true;
        addShortcut(shortcutSetting);

        // 帮助中心
        Shortcut shortcutHelp = new Shortcut(ShortcutConst.HELP_CENTER_PAGE,
                ShortcutConst.HELP_CENTER_POS,
                "帮助中心", ShortcutType.HELP, false);
        shortcutHelp.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutHelp.extra = new ShortcutExtra(BaseWebActivity.class.getName(), null);
        shortcutHelp.iconResId = R.drawable.ic_help_center;
        shortcutHelp.style = new ShortcutStyle(R.drawable.shortcut_box_third_bg, R.color.white);
        shortcutHelp.enableUpdate = true;
        addShortcut(shortcutHelp);

        // 所有应用
        Shortcut shortcutAllApps = new Shortcut(
                ShortcutConst.ALL_APPS_PAGE, ShortcutConst.ALL_APPS_POS,
                "所有应用", ShortcutType.ALL_APPS, false);
        shortcutAllApps.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
        shortcutAllApps.extra = new ShortcutExtra(AllAppActivity.class.getName(), null);
        shortcutAllApps.iconResId = R.drawable.ic_all_apps;
        shortcutAllApps.style = new ShortcutStyle(R.drawable.shortcut_box_first_bg, R.color.white);
        shortcutAllApps.enableUpdate = true;
        addShortcut(shortcutAllApps);

        // 百度医生
        Shortcut shortcutBdDoctor = new Shortcut(
                ShortcutConst.BD_DOCTOR_PAGE, ShortcutConst.BD_DOCTOR_POS,
                ShortcutConst.BD_DOCTOR_NAME, ShortcutType.BD_DOCTOR, false);
        Intent intentBdDoctor = null;
        if (context.getPackageManager().getLaunchIntentForPackage(ShortcutConst.BD_DOCTOR_PKG_NAME) != null) {
            intentBdDoctor = context.getPackageManager().getLaunchIntentForPackage(ShortcutConst.BD_DOCTOR_PKG_NAME);
        }
        shortcutBdDoctor.appPackageName = ShortcutConst.BD_DOCTOR_PKG_NAME;
        shortcutBdDoctor.intent = intentBdDoctor;
        shortcutBdDoctor.apkUrl = ShortcutConst.BD_DOCTOR_APK_URL;
        shortcutBdDoctor.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutBdDoctor.iconResId = R.drawable.ic_bd_doctor;
        shortcutBdDoctor.style = new ShortcutStyle(R.drawable.shortcut_box_sixth_bg, R.color.white);
        shortcutBdDoctor.enableUpdate = true;
        addShortcut(shortcutBdDoctor);

        // 一键查话费
        Shortcut shortcutQueryCost = new Shortcut(
                ShortcutConst.QUERY_COST_PAGE, ShortcutConst.QUERY_COST_POS,
                ShortcutConst.QUERY_COST_NAME, ShortcutType.QUERY_COST, false);
        shortcutQueryCost.intentType = ShortcutConst.INTENT_TYPE_NULL;
        shortcutQueryCost.iconResId = R.drawable.ic_query_fee;
        shortcutQueryCost.style = new ShortcutStyle(R.drawable.shortcut_box_fifth_bg, R.color.white);
        shortcutQueryCost.events = new ArrayList<>();
        shortcutQueryCost.events.add(EventType.SVC_QUERY_BALANCE_UPD);
        shortcutQueryCost.enableUpdate = true;
        addShortcut(shortcutQueryCost);

        // 头条新闻
        Shortcut shortcutNeteaseNews = new Shortcut(
                ShortcutConst.NETEASE_NEWS_PAGE, ShortcutConst.NETEASE_NEWS_POS,
                ShortcutConst.NETEASE_NEWS_NAME, ShortcutType.NETEASE_NEWS, false);
        Intent intentNews = null;
        if (context.getPackageManager().getLaunchIntentForPackage(ShortcutConst.NETEASE_NEWS_PKG_NAME) != null) {
            intentNews = context.getPackageManager().getLaunchIntentForPackage(ShortcutConst.NETEASE_NEWS_PKG_NAME);
        }
        shortcutNeteaseNews.appPackageName = ShortcutConst.NETEASE_NEWS_PKG_NAME;
        shortcutNeteaseNews.intent = intentNews;
        shortcutNeteaseNews.apkUrl = ShortcutConst.NETEASE_NEWS_APK_URL;
        shortcutNeteaseNews.intentType = ShortcutConst.INTENT_TYPE_EXTERNAL_APP;
        shortcutNeteaseNews.icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_netease_news);
        shortcutNeteaseNews.style = new ShortcutStyle(R.drawable.shortcut_box_second_bg, R.color.white);
        shortcutNeteaseNews.enableUpdate = true;
        addShortcut(shortcutNeteaseNews);
    }

    // 加载内置的 shortcuts
    private void loadDefaultShortcuts() {
        loadFirstPageShortcuts();
        loadSecondPageShortcuts();
        loadThirdPageShortcuts();
        loadForthPageShortcuts();
    }

    // 相册
    public String choosePhoto() {
        if (context.getPackageManager().getLaunchIntentForPackage("com.miui.gallery") != null) {
            return "com.miui.gallery";
        }

        if (context.getPackageManager().getLaunchIntentForPackage("com.android.gallery3d") != null) {
            return "com.android.gallery3d";
        }

        if (context.getPackageManager().getLaunchIntentForPackage("com.meizu.media.gallery") != null) {
            return "com.meizu.media.gallery";
        }

        return null;
    }

    // 闹钟
    public String chooseAlarmClock() {
        if (context.getPackageManager().getLaunchIntentForPackage("com.android.deskclock") != null) {
            return "com.android.deskclock";
        }

        return null;
    }

    // 加载未接电话数
    private int readMissCall() {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{
                CallLog.Calls.TYPE
        }, " type=? and new=?", new String[]{
                CallLog.Calls.MISSED_TYPE + "", "1"
        }, "date desc");

        if (cursor != null) {
            result = cursor.getCount();
            cursor.close();
        }

        return result;
    }

    // 用于当联系人号码改变时 刷新对应桌面的快捷联系人
    public void updateContactShortcut(Shortcut shortcut, String param, long contactId) {
        if (shortcut != null && shortcut.extra.param != null &&
                shortcut.extra.param.equals(param)) {
            Contact contact = ContactMgr.getInstance().getContactByContactId(contactId);
            if (contact != null) {
                shortcut.title = contact.name;
                if (contact.bitmap == null) {
                    shortcut.setIcon(R.drawable.ic_head_image);
                } else {
                    shortcut.setIcon(Utilities.createCircleBitmap(contact.bitmap));
                }
            } else {
                shortcut.extra.param = null;
                shortcut.intentType = ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY;
                shortcut.extra = new ShortcutExtra(AddContactActivity.class.getName(), null);
                shortcut.setIcon(R.drawable.ic_add);
                shortcut.title = "添加";
            }
            ShortcutMgr.getInstance().updateShortcut(shortcut);
        }
    }

}
