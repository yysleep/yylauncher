package com.yanhuahealth.healthlauncher.ui.note;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.model.note.SmsInfo;
import com.yanhuahealth.healthlauncher.model.sms.UserSmsRecord;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 点击短信收件箱进入到固定联系人的所有信息展示
 */
public class FixContactSmsActivity extends YHBaseActivity implements View.OnClickListener {
    @Override
    protected String tag() {
        return null;
    }

    private UserSmsRecord userSmsRecord;
    private ArrayList<SmsInfo> smsList;

    private String userNumber;

//    private SmsListObserver smsListObserver;
    private  ListView listView;
    private SmsDetailAdapter adapter;

    private DialogUtil deleteOneSmsDialog;
    private SmsInfo smsWillBeDelete;

    SmsHandle smsHandle = new SmsHandle(this);

    // 必须从0开始,否则刷新会崩溃
    private static final int SMS_RECCEIVE_TYPE = 0;
    private static final int SMS_SEND_TYPE = 1;
    private static final int RETURN_SMS_RESULT = 0x81;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_sms);
        initView();

    }

    public void initView() {
        if (getIntent() == null) {
            return;
        }
        userNumber = getIntent().getStringExtra(LauncherConst.INTENT_PARAM_PHONE_NUMBER);
        if (userNumber == null) {
            finish();
            return;
        }

        userSmsRecord = SmsMgr.getInstance().getSmsByNumber(userNumber);
        smsList = userSmsRecord.smsList;
        NavBar navBar = new NavBar(this);
        navBar.hideRight();
        navBar.setTitle(userSmsRecord.userName);

        ImageView callSmsContactIv = (ImageView) findViewById(R.id.call_sms_contact_iv);
        callSmsContactIv.setOnClickListener(this);
        Button returnSmsBtn = (Button) findViewById(R.id.return_sms_btn);
        returnSmsBtn.setOnClickListener(this);
        listView = (ListView) findViewById(R.id.contacts_all_sms);

        // 注册观察者
        Uri uri = Uri.parse("content://sms");
        final SmsListObserver smsListObserver = new SmsListObserver(null);
        getContentResolver().registerContentObserver(uri, true, smsListObserver);

        // listView的分割线
        adapter = new SmsDetailAdapter();

        listView.setAdapter(adapter);

        // position的最后一项
        listView.setSelection(adapter.getCount() - 1);

        // 当listView条目增加时,自动显示最后一条(必须在当前list页面)
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        // listView的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SmsInfo smsInfo = (SmsInfo)adapter.getItem(position);
                if (smsInfo == null) {
                    return;
                }
                smsWillBeDelete = smsInfo;
                deleteOneSmsDialog.showFirstStyleDialog("删除", "是否删除此条短信", "确认删除");
            }
        });

        deleteOneSmsDialog = new DialogUtil(FixContactSmsActivity.this, new DialogUtil.OnDialogUtilListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_btn_one_tv:
                        SmsMgr.getInstance().deleteSms(FixContactSmsActivity.this, smsWillBeDelete);
                        adapter.notifyDataSetChanged();

                        // 如果是与找个联系人的最后一条信息就返回到SmsListActivity
                        if (smsList.size() <= 0) {
                            Intent intent = new Intent(FixContactSmsActivity.this, SmsListActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        deleteOneSmsDialog.dismiss();
                        break;

                    default:
                        break;
                }
            }
        });

    }

    // 取消注册SmsListObserver
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(new SmsListObserver(new Handler()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SmsListActivity.REQ_CODE_SMS_RESULT) {
            if (resultCode > 0) {
                setResult(resultCode);
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_sms_contact_iv:
                if (userSmsRecord.userNumber.equals("")) {
                    return;
                } else {
                    callPhone(userSmsRecord.userNumber);
                }
                break;

            case R.id.return_sms_btn:
                if (userSmsRecord.userNumber.equals("")) {
                    return;
                } else {
                    sendMsg(userSmsRecord.userNumber);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 拨打电话
     */
    private void callPhone(String phone) {
        Intent intentPhone = new Intent(Intent.ACTION_CALL,
                Uri.parse("tel:" + phone));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intentPhone);
    }

    /**
     * 发送短信
     */
    private void sendMsg(String mobilePhone){
        Intent intent = new Intent(FixContactSmsActivity.this, ReturnSmsActivity.class);
        intent.putExtra("name", userSmsRecord.userName);
        intent.putExtra("phoneNumber", mobilePhone);
        startActivityForResult(intent, RETURN_SMS_RESULT);
    }

    private class SmsDetailAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return smsList == null ? 0 : smsList.size();
        }

        @Override
        public Object getItem(int position) {
            if (getCount() != 0) {
                // 倒序
                return smsList.get(getCount() - position - 1);
            }else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            SmsInfo smsInfo = smsList.get(smsList.size() - position - 1);
            if (smsInfo.type == null) {
                return -1;
            }

            switch (smsInfo.type){
                // 接收
                case LauncherConst.SMS_TYPE_RECV:
                    return SMS_RECCEIVE_TYPE;

                // 发送
                case LauncherConst.SMS_TYPE_SEND:
                    return SMS_SEND_TYPE;

                default:
                    return -1;
            }

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SmsInfo smsInfo = (SmsInfo)getItem(position);
            ReceiveViewHolder receiveViewHolder = null;
            SendViewHolder sendViewHolder = null;
            if (convertView == null) {
                switch (getItemViewType(position)) {
                    case SMS_RECCEIVE_TYPE:
                        receiveViewHolder = new ReceiveViewHolder();
                        convertView = LayoutInflater.from(FixContactSmsActivity.this).inflate(R.layout.receive_sms_item, parent, false);
                        receiveViewHolder.receive_content = (TextView) convertView.findViewById(R.id.receive_content);
                        receiveViewHolder.receive_date = (TextView) convertView.findViewById(R.id.receive_date);
                        convertView.setTag(receiveViewHolder);
                        break;

                    case SMS_SEND_TYPE:
                        sendViewHolder = new SendViewHolder();
                        convertView = LayoutInflater.from(FixContactSmsActivity.this).inflate(R.layout.send_sms_item, parent, false);
                        sendViewHolder.send_content = (TextView) convertView.findViewById(R.id.send_content);
                        sendViewHolder.send_date = (TextView) convertView.findViewById(R.id.send_date);
                        convertView.setTag(sendViewHolder);
                        break;

                    default:
                        break;
                }
            } else {
                switch (getItemViewType(position)) {
                    case SMS_RECCEIVE_TYPE:
                        receiveViewHolder = (ReceiveViewHolder) convertView.getTag();
                        break;

                    case SMS_SEND_TYPE:
                        sendViewHolder = (SendViewHolder) convertView.getTag();
                        break;

                    default:
                        break;
                }
            }

            // 赋值
            switch (getItemViewType(position)) {
                case SMS_RECCEIVE_TYPE:
                    if (receiveViewHolder != null) {
                        receiveViewHolder.receive_content.setText(smsInfo.smsbody);
                        receiveViewHolder.receive_date.setText(smsInfo.date);
                    }
                    break;
                case SMS_SEND_TYPE:
                    if (sendViewHolder != null) {
                        sendViewHolder.send_content.setText(smsInfo.smsbody);
                        sendViewHolder.send_date.setText(smsInfo.date);
                    }
                    break;

                default:
                    break;
            }
            return convertView;
        }

        // 用来优化listView，两种布局，用了两个ViewHolder
        class ReceiveViewHolder{
            TextView receive_content;
            TextView receive_date;
        }

        class SendViewHolder{
            TextView send_content;
            TextView send_date;
        }
    }

    // 自定义mServiceReceiver重写BroadcastReceiver监听短信状态信息
    public class mServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals("SMS_SEND_ACTIOIN"))
            {
                try
                {
                    switch(getResultCode())
                    {
                        case Activity.RESULT_OK:
                            /* 发送短信成功 */
                            Toast.makeText(FixContactSmsActivity.this, "短信发送成功", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
              /* 发送短信失败 */
                            Toast.makeText(FixContactSmsActivity.this, "短信发送失败", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            break;
                    }
                }
                catch(Exception e)
                {
                    e.getStackTrace();
                }
            }
            else if(intent.getAction().equals("SMS_DELIVERED_ACTION"))
            {
                try
                {
          /* android.content.BroadcastReceiver.getResultCode()方法 */
                    switch(getResultCode())
                    {
                        case Activity.RESULT_OK:/* 短信成功送达 */
                            Toast.makeText(FixContactSmsActivity.this, "短信成功送达", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:  /* 短信未送达 */
                            Toast.makeText(FixContactSmsActivity.this, "短信未送达", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            break;
                    }
                }
                catch(Exception e)
                {
                    e.getStackTrace();
                }
            }
        }
    }

    private class SmsHandle extends Handler{
        private final WeakReference<FixContactSmsActivity> sActivity;

        SmsHandle(FixContactSmsActivity sActivity) {
            this.sActivity = new WeakReference<>(sActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FixContactSmsActivity activity = sActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 100:

                        if (activity.adapter != null) {

                            activity.adapter.notifyDataSetChanged();
                            listView.setSelection(adapter.getCount() - 1);
                        }
                        break;

                    default:
                        break;

                }
            }
        }
    }

    private final class SmsListObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SmsListObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            userSmsRecord = SmsMgr.getInstance().getSmsByNumber(userNumber);
            smsList = userSmsRecord.smsList;

            Message message = new Message();
            message.what = 100;
            message.obj = smsList;
            smsHandle.sendMessage(message);

        }
    }

}
