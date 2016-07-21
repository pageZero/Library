package com.example.zjl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.zjl.myview.CheckTextView;
import com.example.zjl.myview.FlowLayout;
import com.example.zjl.utils.ToastFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjl on 2016/6/8.
 * 用SharedPreferences保存用户登录信息
 * 存储：
 * 1.初始化SharedPreferences对象
 * 2.获取Editor对象
 * 3.调用commit()方法提交数据
 *
 */
public class TestActivity extends Activity{

    private EditText mUsernameEditView;
    private EditText mPasswordEditView;
    private Button mLoginBtn;
    private TextView mForgetTextView;
    private Spinner mRoleSpinner;

    private SharedPreferences preferences;

    private String userId = "";
    private String password = "";
    private String role = "";
    private String[] roles = new String[] {"Common","Leader","group leader"};

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        /*
        初始化SharedPreferences对象
        参数一：存储的文件名，默认以xml文件形式存储
        参数二：存储文件模式，经常以MODE_PRIVATE形式存储，表示本应用可以访问
         */
        preferences = getSharedPreferences("login",MODE_PRIVATE);
        initView();

    }

    private void initView() {

        mUsernameEditView = (EditText) findViewById(R.id.username_edit_view);
        mPasswordEditView = (EditText) findViewById(R.id.password_edit_view);
        mForgetTextView = (TextView) findViewById(R.id.forget_text_view);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userId = mUsernameEditView.getText().toString();
                password = mPasswordEditView.getText().toString();
                role = roles[mRoleSpinner.getSelectedItemPosition()];
                if ((userId.equals("")) || (password.equals("")) || (role.equals(""))) {
                    //请输入用户名和密码,选择角色
                    ToastFactory.showToast(context,"请输入用户名和密码!");
                } else {
                    login();
                }

            }
        });

        mForgetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastFactory.showToast(context,"请联系管理员修改密码！");
            }
        });
        mRoleSpinner = (Spinner) findViewById(R.id.role_spinner);
        initSpinnerData();

    }

    //初始化spinner数据
    private void initSpinnerData() {

        String[] mItems = getResources().getStringArray(R.array.roles);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        mRoleSpinner.setAdapter(adapter);
    }


    //登录
    private void login() {

        //登录验证成功之后保存用户信息
        //获取editor对象
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userId",userId);
        editor.putString("password",password);
        editor.putString("role",role);
        //提交保存数据
        editor.commit();
        Log.e("----------------","数据保存成功");


        //取出数据,第一个参数是存取的键，第二个参数-->如果该key不存在，返回默认值
        Log.e("-------------","username="+preferences.getString("userId",""));
        Log.e("-------------","password="+preferences.getString("password",""));
        Log.e("-------------","role="+preferences.getString("role",""));

    }


}
