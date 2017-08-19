package com.yanhuahealth.healthlauncher.ui.player;

import android.os.Bundle;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 * Created by Administrator on 2016/3/14.
 */
public class PlayingActivity extends YHBaseActivity {
    @Override
    protected String tag() {
        return PlayingActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
    }
}
