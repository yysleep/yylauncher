
package com.yanhuahealth.healthlauncher.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.tool.WeatherNewAsynctask;
import com.yanhuahealth.healthlauncher.common.ShortcutConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.model.weather.TodayWeather;
import com.yanhuahealth.healthlauncher.sys.NetMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.base.ShortcutBoxView;
import com.yanhuahealth.healthlauncher.ui.call.CallRecordActivity;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;
import com.yanhuahealth.healthlauncher.utils.Voice;
import com.yanhuahealth.healthlauncher.utils.VoiceTime;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CenterFragment extends YHBaseFragment implements View.OnClickListener {
    //    private DialogUtil dialogManager;
    private TextView tvTime;
    private ImageView ivWeaPic;
    private TextView tvWea;
    private TextView tvTem;
    private ProgressBar pgTime;
    public String city;

    // LocationClient类必须在主线程中声明。需要Context类型的参数
    private LocationClient mLocationClient = null;
    private BDLocationListener mListener = new MyLocationListener();

    // 对应于 8 个 前2个是空的 shortcut box
    private List<ShortcutBoxView> shortcutBoxViewList = new ArrayList<>();

    String contentWeather;
    String contentTime;

    // 语记安装助手类
    SpeechSynthesizer player;
    private SynthesizerListener mSynListener;
    // 接收温度的数据
    private WeatherHandler handler = new WeatherHandler(this);

    // 接收时间并且显示
    private TimeHandler timeHandler = new TimeHandler(this);

    @Override
    protected String tag() {
        return CenterFragment.class.getName();
    }

    public CenterFragment() {
    }

    // 保存 parent view，其他组件使用此 find 和 加载
    private View parentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (parentView == null) {
            parentView = inflater.inflate(R.layout.fragment_center, container, false);
            initView(parentView);
        } else {
            ViewGroup parent = (ViewGroup) parentView.getParent();
            if (parent != null) {
                parent.removeView(parentView);
            }
        }

        startLocation();
        return parentView;
    }

    // 开始定位
    private void startLocation() {
        // 百度地图
        mLocationClient = new LocationClient(getActivity().getApplicationContext());
        mLocationClient.registerLocationListener(mListener);
        initLocation();
        mLocationClient.start();
    }

    @Override
    public void onStart() {
        YHLog.d(tag(), "onStart");
        super.onStart();
        reloadShortcuts();
    }

    // UI 组件初始化
    public void initView(View rootView) {
        loadShortcutBoxViews(rootView);
        player = Voice.getInstance().createPlayer(getActivity());
        mSynListener = new SynthesizerListener() {
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
                if (speechError == null) {
                    flag = true;
                } else {
                    flag = false;
                }
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        };
        LinearLayout llWeather = (LinearLayout) rootView.findViewById(R.id.page_one_weather_ll);
        llWeather.setOnClickListener(this);

        // 时间控件
        tvTime = (TextView) rootView.findViewById(R.id.page_ft_time_tv);
        pgTime = (ProgressBar) rootView.findViewById(R.id.page_ft_time_pg);
        pgTime.setVisibility(View.VISIBLE);

        // 天气控件
        ivWeaPic = (ImageView) rootView.findViewById(R.id.second_activity_wea_pic_iv);
        tvWea = (TextView) rootView.findViewById(R.id.second_activity_wea_tv);
        tvTem = (TextView) rootView.findViewById(R.id.second_activity_tem_tv);

        if (!PhoneStatus.getInstance().isWiFi(getActivity()) && !NetMgr.getInstance().isNetConnect()) {
            ivWeaPic.setImageResource(R.drawable.icon_weather_refresh);
            tvTem.setText("天气数据异常");
            tvWea.setText("点击图片刷新");
        }
        ivWeaPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvTem.getText().equals("天气数据异常")) {
                    updateWeather(getActivity());
                }
            }
        });

        //  在时间栏上显示时间
        new TimeThread().start();

        // 6小时获取一次天气
        new Thread(new HourGetWeatherThread()).start();

        // 小秘书
        ShortcutBoxView boxViewHealth = getShortcutBoxWithPos(ShortcutConst.HEALTH_ASSISTANT_POS);
        if (boxViewHealth != null) {
            boxViewHealth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Voice.getInstance().play(player, "小秘书");
                    // 给IM用的 todo
                    if (getActivity().getPackageManager().getLaunchIntentForPackage("com.laoyou99.im") != null) {
                        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.laoyou99.im");
                        intent.putExtra("extra_data", "1");
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), "您没有安装IM~~", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setAction("yhlauncher.download");
                        String apk = "http://apk.r1.market.hiapk.com/data/upload/apkres/2016/2_4/15/com.tencent.mobileqq_031014.apk";
                        intent.putExtra("ApkDownLoad", apk);
                        intent.putExtra("ApkTitle", "IM");
                        getContext().sendBroadcast(intent);
                    }
                }
            });
        }

        // 幸福小秘书
        ShortcutBoxView boxViewHappiness = getShortcutBoxWithPos(ShortcutConst.HEALTH_MGR_POS);
        if (boxViewHappiness != null) {
            boxViewHappiness.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Voice.getInstance().play(player, "健康管家");
                    if (getActivity().getPackageManager().getLaunchIntentForPackage("net.uniontest.fhm") != null) {
                        Intent intentHealth = getActivity().getPackageManager().getLaunchIntentForPackage("net.uniontest.fhm");
                        startActivity(intentHealth);
                    } else {
                        Toast.makeText(getActivity(), "您还没有安装健康管家", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setAction("yhlauncher.download");
                        String apk = "http://www.laoyou99.cn/app/3/1/Medical-4.2.4.apk";
                        intent.putExtra("ApkDownLoad", apk);
                        intent.putExtra("ApkTitle", "健康管家");
                        getContext().sendBroadcast(intent);
                    }
                }
            });
        }

        // 照相机
        ShortcutBoxView boxViewCamera = getShortcutBoxWithPos(ShortcutConst.CAMERA_POS);
        if (boxViewCamera != null) {
            boxViewCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Voice.getInstance().play(player, "照相机");
                    if (chooseCamera() != null) {
                        Intent intentCamera = getActivity().getPackageManager().getLaunchIntentForPackage(chooseCamera());
                        startActivity(intentCamera);
                    } else {
                        Toast.makeText(getActivity(), "您的相机有些小问题~", Toast.LENGTH_LONG).show();
                    }


                }
            });
        }

        // 微信
        ShortcutBoxView boxViewWeiXin = getShortcutBoxWithPos(ShortcutConst.WEIXIN_POS);
        if (boxViewWeiXin != null) {
            boxViewWeiXin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Voice.getInstance().play(player, "微信");
                    if (getActivity().getPackageManager().getLaunchIntentForPackage("com.tencent.mm") != null) {
                        Intent intentWeinxin = getActivity().getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                        startActivity(intentWeinxin);
                    } else {
                        Toast.makeText(getActivity(), "您还没有安装微信", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setAction("yhlauncher.download");
                        String apk = "http://apk.r1.market.hiapk.com/data/upload/apkres/2016/2_3/17/com.tencent.mm_054825.apk";
                        intent.putExtra("ApkDownLoad", apk);
                        intent.putExtra("ApkTitle", "微信");
                        getContext().sendBroadcast(intent);
                    }
                }
            });
        }

        // 电话
        ShortcutBoxView boxViewPhone = getShortcutBoxWithPos(ShortcutConst.PHONE_POS);
        if (boxViewPhone != null) {
            boxViewPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Voice.getInstance().play(player, "电话");
                    Intent intent = new Intent(getActivity(), CallRecordActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 语音助手
        ShortcutBoxView boxViewVoiceAssistant = getShortcutBoxWithPos(ShortcutConst.VOICE_ASSISTANT_POS);
        if (boxViewVoiceAssistant != null) {
            boxViewVoiceAssistant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Voice.getInstance().play(player, "语音助手");
                    if (getActivity().getPackageManager().getLaunchIntentForPackage("com.wowenwen.yy") != null) {
                        Intent intentNews = getActivity().getPackageManager().getLaunchIntentForPackage("com.wowenwen.yy");
                        startActivity(intentNews);
                    } else {
                        Intent intent = new Intent();
                        intent.setAction("yhlauncher.download");
                        String apk = "http://61.155.220.192/dd.myapp.com/16891/8C8D2ADD86EBEB61CD88CFA920C12328.apk?" +
                                "mkey=56d52fc168854541&f=b110&fsname=com.wowenwen.yy_1.5.5_1122.apk&p=.apk";
                        intent.putExtra("ApkDownLoad", apk);
                        intent.putExtra("ApkTitle", "语音助手");
                        getContext().sendBroadcast(intent);
                        Toast.makeText(getActivity(), "您没有安装语音助手", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }


    boolean flag = true;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.page_one_weather_ll:

                // 判断是否安装语记,未安装则跳转到提示安装页面
                if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                    Toast.makeText(getActivity(), "您没有安装语记", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setAction("yhlauncher.download");
                    String apk = "http://apk.r1.market.hiapk.com/data/upload/apkres" +
                            "/2016/3_9/19/com.iflytek.vflynote_071814.apk";
                    intent.putExtra("ApkDownLoad", apk);
                    intent.putExtra("Name", "是否下载语音助手");
                    getContext().sendBroadcast(intent);
                } else if (contentWeather != null) {

                    //  判断当前网络是否连接，有网才能播放天气
                    if (NetMgr.getInstance().isMobileConnected() || NetMgr.getInstance().isWifiConnected()) {
                        if (flag) {
                            player.startSpeaking(contentTime + contentWeather, mSynListener);
                            flag = false;
                        } else {
                            Voice.getInstance().pause(player);
                            flag = true;
                        }
                    } else {
                        Toast.makeText(getActivity(), "当前无网络连接，请去控制中心设置网络", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                break;
        }

    }

    // 重新加载 shortcut box
    public void reloadShortcuts() {

        //如果指定了页号，则在初始化时，会自动加载指定页面下的所有 shortcut 列表
        int pageNo = 1;
        for (int pos = 0; pos < ShortcutConst.DEFAULT_BOX_NUM_PER_PAGE; ++pos) {
            ShortcutBoxView boxView = shortcutBoxViewList.get(pos);
            if (boxView != null) {
                boxView.hideShortcut();
                Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(pageNo, pos);
                if (shortcut != null) {
                    boxView.setWithShortcut(shortcut);
                }
            }
        }
    }

    // 加载所有的 shortcutBoxView 组件至 shortcutBoxViewList
    public void loadShortcutBoxViews(View root) {
        YHLog.d(tag(), "loadShortcutBoxViews");
        if (root == null) {
            return;
        }

        shortcutBoxViewList.add(null);
        shortcutBoxViewList.add(null);
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.center_third_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.center_forth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.center_fifth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.center_sixth_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.center_seventh_shortcut));
        shortcutBoxViewList.add((ShortcutBoxView) root.findViewById(R.id.center_eighth_shortcut));
    }

    // 获取指定位置的 shortcut box
    public ShortcutBoxView getShortcutBoxWithPos(int pos) {
        if (pos < 0 || pos >= shortcutBoxViewList.size()) {
            return null;
        }

        return shortcutBoxViewList.get(pos);
    }

    /**
     * 根据 shortcut 实例展示对应的快捷方式
     */
    public void setShortcut(final Shortcut shortcut) {

        if (shortcut == null) {
            return;
        }

        if (shortcut.posInPage >= shortcutBoxViewList.size()) {
            return;
        }

        ShortcutBoxView shortcutBoxView = shortcutBoxViewList.get(shortcut.posInPage);
        if (shortcutBoxView == null) {
            return;
        }

        shortcutBoxView.setWithShortcut(shortcut);
        shortcutBoxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = shortcut.intent;
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    /**
     * 清除指定的 shortcut box
     */
    public void clearShortcutBox(Shortcut shortcut) {
        if (shortcut == null) {
            return;
        }

        if (shortcut.posInPage >= shortcutBoxViewList.size()) {
            return;
        }

        ShortcutBoxView shortcutBoxView = shortcutBoxViewList.get(shortcut.posInPage);
        if (shortcutBoxView == null) {
            return;
        }

        shortcutBoxView.hideShortcut();
    }


    // 子线程获取当前时间
    class TimeThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 101;
                    timeHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 更新健康小秘书
    public void addHealthAssistantShortcut(Shortcut shortcut) {

    }

    // 其中指定 shortcut 所对应的应用
    private void startAppWithShortcut(Shortcut shortcut) {

        if (shortcut == null || shortcut.intent == null) {
            YHLog.w(tag(), "startAppWithShortcut - shortcut is null");
            return;
        }

        Intent intent = shortcut.intent;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public String getWeather(String wea) {
        int v;
        String w;
        switch (wea) {
            // 晴天
            case "00":
                v = R.drawable.ic_china_wea_sunshine;
                w = "晴天";
                break;

            // 多云
            case "01":
                v = R.drawable.ic_china_wea_cloudy;
                w = "多云";
                break;

            // 阴
            case "02":
                v = R.drawable.ic_china_wea_overcast;
                w = "阴";
                break;

            // 阵雨
            case "03":
                v = R.drawable.ic_china_wea_shower;
                w = "阵雨";
                break;

            // 雷阵雨
            case "04":
                v = R.drawable.ic_china_wea_thundershower;
                w = "雷阵雨";
                break;

            // 雷阵雨伴有冰雹
            case "05":
                v = R.drawable.ic_china_wea_thundershower_with_hail;
                w = "雷阵雨伴有冰雹";
                break;

            // 雨夹雪
            case "06":
                v = R.drawable.ic_china_wea_sleet;
                w = "雨夹雪";
                break;

            // 小雨
            case "07":
                v = R.drawable.ic_china_wea_light_rain;
                w = "小雨";
                break;

            // 中雨
            case "08":
                v = R.drawable.ic_china_wea_moderate_rain;
                w = "中雨";
                break;

            // 大雨
            case "09":
                v = R.drawable.ic_china_wea_heavy_rain;
                w = "大雨";
                break;

            // 暴雨
            case "10":
                v = R.drawable.ic_china_wea_storm;
                w = "暴雨";
                break;

            // 大暴雨
            case "11":
                v = R.drawable.ic_china_wea_heavy_storm;
                w = "大暴雨";
                break;

            // 特大暴雨
            case "12":
                v = R.drawable.ic_china_wea_severe_storm;
                w = "特大暴雨";
                break;

            // 阵雪
            case "13":
                v = R.drawable.ic_china_wea_snow_flurry;
                w = "阵雪";
                break;

            // 小雪
            case "14":
                v = R.drawable.ic_china_wea_light_snow;
                w = "小雪";
                break;

            // 中雪
            case "15":
                v = R.drawable.ic_china_wea_moderate_snow;
                w = "中雪";
                break;

            // 大雪
            case "16":
                v = R.drawable.ic_china_wea_heavy_snow;
                w = "大雪";
                break;

            // 暴雪
            case "17":
                v = R.drawable.ic_china_wea_snowstorm;
                w = "暴雪";
                break;

            // 雾
            case "18":
                v = R.drawable.ic_china_wea_foggy;
                w = "雾";
                break;

            // 冻雨
            case "19":
                v = R.drawable.ic_china_wea_ice_rain;
                w = "冻雨";
                break;

            // 沙尘暴
            case "20":
                v = R.drawable.ic_china_wea_dust_storm;
                w = "沙尘暴";
                break;

            // 小到中雨
            case "21":
                v = R.drawable.ic_china_wea_light_to_moderate_rain;
                w = "小到中雨";
                break;

            //  中到大雨
            case "22":
                v = R.drawable.ic_china_wea_moderate_to_heavy_rain;
                w = "中到大雨";
                break;

            // 大到暴雨
            case "23":
                v = R.drawable.ic_china_wea_heavy_rain_to_storm;
                w = "大到暴雨";
                break;

            // 暴雨到大暴雨
            case "24":
                v = R.drawable.ic_china_wea_storm_to_heavy__storm;
                w = "暴雨到大暴雨";
                break;

            // 大暴雨到特大暴雨
            case "25":
                v = R.drawable.ic_china_wea_heavy_to_severe_storm;
                w = "大暴雨到特大暴雨";
                break;

            // 小到中雪
            case "26":
                v = R.drawable.ic_china_wea_light_to_moderate_snow;
                w = "小到中雪";
                break;

            // 中到大雪
            case "27":
                v = R.drawable.ic_china_wea_moderate_to_heavy_snow;
                w = "中到大雪";
                break;

            // 大到暴雪
            case "28":
                v = R.drawable.ic_china_wea_heavy_snow_to_snowstorm;
                w = "大到暴雪";
                break;

            // 浮尘
            case "29":
                v = R.drawable.ic_china_wea_dust;
                w = "浮尘";
                break;

            // 扬沙
            case "30":
                v = R.drawable.ic_china_wea_sand;
                w = "扬沙";
                break;

            // 强沙尘暴
            case "31":
                v = R.drawable.ic_china_wea_sandstorm;
                w = "强沙尘暴";
                break;

            // 霾
            case "53":
                v = R.drawable.ic_china_wea_haze;
                w = "霾";
                break;

            // 无数据
            case "99":
                v = R.drawable.ic_china_wea_unknown;
                w = "无数据";
                break;

            default:
                v = R.drawable.ic_china_wea_unknown;
                w = "无数据";
                break;
        }
        ivWeaPic.setImageResource(v);
        tvWea.setText(w);
        return w;
    }

    public String chooseCamera() {
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.zte.camera") != null) {
            return "com.zte.camera";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.android.camera") != null) {
            return "com.android.camera";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.android.camera1") != null) {
            return "com.android.camera1";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.android.camera2") != null) {
            return "com.android.camera2";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.android.camera3") != null) {
            return "com.android.camera3";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.android.camera4") != null) {
            return "com.android.camera4";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.meizu.media.camera") != null) {
            return "com.meizu.media.camera";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.huawei.camera") != null) {
            return "com.huawei.camera";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.android.gallery3d") != null) {
            return "com.android.gallery3d";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.android.gallery2d") != null) {
            return "com.android.gallery2d";
        }
        if (getActivity().getPackageManager().getLaunchIntentForPackage("com.android.galleryd") != null) {
            return "com.android.galleryd";
        }

        return null;
    }

    // 百度定位
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();

        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        // 可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd0911");
        int span = 10000;

        // 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于10
        option.setScanSpan(span);

        // 可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);

        // 开启GPS，默认就是不开启，可设置
        option.setOpenGps(false);

        // 默认false，设置是否当gps有效时按照1S1次频率输出
        option.setLocationNotify(false);

        // 可选，默认false，设置是否需要位置语义化结果
        option.setIsNeedLocationDescribe(true);

        // 可选，默认false，设置是否需要POI结果
        option.setIsNeedAltitude(true);

        // 默认true，定位SDK内部是一个SERVICE，并放到了独立进程
        option.setIgnoreKillProcess(true);

        // 默认false，设置是否收集CRASH信息
        option.SetIgnoreCacheException(false);

        // 默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setEnableSimulateGps(false);

        mLocationClient.setLocOption(option);
    }

    // 获取地理位置的监听器
    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            if (location == null) {
                YHLog.w(tag(), "location is null");
                return;
            }

            city = location.getCity();
            //  updateWeather();
        }
    }

    private void updateWeather(Context context) {
        //  天气的获取和显示
        if (PhoneStatus.getInstance().isWiFi(context) || NetMgr.getInstance().isNetConnect()) {
            new WeatherNewAsynctask(handler).execute();
        } else {
            Toast.makeText(context, "网络异常，请链接网络后再次尝试", Toast.LENGTH_LONG).show();
        }
    }

    class HourGetWeatherThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                Message msg = new Message();
                msg.what = 102;
                timeHandler.sendMessage(msg);
                try {
                    Thread.sleep(3600000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class WeatherHandler extends Handler {
        private final WeakReference<CenterFragment> centerFragment;

        WeatherHandler(CenterFragment centerFragment) {
            this.centerFragment = new WeakReference<>(centerFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CenterFragment fragment = centerFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                    case 50:
                        TodayWeather todayWeather = (TodayWeather) msg.obj;
                        if (todayWeather.getFa() != null && !(fragment.getWeather(todayWeather.getFa()).equals("无数据"))) {
                            String wea = todayWeather.getFa();
                            fragment.getWeather(wea);
                            fragment.tvTem.setText(todayWeather.getFc() + " ℃/" + todayWeather.getFd() + "℃");
                            fragment.contentWeather = "今天" + fragment.tvWea.getText().toString().trim() + "," +
                                    "最高温度" + todayWeather.getFc() + "度,最低温度" + todayWeather.getFd() + "度";
                        } else if (todayWeather.getFb() != null) {
                            String wea = todayWeather.getFb();
                            fragment.getWeather(wea);
                            fragment.tvTem.setText(todayWeather.getFc() + " ℃/" + todayWeather.getFd() + "℃");
                            fragment.contentWeather = "今天" + fragment.tvWea.getText().toString().trim() + "," +
                                    "最高温度" + todayWeather.getFc() + "度,最低温度" + todayWeather.getFd() + "度";
                        }
                        break;

                    case 51:
                        fragment.ivWeaPic.setImageResource(R.drawable.icon_weather_refresh);
                        fragment.tvTem.setText("天气数据异常");
                        fragment.tvWea.setText("点击图片刷新");
                        break;

                    default:
                        break;
                }

            }
        }
    }

    private static class TimeHandler extends Handler {
        private final WeakReference<CenterFragment> centerFragment;

        TimeHandler(CenterFragment centerFragment) {
            this.centerFragment = new WeakReference<CenterFragment>(centerFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CenterFragment fragment = centerFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                    case 101:
                        fragment.pgTime.setVisibility(View.GONE);
                        fragment.tvTime.setText(VoiceTime.getInstance().showTime());
                        fragment.contentTime = VoiceTime.getInstance().timeVoice(fragment.getActivity());
                        break;

                    case 102:
                        fragment.updateWeather(fragment.getActivity());
                        break;
                    default:
                        break;
                }
            }
        }
    }

}