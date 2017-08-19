package com.yanhuahealth.healthlauncher.sys;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.utils.Utilities;

import java.io.File;

/**
 * 应用内部各种图片的统一管理
 */
public class ImageManager {

    private static ImageManager instance = new ImageManager();
    public static ImageManager getInstance() {
        return instance;
    }

    /**
     * 获取指定应用的图标路径
     */
    public String getAppIconPathWithPkgName(String packageName) {
        return LauncherConst.getIconRootPath() + packageName + ".png";
    }

    /**
     * 判断本地图标路径下是否存在指定应用的图标
     *
     * @param packageName 应用的包名称
     * @return true 为存在，否则不存在
     */
    public boolean existsAppIconWithPkgName(String packageName) {

        if (packageName == null || packageName.length() == 0) {
            return false;
        }

        String iconPath = getAppIconPathWithPkgName(packageName);
        File fileIcon = new File(iconPath);
        return fileIcon.exists();
    }

    /**
     * 新增应用图标
     *
     * @return 保存成功后返回保存的绝对路径
     */
    public String saveAppIcon(Context context, ApplicationInfo appInfo) {
        Bitmap bmpIcon = Utilities.createIconBitmap(
                appInfo.loadIcon(context.getPackageManager()), context);
        if (bmpIcon != null) {
            String iconPath = LauncherConst.getIconRootPath() + appInfo.packageName + ".png";
            if (Utilities.writeBitmapToFile(bmpIcon, iconPath)) {
                return "file://" + iconPath;
            }
        }

        return null;
    }
}
