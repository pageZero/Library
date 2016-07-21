package com.example.zjl.utils;

/**
 * 响应体进度回调接口，比如用于文件下载中
 * Created by zjl on 2016/6/11.
 */
public interface ProgressResponseListener {

    public void onResponseProgress(long bytesRead, long contentLength, boolean done);
}
