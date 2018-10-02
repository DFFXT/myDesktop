package com.example.desktop;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.config.AppListAdapter;
import com.example.config.PublicData;
import com.example.util.ShortCut;
import com.example.config.appdata.AppConfigManager;
import com.example.config.appdata.configs.ThemeConfig;
import com.example.dataType.AppList;
import com.example.dataType.DesktopAppInfo;
import com.example.interface_.MyActivity;
import com.example.io.AppInfoIO;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 选择应用在当期的页面显示
 * Created by home on 2018/3/11.
 */

public class AppSelectAdd extends MyActivity {

    private ListView thirdAppList,systemAppList,rightList;
    //**两个listView的高度
    private int thirdH,sysH;
    //**窗口id
    private int page;
    //**该窗口已有的APP
    private ArrayList<DesktopAppInfo> pageList=null;
    //**未添加的系统APP
    private ArrayList<DesktopAppInfo> systemApp=new ArrayList<>();
    //**未添加的第三方APP
    private ArrayList<DesktopAppInfo> thirdPartyApp=new ArrayList<>();
    //**listView适配器
    private AppListAdapter thirdPartyAppListAdapter,sysAppListAdapter;
    //**App数据
    private AppList appList;

    //**列表的展开和隐藏状态
    private boolean thirdList_show=true,sysList_show=true;

