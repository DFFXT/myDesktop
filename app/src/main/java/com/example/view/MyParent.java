package com.example.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * 自定义parent，进行事件的拦截
 */
public class MyParent extends FrameLayout {
    private OnTouchListener listener=null;
    private boolean intercept=false;
    public MyParent(@NonNull Context context) {
        super(context);
    }

    public MyParent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyParent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(listener!=null) intercept=listener.onTouch(this,ev);
        return super.dispatchTouchEvent(ev);
    }
    public void setOnTouchListener(OnTouchListener listener){
        this.listener=listener;
    }
    public boolean onTouchEvent(MotionEvent e){
        return true;
    }

}
