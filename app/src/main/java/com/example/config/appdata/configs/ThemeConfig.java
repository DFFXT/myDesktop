package com.example.config.appdata.configs;

import android.support.annotation.IntDef;

import com.example.config.appdata.AppConfigManager;
import com.example.config.appdata.tools.SPTools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ThemeConfig {
    private static ThemeConfig themeConfig;
    private int intDef=0;
    private String themeKey="theme";
    public static final byte THEME_DEFAULT = 1;
    public static final byte THEME_CARTON=2;
    private int theme=THEME_DEFAULT;
    @IntDef(value = {THEME_DEFAULT,THEME_CARTON})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ThemeStyle{}

    private ThemeConfig(){}
    public static ThemeConfig instance(){
        if(themeConfig==null){
            synchronized (ThemeConfig.class){
                if(themeConfig==null)
                    themeConfig=new ThemeConfig();
            }
        }
        return themeConfig;
    }

    public int getTheme() {
        return theme==intDef? SPTools.getIntRecord
                (AppConfigManager.NAME_ThemeConfig,themeKey):theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
        SPTools.saveIntRecord(AppConfigManager.NAME_ThemeConfig,themeKey,theme);
    }
}
