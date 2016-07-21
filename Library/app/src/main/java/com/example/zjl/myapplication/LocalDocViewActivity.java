package com.example.zjl.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zjl.bean.Document;
import com.example.zjl.utils.ToastFactory;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.io.File;

import static java.lang.String.format;

/**
 * Created by zjl on 2016/6/11.
 */
public class LocalDocViewActivity extends Activity {

    public static final String EXTRA_LOCAL_PDF_NAME = DocActivity.EXTRA_LOCAL_KEY;

    private ImageView returnPreImg;
    private TextView mTitleTv;
    private PDFView mPdfView;
    private ProgressDialog mProgressDialog;

    private String pdfName;
    private Document document;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        document = (Document) getIntent().getSerializableExtra(EXTRA_LOCAL_PDF_NAME);
        pdfName = document.getDocTitle();
        initView();

        readPdf();
    }

    private void initView() {
        returnPreImg = (ImageView) findViewById(R.id.return_pre_image_View);
        mTitleTv = (TextView) findViewById(R.id.show_doc_title_textView);
        mTitleTv.setText(pdfName);
        mPdfView = (PDFView) findViewById(R.id.pdf_view);
        returnPreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("-------------","返回上一页");
            }
        });
    }

    /**
     * 读取本地的PDF文件
     */
    private void readPdf() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String pdfUrl = Environment.getExternalStorageDirectory()+"/myLibrary/"+pdfName;//pdf文件的路径
            if(!new File(pdfUrl).exists()) {
                ToastFactory.showToast(context,"该文件已被删除，无法查看！");
                return;
            }
            Log.e("-------------","pdfUrl-->"+pdfUrl);
            File pdfFile = new File(pdfUrl);
            mProgressDialog = new ProgressDialog(LocalDocViewActivity.this);
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.show();
            if(pdfFile.exists()) {//文件存在
                Log.e("-------------","PDF文件存在");
                loadPDFView(pdfFile);
            }
        } else {
            Log.e("-------------","没有sd卡");
        }

    }

    /**
     * 加载文件到视图
     * @param pdfFile
     */
    private void loadPDFView(File pdfFile) {
        mPdfView.setFitsSystemWindows(true);
        mPdfView.setFocusableInTouchMode(false);

        mPdfView.fromFile(pdfFile)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        mProgressDialog.dismiss();//隐藏加载的进度条
                    }
                })
                .swipeVertical(true)//设置pdf文档垂直翻页，默认是左右滑动翻页
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        Log.e("-------------","pageWidth:"+pageWidth+"-------pageHeight"+pageHeight);
                    }
                })
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        mTitleTv.setText(format("%s %s / %s", pdfName, page, pageCount));
                    }
                })
                .defaultPage(1)
                .showMinimap(true)//是否显示缩放小地图
                .load();
    }
}
