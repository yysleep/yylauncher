package com.yanhuahealth.healthlauncher.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.sys.MessageInfo;
import com.yanhuahealth.healthlauncher.ui.base.MarqueeTextView;
import com.yanhuahealth.healthlauncher.model.app.AppInfo;

/*
* 公用的dialog
* */

public class DialogUtil extends Dialog implements View.OnClickListener {
    /**
     * 以下为dialog的初始化控件，包括其中的布局文件
     */

    public interface OnDialogUtilListener {
        public void onClick(View view);
    }

    private OnDialogUtilListener listener;
    private TextView tvDialogTitle;
    private ImageView ivCancel;
    private TextView tvDialogContent;
    private MarqueeTextView mtvOne;
    private MarqueeTextView mtvTwo;
    private MarqueeTextView mtvThree;
    private MarqueeTextView mtvFour;
    private ProgressBar pbApkDownLoad;
    private TextView tvDialogDownLoadPercent;
    private AppInfo appUnit;
    private ImageView imageView;
    View view;

    public DialogUtil(Context context, OnDialogUtilListener listener) {
        super(context, R.style.Theme_AudioDialog);
        this.listener = listener;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.dialog_launcher, null);

        tvDialogTitle = (TextView) view.findViewById(R.id.dialog_pic_iv);
        ivCancel = (ImageView) view.findViewById(R.id.dialog_one_iv);
        // X的点击效果 取消
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tvDialogContent = (TextView) view.findViewById(R.id.id_recorder_dialog_label);

        mtvOne = (MarqueeTextView) view.findViewById(R.id.dialog_btn_one_tv);
        mtvTwo = (MarqueeTextView) view.findViewById(R.id.dialog_btn_two_tv);
        mtvThree = (MarqueeTextView) view.findViewById(R.id.dialog_btn_three_tv);
        mtvFour = (MarqueeTextView) view.findViewById(R.id.dialog_btn_four_delete_tv);

        pbApkDownLoad = (ProgressBar) view.findViewById(R.id.dialog_progress);
        tvDialogDownLoadPercent = (TextView) view.findViewById(R.id.dialog_download_percent_tv);

