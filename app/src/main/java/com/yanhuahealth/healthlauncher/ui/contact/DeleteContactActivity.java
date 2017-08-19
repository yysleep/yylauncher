package com.yanhuahealth.healthlauncher.ui.contact;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;

import java.util.ArrayList;
import java.util.List;

public class DeleteContactActivity extends YHBaseActivity {
    @Override
    protected String tag() {
        return null;
    }

    private List<Contact> contacts;
    private DeleteAdapter deleteAdapter;
    private ArrayList<Contact> contactList;
    private ListView lvContact;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_contact);
        initView();
        new LoadContactsTask().execute();

    }

    private void initView() {
        NavBar navBar = new NavBar(this);
        navBar.setTitle("联系人");
        lvContact = (ListView) findViewById(R.id.delete_contact_lv);
        checkBox = (CheckBox) findViewById(R.id.delete_contact_cb_item);
        contactList = new ArrayList<>();
        deleteAdapter = new DeleteAdapter(DeleteContactActivity.this,contactList);
        lvContact.setAdapter(deleteAdapter);

        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeleteAdapter adapter = (DeleteAdapter) parent.getAdapter();
                Contact contact = (Contact) adapter.getItem(position);

            }
        });

    }


    // 加载联系人列表
    private List<Contact> getContacts() {
        long contactId;
        String name;


        contacts = new ArrayList<>();
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null,
                null, "sort_key asc");
        while (cursor.moveToNext()) {

            // 取得系统联系人id,姓名
            contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            int phoneCount = cursor
                    .getInt(cursor
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (phoneCount > 0) {
                contacts.add(new Contact(contactId, name));

            }
        }

        cursor.close();
        return contacts;
    }


    class DeleteAdapter extends BaseAdapter {

        private List<Contact> contactsList;
        private Context context;

        public DeleteAdapter(Context context,List<Contact> contactsList) {
            this.context = context;
            this.contactsList = contactsList;
        }

        public void setContacts(List<Contact> contacts) {
            this.contactsList = contacts;
        }
        @Override
        public int getCount() {
            return contactsList.size() > 0 ? contactsList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return contactsList.size() > 0 ? contactsList.get(position) : null;
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
                convertView = LayoutInflater.from(context).inflate(R.layout.delete_contact_item,null);
                viewHolder.name = (TextView) convertView.findViewById(R.id.delete_contact_tv_item);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.delete_contact_cb_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            Contact contact = contactsList.get(position);
            viewHolder.name.setText(contact.name);
            return convertView;
        }

        class ViewHolder {
            TextView name;
            CheckBox checkBox;
        }
    }

    class LoadContactsTask extends AsyncTask<Void, Void, List<Contact>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Contact> doInBackground(Void... params) {

            return getContacts();
        }

        @Override
        protected void onPostExecute(List<Contact> contacts) {
            if (contacts != null && deleteAdapter != null) {
                deleteAdapter.setContacts(contacts);
                deleteAdapter.notifyDataSetChanged();
            }
            //progressBar.setVisibility(View.GONE);


        }
    }

}
