package com.example.config.appdata;

import android.support.annotation.StringDef;

import com.example.config.appdata.configs.CommonConfig;
import com.example.config.appdata.configs.ThemeConfig;

/**
 * 数据管理
 */
public class AppConfigManager {
    private AppConfigManager(){}
    private static AppConfigManager manager;

    public final static String NAME_CommonConfig="commonConfig";
    public final static String NAME_ThemeConfig="themeConfig";
    @StringDef({NAME_CommonConfig,NAME_ThemeConfig})
    public @interface DataPoolName{}


    public static AppConfigManager instance(){
        if(manager==null){
            synchronized (AppConfigManager.class){
                if(manager==null){
                    manager=new AppConfigManager();
                }
            }
        }
        return manager;
    }


    public CommonConfig getCommonConfig() {
        return CommonConfig.instance();
    }

    public ThemeConfig getThemeConfig(){
        return ThemeConfig.instance();
    }
}
