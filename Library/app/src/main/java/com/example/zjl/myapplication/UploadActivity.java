package com.example.zjl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zjl.bean.Tag;
import com.example.zjl.myview.CheckTextView;
import com.example.zjl.myview.FlowLayout;
import com.example.zjl.myview.MarqueeTextView;
import com.example.zjl.utils.OkHttpClientManager;
import com.example.zjl.utils.ToastFactory;
import com.example.zjl.utils.UIProgressRequestListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zjl on 2016/6/7.
 *完善：键盘出现的时候升高EditView的位置或者升高整个布局
 * 
 *
 */
public class UploadActivity extends Activity implements View.OnClickListener{

    private ImageView returnPreImg;
    private MarqueeTextView top_bar_title_tv;
    private LinearLayout mSelectedFileLayout;
    private TextView mSelectedFileTextView;
    private Spinner mLibrarySpinner;
    private EditText mDocAbstractEditView;
    private Button mUploadButton;
    private FlowLayout mTagsFlowLayout;
    private ProgressBar mUploadProgress;
    private LinearLayout mProgressLayout;
    private TextView mProgressTv;

    private Context context;
    private String userId;
    OkHttpClientManager mOkHttpClientManager;

    private String flag;
    private List<CheckTextView> checkTextViews;
    private static final int FILE_SELECT_CODE = 1;
    private String filePath = "";
    private String uploadUrl = "http://113.55.26.171:8088/DataBaseDesignBate/Course/Control/FileControl/UpLoadFile.php";
    private String[] library_en;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        context = this;
        mOkHttpClientManager = OkHttpClientManager.getInstance();
        checkTextViews = new ArrayList<>();
        initView();
        initFlaowLayoutData();
        spinnerSetting();

    }

    private void initFlaowLayoutData() {
        //获取tag缓存
        Gson mGson = new Gson();
        Type type = new TypeToken<List<Tag>>() {}.getType();
        SharedPreferences tagPreferences = getSharedPreferences("tags", MODE_PRIVATE);
        String tagsCache = tagPreferences.getString("tags", "");
        List<Tag> tags = mGson.fromJson(tagsCache, type);

        //动态初始化标签显示的数据
        LayoutInflater mInflater = LayoutInflater.from(this);
        for(int i = 0; i < tags.size() ; i++) {

            CheckTextView tv = (CheckTextView) mInflater.inflate(R.layout.text_view,
                    mTagsFlowLayout,false);
            tv.setText(tags.get(i).getName());
            mTagsFlowLayout.addView(tv);
            checkTextViews.add(tv);
        }
    }

    /**
     * 为spinner添加适配器
     */
    private void spinnerSetting() {
        // 建立数据源
        final String[] mItems = getResources().getStringArray(R.array.librarys);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        mLibrarySpinner.setAdapter(adapter);
    }

    private void initView() {
        mSelectedFileLayout = (LinearLayout) findViewById(R.id.select_file_url_layout);
        //设置点击事件，点击打开文件管理器
        mSelectedFileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseFile();
            }
        });
        mSelectedFileTextView = (TextView) findViewById(R.id.select_file_url_text_view);
        //选中文件之后显示文件路径

        mLibrarySpinner = (Spinner) findViewById(R.id.library_spinner);

        mDocAbstractEditView = (EditText) findViewById(R.id.doc_abstract_edit_view);
        mTagsFlowLayout = (FlowLayout) findViewById(R.id.tags_flow_layout);
        mUploadButton = (Button) findViewById(R.id.upload_btn);
        mUploadButton.setOnClickListener(this);
        mUploadProgress = (ProgressBar) findViewById(R.id.upload_progress);
        mProgressLayout = (LinearLayout) findViewById(R.id.progress_info_layout);
        mProgressTv = (TextView) findViewById(R.id.upload_progress_tv);

        returnPreImg = (ImageView) findViewById(R.id.return_pre_image_View);
        top_bar_title_tv = (MarqueeTextView) findViewById(R.id.show_doc_title_textView);
        top_bar_title_tv.setText("上传文件");
        returnPreImg.setOnClickListener(this);

    }

    //上传文件
    private void uploadFile() {
     //   String library = mLibrarySpinner.getSelectedItem().toString();
        flag = library_en[mLibrarySpinner.getSelectedItemPosition()];
        String docAbstract = mDocAbstractEditView.getText().toString();
        List<String> tags = new ArrayList<String>();
        //遍历checkTextViews，取出被选中的checkTextView
        for (CheckTextView checkTextView : checkTextViews) {
            if(checkTextView.isSelected()) {
                tags.add((String) checkTextView.getText());
            }
        }
        StringBuffer sBuf = new StringBuffer();
        //标签用,号分隔
        for(int i = 0; i<tags.size(); i++) {
            if(i != (tags.size() - 1)) {
                sBuf.append(tags.get(i)+",");
            } else {
                sBuf.append(tags.get(i));
            }
        }
        if(userId.equals("")) {
            userId = getUserInfo();
        }
        if(userId.equals("")|| getFileName().equals("")|| flag.equals("")|| docAbstract.equals("")||sBuf.toString().equals("")||filePath.equals("")) {
            ToastFactory.showToast(context,"请选择必要信息!");
            return;
        }
        //显示上传的进度控件
        mProgressLayout.setVisibility(View.VISIBLE);

        Map<String, String> params = new HashMap<String, String>();
        params.put("MainUserId",userId);
        params.put("DocTitle",getFileName());
        params.put("fileLB",flag);
        params.put("fileAbstract",docAbstract);
        params.put("fileTags",sBuf.toString());
        Log.e("----------------","library="+flag+"...docAbstract="+docAbstract+"...tags"+tags);

        //设置监听器
        UIProgressRequestListener listener = new UIProgressRequestListener() {
            @Override
            public void onUIRequestProgress(long bytesWrite, long contentLength, boolean done) {
                Log.e("TAG", "bytesWrite:" + bytesWrite);
                Log.e("TAG", "contentLength" + contentLength);
                Log.e("TAG", (100 * bytesWrite) / contentLength + " % done ");
                Log.e("TAG", "done:" + done);
                Log.e("TAG", "================================");
                //ui层回调
                if(bytesWrite == contentLength || done) {//上传成功
                    mProgressLayout.setVisibility(View.GONE);
                } else {
                    mUploadProgress.setProgress((int) ((100 * bytesWrite) / contentLength));
                    mProgressTv.setText(((100 * bytesWrite) / contentLength)+"%");
                    //Toast.makeText(getApplicationContext(), bytesWrite + " " + contentLength + " " + done, Toast.LENGTH_LONG).show();

                }
                mUploadProgress.setProgress((int) ((100 * bytesWrite) / contentLength));
                //Toast.makeText(getApplicationContext(), bytesWrite + " " + contentLength + " " + done, Toast.LENGTH_LONG).show();
            }
        };
        mOkHttpClientManager.uploadFile(new File(filePath),uploadUrl,params,listener);

    }

    //选择文件
    private void choseFile() {
        //初始化Intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//不筛选任何类型
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //启动一个Intent并接受返回的结果，要对返回结果进行解析
        //   startActivityForResult(intent,1);
        try {
            startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    Uri uri = data.getData();//带回选中的文件数据
                    filePath = uri.getPath();//获取文件路径
                    mSelectedFileTextView.setText(filePath.toString());
                    Log.e("------TAG---------","filePath:"+filePath);
                    Log.e("------TAG---------","打开文件成功");
                    Toast.makeText(context,"成功",Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_btn:
                uploadFile();
                break;
            case R.id.return_pre_image_View:
                Intent intent = new Intent(context,DocActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        library_en = getResources().getStringArray(R.array.librarys_en);
        userId = getUserInfo();

    }

    private String getUserInfo() {
        //从本地获取用户id
        SharedPreferences userPreferences = getSharedPreferences("login",MODE_PRIVATE);
        return userPreferences.getString("id","");
    }

    private String getFileName() {
        int separatorIndex = filePath.lastIndexOf("/");
        return (separatorIndex < 0) ? filePath : filePath.substring(separatorIndex + 1, filePath.length());
    }

/*
<b>Notice</b>:  Undefined index: fileContent in <b>E:\Xampp\htdocs\DataBaseDesignBate\Course\Control\FileControl\UpLoadFile.php</b> on line <b>26</b><br />
                                                                              <br />
                                                                              <b>Notice</b>:  Undefined index: fileContent in <b>E:\Xampp\htdocs\DataBaseDesignBate\Course\Control\FileControl\UpLoadFile.php</b> on line <b>29</b><br />
                                                                              <br />
                                                                              <b>Notice</b>:  Undefined offset: 1 in <b>E:\Xampp\htdocs\DataBaseDesignBate\Course\Control\FileControl\UpLoadFile.php</b> on line <b>31</b><br />
                                                                              <br />
 */


}
