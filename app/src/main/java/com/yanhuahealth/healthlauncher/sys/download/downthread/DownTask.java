package com.yanhuahealth.healthlauncher.sys.download.downthread;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.sys.download.dbservice.DownloadManagers;
import com.yanhuahealth.healthlauncher.sys.download.downdb.ThreadDao;
import com.yanhuahealth.healthlauncher.sys.download.downdb.ThreadDaoImpl;
import com.yanhuahealth.healthlauncher.sys.download.downmodle.FileInfo;
import com.yanhuahealth.healthlauncher.sys.download.downmodle.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载任务
 */
public class DownTask {

    private Context context;
    private FileInfo fileInfo;
    private ThreadDao threadDao;
    final int APK = 1;
    final int EBOOK = 2;
    final int MUSIC = 3;
    private int finished;
    public boolean isPause = false;
    // 下载单一文件所使用的线程数量
    private int threadCount = 1;
    private List<DownloadThread> threadList;

    public DownTask(Context context, FileInfo fileInfo, int threadCount) {
        this.context = context;
        this.fileInfo = fileInfo;
        this.threadCount = threadCount;
        threadDao = new ThreadDaoImpl(context);
    }

    public void downLoad() {
        // 读取数据库的线程信息
        List<ThreadInfo> threadInfoList = threadDao.getThreads(fileInfo.getDownloadUrl());
        if (threadInfoList.size() == 0) {
            int length = fileInfo.getFileLength() / threadCount;
            for (int i = 0; i < threadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, fileInfo.getDownloadUrl(), length * i, length * (i + 1) - 1, 0);
                if (i == threadCount - 1) {
                    threadInfo.setEnd(fileInfo.getFileLength());
                }
                threadInfoList.add(threadInfo);
                // 向数据库插入线程信息
                threadDao.insertThread(threadInfo);
            }
        }
        threadList = new ArrayList<>();
        for (ThreadInfo tInfo : threadInfoList) {
            DownloadThread thread = new DownloadThread(tInfo);
            thread.start();
            threadList.add(thread);
        }
    }

    // 是否所有线程都执行完毕
    private synchronized void checkAllThreadFinished() {
        boolean allFininshed = true;
        for (DownloadThread thread : threadList) {
            if (!thread.isFinished) {
                allFininshed = false;
                break;
            }
        }
        if (allFininshed) {
            // 删除线程信息
            threadDao.deleteThread(fileInfo.getDownloadUrl());
            // 发送下载结束广播
            Intent intent = new Intent(DownloadManagers.DOWNLOAD_ACTION_FINISHED);
            intent.putExtra("fileInfo", fileInfo);
            context.sendBroadcast(intent);
        }
    }

    class DownloadThread extends Thread {
        private ThreadInfo threadInfo;
        public boolean isFinished = false;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            HttpURLConnection con = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            try {
                URL dUrl = new URL(threadInfo.getUrl());
                con = (HttpURLConnection) dUrl.openConnection();
                con.setConnectTimeout(3000);
                con.setRequestMethod("GET");
                // 设置下载位置
                int start = threadInfo.getStart() + threadInfo.getFinished();
                con.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());

                // 设置文件写入位置
                File file = new File(getPath(fileInfo.getType()), fileInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                Intent intent = new Intent(DownloadManagers.DOWNLOAD_ACTION_UPDATE);
                finished += threadInfo.getFinished();

                // 开始下载
                if (con.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    // 读取数据
                    input = con.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        // 写入文件
                        raf.write(buffer, 0, len);
                        // 整个文件的下载进度
                        finished += len;
                        // 累加每个线程完成的进度
                        threadInfo.setFinished(threadInfo.getFinished() + len);

                        if (System.currentTimeMillis() - time > 1000) {
                            intent.putExtra("finishedProgress", finished * 100 / fileInfo.getFileLength());
                            intent.putExtra("fileInfoId", fileInfo.getId());
                            context.sendBroadcast(intent);
                            time = System.currentTimeMillis();
                        }

                        // 暂停时 保存进度
                        if (isPause) {
                            threadDao.updateThread(threadInfo.getUrl(),
                                    threadInfo.getId(), threadInfo.getFinished());
                            return;
                        }
                    }

                    // 标识下载完毕
                    isFinished = true;
                    // 检查下载任务是否执行完毕
                    checkAllThreadFinished();
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                try {
                    if (raf != null) {
                        raf.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getPath(int type) {
        String path;
        switch (type) {
            case APK:
                path = LauncherConst.getApkRootPath();
                break;

            case EBOOK:
                path = LauncherConst.getEbookRootPath();
                break;

            case MUSIC:
                path = LauncherConst.getVoiceRootPath();
                break;

            default:
                path = Environment.getExternalStorageDirectory().getAbsolutePath();
                break;
        }
        return path;
    }
}
