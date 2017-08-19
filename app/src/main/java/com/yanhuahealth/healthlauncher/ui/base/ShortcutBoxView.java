package com.yanhuahealth.healthlauncher.ui.base;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.ShortcutType;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.IEventHandler;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.appmgr.AppMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;
import com.yanhuahealth.healthlauncher.utils.Voice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 专用于 shortcut box 的布局视图
 */
public class ShortcutBoxView extends LinearLayout implements IEventHandler {

    protected String tag() {
        return ShortcutBoxView.class.getName();
    }

    // 总体布局
    private LinearLayout layoutBox;

    // shortcut 中的标题
    private TextView tvTitle;

    // shortcut 中的图标 是在文字上方
    private ImageView ivIcon;

    // shortcut folder layout
    private LinearLayout layoutFolder;

    // 小图标一
    private ImageView iconOne;

    // 小图标二
    private ImageView iconTwo;

    // 小图标三
    private ImageView iconThree;

    // 小图标四
    private ImageView iconFour;

    // 角标
    private TextView tvNum;

    // 删除shortcut
    private DialogUtil deleteShortcutDialog;

    // // TODO: 2016/5/9
    private int lastX;
    private int lastY;
    int defaultLeft;
    int defaultTop;
    int defaultRight;
    int defaultBottom;

    public ShortcutBoxView(Context context) {
        super(context);
        init(null, 0);
    }

