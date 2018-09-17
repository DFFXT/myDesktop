package com.example.config;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.widget.EditText;

import com.example.desktop.LookingClickData;
import com.example.desktop.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 配置
 * Created by home on 2018/3/16.
 */

public class Config {
    private static String configName="config2.0";
    private static Info info=null;


    /**
     * 读取配置信息
     */
    public static Info readConfig(){
        if(info!=null)return info;
        try {
            InputStream is=MainApplication.getContext().openFileInput(configName);
            ObjectInputStream ois=new ObjectInputStream(is);
            Config.info= (Info) ois.readObject();
            is.close();
            ois.close();
            return Config.info;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Config.info = new Info();
    }

    /**
     * 保存配置
     */
    public static void saveConfig(){
        if(info==null)return;
        try {
            OutputStream os=MainApplication.getContext().openFileOutput(configName, Context.MODE_PRIVATE);
            ObjectOutputStream oos=new ObjectOutputStream(os);
            oos.writeObject(info);
            os.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveConfigSync(){
        new Thread(Config::saveConfig).start();
    }

    public static class Info implements Serializable{
        //**桌面是否显示字体
        private boolean showText=true;
        //**字体颜色
        private int color=Color.WHITE;

        //**主题信息

        public static final byte THEME_DEFAULT = 1;
        public static final byte THEME_CARTON=2;
        private int theme=THEME_DEFAULT;
        @IntDef(value = {THEME_DEFAULT,THEME_CARTON})
        @Retention(RetentionPolicy.SOURCE)
        public @interface ThemeStyle{}



        //**是否记录点击量
        private boolean recordClick=true;
        //**是否自动更新
        private boolean autoUpdate=true;

        public void setAutoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
            saveConfig();
        }

        public void setColor(int color) {
            this.color = color;
            saveConfig();
        }

        public void setRecordClick(boolean recordClick) {
            this.recordClick = recordClick;
            saveConfig();
        }

        public void setTheme(@ThemeStyle int theme) {
            this.theme = theme;
            saveConfig();
        }

        public void setShowText(boolean showText) {
            this.showText = showText;
            saveConfig();
        }


        public int getColor() {
            return color;
        }

        public int getTheme() {
            return theme;
        }

        public boolean isAutoUpdate() {
            return autoUpdate;
        }

        public boolean isRecordClick() {
            return recordClick;
        }

        public boolean isShowText() {
            return showText;
        }
    }



}
