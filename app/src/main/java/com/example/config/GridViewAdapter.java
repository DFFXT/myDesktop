package com.example.config;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.config.appData.AppConfigManager;
import com.example.dataType.DesktopAppInfo;
import com.example.desktop.R;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {
	private List<ResolveInfo> infos=new ArrayList<>();
	private ArrayList<DesktopAppInfo> data;
	private PackageManager pManager;
	private LayoutInflater inflater;
	private int resource;
	public GridViewAdapter(Context context, int layout, ArrayList<DesktopAppInfo> data, PackageManager pManager){
		this.data=data;
		this.pManager=pManager;
		inflater=LayoutInflater.from(context);
		resource=layout;
		readResolve();

	}
	public int getCount() {
		return data.size();
	}
	public Object getItem(int arg0) {
		return data.get(arg0);
	}
	public long getItemId(int arg0) {
		return arg0;
	}
	class Item{
		public ImageView icon;
		public TextView title;
	}
	public View getView(int position, View v, ViewGroup arg2) {
		Item item;
		DesktopAppInfo app=data.get(position);
		if(v==null){
			item=new Item();
			v=inflater.inflate(resource, arg2,false);

			item.icon=v.findViewById(R.id.icon);
			item.title=v.findViewById(R.id.title);

			v.setTag(item);
		}
		else{
			item= (Item) v.getTag();
		}

		item.title.setText(app.getOtherLabel()==null?app.getLabel():app.getOtherLabel());
		if(app.isHasOtherIcon()){
			item.icon.setImageBitmap(app.getOtherIcon());
		}else{
			item.icon.setImageDrawable(infos.get(position).loadIcon(pManager));
		}
		item.title.setTextColor(AppConfigManager.instance().getThemeConfig().getTheme());

		return v;
	}

	private void readResolve(){
		for(int i=0;i<data.size();i++){
			infos.add(getResolveInfo(data.get(i).getPkgName(),data.get(i).getName()));
		}
	}
	/**
	 * 根据包名获取resolveInfo
	 * @param pkgName pkgName
	 * @return info
	 */
	private ResolveInfo getResolveInfo(String pkgName,String name){
		Intent intent=new Intent(Intent.ACTION_MAIN);
		intent.setPackage(pkgName);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> list=pManager.queryIntentActivities(intent,0);
		if(list.size()==0)return null;
		for(int i=0;i<list.size();i++){
			if(list.get(i).activityInfo.name.equals(name)){
				return list.get(i);
			}
		}
		return null;
	}

}
