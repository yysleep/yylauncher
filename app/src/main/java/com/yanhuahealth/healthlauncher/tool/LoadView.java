package com.yanhuahealth.healthlauncher.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yanhuahealth.healthlauncher.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 自定义ImageView
 */
public class LoadView extends ImageView {
    private float degress = 0f;
    private Matrix max;
    private int width;
    private int height;
    private Bitmap bitmap;
    public LoadView(Context context) {
        super(context);
        init();
    }

    public LoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            degress += 30f;
            max.setRotate(degress, width, height);
            setImageMatrix(max);
            if (degress == 360) {
                degress = 0;
            }
        }
    };

    public void init(){
        // MATRIX矩阵可以动态缩小放大图片来显示
        setScaleType(ScaleType.MATRIX);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_loading);
        setImageBitmap(bitmap);
        max = new Matrix();

        width = bitmap.getWidth() / 2;
        height = bitmap.getHeight() / 2;
        Timer time = new Timer();

        // 在指定时间执行一次
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0 , 80);
    }

}
