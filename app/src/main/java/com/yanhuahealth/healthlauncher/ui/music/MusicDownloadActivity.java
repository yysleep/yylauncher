package com.yanhuahealth.healthlauncher.ui.music;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.yanhuahealth.healthlauncher.model.voicechannel.VoiceItem;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.TaskType;
import com.yanhuahealth.healthlauncher.sys.VoiceChannelMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 语音频道下载页面
 */
public class MusicDownloadActivity extends YHBaseActivity{
    private ArrayList<VoiceItem> downloadVoices;
    private DownloadAdapter downloadAdapter;
    private LinearLayout layoutLoading;
    private int catId = 1;

    private static final int DEFAULT_LOAD_VOICE_CNT = 10;

    private ListView lvMusicDownload;

    // 底部加载更多
    private View viewFooter;

    private TextView tvLoadMoreTip;
    private ProgressBar pbarLoadMore;


    // 广播
    BroadcastReceiver completeTaskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
               String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    String fileName = intent.getStringExtra(LauncherConst.DOWNLOAD_PATH_VOICE);
                    if (fileName != null) {
                        Toast.makeText(MusicDownloadActivity.this, fileName + "已经完成下载", Toast.LENGTH_LONG).show();
                    }

                    VoiceItem voice = VoiceChannelMgr.getInstance().getDownloadingVoiceWithDownloadId(downloadId);
                    if (voice != null) {
                        voice.status = VoiceItem.STATUS_DOWNLOAD_FINISH;
                    }
                    downloadAdapter.notifyDataSetChanged();
                }
            }
    };

    @Override
    protected String tag() {
        return MusicDownloadActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_download);
        initView();
        MainService.getInstance().getVoices(tag(), catId, 2, 0, DEFAULT_LOAD_VOICE_CNT, null);

        // 注册广播
        registerReceiver(completeTaskReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void initView() {
        NavBar navBar = new NavBar(MusicDownloadActivity.this);
        navBar.setTitle("网络语音");
        navBar.hideRight();

        layoutLoading = (LinearLayout)findViewById(R.id.loading_layout_download_voice);
        downloadVoices = new ArrayList<>();
        lvMusicDownload = (ListView)findViewById(R.id.music_download_lv);
        viewFooter = LayoutInflater.from(this).inflate(R.layout.voice_list_footer, lvMusicDownload, false);
        tvLoadMoreTip = (TextView) viewFooter.findViewById(R.id.load_more_voice_tv);
        tvLoadMoreTip.setText("点击加载更多语音");
        pbarLoadMore = (ProgressBar) viewFooter.findViewById(R.id.load_voice_progress);

        pbarLoadMore.setVisibility(View.GONE);
        lvMusicDownload.addFooterView(viewFooter);

        // 默认隐藏底部加载更多
        viewFooter.setVisibility(View.GONE);
        viewFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbarLoadMore.setVisibility(View.VISIBLE);
                tvLoadMoreTip.setText("正在加载更多语音");
                MainService.getInstance().getVoices(tag(), catId, 2,
                        downloadAdapter.getCount(), DEFAULT_LOAD_VOICE_CNT, null);
            }
        });
        downloadAdapter = new DownloadAdapter();
        lvMusicDownload.setAdapter(downloadAdapter);
    }

    // 语音频道下载管理页面的Adapter
    private class DownloadAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return downloadVoices.size();
        }

        @Override
        public Object getItem(int position) {
            return downloadVoices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MusicDownloadActivity.this).
                        inflate(R.layout.music_list_item, parent, false);
            }
            final VoiceItem voice = downloadVoices.get(position);
            if (voice == null) {
                return convertView;
            }

            // 语音名称
            TextView musicName = (TextView)convertView.findViewById(R.id.music_name_tv);
            if (musicName == null) {
                return convertView;
            }
            if (voice.name != null) {
                musicName.setText(voice.name);
            }

            // 语音作者
            TextView musicSubTitle = (TextView)convertView.findViewById(R.id.music_subtitle_tv);
            if (musicSubTitle == null) {
                return convertView;
            }
            if (voice.author != null) {
                String author = "歌手 : " + voice.author;
                musicSubTitle.setText(author);
            }

            // 语音大小
            TextView musicSize = (TextView) convertView.findViewById(R.id.music_size_tv);
                long voiceSize = voice.size;
                String size = "大小 : " + voiceSize + "MB";
                musicSize.setText(size);

            // 缩略图
            final ImageView ivVoice = (ImageView) convertView.findViewById(R.id.music_image_iv);
            ivVoice.setImageResource(R.drawable.ic_default_book_transparent);
            if (voice.thumbUrl != null) {
                ImageLoader.getInstance().loadImage(voice.thumbUrl, new ImageLoadingListener() {
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
                            VoiceChannelMgr.getInstance().saveVoiceThumb(voice, fileLocalImage);
                        }
                        if (loadedImage != null) {
                            ivVoice.setImageBitmap(loadedImage);

                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
            }

            // 右侧图标下载
            final Button btnStatus = (Button) convertView.findViewById(R.id.music_status_btn);
            if (btnStatus == null) {
                return convertView;
            }

            btnStatus.setText("下载");
            btnStatus.setVisibility(View.VISIBLE);
            btnStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (voice.downloadUrl == null) {
                        return;
                    }
                    int voiceId = voice.catId;
                    String voiceName = voice.id + "-" + voiceId + "-" + voice.name + "-" + voice.author + ".mp3";
                    File voiceFile = new File(LauncherConst.getVoiceRootPath() + voiceName);

                    // 已经下载弹出提示
                    if (voiceFile.exists()) {
                        Toast.makeText(MusicDownloadActivity.this, "此音频已经下载", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // 提示打开wifi
                    if (!PhoneStatus.getInstance().isWiFi(MusicDownloadActivity.this)) {
                        Toast.makeText(MusicDownloadActivity.this, "请打开WiFi下载", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    btnStatus.setText("下载中");
                    btnStatus.setEnabled(false);
                    btnStatus.setBackgroundResource(R.drawable.default_btn_disclick_bg);
                    VoiceChannelMgr.getInstance().downloadVoice(MusicDownloadActivity.this, voice);
                }
            });

            switch (voice.status) {
                case VoiceItem.STATUS_DOWNLOAD_FINISH:
                    btnStatus.setText("已下载");
                    btnStatus.setEnabled(false);
                    btnStatus.setBackgroundResource(R.drawable.default_btn_disable_bg);
                    break;

                case VoiceItem.STATUS_INIT:
                    btnStatus.setText("下载");
                    btnStatus.setEnabled(true);
                    btnStatus.setBackgroundResource(R.drawable.default_btn_normal_bg);
                    break;

                case VoiceItem.STATUS_DOWNLOADING:
                    btnStatus.setText("下载中");
                    btnStatus.setEnabled(false);
                    btnStatus.setBackgroundResource(R.drawable.default_btn_disclick_bg);
                    break;

                case VoiceItem.STATUS_DOWNLOAD_UNKNOWN:
                case VoiceItem.STATUS_DOWNLOAD_FAIL:
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
            case TaskType.GET_VOICES:
                pbarLoadMore.setVisibility(View.GONE);
                tvLoadMoreTip.setText("点击加载更多语音");

                ApiResponseResult responseResult = (ApiResponseResult) params.get(MainService.MSG_PARAM_API_RESP);
                if (responseResult != null && responseResult.getResult() == 0
                        && responseResult.getData() != null && responseResult.getData().containsKey(ApiConst.PARAM_VOICES)) {
                    String jsonVoices = gson.toJson(responseResult.getData().get(ApiConst.PARAM_VOICES));
                    if (jsonVoices != null) {
                        List<VoiceItem> voiceList = gson.fromJson(jsonVoices, new TypeToken<List<VoiceItem>>() {}.getType());
                        if (voiceList != null) {
                            for (VoiceItem voice : voiceList) {
                                if (voice != null && voice.id > 0
                                        && voice.downloadUrl != null && voice.extraAttr != null
                                         && voice.extraAttr.length() > 2

                                        // 判断voice.downloadUrl的后缀是不是.mp3结尾
                                        && VoiceChannelMgr.getInstance().isSupportVoiceFormat(voice.downloadUrl)) {

                                    // 从media attribute 中解析出当前音频的分类
                                    Map<String, Object> mediaAttr = gson.fromJson(voice.extraAttr,
                                            new TypeToken<Map<String, Object>>() {}.getType());
                                    if (mediaAttr != null && mediaAttr.get("catid") != null) {
                                        voice.catId = ((Double)mediaAttr.get("catid")).intValue();
                                        downloadVoices.add(voice);

                                        String voiceName = voice.id + "-" + voice.catId + "-" + voice.name + "-" + voice.author + ".mp3";
                                        File voiceFile = new File(LauncherConst.getVoiceRootPath() + voiceName);

                                        // 判断数据库有没有该数据
                                        if (voiceFile.exists()) {
                                            voice.status = VoiceItem.STATUS_DOWNLOAD_FINISH;
                                        } else {
                                            voice.status = VoiceItem.STATUS_INIT;
                                        }
                                    }
                                }
                            }

                            if (voiceList.size() < DEFAULT_LOAD_VOICE_CNT) {
                                lvMusicDownload.removeFooterView(viewFooter);
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
        // 取消注册广播
        unregisterReceiver(completeTaskReceiver);
    }
}
