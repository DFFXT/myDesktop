package com.example.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class AppWidgetParent extends FrameLayout {
    private OnTouchListener listener=null;
    public AppWidgetParent(@NonNull Context context) {
        super(context);
    }

    public AppWidgetParent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppWidgetParent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean dispatchTouchEvent(MotionEvent e){
        if(listener!=null)listener.onTouch(this,e);
        return super.dispatchTouchEvent(e);
    }
    public void setOnTouchListener(OnTouchListener listener){
        this.listener=listener;
    }

}
