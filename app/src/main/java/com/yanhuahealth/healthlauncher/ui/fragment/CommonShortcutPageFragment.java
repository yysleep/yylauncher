package com.yanhuahealth.healthlauncher.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.IEventHandler;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.base.ShortcutBoxView;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于新增的通用的快捷方式桌面
 * 当前仅支持 2x4 八宫格
 */
public class CommonShortcutPageFragment extends YHBaseFragment implements IEventHandler {

    @Override
    protected String tag() {
        return CommonShortcutPageFragment.class.getName();
    }

    public static final String PARAM_PAGE = "page";

    /**
     * 如果指定了页号，则在初始化时，会自动加载指定页面下的所有 shortcut 列表
     */
    private int pageNo = ShortcutMgr.getInstance().getShortcutPageNum()-1;

    public CommonShortcutPageFragment() {
    }

    // 对应于 8 个 shortcut box
    private List<ShortcutBoxView> shortcutBoxViewList = new ArrayList<>();

    // 保存 parent view，其他组件使用此 find 和 加载
    private View parentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        YHLog.d(tag(), "onCreateView - page:" + pageNo);
        Bundle bundle = getArguments();
        if (bundle != null) {
            pageNo = bundle.getInt(PARAM_PAGE, -1);
        }

        if (parentView == null) {
            parentView = inflater.inflate(R.layout.shortcut_page, container, false);
            initView(parentView);
        } else {
            ViewGroup parent = (ViewGroup)parentView.getParent();
            if (parent != null) {
                parent.removeView(parentView);
            }
        }

        return parentView;
    }

    @Override
    public void onStart() {
        YHLog.d(tag(), "onStart - page:" + pageNo);
        super.onStart();
        reloadShortcuts();
        MainService.getInstance().regEventHandler(EventType.SVC_NOTIFY_REFRESH, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        MainService.getInstance().unregEventHandler(EventType.SVC_NOTIFY_REFRESH, this);
    }

    // UI 组件初始化
    public void initView(View rootView) {
        loadShortcutBoxViews(rootView);
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    // 上次 reload 时间
    private long lastReloadTime;

    // 重新加载 shortcut box
    public void reloadShortcuts() {
        if (pageNo >= 0) {
            YHLog.d(tag(), "reloadShortcuts - page: " + pageNo);
            for (int pos = 0; pos < ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE; ++pos) {
                ShortcutBoxView boxView = shortcutBoxViewList.get(pos);
                if (boxView != null) {
                    Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(pageNo, pos);
                    if (shortcut != null) {
                        boxView.setWithShortcut(shortcut);
                    } else  {
                        boxView.clearShortcut();
                    }
                }
            }

            lastReloadTime = System.currentTimeMillis();
        }
    }

    // 加载所有的 shortcutBoxView 组件至 shortcutBoxViewList
    public void loadShortcutBoxViews(View root) {
        YHLog.d(tag(), "loadShortcutBoxViews");
        if (root == null) {
            return ;
        }

        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.first_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.second_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.third_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.forth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.fifth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.sixth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.seventh_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.eighth_shortcut));
    }

    // 获取指定位置的 shortcut box
    public ShortcutBoxView getShortcutBoxWithPos(int pos) {
        if (pos < 0 || pos >= shortcutBoxViewList.size()) {
            return null;
        }

        return shortcutBoxViewList.get(pos);
    }

    /**
     * 根据 shortcut 实例展示对应的快捷方式
     */
    public void setShortcut(final Shortcut shortcut) {

        if (shortcut == null) {
            return ;
        }

        if (shortcut.posInPage >= shortcutBoxViewList.size()) {
            return ;
        }

        ShortcutBoxView shortcutBoxView = shortcutBoxViewList.get(shortcut.posInPage);
        if (shortcutBoxView == null) {
            return ;
        }

        shortcutBoxView.setWithShortcut(shortcut);
        shortcutBoxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = shortcut.intent;
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    /**
     * 清除指定的 shortcut box
     */
    public void clearShortcutBox(Shortcut shortcut) {
        if (shortcut == null) {
            return ;
        }

        if (shortcut.posInPage >= shortcutBoxViewList.size()) {
            return ;
        }

        ShortcutBoxView shortcutBoxView = shortcutBoxViewList.get(shortcut.posInPage);
        if (shortcutBoxView == null) {
            return ;
        }

        shortcutBoxView.hideShortcut();
    }

    @Override
    public void notify(BroadEvent event) {
        YHLog.d(tag(), "notify - page: " + pageNo + "|" + event);
        reloadShortcuts();
    }

}
