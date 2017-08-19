package com.yanhuahealth.healthlauncher;

import android.app.Application;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;

import com.bugtags.library.Bugtags;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.yanhuahealth.healthlauncher.api.HealthApi;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.dao.LauncherDBMgr;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.model.ebook.Ebook;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.EbookMgr;
import com.yanhuahealth.healthlauncher.sys.MainService;
import com.yanhuahealth.healthlauncher.sys.NetMgr;
import com.yanhuahealth.healthlauncher.sys.CallMgr;
import com.yanhuahealth.healthlauncher.sys.VoiceChannelMgr;

import java.io.File;

/**
 * 整个应用的入口
 * <p/>
 * Created by steven on 2016/1/12.
 */
public class HealthLauncherApplication extends Application {

    public String tag() {
        return HealthLauncherApplication.class.getName();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 科大讯飞语音包初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=568f81ed");

        HealthApi.init(getString(R.string.server_host), Integer.valueOf(getString(R.string.server_port)));
        NetMgr.getInstance().init(getApplicationContext());
        LauncherDBMgr.getInstance().init(getApplicationContext());
        ContactMgr.getInstance().init(getApplicationContext());

        VoiceChannelMgr.getInstance().init(getApplicationContext());
        EbookMgr.getInstance().init(getApplicationContext());

        MainService.getInstance().init();
        DisplayImageOptions defaultImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .resetViewBeforeLoading(true)
                .considerExifParams(true).build();

        ImageLoaderConfiguration imageLoaderConfiguration
                = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultImageOptions)
                .denyCacheImageMultipleSizesInMemory()
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);

        // 创建好所有相关的存储路径
        initStorage();

        // 注册observer
        ContactObserver contactObserver=new ContactObserver(null);
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI,true,contactObserver);

        Bugtags.start("3ff2e8fe072083b533a714dc377bac62", this, Bugtags.BTGInvocationEventNone);
    }

    private boolean initStorage() {

        // 安装包路径
        String apkPath = LauncherConst.getApkRootPath();
        File fileApk = new File(apkPath);
        if (!fileApk.exists()) {
            if (!fileApk.mkdirs()) {
                YHLog.e(tag(), "can not create [apk] path!!!!");
                return false;
            }
        }

        // 电子书路径
        String ebookPath = LauncherConst.getEbookRootPath();
        File fileEbook = new File(ebookPath);
        if (!fileEbook.exists()) {
            if (!fileEbook.mkdirs()) {
                YHLog.e(tag(), "can not create [ebook] path!!!!");
                return false;
            }
        }

        // 语音频道路径
        String voicePath = LauncherConst.getVoiceRootPath();
        File fileVoice = new File(voicePath);
        if (!fileVoice.exists()) {
            if (!fileVoice.mkdirs()) {
                YHLog.e(tag(), "can not create [voice] path!!!!");
                return false;
            }
        }

        // 图片存储路径
        String imagePath = LauncherConst.getImageRootPath();
        File fileImage = new File(imagePath);
        if (!fileImage.exists()) {
            if (!fileImage.mkdirs()) {
                YHLog.e(tag(), "can not create [image] path!!!!");
                return false;
            }
        }

        // 图标存储路径
        String iconPath = LauncherConst.getIconRootPath();
        File fileIcon = new File(iconPath);
        if (!fileIcon.exists()) {
            if (!fileIcon.mkdirs()) {
                YHLog.e(tag(), "can not create [icon] path!!!!");
                return false;
            }
        }

        return true;
    }

    class ContactObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ContactObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ContactMgr.getInstance().updateContactList(getApplicationContext());
        }
    }
}
