package com.example.zjl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Created by zjl on 2016/6/14.
 */
public class MycenterActivity extends Activity {

    private ImageView returnImg;
    private TextView userIdTv;
    private TextView downloadPathTv;
    private Button mLogoutBtn;

    private String userId;
    private String password;
    private String downloadPath;

    SharedPreferences preferences;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycenter);
        preferences = getSharedPreferences("login",MODE_PRIVATE);
        context = this;

        initView();
    }

    //初始化控件
    private void initView() {

        returnImg = (ImageView) findViewById(R.id.id_img_center_return_right);
        userIdTv = (TextView) findViewById(R.id.id_center_tv_id);
        downloadPathTv = (TextView) findViewById(R.id.center_download_path);
        mLogoutBtn = (Button) findViewById(R.id.id_center_logout);

    }

    //初始化数据
    private void initData() {
        userIdTv.setText(userId);
        downloadPathTv.setText(downloadPath);
        returnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,DocActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //退出登录，删除login的用户信息
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();//清除login的数据
                editor.commit();
                Intent intent = new Intent(context,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //获取本地用户信息
        userId = preferences.getString("userId","");
        password = preferences.getString("password","");
        downloadPath = Environment.getExternalStorageDirectory()+"/myLibrary/";
        File file = new File(downloadPath);
        //如果sd卡目录不存在，就创建一个
        if (!file.exists()){
            file.mkdir();
        }
        initData();
    }
}
