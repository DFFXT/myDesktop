package com.example.config;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dataType.DesktopAppInfo;
import com.example.desktop.R;

import java.util.ArrayList;

/**
 * appList 适配器
 * Created by home on 2018/3/11.
 */

public class AppListAdapter extends BaseAdapter {
    private ArrayList<DesktopAppInfo> appInfos;
    private Context context;
    private PackageManager packageManager;
    public AppListAdapter(Context context,ArrayList<DesktopAppInfo> appInfos){
        this.appInfos=appInfos;
        this.context=context;
        packageManager=context.getPackageManager();
    }
    @Override
    public int getCount() {
        if(appInfos==null) return 0;
        return appInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        AppItem appItem;
        if(v==null){
            appItem=new AppItem();
            v=LayoutInflater.from(context).inflate(R.layout.app_item,parent,false);
            appItem.icon=v.findViewById(R.id.appIcon);
            appItem.label=v.findViewById(R.id.appLabel);
            v.setTag(appItem);
        }else {
            appItem= (AppItem) v.getTag();
        }
        DesktopAppInfo info=appInfos.get(position);
        appItem.label.setText(info.getOtherLabel()==null?info.getLabel():info.getOtherLabel());
        try {
            if(info.isHasOtherIcon()){
                appItem.icon.setImageBitmap(info.getOtherIcon());
            }else{
                appItem.icon.setImageDrawable(packageManager.getApplicationIcon(info.getPkgName()));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return v;
    }

    private class AppItem{
        private ImageView icon;
        private TextView label;
    }

}
