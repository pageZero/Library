package com.example.zjl.utils;


import android.util.Log;


import com.example.zjl.bean.Document;
import com.example.zjl.bean.DocumentDetail;
import com.example.zjl.bean.DocumentList;
import com.example.zjl.bean.DocumentUrlInfo;
import com.example.zjl.bean.Tag;
import com.example.zjl.bean.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zjl on 2016/5/28.
 * 提供网络请求:
 *            1.登录
 *            2.文件列表查询
 *            3.单个文件浏览（根据文件id获取）
 *            4.用户信息查询（用户名和密码缓存到本地，根据用户名查询）
 *            5.文件上传（加入UI线程）
 *            6.文件下载（加入UI线程）
 *
 * 单例（由一个管理器提供所有的网络请求，只有一个OkHttpClient客户端处理所有的网络请求）
 */
public class OkHttpClientManager {

    private static OkHttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;//不要创建多个客户端
    private Gson mGson;//解析返回的JSon格式

    private OkHttpClientManager() {
        mOkHttpClient = new OkHttpClient();
        mGson = new Gson();
    }

    public static OkHttpClientManager getInstance()
    {
        if (mInstance == null)
        {
            synchronized (OkHttpClientManager.class)
            {
                if (mInstance == null)
                {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 登录验证
     * @param urlAddress 请求地址
     * @param params 请求的参数，存储在Map集合中
     * @return response
     */
    public User loginCheck(final String urlAddress, final Map params) {
        RequestBody formBody = bindRequestBodyForMap(params);
        //请求地址和请求体封装
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(formBody)
                .build();
        Response response = null;
        try {
            //同步请求
            Log.e("------------","发送请求");
            response = mOkHttpClient.newCall(request).execute();
            Log.e("-------------",""+response.isSuccessful());
            if (response.isSuccessful()) {
                Log.e("------------","header:"+response.headers().toString());
                User user = mGson.fromJson(response.body().charStream(),User.class);
                Log.e("-------------","result:"+user.getId().getId()+","+user.getUserId());
                return user;
            } else {
                Log.e("------------","----fail-----");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("--------------",""+e.toString());
        }
        return null;

    }

    //绑定map集合存储参数到Bulider
    private RequestBody bindRequestBodyForMap(Map<String, String> params) {

        //创建from表单请求体的builder，封装请求参数
        FormEncodingBuilder builder = new FormEncodingBuilder();

        Set<Map.Entry<String,String>> entrySet = params.entrySet();
        Iterator<Map.Entry<String,String>> it = entrySet.iterator();
        while(it.hasNext()) {
            Map.Entry<String,String> me = it.next();
            builder.add(me.getKey(),me.getValue());
        }
        //返回和HTML的form一样的表单请求体
        return builder.build();
    }


    /**
     * 文件上传，模拟网页端文件上传，post请求
     * @param file 目标文件
     * @param urlAddress  url地址
     * @param params 文件相关的参数
     * @param progressRequestListener 请求进度监听器
     * @return
     */
    public void uploadFile(File file, String urlAddress, Map<String,String> params, ProgressRequestListener progressRequestListener) {

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
    private RequestBody bindRequestBodyForUpload(File file, Map<String,String> params) {

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
     * @param urlAddress 请求地址
     * @param progressListener 下载进度监听器
     */
    public void downloadFile(Map<String,String> params,final String targetPath, final String urlAddress, ProgressResponseListener progressListener){

        RequestBody formBody = bindRequestBodyForMap(params);
        Request request = new Request.Builder()
                .post(formBody)
                .url(urlAddress)
                .build();
        //重写responseBody，带有进度，使用拦截器将重写的responseBody加载到okHttpClient中
        //获取重新加载responseBody的OkHttpClient
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
                File file = new File(targetPath);

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


    /**Json
     * 查询文档列表(所有的文档列表查询都用这个方法)
     * 注：分页加载：如果只告诉服务器给我返回几条数据，服务器会知道怎么跳过之前查询到的吗？？？？设计到请求参数的封装的问题
     * @param params 请求的参数   注意：请求的参数中还要再加入requestDocNum的键和值,pageNum的键值
     * @param urlAddress 请求的地址  同步
     * @return 返回查询到的文档列表
     */
    public List<Document> queryDocs(Map<String,String> params, String urlAddress) {
        List<Document> documents = new ArrayList<>();
        RequestBody formBody = bindRequestBodyForMap(params);
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(formBody)
                .build();
        //发送一个同步请求
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.e("------------","head:"+response.headers().toString());
                Type type = new TypeToken<List<Document>>(){}.getType();
                documents = mGson.fromJson(response.body().charStream(),type);//从Json格式的数据中获取文件列表
                Log.e("-----------","获取到数据:"+documents.toString());
            } else {
                Log.e("------------------","联网失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("------------------","失败"+e.toString());
        }

        return documents;
    }


    /**
     * 查询是否具有删除权限：
     * 返回参数：1：可删除   0；不可删除
     * 请求参数：文件id
     */
    public boolean queryRemovable(Map<String, String> params, String urlAddress) {
        RequestBody formBody = bindRequestBodyForMap(params);
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(formBody)
                .build();
        Response response = null;
        boolean isRemovable = false;
        try {
            //同步请求
            response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String code = response.header("isRemovable");//提取响应头的数据
                if(code.equals("1")){
                    Log.e("-------------------","删除权限验证成功:"+code);
                    isRemovable = true;
                }
            } else {
                Log.e("------------","----fail-----");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isRemovable;
    }

    /**
     * 删除文档：
     * 返回参数：1：删除成功   0；删除失败
     * 请求参数：文件id
     */
    public boolean removeDoc(Map<String, String> params, String urlAddress) {
        RequestBody formBody = bindRequestBodyForMap(params);
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(formBody)
                .build();
        Response response = null;
        boolean isRemovable = false;
        try {
            //同步请求
            response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String code = response.header("isRemove");//提取响应头的数据
                if(code.equals("true")){
                    Log.e("-------------------","删除成功:"+code);
                    isRemovable = true;
                }
            } else {
                Log.e("------------","----fail-----");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isRemovable;
    }

    /**
     * 删除文档：
     * 返回参数：1：删除成功   0；删除失败
     * 请求参数：文件id
     */
    public boolean modifyDoc(Map<String, String> params, String urlAddress) {
        RequestBody formBody = bindRequestBodyForMap(params);
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(formBody)
                .build();
        Response response = null;
        boolean isRemovable = false;
        try {
            //同步请求
            response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String code = response.header("isModify");//提取响应头的数据
                if(code.equals("1")){
                    Log.e("-------------------","修改成功:"+code);
                    isRemovable = true;
                }
            } else {
                Log.e("------------","----fail-----");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isRemovable;
    }

    /**
     * 查询是否具有修改权限
     * 参数文件id
     */
    public boolean queryModifiable(Map<String, String> params, String urlAddress) {

        RequestBody formBody = bindRequestBodyForMap(params);
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(formBody)
                .build();
        Response response = null;
        boolean isModifiable = false;
        try {
            response = mOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.isSuccessful()) {
                String code = response.header("isModifiable");//提取响应头的数据
                if(code.equals("1")){
                    Log.e("-------------------","修改权限验证成功:"+code);
                    isModifiable = true;
                } else {
                    Log.e("------------", "----fail-----");
                }
            }
        return isModifiable;
    }

    //查询标签
    public List<Tag> queryTags(String urlAddress) {
        List<Tag> tags = new ArrayList<>();
        RequestBody formBody = new FormEncodingBuilder()
                .add("skipcount","0")
                .build();
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(formBody)
                .build();
        //发送一个同步请求
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Type type = new TypeToken<List<Tag>>(){}.getType();
                tags = mGson.fromJson(response.body().charStream(),type);//从Json格式的数据中获取文件列表
                Log.e("-----------","result:"+"获取到数据");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tags;
    }

    public DocumentDetail queryOne(Map<String, String> params, String urlAddress) {
        RequestBody formBody = bindRequestBodyForMap(params);
        //请求地址和请求体封装
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(formBody)
                .build();
        Response response = null;
        try {
            //同步请求
            response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.e("------------","header:"+response.headers().toString());
                DocumentDetail docuument = mGson.fromJson(response.body().charStream(),DocumentDetail.class);
                return docuument;
            } else {
                Log.e("------------","----fail-----");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("--------------",""+e.toString());
        }
        return null;

    }

}

