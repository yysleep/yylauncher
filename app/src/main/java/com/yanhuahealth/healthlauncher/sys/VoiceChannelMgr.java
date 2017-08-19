package com.yanhuahealth.healthlauncher.sys;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;

import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.ebook.Ebook;
import com.yanhuahealth.healthlauncher.model.voicechannel.VoiceItem;
import com.yanhuahealth.healthlauncher.utils.DownloadManagerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 语音频道的资源管理，负责：
 * <p/>
 * - 语音频道资源的新增，删除，查看，以及下载等管理
 */
public class VoiceChannelMgr {

    private static volatile VoiceChannelMgr instance;

    public static VoiceChannelMgr getInstance() {
        if (instance == null) {
            synchronized (VoiceChannelMgr.class) {
                if (instance == null) {
                    instance = new VoiceChannelMgr();
                }
            }
        }
        return instance;
    }

    public String tag() {
        return VoiceChannelMgr.class.getName();
    }

    // 当前语音频道支持的分类（21 讲座，22 评书）
    public static final int CAT_CHAIR = 21;
    public static final int CAT_STORYTELL = 22;

    public static final int[] ALL_CATS = new int[]{
            CAT_CHAIR, CAT_STORYTELL
    };

    // 存储所有本地文件系统中的语音资源列表
    // 按照语音资源的 ID 倒序排列
    private List<VoiceItem> allLocalVoices = new ArrayList<>();

    // 存储所有本地的按照分类进行映射的语音资源列表
    // key: 分类标识
    // value: 指定分类下的语音资源列表
    private Map<Integer, List<VoiceItem>> allLocalCatVoices = new HashMap<>();

    private Lock lockAllLocalVoices = new ReentrantLock();

    // 维护所有正在下载的音频列表
    private List<VoiceItem> allDownloadingVoices = new ArrayList<>();
    private Map<Integer, VoiceItem> allDownloadIdMapVoices = new HashMap<>();
    private Lock lockAllDownloadingVoices = new ReentrantLock();

    /**
     * 初始化 语音频道管理器
     */
    public boolean init(Context context) {
        if (context == null) {
            return false;
        }

        loadVoicesFromLocal(context);
        return true;
    }

    /**
     * 获取语音频道列表
     * 为避免并发问题，这里采用完整copy供调用方使用
     */
    public List<VoiceItem> getAllLocalVoices() {
        List<VoiceItem> voices = new ArrayList<>();
        lockAllLocalVoices.lock();
        try {
            for (VoiceItem voice : allLocalVoices) {
                if (voice != null) {
                    voices.add(voice);
                }
            }

        } finally {
            lockAllLocalVoices.unlock();
        }
        return voices;
    }

    /**
     * 根据指定 语音标识 获取在本地的对应的 语音记录
     */
    public VoiceItem getLocalVoiceItemByVoiceId(long voiceId) {
        if (voiceId <= 0) {
            return null;
        }

        lockAllLocalVoices.lock();
        try {
            for (VoiceItem voiceItem : allLocalVoices) {
                if (voiceItem != null && voiceItem.id == voiceId) {
                    return voiceItem;
                }
            }
        } finally {
            lockAllLocalVoices.unlock();
        }

        return null;
    }

    /**
     * 获取指定分类的语音频道列表
     */
    public List<VoiceItem> getLocalVoicesWithCat(int catId) {

        if (catId < 0) {
            return null;
        }

        lockAllLocalVoices.lock();
        try {
            List<VoiceItem> voices = allLocalCatVoices.get(catId);
            if (voices == null || voices.size() == 0) {
                return null;
            }

            List<VoiceItem> catVoices = new ArrayList<>();
            for (VoiceItem voice : catVoices) {
                if (voice != null) {
                    catVoices.add(voice);
                }
            }

            return catVoices;
        } finally {
            lockAllLocalVoices.unlock();
        }
    }

    /**
     * 判断当前支持的语音频道文件的格式
     */
    public boolean isSupportVoiceFormat(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }

        String[] pathSep = path.split("\\.");
        if (pathSep.length < 2) {
            return false;
        }

        String extendName = pathSep[pathSep.length - 1];
        if (!extendName.equalsIgnoreCase("mp3")) {
            return false;
        }

