package com.example.zjl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.zjl.bean.Document;
import com.example.zjl.utils.FileCacheUtil;
import com.example.zjl.utils.ToastFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by zjl on 2016/6/10.
 */
public class InnerStoreTestActivity extends Activity implements View.OnClickListener{

    EditText input;
    Button saveBtn;
    Button showBtn;

    private Context context;
    XXX persons;
    Gson gson = new Gson();

    String data = "{ \"people\": [" +
            "{ \"firstName\": \"Brett\", \"lastName\":\"McLaughlin\", \"email\": \"aaaa\" }," +
            "{ \"firstName\": \"Jason\", \"lastName\":\"Hunter\", \"email\": \"bbbb\"}," +
            "{ \"firstName\": \"Elliotte\", \"lastName\":\"Harold\", \"email\": \"cccc\" }" +
            "]}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_innerstore);

        context = this;
        input = (EditText) findViewById(R.id.editview);
        saveBtn = (Button) findViewById(R.id.save_btn);
        showBtn = (Button) findViewById(R.id.show_btn);
        saveBtn.setOnClickListener(this);
        showBtn.setOnClickListener(this);

        File file = new File(FileCacheUtil.getCachePath(context)+"/"+FileCacheUtil.companyDocCache);
        //获取缓存文件最后修改的时间，判断是是否从缓存读取
        long date = file.lastModified();
        long time_out = (System.currentTimeMillis() - date);
        input.setText(""+time_out/1000/60);
     //   Type type = new TypeToken<List<Person>>(){}.getType();
        persons = gson.fromJson(data,XXX.class);//从Json格式的数据中获取文件列表
       // input.setText(persons.people.toString());

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case (R.id.save_btn):
                saveInner();
                break;

            case (R.id.show_btn):
                showInner();
                break;
        }

    }

    private void showInner() {
        String cache = FileCacheUtil.getCache(context,FileCacheUtil.companyDocCache);
        persons = gson.fromJson(cache,XXX.class);//从Json格式的数据中获取文件列表
        Log.e("----------","cache内容："+cache);
        input.setText(persons.people.toString());

    }

    private void saveInner() {

        Log.e("-----------","开始");
        String inputString = input.getText().toString();
        FileCacheUtil.setCache(data,context,FileCacheUtil.companyDocCache,context.MODE_PRIVATE);

    }

    private class XXX {

        List<Person> people;

        public XXX() {

        }


    }

    public class Person {
        String firstName;
        String lastName;
        String email;

        public Person() {

        }

        @Override
        public String toString() {
            return firstName+"-"+lastName+email;
        }
    }
}
