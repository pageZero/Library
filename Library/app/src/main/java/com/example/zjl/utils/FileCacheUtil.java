package com.example.zjl.utils;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zjl on 2016/6/11.
 *
 * 缓存文件列表12条
 */
public class FileCacheUtil {

    //公司文件，部门文件，个人文件,下载文件，的缓存文件不一样
    public static final String companyDocCache = "com_docs_cache.txt";//缓存文件
    public static final String departmentDocCache = "dep_docs_cache.txt";//缓存文件
    public static final String personalDocCache = "per_docs_cache.txt";//缓存文件
    public static final String downloadDocCache = "download_docs_cache.txt";//缓存文件

    //缓存超时时间
    public static final int CACHE_SHORT_TIMEOUT=1000 * 60 * 5; // 5 分钟

    //设置缓存
    //downloadDocCache应该设为追加，其他的设为private
    public static void setCache(String content,Context context, String cacheFileName,int mode) {
        Log.e("-----------","开始缓存");
        FileOutputStream fos = null;
        try {
            //打开文件输出流
            fos = context.openFileOutput(cacheFileName,mode);
            fos.write(content.getBytes());
            Log.e("-----------","缓存完毕");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //读取缓存，返回字符串（JSON）
    public static String getCache(Context context, String cacheFileName) {
        FileInputStream fis = null;
        StringBuffer sBuf = new StringBuffer();
        try {
            fis = context.openFileInput(cacheFileName);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                sBuf.append(new String(buf,0,len));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(sBuf != null) {
            Log.e("-----------",sBuf.toString());
            return sBuf.toString();
        }
        return null;
    }

    public static String getCachePath(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }
}
