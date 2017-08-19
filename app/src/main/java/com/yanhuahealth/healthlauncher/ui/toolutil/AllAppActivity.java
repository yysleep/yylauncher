package com.yanhuahealth.healthlauncher.ui.toolutil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.app.AppInfo;
import com.yanhuahealth.healthlauncher.sys.appmgr.AppMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有应用
 */
public class AllAppActivity extends YHBaseActivity {
    @Override
    protected String tag() {
        return AllAppActivity.class.getName();
    }

    private AppAdapter appAdapter;
    private ProgressBar appProgress;
    /**
     * 该列表总数对应于需要展示的 shortcut page 总数
     * 列表中的每个值对应于更新后的页号
     * 如果值为 -1 表示原始的对应于该位置序号的 shortcut page 需要被移除
     */
    private ArrayList<Integer> lstShortcutPageNo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allapp);

        NavBar navBar = new NavBar(this);
        navBar.setTitle("所有应用");
        navBar.hideRight();
        navBar.getLeftImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lstShortcutPageNo.size() > 0) {
                    getIntent().putIntegerArrayListExtra("PageNo", lstShortcutPageNo);
                }

                setResult(100, getIntent());
                finish();
            }
        });

        ArrayList<AppInfo> appList = new ArrayList<>();

        // 根据当前页面总数初始化页面序号
        int currentPageCount = ShortcutMgr.getInstance().getShortcutPageNum();
        for (int idxPage = 0; idxPage < currentPageCount; ++idxPage) {
            lstShortcutPageNo.add(idxPage);
        }

        // 获取ListView并且绑定适配器
        ListView appListView = (ListView) findViewById(R.id.main_list);
        appProgress = (ProgressBar) findViewById(R.id.app_progress);
        appAdapter = new AppAdapter(AllAppActivity.this, appList);

        if (appListView != null) {
            appListView.setAdapter(appAdapter);
        } else {
            Toast.makeText(this, "没有应用可加载", Toast.LENGTH_SHORT).show();
        }

        // 启动加载应用的任务
        new LoadAppTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // 异步加载设备上安装的应用
    class LoadAppTask extends AsyncTask<String, Integer, Integer> {

        // 存放所有加载成功的应用包列表
        private List<AppInfo> allAppPackages = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            appProgress.setVisibility(View.VISIBLE);
            allAppPackages.clear();
        }

        @Override
        protected Integer doInBackground(String... params) {
            while (true) {
                List<AppInfo> alreadyLoadedApp = AppMgr.getInstance().getAllApps();
                if (alreadyLoadedApp == null || alreadyLoadedApp.size() == 0) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    for (AppInfo appInfo : alreadyLoadedApp) {
                        if (appInfo != null) {
                            allAppPackages.add(appInfo);
                        }
                    }

                    break;
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == 0) {
                for (AppInfo appInfo : allAppPackages) {
                    appAdapter.addApp(appInfo);
                }

                appAdapter.notifyDataSetChanged();
            }
            appProgress.setVisibility(View.GONE);
        }
    }

    /**
     * 自定义BaseAdapter
     */
    public class AppAdapter extends BaseAdapter {

        private DialogUtil allAppDialog;
        Context context;
        private ArrayList<AppInfo> dataList = new ArrayList<>();
        private AppInfo sendAppUnits;
        private ImageView sendIv;

        public AppAdapter(Context context, ArrayList<AppInfo> inputDataList) {
            this.context = context;
            dataList.clear();
            for (int i = 0; i < inputDataList.size(); i++) {
                dataList.add(inputDataList.get(i));
            }
        }

        public void addApp(AppInfo app) {
            if (dataList == null) {
                dataList = new ArrayList<>();
            }

            dataList.add(app);
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            final AppInfo appUnit = dataList.get(position);
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.activity_allapp_detail, parent, false);
            }

            TextView tvAllAppName = (TextView) convertView.findViewById(R.id.allapp_title_tv);
            tvAllAppName.setSingleLine();
            tvAllAppName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
            tvAllAppName.setText(appUnit.appName);

            final ImageView appIcon = (ImageView) convertView.findViewById(R.id.allapp_icon_iv);
            if (appIcon != null) {
                appIcon.setImageDrawable(appUnit.appIcon);
            }

            final ImageView ivAppChange = (ImageView) convertView.findViewById(R.id.allapp_change_iv);
            if (appUnit.shortcut == null) {
                ivAppChange.setImageResource(R.drawable.ic_allapp_add);
            } else {
                ivAppChange.setImageResource(R.drawable.ic_allapp_remove);
            }

            allAppDialog = new DialogUtil(AllAppActivity.this, new DialogUtil.OnDialogUtilListener() {
                @Override
                public void onClick(View view) {
                    int pageNumBeforeRemoved = ShortcutMgr.getInstance().getShortcutPageNum();
                    if (ShortcutMgr.getInstance().removeExAppShortcut(sendAppUnits.packageInfo)) {
                        sendIv.setImageResource(R.drawable.ic_allapp_add);
                        int pageNumAfterRemoved = ShortcutMgr.getInstance().getShortcutPageNum();
                        if (pageNumBeforeRemoved > pageNumAfterRemoved) {
                            // 有页面被删除，则更新对应的页面序号值为 -1
                            int idxPageNo = 0;
                            for (int pageNo : lstShortcutPageNo) {
                                if (pageNo == sendAppUnits.shortcut.page) {
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

                        sendAppUnits.shortcut = null;
                    }
                    ShortcutMgr.getInstance().removeExAppShortcut(sendAppUnits.packageInfo);
                    allAppDialog.dismiss();

                }
            }, sendAppUnits, sendIv);

            ivAppChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (appUnit.shortcut == null) {
                        appUnit.shortcut = ShortcutMgr.getInstance().addExAppShortcut(appUnit.packageInfo);
                        if (appUnit.shortcut != null) {
                            ivAppChange.setImageResource(R.drawable.ic_allapp_remove);
                        }
                    } else {
                        sendAppUnits = appUnit;
                        sendIv = ivAppChange;
                        allAppDialog.showFirstStyleDialog("移除应用", "确定移除吗？", "确定移除");
                    }
                }
            });

            return convertView;
        }

    }
}
