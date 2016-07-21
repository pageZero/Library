package com.example.zjl.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 实现响应进度监听器接口，接收进度信息，使用handler传递进度参数
 * Created by zjl on 2016/6/11.
 */
public abstract class UIProgressResponseListener implements ProgressResponseListener {

    private static final int RESPONSE_UPDATE = 0x02;

    //处理UI层的Handler子类
    private static class UIHandler extends Handler {
        //弱引用
        private final WeakReference<UIProgressResponseListener> mUIProgressResponseListenerWeakReference;

        public UIHandler(Looper looper, UIProgressResponseListener uiProgressResponseListener) {
            super(looper);
            mUIProgressResponseListenerWeakReference = new WeakReference<UIProgressResponseListener>(uiProgressResponseListener);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESPONSE_UPDATE:
                    UIProgressResponseListener uiProgressResponseListener = mUIProgressResponseListenerWeakReference.get();
                    if (uiProgressResponseListener != null) {
                        //获得进度实体类
                        ProgressModel progressModel = (ProgressModel) msg.obj;
                        //回调抽象方法
                        uiProgressResponseListener.onUIResponseProgress(progressModel.getCurrentBytes(), progressModel.getContentLength(), progressModel.isDone());
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    //主线程Handler
    private final Handler mHandler = new UIHandler(Looper.getMainLooper(), this);

    //封装进度信息，发送给主线程
    @Override
    public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
        //通过Handler发送进度消息
        Message message = Message.obtain();
        message.obj = new ProgressModel(bytesRead, contentLength, done);
        message.what = RESPONSE_UPDATE;
        mHandler.sendMessage(message);
    }

    /**
     * UI层回调抽象方法
     * @param bytesRead 当前读取响应体字节长度
     * @param contentLength 总字节长度
     * @param done 是否读取完成
     */
    public abstract void onUIResponseProgress(long bytesRead, long contentLength, boolean done);
}

