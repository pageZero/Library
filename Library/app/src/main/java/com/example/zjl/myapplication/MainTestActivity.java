package com.example.zjl.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.zjl.bean.Document;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjl on 2016/6/14.
 */
public class MainTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_main_test);

        String data = "[" +
                "{\"id\" : \"0\",\"DocTitle\" : \"数据库系统概念000\",\"UploadTime\" : \"2016-6-5 1:02\", \"Editorname\" : \"John Day\", \"filesize\" : \"10MB\", \"ViewCount\" : \"10086\", \"DownloadCount\" : \"10\" ,\"Library\" : \"CL\"}," +
                "{" +
                "\n\"id\" : \"1\"," +
                "\"DocTitle\" : \"数据库系统概念\", " +
                "\"UploadTime\" : \"2016-6-5 1:02\", " +
                "\"Editorname\" : \"John Day\", " +
                "\"filesize\" : \"10MB\", " +
                "\"ViewCount\" : \"10086\", " +
                "\"DownloadCount\" : \"10\" ," +
                "\"Library\" : \"DL\"" +
                "}," +
                "{" +
                "\"id\" : \"2\", " +
                "\"DocTitle\" : \"JavaScript从入门到放弃\", " +
                "\"UploadTime\" : \"2016-6-5 1:02\", " +
                "\"Editorname\" : \"John Day\", " +
                "\"filesize\" : \"10MB\", " +
                "\"ViewCount\" : \"10086\", " +
                "\"DownloadCount\" : \"10\"," +
                "    \"Library\" : \"PL\"" +
                "}," +
                "{" +
                "\"id\" : \"573c802bbf04ec30fcf356b1\", " +
                "\"DocTitle\" : \"JavaScript从入门到懵逼\", " +
                "\"UploadTime\" : \"2016-6-5 1:02\", " +
                "\"Editorname\" : \"John Day\", " +
                "\"filesize\" : \"10MB\", " +
                "\"ViewCount\" : \"10086\", " +
                "\"DownloadCount\" : \"10\" ," +
                "\"Library\" : \"DL\"" +
                "}]";
        String data1 = "[{\"id\":\"573c802bbf04ec30fcf356b1\"," +
                "\"DocTitle\":\"ceshi.docx\"," +
                "\"UploadTime\":\"2016\",\"Editorname\":null," +
                "\"filesize\":\"0.03\",\"ViewCount\":69," +
                "\"DownloadCount\":9,\"Library\":\"DL\"}," +
                "{\"id\":\"575faec4234815f003000032\",\"DocTitle\":\"123\",\"UploadTime\":\"16\\/06\\/14\",\"Editorname\":\"110\",\"filesize\":\"0.07\",\"ViewCount\":2,\"DownloadCount\":3,\"Library\":\"DL\"},{\"id\":\"575fd627234815b81b000033\",\"DocTitle\":\"1111\",\"UploadTime\":\"16\\/06\\/14\",\"Editorname\":\"123\",\"filesize\":\"0.02\",\"ViewCount\":1,\"DownloadCount\":1,\"Library\":\"PL\"}]";
        Type type = new TypeToken<List<Document>>(){}.getType();
        Gson gson = new Gson();
        List<Document> docs = new ArrayList<>();
        docs = gson.fromJson(data1,type);
        Log.e("--------------",docs.toString());


    }
}
