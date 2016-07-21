package com.example.zjl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.zjl.bean.Document;
import com.example.zjl.utils.OkHttpClientManager;
import com.example.zjl.utils.ToastFactory;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zjl on 2016/6/12.
 */
public class ModifyActivity extends Activity implements View.OnClickListener{

    public static final String EXTRA_MODIFY_DOC = "modifyDoc";
    private Document document;

    private EditText mTitleEt;
    private Spinner mLibrarySpinner;
    private EditText mAbstractEt;
    private Button mSaveBtn;

    private Context context;

    private String[] library_en = getResources().getStringArray(R.array.librarys_en);

    private OkHttpClientManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        document = (Document) getIntent().getSerializableExtra(EXTRA_MODIFY_DOC);
        context = this;
        manager = OkHttpClientManager.getInstance();

        initView();
        initData();
    }

    private void initData() {
        mTitleEt.setText(document.getDocTitle());
   //     mAbstractEt.setText(document.getDocAbstract());
        // 建立数据源
        final String[] mItems = getResources().getStringArray(R.array.librarys);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        mLibrarySpinner.setAdapter(adapter);

    }

    private void initView() {
        mTitleEt = (EditText) findViewById(R.id.doc_title_et);
        mLibrarySpinner = (Spinner) findViewById(R.id.library_spinner);
        mAbstractEt = (EditText) findViewById(R.id.doc_abstract_edit_view);
        mSaveBtn = (Button) findViewById(R.id.save_btn);
        mSaveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_btn:
                saveInfo();
                break;
        }
    }

    private void saveInfo() {
        String title = mTitleEt.getText().toString();
        String docAbstract = mAbstractEt.getText().toString();
        String library = library_en[mLibrarySpinner.getSelectedItemPosition()];
        long date = System.currentTimeMillis();
        Date date1 = new Date(date);
        DateFormat format = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");
        String dateString = format.format(date1);

        Map<String,String> params = new HashMap<String,String>();
        params.put("title",title);
        params.put("docAbstract",docAbstract);
        params.put("library",library);
        String urlAddress = "";
        boolean success = manager.modifyDoc(params, urlAddress);
        if (success) {
            ToastFactory.showToast(context,"修改成功");

        } else {
            ToastFactory.showToast(context,"修改失败，请稍后重试！");
        }

    }
}
