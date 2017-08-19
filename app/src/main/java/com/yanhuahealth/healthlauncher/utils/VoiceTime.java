package com.yanhuahealth.healthlauncher.utils;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

public class VoiceTime {
    private static volatile VoiceTime instance;

    public static VoiceTime getInstance() {
        if (instance == null) {
            synchronized (VoiceTime.class) {
                if (instance == null) {
                    instance = new VoiceTime();
                }
            }
        }
        return instance;
    }

    // 播放当前时间
    public String timeVoice(Context context) {
        Time t = new Time();
        // 取得系统时间。
        t.setToNow();
        // 0-23
        int hour = t.hour;
        int min = t.minute;
        int sec = t.second;
//        Voice.getInstance().play(context).cancel();
//        Voice.getInstance().play(context).speak("现在的时间是，" + getMoment() + "。" + hour + "点。" + min + "分");
        String content = "现在时刻" + hour + "点" + " : " + min + "分，";
        return content;
    }

    // 判断上午下午
    public String getMoment() {
        Time t = new Time();
        // 取得系统时间。
        t.setToNow();
        // 0-23
        int hour = t.hour;
        int min = t.minute;
        int sec = t.second;
        String moment;
        if (hour > 11) {
            moment = "下午";
        } else {
            moment = "上午";
        }
        Log.i("tag1991", hour + "");
        return moment;
    }

    // 显示当前时间
    public String showTime() {
        Time t = new Time();
        // 取得系统时间。
        t.setToNow();
        // 0-23
        int hour = t.hour;
        int min = t.minute;
        int sec = t.second;
        String hours;
        String mins;
        String secs;
        if (hour < 10) {
            hours = "0" + hour;
        } else {
            hours = "" + hour;
        }
        if (min < 10) {
            mins = "0" + min;
        } else {
            mins = "" + min;
        }
        if (sec < 10) {
            secs = "0" + sec;
        } else {
            secs = "" + sec;
        }

        String content = hours + " : " + mins;
        return content;
    }

    // 月份
    public String showDate() {
        Time t = new Time();
        t.setToNow();
        int moun = t.month + 1;
        int mounday = t.monthDay;
        String date = moun + "月" + mounday + "日";
        return date;
    }

    // 星期
    public String showWeek() {
        Time t = new Time();
        t.setToNow();
        int week = t.weekDay;
        String weeks = "";
        switch (week) {
            case 1:
                weeks = "一";
                break;

            case 2:
                weeks = "二";
                break;

            case 3:
                weeks = "三";
                break;

            case 4:
                weeks = "四";
                break;

            case 5:
                weeks = "五";
                break;

            case 6:
                weeks = "六";
                break;

            case 7:
                weeks = "天";
                break;
        }
        String weekday = "星期" + weeks;
        return weekday;
    }

}