        return true;
    }

    /**
     * 获取指定 downloadId 的正在下载的音频
     * 如果不存在，则返回 null
     */
    public VoiceItem getDownloadingVoiceWithDownloadId(long downloadId) {
        if (downloadId <= 0) {
            return null;
        }

        lockAllDownloadingVoices.lock();
        try {
            return allDownloadIdMapVoices.get((int) downloadId);
        } finally {
            lockAllDownloadingVoices.unlock();
        }
    }

    /**
     * 判断给定的文件名是否为支持的音频文件缩略图的名称
     *
     * @return true 表示有效
     * false 无效语音频道缩略图文件名
     */
    public boolean isValidThumbFileName(String filename) {

        if (filename == null || filename.length() < 2) {
            return false;
        }

        String[] filepathSections = filename.split("/");
        if (filepathSections.length < 1) {
            return false;
        }

        String filenameWithExtend = filepathSections[filepathSections.length - 1];
        String[] filenameSections = filenameWithExtend.split("\\.");
        if (filenameSections.length != 2) {
            return false;
        }

        // xxx-x
        String filenameNoExtendWithPath = filenameSections[0];
        if (filenameNoExtendWithPath.length() < 3) {
            return false;
        }

        String[] filenameEbookInfo = filenameNoExtendWithPath.split("\\-");
        if (filenameEbookInfo.length != 2) {
            return false;
        }

        try {
            int ebookId = Integer.valueOf(filenameEbookInfo[0]);
            if (ebookId <= 0) {
                return false;
            }

            int thumbIndex = Integer.valueOf(filenameEbookInfo[1]);
            if (thumbIndex < 0) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // 后缀名
        String extendName = filenameSections[filenameSections.length - 1];
        return extendName.equalsIgnoreCase("png") || extendName.equalsIgnoreCase("jpg")
                || extendName.equalsIgnoreCase("jpeg");
    }

    /**
     * 指定的音频已经下载完成
     * 需要从正在下载的语音列表中移除
     */
    public boolean updateVoiceItemWithDownloadFinish(Context context, long downloadId) {

        if (downloadId < 0) {
            return false;
        }

        DownloadManager.Query queryByDownloadId = new DownloadManager.Query();
        queryByDownloadId.setFilterById(downloadId);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursorDownload = downloadManager.query(queryByDownloadId);
        if (cursorDownload == null) {
            return false;
        }

        if (!cursorDownload.moveToFirst()) {
            cursorDownload.close();
            return false;
        }

        int downloadStatus = cursorDownload.getInt(cursorDownload.getColumnIndex(DownloadManager.COLUMN_STATUS));
        cursorDownload.close();

        lockAllDownloadingVoices.lock();
        try {
            VoiceItem downloadingVoiceItem = allDownloadIdMapVoices.remove((int) downloadId);
            for (VoiceItem voice : allDownloadingVoices) {
                if (voice != null && voice.downloadId == downloadId) {
                    allDownloadingVoices.remove(voice);
                    break;
                }
            }

            if (downloadingVoiceItem != null) {
                if (downloadStatus == DownloadManager.STATUS_FAILED) {
                    downloadingVoiceItem.status = VoiceItem.STATUS_DOWNLOAD_FAIL;
                } else if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                    downloadingVoiceItem.status = VoiceItem.STATUS_DOWNLOAD_FINISH;

                    // 转移到本地音频列表中
                    addVoice(downloadingVoiceItem, true);
                } else {
                    downloadingVoiceItem.status = VoiceItem.STATUS_DOWNLOAD_UNKNOWN;
                }

            }
        } finally {
            lockAllDownloadingVoices.unlock();
        }
        return true;
    }

    /**
     * 新增一个音频到顶部or底部
     *
     * @param voice   待添加的音频
     * @param toBegin 是否添加到开始位置 true添加到开始 false添加到底部
     */
    public boolean addVoice(VoiceItem voice, boolean toBegin) {
        if (voice == null || voice.id <= 0 || voice.catId <= 0) {
            return false;
        }

        lockAllLocalVoices.lock();
        try {
            boolean isExists = false;
            for (VoiceItem localVoice : allLocalVoices) {
                if (localVoice != null && localVoice.id == voice.id) {
                    isExists = true;
                    break;
                }
            }

            if (!isExists) {
                List<VoiceItem> catVoices = allLocalCatVoices.get(voice.catId);
                if (catVoices == null) {
                    catVoices = new ArrayList<>();
                    allLocalCatVoices.put(voice.catId, catVoices);
                }

                if (toBegin) {
                    allLocalVoices.add(0, voice);
                    catVoices.add(0, voice);
                } else {
                    allLocalVoices.add(voice);
                    catVoices.add(voice);
                }
            }
        } finally {
            lockAllLocalVoices.unlock();
        }

        return true;
    }

    /**
     * 存储在本地的缩略图文件名（规范）
     */
    private String localVoiceThumbName(VoiceItem voice) {
        if (voice == null || voice.id <= 0 || voice.thumbUrl == null) {
            return null;
        }

        String[] thumbUrlSections = voice.thumbUrl.split("\\.");
        if (thumbUrlSections.length < 2) {
            return null;
        }
        String thumbExtendName = thumbUrlSections[thumbUrlSections.length - 1];

        return voice.id + "-0." + thumbExtendName;
    }

    /**
     * 保存缩略图
     * 主要用于将 UIL 缓存的文件保存到本地的缩略图文件中
     * 从而能够在下次启动后从本地文件系统加载进来
     *
     * @return 0  表示保存成功
     * -1  输入参数无效
     * -2  文件不存在
     * 其他 异常
     */
    public int saveVoiceThumb(VoiceItem voice, File localCacheThumbFile) {
        if (voice == null || localCacheThumbFile == null || voice.id <= 0) {
            return -1;
        }

        if (!localCacheThumbFile.exists()) {
            return -2;
        }

        // 保存到本地文件系统的缩略图路径
        String voiceThumbPath = LauncherConst.getEbookRootPath() + localVoiceThumbName(voice);

        try {
            InputStream srcStream = new FileInputStream(localCacheThumbFile);
            FileOutputStream destStream = new FileOutputStream(voiceThumbPath);
            byte[] buf = new byte[1024];
            int byteRead;
            while ((byteRead = srcStream.read(buf)) != -1) {
                destStream.write(buf, 0, byteRead);
            }
            srcStream.close();
            destStream.close();
        } catch (FileNotFoundException e) {
            YHLog.w(tag(), "not found - " + e.getMessage());
            return -3;
        } catch (IOException e) {
            return -4;
        }

        return 0;
    }

    /**
     * 移除指定音频标识的音频
     */
    public boolean removeVoice(VoiceItem voice) {
        if (voice == null || voice.catId <= 0 || voice.id <= 0) {
            return false;
        }

        lockAllLocalVoices.lock();
        try {
            for (VoiceItem localVoice : allLocalVoices) {
                if (localVoice != null && localVoice.id == voice.id) {
                    allLocalVoices.remove(localVoice);
                }

                // 从相应的分类音频列表中移除
                List<VoiceItem> catVoices = allLocalCatVoices.get(voice.catId);
                if (catVoices != null && catVoices.size() > 0) {
                    for (VoiceItem catVoice : catVoices) {
                        if (catVoice != null && catVoice.id == voice.id) {
                            catVoices.remove(catVoice);
                            break;
                        }
                    }
                }
                return true;
            }
        } finally {
            lockAllLocalVoices.unlock();
        }

        return false;
    }

    private String localVoiceName(VoiceItem voice) {
        if (voice == null || voice.id <= 0 || voice.catId <= 0 && voice.name == null || voice.author == null) {
            return null;
        }

        return voice.id + "-" + voice.catId + "-" + voice.name + "-" + voice.author + ".mp3";
    }

    /**
     * 添加正在下载的音频
     */
    public boolean addDownloadingVoice(VoiceItem voice, boolean toBegin) {

        if (voice == null || voice.id <= 0 || voice.catId <= 0) {
            return false;
        }

        lockAllDownloadingVoices.lock();
        try {
            boolean isExists = false;
            for (VoiceItem downloadingVoiceItem : allDownloadingVoices) {
                if (downloadingVoiceItem != null
                        && (downloadingVoiceItem.id == voice.id
                        || downloadingVoiceItem.downloadId == voice.downloadId)) {
                    isExists = true;
                    break;
                }
            }

            if (!isExists) {
                allDownloadIdMapVoices.put((int) voice.downloadId, voice);
                if (toBegin) {
                    allDownloadingVoices.add(0, voice);
                } else {
                    allLocalVoices.add(voice);
                }
            }
        } finally {
            lockAllDownloadingVoices.unlock();
        }

        return true;
    }

    /**
     * 提交下载音频的任务
     *
     * @param voice 待下载的音频
     * @return 0   表示提交下载任务成功
     * -1  参数有误
     * -2  启动下载任务失败
     * 1   表示本地已经下载且存在于本地文件系统中，
     * 并且会设置参数 voice.localPath 指向本地文件路径
     * 2   表示已经在下载队列中
     */
    public int downloadVoice(Context context, VoiceItem voice) {
        if (voice == null || voice.downloadUrl == null || !voice.downloadUrl.startsWith("http")) {
            return -1;
        }

        String voiceFileName = localVoiceName(voice);
        String voiceFilePath = LauncherConst.getVoiceRootPath() + voiceFileName;
        File voiceFile = new File(voiceFilePath);
        if (voiceFile.exists()) {
            voice.localPath = voiceFile.getAbsolutePath();
            return 1;
        }

        try {
            voice.status = VoiceItem.STATUS_DOWNLOADING;
            voice.localPath = voiceFilePath;
            voice.downloadId = DownloadManagerUtils.getInstance().startDownloadApk(
                    context, voiceFileName, null, voice.downloadUrl, LauncherConst.DOWNLOAD_PATH_VOICE);
            Log.d(tag(), "ID" + voice.downloadId);
            addDownloadingVoice(voice, false);
        } catch (Exception e) {
            voice.status = VoiceItem.STATUS_DOWNLOAD_FAIL;
            e.printStackTrace();
            return -2;
        }
        return 0;
    }

    /**
     * 加载本地音频列表
     */
    private boolean loadVoicesFromLocal(Context context) {
        if (context == null) {
            return false;
        }

        lockAllLocalVoices.lock();
        try {
            for (int catId : ALL_CATS) {
                // 先扫描本地是否有指定分类的音频列表
                File dirVoice = new File(LauncherConst.getVoiceRootPath());
                File[] voiceFiles = dirVoice.listFiles(new VoiceFileFilter(catId));
                if (voiceFiles != null && voiceFiles.length > 0) {
                    for (File voiceFile : voiceFiles) {
                        if (voiceFile != null) {
                            String fileName = voiceFile.getName();
                            String[] fileNameSep = fileName.split("-");
                            if (fileNameSep.length != 4) {
                                continue;
                            }

                            VoiceItem voiceInfo = new VoiceItem();
                            voiceInfo.id = Integer.valueOf(fileNameSep[0]);
                            voiceInfo.catId = Integer.valueOf(fileNameSep[1]);
                            voiceInfo.name = fileNameSep[2];
                            voiceInfo.author = fileNameSep[3].split("\\.")[0];
                            voiceInfo.localPath = voiceFile.getAbsolutePath();

                            if (voiceInfo.id > 0 && voiceInfo.catId > 0) {
                                allLocalVoices.add(voiceInfo);

                                List<VoiceItem> catVoices = allLocalCatVoices.get(voiceInfo.catId);
                                if (catVoices == null) {
                                    catVoices = new ArrayList<>();
                                    allLocalCatVoices.put(voiceInfo.catId, catVoices);
                                }
                                catVoices.add(voiceInfo);
                            }
                        }
                    }
                }
            }

            // 对所有音频做统一排序
            if (allLocalVoices != null && allLocalVoices.size() > 1) {
                Collections.sort(allLocalVoices);
            }

            for (Integer catId : allLocalCatVoices.keySet()) {
                List<VoiceItem> localCatVoices = allLocalCatVoices.get(catId);
                if (localCatVoices != null && localCatVoices.size() > 1) {
                    Collections.sort(localCatVoices);
                }
            }
        } finally {
            lockAllLocalVoices.unlock();
        }
        return true;
    }

    // 音频文件过滤
    class VoiceFileFilter implements FilenameFilter {

        private int catId;

        public VoiceFileFilter(int catId) {
            this.catId = catId;
        }

        @Override
        public boolean accept(File dir, String filename) {
            if (!isSupportVoiceFormat(filename)) {
                YHLog.i(tag(), "not MP3 file of " + filename);
                return false;
            }

            String[] arrFileInfo = filename.split("-");
            if (arrFileInfo.length != 4) {
                YHLog.i(tag(), "error format of " + filename);
                return false;
            }

            try {
                int catIdOfFilename = Integer.valueOf(arrFileInfo[1]);
                if (catId > 1 && catIdOfFilename != catId) {
                    YHLog.i(tag(), "not equal cat<" + catId + "> of " + filename);
                    return false;
                }
            } catch (NumberFormatException ex) {
                YHLog.i(tag(), "error <category> format of " + filename);
                return false;
            }

            return true;
        }
    }

    /**
     * 清空所有音频列表
     */
    public boolean clearAllLocalVoices() {
        lockAllLocalVoices.lock();
        try {
            allLocalVoices.clear();
            allLocalCatVoices.clear();
        } finally {
            lockAllLocalVoices.unlock();
        }
        return true;
    }

}
