package com.yanhuahealth.healthlauncher.utils;

import android.media.MediaPlayer;
import android.util.Log;

import com.yanhuahealth.healthlauncher.model.voicechannel.VoiceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 语音播放器
 */
public class MusicPlayer {

    public static final String TAG = MusicPlayer.class.getName();

    // 存放当前语音频道列表下的所有mp3的绝对路径
    public List<VoiceItem> musicList = new ArrayList<>();

    // 定义多媒体对象
    public MediaPlayer player = new MediaPlayer();

    // 当前播放的歌曲在List中的下标
    public int songNum;

    // 当前播放的歌曲名
    public String songName;

    public void addVoiceToTail(VoiceItem voiceItem) {
        if (voiceItem == null) {
            return;
        }

        musicList.add(voiceItem);
    }

    public void removeAllVoiceToail() {
        if (musicList != null && musicList.size() > 0) {
            musicList = null;
            musicList = new ArrayList<>();
        }
    }

    public void setPlayName(String name) {
        songName = name;
    }

    public void start() {
        try {
            // 重置多媒体
            player.reset();
            VoiceItem voiceItem = musicList.get(songNum);

            // 设置歌名
            setPlayName(voiceItem.name);

            // 为多媒体对象设置播放路径
            player.setDataSource(voiceItem.localPath);
            player.prepare();
            player.start();

            // setOnCompletionListener 当前多媒体对象播放完成时发生的事件
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer arg0) {
                    next();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "start " + e.getMessage());
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
        if (player != null && player.isPlaying()) {
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
