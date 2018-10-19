package com.example.desktop;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.config.ClickData;
import com.example.config.appData.AppConfigManager;
import com.example.config.appData.configs.ThemeConfig;
import com.example.interface_.MyActivity;
import com.example.io.ClickDataIO;
import com.popwindow.w.WindowConfirm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
public class LookingClickData extends MyActivity{
	ListView listView=null;
	TextView textView=null;
	ArrayList<String> data=null;
	ArrayList<String> pkn=new ArrayList<>();//--保存每一行对应的应用包名。
	ArrayAdapter<String> adapter=null;

	public void setNavigation(int h){

		View v=findViewById(R.id.navigator);
		ViewGroup.LayoutParams params=v.getLayoutParams();
		params.height=h;
		v.setLayoutParams(params);
	}
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.looking_click_data);
		listView=findViewById(R.id.stream_text);
		textView=findViewById(R.id.stream_title);
		getData(listView);
		listView.setAdapter(adapter);
		
		listView.setOnItemLongClickListener((parent, view, position, id) -> {
            final WindowConfirm alert=new WindowConfirm(LookingClickData.this);
            alert.setTitle("删除记录？");
            alert.setMessage(data.get(position));
            alert.setLeftButton("取消",null);
            alert.setRightButton("删除", v -> {
                String str=pkn.get(position);
                new ClickDataIO().removeOneRecord(str.substring(0, str.indexOf(":    ")));
                data.remove(position);
                adapter.notifyDataSetChanged();
            });
            alert.show();
            return false;
        });
		setTheme();
	}

	/**
	 * 设置主题
	 */
	private void setTheme(){
		View navigator=findViewById(R.id.navigator);
		TextView title=findViewById(R.id.stream_title);
		View box=findViewById(R.id.parent);
		int theme= AppConfigManager.instance().getThemeConfig().getTheme();
		switch (theme){
			case ThemeConfig.THEME_DEFAULT:{
				navigator.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
				title.setTextColor(Color.WHITE);
			}break;
			case ThemeConfig.THEME_CARTON:{
				title.setTextColor(Color.WHITE);
				navigator.setBackgroundColor(getResources().getColor(R.color.blue));
				box.setBackgroundResource(R.drawable.big_bg_s);
				box.setPadding(0,0,0,0);
			}break;
		}
	}
	public void getData(final ListView listView){//--获取数据，如果卸载了则删除相应的数据
		new Thread(() -> {
            boolean change=false;
            final ArrayList<String> arrayList=new ArrayList<>();
            ArrayList<String> removeList=new ArrayList<>();
            ClickData clickData=null;
            try {
                clickData=new ClickDataIO().getClickData();
                if(clickData==null){
                    listView.post(() -> textView.setText("无数据"));
                    return;
                }
                Set<String> set=clickData.list.keySet();
                Iterator<String> iterable=set.iterator();
                PackageManager pm=getPackageManager();
                while(iterable.hasNext()){
                    String pkg=iterable.next();
                    try{
                        arrayList.add(pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES).applicationInfo.loadLabel(pm)+":    "+clickData.list.get(pkg));
                        pkn.add(pkg+":    "+clickData.list.get(pkg));
                    }catch(Exception ee){
                        change=true;
                        removeList.add(pkg);
                    }

                }
                if(change){//--删除无用的数据
                    for(int j=0;j<removeList.size();j++){
                        clickData.list.remove(removeList.get(j));
                    }
                    new ClickDataIO().saveClickDataSync(clickData);
                }
            } catch (Exception e) {
                show(e.toString());
            }
            Collections.sort(arrayList,new Comp());
            Collections.sort(pkn,new Comp());
            final ClickData finalClickData = clickData;
            listView.post(() -> {
                data=arrayList;
                setT(finalClickData);
                listView.setAdapter(adapter=new ArrayAdapter<>(LookingClickData.this,
                        android.R.layout.simple_list_item_1,data));
            });
        }).start();
	}
	void setT(ClickData clickData){//---设置显示日期
		Calendar calendar=Calendar.getInstance();
		textView.setText(clickData.startDate+"至"+
		calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+"应用点击数");
	}
	
	public boolean onKeyDown(int keyCode,KeyEvent e){
		if(keyCode==KeyEvent.KEYCODE_HOME){
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, e);
	}
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.NONE, 0, Menu.NONE, "清空");
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId()==0){
			new ClickDataIO().clear();
			data.clear();
			textView.setText("无数据");
			adapter.notifyDataSetChanged();
		}
		return false;
	}
	/*
	 * 排序为从小到大，若想排序为从大到小，则str1>str2返回-1，str1<str2返回+1；
	 */
	private class Comp implements Comparator<String>{
		public int compare(String str1, String str2) {
			int t1=Integer.parseInt(str1.substring(str1.lastIndexOf(":    ")+5));
			int t2=Integer.parseInt(str2.substring(str2.lastIndexOf(":    ")+5));
			if(t1>t2) return -1;
			else if(t1<t2) return 1;
			return 0;
		}
	}
}