    public ShortcutBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ShortcutBoxView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ShortcutBoxView, defStyle, 0);

        // 标题
        String title = null;
        if (a.hasValue(R.styleable.ShortcutBoxView_shortcutTitle)) {
            title = a.getString(R.styleable.ShortcutBoxView_shortcutTitle);
        }

        // 图标
        Drawable drawableIcon = null;
        if (a.hasValue(R.styleable.ShortcutBoxView_shortcutIcon)) {
            drawableIcon = a.getDrawable(R.styleable.ShortcutBoxView_shortcutIcon);
            if (drawableIcon != null) {
                drawableIcon.setCallback(this);
            }
        }

        a.recycle();

        LayoutInflater inflater
                = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.shortcut_item, this, true);

        // shortcut box
        layoutBox = (LinearLayout) findViewById(R.id.shortcut_box);

        // icon
        ivIcon = (ImageView) findViewById(R.id.shortcut_icon_iv);
        if (ivIcon != null && drawableIcon != null) {
            ivIcon.setImageDrawable(drawableIcon);
        }

        // 角标
        tvNum = (TextView) findViewById(R.id.shortcut_spc_num_tv);

        // title
        tvTitle = (TextView) findViewById(R.id.shortcut_title_tv);
        if (tvTitle != null && title != null) {
            tvTitle.setText(title);
        }

        // icon folder
        layoutFolder = (LinearLayout) findViewById(R.id.shortcut_folder_icon_layout);
        if (layoutFolder != null) {
            layoutFolder.setVisibility(View.GONE);
        }

        // 小图标一
        iconOne = (ImageView) findViewById(R.id.shortcut_first_icon_iv);

        // 小图标二
        iconTwo = (ImageView) findViewById(R.id.shortcut_second_icon_iv);

        // 小图标三
        iconThree = (ImageView) findViewById(R.id.shortcut_third_icon_iv);

        // 小图标四
        iconFour = (ImageView) findViewById(R.id.shortcut_forth_icon_iv);
    }

    /**
     * 当前 ShortcutBoxView 对应的 Shortcut 属性
     */
    private Shortcut shortcut;

    /**
     * 根据给定的 shortcut 来设定 Shortcut Box
     */
    public void setWithShortcut(final Shortcut s) {

        shortcut = s;
        if (shortcut == null) {
            return;
        }

        // 标题设置
        tvTitle.setText(shortcut.title);
        tvTitle.setVisibility(View.VISIBLE);
        if (shortcut.style != null && shortcut.style.titleColor > 0) {
            tvTitle.setTextColor(getResources().getColor(shortcut.style.titleColor));
        }

        // 图标设置
        if (shortcut.isFolder) {
            layoutFolder.setVisibility(View.VISIBLE);
            setIconToFolder(shortcut);
            ivIcon.setVisibility(View.GONE);
        } else {
            setIconWithShortcut(ivIcon, shortcut, false);
        }

        // 背景设置
        if (shortcut.style != null && shortcut.style.backgroundColor > 0) {
            layoutBox.setBackgroundResource(shortcut.style.backgroundColor);
        }

        if (shortcut.numSign != 0) {
            tvNum.setVisibility(VISIBLE);
            tvNum.setText(String.valueOf(shortcut.numSign));
        }

        // 如果存在 intent
        // 则监听点击事件，并跳转至指定的页面
        if (!isSetClickListener) {
            super.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    YHLog.i(tag(), "click shortcut: " + shortcut);
                    new Voice(getContext()).playVoice(shortcut.title);

                    if (shortcut.type == ShortcutType.QUERY_COST && shortcut.isEnable) {
                        Map<String, Object> eventInfo = new HashMap<>();
                        eventInfo.put(EventType.KEY_SVC_QUERY_BALANCE_SHORTCUT_ID, shortcut.localId);
                        MainService.getInstance().sendBroadEvent(
                                new BroadEvent(EventType.SVC_QUERY_BALANCE, eventInfo));
                    }

                    // 处理页面间跳转
                    if (shortcut.intentType != ShortcutConst.INTENT_TYPE_NULL) {
                        startActivityWithShortcut(shortcut);
                    }
                }
            });


            // todo shortcut长按时间 只针对外部应用
            if (shortcut != null && shortcut.isEnable &&
                    (shortcut.page >= 4 || (shortcut.page == 3 && shortcut.posInPage == 7))) {
                deleteShortcutDialog = new DialogUtil(getContext(), new DialogUtil.OnDialogUtilListener() {
                    @Override
                    public void onClick(View view) {
//                        AppMgr.getInstance().updateApp(getContext(), shortcut.appPackageName);
//                        ShortcutMgr.getInstance().removeShortcut(shortcut, false);
//                        clearShortcut();
//                        deleteShortcutDialog.dismiss();
                        int pageNumBeforeRemoved = ShortcutMgr.getInstance().getShortcutPageNum();
                        ArrayList<Integer> lstShortcutPageNo = new ArrayList<>();
                        for (int idxPage = 0; idxPage < pageNumBeforeRemoved; ++idxPage) {
                            lstShortcutPageNo.add(idxPage);
                        }
                        if (ShortcutMgr.getInstance().removeShortcutApp(shortcut.appPackageName, shortcut)) {
                            int pageNumAfterRemoved = ShortcutMgr.getInstance().getShortcutPageNum();
                            if (pageNumBeforeRemoved > pageNumAfterRemoved) {
                                // 有页面被删除，则更新对应的页面序号值为 -1
                                int idxPageNo = 0;
                                for (int pageNo : lstShortcutPageNo) {
                                    if (pageNo == shortcut.page) {
                                        lstShortcutPageNo.set(idxPageNo, -1);
                                        break;
                                    }

                                    ++idxPageNo;
                                }
                                // 更新该页序号之后的 序号值递减 1
                                for (int tmpIdxPageNo = idxPageNo + 1; tmpIdxPageNo < lstShortcutPageNo.size(); ++tmpIdxPageNo) {
                                    int pageNo = lstShortcutPageNo.get(tmpIdxPageNo);
                                    lstShortcutPageNo.set(tmpIdxPageNo, pageNo - 1);
                                }
                            }
                            clearShortcut();
                        }
                        deleteShortcutDialog.dismiss();

                    }
                });

                super.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        deleteShortcutDialog.showDeleStyleDialog("是否删除");
                        return true;
                    }
                });

                // todo
               super.setOnTouchListener(new OnTouchListener() {
                   @Override
                   public boolean onTouch(View v, MotionEvent event) {
                       //获取到手指处的横坐标和纵坐标
                       int x = (int) event.getX();
                       int y = (int) event.getY();
                       switch (event.getAction()) {
                           case MotionEvent.ACTION_DOWN:

                               lastX = x;
                               lastY = y;

                               if (defaultLeft == 0) {
                                   defaultLeft = getLeft();
                               }

                               if (defaultTop == 0) {
                                   defaultTop = getTop();
                               }

                               if (defaultRight == 0) {
                                   defaultRight = getRight();
                               }

                               if (defaultBottom == 0) {
                                   defaultBottom = getBottom();
                               }

                               Log.i("tag123456", defaultLeft + "---" + defaultTop + "---" + defaultRight + "---" + defaultBottom);
                               break;


                           case MotionEvent.ACTION_MOVE:
                               //计算移动的距离
                               int offX = x - lastX;
                               int offY = y - lastY;
                               //调用layout方法来重新放置它的位置
                               layout(getLeft() + offX, getTop() + offY,
                                       getRight() + offX, getBottom() + offY);
                               Log.i("tag123456", getLeft() + "---" + getTop() + "---" + getRight() + "---" + getBottom() + "-----" + offX + "--" + offY);
                               break;

                           case MotionEvent.ACTION_UP:
                               layout(defaultLeft, defaultTop, defaultRight, defaultBottom);
                               Log.i("tag123456","我擦");
                               break;
                       }

                       return true;
                   }
               });
            }
        }

        // 处理监听事件
        if (shortcut.events != null && shortcut.events.size() > 0) {
            YHLog.d(tag(), "shortcut events: " + shortcut.events.size() + "|" + shortcut.title);
            for (int eventType : shortcut.events) {
                MainService.getInstance().regEventHandler(eventType, this);
                YHLog.d(tag(), "reg event handler - " + eventType + "|" + shortcut.title);
            }
        }
    }

    private boolean isSetClickListener = false;

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);

        if (l != null) {
            isSetClickListener = true;
        }
    }

    /**
     * 隐藏 shortcut
     */
    public void hideShortcut() {
        tvTitle.setVisibility(View.GONE);
        ivIcon.setVisibility(View.GONE);
        layoutFolder.setVisibility(View.GONE);
    }

    /**
     * 移除对应的 shortcut
     */
    public void clearShortcut() {
        hideShortcut();

        // 停止处理点击事件
        setOnClickListener(null);

        setOnLongClickListener(null);

        // 移除监听事件
        if (shortcut != null && shortcut.events != null && shortcut.events.size() > 0) {
            for (int eventType : shortcut.events) {
                MainService.getInstance().unregEventHandler(eventType, this);
            }
        }

        shortcut = null;
    }

    // 其中指定 shortcut 所对应的应用
    private void startActivityWithShortcut(Shortcut shortcut) {

        if (shortcut == null) {
            YHLog.w(tag(), "startActivityWithShortcut - shortcut is null");
            return;
        }

        if (shortcut.intentType == ShortcutConst.INTENT_TYPE_EXTERNAL_APP) {
            if (shortcut.intent != null) {
                // 跳转至 外部应用
                Intent intent = shortcut.intent;
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    YHLog.e(tag(), "setWithShortcut - exception: " + e.getMessage());
                    Toast.makeText(getContext(), "无法启动您选择的应用", Toast.LENGTH_LONG).show();
                    shortcut.intent = null;
                    ShortcutMgr.getInstance().updateShortcut(shortcut);
                }
            } else {
                // 本地没有安装，则下载并安装
                if (getContext().getPackageManager().getLaunchIntentForPackage(shortcut.appPackageName + "") != null) {
                    shortcut.intent = getContext().getPackageManager().getLaunchIntentForPackage(shortcut.appPackageName);
                    getContext().startActivity(shortcut.intent);
                } else {
                    if (shortcut.apkUrl != null) {
                        downloadApk(shortcut);
                    } else {
                        Toast.makeText(getContext(), "没有安装该应用", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else if (shortcut.intentType == ShortcutConst.INTENT_TYPE_INTERNAL_ACTIVITY
                && shortcut.extra != null) {
            // 跳转至指定的内部页面
            try {
                if (shortcut.extra.activity != null && shortcut.extra.activity.length() > 0) {
                    Intent intent = new Intent(getContext(), Class.forName(shortcut.extra.activity));

                    // 如果有参数需要传递给下一个页面
                    if (shortcut.extra.param != null && shortcut.extra.param.length() > 0) {
                        intent.putExtra(ShortcutConst.PARAM_INTENT, shortcut.extra.param);
                    }

                    // 传递对应的 shortcut 实例
                    intent.putExtra(ShortcutConst.PARAM_SHORTCUT, shortcut);
                    intent.putExtra(LauncherConst.INTENT_PARAM_SHORTCUT_ID, shortcut.localId);
                    ((Activity) getContext()).startActivityForResult(intent, 100);
                }
            } catch (ClassNotFoundException e) {
                YHLog.e(tag(), "setWithShortcut - can not for class: "
                        + shortcut.extra + "|" + e.getMessage());
            } catch (Exception e) {
                YHLog.e(tag(), "setWithShortcut - exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void notify(BroadEvent event) {

        if (event == null) {
            return;
        }

        switch (event.eventType) {
            case EventType.SYS_NET_CHANGE:
                handleNetChange(event, shortcut);
                break;

            case EventType.SYS_SMS_RECEIVED:
                handleSmsNum(event, shortcut);
                break;

            case EventType.SYS_SMS_CHANGE:
                break;

            case EventType.SYS_CALL_RECEIVED:
                handleCallNum(event, shortcut);
                break;

            case EventType.SVC_QUERY_BALANCE_UPD:
                // 查询话费余额的 view 更新
                YHLog.d(tag(), "notify - query balance update");
                setWithShortcut(shortcut);
                break;
        }
    }

    //  更新未读短信数量
    private void handleSmsNum(BroadEvent event, Shortcut shortcut) {
        YHLog.d(tag(), "handleSmsNum - " + shortcut);
        if (shortcut == null || event == null || event.eventInfo == null) {
            return;
        }

        if (shortcut.numSign != 0) {
            tvNum.setVisibility(VISIBLE);
            tvNum.setText(String.valueOf(shortcut.numSign));
        } else {
            tvNum.setVisibility(GONE);
        }
    }

    private void handleCallNum(BroadEvent event, Shortcut shortcut) {
        YHLog.d(tag(), "handleCallNum - " + shortcut);
        if (shortcut == null || event == null || event.eventInfo == null) {
            return;
        }

        if (shortcut.numSign != 0) {
            tvNum.setVisibility(VISIBLE);
            tvNum.setText(String.valueOf(shortcut.numSign));
        } else {
            tvNum.setVisibility(GONE);
        }
    }

    /**
     * 处理网络更新事件
     */
    private void handleNetChange(BroadEvent event, Shortcut shortcut) {
        YHLog.d(tag(), "handleNetChange - " + shortcut);
        if (shortcut == null || event == null || event.eventInfo == null) {
            return;
        }

        if (shortcut.stateIcons == null || shortcut.stateIcons.size() == 0) {
            return;
        }

        if (!(event.eventInfo.containsKey(EventType.KEY_SYS_NET_CHANGE_WIFI)
                && event.eventInfo.containsKey(EventType.KEY_SYS_NET_CHANGE_MOBILE))) {
            YHLog.i(tag(), "handleNetChange - not all exists of wifi and mobile");
            return;
        }

        NetworkInfo.State wifiState = (NetworkInfo.State) event.eventInfo.get(EventType.KEY_SYS_NET_CHANGE_WIFI);
        NetworkInfo.State mobileState = (NetworkInfo.State) event.eventInfo.get(EventType.KEY_SYS_NET_CHANGE_MOBILE);

        Bitmap iconBmp;
        if (NetworkInfo.State.CONNECTED == wifiState) {
            if (NetworkInfo.State.CONNECTED == mobileState) {
                // wifi 连接，mobile 连接
                iconBmp = shortcut.stateIcons.get(LauncherConst.CC_NET_STATE_WIFI_CONN_MOBILE_CONN);
            } else {
                // wifi 连接，mobile 未连接
                iconBmp = shortcut.stateIcons.get(LauncherConst.CC_NET_STATE_WIFI_CONN_MOBILE_DISCONN);
            }
        } else {
            if (NetworkInfo.State.CONNECTED == mobileState) {
                // wifi 未连接，mobile 连接
                iconBmp = shortcut.stateIcons.get(LauncherConst.CC_NET_STATE_WIFI_DISCONN_MOBILE_CONN);
            } else {
                // wifi 未连接，mobile 未连接
                iconBmp = shortcut.stateIcons.get(LauncherConst.CC_NET_STATE_WIFI_DISCONN_MOBILE_DISCONN);
            }
        }

        if (iconBmp != null) {
            ivIcon.setVisibility(View.VISIBLE);
            layoutFolder.setVisibility(View.GONE);
            ivIcon.setImageBitmap(iconBmp);
        }
    }

    // 下载未安装的APK
    public void downloadApk(Shortcut shortcut) {
        Intent intent = new Intent();
        intent.setAction(LauncherConst.INTENT_ACTION_DOWN_APK);
        intent.putExtra(LauncherConst.INTENT_PARAM_APK_URL, shortcut.apkUrl);
        intent.putExtra(LauncherConst.INTENT_PARAM_APK_TITLE, shortcut.title);
        getContext().sendBroadcast(intent);
    }

    public ArrayList<Shortcut> folderIcon(Shortcut shortcutFolder) {
        if (shortcutFolder != null && shortcutFolder.isFolder) {
            List<Integer> shortcuts = shortcutFolder.shortcuts;
            if (shortcuts.size() > 0) {
                ArrayList<Shortcut> shortcutIcons = new ArrayList<>();
                for (int shortcutLocalId : shortcuts) {
                    Shortcut st = ShortcutMgr.getInstance().getShortcut(shortcutLocalId);
                    shortcutIcons.add(st);
                    if (st.title.equals("添加")) {
                        shortcutIcons.remove(st);
                    }
                }
                return shortcutIcons;
            } else {
                return null;
            }
        }
        return null;
    }

    private void setIconWithShortcut(ImageView ivIconToSet, Shortcut shortcut, boolean isFolder) {

        if (ivIconToSet == null || shortcut == null) {
            return;
        }

        if (isFolder) {
            layoutFolder.setVisibility(View.VISIBLE);
            ivIcon.setVisibility(View.GONE);
        } else {
            layoutFolder.setVisibility(View.GONE);
            ivIcon.setVisibility(View.VISIBLE);
        }

        if (shortcut.iconResId > 0) {
            ivIconToSet.setVisibility(View.VISIBLE);
            ivIconToSet.setImageResource(shortcut.iconResId);
        } else if (shortcut.iconUrl != null) {
            ivIconToSet.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(shortcut.iconUrl, ivIconToSet);
        } else if (shortcut.icon != null) {
            ivIconToSet.setVisibility(View.VISIBLE);
            ivIconToSet.setImageBitmap(shortcut.icon);
        }
    }

    public void setIconToFolder(Shortcut shortcut) {
        if (folderIcon(shortcut) != null) {
            ArrayList<Shortcut> list = folderIcon(shortcut);
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).title.equals("添加")) {
                        list.remove(i);
                    }
                }
            }

            switch (list.size()) {
                case 0:
                    iconOne.setImageResource(R.drawable.ic_add);
                    iconTwo.setImageResource(R.drawable.ic_add);
                    iconThree.setImageResource(R.drawable.ic_add);
                    iconFour.setImageResource(R.drawable.ic_add);
                    break;

                case 1:
                    setIconWithShortcut(iconOne, list.get(0), true);
                    iconTwo.setImageResource(R.drawable.ic_add);
                    iconThree.setImageResource(R.drawable.ic_add);
                    iconFour.setImageResource(R.drawable.ic_add);
                    break;

                case 2:
                    setIconWithShortcut(iconOne, list.get(0), true);
                    setIconWithShortcut(iconTwo, list.get(1), true);
                    iconThree.setImageResource(R.drawable.ic_add);
                    iconFour.setImageResource(R.drawable.ic_add);
                    break;

                case 3:
                    setIconWithShortcut(iconOne, list.get(0), true);
                    setIconWithShortcut(iconTwo, list.get(1), true);
                    setIconWithShortcut(iconThree, list.get(2), true);
                    iconFour.setImageResource(R.drawable.ic_add);
                    break;

                default:
                    setIconWithShortcut(iconOne, list.get(0), true);
                    setIconWithShortcut(iconTwo, list.get(1), true);
                    setIconWithShortcut(iconThree, list.get(2), true);
                    setIconWithShortcut(iconFour, list.get(3), true);
                    break;
            }
        }
    }

}
