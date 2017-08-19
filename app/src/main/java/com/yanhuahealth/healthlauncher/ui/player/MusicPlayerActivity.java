package com.yanhuahealth.healthlauncher.ui.player;

import android.content.Intent;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.sys.VoiceChannelMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.music.MusicDownloadActivity;

/**
 * 语音频道
 */
public class MusicPlayerActivity extends AppCompatActivity {

    public boolean hideSeekbar = false;

    public boolean hidePlay = false;

    private static final String[] VOICE_CHANNEL_CATS = {
            "全部", "讲座", "评书"
    };

    private static final int[] VOICE_CHANNEL_CAT_ID = {
            1, VoiceChannelMgr.CAT_CHAIR, VoiceChannelMgr.CAT_STORYTELL
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        initView();
    }

    public void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle(R.string.title_voice_channel);
        navBar.showRightDownload();
        ImageView navBarRight = (ImageView) findViewById(R.id.nav_right_iv);
        navBarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MusicPlayerActivity.this, MusicDownloadActivity.class));
            }
        });
        
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.music_player_vp);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

                AllMusicFragment fragment = new AllMusicFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(AllMusicFragment.ARG_CAT_ID, VOICE_CHANNEL_CAT_ID[position]);
                fragment.setArguments(bundle);
                return fragment;

        }

        @Override
        public int getCount() {
            return VOICE_CHANNEL_CATS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position < VOICE_CHANNEL_CATS.length) {
                return VOICE_CHANNEL_CATS[position];
            }

            return null;
        }
    }

}
