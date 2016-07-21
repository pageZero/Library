package com.example.zjl.utils;


import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * 进度回调辅助类
 * Created by zjl on 2016/6/11.
 */
public class ProgressHelper {

    /**
     * 重写的ResponseBody，添加到okHttpClient
     * @param client 带包装的client
     * @param progressListener 进度监听器
     * @return
     */
    public static OkHttpClient addProgressResponseListener(OkHttpClient client, final ProgressResponseListener progressListener) {

        //克隆，防止内存溢出
        OkHttpClient cloneClient = client.clone();
        //增加拦截器
        cloneClient.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //拦截
                Response originalResponse = chain.proceed(chain.request());
                //包装响应体并返回
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        });
        return cloneClient;
    }

    /**
     * 包装请求体用于上传文件的回调
     * @param requestBody
     * @param progressRequestListener
     * @return
     */
    public static ProgressRequestBody addProgressRequestListener(RequestBody requestBody, ProgressRequestListener progressRequestListener){
        //包装请求体
        return new ProgressRequestBody(requestBody,progressRequestListener);
    }
}
