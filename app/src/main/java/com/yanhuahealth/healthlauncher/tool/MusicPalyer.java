package com.yanhuahealth.healthlauncher.tool;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/8.
 */
public class MusicPalyer {

    // 找到music存放的路径。
    private static final File MUSIC_PATH = Environment.getExternalStorageDirectory();
    private static final String MUSIC_PATH_All = Environment.getExternalStorageDirectory().getAbsolutePath();

    // 存放找到的所有mp3的绝对路径。
    public List<String> musicList;

    // 定义多媒体对象
    public MediaPlayer player;

    // 当前播放的歌曲在List中的下标
    public int songNum;

    // 当前播放的歌曲名
    public String songName;

    public MusicPalyer(String path) {
        musicList = new ArrayList<>();
        player = new MediaPlayer();
        File allMusic = new File(MUSIC_PATH_All + "/" + path);
        if (!allMusic.exists()) {
            allMusic.mkdir();
        }

        if (allMusic.listFiles(new MusicFilter()) != null && allMusic.listFiles(new MusicFilter()).length > 0) {
            for (File file : allMusic.listFiles(new MusicFilter())) {

                musicList.add(file.getAbsolutePath());
            }
        }
    }

    public void setPlayName(String dataSource) {
        File file = new File(dataSource);
        String name = file.getName();
        //找到最后一个.
        int index = name.lastIndexOf(".");
        songName = name.substring(0, index);
    }


    public void start() {
        try {
            // 重置多媒体
            player.reset();
            String dataSource = musicList.get(songNum);
            // 截取歌名
            setPlayName(dataSource);
            // 为多媒体对象设置播放路径
            player.setDataSource(dataSource);
            player.prepare();
            player.start();
            // setOnCompletionListener 当当前多媒体对象播放完成时发生的事件
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer arg0) {
                    next();
                }
            });
        } catch (Exception e) {
            Log.v("M", e.getMessage());
        }
    }

    public void next() {
        songNum = songNum == musicList.size() - 1 ? 0 : songNum + 1;
        start();
    }

    public void last() {
        songNum = songNum == 0 ? musicList.size() - 1 : songNum - 1;
        start();
    }

    public void pause() {
        player.pause();
    }

    public void goOn() {
        player.start();
    }

    public void stop() {
        if (player.isPlaying()) {
            player.stop();
        }
    }

    public boolean starting() {
        if (player != null && player.isPlaying()) {
            return true;
        } else {
            return false;
        }
    }


    class MusicFilter implements FilenameFilter {
        // 返回当前目录所有以.mp3结尾的文件
        public boolean accept(File dir, String name) {

            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }

    public void songNum(int position) {
        this.songNum = position;
    }

    public void destroyPlaye() {
        if (player.isPlaying()) {
            player.stop();
            player.release();
        }
        if (player != null) {
            player = null;
        }
    }

    public void setVolume() {
        player.setVolume(1.0f, 1.0f);
    }

}
