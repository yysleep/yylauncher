package com.yanhuahealth.healthlauncher.ui.base;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;

/**
 * 用于各个 Activity 页面中的通用导航栏
 * <p>
 * - 对应的布局文件为 layout/navbar.xml
 * - 在使用前，其他页面布局需要在对应页面的布局文件中 include navbar 布局文件
 * - 其中 左边 图标默认为 返回箭头，表示关闭当前页面
 */
public class NavBar {

    // 该导航栏所属的页面 Activity
    private Activity activity;

    // 左侧图标(如：返回)
    private ImageView ivLeft;

    // 中间文字(如：展示标题)
    private TextView tvTitle;

    // 右侧图标(如：菜单)
    private ImageView ivRight;

    public NavBar(Activity activity) {
        this.activity = activity;

        // 左侧图标
        this.ivLeft = (ImageView) activity.findViewById(R.id.nav_left_iv);
        this.ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickLeft();
            }
        });

        // 标题
        this.tvTitle = (TextView) activity.findViewById(R.id.nav_title_tv);

        // 右侧图标
        this.ivRight = (ImageView) activity.findViewById(R.id.nav_right_iv);
        this.ivRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickRight();
            }
        });
    }

    public NavBar(Activity activity, final ClickRightListener rightListener) {
        this.activity = activity;

        // 左侧图标
        this.ivLeft = (ImageView) activity.findViewById(R.id.nav_left_iv);
        this.ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickLeft();
            }
        });

        // 标题
        this.tvTitle = (TextView) activity.findViewById(R.id.nav_title_tv);

        // 右侧图标
        this.ivRight = (ImageView) activity.findViewById(R.id.nav_right_iv);
        this.ivRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickRight(rightListener);
            }
        });
    }

    //-------- 标题 ----------

    // 获取标题对应的 view
    public TextView getTitleTextView() {
        return tvTitle;
    }

    // 设置标题
    public void setTitle(String title) {
        if (title == null) {
            return;
        }

        this.tvTitle.setText(title);
    }

    // 支持使用 R.string. 的字符串资源标识来设置
    public void setTitle(int titleResId) {
        if (titleResId <= 0) {
            return;
        }

        tvTitle.setText(titleResId);
    }

    //-------- 左侧图标 ----------

    // 显示左侧图标
    public void showLeft() {
        ivLeft.setVisibility(View.VISIBLE);
    }

    // 隐藏左侧图标
    public void hideLeft() {
        ivLeft.setVisibility(View.GONE);
    }

    // 应用可以通过实现该接口来处理点击左侧图标的事件
    public interface ClickLeftListener {
        void onClick();
    }

    // 获取左侧图标
    public ImageView getLeftImageView() {
        return ivLeft;
    }

    // 设置左边图标
    public void setLeft(int iconResId) {
        if (iconResId <= 0) {
            return;
        }

        ivLeft.setImageResource(iconResId);
    }

    // 处理左侧图标点击事件
    private void handleClickLeft() {
        if (activity instanceof ClickLeftListener) {
            ((ClickLeftListener) activity).onClick();
        } else {
            // 默认为关闭该页面
            activity.finish();
        }
    }

    //-------- 右侧图标 ----------

    // 显示右侧图标
    public void showRight() {
        ivRight.setVisibility(View.VISIBLE);
    }

    // 显示右侧的下载图标
    public void showRightDownload() {
        ivRight.setImageResource(R.drawable.ic_download);
        ivRight.setVisibility(View.VISIBLE);
    }

    // 隐藏右侧图标
    public void hideRight() {
        ivRight.setVisibility(View.GONE);
    }

    // 应用可以通过实现该接口来处理点击右侧图标的事件
    public interface ClickRightListener {
        void onClick();
    }

    // 获取右侧图标
    public ImageView getRightImageView() {
        return ivRight;
    }

    // 设置右侧图标
    public void setRight(int iconResId) {
        if (iconResId <= 0) {
            return;
        }

        ivRight.setImageResource(iconResId);
    }

    // 处理右侧图标点击事件
    private void handleClickRight() {
        if (activity instanceof ClickRightListener) {
            ((ClickLeftListener) activity).onClick();
        }
    }

    // 使用指定的时间处理器来处理右侧图标点击事件
    private void handleClickRight(ClickRightListener rightListener) {
        rightListener.onClick();
    }
}
