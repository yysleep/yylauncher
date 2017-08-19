


package com.yanhuahealth.healthlauncher.ui.ebook;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.api.ApiConst;
import com.yanhuahealth.healthlauncher.api.ApiResponseResult;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.ebook.Ebook;
import com.yanhuahealth.healthlauncher.sys.EbookMgr;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.TaskType;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.DownloadManagerUtils;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 电子书网络图书馆
 */
public class EbookDownloadActivity extends YHBaseActivity {
    private ArrayList<Ebook> downloadEbooks;
    private DownloadAdapter downloadAdapter;
    private LinearLayout layoutLoading;
    private ProgressBar progressBar;
    private TextView tvLoadMore;
    private ListView lvDownload;
    private View viewFooter;
    private long selectDownloadId;
    SharedPreferences preferences;

    private static final int DEFAULT_LOAD_EBOOK_CNT = 10;

    BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                String fileName = intent.getStringExtra(LauncherConst.DOWNLOAD_PATH_EBOOK);
                if (fileName != null) {
                    Toast.makeText(EbookDownloadActivity.this,
                            fileName + "已经下载完成", Toast.LENGTH_SHORT).show();
                }

                Ebook ebook = EbookMgr.getInstance().getDownloadingEbookWithDownloadId(downloadId);
                if (ebook != null) {
                    ebook.status = Ebook.STATUS_DOWNLOAD_FINISH;
                }
                YHLog.i(tag(), "download task finish - " + downloadId);
                downloadAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected String tag() {
        return EbookDownloadActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_download);
        initView();
        MainService.getInstance().getEbooks(tag(), 0, 1, 0, DEFAULT_LOAD_EBOOK_CNT, null);
        registerReceiver(downloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void initView() {
        NavBar navBar = new NavBar(EbookDownloadActivity.this);
        navBar.setTitle("网络图书馆");
        navBar.hideRight();

        layoutLoading = (LinearLayout) findViewById(R.id.loading_layout_download_ebooks);
        layoutLoading.setVisibility(View.VISIBLE);
        lvDownload = (ListView) findViewById(R.id.ebook_download_lv);

        viewFooter = LayoutInflater.from(EbookDownloadActivity.this).inflate(R.layout.ebooks_list_footer,lvDownload,false);
        tvLoadMore = (TextView) viewFooter.findViewById(R.id.load_more_ebooks_tv);
        progressBar = (ProgressBar) viewFooter.findViewById(R.id.load_progress_ebooks);
        progressBar.setVisibility(View.GONE);
        tvLoadMore.setText("点击加载更多书籍");
        lvDownload.addFooterView(viewFooter);
        viewFooter.setVisibility(View.GONE);
        downloadEbooks = new ArrayList<>();

        // 列表底部加载更多点击事件
        viewFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                tvLoadMore.setText("正在加载");
                MainService.getInstance().getEbooks(tag(), 0, 1, downloadAdapter.getCount(), DEFAULT_LOAD_EBOOK_CNT, null);
            }
        });

        downloadAdapter = new DownloadAdapter();
        lvDownload.setAdapter(downloadAdapter);
    }

    private class DownloadAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return downloadEbooks.size();
        }

        @Override
        public Object getItem(int position) {
            return downloadEbooks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(EbookDownloadActivity.this).
                        inflate(R.layout.ebook_list_item, parent, false);
            }

            final Ebook ebook = downloadEbooks.get(position);
            if (ebook == null) {
                return convertView;
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.name_tv);
            if (ebook.name != null) {
                tvName.setText(ebook.name);
            }

            TextView tvSubtitle = (TextView) convertView.findViewById(R.id.subtitle_tv);
            if (ebook.author != null) {
                String author = "作者：" + ebook.author;
                tvSubtitle.setText(author);
            }

            TextView tvContentSize = (TextView) convertView.findViewById(R.id.content_size_tv);
            String contentSize = "大小：" + ebook.size + " MB";
            tvContentSize.setText(contentSize);
            final ImageView ivEbook = (ImageView) convertView.findViewById(R.id.ebook_left_iv);
            ivEbook.setImageResource(R.drawable.ic_default_book_transparent);

            if (ebook.thumbUrl != null && ebook.thumbUrl.length() > 0) {
                ImageLoader.getInstance().loadImage(ebook.thumbUrl, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        File fileLocalImage = ImageLoader.getInstance().getDiskCache().get(imageUri);
                        if (fileLocalImage.exists()) {
                            EbookMgr.getInstance().saveEbookThumb(ebook, fileLocalImage);
                        }

                        if (loadedImage != null) {
                            ivEbook.setImageBitmap(loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
            }

            final Button btnStatus = (Button) convertView.findViewById(R.id.status_btn);
            btnStatus.setVisibility(View.VISIBLE);
            btnStatus.setTag(position);
            btnStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ebook.downloadUrl == null) {
                        return;
                    }

                    int ebookId = ebook.catId;
                    String ebookName = ebook.id + "-" + ebookId + "-" + ebook.name + "-" + ebook.author + ".pdf";
                    File ebookFile = new File(LauncherConst.getEbookRootPath() + ebookName);
                    if (ebookFile.exists()) {
                        Toast.makeText(EbookDownloadActivity.this, "此电子书已经下载过", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!PhoneStatus.getInstance().isWiFi(EbookDownloadActivity.this)) {
                        Toast.makeText(EbookDownloadActivity.this, "请打开WiFi下载", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    btnStatus.setText("下载中");
                    btnStatus.setEnabled(false);
                    ebook.status = Ebook.STATUS_DOWNLOADING;
                    btnStatus.setBackgroundResource(R.drawable.default_btn_disclick_bg);
                    EbookMgr.getInstance().downloadEbook(EbookDownloadActivity.this, ebook);
                    selectDownloadId = DownloadManagerUtils.getInstance().currDownloadId;
                    ebook.downloadId = selectDownloadId;
                }
            });

            switch (ebook.status) {
                case Ebook.STATUS_DOWNLOAD_FINISH:
                    btnStatus.setText("已下载");
                    btnStatus.setEnabled(false);
                    btnStatus.setBackgroundResource(R.drawable.default_btn_disable_bg);
                    break;

                case Ebook.STATUS_INIT:
                    btnStatus.setText("下载");
                    btnStatus.setEnabled(true);
                    btnStatus.setBackgroundResource(R.drawable.default_btn_normal_bg);
                    break;

                case Ebook.STATUS_DOWNLOADING:
                    btnStatus.setText("下载中");
                    btnStatus.setEnabled(false);
                    btnStatus.setBackgroundResource(R.drawable.default_btn_disclick_bg);
                    break;

                case Ebook.STATUS_DOWNLOAD_UNKNOWN:
                case Ebook.STATUS_DOWNLOAD_FAIL:
                    btnStatus.setText("下载失败");
                    btnStatus.setEnabled(false);
                    btnStatus.setBackgroundResource(R.drawable.default_btn_error_bg);
                    break;

                default:
                    btnStatus.setText("下载");
                    btnStatus.setBackgroundResource(R.drawable.default_btn_normal_bg);
                    break;
            }
            return convertView;
        }

    }

    @Override
    public boolean refresh(int taskTypeId, Map<String, Object> params) {
        super.refresh(taskTypeId, params);
        switch (taskTypeId) {
            case TaskType.GET_EBOOKS:
                progressBar.setVisibility(View.GONE);
                tvLoadMore.setText("点击加载更多");

                ApiResponseResult responseResult = (ApiResponseResult) params.get(MainService.MSG_PARAM_API_RESP);
                if (responseResult != null && responseResult.getResult() == 0
                        && responseResult.getData() != null && responseResult.getData().containsKey(ApiConst.PARAM_EBOOKS)) {
                    String jsonEbooks = gson.toJson(responseResult.getData().get(ApiConst.PARAM_EBOOKS));
                    if (jsonEbooks != null) {
                        List<Ebook> ebookList = gson.fromJson(jsonEbooks, new TypeToken<List<Ebook>>() {}.getType());
                        if (ebookList != null) {
                            for (Ebook ebook : ebookList) {
                                if (ebook != null && ebook.id > 0 && ebook.downloadUrl != null
                                        && ebook.extraAttr != null && ebook.extraAttr.length() > 2) {

                                    // 从 media attribute 中解析出当前电子书的分类
                                    Map<String, Object> mediaAttr = gson.fromJson(ebook.extraAttr,
                                            new TypeToken<Map<String, Object>>() {}.getType());
                                    if (mediaAttr != null && mediaAttr.get("catid") != null) {
                                        ebook.catId = ((Double) mediaAttr.get("catid")).intValue();
                                        downloadEbooks.add(ebook);
                                        String temp = ebook.id + "-" + ebook.catId + "-" + ebook.name + "-" + ebook.author + ".pdf" + ".tmp";
                                        File file = new File(temp);
                                        if (file.exists()) {
                                            ebook.status = Ebook.STATUS_DOWNLOADING;
                                        } else if (EbookMgr.getInstance().getEbookWithEbookId(ebook.id) != null) {
                                            ebook.status = Ebook.STATUS_DOWNLOAD_FINISH;
                                        } else {
                                            ebook.status = Ebook.STATUS_INIT;
                                        }
                                    }
                                }
                            }

                            if (ebookList.size() < DEFAULT_LOAD_EBOOK_CNT) {
                                lvDownload.removeFooterView(viewFooter);
                            } else {
                                viewFooter.setVisibility(View.VISIBLE);
                            }

                            layoutLoading.setVisibility(View.GONE);
                            downloadAdapter.notifyDataSetChanged();
                        }
                    }
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadReceiver);
    }
}
