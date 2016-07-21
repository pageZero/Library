package com.example.zjl.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zjl.bean.Document;
import com.example.zjl.bean.DocumentDetail;
import com.example.zjl.bean.Tag;
import com.example.zjl.myview.MarqueeTextView;
import com.example.zjl.utils.FileCacheUtil;
import com.example.zjl.utils.OkHttpClientManager;
import com.example.zjl.utils.ToastFactory;
import com.example.zjl.utils.UIProgressResponseListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zjl on 2016/6/8.
 * 获取参数（document）,显示document的详细信息，获取文件的Url地址，下载
 * 完善：添加进度条显示
 */
public class BrowseOneActivity extends Activity implements View.OnClickListener{

    private static final String EXTRA_DOC =DocActivity.EXTRA_NET_KEY;

    private ImageView mReturnPreImg;
    private MarqueeTextView docTitleTextView;
    private TextView mDateTextView;
    private TextView mLibraryTextView;
    private TextView mAuthorTextView;
    private TextView mAbstractTextView;
    private Button downloadBtn;
    private Button deleteBtn;
    private Button modifyBtn;
    private ProgressBar mDownloadProgress;
    private RelativeLayout mProgressLayout;
    private TextView mProgressTv;

    private Document document;
    private DocumentDetail docDetail;
    private String urlAddress = "";//要下载文件的地址
    private String saveFilePath;//存储文件的地址

    private Context context;

