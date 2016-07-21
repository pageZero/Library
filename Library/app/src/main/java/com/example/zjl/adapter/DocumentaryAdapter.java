package com.example.zjl.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zjl.bean.Document;
import com.example.zjl.myapplication.R;

import java.util.List;


/**
 * Created by zjl on 2016/4/24.
 */
public class DocumentaryAdapter extends BaseContentAdapter<Document>{
    /**
     * 构造方法,初始化上下文对象和数据集合
     *
     * @param context
     * @param list
     */
    public DocumentaryAdapter(Context context, List<Document> list) {
        super(context, list);

    }

    @Override
    public View getConvertView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.ite_list_document, null);
            viewHolder.docImg = (ImageView) convertView
                    .findViewById(R.id.doc_img);
            viewHolder.docTitle = (TextView) convertView
                    .findViewById(R.id.tv_doc_title);
            viewHolder.docAuthor = (TextView) convertView
                    .findViewById(R.id.tv_doc_author);
            viewHolder.docData = (TextView) convertView
                    .findViewById(R.id.tv_doc_data);
            viewHolder.docSize = (TextView) convertView.findViewById(R.id.tv_doc_size);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 得到当前视频资源
        final Document document = dataList.get(position);
        //设置列表显示
        String title = document.getDocTitle();
        viewHolder.docTitle.setText(title);
        Log.e("--------------",title);
        //不同类型的文档设置不同的图片显示

        if (title.endsWith(".doc")||
                title.endsWith(".docx")) {
            Log.e("---------","doc");
            viewHolder.docImg.setImageResource(R.drawable.word);
        } else if (title.endsWith(".pdf")){
            Log.e("---------","pdf");
            viewHolder.docImg.setImageResource(R.drawable.pdf);
        } else if (title.endsWith(".xlsx")) {
            Log.e("---------","xlsl");
            viewHolder.docImg.setImageResource(R.drawable.x);
        } else if (title.endsWith(".zip")) {
            Log.e("---------","zip");
            viewHolder.docImg.setImageResource(R.drawable.zip);
        }else if(title.endsWith(".txt")) {
            Log.e("---------","txt");
            viewHolder.docImg.setImageResource(R.drawable.t);
        }

        viewHolder.docSize.setText(document.getSize()+"M");
        viewHolder.docAuthor.setText(document.getEditor());
        viewHolder.docData.setText(document.getUploadTime().toString());
        return convertView;
    }



    public static class ViewHolder {
        public ImageView docImg; // 文档图片
        public TextView docTitle; // 标题
        public TextView docAuthor; // 作者
        public TextView docSize;//文档大小
        public TextView docData; // 时间
    }
}
