package com.yanhuahealth.healthlauncher.ui.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.api.ApiConst;
import com.yanhuahealth.healthlauncher.api.ApiResponseResult;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.voicechannel.VoiceItem;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.TaskType;
import com.yanhuahealth.healthlauncher.sys.VoiceChannelMgr;
import com.yanhuahealth.healthlauncher.ui.MainActivity;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseFragment;
import com.yanhuahealth.healthlauncher.utils.MusicPlayer;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 语音频道的节目列表
 */
public class AllMusicFragment extends YHBaseFragment implements View.OnClickListener {

    @Override
    protected String tag() {
        return AllMusicFragment.class.getName() + "-" + catId;
    }

    private View parentView;
    private ListView lvAllMusic;
    private MusicPlayer musicService;
    private SeekBar skMusic;
    private ImageView ivMusicStart;
    private TextView tvMusicMsg;
    private TextView tvMusicTime;
    private View vControl;
    private boolean autoPlay;
    boolean gonPlay = false;
    private boolean hideself = false;

    // 没有语音时的提示
    private TextView tvNoItemTip;

    public static final String ARG_CAT_ID = "cat_id";

    private int catId;
    // seekBar最大值，算百分比
    int sMax;
    // 当前歌曲播放进度(秒)
    int position;
    // 最大秒数
    int max;

