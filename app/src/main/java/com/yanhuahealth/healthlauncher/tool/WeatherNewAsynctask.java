package com.yanhuahealth.healthlauncher.tool;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.yanhuahealth.healthlauncher.model.weather.TodayWeather;
import com.yanhuahealth.healthlauncher.sys.NetMgr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 解析天气接口json 获取天气数据
 */
public class WeatherNewAsynctask extends AsyncTask<Void, Void, Object> {

    private Handler handler;

    public WeatherNewAsynctask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected Object doInBackground(Void... params) {
        NetMgr.getInstance().weaHttp(handler);
        return null;
    }

}
