package com.yanhuahealth.healthlauncher.ui.call;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.model.contact.ContactForIntent;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.contact.ContactListActivity;
import com.yanhuahealth.healthlauncher.ui.contact.NewContactActivity;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/2/25.
 */
public class CallActivity extends YHBaseActivity {
    private ImageView tvDelete;
    private TextView tvNum;
    private TextView tvShowName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView tvSaveNum;
    private ImageView ivContact;
    String oldNum;
    private View allView;
    ProgressBar progressBar;
    private ArrayList<ContactForIntent> searchList = new ArrayList<>();
    private List<Contact> contacts;

    @Override
    protected String tag() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        sharedPreferences = getSharedPreferences("NUM", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("num", "");
        editor.apply();
        initView();
        allView = findViewById(R.id.call_ac_view);
        progressBar = (ProgressBar) findViewById(R.id.call_ac_pb);
        new LoadContactsTask().execute();
        tvDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editor.remove("num");
                editor.apply();
                tvNum.setText("");
                tvDelete.setVisibility(View.GONE);
                return true;
            }
        });

        tvNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    getUser(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void callOnClick(View v) {

        switch (v.getId()) {
            case R.id.call_one_tv:
                numClick("1");
                break;

            case R.id.call_two_tv:
                numClick("2");
                break;

            case R.id.call_three_tv:
                numClick("3");
                break;

            case R.id.call_four_tv:
                numClick("4");
                break;

            case R.id.call_five_tv:
                numClick("5");
                break;

            case R.id.call_six_tv:
                numClick("6");
                break;

            case R.id.call_seven_tv:
                numClick("7");
                break;

            case R.id.call_eight_tv:
                numClick("8");
                break;


            case R.id.call_nine_tv:
                numClick("9");
                break;

            case R.id.call_xing_tv:
                numClick("*");
                break;

            case R.id.call_zero_tv:
                numClick("0");
                break;

            case R.id.call_jing_tv:
                numClick("#");
                break;

            case R.id.call_num_delete_iv:
                String residualNum = sharedPreferences.getString("num", "");
                String newNum = residualNum.substring(0, residualNum.length() - 1);
                tvNum.setText(newNum);
                editor.putString("num", tvNum.getText().toString());
                editor.commit();
                if (sharedPreferences.getString("num", "").equals("")) {
                    tvDelete.setVisibility(View.GONE);
                    tvShowName.setVisibility(View.INVISIBLE);
                    tvSaveNum.setVisibility(View.GONE);
                    ivContact.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.call_save_or_tocontact_ll:
                if (!(tvNum.getText().toString().replace(" ", "").equals(""))) {
                    Intent intent = new Intent(CallActivity.this, NewContactActivity.class);
                    intent.putExtra("CallNumber", tvNum.getText().toString().replace(" ", ""));
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CallActivity.this, ContactListActivity.class);
                    startActivity(intent);
                }
                break;


            case R.id.call_num_btn:
                if (!(tvNum.getText().toString().equals(""))) {
                    Intent intentPhone = new Intent(Intent.ACTION_CALL,
                            Uri.parse("tel:" + tvNum.getText().toString()));
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    if (PhoneStatus.checkPhoneSimCard(this)) {
                        startActivity(intentPhone);
                    }
                }
                break;

            case R.id.call_show_name_tv:
                if (searchList.size() > 0) {
                    Intent intent = new Intent(CallActivity.this, CallListActivity.class);
//                    intent.putExtra("searchList", tvNum.getText().toString().trim());
                    intent.putExtra("searchList", searchList);
                    editor.remove("num");
                    editor.apply();
                    tvNum.setText("");
                    tvDelete.setVisibility(View.GONE);
                    tvShowName.setVisibility(View.INVISIBLE);
                    tvSaveNum.setVisibility(View.GONE);
                    ivContact.setVisibility(View.VISIBLE);
                    startActivity(intent);
                }
                break;

            default:
                break;

        }
    }

    public void initView() {
        tvNum = (TextView) findViewById(R.id.call_num_tv);
        tvDelete = (ImageView) findViewById(R.id.call_num_delete_iv);
        tvShowName = (TextView) findViewById(R.id.call_show_name_tv);
        tvSaveNum = (TextView) findViewById(R.id.call_save_num_tv);
        ivContact = (ImageView) findViewById(R.id.call_go_to_contact_iv);
    }

    public void numClick(String number) {
        oldNum = sharedPreferences.getString("num", "");
        tvNum.setText(oldNum + number);
        editor.putString("num", tvNum.getText().toString());
        editor.commit();
        tvDelete.setVisibility(View.VISIBLE);
        tvSaveNum.setVisibility(View.VISIBLE);
        ivContact.setVisibility(View.GONE);
    }

    class LoadContactsTask extends AsyncTask<Void, Void, List<Contact>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            allView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Contact> doInBackground(Void... params) {
            while (true) {
                List<Contact> list = ContactMgr.getInstance().getContactList();
                if (list == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    return list;
                }
            }
        }

        @Override
        protected void onPostExecute(List<Contact> list) {
            if (list != null && list.size() > 0) {
                contacts = list;
            }
            allView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

    }


    // 查询联系人列表
    public void getUser(String input) {
        if (contacts != null && contacts.size() > 0) {
            if (searchList.size() > 0) {
                searchList.clear();
            }
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).phoneOne != null && contacts.get(i).phoneOne.replace(" ", "").contains(input)) {
                    searchList.add(new ContactForIntent(contacts.get(i).name,
                            contacts.get(i).contactId, contacts.get(i).rawContactId, contacts.get(i).phoneOne));
                }
            }
            if (searchList.size() > 0) {
                tvShowName.setVisibility(View.VISIBLE);
                tvShowName.setText(searchList.get(0).name + "      " + searchList.size() + "人");
            } else {
                tvShowName.setVisibility(View.INVISIBLE);
            }
        }
    }

}
