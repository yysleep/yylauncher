package com.yanhuahealth.healthlauncher.ui.call;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.contact.RecordEntity;
import com.yanhuahealth.healthlauncher.sys.CallMgr;
import com.yanhuahealth.healthlauncher.tool.RecordAdapter;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 通话记录列表页
 */
public class CallRecordActivity extends YHBaseActivity {

    ListView recordview;
    String deleteNum;
    DialogUtil deleteDialog;
    RecordAdapter adapter;
    ArrayList<RecordEntity> recordList;
    Uri uri = CallLog.Calls.CONTENT_URI;
    MissCallObservice missCallObservice;
    long lastTime = 0;

    CallRrcordHandler handler = new CallRrcordHandler(this);

    @Override
    protected String tag() {
        return CallRecordActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_record);

        NavBar navBar = new NavBar(this);
        navBar.setTitle("通话记录");
        navBar.hideRight();
        recordview = (ListView) findViewById(R.id.call_record_calls_lv);

        recordview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 大于一秒才可以再次点击
                if (System.currentTimeMillis() - lastTime <= 3000) {
                    return;
                }
                RecordAdapter recordAdapter = (RecordAdapter) parent.getAdapter();
                if (recordAdapter == null) {
                    return;
                }
                RecordEntity record = (RecordEntity) recordAdapter.getItem(position);

                if (record == null) {
                    return;
                }
                if (record.number != null) {
                    Intent intentPhone = new Intent(Intent.ACTION_CALL,
                            Uri.parse("tel:" + record.number));
                    if (ActivityCompat.checkSelfPermission(CallRecordActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (PhoneStatus.checkPhoneSimCard(CallRecordActivity.this)) {
                        lastTime = System.currentTimeMillis();
                        startActivity(intentPhone);
                    }
                }
            }
        });

        recordview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                RecordEntity record = (RecordEntity) adapter.getItem(position);
                if (record.number != null) {
                    deleteNum = record.number;
                    deleteDialog.showDeleStyleDialog("是否删除记录");
                }
                return true;
            }
        });

        deleteDialog = new DialogUtil(this, new DialogUtil.OnDialogUtilListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_btn_four_delete_tv:
                        delete(deleteNum);
                        adapter.list = CallMgr.getInstance().RecordCalls(CallRecordActivity.this);
                        if (adapter.list != null) {
                            adapter.notifyDataSetChanged();
                        }
                        deleteDialog.dismiss();

                        break;

                    default:
                        break;
                }
            }
        });

        missCallObservice = new MissCallObservice(null);
        getContentResolver().registerContentObserver(uri, true, missCallObservice);
        if (CallMgr.getInstance().RecordCalls(this) == null) {
            finish();
            return;
        }
        adapter = new RecordAdapter(CallMgr.getInstance().RecordCalls(this), CallRecordActivity.this);
        recordview.setAdapter(adapter);
    }

    public void dialing(View view) {
        Intent intent = new Intent(CallRecordActivity.this, CallActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        recordList = CallMgr.getInstance().CallLogCalls(CallRecordActivity.this);
        if (recordList != null && recordList.size() > 0) {
            for (RecordEntity record : recordList) {
                if (record.type == 3 && !record.callNew.equals("0") && record._id != null) {
                    changeStatus(record);
                    sendBroadcast(new Intent("yhlauncher.delete.callrecord"));
                }
            }
        }

    }

    public void delete(String deleteNum) {
        if (deleteNum == null && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER + " =?", new String[]{deleteNum});
    }

    public void changeStatus(RecordEntity recordEntity) {
        ContentValues content = new ContentValues();
//        content.put(CallLog.Calls.TYPE, recordEntity.type);
//        content.put(CallLog.Calls.NUMBER, recordEntity.number);
//        content.put(CallLog.Calls.DATE, recordEntity.lDate);
        // 1未看 0已看
        content.put(CallLog.Calls.NEW, "0");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getContentResolver().update(CallLog.Calls.CONTENT_URI, content, CallLog.Calls._ID + "=?", new String[]{recordEntity._id});
    }

    class MissCallObservice extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */

        public MissCallObservice(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Message message = new Message();
            message.obj = CallMgr.getInstance().RecordCalls(CallRecordActivity.this);
            message.what = 100;
            handler.sendMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (missCallObservice != null) {
            getContentResolver().unregisterContentObserver(missCallObservice);
        }
    }

    private static class CallRrcordHandler extends Handler {
        private final WeakReference<CallRecordActivity> cActivity;

        CallRrcordHandler(CallRecordActivity cActivity) {
            this.cActivity = new WeakReference<CallRecordActivity>(cActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CallRecordActivity activity = cActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 100:
                        if (activity.adapter != null) {
                            activity.adapter.list = (ArrayList<RecordEntity>) msg.obj;
                            activity.adapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
