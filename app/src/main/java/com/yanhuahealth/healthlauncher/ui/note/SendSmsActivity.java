package com.yanhuahealth.healthlauncher.ui.note;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.model.contact.ContactForIntent;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.contact.ContactListActivity;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 发送短信界面
 */
public class SendSmsActivity extends YHBaseActivity {
    @Override
    protected String tag() {
        return SendSmsActivity.class.getName();
    }

    private EditText receiverEditText;
    private EditText smsBodyEditText;

    // 输入框下面的linearLayout
    private LinearLayout smsBodyLayout;
    private ListView searchContactLv;

    // 用于展示的ListView
    private ArrayList<ContactForIntent> searchList = new ArrayList<>();
    private List<Contact> contacts = ContactMgr.getInstance().getContactList();
    private SmsListAdapter adapter = new SmsListAdapter(searchList);

    private boolean changeContact = false;

    private String userName = null;
    private String phoneNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        initView();
    }

    public static final int REQ_CODE_GET_CONTACTS = 100;

    public void initView() {
        Button sendSmsBtn = (Button) findViewById(R.id.send_sms_btn);
        receiverEditText = (EditText) findViewById(R.id.phone_send_number_et);
        smsBodyEditText = (EditText) findViewById(R.id.sms_detail_et);

        // 初始化listView和LinearLayout
        smsBodyLayout = (LinearLayout) findViewById(R.id.send_sms_layout);
        searchContactLv = (ListView) findViewById(R.id.search_contact_lv);
        ImageView imageViewLeft = (ImageView) findViewById(R.id.nav_send_left_iv);
        ImageView imageViewRight = (ImageView) findViewById(R.id.nav_send_right_iv);


        // 联系人详情过来的号码
        phoneNumber = getIntent().getStringExtra(LauncherConst.INTENT_PARAM_PHONE_NUMBER);
        userName = getIntent().getStringExtra(LauncherConst.INTENT_PARAM_CONTACT_NAME);
        YHLog.w(tag(), "phone: " + phoneNumber + "|name: " + userName);
        if (userName != null && userName.length() > 0) {

            receiverEditText.setText(userName);
        } else if (phoneNumber != null && phoneNumber.length() > 0) {
            receiverEditText.setText(phoneNumber);
        }

        // 监听EditText的里面的变化
        receiverEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 判断是否是选择联系人传过来的号码
                // 如果是就不把号码和姓名清空
                if (!changeContact) {
//                    phoneNumber = null;
//                    userName = null;

                    // 删除直接清空EditText
//                    if (count < before) {
//                        receiverEditText.getText().clear();
//                    }

                    // 输入框内容不为0就显示listView
                    // 输入框内容为0就显示Layout
                    if (s.length() != 0) {
                        searchContactLv.setAdapter(adapter);
                        smsBodyLayout.setVisibility(View.GONE);
                        searchContactLv.setVisibility(View.VISIBLE);

                        // 输入查询联系人
                        searchUser(s.toString());

                    } else {
                        smsBodyLayout.setVisibility(View.VISIBLE);
                        searchContactLv.setVisibility(View.GONE);
                    }

                }
                changeContact = false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // listView中选择联系人的点击事件
        searchContactLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SmsListAdapter smsListAdapter = (SmsListAdapter) parent.getAdapter();
                ContactForIntent contactForIntent = (ContactForIntent) smsListAdapter.getItem(position);
                if (contactForIntent == null) {
                    return;
                }
                userName = contactForIntent.name;
                phoneNumber = contactForIntent.phoneNumber;
                if (userName != null && userName.length() > 0) {
                    receiverEditText.setText(userName);
                } else if (phoneNumber != null && phoneNumber.length() > 0) {
                    receiverEditText.setText(phoneNumber);
                }
                smsBodyLayout.setVisibility(View.VISIBLE);
                searchContactLv.setVisibility(View.GONE);
            }
        });

        // 发送短信按钮的点击事件
        sendSmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 判断手机是否有sim卡
                if (!PhoneStatus.checkPhoneSimCard(SendSmsActivity.this)) {
                    return;
                }

                String smsBody = smsBodyEditText.getText().toString().trim();
                if (smsBody.equals("")) {
                    Toast.makeText(SendSmsActivity.this, "请输入信息内容", Toast.LENGTH_LONG).show();
                    return;
                }

                // 如果联系人号码是从联系人列表传入
                if (userName == null || userName.length() == 0) {
                    phoneNumber = receiverEditText.getText().toString().trim();
                }

                if (phoneNumber == null || phoneNumber.length() == 0) {
                    Toast.makeText(SendSmsActivity.this, "请输入对方的手机号码", Toast.LENGTH_LONG).show();
                    return;
                }
