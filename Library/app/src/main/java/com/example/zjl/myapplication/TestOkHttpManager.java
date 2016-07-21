package com.example.zjl.myapplication;

import android.util.Log;

import com.example.zjl.utils.ProgressHelper;
import com.example.zjl.utils.ProgressRequestBody;
import com.example.zjl.utils.ProgressRequestListener;
import com.example.zjl.utils.ProgressResponseListener;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zjl on 2016/6/11.
 */
public class TestOkHttpManager {

    private static TestOkHttpManager mInstance;
    private OkHttpClient mOkHttpClient;//不要创建多个客户端
    private Gson mGson;//解析返回的JSon格式

    private TestOkHttpManager() {
        mOkHttpClient = new OkHttpClient();
        mGson = new Gson();
    }

    public static TestOkHttpManager getInstance()
    {
        if (mInstance == null)
        {
            synchronized (TestOkHttpManager.class)
            {
                if (mInstance == null)
                {
                    mInstance = new TestOkHttpManager();
                }
            }
        }
        return mInstance;
    }


    public void uploadFile(File file, String urlAddress, Map params, ProgressRequestListener progressRequestListener) {

        RequestBody requestBody = bindRequestBodyForUpload(file, params);

        //封装带有进度的请求体，用于文件上传
        ProgressRequestBody progressBody = ProgressHelper.addProgressRequestListener(requestBody,progressRequestListener);

        Request request = new Request.Builder()
                .url(urlAddress)
                .post(progressBody)
                .build();
        //异步上传
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                /*
                加入UI线程
                 */
                Log.e("------------","----fileUpload fail-----"+request.body().toString()+e.toString()+e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                /*
                加入线程UI
                 */
                Log.e("-------------","----fileUpload success------"+response.body().string());

            }
        });

    }

    //绑定文件的相关参数到body
    private RequestBody bindRequestBodyForUpload(File file, Map params) {

        //用MultipartBuilder构建复杂请求体
        MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM);
        if(params != null) {
            //遍历map，添加请求参数
            Set<Map.Entry<String,String>> entrySet = params.entrySet();
            Iterator<Map.Entry<String,String>> it = entrySet.iterator();
            while(it.hasNext()) {
                Map.Entry<String,String> me = it.next();
                //拼接头部---eg:name="image" xxx.jpeg  -->
            /*
            相关参数设置:filename
                       author:zzz
                       type:doc
             */
                builder.addPart(Headers.of("Content-Disposition","form-data; name=\""+me.getKey()+"\""),
                        RequestBody.create(null,me.getValue()));
            }
        }

        if(file != null) {
            MediaType mediaType = MediaType.parse("application/octet-stream");

            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"mFile\""),
                    RequestBody.create(mediaType, file));
            Log.e("-----------------","添加文件到requestbody");
        }

        return builder.build();
    }

    /**
     * 下载文件:封装请求，访问文件-->读取响应体-->创建本地文件-->写入
     * @param targetPath 本地存储文件的地址
     * @param urlAddress 下载文件的地址
     */
    public void downloadFile(final String targetPath, final String urlAddress, ProgressResponseListener progressListener){

        Request request = new Request.Builder()
                .url(urlAddress)
                .build();
        OkHttpClient client = ProgressHelper.addProgressResponseListener(mOkHttpClient,progressListener);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //失败
                Log.e("------------","----fileDownload fail-----");
            }

            //获取响应体，写入本地文件
            @Override
            public void onResponse(Response response) throws IOException {

                Log.e("------------","----连接成功-----");

                //拼接文件的目标存储目录
                File file = new File(targetPath+getFileNameFromUrl(urlAddress));

                byte[] buffer = new byte[2048];
                int len  = 0;
                Log.e("------------","----开始下载-----");
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fos = new FileOutputStream(file);
                while ((len = inputStream.read(buffer))!=-1) {
                    fos.write(buffer, 0, len);
                }
                inputStream.close();
                fos.close();
                Log.e("------------","----结束-----");

            }
        });
    }

    /**
     * 下载时根据文件的url地址获取文件名
     * @param urlAddress
     * @return
     */
    private String getFileNameFromUrl(String urlAddress) {
        int separatorIndex = urlAddress.lastIndexOf("/");
        return (separatorIndex < 0) ? urlAddress : urlAddress.substring(separatorIndex + 1, urlAddress.length());
    }


}
