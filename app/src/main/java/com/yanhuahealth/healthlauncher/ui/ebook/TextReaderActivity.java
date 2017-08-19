package com.yanhuahealth.healthlauncher.ui.ebook;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

/**
 * txt 格式的文本阅读器
 */
public class TextReaderActivity extends YHBaseActivity implements View.OnTouchListener {

    @Override
    protected String tag() {
        return TextReaderActivity.class.getName();
    }

    private ViewFlipper viewFlipper;

    // 左右滑动时手指按下的 X 坐标
    private float touchDownX;

    // 左右滑动时手指松开的 X 坐标
    private float touchUpX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_reader);

        viewFlipper = (ViewFlipper) findViewById(R.id.reader_flipper);
        viewFlipper.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDownX = event.getX();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            touchUpX = event.getX();
            if (touchUpX - touchDownX > 100) {
                // 显示上一屏
                viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.text_reader_push_right_in));
                viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.text_reader_push_right_out));
                viewFlipper.showPrevious();
            } else if (touchDownX - touchUpX > 100) {
                // 显示下一屏
                viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.text_reader_push_left_in));
                viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.text_reader_push_left_out));
                viewFlipper.showNext();
            }

            return true;
        }

        return false;
    }
}
