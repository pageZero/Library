package com.example.zjl.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zjl on 2016/6/10.
 */
public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //获得焦点时滚动显示
    @Override
    public boolean isFocused() {
        return true;
    }
}