        mtvOne.setOnClickListener(this);
        mtvTwo.setOnClickListener(this);
        mtvThree.setOnClickListener(this);
        mtvFour.setOnClickListener(this);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        getWindow().setGravity(Gravity.BOTTOM);
        Window win = getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);

        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        setContentView(view);
    }

    // 特殊的构造方法 只是用在AllApp界面中
    public DialogUtil(Context context, OnDialogUtilListener listener, AppInfo appUnit, ImageView imageView) {
        super(context, R.style.Theme_AudioDialog);
        this.listener = listener;
        this.appUnit = appUnit;
        this.imageView = imageView;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_launcher, null);

        tvDialogTitle = (TextView) view.findViewById(R.id.dialog_pic_iv);
        ivCancel = (ImageView) view.findViewById(R.id.dialog_one_iv);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tvDialogContent = (TextView) view.findViewById(R.id.id_recorder_dialog_label);

        mtvOne = (MarqueeTextView) view.findViewById(R.id.dialog_btn_one_tv);
        mtvTwo = (MarqueeTextView) view.findViewById(R.id.dialog_btn_two_tv);
        mtvThree = (MarqueeTextView) view.findViewById(R.id.dialog_btn_three_tv);
        mtvFour = (MarqueeTextView) view.findViewById(R.id.dialog_btn_four_delete_tv);

        mtvOne.setOnClickListener(this);
        mtvTwo.setOnClickListener(this);
        mtvThree.setOnClickListener(this);
        mtvFour.setOnClickListener(this);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        getWindow().setGravity(Gravity.BOTTOM);
        Window win = getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);

        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        setContentView(view);
    }

    // 主要用于消息对话框上的按钮的回调处理
    public interface OnDialogUtilListenerWithMessageDialog {
        void onClick(View view, AlertDialog dialog);
    }

    // 消息对话框 的属性
    public static class MessageDialogProp {

        // 左边按钮
        public String leftBtnTitle;
        public OnDialogUtilListenerWithMessageDialog leftBtnClickListener;

        // 右边按钮
        public String rightBtnTitle;
        public OnDialogUtilListenerWithMessageDialog rightBtnClickListener;

        public int iconResId;

        public MessageDialogProp(int iconResId,
                                 OnDialogUtilListenerWithMessageDialog leftBtnClickListener, String leftBtnTitle,
                                 OnDialogUtilListenerWithMessageDialog rightBtnClickListener, String rightBtnTitle) {
            this.iconResId = iconResId;
            this.leftBtnClickListener = leftBtnClickListener;
            this.leftBtnTitle = leftBtnTitle;
            this.rightBtnClickListener = rightBtnClickListener;
            this.rightBtnTitle = rightBtnTitle;
        }

        @Override
        public String toString() {
            return "MessageDialogProp{" +
                    "iconResId=" + iconResId +
                    ", leftBtnTitle='" + leftBtnTitle + '\'' +
                    ", rightBtnTitle='" + rightBtnTitle + '\'' +
                    '}';
        }
    }

    private MessageDialogProp messageDialogProp;
    private AlertDialog messageDialog;
    private Button messageDialogLeftBtn;
    private Button messageDialogRightBtn;
    private ImageView messageDialogTitleIcon;
    private TextView messageDialogTitle;
    private TextView messageDialogContent;

    // 创建 消息对话框
    public DialogUtil(Context context, MessageDialogProp prop) {
        super(context, R.style.Theme_AudioDialog);
        messageDialogProp = prop;

        View messageView = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
        messageDialogLeftBtn = (Button) messageView.findViewById(R.id.left_btn);
        messageDialogRightBtn = (Button) messageView.findViewById(R.id.right_btn);
        messageDialogTitleIcon = (ImageView) messageView.findViewById(R.id.title_icon_iv);
        messageDialogTitle = (TextView) messageView.findViewById(R.id.title_tv);
        messageDialogContent = (TextView) messageView.findViewById(R.id.content_tv);

        // 左边按钮的标题及触发事件设置
        messageDialogLeftBtn.setText(messageDialogProp.leftBtnTitle);
        if (messageDialogProp.leftBtnClickListener != null) {
            messageDialogLeftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageDialogProp.leftBtnClickListener.onClick(v, messageDialog);
                }
            });
        }

        // 右边按钮的标题及触发事件设置
        messageDialogRightBtn.setText(messageDialogProp.rightBtnTitle);
        if (messageDialogProp.rightBtnClickListener != null) {
            messageDialogRightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageDialogProp.rightBtnClickListener.onClick(v, messageDialog);
                }
            });
        }

        messageDialogTitleIcon.setImageResource(messageDialogProp.iconResId);
        messageDialog = new AlertDialog.Builder(context)
                .setView(messageView).create();
    }

    public void showMessageDialog(MessageInfo messageInfo) {
        if (messageInfo == null || messageInfo.title == null || messageInfo.content == null) {
            return;
        }

        messageDialogTitle.setText(messageInfo.title);
        messageDialogContent.setText(messageInfo.content);
        messageDialog.setCancelable(false);
        messageDialog.show();
    }

    public void showFirstStyleDialog(String title, String content, String btnone) {
        mtvTwo.setVisibility(View.GONE);
        mtvThree.setVisibility(View.GONE);
        mtvFour.setVisibility(View.GONE);
        tvDialogContent.setVisibility(View.VISIBLE);
        tvDialogTitle.setVisibility(View.VISIBLE);
        mtvOne.setVisibility(View.VISIBLE);
        tvDialogTitle.setText(title);
        tvDialogContent.setText(content);
        mtvOne.setText(btnone);
        show();
    }

    public void showSecondStyleDialog(String title, String btnone, String btntwo) {
        tvDialogContent.setVisibility(View.GONE);
        mtvThree.setVisibility(View.GONE);
        mtvFour.setVisibility(View.GONE);
        tvDialogTitle.setVisibility(View.VISIBLE);
        mtvOne.setVisibility(View.VISIBLE);
        mtvTwo.setVisibility(View.VISIBLE);
        tvDialogTitle.setText(title);
        mtvOne.setText(btnone);
        mtvTwo.setText(btntwo);
        show();
    }

    public void showThirdStyleDialog(String title, String content, String btnone, String btntwo) {
        tvDialogContent.setVisibility(View.VISIBLE);
        tvDialogTitle.setVisibility(View.VISIBLE);
        mtvOne.setVisibility(View.VISIBLE);
        mtvTwo.setVisibility(View.VISIBLE);
        mtvThree.setVisibility(View.GONE);
        mtvFour.setVisibility(View.GONE);
        tvDialogTitle.setText(title);
        tvDialogContent.setText(content);
        mtvOne.setText(btnone);
        mtvTwo.setText(btntwo);
        show();
    }

    public void showDeleStyleDialog(String title) {
        tvDialogContent.setVisibility(View.GONE);
        mtvOne.setVisibility(View.GONE);
        mtvTwo.setVisibility(View.GONE);
        mtvThree.setVisibility(View.GONE);
        mtvFour.setVisibility(View.VISIBLE);
        tvDialogTitle.setVisibility(View.VISIBLE);
        tvDialogTitle.setText(title);
        mtvFour.setText("移除");
        show();
    }

    public void showProgessDialog(String title, String btnone, String btntwo) {
        ivCancel.setVisibility(View.GONE);
        mtvThree.setVisibility(View.GONE);
        tvDialogContent.setVisibility(View.GONE);
        mtvFour.setVisibility(View.GONE);
        tvDialogTitle.setVisibility(View.VISIBLE);
        mtvOne.setVisibility(View.VISIBLE);
        mtvTwo.setVisibility(View.VISIBLE);
        tvDialogTitle.setText(title);
        mtvOne.setText(btnone);
        mtvTwo.setText(btntwo);
        show();
    }

    public void showOneButtonDialog(String btnone) {
        mtvTwo.setVisibility(View.GONE);
        mtvThree.setVisibility(View.GONE);
        mtvFour.setVisibility(View.GONE);
        tvDialogContent.setVisibility(View.GONE);
        tvDialogTitle.setVisibility(View.INVISIBLE);
        mtvOne.setVisibility(View.VISIBLE);
        mtvOne.setText(btnone);
        show();
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }

    public void updateProgress(int num) {
        mtvOne.setVisibility(View.GONE);
        tvDialogDownLoadPercent.setVisibility(View.VISIBLE);
        pbApkDownLoad.setVisibility(View.VISIBLE);
        tvDialogTitle.setText("正在下载");
        pbApkDownLoad.setProgress(num);
        tvDialogDownLoadPercent.setText(num + "%");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DownloadManagerUtils.getInstance().cancelDownload(getContext());
        return super.onKeyDown(keyCode, event);
    }
}
