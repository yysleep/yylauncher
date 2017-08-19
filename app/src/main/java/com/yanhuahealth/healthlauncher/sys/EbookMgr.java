package com.yanhuahealth.healthlauncher.sys;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.ebook.Ebook;
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
 * 电子书的管理层
 * <p/>
 * 提供电子书的统一增加，删除，查看，下载等操作的管理
 * <p/>
 * 注：
 * <p/>
 * 1. 所有的针对电子书的管理操作都需要通过 **EbookMgr** 来处理；
 * 2. 所有的电子书操作都提供 Ebook 实例，并要求提供的 Ebook 实例必须初始化 id 以及 catId；
 */
public class EbookMgr {

    private static volatile EbookMgr instance;

    public static EbookMgr getInstance() {
        if (instance == null) {
            synchronized (EbookMgr.class) {
                if (instance == null) {
                    instance = new EbookMgr();
                }
            }
        }
        return instance;
    }

    public String tag() {
        return EbookMgr.class.getName();
    }

    // 当前支持的电子书分类
    public static final int CAT_HEALTH = 11;
    public static final int CAT_STORY = 12;
    public static final int CAT_OTHER = 13;

    public static final int[] ALL_CATS = new int[]{
            CAT_HEALTH, CAT_STORY, CAT_OTHER
    };

    // 存储所有本地文件系统中的电子书列表
    // 按照电子书的 ID 倒序排列
    private List<Ebook> allLocalEbooks = new ArrayList<>();

    // 存储所有本地的按照分类进行映射的电子书列表
    // key: 分类标识
    // value: 指定分类下的电子书列表
    private Map<Integer, List<Ebook>> allLocalCatEbooks = new HashMap<>();

    private Lock lockAllLocalEbooks = new ReentrantLock();

    // 维护所有的正在下载的电子书列表
    private List<Ebook> allDownloadingEbooks = new ArrayList<>();
    private Map<Integer, Ebook> allDownloadIdMapEbooks = new HashMap<>();
    private Lock lockAllDownloadingEbooks = new ReentrantLock();

    /**
     * 电子书管理器的初始化
     * <p/>
     * - 加载本地的电子书列表
     */
    public boolean init(Context context) {
        if (context == null) {
            return false;
        }

        loadEbooksFromLocal(context);
        return true;
    }

    /**
     * 获取电子书列表
     * 为了避免并发问题，这里采用完整copy供调用方使用
     */
    public List<Ebook> getAllLocalEbooks() {
        List<Ebook> ebooks = new ArrayList<>();
        lockAllLocalEbooks.lock();
        try {
            for (Ebook ebook : allLocalEbooks) {
                if (ebook != null) {
                    ebooks.add(ebook);
                }
            }
        } finally {
            lockAllLocalEbooks.unlock();
        }
        return ebooks;
    }

    /**
     * 获取指定分类的电子书列表
     */
    public List<Ebook> getLocalEbooksWithCat(int catId) {

        if (catId <= 0) {
            return null;
        }

        lockAllLocalEbooks.lock();
        try {
            List<Ebook> ebooks = allLocalCatEbooks.get(catId);
            if (ebooks == null || ebooks.size() == 0) {
                return null;
            }

            List<Ebook> catEbooks = new ArrayList<>();
            for (Ebook ebook : ebooks) {
                if (ebook != null) {
                    catEbooks.add(ebook);
                }
            }

            return catEbooks;
        } finally {
            lockAllLocalEbooks.unlock();
        }
    }

    /**
     * 判断当前支持的电子书文件的格式
     */
    public static boolean isSupportEbookFormat(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }

        String[] pathSep = path.split("\\.");
        if (pathSep.length < 2) {
            return false;
        }

