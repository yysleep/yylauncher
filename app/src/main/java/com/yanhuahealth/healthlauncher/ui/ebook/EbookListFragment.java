
package com.yanhuahealth.healthlauncher.ui.ebook;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.ebook.Ebook;
import com.yanhuahealth.healthlauncher.sys.EbookMgr;
import com.yanhuahealth.healthlauncher.ui.MainActivity;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseFragment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 电子书列表页
 */
public class EbookListFragment extends YHBaseFragment {


    // 电子书分类位置和名称
    public static final String ARG_CAT_ID = "cat_id";

    private int catId;

    @Override
    protected String tag() {
        return null;
    }

    public EbookListFragment() {
        // Required empty public constructor
    }

    BroadcastReceiver completeTaskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity.CAT_ID_TO_FRAGMENT_ACTION)) {
                new EbookTask(catId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                ebookListAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // 该条语句必须放在 super.onCreate 之前
        catId = getArguments().getInt(ARG_CAT_ID);
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(completeTaskReceiver, new IntentFilter("catIdToFragment"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(completeTaskReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ebook_list, container, false);
        initView(rootView);
        return rootView;
    }

    // 加载电子书的进度条
    private LinearLayout layoutLoading;

    // 电子书列表
    private ListView lvEbooks;

    // 电子书列表项适配器
    public EbookListAdapter ebookListAdapter;

    // 没有电子书时的提示
    private TextView tvNoItemTip;

    private void initView(View rootView) {
        layoutLoading = (LinearLayout) rootView.findViewById(R.id.loading_ebook_layout);

        tvNoItemTip = (TextView) rootView.findViewById(R.id.no_item_tip_tv);
        tvNoItemTip.setVisibility(View.GONE);

        lvEbooks = (ListView) rootView.findViewById(R.id.ebooks_lv);
        ebookListAdapter = new EbookListAdapter(getActivity());
        lvEbooks.setAdapter(ebookListAdapter);
        lvEbooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Ebook ebook = (Ebook) ebookListAdapter.getItem(position);
                if (ebook == null) {
                    return;
                }

                String localPath;
                if (ebook.localPath != null && ebook.localPath.length() > 0) {
                    localPath = ebook.localPath;
                    File file = new File(localPath);
                    if (file.exists()) {
                        ebook.status = Ebook.STATUS_DOWNLOAD_FINISH;
                        Uri path = Uri.fromFile(file);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(path, "application/pdf");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }

                        ebookListAdapter.notifyDataSetChanged();
                        return;
                    }
                }

                Toast.makeText(getActivity(), "当前暂不支持 pdf 以外的格式", Toast.LENGTH_LONG).show();
            }
        });
//        lvEbooks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
//                final Ebook ebook = (Ebook) ebookListAdapter.getItem(position);
//                if (ebook == null) {
//                    return false;
//                }
//                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("提示");
//                builder.setMessage("确认删除吗");
//                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        boolean isRemove = EbookMgr.getInstance().removeEbook(ebook);
//                        if (isRemove) {
//                            ebookListAdapter.ebookList.remove(ebook);
//                            new EbookTask(catId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                            ebookListAdapter.notifyDataSetChanged();
//                        }
//                        dialog.dismiss();
//                    }
//                });
//                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                builder.create().show();
//                return true;
//            }
//        });
    }

    @Override
    public void onStart() {
        super.onStart();
        new EbookTask(catId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 电子书列表项适配
     */
    public class EbookListAdapter extends BaseAdapter {

        // 表示当前分类下的所有电子书列表
        // 排在列表最前面的为 最新的电子书
        public List<Ebook> ebookList = new ArrayList<>();
        private Context context;

        public EbookListAdapter(Context context) {
            this.context = context;
        }

        public void setEbooks(List<Ebook> ebooks) {
            this.ebookList = ebooks;
        }

        @Override
        public int getCount() {
            return ebookList.size() > 0 ? ebookList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return ebookList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.ebook_fragment_list_item, parent, false);
            }

            Ebook ebook = (Ebook) getItem(position);
            if (ebook == null) {
                return convertView;
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.name_tv);
            tvName.setText(ebook.name);

            TextView tvSubtitle = (TextView) convertView.findViewById(R.id.subtitle_tv);
            tvSubtitle.setText("作者：" + ebook.author);
            ImageView ivEbook = (ImageView) convertView.findViewById(R.id.ebook_left_iv);
            if (ebook.thumbUrl != null && ebook.thumbUrl.length() > 0) {
                ImageLoader.getInstance().displayImage(ebook.thumbUrl, ivEbook);
            } else {
                ivEbook.setImageResource(R.drawable.ic_default_book_transparent);
            }

            return convertView;
        }
    }

    /**
     * 异步加载当前分类下的本地电子书列表
     */
    class EbookTask extends AsyncTask<Void, Void, List<Ebook>> {

        // 电子书分类标识
        private int catId;

        public EbookTask(int catId) {
            this.catId = catId;
        }

        @Override
        protected void onPostExecute(List<Ebook> ebooks) {
            super.onPostExecute(ebooks);
            if (ebooks != null && ebooks.size() > 0) {
                ebookListAdapter.setEbooks(ebooks);

                lvEbooks.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.GONE);
                tvNoItemTip.setVisibility(View.GONE);

                ebookListAdapter.notifyDataSetChanged();
            } else {
                layoutLoading.setVisibility(View.GONE);
                tvNoItemTip.setVisibility(View.VISIBLE);
                lvEbooks.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Ebook> doInBackground(Void... params) {
            List<Ebook> ebooks;
            if (catId == 0) {
                ebooks = EbookMgr.getInstance().getAllLocalEbooks();
            } else {
                ebooks = EbookMgr.getInstance().getLocalEbooksWithCat(catId);
            }

            return ebooks;
        }
    }

}
