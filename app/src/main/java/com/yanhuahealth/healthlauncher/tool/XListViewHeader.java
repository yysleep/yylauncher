package com.yanhuahealth.healthlauncher.tool;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;

/**
 * 自定义LinearLayout
 */
public class XListViewHeader extends LinearLayout {
    private LinearLayout mContainer;

    // 箭头图片
    private ImageView mArrowImageView;
    private LoadView mProgressBar;
    private TextView mHintTextView;
    private int mState = STATE_NORMAL;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;

    private final int ROTATE_ANIM_DURATION = 180;

    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;
    public final static int STATE_SUCCESS = 3;

    public final static int STATE_DEFAULT = 4;

    private TextView tvHeaderTime;

    // 上次更新时间的字符串常量，用于作为SharedPreferences的键值
    private static final String UPDATED_AT = "updated_at";

    // 1分钟的毫秒值，用于判断上次更新的时间
    private static final long ONE_MINUTE = 60 * 1000;

    // 1小时的毫秒值，用于判断上次更新时间
    private static final long ONE_HOUR = 60 * ONE_MINUTE;

    // 1天的毫秒值，用于判断上次更新时间
    private static final long ONE_DAY = 60 * ONE_HOUR;

    // 1个月的毫秒值，用于判断上次更新时间
    private static final long ONE_MONTH = 60 * ONE_DAY;

    // 1年的毫秒值，用于判断上次更新时间
    public static final long ONE_YEAR = 12 * ONE_MONTH;

    // 上次更新时间的毫秒值
    private long lastUpdateTime;

    // 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
    private int mId = -1;
    private SharedPreferences preferences;

    public XListViewHeader(Context context) {
        super(context);
        initView(context);
    }

    public XListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initView(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // 初始状态下，设置下拉刷新view高度为0
        ViewGroup.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.news_list_header, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);

        // 初始化控件
        mArrowImageView = (ImageView) findViewById(R.id.iv_header_arrow);
        mHintTextView = (TextView) findViewById(R.id.news_tv_header_hint);
        mProgressBar = (LoadView) findViewById(R.id.pg_header);
        tvHeaderTime = (TextView) findViewById(R.id.news_header_time);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    public void setState(int state) {
        if (state ==  mState)
            return;
        if (state == STATE_REFRESHING) {
            // 显示进度
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            // 显示箭头
            mArrowImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }

        switch (state) {
            case STATE_NORMAL:
                mArrowImageView.setImageResource(R.drawable.ic_default_ptr_flip);
                if (mState == STATE_READY) {
                    mArrowImageView.startAnimation(mRotateDownAnim);
                }
                if (mState == STATE_REFRESHING) {
                    mArrowImageView.clearAnimation();
                }
                mHintTextView.setText(R.string.header_hint_normal);
                break;

            case STATE_READY:
                if (mState != STATE_READY) {
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(mRotateUpAnim);
                    mHintTextView.setText(R.string.header_hint_ready);
                }
                break;

            case STATE_REFRESHING:
                mHintTextView.setText(R.string.header_hint_loading);
                break;

            case STATE_SUCCESS:
                mHintTextView.setText(R.string.header_hint_success);
                mArrowImageView.setImageResource(R.drawable.ic_done_dp);

                // 获取到编辑器（实现本地存储）
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(UPDATED_AT + mId, System.currentTimeMillis());
                editor.commit();
                break;

            case STATE_DEFAULT:
                mHintTextView.setText("刷新失败");
                // 获取到编辑器（实现本地存储）
                SharedPreferences.Editor editor2 = preferences.edit();
                editor2.putLong(UPDATED_AT + mId, System.currentTimeMillis());
                editor2.commit();
                break;

            default:
                break;
        }
        mState = state;
    }

    public void setVisibleHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams) mContainer
                .getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        return mContainer.getLayoutParams().height;
    }

    /**
     * 刷新下拉头中上次更新时间的文字描述。
     */
    public void refreshUpdatedAtValue() {

        lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastUpdateTime;
        long timeIntoFormat;
        String updateAtValue;
        if (lastUpdateTime == -1) {
            updateAtValue = getResources().getString(R.string.not_updated_yet);
        } else if (timePassed < 0) {
            updateAtValue = getResources().getString(R.string.time_error);
        } else if (timePassed < ONE_MINUTE) {
            updateAtValue = getResources().getString(R.string.updated_just_now);
        } else if (timePassed < ONE_HOUR) {
            timeIntoFormat = timePassed / ONE_MINUTE;
            String value = timeIntoFormat + "分钟";
            updateAtValue = String.format(getResources().getString(R.string.updated_at),

                    value);
        } else if (timePassed < ONE_DAY) {
            timeIntoFormat = timePassed / ONE_HOUR;
            String value = timeIntoFormat + "小时";
            updateAtValue = String.format(getResources().getString(R.string.updated_at),

                    value);
        } else if (timePassed < ONE_MONTH) {
            timeIntoFormat = timePassed / ONE_DAY;
            String value = timeIntoFormat + "天";
            updateAtValue = String.format(getResources().getString(R.string.updated_at),

                    value);
        } else if (timePassed < ONE_YEAR) {
            timeIntoFormat = timePassed / ONE_MONTH;
            String value = timeIntoFormat + "个月";
            updateAtValue = String.format(getResources().getString(R.string.updated_at),

                    value);
        } else {
            timeIntoFormat = timePassed / ONE_YEAR;
            String value = timeIntoFormat + "年";
            updateAtValue = String.format(getResources().getString(R.string.updated_at),

                    value);
        }
        tvHeaderTime.setText(updateAtValue);
    }

}
