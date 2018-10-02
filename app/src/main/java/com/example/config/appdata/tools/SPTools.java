package com.example.config.appdata.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.config.MainApplication;
import com.example.config.appdata.AppConfigManager;

public class SPTools {
    public static void saveIntRecord(@AppConfigManager.DataPoolName String name, String key, int value){
        SharedPreferences sp= MainApplication.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }
    public static void saveBooleanRecord(@AppConfigManager.DataPoolName String name, String key, boolean value){
        SharedPreferences sp= MainApplication.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public static int getIntRecord(@AppConfigManager.DataPoolName String name, String key){
        SharedPreferences sp=MainApplication.getContext().getSharedPreferences(name,Context.MODE_PRIVATE);
        return sp.getInt(key,0);
    }
    public static boolean getBooleanRecord(@AppConfigManager.DataPoolName String name, String key){
        SharedPreferences sp=MainApplication.getContext().getSharedPreferences(name,Context.MODE_PRIVATE);
        return sp.getBoolean(key,false);
    }
}