//                if (phoneNumber == null) {
//                    return;
//                }
                SmsMgr.getInstance().sendSms(SendSmsActivity.this, phoneNumber, smsBody);

                // 跳转到与该联系人短信详情页面
                Intent intentToFix = new Intent(SendSmsActivity.this, FixContactSmsActivity.class);
                intentToFix.putExtra(LauncherConst.INTENT_PARAM_PHONE_NUMBER, phoneNumber);
                startActivity(intentToFix);

                receiverEditText.setText("");
                finish();
            }
        });

        // title左面图片的点击事件
        imageViewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // title右面图片的点击事件
        imageViewRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendSmsActivity.this, ContactListActivity.class);
                intent.putExtra(ContactListActivity.PARAM_OP, ContactListActivity.OP_SELECT_CONTACT);
                startActivityForResult(intent, REQ_CODE_GET_CONTACTS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_CODE_GET_CONTACTS) {
            if (resultCode == RESULT_OK && data != null) {
                // 表示选择了一个待发送的联系人
                long contactId = data.getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1);
                if (contactId != -1) {
                    // 获取联系人信息并展示
                    // 在更新联系人中重新获取联系人信息
                    Contact contact = ContactMgr.getInstance().getContactByContactId(contactId);
                    userName = contact.name;
                    phoneNumber = contact.phoneOne;

                    // 有名字就显示名字，单纯的号码就显示号码
                    if (userName != null && phoneNumber != null) {
                        changeContact = true;
                        receiverEditText.setText(userName);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 发送短信界面展示搜索出联系人的Adapter
    class SmsListAdapter extends BaseAdapter {
        private ArrayList<ContactForIntent> searchList;
        SmsListAdapter(ArrayList<ContactForIntent> searchList) {
            this.searchList = searchList;
        }
        @Override
        public int getCount() {
            return searchList.size();
        }

        @Override
        public Object getItem(int position) {
            return searchList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SendSmsActivity.this).inflate(R.layout.call_list_item, null);
            }
            ContactForIntent contactForIntent = searchList.get(position);
            TextView tvName = (TextView) convertView.findViewById(R.id.call_list_item_name);
            if (contactForIntent.name != null) {
                tvName.setText(contactForIntent.name);
            }
            return convertView;
        }
    }

    // 查询联系人列表
    private void searchUser(String input) {
        if (contacts != null && contacts.size() > 0) {
            if (searchList.size() > 0) {
                searchList.clear();
            }
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).phoneOne != null && contacts.get(i).phoneOne.replace("", "").contains(input)) {
                    searchList.add(new ContactForIntent(contacts.get(i).name,
                            contacts.get(i).contactId, contacts.get(i).rawContactId, contacts.get(i).phoneOne));
                }
            }
            if (searchList.size() > 0) {
                smsBodyLayout.setVisibility(View.GONE);
                searchContactLv.setVisibility(View.VISIBLE);
            } else {
                smsBodyLayout.setVisibility(View.VISIBLE);
                searchContactLv.setVisibility(View.VISIBLE);
            }
        }
    }

}
