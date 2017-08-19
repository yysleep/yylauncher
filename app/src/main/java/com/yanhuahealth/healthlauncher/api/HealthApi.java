package com.yanhuahealth.healthlauncher.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yanhuahealth.healthlauncher.common.YHLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 与健康云平台之间的接口封装
 */
public class HealthApi {

    private static final String TAG = HealthApi.class.getName();

    // API 服务地址
    private static String API_ADDR = null;

    // API 超时时长
    private static final int DEFAULT_CONN_TIMEOUT = 10;
    private static final int DEFAULT_READ_TIMEOUT = 10;

    /**
     * 初始化 API
     */
    public static boolean init(String serverHost, int serverPort) {
        API_ADDR = "http://" + serverHost + ":" + serverPort;
        return true;
    }

    /**
     * 统一的 json 解析器
     */
    private static Gson gson
            = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private static OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    /**
     * `
     * 版本检测
     */
    public static String URL_VER_CHECK = "/admin/app/ver/check?appId=%d&token=%s&userId=%d&osId=%s&versionCode=%s&sdkVersion=%s&siteId=%s";

    public static ApiResponseResult checkVer(ApiBaseParam abp, Map<String, Object> params) {

        if (abp == null) {
            return null;
        } else if (params == null) {
            return null;
        }

        String osId = (String) params.get(ApiConst.PARAM_OS_ID);
        String sdkVer = (String) params.get(ApiConst.PARAM_SDK_VER);
        String siteId = (String) params.get(ApiConst.PARAM_SITE_ID);
        String verCode = (String) params.get(ApiConst.PARAM_VER_CODE);

        String strUrl = String.format(API_ADDR + URL_VER_CHECK,
                abp.getAppId(), abp.getSessionToken(), abp.getUserId(),
                osId, verCode, sdkVer, siteId);
        YHLog.d(TAG, "checkVer - url: " + strUrl);
        Request requestCheckVer = new Request.Builder().url(strUrl).build();

        String responseContent = null;
        try {
            Response response = httpClient.newCall(requestCheckVer).execute();
            if (!response.isSuccessful()) {
                YHLog.w(TAG, "unexcepted execute [checkVer] - " + response);
                return null;
            }

            responseContent = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ApiResponseResult responseResult;

        try {
            responseResult = gson.fromJson(responseContent,
                    new TypeToken<ApiResponseResult>() {
                    }.getType());
        } catch (JsonSyntaxException jse) {
            YHLog.w(TAG, "checkVer - content syntax exception from url: " + strUrl);
            return null;
        }

        return responseResult;
    }

    /**
     * 获取资讯
     */
    public static final String URL_GET_NEWS = "http://114.215.237.162/203/info/?site=1&pagestart=%d&pagenum=%d";
    public static ApiResponseResult getNews(ApiBaseParam abp, Map<String, Object> params) {

        if (abp == null) {
            return null;
        } else if (params == null) {
            return null;
        }

        String strUrl = String.format(URL_GET_NEWS,
                params.get(ApiConst.PARAM_PAGE_START),
                params.get(ApiConst.PARAM_PAGE_NUM));

        Log.i("tag00048", "getNews - url: " + strUrl);
        Request requestNews = new Request.Builder().url(strUrl).build();
        String responseContent = null;
        try {
            Response response = httpClient.newCall(requestNews).execute();
            if (!response.isSuccessful()) {
                YHLog.w(TAG, "getNews - unexcepted execute [getNews] - " + response);
                return null;
            }

            responseContent = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ApiResponseResult responseResult = new ApiResponseResult();
        try {
            Map<String, Object> retMap = gson.fromJson(responseContent,
                    new TypeToken<Map<String, Object>>() {
                    }.getType());
            if (retMap != null) {
                if (retMap.containsKey("Ret")) {
                    String result = (String) retMap.get("Ret");
                    responseResult.setResult(Integer.valueOf(result));
                }

                if (retMap.containsKey("Informations")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(ApiConst.PARAM_NEWSES, retMap.get("Informations"));
                    Log.i("tag00048", data.toString() + "=======");
                    responseResult.setData(data);
                }
            }

            return responseResult;

        } catch (JsonSyntaxException jse) {
            YHLog.w(TAG, "getNews - content syntax exception from url: " + strUrl);
            return null;
        }
    }

    /**
     * 获取最新电子书列表
     */
    public static final String URL_GET_EBOOKS = "http://114.215.237.162/203/media/?site=1&pagestart=%d&pagenum=%d&mediatype=%d&catId=%d";

    public static ApiResponseResult getEbooks(ApiBaseParam abp, Map<String, Object> params) {

        if (abp == null) {
            return null;
        } else if (params == null) {
            return null;
        }

        String strUrl = String.format(URL_GET_EBOOKS,
                params.get(ApiConst.PARAM_PAGE_START),
                params.get(ApiConst.PARAM_PAGE_NUM),
                params.get(ApiConst.PARAM_MEDIA_TYPE),
                params.get(ApiConst.PARAM_CAT_ID));
        YHLog.d(TAG, "getEbooks - url: " + strUrl);
        Request requestNews = new Request.Builder().url(strUrl).build();
        String responseContent;
        try {
            Response response = httpClient.newCall(requestNews).execute();
            if (!response.isSuccessful()) {
                YHLog.w(TAG, "unexcepted execute [getEbooks] - " + response);
                return null;
            }

            responseContent = response.body().string();
            YHLog.d(TAG, "getEbooks - " + strUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ApiResponseResult responseResult = new ApiResponseResult();
        try {
            Map<String, Object> retMap = gson.fromJson(responseContent,
                    new TypeToken<Map<String, Object>>() {
                    }.getType());
            if (retMap != null) {
                if (retMap.containsKey("Ret")) {
                    String result = (String) retMap.get("Ret");
                    responseResult.setResult(Integer.valueOf(result));
                }

                if (retMap.containsKey("Medias")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(ApiConst.PARAM_EBOOKS, retMap.get("Medias"));
                    responseResult.setData(data);
                }
            }
            return responseResult;
        } catch (JsonSyntaxException jse) {
            YHLog.w(TAG, "getEbooks - content syntax exception from url: " + strUrl);
            return null;
        }
    }

    /**
     * 获取最新音频文件列表
     */
    public static final String URL_GET_VOICES = "http://114.215.237.162/203/media/?site=1&pagestart=%d&pagenum=%d&mediatype=%d&catId=%d";

    public static ApiResponseResult getVoices(ApiBaseParam abp, Map<String, Object> params) {

        if (abp == null) {
            return null;
        } else if (params == null) {
            return null;
        }

        String strUrl = String.format(URL_GET_VOICES,
                params.get(ApiConst.PARAM_PAGE_START),
                params.get(ApiConst.PARAM_PAGE_NUM),
                params.get(ApiConst.PARAM_MEDIA_TYPE),
                params.get(ApiConst.PARAM_CAT_ID));
        Log.i("tag0048", strUrl);

        Request requestNews = new Request.Builder().url(strUrl).build();
        String responseContent;
        try {
            Response response = httpClient.newCall(requestNews).execute();
            if (!response.isSuccessful()) {
                YHLog.w(TAG, "unexcepted execute [getVoices] - " + response);
                return null;
            }

            responseContent = response.body().string();
            YHLog.d(TAG, "getVoices - " + strUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ApiResponseResult responseResult = new ApiResponseResult();
        try {
            Map<String, Object> voiceMap = gson.fromJson(responseContent, new TypeToken<Map<String, Object>>() {
            }.getType());
            if (voiceMap == null) {
                return null;
            }
            if (voiceMap.containsKey("Ret")) {
                String result = (String) voiceMap.get("Ret");
                responseResult.setResult(Integer.valueOf(result));
            }

            if (voiceMap.containsKey("Medias")) {
                Map<String, Object> voiceData = new HashMap<>();
                voiceData.put(ApiConst.PARAM_VOICES, voiceMap.get("Medias"));
                responseResult.setData(voiceData);
            }
            Log.i("tag00048", "(看内容)" + responseResult);
            return responseResult;
        } catch (JsonSyntaxException jse) {
            YHLog.w(TAG, "getEbooks - content syntax exception from url: " + strUrl);
            return null;
        }
    }
}
