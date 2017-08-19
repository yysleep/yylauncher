package com.yanhuahealth.healthlauncher.utils;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 语音播放
 */
public class Voice {

    private static volatile Voice instance;


    public static Voice getInstance() {
        if (instance == null) {
            synchronized (Voice.class) {
                if (instance == null) {
                    instance = new Voice();

                }
            }
        }
        return instance;
    }

    public Voice() {

    }

    private SpeechSynthesizer sp;

    private Context context;
    private SynthesizerListener synthesizerListener;
    private InitListener initListener;
    SpeechError speechError;

    public Voice(Context context) {
        this.context = context;
        sp = SpeechSynthesizer.createSynthesizer(context, null);
    }


    // 单例下 创建语音合成播放器实例
    public SpeechSynthesizer createPlayer(Context pContext) {
        SpeechSynthesizer player = SpeechSynthesizer.createSynthesizer(pContext, null);
        player.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        player.setParameter(SpeechConstant.SPEED, "30");
        player.setParameter(SpeechConstant.VOLUME, "80");
        player.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        return player;
    }

    // 单例下 使用播放器播放语音
    public void play(SpeechSynthesizer player, String conentent) {
        player.startSpeaking(conentent, mSynListener);

    }

    public boolean isSpeak(SpeechSynthesizer player) {
        return player.isSpeaking();
    }

    public void pause(SpeechSynthesizer player) {
        player.pauseSpeaking();
    }

    // 单例下 使用语音播放器停止语音
    public void stop(SpeechSynthesizer player) {
        player.stopSpeaking();
    }

    // 非单例 开始播放
    public void playVoice(String content) {
        sp.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        sp.setParameter(SpeechConstant.SPEED, "30");
        sp.setParameter(SpeechConstant.VOLUME, "80");
        sp.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        sp.startSpeaking(content, mSynListener);
    }


    // 非单例 停止播放
    public void stopVoice() {
        sp.stopSpeaking();
    }


    private SynthesizerListener mSynListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }

    };

}