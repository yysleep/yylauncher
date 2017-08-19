package com.yanhuahealth.healthlauncher.tool;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.contact.RecordEntity;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/2/25.
 */
public class RecordAdapter extends BaseAdapter {
    public ArrayList<RecordEntity> list;
    private Context context;

    public RecordAdapter(ArrayList<RecordEntity> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (list != null && list.size() > 0) {
            return list.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {

        if (list != null && list.size() > 0) {
            return list.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (list == null) {
            return 0;
        }
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler hodler;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.call_record_item, parent,false);
            hodler = new ViewHodler();
            hodler.tvNum = (TextView) convertView.findViewById(R.id.call_record_num_tv);
            hodler.tvTime = (TextView) convertView.findViewById(R.id.call_record_time_tv);
            hodler.tvDate = (TextView) convertView.findViewById(R.id.call_record_date_tv);
            hodler.ivType = (ImageView) convertView.findViewById(R.id.call_type_iv);
            hodler.tvNumTime = (TextView) convertView.findViewById(R.id.call_num_time_tv);
            convertView.setTag(hodler);
        } else {
            hodler = (ViewHodler) convertView.getTag();
        }

        RecordEntity recordEntity = list.get(position);
        if (recordEntity != null) {

            if (recordEntity.name != null && !(recordEntity.name.trim().equals("")) && !recordEntity.name.equals("null")) {
                hodler.tvNum.setText(recordEntity.name);
            } else if (recordEntity.number != null && !(recordEntity.number.trim().equals(""))) {
                hodler.tvNum.setText(recordEntity.number);
            } else {
                hodler.tvNum.setText("未知号码");
            }

            if (recordEntity.lDate != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                String date = sdf.format(new Date(Long.parseLong(recordEntity.lDate + "")));
                hodler.tvDate.setText(date);

            }

            if (recordEntity.lDate != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String time = sdf.format(new Date(Long.parseLong(recordEntity.lDate + "")));
                hodler.tvTime.setText(time);
            }
            if (recordEntity.numTime != 1) {
                hodler.tvNumTime.setVisibility(View.VISIBLE);
                hodler.tvNumTime.setText(recordEntity.numTime + "次");
            } else {
                hodler.tvNumTime.setVisibility(View.GONE);
                hodler.tvNumTime.setText(recordEntity.numTime + "次");
            }

            switch (recordEntity.type) {
                case 0:
                    hodler.tvNum.setTextColor(Color.BLACK);
                    hodler.ivType.setImageResource(R.drawable.ic_call_out);
                    hodler.tvNumTime.setTextColor(Color.BLACK);
                    break;
                case 1:
                    hodler.tvNum.setTextColor(Color.BLACK);
                    hodler.tvNumTime.setTextColor(Color.BLACK);
                    hodler.ivType.setImageResource(R.drawable.ic_call_in);
                    break;
                case 2:
                    hodler.ivType.setImageResource(R.drawable.ic_call_out);
                    hodler.tvNum.setTextColor(Color.BLACK);
                    hodler.tvNumTime.setTextColor(Color.BLACK);
                    break;
                case 3:
                    hodler.tvNum.setTextColor(android.graphics.Color.RED);
                    hodler.tvNumTime.setTextColor(android.graphics.Color.RED);
                    hodler.ivType.setImageResource(R.drawable.ic_call_missed);
                    break;
                default:
                    hodler.tvNum.setTextColor(Color.BLACK);
                    hodler.ivType.setImageResource(R.drawable.ic_call_out);
                    hodler.tvNumTime.setTextColor(Color.BLACK);
                    break;
            }

            if (recordEntity.type == 3) {
                hodler.tvNum.setTextColor(android.graphics.Color.RED);
                hodler.ivType.setImageResource(R.drawable.ic_call_missed);
                ;
                hodler.tvNumTime.setTextColor(Color.RED);

            }
            if (recordEntity.type == 2) {
                hodler.ivType.setImageResource(R.drawable.ic_call_out);
                hodler.tvNum.setTextColor(Color.BLACK);
                hodler.tvNumTime.setTextColor(Color.BLACK);
            }
            if (recordEntity.type == 1) {
                hodler.tvNum.setTextColor(Color.BLACK);
                hodler.tvNumTime.setTextColor(Color.BLACK);
                hodler.ivType.setImageResource(R.drawable.ic_call_in);
            }
        }
        return convertView;
    }

    class ViewHodler {
        TextView tvNum;
        TextView tvTime;
        TextView tvDate;
        ImageView ivType;
        TextView tvNumTime;
    }
}
