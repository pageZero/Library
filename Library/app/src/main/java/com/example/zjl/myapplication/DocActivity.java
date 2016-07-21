package com.example.zjl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.zjl.adapter.DocumentaryAdapter;
import com.example.zjl.bean.Document;
import com.example.zjl.bean.DocumentList;
import com.example.zjl.bean.Tag;
import com.example.zjl.bean.User;
import com.example.zjl.utils.FileCacheUtil;
import com.example.zjl.utils.OkHttpClientManager;
import com.example.zjl.utils.ToastFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;


import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DocActivity extends Activity {


    public static final String EXTRA_NET_KEY = "com.example.zjl.myapplication.doc_from_net";
    public static final String EXTRA_LOCAL_KEY = "com.example.zjl.myapplication.doc_local";
    private Context mContext;
    private SharedPreferences preferences;

    //标志所在文库,初始显示个人文库
    private int FLAG_LIB = 0;
    private String library = "PL";

    //文库选择
    private LinearLayout ll_perDoc;
    private LinearLayout ll_depDoc;
    private LinearLayout ll_comDoc;
    private LinearLayout ll_myDowDoc;

    private TextView tv_perDoc;
    private TextView tv_depDoc;
    private TextView tv_comDoc;
    private TextView tv_myDowDoc;

    //top按钮
    private ImageView iv_mycenter;
    private ImageView iv_uploading;
    private ImageView iv_search;
    private ImageView iv_labs;

    //标签筛选
    private List<String> lab_list = new ArrayList<String>();
    private Spinner mSpinner;
    private ArrayAdapter<String> lab_dapter;

    private static final int DOC_NUM = 20;
    private ListView mainList;
    private PullToRefreshListView refreshList;//下拉刷新列表
    private ArrayList<Document> mListItems = new ArrayList<>(); //帖子列表
    private DocumentaryAdapter mDocAdapter; //列表适配器
    private int pageNum = 0; //当前加载到第几页
    private boolean haveData = true, isRefresh = false; //是否还有数据

    private boolean isCache = false;
    private List<Document> documentList = new ArrayList<>();

    OkHttpClientManager okHttpClientManager;
    private Gson mGson;

    private String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_doc);

        mContext = this;
        mGson = new Gson();
        okHttpClientManager = OkHttpClientManager.getInstance();
        preferences = getSharedPreferences("lib_modify", MODE_PRIVATE);

        initView();
        setListInfo();


    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Log.e("---------------", "更新UI");
                refreshList.onRefreshComplete();//完成刷新
                mDocAdapter.notifyDataSetChanged();//通知数据更新了
            } else if (msg.what == 2) {
                //spinner更新数据
                lab_dapter.notifyDataSetChanged();//适配器通知有数据更新
            }
        }
    };

    //根据文库修改tab
    private void updateUi() {
        switch (FLAG_LIB) {
            case 0:
                reSet();
                tv_perDoc.setTextColor(Color.parseColor("#5BC0DE"));
                break;
            case 1:
                reSet();
                //加载本文库数据
                tv_depDoc.setTextColor(Color.parseColor("#5BC0DE"));
                break;
            case 2:
                reSet();
                tv_comDoc.setTextColor(Color.parseColor("#5BC0DE"));
                break;
            case 3:
                reSet();
                //搜索本地目录
                tv_myDowDoc.setTextColor(Color.parseColor("#5BC0DE"));
                break;
        }
    }

    private void initView() {
        refreshList = (PullToRefreshListView) findViewById(R.id.refresh_list);
        mainList = refreshList.getRefreshableView();//获取刷新列表

        //top_home按钮绑定
        iv_mycenter = (ImageView) findViewById(R.id.image_user);
        iv_uploading = (ImageView) findViewById(R.id.image_upload);
        iv_search = (ImageView) findViewById(R.id.image_seach);
        iv_labs = (ImageView) findViewById(R.id.image_labs);
        mSpinner = (Spinner) findViewById(R.id.Spinner_lab);

        //点击头像跳转
        iv_mycenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent center_in = new Intent(mContext, MycenterActivity.class);
                startActivity(center_in);
            }
        });

        //点击上传跳转
        iv_uploading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploading_in = new Intent(mContext, UploadActivity.class);
                startActivity(uploading_in);
            }
        });

        // Spinner 下拉菜单的使用-----------------------------------------------------------------
        //第一步：获取标签数据
        initSpinnerData();
        //第二步：为下拉列表定义一个适配器，这里就用到里前面定义的list。
        lab_dapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lab_list);
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        lab_dapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        mSpinner.setAdapter(lab_dapter);
        //第五步：为下拉列表设置各种事件的响应，这个事响应菜单被选中
        mSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 将mSpinner 显示
                parent.setVisibility(View.VISIBLE);
                isCache = false;
              /*  params.clear();
                params.put("library", library);
                params.put("type", lab_list.get(position));//获取选中的类型
                params.put("pageNum", "0");
                documentList.clear();
                mListItems.clear();
                queryContent();*/
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                // myTextView.setText("NONE");
                arg0.setVisibility(View.VISIBLE);
            }
        });

        //绑定文库按钮
        ll_perDoc = (LinearLayout) findViewById(R.id.id_ll_personal_documents);
        ll_depDoc = (LinearLayout) findViewById(R.id.id_ll_department_documents);
        ll_comDoc = (LinearLayout) findViewById(R.id.id_ll_company_documents);
        ll_myDowDoc = (LinearLayout) findViewById(R.id.id_ll_myDownload_documents);

        tv_perDoc = (TextView) findViewById(R.id.id_tv_personal_documents);
        tv_depDoc = (TextView) findViewById(R.id.id_tv_department_documents);
        tv_comDoc = (TextView) findViewById(R.id.id_tv_company_documents);
        tv_myDowDoc = (TextView) findViewById(R.id.id_tv_myDownload_documents);


        ll_perDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLAG_LIB = 0;//设置标志位
                library = "PL";
                updateUi();//更新UI
                getData();
            }
        });

        ll_depDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLAG_LIB = 1;
                library = "DL";
                updateUi();
                getData();
            }
        });

        ll_comDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLAG_LIB = 2;
                library = "CL";
                updateUi();
                getData();
            }
        });

        ll_myDowDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLAG_LIB = 3;
                updateUi();
                getData();
            }
        });
