package com.example.desktop;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.background.BackgroundService;
import com.example.config.DownloadUpdate;
import com.example.config.PublicData;
import com.example.config.appdata.AppConfigManager;
import com.example.config.appdata.configs.CommonConfig;
import com.example.config.appdata.configs.ThemeConfig;
import com.example.dataType.AppList;
import com.example.dataType.DesktopAppInfo;
import com.example.interface_.MyActivity;
import com.example.io.AppInfoIO;
import com.popwindow.w.WindowConfirm;
import com.popwindow.w.plug.InputDialog;
import com.popwindow.w.plug.LineMenu;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 设置
 * Created by home on 2018/3/15.
 */

public class Setting extends MyActivity implements View.OnClickListener{

    private MyHandler handler=new MyHandler(this);

    private CheckBox textShowCheckBox,recordCheckBox;
    private View textColorShow;

    @Override
    public void setNavigation(int h) {
        View v= findViewById(R.id.navigator);
        ViewGroup.LayoutParams params=v.getLayoutParams();
        params.height=h;
        v.setLayoutParams(params);
    }

    /**
     * 根据主题设置样式
     */
    private void setTheme(){
        int theme= AppConfigManager.instance().getThemeConfig().getTheme();
        TextView title=findViewById(R.id.title);
        View navigator=findViewById(R.id.navigator);
        View itemBox=findViewById(R.id.itemBox);
        switch (theme){
            case ThemeConfig.THEME_DEFAULT:{
                int bg=getResources().getColor(R.color.colorPrimary);
                navigator.setBackgroundColor(bg);
                title.setBackgroundColor(bg);

            }break;
            case ThemeConfig.THEME_CARTON:{
                int bg=getResources().getColor(R.color.blue);
                navigator.setBackgroundColor(bg);
                title.setBackgroundColor(bg);
                itemBox.setBackgroundResource(R.drawable.big_bg_s);
                itemBox.setPadding(0,0,0,0);
            }break;
        }
    }
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.setting);
        findId();
        setTheme();
    }

    private void findId(){
        findViewById(R.id.clear).setOnClickListener(this);
        findViewById(R.id.recovery).setOnClickListener(this);
        findViewById(R.id.showText).setOnClickListener(this);
        findViewById(R.id.textColorSelect).setOnClickListener(this);
        findViewById(R.id.addTip).setOnClickListener(this);
        findViewById(R.id.lookClickData).setOnClickListener(this);
        findViewById(R.id.version).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.recodeClick).setOnClickListener(this);
        textShowCheckBox=findViewById(R.id.showTextCheckBox);
        recordCheckBox=findViewById(R.id.recordClickCheckBox);
        textColorShow=findViewById(R.id.textColorShow);

        CommonConfig commonConfig= AppConfigManager.instance().getCommonConfig();


        textShowCheckBox.setChecked(commonConfig.isShowText());
        recordCheckBox.setChecked(commonConfig.isRecordClick());
        textColorShow.setBackgroundColor(commonConfig.getTextColor());



        //***切换是否显示字体
        textShowCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(textShowCheckBox.isChecked()){
                commonConfig.setShowText(true);
            }else {
                commonConfig.setShowText(false);
            }
            rebootDesktop();
        });
        //**切换是否记录点击量
        recordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(recordCheckBox.isChecked()){
                commonConfig.setRecordClick(true);
            }else {
                commonConfig.setRecordClick(false);
            }
            rebootDesktop();
        });
    }


    /**
     * 软件更新
     */
    private void softwareUpdate(){


        new Thread(()->{

            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder().url("http://112.74.25.13/app/desktop/conf.ini").build();
            try {
                Response response=client.newCall(request).execute();
                if(response.isSuccessful()){
                    Properties properties=new Properties();
                    ResponseBody body=response.body();
                    if(body==null){
                        throw new IOException("null body");
                    }
                    properties.load(body.byteStream());
                    PackageManager pm=getPackageManager();
                    int versionCode = pm.getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_ACTIVITIES).versionCode;
                    int netVersionCode=Integer.parseInt(properties.getProperty("versionCode","0"));
                    Message msg=new Message();

                    if(versionCode<netVersionCode){
                        msg.what=1;
                        msg.obj="检测到更新V"+properties.getProperty("versionName");
                        handler.sendMessage(msg);
                    }
                    else if(versionCode==netVersionCode){
                        msg.what=0;
                        msg.obj="当前已是最新版本";
                        handler.sendMessage(msg);
                    }else{
                        msg.what=0;
                        msg.obj="哟版本比官方版本还高！";
                        handler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                Message msg=new Message();
                msg.obj=e;
                handler.sendMessage(msg);
            }
        }).start();

        new Thread(() -> {
            try {
                URL url=new URL("http://112.74.25.13/app/desktop/conf.ini");
                Properties properties=new Properties();
                properties.load(url.openStream());
                PackageManager pm=getPackageManager();
                int versionCode = pm.getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_ACTIVITIES).versionCode;
                int netVersionCode=Integer.parseInt(properties.getProperty("versionCode","0"));
                Message msg=new Message();
                if(versionCode<netVersionCode){
                    msg.what=1;
                    msg.obj="检测到更新V"+properties.getProperty("versionName");
                    handler.sendMessage(msg);
                }
                else if(versionCode==netVersionCode){
                    msg.what=0;
                    msg.obj="当前已是最新版本";
                    handler.sendMessage(msg);
                }else{
                    msg.what=0;
                    msg.obj="哟本版本比官方版本还高！";
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                Message msg=new Message();
                msg.obj=e;
                handler.sendMessage(msg);
            }
        });

    }

    /**
     * 重启桌面
     */
    private void rebootDesktop(){
        Intent intent=new Intent(Setting.this, BackgroundService.class);
        intent.setAction("recovery");
        startService(intent);
    }

    /**
     * 选字体颜色
     */
    private void textColorShow(){
        LineMenu lineMenu=new LineMenu(this);
        lineMenu.setIconSize(80,50);
        final int colors[]=new int[]{Color.BLACK,Color.RED,Color.WHITE,Color.GREEN,Color.CYAN};
        View views[]=new View[colors.length];
        CommonConfig commonConfig= AppConfigManager.instance().getCommonConfig();
        for(int i=0;i<colors.length;i++){
            final int finalI = i;
            views[i]=lineMenu.addItem("", -1, v -> {
                commonConfig.setTextColor(colors[finalI]);
                textColorShow.setBackgroundColor(colors[finalI]);
                if(commonConfig.isShowText()){
                    rebootDesktop();
                }
            });
            views[i].setBackgroundColor(colors[i]);
        }
        //**自定义颜色
        lineMenu.addItem("自定义", R.drawable.add, v -> {
            InputDialog inputDialog=new InputDialog(Setting.this);
            inputDialog.setTitle("输入16进制颜色：#ffff0000");
            inputDialog.setLeft("取消",null);
            inputDialog.setRight("确认", new InputDialog.OnClickListener() {
                @Override
                public void onClick(String s) {
                    int color;
                    try {//**颜色转换
                        color=Color.parseColor(s);
                    }catch (Exception e){
                        show("颜色格式错误");
                        return;
                    }
                    commonConfig.setTextColor(Color.parseColor(s));
                    textColorShow.setBackgroundColor(color);//**颜色预览
                    if(commonConfig.isShowText()){
                        rebootDesktop();
                    }
                }
            });
            inputDialog.show();
        });

        lineMenu.show();
    }


    @Override
    public void onClick(final View v) {
        switch (v.getId()){
            case R.id.clear:{//***还原App修改
                WindowConfirm confirm=new WindowConfirm(this);
                confirm.setTitle("是否还原修改");
                confirm.setMessage("还原后图标修改和名称修改将恢复");
                confirm.setLeftButton("还原", v1 -> {
                    AppInfoIO appInfoIO=new AppInfoIO();
                    AppList appList= PublicData.getAppList();
                    for(int page=0;page<appList.getMaxPage();page++){//**复原信息，删除图标
                        ArrayList<DesktopAppInfo> apps=appList.getPage(page);
                        for(int i=0;i<apps.size();i++){
                            DesktopAppInfo app=apps.get(i);
                            app.clearIcon();
                            app.clearLabel();
                        }
                    }
                    appInfoIO.saveAppInfo(appList);
                    rebootDesktop();
                });
                confirm.setRightButton("取消",null);
                confirm.show();
            }break;
            case R.id.recovery:{//**清空设置
                WindowConfirm confirm=new WindowConfirm(this);
                confirm.setTitle("清空所有信息");
                confirm.setMessage("清空等于卸载后重新安装");
                confirm.setLeftButton("清空", v12 -> {
                    new AppInfoIO().deleteAppList();
                    PublicData.refreshAppList();
                    rebootDesktop();
                });
                confirm.setRightButton("取消",null);
                confirm.show();
            }break;
            case R.id.showText:{//**是否显示App名称
                textShowCheckBox.performClick();
            }break;

            case R.id.textColorSelect:{//**选择颜色
                textColorShow();
            }break;
            case R.id.addTip:{

            }break;
            case R.id.recodeClick:{
                recordCheckBox.performClick();
            }break;
            case R.id.lookClickData:{
                Intent intent=new Intent(this,LookingClickData.class);
                startActivity(intent);
            }break;

            case R.id.version:{//**版本信息
                WindowConfirm confirm=new WindowConfirm(this);
                confirm.setMessage("当前版本：V "+ BuildConfig.VERSION_NAME+"\n\n"+
                                    "功能全面升级\n\n"+
                                    "放弃流量记录功能\n\n"+
                                    "优化桌面滑动，修改应用方式\n\n"+
                                    "支持显示插件，快捷方式");
                confirm.show();
            }break;
            case R.id.update:{
                softwareUpdate();
            }break;
        }
    }


    private static class MyHandler extends Handler {
        private WeakReference reference;
        MyHandler(Activity activity){
            reference=new WeakReference<>(activity);
        }
        public void handleMessage(Message msg){
            Setting act= (Setting) reference.get();
            switch (msg.what){
                case 0:{
                    if(msg.obj!=null)
                        act.show(msg.obj.toString());
                }break;
                case 1:{
                    try {
                        AlertDialog.Builder builder=new AlertDialog.Builder(act,R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                        builder.setTitle("检测到可用更新");
                        builder.setMessage(msg.obj.toString());
                        builder.setPositiveButton("是",((dialog, which) -> new DownloadUpdate(act).onClick(null)));
                        builder.setNegativeButton("否",(dialog1,wc)-> dialog1.dismiss());
                        builder.create().show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }break;

            }
        }
    }
}
