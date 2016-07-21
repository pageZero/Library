package com.example.zjl.utils;

/**
 * 请求体进度回调接口，比如用于文件上传中
 * Created by zjl on 2016/6/11.
 */
public interface ProgressRequestListener {
    void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}
