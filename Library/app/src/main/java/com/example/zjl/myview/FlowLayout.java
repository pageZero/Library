package com.example.zjl.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjl on 2016/6/8.
 */
public class FlowLayout extends ViewGroup {

    //存储所有的子View，第一行的所有子View，第二行的所有子View
    private List<List<View>> mAllViews = new ArrayList<List<View>>();

    //存储每一行的高度
    private List<Integer> mLineHeight = new ArrayList<>();

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


    }

    //设置子View的位置
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();

        //当前ViewGroup的宽度
        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        //一行子View
        List<View> lineViews = new ArrayList<>();
        int cCount = getChildCount();
        for(int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            //如果需要换行
            if(childWidth + lineWidth + lp.rightMargin + lp.leftMargin > width - getPaddingLeft() - getPaddingRight()) {
                //记录lineHeight
                mLineHeight.add(lineHeight);

                mAllViews.add(lineViews);

                //重置行宽和行高
                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;

                //重置view集合
                lineViews = new ArrayList<>();
            }
            //不需要换行，累加
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight,childHeight+lp.topMargin+lp.bottomMargin);
            lineViews.add(child);
        }//for end

        //处理最后一行
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);

        //设置所有子view的位置
        int left = getPaddingLeft();
        int top = getPaddingTop();

        //行数
        int lineNum = mAllViews.size();
        for(int i = 0; i < lineNum; i++) {

            //当前行所有的view
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            //遍历每一行
            for(int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                //判断当前子View是否显示
                if (child.getVisibility() == View.GONE) {
                    continue;
                }

                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                //计算当前View的left,top,right,buttom
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                //为子View设置布局
                child.layout(lc,tc,rc,bc);
                //后一个view的初始left值要重新设置
                left += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }

            //下一行left = getPaddingLeft() ;
            left = getPaddingLeft() ;
            top += lineHeight;

        }

    }

    /**
     *
     * @param widthMeasureSpec 父级传入的测量值
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        //wrap_content
        int width = 0;
        int height = 0;

        //记录每一行的宽度和高度
        int lineWidth = 0;
        int lineHeight = 0;

        //得到内部元素的个数
        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {

            View child = getChildAt(i);
            //测量子View的宽和高
            measureChild(child,widthMeasureSpec, heightMeasureSpec);
            //得到layoutParams
            MarginLayoutParams lp = (MarginLayoutParams)child.getLayoutParams();

            //子View占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            //子view占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineWidth + childWidth > sizeWidth - getPaddingLeft() - getPaddingRight()) {//需要换行

                //对比得到最大的宽度
                width = Math.max(width, lineWidth);
                lineWidth = childWidth;//重置lineWith
                height += lineHeight;//换行之后记录行高
                lineHeight = childHeight;
            } else {//不换行时
                //叠加行宽
                lineWidth += childWidth;
                //得到当前行最大的高度
                lineHeight = Math.max(lineHeight,childHeight);
            }
            //每一行的最后一个控件
            if(i == cCount - 1) {
                width = Math.max(lineWidth, width);
                height += lineHeight;
            }

        }

        Log.e("------------------","sizeWidth="+sizeWidth);
        Log.e("------------------","sizeHeight="+sizeHeight);

        //wrap_content-->对应AT_MOST模式；match_parent-->对应EXACTLY模式
        setMeasuredDimension(
                modeWidth == MeasureSpec.EXACTLY?sizeWidth:width + getPaddingLeft() + getPaddingRight(),
                modeHeight == MeasureSpec.EXACTLY?sizeHeight:height + getPaddingTop()+ getPaddingBottom()
        );
/*
        //wrap_content
        if (modeWidth == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, height);
        } else {//match_parent-->使用精确宽度和高度
            setMeasuredDimension(sizeWidth, sizeHeight);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
*/


    }

    /**
     * 与当前ViewGroup对应的layoutParams
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
