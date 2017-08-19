package com.yanhuahealth.healthlauncher.ui.news;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.news.News;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 * 新闻列表页点击图片显示的页面
 */
public class NewsImageActivity extends YHBaseActivity implements GestureDetector.OnGestureListener {

    public static String PARAM_CARE_NEWS = "detailNews";

    public static void startActivity(Activity from, News news) {
        Intent intent = new Intent(from, NewsImageActivity.class);
        intent.putExtra(PARAM_CARE_NEWS, news);
        from.startActivity(intent);
    }

    private ViewFlipper viewFlipper;

    GestureDetector detector;

    private static final float FING_DISTANCE = 50;

    @Override
    protected String tag() {
        return NewsImageActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsimage_detail);
        initVar();
        initView();
        initData();
    }

    private void initVar() {
        NavBar navBar = new NavBar(NewsImageActivity.this);
        navBar.hideRight();
        navBar.setTitle("图片详情");
    }

    private void initView() {
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
        detector = new GestureDetector(this, this);
    }

    private void initData() {
        News news = (News) getIntent().getSerializableExtra(PARAM_CARE_NEWS);
        String urlAttach1 = news.getAttach1();
        String urlAttach2 = news.getAttach2();
        String urlAttach3 = news.getAttach3();
        if (urlAttach1 != null) {
            invertUrlToImage(urlAttach1);
        }

        if (urlAttach2 != null) {
            invertUrlToImage(urlAttach2);
        }

        if (urlAttach3 != null) {
            invertUrlToImage(urlAttach3);
        }

    }

    private ImageView getImageView (Bitmap drawable) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(drawable);
        return imageView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

        float x = motionEvent1.getX() - motionEvent1.getX();

        if (x - FING_DISTANCE > 0) {
            viewFlipper.showNext();
            viewFlipper.setInAnimation(this, android.R.anim.slide_out_right);

        }  else if (x - FING_DISTANCE < 0){
            viewFlipper.showPrevious();
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        }

        return true;
    }

    private void invertUrlToImage(String imageUrl) {

        ImageLoader.getInstance().loadImage(imageUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

                BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);
                viewFlipper.addView(getImageView(bitmap));
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });

    }
}
