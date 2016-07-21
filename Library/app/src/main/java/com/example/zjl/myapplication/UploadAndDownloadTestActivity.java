package com.example.zjl.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.zjl.utils.UIProgressRequestListener;
import com.example.zjl.utils.UIProgressResponseListener;

import java.io.File;

/**
 * Created by zjl on 2016/6/11.
 */
public class UploadAndDownloadTestActivity extends Activity implements View.OnClickListener{

    Button uploadBtn;
    Button downloadBtn;
    ProgressBar uploadProgressBar;
    ProgressBar downloadProgressBar;

    TestOkHttpManager manager;
    private static final int FILE_SELECT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_download_test);

        manager = TestOkHttpManager.getInstance();

        uploadBtn = (Button) findViewById(R.id.upload);
        downloadBtn = (Button) findViewById(R.id.download);
        uploadProgressBar = (ProgressBar) findViewById(R.id.upload_progress);
        downloadProgressBar = (ProgressBar) findViewById(R.id.download_progress);

        uploadBtn.setOnClickListener(this);
        downloadBtn.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload:
                upload();
                break;

            case R.id.download:
                download();
                break;
        }
    }

    private void download() {
        UIProgressResponseListener listener = new UIProgressResponseListener() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                Log.e("TAG", "bytesRead:" + bytesRead);
                Log.e("TAG", "contentLength:" + contentLength);
                Log.e("TAG", "done:" + done);
                if (contentLength != -1) {
                    //长度未知的情况下回返回-1
                    Log.e("TAG", (100 * bytesRead) / contentLength + "% done");
                }
                Log.e("TAG", "================================");
                //ui层回调
                downloadProgressBar.setProgress((int) ((100 * bytesRead) / contentLength));
            }
        };
        String saveFilePath = getSavePath();
        String urlAddress = "http://file.bmob.cn/M02/27/39/oYYBAFaBWGqAMlgDAAAlTbpgQPA404.jpg";
        manager.downloadFile(saveFilePath, urlAddress,listener);




    }

    private void upload() {

        UIProgressRequestListener listener = new UIProgressRequestListener() {
            @Override
            public void onUIRequestProgress(long bytesWrite, long contentLength, boolean done) {
                Log.e("TAG", "bytesWrite:" + bytesWrite);
                Log.e("TAG", "contentLength" + contentLength);
                Log.e("TAG", (100 * bytesWrite) / contentLength + " % done ");
                Log.e("TAG", "done:" + done);
                Log.e("TAG", "================================");
                //ui层回调
                uploadProgressBar.setProgress((int) ((100 * bytesWrite) / contentLength));
                //Toast.makeText(getApplicationContext(), bytesWrite + " " + contentLength + " " + done, Toast.LENGTH_LONG).show();
            }
        };
        String filePath = "/storage/emulated/0/AL812_test_sensor_0.ini";
        String uploadUrl = "http://www.maizitime.com:8081/upload_test";
        manager.uploadFile(new File(filePath),uploadUrl,null,listener);
    }

    private String getSavePath() {
        String path = Environment.getExternalStorageDirectory()+"/myLibrary/";
        File file = new File(path);
        //如果sd卡目录不存在，就创建一个
        if (!file.exists()){
            file.mkdir();
        }
        return path;
    }

}
