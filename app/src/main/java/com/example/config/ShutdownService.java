package com.example.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.background.BackgroundService;

public class ShutdownService extends BroadcastReceiver{//---关机，提醒保存流量信息

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intent2=new Intent(context,BackgroundService.class);
		intent2.setAction("shutdown");
		context.startService(intent2);
	}

}
