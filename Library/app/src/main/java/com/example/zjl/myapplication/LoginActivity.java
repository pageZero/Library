package com.example.zjl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import com.example.zjl.bean.User;
import com.example.zjl.utils.OkHttpClientManager;
import com.example.zjl.utils.ToastFactory;
import com.google.gson.Gson;
;import java.util.HashMap;
import java.util.Map;

/**
 * Created by zjl on 2016/6/5.
 */
public class LoginActivity extends Activity {

    private EditText mUsernameEditView;
    private EditText mPasswordEditView;
    private Button mLoginBtn;
    private TextView mForgetTextView;
    private Spinner mRoleSpinner;

    private String userId = "";
    private String password = "";
    private String role = "";
    private String urlAddress = "http://113.55.26.171:8088/DataBaseDesignBate/Course/AppControl/LogControlInApp.php";
    private String[] roles; //getResources().getStringArray(R.array.roles_en);
    private User user;

    private Context context;
    public static SharedPreferences preferences;


    OkHttpClientManager httpClientManager;//httpl连接管理

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        
        preferences = getSharedPreferences("login",MODE_PRIVATE);
        roles =  getResources().getStringArray(R.array.roles_en);
        /*
        初始化SharedPreferences对象
        参数一：存储的文件名，默认以xml文件形式存储
        参数二：存储文件模式，经常以MODE_PRIVATE形式存储，表示本应用可以访问
         */

        if (isLogin()) {//用户已经登录过，直接跳到主页
            Intent intent = new Intent(context,DocActivity.class);
            startActivity(intent);
            this.finish();
        } else {//第一次进入应用，登录
            httpClientManager = OkHttpClientManager.getInstance();
            initView();
        }
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == -1){
                Log.e("---------------","更新UI");
                ToastFactory.showToast(context,"登录失败，请检查网络连接！");
            } else if(msg.what == 0) {
                ToastFactory.showToast(context,"用户名或密码错误！");
            }
        }
    };

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

        String[] mItems =roles;// getResources().getStringArray(R.array.roles);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        mRoleSpinner.setAdapter(adapter);
    }

    //登录
    private void login() {
        final Map<String, String> params = new HashMap<>();
        params.put("username", userId);
        params.put("password", password);
        params.put("role",role);
        Log.e("--------------","userId="+userId+"password="+password+"role="+role);
        //更新UI
        final Message message = new Message();
        new Thread(new Runnable() {//网络请求放在异步线程中
            @Override
            public void run() {
       /*        String result = PostTest.sendPost(urlAddress,"username="+userId+"&"+"password="+password+"&"+"role="+role);
                Log.e("-------------",result);
                if(result.equals("false")) {
                    message.what = 0;
                    handler.sendMessage(message);
                } else if (result.equals("")) {
                    message.what = -1;
                    handler.sendMessage(message);
                } else {
                    Gson gson = new Gson();
                    user = gson.fromJson(result,User.class);
                    saveLoginParams();
                    Log.e("-------------","-------------"+user.getId().getId()+","+user.getUserId());
                }*/
                //登录验证
                String result = PostTest.sendPost(urlAddress,"username="+userId+"&"+"password="+password+"&"+"role="+role);
                Log.e("-------------",result);
                user = httpClientManager.loginCheck(urlAddress, params);
                if(user == null) {//说明登录失败
                    message.what = 0;
                    handler.sendMessage(message);
                } else {//获取到用户信息
                    saveLoginParams();
                    Log.e("-------------","登录成功");
                    Intent intent = new Intent(context,DocActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }).start();
    }

    //登录验证成功之后保存用户信息
    private void saveLoginParams() {

        //获取editor对象
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("id",user.getId().getId());
        editor.putString("userId",user.getUserId());
        editor.putString("username",user.getUserName());
        editor.putString("password",user.getPassword());
        editor.putString("role",user.getUserRole());
        //提交保存数据
        editor.commit();
        Log.e("----------------","数据保存成功");
    }

    //判断用户是否已经登录，
    private boolean isLogin() {
        //取出数据,第一个参数是存取的键，第二个参数-->如果该key不存在，返回默认值
        String userId = preferences.getString("username","");
        String password = preferences.getString("password","");
        String role = preferences.getString("role","");
        return (userId.equals("") || password.equals("") || role.equals("")?false:true);
    }

}
