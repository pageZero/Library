package com.example.zjl.myview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.example.zjl.myapplication.R;

/**
 * Created by zjl on 2016/6/9.
 */
public class CheckTextView extends TextView {

    private boolean isSelected;

    public CheckTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckTextView);
        isSelected = a.getBoolean(R.styleable.CheckTextView_isSelected,false);//获取自定义属性的值

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSelected) {//说明之前是选中状态，再次点击变成为选中状态
                    isSelected = false;
                    CheckTextView.this.setBackgroundResource(R.drawable.text_view_normal_bg);

                } else {//说明之前未选中，点击之后选中了
                    isSelected = true;
                    setBackgroundResource(R.drawable.text_view_selected_bg);

                }
            }
        });
    }

    public CheckTextView(Context context) {
        super(context);
    }

    //获取选择状态
    @Override
    public boolean isSelected() {
        return isSelected;
    }

    //设置选中状态
    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
