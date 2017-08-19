package com.yanhuahealth.healthlauncher.ui.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.model.contact.ContactForIntent;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.contact.ContactDetailActivity;

import java.util.ArrayList;

/**
 * 电话查询列表
 */
public class CallListActivity extends YHBaseActivity {
    private ListView lvCallList;
    private ProgressBar progressBar;
    int position;
    CallListAdapter adapter;
    ArrayList<ContactForIntent> searchList;

    private BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ContactMgr.ACTION_UPDATE_CONTACT)) {
                long rawContactId = intent.getLongExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1);
                if (rawContactId <= 0) {
                    return;
                }
                Contact contact = ContactMgr.getInstance().getContactByRawContactId(rawContactId);
                if (contact == null) {
                    return;
                }
                searchList.remove(position);
                searchList.add(position, new ContactForIntent(contact.name,
                        contact.contactId, contact.rawContactId, contact.phoneOne));
                adapter.list = searchList;
                adapter.notifyDataSetChanged();

            }
        }
    };

    @Override
    protected String tag() {
        return CallListActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_list);
        lvCallList = (ListView) findViewById(R.id.call_list_lv);
        progressBar = (ProgressBar) findViewById(R.id.call_list_pb);

        NavBar navBar = new NavBar(this);
        navBar.setTitle("联系人");
        navBar.hideRight();
        if (getIntent().getSerializableExtra("searchList") != null) {
            new LoadContactsTask().execute();
        }
        lvCallList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CallListAdapter adapter = (CallListAdapter) parent.getAdapter();
                ContactForIntent contactForIntent = (ContactForIntent) adapter.getItem(position);
                if (contactForIntent == null) {
                    return;
                }
                CallListActivity.this.position = position;
                Intent intent = new Intent(CallListActivity.this, ContactDetailActivity.class);
                intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contactForIntent.contactId);
                intent.putExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, contactForIntent.rawContactId);
                startActivity(intent);
            }
        });

        // 注册更新广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ContactMgr.ACTION_UPDATE_CONTACT);
        registerReceiver(updateListReceiver, intentFilter);
    }

    class LoadContactsTask extends AsyncTask<Void, Void, ArrayList<ContactForIntent>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lvCallList.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<ContactForIntent> doInBackground(Void... params) {
            searchList = (ArrayList<ContactForIntent>) getIntent().getSerializableExtra("searchList");
            return searchList;
        }

        @Override
        protected void onPostExecute(ArrayList<ContactForIntent> list) {
            if (list != null && list.size() > 0) {
                adapter = new CallListAdapter(list);
                lvCallList.setAdapter(adapter);
                lvCallList.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    class CallListAdapter extends BaseAdapter {
        private ArrayList<ContactForIntent> list;

        public CallListAdapter(ArrayList<ContactForIntent> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(CallListActivity.this).inflate(R.layout.call_list_item, null);
            }
            ContactForIntent contactForIntent = list.get(position);
            TextView tvName = (TextView) convertView.findViewById(R.id.call_list_item_name);
//            TextView tvNumber = (TextView) convertView.findViewById(R.id.call_list_item_number);
//            if (contact.phoneOne != null) {
//                tvNumber.setText(contact.phoneOne);
//            }
            if (contactForIntent.name != null) {
                tvName.setText(contactForIntent.name);
            }
            return convertView;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateListReceiver);
    }
}
