package com.example.config;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;

/**
 *
 * Created by home on 2018/3/9.
 */

public class MainApplication extends LitePalApplication {
    public static Context context;
    public void onCreate() {

        super.onCreate();
        context=getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }
}
