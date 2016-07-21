package com.example.zjl.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zjl.myapplication.R;

/**
 * Created by zjl on 2016/6/5.
 * 全局只有一个toast实例
 */
public class ToastFactory {

    private static Context context = null;
    private static Toast toast = null;
    private static TextView text;
    /**
     * 弹出消息
     */
    public static void showToast(Context context, String message) {
        if (ToastFactory.context == context) {
            //cancelToast();
            if(text==null){
                text = (TextView) toast.getView().findViewById(R.id.toast_message);
            }
            text.setText(message);
        } else {
            cancelToast();
            ToastFactory.context = context;
            toast = new Toast(context);
            toast.setGravity(Gravity.TOP, 0, 100);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.myview_toast, null);// 自定义布局
            text = (TextView) view.findViewById(R.id.toast_message);// 显示的提示文字
            text.setText(message);
            toast.setView(view);
        }
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
    /**
     * 关闭当前toast
     */
    public static void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
