package com.yanhuahealth.healthlauncher.ui.contact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.setting.SosSettingActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactListActivity extends YHBaseActivity {
    @Override
    protected String tag() {
        return null;
    }

    private Button newContact;
    private Button btnSearchContact;
    private ListView lvContact;
    private ProgressBar progressBar;
    private EditText etSearchContact;
    private MyAdapter myAdapter;
    private ArrayList<Contact> searchList;
    private List<Contact> contacts;
    ArrayList<Contact> contactsList;
    ArrayList<Contact> changeList;

    // 其他调用页面可以指定操作
    public static final String PARAM_OP = "op";

    // 表示选择联系人的操作
    public static final int OP_SELECT_CONTACT = 100;

    // 表示外部需要的操作
    private int needOp = 0;

    // 接受联系人数据库变化的广播接收器 根据数据库的变化来进行刷新
    BroadcastReceiver contactReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ContactMgr.ACTION_UPDATE_CONTACT)) {
                myAdapter.contactLists = ContactMgr.getInstance().getContactsInApplication(ContactListActivity.this);
                myAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        needOp = getIntent().getIntExtra(PARAM_OP, -1);
        initView();
    }

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle("联系人");
        newContact = (Button) findViewById(R.id.contact_list_new_btn);
        btnSearchContact = (Button) findViewById(R.id.contact_list_select_btn);
        lvContact = (ListView) findViewById(R.id.contact_list_item_lv);
        progressBar = (ProgressBar) findViewById(R.id.contact_list_pb);
        etSearchContact = (EditText) findViewById(R.id.contact_list_select_et);
        ImageView contactsRight = (ImageView) findViewById(R.id.nav_right_iv);
        contactsList = new ArrayList<>();
        searchList = new ArrayList<>();
        myAdapter = new MyAdapter(ContactListActivity.this, contactsList);
        lvContact.setAdapter(myAdapter);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("TestUpdateContacts");
        registerReceiver(contactReceiver, intentFilter);

        // 联系人列表项点击事件响应，跳转到联系人详情页面
        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyAdapter adapter = (MyAdapter) parent.getAdapter();
                Contact contact = (Contact) adapter.getItem(position);
                YHLog.i("tagyanhua", contact.toString());
                if (needOp == OP_SELECT_CONTACT) {
                    // 发送短信界面交互
                    Intent intent = new Intent();
                    intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contact.contactId);
                    setResult(RESULT_OK, intent);
                    finish();

                } else if (getIntent().getIntExtra(LauncherConst.INTENT_PARAM_SELECT_CONTACT, -1) == ContactMgr.ADD_CONTACT_FOR_LIST) {
                    Intent intent = getIntent();
                    intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_NAME, contact.name);
                    intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contact.contactId);
                    intent.putExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, contact.rawContactId);
                    ContactListActivity.this.setResult(ContactMgr.RESULT_CODE_CONTACT, intent);
                    finish();

                } else if (getIntent().getIntExtra(ContactMgr.SOS_CHOOSE_CONTACT, 0) != 0) {
                    // 传递姓名和ID的值给报警（SosSettingActivity）第一个界面
                    Intent intentSos = new Intent(ContactListActivity.this, SosSettingActivity.class);
                    int a = getIntent().getIntExtra(ContactMgr.SOS_CHOOSE_CONTACT, 0);
                    intentSos.putExtra("returnAddContact", a);
                    intentSos.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contact.contactId);
                    startActivity(intentSos);
                    finish();

                } else {
                    Intent intent = new Intent(ContactListActivity.this, ContactDetailActivity.class);
                    intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contact.contactId);
                    intent.putExtra((LauncherConst.INTENT_FROM_CONTACT_LIST), LauncherConst.INTENT_FROM_CONTACT_LIST);
                    intent.putExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, contact.rawContactId);
                    startActivity(intent);
                }


            }
        });

        contactsRight.setVisibility(View.GONE);
        etSearchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    myAdapter.contactLists = searchList;
                    getUser(s.toString());
                    if (searchList.size() == 0) {
                        Toast.makeText(ContactListActivity.this, "无此联系人", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    myAdapter.contactLists = contacts;
                }
                myAdapter.notifyDataSetChanged();
                lvContact.invalidate();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    // 构造适配器
    class MyAdapter extends BaseAdapter {
        private List<Contact> contactLists;
        private Context context;

        public MyAdapter(Context context, List<Contact> contactLists) {
            this.context = context;
            this.contactLists = contactLists;
        }

        public void setContacts(List<Contact> contacts) {
            this.contactLists = contacts;
        }

        @Override
        public int getCount() {
            return contactLists.size() > 0 ? contactLists.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return contactLists.size() > 0 ? contactLists.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.contacts_item, null);
                viewHolder.name = (TextView) convertView.findViewById(R.id.item_contacts_name_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Contact contact = contactLists.get(position);
            viewHolder.name.setText(contact.name);

            return convertView;
        }

        class ViewHolder {
            TextView name;
        }
    }

    class LoadContactsTask extends AsyncTask<Void, Void, List<Contact>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Contact> doInBackground(Void... params) {
            while (ContactMgr.getInstance().getContactList() == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<Contact> contacts = ContactMgr.getInstance().getContactList();
            Collections.sort(contacts, new SortByName());
            return contacts;
        }

        @Override
        protected void onPostExecute(List<Contact> newContacts) {
            if (newContacts != null && myAdapter != null) {
                contacts = newContacts;
                myAdapter.setContacts(newContacts);
                myAdapter.notifyDataSetChanged();
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    // 查询联系人列表
    public void getUser(String input) {
        searchList.clear();
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            if (contact.name != null && contact.name.contains(input)) {
                searchList.add(contact);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new LoadContactsTask().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        changeList = contactsList;
    }

    public void yanhuaOnClick(View v) {
        switch (v.getId()) {

            case R.id.contact_list_select_btn:
                btnSearchContact.setVisibility(View.GONE);
                newContact.setVisibility(View.GONE);
                etSearchContact.setVisibility(View.VISIBLE);
                etSearchContact.setFocusable(true);
                etSearchContact.setFocusableInTouchMode(true);
                etSearchContact.requestFocus();
                InputMethodManager imm = (InputMethodManager) ContactListActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;

            case R.id.contact_list_new_btn:
                // 新建按钮点击事件响应，跳转到新建联系人页面
                Intent intent = new Intent(ContactListActivity.this, NewContactActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(contactReceiver);
    }

    // 用来给arraylist进行排序
    class SortByName implements Comparator<Contact> {
        @Override
        public int compare(Contact one, Contact two) {
            if (one.pinyinName.compareTo(two.pinyinName) < 0)
                return -1;
            else if (one.pinyinName.compareTo(two.pinyinName) > 0) {
                return 1;
            }
            return 0;
        }
    }
}
