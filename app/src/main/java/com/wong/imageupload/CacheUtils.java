package com.wong.imageupload;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

/**
 * 缓存文件夹的工具类
 */
public final class CacheUtils {
    // 生成images文件夹
    public static void createImagesCacheFolder(Context context) {

        createCacheFolder(context,"images");
    }

    // 获取images文件夹
    public static File getCacheImagesFolder(Context context){
        return getFile(context,"images");
    }



    /**
     * 返回指定的文件对象
     * @param context 上下文
     * @param fileName 文件名
     * @return 如果存在，则返回文件对象，否则返回null
     */
    public static File getFile(Context context,String fileName){

        if (TextUtils.isEmpty(fileName)) return null;

        String path = null;

        if (fileName.startsWith("/")) {
            path = context.getCacheDir() + fileName;
        } else {
            path = context.getCacheDir() + "/" + fileName;
        }

        File file = new File(path);
        if(file.exists()){
            return file;
        }else {
            return null;
        }
    }

    /**
     * 创建应用内cache/下的文件夹
     * @param context 上下文
     * @param folder 文件
     * @return 成功 返回 true，否则返回false
     */
    public static boolean createCacheFolder(Context context, String folder) {

        if (TextUtils.isEmpty(folder)) return false;

        String path = null;

        if (folder.startsWith("/")) {
            path = context.getCacheDir() + folder;
        } else {
            path = context.getCacheDir() + "/" + folder;
        }

        if (TextUtils.isEmpty(path)) return false;

        File file = new File(path);

        if(!file.exists()){
            return file.mkdirs();
        }

        return true;
    }

}
