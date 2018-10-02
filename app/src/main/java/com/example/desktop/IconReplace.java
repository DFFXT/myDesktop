package com.example.desktop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.config.GridViewDrawableAdapter;
import com.example.config.PublicData;
import com.example.config.appdata.AppConfigManager;
import com.example.config.appdata.configs.ThemeConfig;
import com.example.dataType.AppList;
import com.example.dataType.DesktopAppInfo;
import com.example.interface_.MyActivity;
import com.example.util.CommonsUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用图标替换
 * Created by home on 2018/3/13.
 */

public class IconReplace extends MyActivity implements View.OnClickListener{
    //**GridView视图
    private GridView originView,replaceView;
    //**选择的图标
    private Drawable selectedDrawable;
    private PackageManager pm=null;
    //**图片列表
    private ArrayList<Drawable> originList,replacedList;
    //**本地图片显示区域的parent
    private View iconShowParent;
    private ImageView iconShow;
    //**适配器
    private GridViewDrawableAdapter replacedListAdapter;
    //**需要替换的APP包名
    private String pkgName;
    //**顶部的3个按钮
    private TextView but1,but2,but3;

    private MyHandler handler=new MyHandler(this);
    @Override
    public void setNavigation(int h) {
        View v=findViewById(R.id.navigator);
        ViewGroup.LayoutParams params=v.getLayoutParams();
        params.height=h;
        v.setLayoutParams(params);
    }
    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.app_icon_replace);
        pkgName=getIntent().getStringExtra("pkgName");
        if (pkgName==null) finish();
        findId();
        setTheme();
        run();
    }

    /**
     * 根据主题设置样式
     */
    private void setTheme(){
        View navigator=findViewById(R.id.navigator);
        View titleBar=findViewById(R.id.titleBar);
        int theme= AppConfigManager.instance().getThemeConfig().getTheme();
        requestPermission();
        switch (theme){
            case ThemeConfig.THEME_DEFAULT:{
                int bg=getResources().getColor(R.color.colorPrimary);
                titleBar.setBackgroundColor(bg);
                navigator.setBackgroundColor(bg);
                bg=Color.WHITE;
                originView.setBackgroundColor(bg);
                replaceView.setBackgroundColor(bg);
                iconShowParent.setBackgroundColor(bg);
            }break;
            case ThemeConfig.THEME_CARTON:{
                int bg=getResources().getColor(R.color.blue);
                navigator.setBackgroundColor(bg);
                titleBar.setBackgroundColor(bg);
                bg=R.drawable.big_bg_s;
                originView.setBackgroundResource(bg);
                replaceView.setBackgroundResource(bg);
                iconShowParent.setBackgroundResource(bg);
                originView.setPadding(0,0,0,0);
                replaceView.setPadding(0,0,0,0);
                iconShowParent.setPadding(0,0,0,0);
            }break;
        }
    }


    private void findId(){
        originView=findViewById(R.id.originIconList);
        replaceView=findViewById(R.id.replaceIconList);
        iconShowParent=findViewById(R.id.iconShowParent);
        iconShow=findViewById(R.id.iconShow);
        but1=findViewById(R.id.localIcon);
        but2=findViewById(R.id.replacedIcon);
        but3=findViewById(R.id.localIconFile);

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.sure).setOnClickListener(this);
        but1.setOnClickListener(this);
        but2.setOnClickListener(this);
        but3.setOnClickListener(this);
        iconShow.setOnClickListener(this);

    }


    /**
     * 获取应用自带的icon
     * @return list
     */
    private ArrayList<Drawable> getOriginAppDrawable(){
        ArrayList<Drawable> arr=new ArrayList<>();
        pm=getPackageManager();
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list= pm.queryIntentActivities(intent,0);
        for(int i=0;i<list.size();i++){
            arr.add(list.get(i).activityInfo.loadIcon(pm));
        }
        return arr;
    }

    /**
     * 获取自定义的一些icon
     * @return list
     */
    private ArrayList<Drawable> getReplaceDrawable(){
        ArrayList<Drawable> list=new ArrayList<>();
        AppList appList = PublicData.getAppList();
        for(int page = 0; page< appList.getMaxPage(); page++){
            ArrayList<DesktopAppInfo> apps= appList.getPage(page);
            for(int i=0;i<apps.size();i++){
                DesktopAppInfo app=apps.get(i);
                if(app.isHasOtherIcon()){
                    Bitmap bitmap=app.getOtherIcon();
                    if(bitmap!=null)
                        list.add(new BitmapDrawable(getResources(),apps.get(i).getOtherIcon()));
                }
            }
        }
        return list;
    }

    private void setReplacedListAdapter(){
        replaceView.setAdapter(replacedListAdapter);
    }

    /**
     * 添加点击事件
     */
    private void setItemClick(){
        originView.setOnItemClickListener((parent, view, position, id) -> selectedDrawable=originList.get(position));
        replaceView.setOnItemClickListener((parent, view, position, id) -> selectedDrawable=replacedList.get(position));
    }
    //**对选择进行确认
    private void selectionSure(){
        if(selectedDrawable==null) {
            onBackPressed();
            return;
        }
        AppList appList=PublicData.getAppList();
        appList.replaceIcon(pkgName,((BitmapDrawable)selectedDrawable).getBitmap());

        setResult(RESULT_OK);
        finish();
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.back:{//**返回
                onBackPressed();
            }break;
            case R.id.sure:{//**确认选择
                selectionSure();
            }break;
            case R.id.localIcon:{//**原始icon选择
                v.setBackgroundResource(R.drawable.view_only_right_bottom_border_press);
                but3.setBackgroundColor(Color.WHITE);
                but2.setBackgroundResource(R.drawable.view_only_right_border);
                originView.bringToFront();
            }break;
            case R.id.replacedIcon:{//**替换过的icon选择
                v.setBackgroundResource(R.drawable.view_only_right_bottom_border_press);
                but3.setBackgroundColor(Color.WHITE);
                but1.setBackgroundResource(R.drawable.view_only_right_border);
                replaceView.bringToFront();
            }break;
            case R.id.localIconFile:{//**选择图片预览
                but1.setBackgroundResource(R.drawable.view_only_right_border);
                but2.setBackgroundResource(R.drawable.view_only_right_border);
                v.setBackgroundResource(R.drawable.view_only_bottom_borde_1px);

                if(selectedDrawable==null){
                    selectedDrawable=getAppIcon(pkgName);
                }
                if(selectedDrawable!=null) {
                    ((ImageView)iconShowParent.findViewById(R.id.iconShow)).setImageDrawable(selectedDrawable);
                }
                iconShowParent.bringToFront();

            }break;
            case R.id.iconShow:{//**选择本地图片
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }break;
        }
    }

    /**
     * 文件选择的回调
     * @param requestCode res
     * @param responseCode res
     * @param intent intent
     */
    public void onActivityResult(int requestCode,int responseCode,Intent intent){
        if(requestCode==1&&responseCode==RESULT_OK){
           Uri uri=intent.getData();
            Log.i("uri", "onActivityResult: "+uri);
           if(uri==null)return;
           replaceIcon(uri);
       }
    }

    /**
     * 替换图标
     * @param uri uri
     */
    private void replaceIcon(Uri uri){
        iconShow.setImageURI(uri);
        iconShowParent.setVisibility(View.VISIBLE);
        iconShowParent.bringToFront();
        selectedDrawable=iconShow.getDrawable();
    }
    private void requestPermission(){
        int code= ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (code!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            },111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode!=111)return;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                showL(CommonsUtil.getString(R.string.failed_permission));
            }
        }

    }

    /**
     * 获取pkgName的icon
     * @param pkgName pkgName
     * @return drawable
     */
    private Drawable getAppIcon(String pkgName){
        AppList appList=PublicData.getAppList();
        DesktopAppInfo app=appList.getApp(pkgName);
        if(app.isHasOtherIcon()){
            return new BitmapDrawable(getResources(),app.getOtherIcon());
        }
        try {
            return pm.getApplicationIcon(pkgName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 程序运行
     */
    private void run(){
        originList=getOriginAppDrawable();
        GridViewDrawableAdapter adapter=new GridViewDrawableAdapter(this,originList);
        originView.setAdapter(adapter);
        setItemClick();
        new Thread(() -> {
            replacedList=getReplaceDrawable();
            replacedListAdapter=new GridViewDrawableAdapter(IconReplace.this,replacedList);
            handler.sendEmptyMessage(1);
        }).start();
    }

    public void onBackPressed(){
        setResult(RESULT_CANCELED);
        finish();
    }

    private static class MyHandler extends Handler{
        private WeakReference reference;
        MyHandler(Activity activity){
            reference=new WeakReference<>(activity);
        }
        public void handleMessage(Message msg){
            IconReplace act= (IconReplace) reference.get();
            switch (msg.what){
                case 0:{
                    if(msg.obj!=null)
                        act.show(msg.obj.toString());
                }break;
                case 1:{
                    act.setReplacedListAdapter();
                }break;
            }
        }
    }

}
