package com.yanhuahealth.healthlauncher.ui.music;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.voicechannel.MusicLoader;
import com.yanhuahealth.healthlauncher.service.MusicPlayerService;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseFragment;
import com.yanhuahealth.healthlauncher.ui.music.musicfg.MusicFragment;
import com.yanhuahealth.healthlauncher.utils.musicutils.FormatHelper;

import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends YHBaseActivity implements OnClickListener {

    public static final String TAG = "com.example.nature.MAIN_ACTIVITY";
    private SeekBar pbDuration;
    private TextView tvCurrentMusic;
    private List<MusicLoader.MusicInfo> musicList;
    private int currentMusic;
    private int currentPosition;
    private int currentMax;
    private ImageView ivstartstop;
    private ProgressReceiver progressReceiver;
    private MusicPlayerService.NatureBinder natureBinder;

    private ViewPager vpMusic;
    private List<YHBaseFragment> fragmentList = new ArrayList<>();
    private TabWidget twMusic;
    private String[] names = {"全部", "讲座", "评书","其它"};
    private TextView[] tvName = new TextView[names.length];

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            natureBinder = (MusicPlayerService.NatureBinder) service;
        }
    };

    private void connectToNatureService() {
        Intent intent = new Intent(MusicActivity.this, MusicPlayerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected String tag() {
        return MusicActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        MusicLoader musicLoader = MusicLoader.instance(getContentResolver());
        musicList = musicLoader.getMusicList();
        connectToNatureService();
        initComponents();
        initView();
    }

    public void initView() {
        twMusic = (TabWidget) findViewById(R.id.music_player_tw);
        twMusic.setStripEnabled(false);

        NavBar navBar = new NavBar(this);
        navBar.setTitle("语音频道");
        navBar.hideRight();

        initTextView(0);
        initTextView(1);
        initTextView(2);
        initTextView(3);

        MusicFragment allFg = new MusicFragment();
        MusicFragment bookFg = new MusicFragment();
        MusicFragment healthFg = new MusicFragment();
        MusicFragment otherFg = new MusicFragment();
        fragmentList.add(allFg);
        fragmentList.add(bookFg);
        fragmentList.add(healthFg);
        fragmentList.add(otherFg);

        vpMusic = (ViewPager) findViewById(R.id.music_player_vp);
        PagerAdapter adapterMusic = new MusicAdapter(getSupportFragmentManager(), fragmentList);
        vpMusic.setAdapter(adapterMusic);
        vpMusic.setOnPageChangeListener(pageChangeListener);

        twMusic.setCurrentTab(0);
        tvName[0].setBackgroundColor(Color.rgb(211, 211, 211));
    }

    private OnClickListener tabClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == tvName[0]) {
                vpMusic.setCurrentItem(0);
                tvName[0].setBackgroundColor(Color.rgb(211, 211, 211));
                tvName[1].setBackgroundColor(Color.WHITE);
                tvName[2].setBackgroundColor(Color.WHITE);
                tvName[3].setBackgroundColor(Color.WHITE);
            } else if (v == tvName[1]) {
                vpMusic.setCurrentItem(1);
                tvName[0].setBackgroundColor(Color.WHITE);
                tvName[1].setBackgroundColor(Color.rgb(211, 211, 211));
                tvName[2].setBackgroundColor(Color.WHITE);
                tvName[3].setBackgroundColor(Color.WHITE);

            } else if (v == tvName[2]) {
                vpMusic.setCurrentItem(2);
                tvName[0].setBackgroundColor(Color.WHITE);
                tvName[1].setBackgroundColor(Color.WHITE);
                tvName[2].setBackgroundColor(Color.rgb(211, 211, 211));
                tvName[3].setBackgroundColor(Color.WHITE);

            }
            else if (v == tvName[3]) {
                vpMusic.setCurrentItem(3);
                tvName[0].setBackgroundColor(Color.WHITE);
                tvName[1].setBackgroundColor(Color.WHITE);
                tvName[2].setBackgroundColor(Color.WHITE);
                tvName[3].setBackgroundColor(Color.rgb(211, 211, 211));

            }
        }
    };

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            twMusic.setCurrentTab(arg0);
            choosePage(arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    private class MusicAdapter extends FragmentStatePagerAdapter {

        private List<YHBaseFragment> fragmentList;

        public MusicAdapter(FragmentManager fm, List<YHBaseFragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }
    }

    public void choosePage(int arg0) {
        switch (arg0) {

            case 0:
                tvName[0].setBackgroundColor(Color.rgb(211, 211, 211));
                tvName[1].setBackgroundColor(Color.WHITE);
                tvName[2].setBackgroundColor(Color.WHITE);
                tvName[3].setBackgroundColor(Color.WHITE);
                break;

            case 1:
                tvName[0].setBackgroundColor(Color.WHITE);
                tvName[1].setBackgroundColor(Color.rgb(211, 211, 211));
                tvName[2].setBackgroundColor(Color.WHITE);
                tvName[3].setBackgroundColor(Color.WHITE);
                break;
            case 2:
                tvName[0].setBackgroundColor(Color.WHITE);
                tvName[1].setBackgroundColor(Color.WHITE);
                tvName[2].setBackgroundColor(Color.rgb(211, 211, 211));
                tvName[3].setBackgroundColor(Color.WHITE);
                break;

            case 3:
                tvName[0].setBackgroundColor(Color.WHITE);
                tvName[1].setBackgroundColor(Color.WHITE);
                tvName[2].setBackgroundColor(Color.WHITE);
                tvName[3].setBackgroundColor(Color.rgb(211, 211, 211));

                break;

            default:
                tvName[0].setBackgroundColor(Color.WHITE);
                tvName[1].setBackgroundColor(Color.WHITE);
                tvName[2].setBackgroundColor(Color.WHITE);
                tvName[3].setBackgroundColor(Color.WHITE);
                break;
        }
    }

    public void initTextView(int agr0) {
        tvName[agr0] = new TextView(this);
        tvName[agr0].setFocusable(true);
        tvName[agr0].setText(names[agr0]);
        tvName[agr0].setTextSize(24);
        tvName[agr0].setTextColor(Color.GRAY);
        tvName[agr0].setGravity(Gravity.CENTER);
        tvName[agr0].setPadding(0, 15, 0, 15);
        twMusic.addView(tvName[agr0]);
        tvName[agr0].setOnClickListener(tabClickListener);
    }

    public void onResume() {
        super.onResume();
        registerReceiver();
        if (natureBinder != null) {
            if (natureBinder.isPlaying()) {
                ivstartstop.setBackgroundResource(R.drawable.ic_music_stop);
            } else {
                ivstartstop.setBackgroundResource(R.drawable.ic_music_play);
            }
            natureBinder.notifyActivity();
        }
    }

    public void onPause() {

        super.onPause();
        unregisterReceiver(progressReceiver);
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
        pbDuration = (SeekBar) findViewById(R.id.music_duration_sb);
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

        tvCurrentMusic = (TextView) findViewById(R.id.tvCurrentMusic);

        ivstartstop = (ImageView) findViewById(R.id.music_control_startstop_btns);
        ivstartstop.setOnClickListener(this);

        Button btnNext = (Button) findViewById(R.id.music_control_next_iv);
        btnNext.setOnClickListener(this);

        ImageView ivDetail = (ImageView) findViewById(R.id.music_turn_iv);
        ivDetail.setOnClickListener(this);

        MusicPlayerAdapter adapter = new MusicPlayerAdapter();
        ListView lvSongs = (ListView) findViewById(R.id.music_songs_lv);
        lvSongs.setAdapter(adapter);
        lvSongs.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                currentMusic = position;
                natureBinder.startPlay(currentMusic, 0);
                if (natureBinder.isPlaying()) {
                    ivstartstop.setBackgroundResource(R.drawable.ic_music_stop);
                }
            }
        });
    }

    private void registerReceiver() {
        progressReceiver = new ProgressReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerService.ACTION_UPDATE_PROGRESS);
        intentFilter.addAction(MusicPlayerService.ACTION_UPDATE_DURATION);
        intentFilter.addAction(MusicPlayerService.ACTION_UPDATE_CURRENT_MUSIC);
        registerReceiver(progressReceiver, intentFilter);
    }

    class MusicPlayerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public Object getItem(int position) {
            return musicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return musicList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MusicActivity.this).inflate(R.layout.music_item, null);
                ImageView pImageView = (ImageView) convertView.findViewById(R.id.music_item_album_photo_iv);
                TextView pTitle = (TextView) convertView.findViewById(R.id.music_item_title_tv);
                TextView pDuration = (TextView) convertView.findViewById(R.id.music_item_duration_tv);
                TextView pArtist = (TextView) convertView.findViewById(R.id.music_item_artist_tv);
                viewHolder = new ViewHolder(pImageView, pTitle, pDuration, pArtist);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.imageView.setImageResource(R.drawable.ic_launcher);
            viewHolder.title.setText(musicList.get(position).getTitle());
            viewHolder.duration.setText(FormatHelper.formatDuration(musicList.get(position).getDuration()));
            viewHolder.artist.setText(musicList.get(position).getArtist());

            return convertView;
        }
    }

    class ViewHolder {
        public ViewHolder(ImageView pImageView, TextView pTitle, TextView pDuration, TextView pArtist) {
            imageView = pImageView;
            title = pTitle;
            duration = pDuration;
            artist = pArtist;
        }

        ImageView imageView;
        TextView title;
        TextView duration;
        TextView artist;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_control_startstop_btns:
                play(currentMusic, R.id.music_control_startstop_btn);
                break;
            case R.id.music_control_next_iv:
                natureBinder.toNext();
                ivstartstop.setBackgroundResource(R.drawable.ic_music_stop);
                break;
            case R.id.music_turn_iv:
                Intent intent = new Intent(MusicActivity.this, DetailActivity.class);
                intent.putExtra(DetailActivity.MUSIC_LENGTH, currentMax);
                intent.putExtra(DetailActivity.CURRENT_MUSIC, currentMusic);
                intent.putExtra(DetailActivity.CURRENT_POSITION, currentPosition);
                startActivity(intent);
                break;
        }
    }

    private void play(int position, int resId) {
        if (natureBinder.isPlaying()) {
            natureBinder.stopPlay();
            ivstartstop.setBackgroundResource(R.drawable.ic_music_play);
        } else {
            natureBinder.startPlay(position, currentPosition);
            ivstartstop.setBackgroundResource(R.drawable.ic_music_stop);
        }
    }


    class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MusicPlayerService.ACTION_UPDATE_PROGRESS.equals(action)) {
                int progress = intent.getIntExtra(MusicPlayerService.ACTION_UPDATE_PROGRESS, 0);
                if (progress > 0) {
                    currentPosition = progress; // Remember the current position
                    pbDuration.setProgress(progress / 1000);
                }
            } else if (MusicPlayerService.ACTION_UPDATE_CURRENT_MUSIC.equals(action)) {
                //Retrive the current music and get the title to show on top of the screen.
                currentMusic = intent.getIntExtra(MusicPlayerService.ACTION_UPDATE_CURRENT_MUSIC, 0);
                tvCurrentMusic.setText(musicList.get(currentMusic).getTitle());
            } else if (MusicPlayerService.ACTION_UPDATE_DURATION.equals(action)) {
                //Receive the duration and show under the progress bar
                //Why do this ? because from the ContentResolver, the duration is zero.
                currentMax = intent.getIntExtra(MusicPlayerService.ACTION_UPDATE_DURATION, 0);
                int max = currentMax / 1000;
                pbDuration.setMax(currentMax / 1000);
            }
        }

    }

}
