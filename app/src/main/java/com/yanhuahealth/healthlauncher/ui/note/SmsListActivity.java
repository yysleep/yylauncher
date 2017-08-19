package com.yanhuahealth.healthlauncher.ui.note;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.note.SmsInfo;
import com.yanhuahealth.healthlauncher.model.sms.UserSmsRecord;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.sys.EventType;
import com.yanhuahealth.healthlauncher.sys.IEventHandler;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 短信收件箱记录
 */
public class SmsListActivity extends YHBaseActivity {

    @Override
    protected String tag() {
        return SmsListActivity.class.getName();
    }

    private Button newSms;
    private ProgressBar progressBarNote;
    private TextView smsLoadTv;
    private SmsListAdapter smsListAdapter;
    private ListView lvSms;

    private DialogUtil deleteSmsDialog;

    private UserSmsRecord userSmsRecordWillBeDeleted;

    // 用于传递短信发送是否成功
    public static final int REQ_CODE_SMS_RESULT = 0x81;

    // 发送短信页面发送成功后返回
    public static final int RESULT_SEND_SMS_SUCC = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        YHLog.d(tag(), "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SMS_RESULT) {
            YHLog.d(tag(), "onActivityResult - resultCode: " + resultCode);
            if (resultCode == RESULT_SEND_SMS_SUCC) {
                new SmsListTask().execute();
            }
        }
    }

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle("短信");
        navBar.hideRight();

        newSms = (Button) findViewById(R.id.sms_list_new_btn);
        lvSms = (ListView) findViewById(R.id.sms_list_item_iv);
        progressBarNote = (ProgressBar) findViewById(R.id.sms_list_pb);
        smsLoadTv = (TextView) findViewById(R.id.sms_load_tv);
        ArrayList<UserSmsRecord> smsInfosList = new ArrayList<>();
        smsListAdapter = new SmsListAdapter(SmsListActivity.this, smsInfosList);
        lvSms.setAdapter(smsListAdapter);

        // 为listView绑定监听器
        lvSms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                smsListAdapter = (SmsListAdapter) parent.getAdapter();
                Intent intent = new Intent(SmsListActivity.this, FixContactSmsActivity.class);
                UserSmsRecord smsRecord = SmsMgr.getInstance().getAllUserSmsRecords().get(position);
                Gson gson = new Gson();
                String json = gson.toJson(smsRecord);
                intent.putExtra("UserSmsRecordStr", json);
                intent.putExtra(LauncherConst.INTENT_PARAM_PHONE_NUMBER, smsRecord.userNumber);
                intent.putExtra("UserName", ((UserSmsRecord) smsListAdapter.getItem(position)).userName);
                startActivityForResult(intent, REQ_CODE_SMS_RESULT);
            }
        });

        // listView长按删除短信
        lvSms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(smsListAdapter == null){
                    return true;
                }
                userSmsRecordWillBeDeleted = (UserSmsRecord) smsListAdapter.getItem(position);
                deleteSmsDialog.showFirstStyleDialog("删除", "是否删除此条短信", "确认删除");
                return true;
            }
        });

        // 确认删除的点击事件
        deleteSmsDialog = new DialogUtil(SmsListActivity.this, new DialogUtil.OnDialogUtilListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_btn_one_tv:

                        // 删除一组短信
                        SmsMgr.getInstance().deleteSmsSession(SmsListActivity.this, userSmsRecordWillBeDeleted);

                        // 删除掉一组短信并把listView更新
                        smsListAdapter.smsInfosList = (ArrayList<UserSmsRecord>) SmsMgr.getInstance().getAllUserSmsRecords();
                        if (smsListAdapter != null) {
                            smsListAdapter.notifyDataSetChanged();
                        }
                        deleteSmsDialog.dismiss();
                        break;

                    default:
                        break;
                }
            }
        });

        // 新建短信的点击事件
        newSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSms = new Intent(SmsListActivity.this, SendSmsActivity.class);
                startActivity(intentSms);
            }
        });
    }

    // 处理短信接收事件
    private IEventHandler smsRecvHandler = new IEventHandler() {
        @Override
        public void notify(BroadEvent event) {
            YHLog.d(tag(), "notify - " + event);
            smsListAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        MainService.getInstance().regEventHandler(EventType.SYS_SMS_RECEIVED, smsRecvHandler);
        new SmsListTask().execute();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MainService.getInstance().regEventHandler(EventType.SYS_SMS_RECEIVED, smsRecvHandler);
        new SmsListTask().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainService.getInstance().unregEventHandler(EventType.SYS_SMS_RECEIVED, smsRecvHandler);
    }

    // 构建构造器
    class SmsListAdapter extends BaseAdapter {
        private ArrayList<UserSmsRecord> smsInfosList;
        private Context context;

        public SmsListAdapter(Context context, ArrayList<UserSmsRecord> smsInfosList) {
            this.context = context;
            this.smsInfosList = smsInfosList;
        }

        public void setSmsInfosList(ArrayList<UserSmsRecord> smsRecords) {
            this.smsInfosList = smsRecords;
        }

        @Override
        public int getCount() {
            return smsInfosList.size();
        }

        @Override
        public Object getItem(int position) {
            return smsInfosList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserSmsRecord userSmsRecord = smsInfosList.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.messages_item, parent, false);
            }

            TextView phone = (TextView) convertView.findViewById(R.id.tv_sms_number);
            TextView date = (TextView) convertView.findViewById(R.id.tv_sms_date);
            TextView body = (TextView) convertView.findViewById(R.id.tv_sms_body);

            if (userSmsRecord.userName != null) {
                phone.setText(userSmsRecord.userName);
            } else {
                phone.setText(userSmsRecord.userNumber);
            }

            SmsInfo latestSmsInfo = userSmsRecord.smsList.get(0);
            date.setText(latestSmsInfo.date);
            body.setText(latestSmsInfo.smsbody);
            return convertView;
        }
    }

    class SmsListTask extends AsyncTask<Void, Void, List<UserSmsRecord>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarNote.setVisibility(View.VISIBLE);
            lvSms.setVisibility(View.GONE);
            newSms.setVisibility(View.GONE);
        }

        @Override
        protected List<UserSmsRecord> doInBackground(Void... params) {
            while (true) {
                List<UserSmsRecord> userSmsRecords = SmsMgr.getInstance().getAllUserSmsRecords();
                if (userSmsRecords == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    return userSmsRecords;
                }
            }
        }

        @Override
        protected void onPostExecute(List<UserSmsRecord> userSmsRecords) {
            super.onPostExecute(userSmsRecords);
            if(userSmsRecords != null && smsListAdapter != null) {
                smsListAdapter.setSmsInfosList((ArrayList<UserSmsRecord>) userSmsRecords);
                smsListAdapter.notifyDataSetChanged();
            }
            progressBarNote.setVisibility(View.GONE);
            smsLoadTv.setVisibility(View.GONE);
            lvSms.setVisibility(View.VISIBLE);
            newSms.setVisibility(View.VISIBLE);
        }
    }
}
