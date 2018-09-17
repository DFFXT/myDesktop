package com.example.config;

import com.example.dataType.AppList;
import com.example.io.AppInfoIO;

public class PublicData {
    private static AppList appList;

    /**
     * 获取应用列表
     * @return appList
     */
    public static AppList getAppList(){
        if(appList==null){
            AppInfoIO appInfoIO=new AppInfoIO();
            appList=appInfoIO.readAppList();
        }
        return appList;
    }

    /**
     * 强制从本地读取数据
     */
    public static AppList refreshAppList(){
        appList=null;
        return getAppList();
    }

    /**
     * 保存数据
     */
    public static void saveData(){
        AppInfoIO appInfoIO=new AppInfoIO();
        appInfoIO.saveAppInfo(PublicData.getAppList());
    }
}
