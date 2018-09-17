package com.example.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 应用改变接收通知
 * Created by home on 2018/3/11.
 */

public class AppChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("rec",intent.getAction()+" ");
        intent.setClass(context, BackgroundService.class);
        context.startService(intent);
    }
}
