package com.example.config;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.desktop.R;

import java.util.ArrayList;

public class GridViewDrawableAdapter extends BaseAdapter {
	private ArrayList<Drawable> data;
	private Context context;
	public GridViewDrawableAdapter(Context context, ArrayList<Drawable> data){
		this.context=context;
		this.data=data;

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
	}
	public View getView(int position, View v, ViewGroup arg2) {
		Item item;
		if(v==null){
			item=new Item();
			v= LayoutInflater.from(context).inflate(R.layout.icon,arg2,false);
			item.icon=v.findViewById(R.id.icon);
			v.setTag(item);
		}
		else{
			item= (Item) v.getTag();
		}
		item.icon.setImageDrawable(data.get(position));
		return v;
	}
}
