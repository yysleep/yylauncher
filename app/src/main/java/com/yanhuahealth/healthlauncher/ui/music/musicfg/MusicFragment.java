package com.yanhuahealth.healthlauncher.ui.music.musicfg;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseFragment;

/**
 * Created by Administrator on 2016/3/8.
 */
public class MusicFragment extends YHBaseFragment {
    @Override
    protected String tag() {
        return MusicFragment.class.getName();
    }

    private View parentView;

    public MusicFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (parentView == null) {
            parentView = inflater.inflate(R.layout.fragment_new_music, container, false);
        } else {
            ViewGroup parent = (ViewGroup) parentView.getParent();
            if (parent != null) {
                parent.removeView(parentView);
            }
        }
        return parentView;
    }


}