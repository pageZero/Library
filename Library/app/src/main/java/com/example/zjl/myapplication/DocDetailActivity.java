package com.example.zjl.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zjl.bean.Document;
import com.example.zjl.myview.MarqueeTextView;
import com.example.zjl.utils.FileCacheUtil;
import com.example.zjl.utils.OkHttpClientManager;
import com.example.zjl.utils.ToastFactory;
import com.example.zjl.utils.UIProgressResponseListener;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zjl on 2016/6/14.
 */
public class DocDetailActivity extends Activity implements View.OnClickListener{
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
        initData();
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!isDeletable()) {
                    deleteBtn.setEnabled(false);
                }
                if(!idModifiable()) {
                    modifyBtn.setEnabled(false);
                }
            }
        }).start();

    }

    private void initData() {
        docTitleTextView.setText(document.getDocTitle());
        mDateTextView.setText(document.getUploadTime());
        mLibraryTextView.setText(document.getLibrary());
        mAuthorTextView.setText(document.getEditor());
     //   mAbstractTextView.setText(document.getDocAbstract());
        saveFilePath = getSavePath();
    }



    /**
     * 创建目标文件的存储目录
     * @return
     */
    private String getSavePath() {
        String path = Environment.getExternalStorageDirectory()+"/myLibrary/";
        File file = new File(path);
        //如果sd卡目录不存在，就创建一个
        if (!file.exists()){
            file.mkdir();
        }
        return path;
    }

    //判断是否可修改
    private boolean isDeletable() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("docId",document.getId());
        String urlAddress = "";//处理删除文件的地址
        return mOkHttpClientManager.queryRemovable(params,urlAddress);
    }

    //判断是否可修改
    private boolean idModifiable() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("docId",document.getId());
        String urlAddress = "";//处理删除文件的地址
        return mOkHttpClientManager.queryModifiable(params,urlAddress);
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
                returnPre();
                break;
        }
    }

    private void returnPre() {
        Intent intent = new Intent(context,DocActivity.class);
        startActivity(intent);
        finish();
    }
    //下载
    private void download() {

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
                    //缓存下载文件的信息到本地
                    Gson mGson = new Gson();
                    String cacheDownloadInfo = mGson.toJson(document,Document.class);
                    FileCacheUtil.setCache(cacheDownloadInfo,context,FileCacheUtil.downloadDocCache,context.MODE_APPEND);
                } else {
                    mDownloadProgress.setProgress((int) ((100 * bytesRead) / contentLength));
                    mProgressTv.setText((100 * bytesRead) / contentLength + "%");
                }
            }
        };
     //   mOkHttpClientManager.downloadFile(saveFilePath, urlAddress, listener);
    }
    //删除操作
    private void delete() {
        //弹出提示框
        final AlertDialog.Builder builder  = new AlertDialog.Builder(DocDetailActivity.this);
        builder.setTitle("确认" ) ;
        builder.setMessage("删除操作不克撤销，确定删除？" ) ;
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Map<String, String> params = new HashMap<String, String>();
                params.put("docId",document.getId());
                String urlAddress = "";//处理删除文件的地址
                boolean isRemoved = mOkHttpClientManager.removeDoc(params,urlAddress);
                if(isRemoved) {//删除成功
                    ToastFactory.showToast(context,"删除成功");
                    //修改列表更新标签sharedPreference
                    modifyPreferences();
                    //返回上一页
                }
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

}