        String extendName = pathSep[pathSep.length - 1];
        return extendName.equalsIgnoreCase("pdf");
    }

    /**
     * 获取指定 downloadId 的正在下载的电子书
     * 如果不存在，则返回 null
     */
    public Ebook getDownloadingEbookWithDownloadId(long downloadId) {
        if (downloadId <= 0) {
            return null;
        }

        lockAllDownloadingEbooks.lock();
        try {
            return allDownloadIdMapEbooks.get((int) downloadId);
        } finally {
            lockAllDownloadingEbooks.unlock();
        }
    }

    /**
     * 添加正在下载的电子书
     */
    public boolean addDownloadingEbook(Ebook ebook, boolean toBegin) {

        if (ebook == null || ebook.id <= 0 || ebook.catId <= 0) {
            return false;
        }

        lockAllDownloadingEbooks.lock();
        try {
            boolean isExists = false;
            for (Ebook downloadingEbook : allDownloadingEbooks) {
                if (downloadingEbook != null
                        && (downloadingEbook.id == ebook.id
                        || downloadingEbook.downloadId == ebook.downloadId)) {
                    isExists = true;
                    break;
                }
            }

            if (!isExists) {
                allDownloadIdMapEbooks.put((int) ebook.downloadId, ebook);
                if (toBegin) {
                    allDownloadingEbooks.add(0, ebook);
                } else {
                    allDownloadingEbooks.add(ebook);
                }
            }
        } finally {
            lockAllDownloadingEbooks.unlock();
        }

        return true;
    }

    /**
     * 指定的电子书已经下载完成
     * 需要从 正在下载 的电子书列表中移除
     */
    public boolean updateEbookWithDownloadFinish(Context context, long downloadId) {

        if (downloadId <= 0) {
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
        int reason = cursorDownload.getInt(cursorDownload.getColumnIndex(DownloadManager.COLUMN_REASON));
        switch (downloadStatus) {
            case DownloadManager.STATUS_FAILED:
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        Log.d("download_error", "ERROR_CANNOT_RESUME");
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        Log.d("download_error", "ERROR_DEVICE_NOT_FOUND");
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        Log.d("download_error", "ERROR_FILE_ALREADY_EXISTS");
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        Log.d("download_error", "ERROR_FILE_ERROR");
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        Log.d("download_error", "ERROR_HTTP_DATA_ERROR");
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        Log.d("download_error", "ERROR_INSUFFICIENT_SPACE");
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        Log.d("download_error", "ERROR_TOO_MANY_REDIRECTS");
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        Log.d("download_error", "ERROR_UNHANDLED_HTTP_CODE");
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        Log.d("download_error", "ERROR_UNKNOWN");
                        break;
                    case 404:
                        Log.d("download_error", "出错Reason = 404");
                        break;
                }
        }
        cursorDownload.close();

        lockAllDownloadingEbooks.lock();
        try {
            // 从正在下载的电子书列表中移除
            Ebook downloadingEbook = allDownloadIdMapEbooks.remove((int) downloadId);
            for (Ebook ebook : allDownloadingEbooks) {
                if (ebook != null && ebook.downloadId == downloadId) {
                    allDownloadingEbooks.remove(ebook);
                    break;
                }
            }

            if (downloadingEbook != null) {
                if (downloadStatus == DownloadManager.STATUS_FAILED) {
                    downloadingEbook.status = Ebook.STATUS_DOWNLOAD_FAIL;
                } else if (downloadStatus == DownloadManager.STATUS_RUNNING) {
                    downloadingEbook.status = Ebook.STATUS_DOWNLOADING;
                } else if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                    downloadingEbook.status = Ebook.STATUS_DOWNLOAD_FINISH;

                    // 转移到本地电子书列表中
                    addEbook(downloadingEbook, true);
                } else {
                    downloadingEbook.status = Ebook.STATUS_DOWNLOAD_UNKNOWN;
                }
            }
        } finally {
            lockAllDownloadingEbooks.unlock();
        }


        return true;
    }

    /**
     * 新增一本电子书记录顶部 or 底部
     *
     * @param ebook   待添加的电子书
     * @param toBegin 是否为添加到开始位置，true 添加到开始位置，false 添加到尾部
     */
    public boolean addEbook(Ebook ebook, boolean toBegin) {
        if (ebook == null || ebook.id <= 0 || ebook.catId <= 0) {
            return false;
        }

        lockAllLocalEbooks.lock();
        try {
            boolean isExists = false;
            for (Ebook localEbook : allLocalEbooks) {
                if (localEbook != null && localEbook.id == ebook.id) {
                    isExists = true;
                    break;
                }
            }

            if (!isExists) {
                List<Ebook> catEbooks = allLocalCatEbooks.get(ebook.catId);
                if (catEbooks == null) {
                    catEbooks = new ArrayList<>();
                    allLocalCatEbooks.put(ebook.catId, catEbooks);
                }

                if (toBegin) {
                    allLocalEbooks.add(0, ebook);
                    catEbooks.add(0, ebook);
                } else {
                    allLocalEbooks.add(ebook);
                    catEbooks.add(ebook);
                }
            }
        } finally {
            lockAllLocalEbooks.unlock();
        }

        return true;
    }

    /**
     * 根据指定的 ebookId 获取对应的电子书
     * 如果不存在，则返回 null
     */
    public Ebook getEbookWithEbookId(long ebookId) {

        if (ebookId <= 0) {
            return null;
        }

        lockAllLocalEbooks.lock();
        try {
            for (Ebook ebook : allLocalEbooks) {
                if (ebook != null && ebook.id == ebookId) {
                    return ebook;
                }
            }
        } finally {
            lockAllLocalEbooks.unlock();
        }

        return null;
    }

    /**
     * 移除指定电子书标识的电子书
     */
    public boolean removeEbook(Ebook ebook) {
        if (ebook == null || ebook.id <= 0 || ebook.catId <= 0) {
            return false;
        }

        lockAllLocalEbooks.lock();
        try {
            for (Ebook localEbook : allLocalEbooks) {
                if (localEbook != null && localEbook.id == ebook.id) {
                    allLocalEbooks.remove(localEbook);
                    File file = new File(localEbook.localPath);
                    file.delete();
                    // 从相应的分类电子书列表中移除
                    List<Ebook> catEbooks = allLocalCatEbooks.get(ebook.catId);
                    if (catEbooks != null && catEbooks.size() > 0) {
                        for (Ebook catEbook : catEbooks) {
                            if (catEbook != null && catEbook.id == ebook.id) {
                                catEbooks.remove(catEbook);
                                File file1 = new File(catEbook.localPath);
                                file1.delete();
                                break;
                            }
                        }
                    }

                    return true;
                }
            }
        } finally {
            lockAllLocalEbooks.unlock();
        }

        return false;
    }

    /**
     * 存储在本地的电子书名（规范）
     */
    private static String localEbookName(Ebook ebook) {
        if (ebook == null || ebook.id <= 0 || ebook.name == null || ebook.author == null) {
            return null;
        }

        return ebook.id + "-" + ebook.catId + "-" + ebook.name + "-" + ebook.author + ".pdf";
    }

    /**
     * 存储在本地的缩略图文件名（规范）
     */
    private String localEbookThumbName(Ebook ebook) {
        if (ebook == null || ebook.id <= 0 || ebook.thumbUrl == null) {
            return null;
        }

        String[] thumbUrlSections = ebook.thumbUrl.split("\\.");
        if (thumbUrlSections.length < 2) {
            return null;
        }
        String thumbExtendName = thumbUrlSections[thumbUrlSections.length - 1];

        return ebook.id + "-0." + thumbExtendName;
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
    public int saveEbookThumb(Ebook ebook, File localCacheThumbFile) {
        if (ebook == null || localCacheThumbFile == null || ebook.id <= 0) {
            return -1;
        }

        if (!localCacheThumbFile.exists()) {
            return -2;
        }

        // 保存到本地文件系统的缩略图路径
        String ebookThumbPath = LauncherConst.getEbookRootPath() + localEbookThumbName(ebook);

        try {
            InputStream srcStream = new FileInputStream(localCacheThumbFile);
            FileOutputStream destStream = new FileOutputStream(ebookThumbPath);
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
     * 提交下载电子书的任务
     *
     * @param ebook 待下载的电子书
     * @return 0   表示提交下载任务成功
     * -1  参数有误
     * -2  启动下载任务失败
     * 1   表示本地已经下载且存在于本地文件系统中，
     * 并且会设置参数 ebook.localPath 指向本地文件路径
     * 2   表示已经在下载队列中
     */
    public int downloadEbook(Context context, Ebook ebook) {

        if (ebook == null || ebook.downloadUrl == null || !ebook.downloadUrl.startsWith("http")) {
            return -1;
        }

        String ebookFileName = localEbookName(ebook);
        String ebookFilePath = LauncherConst.getEbookRootPath() + ebookFileName;
        File ebookFile = new File(ebookFilePath);
        if (ebookFile.exists()) {
            ebook.localPath = ebookFile.getAbsolutePath();
            return 1;
        }

        try {
            ebook.status = Ebook.STATUS_DOWNLOADING;
            ebook.localPath = ebookFilePath;
            ebook.downloadId = DownloadManagerUtils.getInstance().startDownloadApk(
                    context, ebookFileName, null, ebook.downloadUrl, LauncherConst.DOWNLOAD_PATH_EBOOK);
            addDownloadingEbook(ebook, false);
        } catch (Exception e) {
            ebook.status = Ebook.STATUS_DOWNLOAD_FAIL;
            e.printStackTrace();
            return -2;
        }

        return 0;
    }

    /**
     * 加载电子书的缩略图列表
     */
    private boolean loadThumbsFromLocal(Context context) {

        if (context == null) {
            return false;
        }

        File dirEbook = new File(LauncherConst.getEbookRootPath());
        File[] ebookThumbFiles = dirEbook.listFiles(new EbookThumbFileFilter());
        if (ebookThumbFiles != null && ebookThumbFiles.length > 0) {
            for (File ebookThumbFile : ebookThumbFiles) {
                if (ebookThumbFile != null) {
                    String ebookThumbFileName = ebookThumbFile.getName();
                    String[] filenameSep = ebookThumbFileName.split("-");
                    int ebookId = Integer.valueOf(filenameSep[0]);

                    // 从电子书列表中查找对应的电子书
                    for (Ebook ebook : allLocalEbooks) {
                        if (ebook != null && ebook.id == ebookId) {
                            ebook.thumbUrl = "file://" + ebookThumbFile.getAbsolutePath();
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * 加载本地电子书列表
     */
    private boolean loadEbooksFromLocal(Context context) {

        if (context == null) {
            return false;
        }

        lockAllLocalEbooks.lock();
        try {
            for (int catId : ALL_CATS) {
                // 先扫描本地是否有指定分类的电子书列表
                File dirEbook = new File(LauncherConst.getEbookRootPath());
                File[] ebookFiles = dirEbook.listFiles(new EbookFileFilter(catId));
                if (ebookFiles != null && ebookFiles.length > 0) {
                    for (File ebookFile : ebookFiles) {
                        if (ebookFile != null) {
                            String filename = ebookFile.getName();
                            String[] filenameSep = filename.split("-");
                            if (filenameSep.length != 4) {
                                continue;
                            }

                            Ebook ebookInfo = new Ebook();
                            ebookInfo.id = Integer.valueOf(filenameSep[0]);
                            ebookInfo.catId = Integer.valueOf(filenameSep[1]);
                            ebookInfo.name = filenameSep[2];
                            ebookInfo.author = filenameSep[3].split("\\.")[0];
                            ebookInfo.localPath = ebookFile.getAbsolutePath();
                            ebookInfo.status = Ebook.STATUS_DOWNLOAD_FINISH;

                            if (ebookInfo.id > 0 && ebookInfo.catId > 0) {
                                allLocalEbooks.add(ebookInfo);

                                List<Ebook> catEbooks = allLocalCatEbooks.get(ebookInfo.catId);
                                if (catEbooks == null) {
                                    catEbooks = new ArrayList<>();
                                    allLocalCatEbooks.put(ebookInfo.catId, catEbooks);
                                }
                                catEbooks.add(ebookInfo);
                            }
                        }
                    }
                }
            }

            // 对所有电子书统一做排序
            if (allLocalEbooks != null && allLocalEbooks.size() > 1) {
                Collections.sort(allLocalEbooks);
            }

            for (Integer catId : allLocalCatEbooks.keySet()) {
                List<Ebook> localCatEbooks = allLocalCatEbooks.get(catId);
                if (localCatEbooks != null && localCatEbooks.size() > 1) {
                    Collections.sort(localCatEbooks);
                }
            }

            // 加载对应的缩略图
            loadThumbsFromLocal(context);
        } finally {
            lockAllLocalEbooks.unlock();
        }

        return true;
    }

    /**
     * 判断给定的文件名是否为支持的电子书缩略图的名称
     *
     * @return true 表示有效
     * false 无效电子书缩略图文件名
     */
    public static boolean isValidThumbFileName(String filename) {

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

    // 电子书缩略图文件过滤
    class EbookThumbFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            return isValidThumbFileName(filename);
        }
    }

    // 电子书文件过滤
    class EbookFileFilter implements FilenameFilter {

        private int catId;

        public EbookFileFilter(int catId) {
            this.catId = catId;
        }

        @Override
        public boolean accept(File dir, String filename) {
            if (!isSupportEbookFormat(filename)) {
                YHLog.i(tag(), "not PDF file of " + filename);
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
     * 清空所有的电子书列表
     */
    public boolean clearAllLocalEbooks() {
        lockAllLocalEbooks.lock();
        try {
            allLocalEbooks.clear();
            allLocalCatEbooks.clear();
        } finally {
            lockAllLocalEbooks.unlock();
        }

        return true;
    }
}