//-----------------------------------------------------------------------
        //初始化数据集合,为列表设置适配器
        mListItems = new ArrayList<Document>();
        mDocAdapter = new DocumentaryAdapter(this, mListItems);
        //设置列表头部组件
        mainList.setAdapter(mDocAdapter);
    }

    /**
     * 初始化列表数据，两个途径：网络，缓存
     */
    private void initSpinnerData() {
        final SharedPreferences tagPreferences = getSharedPreferences("tags", MODE_PRIVATE);
        final Type type = new TypeToken<List<Tag>>() {
        }.getType();
        //没有缓存数据,从网络获取
        String tagsCache = tagPreferences.getString("tags", "");
        if (tagsCache.equals("")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String url = "http://113.55.43.88:8088/DataBaseDesignBate/Course/Control/TagsControl/GetAllTags.php";
                    List<Tag> tags = okHttpClientManager.queryTags(url);//skipcount
                    Iterator<Tag> it = tags.iterator();
                    while (it.hasNext()) {//添加到列表
                        lab_list.add(it.next().getName());
                    }
                    Message message = new Message();
                    message.what = 2;
                    handler.sendMessage(message);

                    //缓存到本地,标签信息用SharedPreferences缓存
                    String cache = mGson.toJson(tags, type);
                    SharedPreferences.Editor editor = tagPreferences.edit();
                    editor.putString("tags", cache);
                    editor.commit();
                }
            }).start();
        } else {
            List<Tag> tags = mGson.fromJson(tagsCache, type);
            Iterator<Tag> it = tags.iterator();
            while (it.hasNext()) {//添加到列表
                lab_list.add(it.next().getName());
            }
        }

    }

    /**
     * 设置列表属性及事件监听
     */
    private void setListInfo() {
        refreshList.setMode(PullToRefreshBase.Mode.BOTH);
        refreshList.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        refreshList.getLoadingLayoutProxy(true, false).setReleaseLabel("松开刷新");
        refreshList.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        refreshList.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
        refreshList.getLoadingLayoutProxy(false, true).setReleaseLabel("松开加载更多");
        refreshList.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载");
        refreshList.getLoadingLayoutProxy(true, true).setLoadingDrawable(getResources().getDrawable(R.drawable.refresh));
        //下拉刷新

        refreshList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                pageNum = 0;
                haveData = true;
                isRefresh = true;
                isCache = true;
                mListItems.clear();//重新加载时要清除列表数据
                documentList.clear();//
                Map<String, String> params = new HashMap<String, String>();
                if (userId.equals("")) {
                    userId = getUserInfo();
                }
                params.put("skipcount", "0");
                params.put("MainUserId", userId);
                Log.e("map.userId", "userId:" + params.get("MainUserId"));
                queryContent(params);

            }

            //上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (haveData) {
                    isRefresh = false;
                    isCache = false;
                    pageNum++;//下一页
                    //***********************！！！！！！！！！！！！！！！***************************
                    //修改请求参数
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("skipcount", String.valueOf(pageNum));
                    params.put("MainUserId", userId);
                    queryContent(params);

                } else {
                    refreshList.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ToastFactory.showToast(mContext, "已加载完所有数据");
                            refreshList.onRefreshComplete();
                        }
                    }, 500);
                }
            }
        });

        //列表点击事件
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                if (FLAG_LIB == 3) {//跳转到查看本地文件的页面
                    intent.setClass(mContext, LocalDocViewActivity.class);
                    intent.putExtra(EXTRA_LOCAL_KEY, mListItems.get(position - 1));
                } else {
                    //将文档信息作为参数传过去
                    intent.setClass(mContext, BrowseOneActivity.class);
                    intent.putExtra(EXTRA_NET_KEY, mListItems.get(position - 1));
                }
                mContext.startActivity(intent);
            }
        });

    }


    //重置
    private void reSet() {
        pageNum = 0; //当前加载到第几页
        haveData = true;
        isRefresh = true;
        mListItems.clear();
        documentList.clear();

        tv_perDoc.setTextColor(Color.BLACK);
        tv_depDoc.setTextColor(Color.BLACK);
        tv_comDoc.setTextColor(Color.BLACK);
        tv_myDowDoc.setTextColor(Color.BLACK);
    }

    /**
     * 下拉刷新时查询内容，填充mainListItems列表
     */
    private void queryContent(final Map<String, String> params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String urlAddress = "";
                if(FLAG_LIB == 0) {
                    urlAddress = "http://113.55.26.171:8088/DataBaseDesignBate/Course/Control/SearchControl/SearchPLControl.php";
                } else if (FLAG_LIB == 1) {
                    urlAddress = "http://113.55.26.171:8088/DataBaseDesignBate/Course/Control/SearchControl/SearchDLControl.php";
                } else if (FLAG_LIB == 2) {
                    urlAddress = "http://113.55.26.171:8088/DataBaseDesignBate/Course/Control/SearchControl/SearchCLControl.php";
                }
                Log.e("-------------", "刷新，查询数据FLAG_LIB="+FLAG_LIB);

                documentList = okHttpClientManager.queryDocs(params, urlAddress);
  //            String result = PostTest.sendPost(urlAddress, "MainUserId=" + userId + "&" + "skipcount=" + "0");
                mListItems.addAll(documentList);//新获取的文档
                //判断是否存储文件到本地
                if (isCache) {
                     cacheDoclistInfo();
                }
                //判断是否还有数据
                if (mListItems.size() < DOC_NUM) {
                    haveData = false;
                }
                //更新UI
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }).start();

    }

    private void queryContentByTag(final Map<String, String> params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String urlAddress = "http://113.55.26.171:8088/DataBaseDesignBate/Course/Control/SearchControl/SearchAllFileByTag.php";
                Log.e("-------------", "刷新，查询数据");
                documentList = okHttpClientManager.queryDocs(params, urlAddress);
                Log.e("-------------", "-------------");
                mListItems.addAll(documentList);//新获取的文档
                //判断是否存储文件到本地
                if (isCache) {
                    cacheDoclistInfo();
                }
                //判断是否还有数据
                if (mListItems.size() < DOC_NUM) {
                    haveData = false;
                }
                //更新UI
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }).start();

    }

    //判断列表信息是都被修改过
    private boolean isModify() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getComponent().getClassName().equals(BrowseOneActivity.class.getName())) {
                //获取文库类型，重新加载视图
                String str = preferences.getString("libraryType", "");
                if (str.equals("CL")) {
                    library = "CL";
                    FLAG_LIB = 2;
                } else if (str.equals("DL")) {
                    FLAG_LIB = 1;
                    library = "DL";
                } else if (str.equals("PL")) {
                    FLAG_LIB = 0;
                    library = "PL";
                } else {
                    Log.e("--------------","未被修改");
                    return false;//没有缓存数据，没有被修改
                }

                //判断是否需要刷新
                boolean isModify = preferences.getBoolean("isModify", false);
                //读取之后清除数据，避免重新加载同样的数据
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear().commit();//移除preferences中所有的值
                return isModify;
            }
        }
        return false;
    }

    /**
     * 判断是否需要重新加载
     * 判断有没有缓存-->加载？缓存是否过期：过期-->加载
     */
    private boolean isReload() {

        if (FLAG_LIB == 3) {
            return false;
        }
        if (FLAG_LIB == 2) {//公司文库
            String cacheData = FileCacheUtil.getCache(mContext, FileCacheUtil.companyDocCache);
            if (cacheData.equals("")) {//没有缓存数据（第一次）需要加载
                Log.e("----------------", "没有缓存");
                return true;
            } else {//有缓存，缓存是否过期
                return cacheIsOutDate(FileCacheUtil.companyDocCache);
            }
        } else if (FLAG_LIB == 1) {//部门文库
            String cacheData = FileCacheUtil.getCache(mContext, FileCacheUtil.departmentDocCache);
            if (cacheData.equals("")) {//没有缓存数据（第一次）
                return true;
            } else {//有缓存，缓存是否过期
                return cacheIsOutDate(FileCacheUtil.departmentDocCache);
            }
        } else if (FLAG_LIB == 0) {//个人文库
            String cacheData = FileCacheUtil.getCache(mContext, FileCacheUtil.personalDocCache);
            if (cacheData.equals("")) {//没有缓存数据（第一次）
                Log.e("-----------","没有缓存数据");
                return true;
            } else {//有缓存，缓存是否过期
                return cacheIsOutDate(FileCacheUtil.personalDocCache);
            }
        }
        return false;
    }

    //判断缓存是否过期,比较文件最后修改时间
    private boolean cacheIsOutDate(String cacheFileName) {

        File file = new File(FileCacheUtil.getCachePath(mContext) + "/" + cacheFileName);
        //获取缓存文件最后修改的时间，判断是是否从缓存读取
        long date = file.lastModified();
        long time_out = (System.currentTimeMillis() - date);
        if (time_out > FileCacheUtil.CACHE_SHORT_TIMEOUT) {
            Log.e("-------------", "缓存过期了~~~");
            return true;
        }
        Log.e("-------------", "缓存未过期了~~~");
        return false;//未过期
    }

    //下拉刷新
    private void refresh() {
        refreshList.setRefreshing();//不使用下拉动画，自动刷新
    }

    //缓存数据到本地，将文档列表转为JSON字符串存储
    private void cacheDoclistInfo() {
        Type type = new TypeToken<List<Document>>() {}.getType();
        //将获取到的文档数据转为Json串
        String data = mGson.toJson(documentList, type);
        switch (FLAG_LIB) {
            case 0://缓存到个人中心
                FileCacheUtil.setCache(data, mContext, FileCacheUtil.personalDocCache, mContext.MODE_PRIVATE);
                break;
            case 1://缓存到部门文库
                FileCacheUtil.setCache(data, mContext, FileCacheUtil.departmentDocCache, mContext.MODE_PRIVATE);
                break;
            case 2://缓存到公司文库
                FileCacheUtil.setCache(data, mContext, FileCacheUtil.companyDocCache, mContext.MODE_PRIVATE);
                break;
        }
    }

    /**
     * 从缓存中读取
     */
    private void getCacheDate() {
        Log.e("-----------------","获取缓存");
        String cacheFileName = null;
        if (FLAG_LIB == 0) {
            cacheFileName = FileCacheUtil.personalDocCache;
        } else if (FLAG_LIB == 1) {
            cacheFileName = FileCacheUtil.departmentDocCache;
        } else if (FLAG_LIB == 2) {
            cacheFileName = FileCacheUtil.companyDocCache;
        } else if (FLAG_LIB == 3) {
            cacheFileName = FileCacheUtil.downloadDocCache;
            String cache = FileCacheUtil.getCache(mContext, cacheFileName);
            if (cache.equals("")) {//没有下载
                ToastFactory.showToast(mContext, "没有下载文件");
                return;
            }
        }
        Type type = new TypeToken<List<Document>>() {
        }.getType();
        String cache = FileCacheUtil.getCache(mContext, cacheFileName);
        documentList.clear();
        mListItems.clear();
        documentList = mGson.fromJson(cache, type);//从Json格式的数据中获取文件列表
        Log.e("----------", "cache内容：" + cache);
        mListItems.addAll(documentList);//加载到视图
        mDocAdapter.notifyDataSetChanged();//通知更新Ui
    }


    //获取数据，加载到视图，缓存？网络
    private void getData() {
        Log.e("--------------","获取数据，网络？缓存？");
        //针对从网络获取的文件列表
        if (isModify()) {//文件在文件详情页被修改过
            refresh();
        } else if (isReload()) {//是否重新加载
            Log.e("--------------","需要重新加载");
            refresh();
        } else {//不重新加载，从缓存读取
            getCacheDate();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //获取标记，加载视图
    @Override
    protected void onResume() {
        super.onResume();
        userId = getUserInfo();
        Log.e("-------------", "onResume");
        Log.e("--------------", "本地userid" + userId);
        //如果没有该值，默认加载0
        FLAG_LIB = preferences.getInt("flag_lib", 0);
        updateUi();
        getData();
    }

    //暂停，跳转到其他activity时执行
    @Override
    protected void onPause() {
        super.onPause();
        //存储FLAG_Lib信息
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("flag_lib", FLAG_LIB);
        editor.commit();
    }

    private String getUserInfo() {
        //从本地获取用户id
        SharedPreferences userPreferences = getSharedPreferences("login", MODE_PRIVATE);
        return userPreferences.getString("id", "");
    }


}
