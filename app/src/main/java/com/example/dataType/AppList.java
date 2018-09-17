package com.example.dataType;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;

import com.example.interface_.DesktopInterface;
import com.example.io.AppInfoIO;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 应用列表
 * Created by home on 2018/3/6.
 */

public class AppList implements Serializable {
    //**当前窗口
    private int page=0;
    //**窗口应用列表[index为窗口id]
    private ArrayList<ArrayList<DesktopAppInfo>> pageAppList=new ArrayList<>();
    //**快捷方式列表
    private ArrayList<DesktopAppInfo> shortCutList=new ArrayList<>();
    //**应用插件列表
    private ArrayList<AppWidget> appWidgetList=new ArrayList<>();
    private int maxPage;



    /**
     * 添加一个应用
     * @param app app
     */
    public boolean addApp(DesktopAppInfo app){
        if(getApp(app.getName())!=null)return false;
        if(pageAppList.size()<=app.getPage()){
            ArrayList<DesktopAppInfo> list=new ArrayList<>();
            list.add(app);
            pageAppList.add(app.getPage(),list);
        }else{
            for(int i=0;i<pageAppList.get(app.getPage()).size();i++){
                if(pageAppList.get(app.getPage()).get(i).getName().equals(app.getName())){
                    return false;
                }
            }
            pageAppList.get(app.getPage()).add(app);
        }
        if(maxPage<app.getPage()){
            maxPage=app.getPage();
        }
        return true;
    }

    /**
     * 添加快捷应用
     * @param app app
     */
    public void addShortCutApp(DesktopAppInfo app){
        if(shortCutList.size()<4){
            shortCutList.add(app);
        }
    }
    public ArrayList<DesktopAppInfo> getShortCutList(){
        return shortCutList;
    }


    /**
     * 获取当前页的应用
     * @param page page
     * @return list
     */
    public ArrayList<DesktopAppInfo> getPage(int page){
        return pageAppList.get(page);
    }

    /**
     * 移除一个app
     * @param pkgName pkgName
     */
    public void removeApp(String pkgName){
        for(int page=0;page<=maxPage;page++){
            ArrayList<DesktopAppInfo> list=pageAppList.get(page);
            for(int i=0;i<list.size();i++){
                if(list.get(i).getPkgName().equals(pkgName)){
                    new AppInfoIO().deleteAppIcon(pkgName);
                    list.remove(i);
                    Log.i("delete",i+"");
                }
            }
        }
    }

    public void remove(DesktopAppInfo obj){
        for(int page=0;page<=maxPage;page++){
            ArrayList<DesktopAppInfo> list=pageAppList.get(page);
            for(int i=0;i<list.size();i++){
                if(list.get(i).equals(obj)){
                    new AppInfoIO().deleteAppIcon(obj.getPkgName());
                    list.remove(i);
                    Log.i("delete",i+"");
                }
            }
        }
    }

    /**
     * 添加一个页面
     * @param page page
     */
    public void addPage(int page){
        if(page>=maxPage+1) {
            pageAppList.add(new ArrayList<DesktopAppInfo>());
        }else {
            for(int p=page;p<maxPage;p++){
                ArrayList<DesktopAppInfo> apps=pageAppList.get(p);
                for(int i=0;apps!=null&&i<apps.size();i++){
                    apps.get(i).setPage(apps.get(i).getPage()+1);
                }
            }
            pageAppList.add(page, new ArrayList<DesktopAppInfo>());

        }
        maxPage++;
    }
    /**
     * 删除一个页面
     * 需要对后面的所有App的page属性进行page--
     * @param page page
     */
    public void removePage(int page){
        if(page>maxPage) return;
        for(int i=page+1;i<maxPage;i++){
            ArrayList<DesktopAppInfo> apps=getPage(i);
            for(int index=0;index<apps.size();index++){
                apps.get(index).setPage(apps.get(index).getPage()-1);
            }
        }
        pageAppList.remove(page);
        maxPage--;
    }

    /**
     * 获取应用所在的页面
     * @param pkgName pkgName
     * @return page
     */
    public DesktopAppInfo getApp(String pkgName){
        if(pkgName==null )return null;
        for(int i=0;i<pageAppList.size();i++){
            ArrayList<DesktopAppInfo> list=pageAppList.get(i);
            for(int index=0;index<list.size();index++){
                if(list.get(index).getPkgName().equals(pkgName)) return list.get(index);
            }
        }
        for(int i=0;i<shortCutList.size();i++){
            if(shortCutList.get(i).getPkgName().equals(pkgName))return shortCutList.get(i);
        }
        return null;
    }



    /**
     * 添加一个桌面插件
     * @param appWidget widget
     */
    public boolean addAppWidget(AppWidget appWidget){
        for(int i=0;i<appWidgetList.size();i++){
            if(appWidgetList.get(i).getId()==appWidget.getId())return false;
        }
        appWidgetList.add(appWidget);
        return true;
    }

    /**
     * 获取相应窗口的widget
     * @param page page
     * @return list
     */
    public ArrayList<AppWidget> getPageWidget(int page){
        ArrayList<AppWidget> appWidgets=new ArrayList<>();
        for(int i=0;i<appWidgetList.size();i++){
            if(appWidgetList.get(i).getPage()==page){
                appWidgets.add(appWidgetList.get(i));
            }
        }
        return appWidgets;
    }



    /**
     * 移除一个widget
     * @param appWidget AppWidget
     */
    public void removeAppWidget(AppWidget appWidget){
        appWidgetList.remove(appWidget);
    }
    public void removeAppWidget(int id){
        for(int i=0;i<appWidgetList.size();i++){
            if(appWidgetList.get(i).getId()==id) {
                appWidgetList.remove(i);
                i--;
            }
        }
    }
    //**对APP图标进行替换
    public void replaceIcon(String pkgName, Bitmap bitmap){
        DesktopAppInfo app=getApp(pkgName);
        replaceIcon(app,bitmap);

    }

    /**
     * 替换图标
     * @param app app
     * @param bitmap bitmap
     */
    public void replaceIcon(DesktopAppInfo app, Bitmap bitmap){
        app.setHasOtherIcon(true);
        AppInfoIO appInfoIO=new AppInfoIO();
        String path=appInfoIO.saveAppIcon(app.getPkgName(),bitmap);
        app.setIconPath(path);
        appInfoIO.saveAppInfo(this);
    }




    public int getMaxPage() {
        return maxPage;
    }

    /**
     * 获取当前页面
     * @return page
     */
    public int getPage() {
        return page;
    }

    /**
     * 设置页面
     * @param page page
     */
    public void setPage(int page) {
        this.page = page;
    }

    public enum AppType implements Serializable{
        systemApp,
        thirdPartyApp,
        shortcut,
        folder,
        file
    }
}