    private MyHand myhand=new MyHand(this);
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.app_select_add);
        getIntentData();
        findId();
        setTheme();
        getAndroidAppList();

    }
    public void setNavigation(int h){
        View v=findViewById(R.id.navigator);
        ViewGroup.LayoutParams params=v.getLayoutParams();
        params.height=h;
        v.setLayoutParams(params);
    }

    /**
     * 设置主题
     */
    private void setTheme(){
        View navigator=findViewById(R.id.navigator);
        View bar=findViewById(R.id.bar);
        View itemBox=findViewById(R.id.mainArea);
        int theme= AppConfigManager.instance().getThemeConfig().getTheme();
        switch (theme){
            case ThemeConfig.THEME_DEFAULT:{
                int bg=getResources().getColor(R.color.colorPrimary);
                bar.setBackgroundColor(bg);
                navigator.setBackgroundColor(bg);
            }break;
            case ThemeConfig.THEME_CARTON:{
                int bg=getResources().getColor(R.color.blue);
                bar.setBackgroundColor(bg);
                navigator.setBackgroundColor(bg);
                itemBox.setBackgroundResource(R.drawable.big_bg_s);
                itemBox.setPadding(0,0,0,0);
            }break;
        }
    }

    /**
     * 获取传递的窗口数据
     */
    private void getIntentData(){
        page=getIntent().getIntExtra("page",-1);
        if(page==-1)finish();
    }
    private void findId(){
        thirdAppList=findViewById(R.id.thirdAppList);
        systemAppList=findViewById(R.id.systemAppList);
        rightList=findViewById(R.id.rightAppList);

        //**确认修改，启动服务，修改桌面显示
        findViewById(R.id.sure).setOnClickListener(v -> {
            AppInfoIO appInfoIO=new AppInfoIO();
            appInfoIO.saveAppInfo(appList);
            //Intent intent=new Intent();
            //intent.setAction("windowManage");
            //intent.putExtra("page",page);
            //AppSelectAdd.this.startService(intent);
            setResult(RESULT_OK,new Intent());
            finish();
        });
        findViewById(R.id.cancel).setOnClickListener((v)->{
            setResult(RESULT_CANCELED,new Intent());
            finish();
        } );

        final Button thirdPartyButton=findViewById(R.id.thirdAppList_Button);
        Button systemAppButton=findViewById(R.id.systemAppList_Button);
        //**第三方应用按钮点击
        thirdPartyButton.setOnClickListener(v -> {
            //int h=(int)AppSelectAdd.this.getResources().getDimension(R.dimen.appSelectListHeight_third);
            if(thirdAppList.getHeight()==0){//**需要展开
                thirdList_show=true;
                hiddenAction(thirdAppList,thirdH,true);
            }else{//**需要隐藏
                thirdList_show=false;
                hiddenAction(thirdAppList,thirdH,false);
            }

        });
        //**系统应用按钮点击
        systemAppButton.setOnClickListener(v -> {
            if(systemAppList.getHeight()==0){//**需要展开
                sysList_show=true;
                hiddenAction(systemAppList,sysH,true);
            }else{//**需要隐藏
                sysList_show=false;
                hiddenAction(systemAppList,sysH,false);
            }
        });
    }

    /**
     * 设置适配器和点击事件
     */
    private void setAdapter(){
        if(pageList==null){
            show("数据错误");
            finish();
        }
        final AppListAdapter adapter=new AppListAdapter(this,pageList);
        rightList.setAdapter(adapter);
        //**已经存在的应用点击回移除，加入相应的系统应用列表或者第三方应用列表
        rightList.setOnItemClickListener((parent, view, position, id) -> {
            DesktopAppInfo app=pageList.get(position);
            if(app.getAppType()== AppList.AppType.shortcut){
                show("快捷方式请直接在桌面操作");
                return;
            }
            if(app.getAppType()== AppList.AppType.systemApp){
                systemApp.add(app);
                pageList.remove(position);
                getListViewHeight();
                if(sysList_show){
                    hiddenAction(systemAppList,systemAppList.getHeight(),sysH,200,true);
                }
                sysAppListAdapter.notifyDataSetChanged();
            }else if(app.getAppType()== AppList.AppType.thirdPartyApp){
                thirdPartyApp.add(app);
                pageList.remove(position);
                getListViewHeight();
                if(thirdList_show){
                    hiddenAction(thirdAppList,thirdAppList.getHeight(),thirdH,200,true);
                }
                thirdPartyAppListAdapter.notifyDataSetChanged();
            }
            adapter.notifyDataSetChanged();

        });


        sysAppListAdapter=new AppListAdapter(this,systemApp);
        thirdPartyAppListAdapter=new AppListAdapter(this,thirdPartyApp);

        systemAppList.setAdapter(sysAppListAdapter);
        thirdAppList.setAdapter(thirdPartyAppListAdapter);
        //**系统应用点击，加入存在的列表
        systemAppList.setOnItemClickListener((parent, view, position, id) -> {
            ShortCut.setPosition(appList,systemApp.get(position),appList.getPage());
            pageList.add(systemApp.get(position));
            systemApp.remove(position);
            getListViewHeight();
            if(sysList_show){
                hiddenAction(systemAppList,systemAppList.getHeight(),sysH,200,true);
            }
            sysAppListAdapter.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
        });
        //**第三方应用点击，加入存在列表
        thirdAppList.setOnItemClickListener((parent, view, position, id) -> {

            ShortCut.setPosition(appList,thirdPartyApp.get(position),appList.getPage());
            pageList.add(thirdPartyApp.get(position));
            thirdPartyApp.remove(position);
            getListViewHeight();
            if(thirdList_show){
                hiddenAction(thirdAppList,thirdAppList.getHeight(),thirdH,200,true);
            }
            thirdPartyAppListAdapter.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
        });

    }


    /**
     * 获取应用数据并分类
     */
    private void getAndroidAppList(){
        //**读取数据
        appList= PublicData.getAppList();
        if(appList==null) return;
        pageList=appList.getPage(page);

        if(pageList==null){
            showL("数据错误:page="+page);
            finish();//**数据错误
        }
        //**读取所有应用信息
        final PackageManager pm=getPackageManager();
        systemApp=new ArrayList<>();
        thirdPartyApp=new ArrayList<>();

        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> list=pm.queryIntentActivities(intent, 0);

        new Thread(() -> {
            for(int i=0;i<list.size();i++){
                boolean exist=false;
                for(int p=0;p<=appList.getMaxPage();p++){
                    ArrayList<DesktopAppInfo> pList=appList.getPage(p);
                    for(int j=0;j<pList.size();j++){
                        if(list.get(i).activityInfo.packageName.equals(pList.get(j).getPkgName())){
                            exist=true;
                            break;
                        }
                    }
                    if(exist)break;
                }

                if(exist) continue;
                final ResolveInfo res=list.get(i);
                final DesktopAppInfo app=new DesktopAppInfo(
                        res.activityInfo.packageName,
                        res.activityInfo.name,
                        "",
                        -1,
                        -1,-1,
                        null
                );
                if((res.activityInfo.applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM)==0){
                    app.setAppType(AppList.AppType.thirdPartyApp);
                    thirdPartyApp.add(app);
                    //myhand.sendMessage(ShortCut.makeMsg(1,1));

                }else{
                    app.setAppType(AppList.AppType.systemApp);
                    systemApp.add(app);
                    //myhand.sendMessage(ShortCut.makeMsg(2,2));
                }
            }
            myhand.sendMessage(ShortCut.makeMsg(1,1));
            myhand.sendMessage(ShortCut.makeMsg(2,2));
            myhand.sendMessage(ShortCut.makeMsg(3,2));

        }).start();
        setAdapter();
    }

    /**
     * 给APP添加Label
     */
    private void setLabel(){
        final PackageManager pm=getPackageManager();
        new Thread(() -> {
            for(int i=0;i<thirdPartyApp.size();i++){
                try {
                    String str= (String) pm.getApplicationLabel(pm.getApplicationInfo(thirdPartyApp.get(i).getPkgName(),0));
                    thirdPartyApp.get(i).setLabel(str);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            myhand.sendMessage(ShortCut.makeMsg(1,null));
            for(int i=0;i<systemApp.size();i++){
                try {
                    String str= (String) pm.getApplicationLabel(pm.getApplicationInfo(systemApp.get(i).getPkgName(),0));
                    systemApp.get(i).setLabel(str);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            myhand.sendMessage(ShortCut.makeMsg(2,null));
        }).start();
    }


    private void hiddenAction(final View view,int start, final int end,int during, final boolean show){
        ValueAnimator valueAnimator=ValueAnimator.ofInt(start,end);
        valueAnimator.setDuration(during);
        valueAnimator.addUpdateListener(animation -> {
            int val=(int)animation.getAnimatedValue();
            if(show){
                setHeight(view,val);
            }else{
                setHeight(view,end-val);
            }
        });
        valueAnimator.start();

    }

    /**
     * 对一个view进行高度变化
      * @param view 将要进行变化的视图
     * @param h 最大高度
     * @param show 是否是展开动画
     */
    private void hiddenAction(final View view, final int h, final boolean show){
        hiddenAction(view,0,h,500,show);
    }

    /**
     * 设置一个view的高度
     * @param v v
     * @param height 高度
     */
    private void setHeight(View v,int height){
        LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) v.getLayoutParams();
        params.height=height;
        v.setLayoutParams(params);
    }

    /**
     * 获取listView的高度，以便于ListView收缩时确定高度
     */
    private void getListViewHeight(){
        float h=thirdPartyApp.size()*getResources().getDimension(R.dimen.appListIconSize);
        float h1=getResources().getDimension(R.dimen.appSelectListHeight_third);
        if(h<h1){
            thirdH=(int)h;
        }else{
            thirdH=(int)h1;
        }

        float h2=systemApp.size()*getResources().getDimension(R.dimen.appListIconSize);
        float h3=getResources().getDimension(R.dimen.appSelectListHeight_sys);
        if(h2<h3){
            sysH=(int)h2;
        }else{
            sysH=(int)h3;
        }
    }


    public void onBackPressed(){
        setResult(RESULT_CANCELED,new Intent());
        finish();
    }














    private static class MyHand extends Handler{
        private WeakReference<AppSelectAdd> reference;
        MyHand(AppSelectAdd appSelectAdd){
            reference=new WeakReference<>(appSelectAdd);
        }
        public void handleMessage(Message msg){
            AppSelectAdd appSelectAdd=reference.get();
            switch (msg.what){
                case 0:reference.get().show(msg.obj.toString());break;
                case 1:{
                    if(appSelectAdd.thirdPartyAppListAdapter!=null) {
                        appSelectAdd.thirdPartyAppListAdapter.notifyDataSetChanged();
                    }
                }break;
                case 2:{
                    if(appSelectAdd.sysAppListAdapter!=null) {
                        appSelectAdd.sysAppListAdapter.notifyDataSetChanged();
                    }
                }break;
                case 3:{
                    appSelectAdd.getListViewHeight();
                    appSelectAdd.setHeight(appSelectAdd.thirdAppList,appSelectAdd.thirdH);
                    appSelectAdd.setHeight(appSelectAdd.systemAppList,appSelectAdd.sysH);
                    appSelectAdd.setLabel();
                }break;
            }
        }
    }

}
