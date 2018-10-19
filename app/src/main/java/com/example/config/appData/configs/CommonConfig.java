package com.example.config.appData.configs;

import android.support.annotation.StringDef;

import com.example.config.appData.AppConfigManager;
import com.example.config.appData.tools.SPTools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CommonConfig {
    private static CommonConfig commonConfig;
    private int intDef=0;
    private int textColor;
    private boolean showText;
    private int screenBrightness;
    private boolean autoUpdate;
    private boolean recordClick;


    public static final String KEY_SHOW_TEXT="showText";
    public static final String KEY_TEXT_COLOR="textColor";
    public static final String KEY_SCREEN_BRIGHTNESS="screenBrightness";
    public static final String KEY_APP_THEME="appTheme";
    public static final String KEY_RECORD_CLICK="recordClick";
    public static final String KEY_AUTO_UPDATE="autoUpdate";
    @StringDef({KEY_SHOW_TEXT,KEY_TEXT_COLOR,KEY_SCREEN_BRIGHTNESS,
            KEY_APP_THEME,KEY_RECORD_CLICK,KEY_AUTO_UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    @interface CommonConfigKey{}


    private CommonConfig(){}
    public static CommonConfig instance(){
        if(commonConfig==null){
            synchronized (CommonConfig.class){
                if(commonConfig==null){
                    commonConfig=new CommonConfig();
                }
            }
        }
        return commonConfig;
    }


    public int getTextColor() {
        return textColor==intDef? SPTools.getIntRecord(AppConfigManager.NAME_CommonConfig,KEY_TEXT_COLOR):textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        SPTools.saveIntRecord(AppConfigManager.NAME_CommonConfig,KEY_TEXT_COLOR,textColor);

    }

    public boolean isShowText() {
        return SPTools.getBooleanRecord(AppConfigManager.NAME_CommonConfig,KEY_SHOW_TEXT);
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
        SPTools.saveBooleanRecord(AppConfigManager.NAME_CommonConfig,KEY_SHOW_TEXT,showText);
    }

    public int getScreenBrightness() {
        return screenBrightness==intDef?SPTools.getIntRecord(AppConfigManager.NAME_CommonConfig,KEY_SCREEN_BRIGHTNESS):screenBrightness;
    }

    public void setScreenBrightness(int screenBrightness) {
        this.screenBrightness = screenBrightness;
        SPTools.saveIntRecord(AppConfigManager.NAME_CommonConfig,KEY_SCREEN_BRIGHTNESS,screenBrightness);
    }

    public boolean isAutoUpdate() {
        return SPTools.getBooleanRecord(AppConfigManager.NAME_CommonConfig,KEY_AUTO_UPDATE);
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        SPTools.saveBooleanRecord(AppConfigManager.NAME_CommonConfig,KEY_AUTO_UPDATE,autoUpdate);
    }

    public boolean isRecordClick() {
        return SPTools.getBooleanRecord(AppConfigManager.NAME_CommonConfig,KEY_RECORD_CLICK);
    }

    public void setRecordClick(boolean recordClick) {
        this.recordClick = recordClick;
        SPTools.saveBooleanRecord(AppConfigManager.NAME_CommonConfig,KEY_RECORD_CLICK,recordClick);
    }
}
