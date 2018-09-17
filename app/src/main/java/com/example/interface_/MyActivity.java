package com.example.interface_;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.desktop.R;

/**
 * 自定义activity
 * Created by home on 2018/3/11.
 */

public abstract class MyActivity extends Activity {


    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }
    public void setContentView(View v){
        super.setContentView(v);
        setNavigation(getNavigatorH());
    }
    public void setContentView(int layout){
        super.setContentView(layout);
        setNavigation(getNavigatorH());

    }
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view,params);
        setNavigation(getNavigatorH());
    }



    /**
     * 设置padding隔离状态栏
     */
    public abstract void setNavigation(int h);
    /**
     * 获取状态栏高度
     * @return h
     */
    private int getNavigatorH(){
        int statusBarHeight1 = -1;
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight1;
    }


    public void showL(String str){
        Toast.makeText(this,str,Toast.LENGTH_LONG).show();
    }
    public void show(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}
