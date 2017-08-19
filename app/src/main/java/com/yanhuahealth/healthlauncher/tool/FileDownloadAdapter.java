package com.yanhuahealth.healthlauncher.tool;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.sys.download.dbservice.DownloadManagers;
import com.yanhuahealth.healthlauncher.sys.download.downmodle.FileInfo;

import java.util.List;

/**
 * Created by Administrator on 2016/3/21.
 */
public class FileDownloadAdapter extends BaseAdapter {
    private Context context;
    private List<FileInfo> fileInfos;

    public FileDownloadAdapter(Context context, List<FileInfo> fileInfos) {
        this.context = context;
        this.fileInfos = fileInfos;
    }

    @Override
    public int getCount() {
        return fileInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return fileInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler hodler = null;
        final FileInfo itemInfo = fileInfos.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.download_item, null);
            hodler = new ViewHodler();
            hodler.tvTitle = (TextView) convertView.findViewById(R.id.download_item_title_tv);
            hodler.btnStart = (Button) convertView.findViewById(R.id.download_item_one_btn);
            hodler.btnStop = (Button) convertView.findViewById(R.id.download_item_two_btn);
            hodler.pbFile = (ProgressBar) convertView.findViewById(R.id.download_item_progressbar);

            hodler.tvTitle.setText(itemInfo.getName());
            hodler.pbFile.setMax(100);
            hodler.btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DownloadManagers.class);
                    intent.setAction(DownloadManagers.DOWNLOAD_ACTION__START);
                    intent.putExtra("fileInfo", itemInfo);
                    context.startService(intent);
                }
            });
            hodler.btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DownloadManagers.class);
                    intent.setAction(DownloadManagers.DOWNLOAD_ACTION_STOP);
                    intent.putExtra("fileInfo", itemInfo);
                    context.startService(intent);
                }
            });

            convertView.setTag(hodler);
        } else {
            hodler = (ViewHodler) convertView.getTag();
        }

        hodler.pbFile.setProgress(itemInfo.getFinished());
        return convertView;
    }

    // 更新每个item 的进度条
    public void updateProgress(int id,int progress){
        FileInfo fileInfo=fileInfos.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

   static class ViewHodler {
        public TextView tvTitle;
        public Button btnStart;
        public Button btnStop;
        public ProgressBar pbFile;

    }
}