    AudioManager am;

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (musicService != null && musicService.starting() && ivMusicStart != null) {
                        if (((MusicPlayerActivity) getActivity()).hideSeekbar) {
                            musicService.stop();
                            if (getActivity() != null) {
                                ((MusicPlayerActivity) getActivity()).hideSeekbar = false;
                            }
                            vControl.setVisibility(View.GONE);
                        } else {
                            ivMusicStart.setImageResource(R.drawable.ic_player_play);
                            autoPlay = true;
                            if (getActivity() != null) {
                                ((MusicPlayerActivity) getActivity()).hidePlay = false;
                            }
                            hideself = true;
                            musicService.pause();
                        }
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (musicService != null && musicService.starting() && ivMusicStart != null) {
                        //短暂失去焦点，先暂停。同时将标志位置成重新获得焦点后就开始播放
                        ivMusicStart.setImageResource(R.drawable.ic_player_play);
                        autoPlay = true;
                        musicService.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (musicService != null && musicService.starting() && ivMusicStart != null) {
                        //短暂失去焦点，先暂停。同时将标志位置成重新获得焦点后就开始播放
                        ivMusicStart.setImageResource(R.drawable.ic_player_play);
                        autoPlay = true;
                        musicService.pause();
                    }
                    break;

                case AudioManager.AUDIOFOCUS_GAIN:
                    //重新获得焦点，且符合播放条件，开始播放
                    if (musicService != null && !musicService.starting() && ivMusicStart != null) {
                        ivMusicStart.setImageResource(R.drawable.ic_player_pause);
                        autoPlay = false;
                        musicService.goOn();
                    }
                    break;

                default:
                    if (vControl != null) {
                        vControl.setVisibility(View.GONE);
                        autoPlay = false;
                    }
                    break;
            }
        }
    };

    public AllMusicFragment() {
    }

    MusicHandler handler = new MusicHandler(this);


    // 加载语音列表的进度条
    private LinearLayout layoutLoading;

    // 语音列表及播放的布局
    private LinearLayout layoutContent;

    // 语音列表适配器
    private PlayerAdapter playerAdapter;

    BroadcastReceiver updateVoice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("")) {
                return;
            } else if (action.equals(MainActivity.CAT_ID_TO_FRAGMENT_VOICE_ACTION)) {
                loadVoices();
                playerAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        YHLog.d(tag(), "onCreate");
        catId = getArguments().getInt(ARG_CAT_ID);
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(updateVoice, new IntentFilter("catIdToVoiceFragment"));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        YHLog.d(tag(), "onCreateView");
        if (parentView == null) {
            parentView = inflater.inflate(R.layout.fragment_music, container, false);
        } else {
            ViewGroup parent = (ViewGroup) parentView.getParent();
            if (parent != null) {
                parent.removeView(parentView);
            }
        }

        am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        init(parentView);

        lvAllMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicService.stop();
                musicService.songNum(position);
                ivMusicStart.setImageResource(R.drawable.ic_player_pause);
                musicService.start();
                hideself = false;
                ((MusicPlayerActivity) getActivity()).hidePlay = true;
                ((MusicPlayerActivity) getActivity()).hideSeekbar = true;
                am.requestAudioFocus(mAudioFocusListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                vControl.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            if ((MusicPlayerActivity) getActivity() != null) {
                                ((MusicPlayerActivity) getActivity()).hideSeekbar = false;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        loadVoices();
        return parentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // 进入下载页 回来刷新列表
        loadVoices();
    }

    public void init(View parentView) {
        layoutLoading = (LinearLayout) parentView.findViewById(R.id.loading_layout);
        layoutContent = (LinearLayout) parentView.findViewById(R.id.content_layout);

        ImageView ivMusicLast = (ImageView) parentView.findViewById(R.id.all_music_last_iv);
        ImageView ivMusicNext = (ImageView) parentView.findViewById(R.id.all_music_next_iv);
        tvNoItemTip = (TextView) parentView.findViewById(R.id.no_item_tip_voice_tv);
        tvNoItemTip.setVisibility(View.GONE);
        skMusic = (SeekBar) parentView.findViewById(R.id.all_music_progress_sb);
        ivMusicStart = (ImageView) parentView.findViewById(R.id.all_music_play_iv);
        lvAllMusic = (ListView) parentView.findViewById(R.id.all_play_music_lv);
        tvMusicMsg = (TextView) parentView.findViewById(R.id.all_music_msg_tv);
        tvMusicTime = (TextView) parentView.findViewById(R.id.all_music_time_tv);
        vControl = parentView.findViewById(R.id.all_music_player_control_view);
        ivMusicLast.setOnClickListener(this);
        ivMusicStart.setOnClickListener(this);
        ivMusicNext.setOnClickListener(this);

        playerAdapter = new PlayerAdapter(getActivity());
        lvAllMusic.setAdapter(playerAdapter);

        new MusicThread().start();
        skMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicService.starting()) {
                    moveSeekbar(seekBar);
                } else if (autoPlay) {
                    moveSeekbar(seekBar);
                }
            }
        });

        musicService = new MusicPlayer();
    }

    // 从服务端加载指定分类下的语音列表
    private void loadVoices() {
        // 先扫描本地是否有指定分类的语音频道列表
        File dirAudio = new File(LauncherConst.getVoiceRootPath());
        File[] audioFiles = dirAudio.listFiles(new AudioFileFilter());
        musicService.removeAllVoiceToail();
        playerAdapter.voiceItems = null;
        playerAdapter.voiceItems = new ArrayList<>();
        if (audioFiles != null && audioFiles.length > 0) {
            for (File audioFile : audioFiles) {
                if (audioFile != null) {
                    String filename = audioFile.getName();
                    String[] filenameSep = filename.split("-");
                    VoiceItem voiceItem = new VoiceItem();
                    voiceItem.id = Integer.valueOf(filenameSep[0]);
                    voiceItem.catId = Integer.valueOf(filenameSep[1]);
                    voiceItem.name = filenameSep[2];
                    voiceItem.author = filenameSep[3].split("\\.")[0];
                    voiceItem.localPath = audioFile.getAbsolutePath();
                    playerAdapter.addToTail(voiceItem);
                    musicService.addVoiceToTail(voiceItem);
                }
            }
        }

        if (playerAdapter.getCount() > 0) {
            layoutLoading.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
            tvNoItemTip.setVisibility(View.GONE);
            playerAdapter.notifyDataSetChanged();
        } else {
            layoutLoading.setVisibility(View.GONE);
            tvNoItemTip.setVisibility(View.VISIBLE);
        }

    }

    // 语音文件过滤
    class AudioFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            if (!filename.endsWith(".mp3")) {
                YHLog.i(tag(), "not MP3 file of " + filename);
                return false;
            }

            String[] arrFileInfo = filename.split("-");
            if (arrFileInfo.length != 4) {
                YHLog.i(tag(), "error format of " + filename);
                return false;
            }

            try {
                int catIdOfFilename = Integer.valueOf(arrFileInfo[1]);
                if (catId > 1 && catIdOfFilename != catId) {
                    YHLog.i(tag(), "not equal cat<" + catId + "> of " + filename);
                    return false;
                }
            } catch (NumberFormatException ex) {
                YHLog.i(tag(), "error <category> format of " + filename);
                return false;
            }

            return true;
        }
    }

    @Override
    public boolean refresh(int taskTypeId, Map<String, Object> params) {
        super.refresh(taskTypeId, params);
        switch (taskTypeId) {
            case TaskType.GET_VOICES:
                layoutLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);

                ApiResponseResult responseResult = (ApiResponseResult) params.get(MainService.MSG_PARAM_API_RESP);
                if (responseResult != null && responseResult.getResult() == 0
                        && responseResult.getData() != null && responseResult.getData().containsKey(ApiConst.PARAM_VOICES)) {
                    String jsonVoices = gson.toJson(responseResult.getData().get(ApiConst.PARAM_VOICES));
                    if (jsonVoices != null) {
                        List<VoiceItem> voiceList = gson.fromJson(jsonVoices, new TypeToken<List<VoiceItem>>() {
                        }.getType());
                        if (voiceList != null) {
                            for (VoiceItem voiceItem : voiceList) {
                                if (voiceItem != null && voiceItem.id > 0
                                        && voiceItem.downloadUrl != null && voiceItem.extraAttr != null
                                        && voiceItem.extraAttr.length() > 2

                                        // 判断voice.downloadUrl的后缀是不是.mp3结尾
                                        && VoiceChannelMgr.getInstance().isSupportVoiceFormat(voiceItem.downloadUrl)) {


                                    // 从media attribute 中解析出当前音频的分类
                                    Map<String, Object> mediaAttr = gson.fromJson(voiceItem.extraAttr,
                                            new TypeToken<Map<String, Object>>() {
                                            }.getType());
                                    if (mediaAttr != null && mediaAttr.get("catid") != null) {
                                        voiceItem.catId = ((Double) mediaAttr.get("catid")).intValue();
                                        playerAdapter.addToTail(voiceItem);
                                        musicService.addVoiceToTail(voiceItem);
                                    }
                                }
                            }

                            playerAdapter.notifyDataSetChanged();
                        }
                    }
                }
                break;
        }

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.all_music_last_iv:
                if (musicService.starting()) {
                    musicService.last();
                } else if (autoPlay) {
                    musicService.stop();
                    ivMusicStart.setImageResource(R.drawable.ic_player_pause);
                    musicService.last();
                }
                break;

            case R.id.all_music_play_iv:
                if (musicService.starting()) {
                    musicService.pause();
                    ivMusicStart.setImageResource(R.drawable.ic_player_play);
                    ((MusicPlayerActivity) getActivity()).hidePlay = false;
                    hideself = true;
                    autoPlay = true;
                } else if (autoPlay) {
                    musicService.goOn();
                    am.requestAudioFocus(mAudioFocusListener,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    ivMusicStart.setImageResource(R.drawable.ic_player_pause);
                    autoPlay = false;
                }
                break;

            case R.id.all_music_next_iv:
                if (musicService.starting()) {
                    musicService.next();
                } else if (autoPlay || gonPlay) {
                    musicService.stop();
                    ivMusicStart.setImageResource(R.drawable.ic_player_pause);
                    musicService.next();
                }
                break;

            default:
                break;
        }
    }

    // 停止拖动
    public void moveSeekbar(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        // 得到该首歌曲最长秒数
        int musicMax = musicService.player.getDuration();
        int seekBarMax = seekBar.getMax();
        // 跳到该曲该秒
        musicService.player.seekTo(musicMax * progress / seekBarMax);
        tvMusicTime.setText(setPlayInfoTime(musicMax * progress / 100000, musicMax / 1000));

    }

    // 设置当前播放的信息
    private String setPlayInfoName() {
        return musicService.songName;
    }

    // 设置当前播放的时间
    private String setPlayInfoTime(int position, int max) {

        int pMinutes = 0;
        while (position >= 60) {
            pMinutes++;
            position -= 60;
        }
        String now = (pMinutes < 10 ? "0" + pMinutes : pMinutes) + ":"
                + (position < 10 ? "0" + position : position);

        int min = 0;
        while (max >= 60) {
            min++;
            max -= 60;
        }
        String musicTime = (min < 10 ? "0" + min : min) + ":"
                + (max < 10 ? "0" + max : max);

        return now + "/" + musicTime;
    }

    class MusicThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    handler.sendMessage(new Message());
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicService != null) {
            vControl.setVisibility(View.GONE);
            tvMusicMsg.setText("");
            tvMusicTime.setText("");
            musicService.destroyPlaye();
            musicService = null;
        }

        getActivity().unregisterReceiver(updateVoice);
    }

    class PlayerAdapter extends BaseAdapter {
        private Context context;
        public List<VoiceItem> voiceItems = new ArrayList<>();

        public PlayerAdapter(Context context) {
            this.context = context;
        }

        // 添加新的语音文件至头部
        // 添加成功返回 0，其他表示添加失败
        public int addToTail(VoiceItem voiceItem) {
            if (voiceItem == null) {
                return -1;
            }

            for (VoiceItem eb : voiceItems) {
                if (eb == voiceItem || (eb.id == voiceItem.id && eb.id > 0)) {
                    // already exists
                    return 1;
                }
            }

            voiceItems.add(0, voiceItem);
            return 0;
        }

        @Override
        public int getCount() {
            return voiceItems.size();
        }

        @Override
        public Object getItem(int position) {
            return voiceItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.music_play_item, parent, false);
            }

            VoiceItem voiceItem = (VoiceItem) getItem(position);
            if (voiceItem == null) {
                return convertView;
            }

            TextView tvVoiceTitle = (TextView) convertView.findViewById(R.id.voice_all_title_tv);
            if (voiceItem.name != null && voiceItem.name.length() > 0) {
                tvVoiceTitle.setText(voiceItem.name);
            }

            TextView tvVoiceAuthor = (TextView) convertView.findViewById(R.id.voice_all_author_tv);
            if (tvVoiceAuthor == null) {
                return convertView;
            }
            if (voiceItem.author != null) {
                String author = "演唱者 : " + voiceItem.author;
                tvVoiceAuthor.setText(author);
            }

            return convertView;
        }
    }

    private static class MusicHandler extends Handler {
        private final WeakReference<AllMusicFragment> aFragment;

        MusicHandler(AllMusicFragment allMusicFragment) {
            this.aFragment = new WeakReference<AllMusicFragment>(allMusicFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            AllMusicFragment fragment = aFragment.get();
            if (fragment != null) {
                if (fragment.musicService != null && fragment.musicService.starting()) {
                    try {
                        fragment.position = fragment.musicService.player.getCurrentPosition();
                        fragment.max = fragment.musicService.player.getDuration();
                        fragment.sMax = fragment.skMusic.getMax();
                        fragment.skMusic.setProgress(fragment.position * fragment.sMax / fragment.max);
                        if (fragment.playerAdapter.getCount() > 0) {
                            VoiceItem voiceItem = (VoiceItem) fragment.playerAdapter.getItem(fragment.musicService.songNum);
                            fragment.tvMusicMsg.setText(voiceItem.name);
                        }
                        fragment.tvMusicTime.setText(fragment.setPlayInfoTime(fragment.position / 1000, fragment.max / 1000));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (fragment.getActivity() != null) {
                    if (((MusicPlayerActivity) fragment.getActivity()).hidePlay && fragment.hideself) {
                        if (fragment.vControl != null) {
                            fragment.vControl.setVisibility(View.GONE);
                        }
                        if (fragment.musicService != null) {
                            fragment.musicService.stop();
                        }
                    }
                }
            }
        }
    }

}