    public static SharedPreferences preferences;
    private OkHttpClientManager mOkHttpClientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_one);
        document = (Document) getIntent().getSerializableExtra(EXTRA_DOC);

        context = this;
        preferences = getSharedPreferences("flushList",MODE_PRIVATE);
        mOkHttpClientManager = OkHttpClientManager.getInstance();

        initView();
       // initData();
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mAbstractTextView.setText(docDetail.getDocAbstract());
            } else if (msg.what == 2) {
                ToastFactory.showToast(context,"删除成功！");
            } else if(msg.what == -2) {
                ToastFactory.showToast(context,"删除失败！");
            }
        }
    };


    private void initData() {
        docTitleTextView.setText(document.getDocTitle());
        mDateTextView.setText(document.getUploadTime().toString());
        mLibraryTextView.setText(document.getLibrary());
        mAuthorTextView.setText(document.getEditor());
        saveFilePath = getSavePath();


    }

    /**
     * 初始化控件
     */
    private void initView() {
        mReturnPreImg = (ImageView) findViewById(R.id.return_pre_image_View);
        docTitleTextView = (MarqueeTextView) findViewById(R.id.show_doc_title_textView);
        mDateTextView = (TextView) findViewById(R.id.show_date_text_view);
        mLibraryTextView = (TextView) findViewById(R.id.show_library_text_view);
        mAuthorTextView = (TextView) findViewById(R.id.show_author_text_view);
        mAbstractTextView = (TextView) findViewById(R.id.show_docabstract_text_view);
        mDownloadProgress = (ProgressBar) findViewById(R.id.download_progressBar);
        mProgressLayout = (RelativeLayout) findViewById(R.id.progress_info_layout);
        mProgressTv = (TextView) findViewById(R.id.download_progress_tv);
        downloadBtn = (Button) findViewById(R.id.download_btn);
        downloadBtn.setOnClickListener(this) ;
        deleteBtn = (Button) findViewById(R.id.delete_btn);
        modifyBtn = (Button) findViewById(R.id.modify_btn);
        deleteBtn.setOnClickListener(this);
        modifyBtn.setOnClickListener(this);
        mReturnPreImg.setOnClickListener(this);

        if(!isDeletable()) {
            deleteBtn.setEnabled(false);
            deleteBtn.setBackgroundResource(R.color.text_color_title);
        }
        if(!idModifiable()) {
            modifyBtn.setEnabled(false);
            modifyBtn.setBackgroundResource(R.color.text_color_title);
        }

    }

    /**
     * 创建目标文件的存储目录
     * @return
     */
    private String getSavePath() {
        String path = Environment.getExternalStorageDirectory()+"/myLibrary/"+document.getDocTitle();
        File file = new File(path);
        //如果sd卡目录不存在，就创建一个
        if (!file.exists()){
            file.mkdir();
        }
        return path;
    }

    //事件监听
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.download_btn:
                download();
                break;
            case R.id.delete_btn:
                delete() ;
                break;
            case R.id.modify_btn:
                Intent intetn = new Intent(context,ModifyActivity.class);
                intetn.putExtra("modifyDoc",document);
                startActivity(intetn);
                break;
            case R.id.return_pre_image_View:
                Intent intent = new Intent(context,DocActivity.class);
                startActivity(intent);
                finish();
                break;
        }


    }

    //删除操作
    private void delete() {
        //弹出提示框
        final AlertDialog.Builder builder  = new AlertDialog.Builder(BrowseOneActivity.this);
        builder.setTitle("确认" ) ;
        builder.setMessage("删除操作不可撤销，确定删除？" ) ;
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("fileid",document.getId());
                        String urlAddress = "http://113.55.26.171:8088/DataBaseDesignBate/Course/Control/FileControl/RemoveFile.php";//处理删除文件的地址
                        boolean isRemoved = mOkHttpClientManager.removeDoc(params,urlAddress);
                        Message message = new Message();
                        if(isRemoved) {//删除成功
                            //更新UI
                            message.what = 2;
                            modifyPreferences();
                        } else {
                            message.what = -2;
                        }
                        handler.sendMessage(message);
                    }
                }).start();


            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder.setCancelable(true);
            }
        });
        builder.show();
    }

    //下载
    private void download() {
        String downloadUrl = "http://113.55.26.171:8088/DataBaseDesignBate/Course/Control/FileControl/DownloadFile.php";
        mProgressLayout.setVisibility(View.VISIBLE);
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
                if(bytesRead == contentLength || done) {//下载完
                    mProgressLayout.setVisibility(View.GONE);
                    ToastFactory.showToast(context,"下载完成!");
                    //缓存下载文件的信息到本地
                    Gson mGson = new Gson();
                    Type type = new TypeToken<List<Document>>() {}.getType();
                    List<Document> docs = new ArrayList<>();
                    docs.add(document);
                    String cacheDownloadInfo = mGson.toJson(docs,type);
                    FileCacheUtil.setCache(cacheDownloadInfo,context,FileCacheUtil.downloadDocCache,context.MODE_APPEND);
                } else {
                    mDownloadProgress.setProgress((int) ((100 * bytesRead) / contentLength));
                    mProgressTv.setText((100 * bytesRead) / contentLength + "%");
                }
            }
        };
        Map<String, String> params = new HashMap<>();
        params.put("fileid",document.getId());
        mOkHttpClientManager.downloadFile(params ,saveFilePath, downloadUrl, listener);
    }

    //判断是否可修改
    private boolean isDeletable() {
        /*
        Map<String, String> params = new HashMap<String, String>();
        params.put("docId",document.getId());
        String urlAddress = "";//处理删除文件的地址
//        return mOkHttpClientManager.queryRemovable(params,urlAddress);*/

        return getUserRole().equals("Admin");
    }

    //判断是否可修改
    private boolean idModifiable() {
        /*
        Map<String, String> params = new HashMap<String, String>();
        params.put("docId",document.getId());
        String urlAddress = "";//处理删除文件的地址
        return mOkHttpClientManager.queryModifiable(params,urlAddress);
        */
        return isDeletable();
    }


    //删除了一个文档，需要重新刷新列表显示
    private void modifyPreferences() {
        //获取editor对象
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("libraryType",document.getLibrary());
        editor.putBoolean("isModify",true);
        //提交保存数据
        editor.commit();
        Log.e("----------------","数据保存成功");
    }

    //查询文档摘要
    private void initDetail() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String findOneUrl = "http://113.55.26.171:8088/DataBaseDesignBate/Course/Control/FileControl/GetOneFile.php";
                String result = PostTest.sendPost(findOneUrl, "fileid=" + document.getId());
                Log.e("-----------","result="+result);
                Map<String, String> params = new HashMap<>();
                params.put("fileid",document.getId());
                docDetail = mOkHttpClientManager.queryOne(params, findOneUrl);

                //更新UI
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }).start();
    }

    private String getUserRole() {
        SharedPreferences userPreferences = getSharedPreferences("login",MODE_PRIVATE);
        return userPreferences.getString("role","");
    }
    @Override
    protected void onResume() {
        super.onResume();
        initData();
        initDetail();
    }

}
