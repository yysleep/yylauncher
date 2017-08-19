package com.yanhuahealth.healthlauncher.ui.music;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.voicechannel.MusicLoader;
import com.yanhuahealth.healthlauncher.service.MusicPlayerService;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.utils.musicutils.CustomAudioIcon;
import com.yanhuahealth.healthlauncher.utils.musicutils.FormatHelper;

import java.util.List;

public class DetailActivity extends Activity implements OnClickListener {

    private static final String TAG = "com.example.natrue.DetailActivity";

    public static final String MUSIC_LENGTH = "com.example.nature.DetailActivity.MUSIC_LENGTH";
    public static final String CURRENT_POSITION = "com.example.nature.DetailActivity.CURRENT_POSITION";
    public static final String CURRENT_MUSIC = "com.example.nature.DetailActivity.CURRENT_MUSIC";

    private SeekBar pbDuration;
    private TextView tvTitle, tvTimeElapsed, tvDuration;
    private List<MusicLoader.MusicInfo> musicList;
    private int currentMusic;

    private int currentPosition;

    private ProgressReceiver progressReceiver;

    private MusicPlayerService.NatureBinder natureBinder;

    private int[] btnResIds = new int[]{
            R.id.music_detil_last_btn,
            R.id.music_control_startstop_btn,
            R.id.music_control_next_iv

    };

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            natureBinder = (MusicPlayerService.NatureBinder) service;
            if (natureBinder.isPlaying()) {
                CustomAudioIcon btnStartStop = (CustomAudioIcon) findViewById(R.id.music_control_startstop_btn);
                btnStartStop.setFlagStart(false);
            }

        }
    };

    private void connectToNatureService() {
        Intent intent = new Intent(DetailActivity.this, MusicPlayerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_right_in, R.anim.hold);
        MusicLoader musicLoader = MusicLoader.instance(getContentResolver());
        musicList = musicLoader.getMusicList();
        setContentView(R.layout.activity_music_detail);
        connectToNatureService();
        initComponents();
        NavBar navBar = new NavBar(this);
        navBar.setTitle("音乐播放");
        navBar.hideRight();
    }

    @Override
    public void onResume() {
        super.onResume();
        initReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(progressReceiver);
        overridePendingTransition(R.anim.hold, R.anim.push_right_out);
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (natureBinder != null) {
            unbindService(serviceConnection);
        }
    }

    private void initComponents() {
        tvTitle = (TextView) findViewById(R.id.music_detil_title_tv);
        currentMusic = getIntent().getIntExtra(CURRENT_MUSIC, 0);
        tvTitle.setText(musicList.get(currentMusic).getTitle());

        tvDuration = (TextView) findViewById(R.id.music_detil_duration_tv);
        int max = getIntent().getIntExtra(MUSIC_LENGTH, 0);
        tvDuration.setText(FormatHelper.formatDuration(max));

        pbDuration = (SeekBar) findViewById(R.id.music_detil_duration_sb);
        pbDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    natureBinder.changeProgress(progress);
                }
            }
        });
        pbDuration.setMax(max / 1000);

        currentPosition = getIntent().getIntExtra(CURRENT_POSITION, 0);
        pbDuration.setProgress(currentPosition / 1000);

        tvTimeElapsed = (TextView) findViewById(R.id.music_detil_time_elapsed_tv);
        tvTimeElapsed.setText(FormatHelper.formatDuration(currentPosition));

        for (int resId : btnResIds) {
            CustomAudioIcon icon = (CustomAudioIcon) findViewById(resId);
            icon.setOnClickListener(this);
        }
    }

    private void initReceiver() {
        progressReceiver = new ProgressReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerService.ACTION_UPDATE_PROGRESS);
        intentFilter.addAction(MusicPlayerService.ACTION_UPDATE_DURATION);
        intentFilter.addAction(MusicPlayerService.ACTION_UPDATE_CURRENT_MUSIC);
        registerReceiver(progressReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_control_startstop_btn:
                play(currentMusic, R.id.music_control_startstop_btn);
                break;
            case R.id.music_control_next_iv:
                natureBinder.toNext();
                break;
            case R.id.music_detil_last_btn:
                natureBinder.toPrevious();
                break;
            default:
                break;
        }
    }

    private void play(int currentMusic, int resId) {
        CustomAudioIcon btnStartStop = (CustomAudioIcon) findViewById(resId);
        if (btnStartStop.isStartStatus()) {
            natureBinder.stopPlay();
        } else {
            natureBinder.startPlay(currentMusic, currentPosition);
        }
    }


    class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MusicPlayerService.ACTION_UPDATE_PROGRESS.equals(action)) {
                int progress = intent.getIntExtra(MusicPlayerService.ACTION_UPDATE_PROGRESS, currentPosition);
                if (progress > 0) {
                    currentPosition = progress; // Remember the current position
                    tvTimeElapsed.setText(FormatHelper.formatDuration(progress));
                    pbDuration.setProgress(progress / 1000);
                }
            } else if (MusicPlayerService.ACTION_UPDATE_CURRENT_MUSIC.equals(action)) {
                //Retrieve the current music and get the title to show on top of the screen.
                currentMusic = intent.getIntExtra(MusicPlayerService.ACTION_UPDATE_CURRENT_MUSIC, 0);
                tvTitle.setText(musicList.get(currentMusic).getTitle());
            } else if (MusicPlayerService.ACTION_UPDATE_DURATION.equals(action)) {
                //Receive the duration and show under the progress bar
                //Why do this ? because from the ContentResolver, the duration is zero.
                int duration = intent.getIntExtra(MusicPlayerService.ACTION_UPDATE_DURATION, 0);
                tvDuration.setText(FormatHelper.formatDuration(duration));
                pbDuration.setMax(duration / 1000);
            }
        }

    }
}