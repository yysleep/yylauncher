package com.yanhuahealth.healthlauncher.ui.ebook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.ebook.Ebook;
import com.yanhuahealth.healthlauncher.sys.EbookMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

import java.util.Iterator;


/**
 * 电子书列表页
 */
public class EbookListActivity extends YHBaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    BroadcastReceiver removeBookReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected String tag() {
        return EbookListActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_list);
        initView();
    }

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle(R.string.title_ebook);
        navBar.showRightDownload();
        ImageView navBarRight = (ImageView) findViewById(R.id.nav_right_iv);
        navBarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EbookListActivity.this, EbookDownloadActivity.class));
            }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new TabOnChangeListener());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (getPackageManager().getLaunchIntentForPackage("com.jisu.pdf") == null) {
            Intent intent = new Intent();
            intent.setAction("yhlauncher.download");
            String apk = "http://thirdsoft.oss-cn-qingdao.aliyuncs.com/jisupdf_setup_1.0.0.1.apk";
            intent.putExtra("ApkDownLoad", apk);
            intent.putExtra("ApkTitle", "电子书");
            sendBroadcast(intent);
            finish();
        }
    }

    // 电子书分类
    public static final String[] EBOOK_CATS = new String[]{
            "全部", "养生", "小说", "其他"
    };

    // 每个 TAB 页对应的 CAT 标识
    public static final int[] EBOOK_CAT_ID_OF_POS = new int[]{
            0, EbookMgr.CAT_HEALTH, EbookMgr.CAT_STORY, EbookMgr.CAT_OTHER
    };

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public EbookListFragment getItem(int position) {
            EbookListFragment fragment = new EbookListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(EbookListFragment.ARG_CAT_ID, EBOOK_CAT_ID_OF_POS[position]);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return EBOOK_CATS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position < EBOOK_CATS.length) {
                return EBOOK_CATS[position];
            }

            return null;
        }
    }

    private class TabOnChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
