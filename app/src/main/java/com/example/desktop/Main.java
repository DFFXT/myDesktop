package com.example.desktop;

import android.animation.ValueAnimator;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.background.BackgroundService;
import com.example.config.GridViewAdapter;
import com.example.config.PublicData;
import com.example.util.ShortCut;
import com.example.config.TouchDirection;
import com.example.config.appdata.AppConfigManager;
import com.example.config.appdata.configs.ThemeConfig;
import com.example.dataType.AppList;
import com.example.dataType.AppWidget;
import com.example.dataType.DesktopAppInfo;
import com.example.interface_.DesktopInterface;
import com.example.interface_.MyActivity;
import com.example.io.AppInfoIO;
import com.example.io.ClickDataIO;
import com.example.view.AppWidgetParent;
import com.example.view.MyParent;
import com.popwindow.w.BoxMenu;
import com.popwindow.w.plug.InputDialog;
import com.popwindow.w.plug.PopMenu;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Main extends MyActivity implements View.OnClickListener{
	private GridView shortcut;
	private PackageManager pManager;
	//**父布局
	private MyParent parent;
	//**viewPager子布局
	private ArrayList<FrameLayout> wList=new ArrayList<>();
	//**小点父布局
	private LinearLayout dotParent;
	//**窗口对应的小点
	private ArrayList<ImageView> wDList=new ArrayList<>();
	//**菜单
	private BoxMenu boxMenu;
	//**长点击的顶层布局
	private RelativeLayout topMask;

	//**所有应用列表
	private AppList appList=null;
	//**触摸事件的保存，保存点击的对象，未重置表明一个事件未结束
	private TouchObj touchObj=new TouchObj();

	//**插件widgetHost
	private AppWidgetHost widgetHost;
	//**是否是重启桌面
	private boolean reboot=false;

	private ServiceConnection connection;

	private boolean switchPaged=false;

	public void setNavigation(int h){

	}

	public void onCreate(Bundle b) {
		super.onCreate(b);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		setContentView(R.layout.main);
		Intent intent=new Intent(this,BackgroundService.class);
		startService(intent);
		init();
		//**和后台进行连接
		bindService(new Intent(this, BackgroundService.class), connection=new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder s) {
				BackgroundService.MyIBinder service= (BackgroundService.MyIBinder) s;
				service.setDesktopInterface(new DesktopInterface() {
					@Override
					public void addApp(String pkgName,int page) {
						//**安装一个应用时被调用
						appList= PublicData.getAppList();
						if(appList==null)return;
						DesktopAppInfo app=appList.getApp(pkgName);
						View v=createAppView(app,app.getPage());
						wList.get(page).addView(v);
					}
					@Override
					public void removeApp(String pkgName,int page) {
						//**卸载一个应用时被调用
						if(page<0) return;
						appList=PublicData.getAppList();
						reDrawWindow(appList,page);
					}
					@Override
					public void reDrawPage(int page){
						//**重新绘制一个窗口
						//**窗口管理结束会被调用
						appList=PublicData.getAppList();
						reDrawWindow(appList,page);
					}
					@Override
					public void reboot(){
						//**完全重新加载
						Main.this.reboot();
					}
				});
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}
		},BIND_AUTO_CREATE);
	}



	public void onDestroy(){
		super.onDestroy();
		unbindService(connection);
	}


	private void init(){//--获取组件
		shortcut=findViewById(R.id.shortcut);
		parent=findViewById(R.id.parentBox);
		dotParent=findViewById(R.id.windowsDots);
		topMask=findViewById(R.id.topMask);
		//**初始化数据
		pManager=getPackageManager();

		//****入口

		initWindow();
	}


	/**
	 * 窗口滑动动画
	 * @param distance 手势水平滑动的长度
	 */
	private void switchPage(float distance){
		if(appList.getMaxPage()==1)return;
		int page=appList.getPage();
		FrameLayout layout=wList.get(page);
		float left=layout.getX()+distance;
		float topAlpha=(width-Math.abs(left)+0.0f)/width;
		if(Math.abs(left)>100){
			int nextPage;
			if(left<0){
				nextPage=page+1;
				if(nextPage>wList.size()-1)nextPage=0;

			}else{
				nextPage=page-1;
				if(nextPage<0)nextPage=wList.size()-1;
			}
			FrameLayout next=wList.get(nextPage);
			next.setVisibility(View.VISIBLE);
			next.setAlpha(1-topAlpha);
		}else {
			int pre=page-1;
			int next=page+1;
			if(pre<0)pre=wList.size()-1;
			if(next>wList.size()-1)next=0;
			wList.get(pre).setVisibility(View.GONE);
			wList.get(next).setVisibility(View.GONE);

		}
		layout.setAlpha(topAlpha);
		layout.setX(left);
	}

	/**
	 * 窗口回弹、换页动画
	 * @param critical 临界值，超过临界值就进行换页
	 */
	private void endSwitch(int critical){
		int page=appList.getPage();
		FrameLayout layout=wList.get(page);
		int nextPage;

		float left=layout.getX();
		int to=0;
		if(left<0) {
			if(left<=-critical) {
				to = -width;
			}
			nextPage=page+1;
			if(nextPage>=wList.size())nextPage=0;
		}
		else if(left>0) {
			if(left>=critical){
				to = width;
			}
			nextPage=page-1;
			if(nextPage<0)nextPage=wList.size()-1;
		}
		else return;
		endSwitchAnimator(page,nextPage,left,to);
	}

	/**
	 * 窗口回弹 切换动画
	 * @param page 当前界面
	 * @param nextPage 要切换到的界面
	 * @param from 当前界面的X值
	 * @param to 当前界面要到达的X值
	 */
	private void endSwitchAnimator(int page,int nextPage,float from,float to){
		switchAction=true;
		FrameLayout layout=wList.get(page);
		FrameLayout next=wList.get(nextPage);
		ValueAnimator animator=ValueAnimator.ofFloat(from,to);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(300);
		next.setVisibility(View.VISIBLE);
		if(to!=0) {
			resumeDot(wDList.get(appList.getPage()));
			appList.setPage(nextPage);
			selectDot(wDList.get(nextPage));
		}
		animator.addUpdateListener((animation -> {
			float val=(float) animation.getAnimatedValue();
			layout.setX(val);
			float topAlpha=(width-Math.abs(val))/width;
			layout.setAlpha(topAlpha);
			next.setAlpha(1-topAlpha);
			if(val== to){
				if(to ==0){//**回弹
					next.setVisibility(View.GONE);
				}else{
					layout.setX(0);
					layout.setVisibility(View.GONE);
					next.setVisibility(View.VISIBLE);
				}
				switchAction=false;
			}
		}));
		animator.start();
	}

	/**
	 * 设置基本的事件
	 */
	public void bind(){
		shortcut.setOnItemClickListener(new ItemClick(appList.getShortCutList()));
		topMask.setOnClickListener(v-> hiddenLongClickItem());

		topMask.findViewById(R.id.item1).setOnClickListener(this);
		topMask.findViewById(R.id.item2).setOnClickListener(this);
		topMask.findViewById(R.id.item3).setOnClickListener(this);
		topMask.findViewById(R.id.item4).setOnClickListener(this);
		topMask.findViewById(R.id.item5).setOnClickListener(this);
		topMask.findViewById(R.id.item6).setOnClickListener(this);


		parent.setOnTouchListener(new TouchDirection() {
			private boolean switchPage=false;
			private float lastDistance;
			@Override
			public void touchDirection(float distance) {
				if(switchAction||deleteAction)return;
				switchPage=true;
				lastDistance=Math.max(Math.abs(lastDistance),Math.abs(distance));
				switchPage(distance);
				releaseEvent();
			}

			private float preX,preY;
			private float originX,originY;
			@Override
			public void onTouch(MotionEvent e) {
				switch (e.getAction()){
					case MotionEvent.ACTION_DOWN:{
						preX=e.getRawX();
						preY=e.getRawY();
						originX=preX;
						originY=preY;
						switchPage=false;
						switchPaged=false;
					}break;
					case MotionEvent.ACTION_MOVE:{
						if(Math.abs(preX-e.getRawX())+Math.abs(preY-e.getRawY())>ShortCut.screenWidth()/30){
							switchPaged=true;
						}
						preX=e.getRawX();
						preY=e.getRawY();
						if(Math.abs(preX-originX)+Math.abs(preY-originY)>ShortCut.screenWidth()/30){
							switchPaged=true;
						}
					}break;
					case MotionEvent.ACTION_UP:{
						if(switchPage){
							if(Math.abs(lastDistance)>ShortCut.screenWidth()/36){//**可以判定为快速滑动
								endSwitch(ShortCut.screenWidth()/30);
							}else {
								endSwitch(width/3);
							}
						}
						switchPage=false;
						lastDistance=0;
					}break;
				}
			}

			/**
			 * 长点击,800mills未移动
			 * @param x x
			 * @param y y
			*/
			public boolean longOnClick(float x,float y){
				if(touchObj.downView==null)return false;
				preX=(int)x;
				preY=(int)y;

				touchObj.downView.setBackgroundResource(R.drawable.view_only_border);
				return true;
			}

			@Override
			public void longClickMove(float x, float y) {
				if(touchObj.objType==TouchObj.APP)
					moveApp((int)x,(int)y);
				else if(touchObj.objType==TouchObj.WIDGET){
					moveWidget((int)x,(int)y);
				}
			}

			@Override
			public void longClickUpNoMove(float x, float y) {
				Main.this.showLongClickItem();
				//releaseEvent();
			}

			@Override
			public void longClickUpMoved(float x, float y) {
				PublicData.saveData();
				releaseEvent();

			}

			@Override
			public void down(MotionEvent event) {
				preX=(int) event.getX();
				preY=(int) event.getY();
			}
		});
	}

	/**
	 * 清空事件
	 */
	private void releaseEvent(){
		if(touchObj.objType==TouchObj.WIDGET){
			touchObj.downView.setBackgroundResource(R.drawable.view_only_border_dash);
		}else{
			hiddenBorder(touchObj.downView);
		}
		touchObj.objType=TouchObj.NONE;
		touchObj.info=null;
		touchObj.widget=null;
		touchObj.downView=null;
	}

	/**
	 * 隐藏边框
	 * @param v v
	 */
	private void hiddenBorder(View v){
		if(v!=null){
			v.setBackgroundColor(Color.TRANSPARENT);
		}
	}



	private int width=0;

	//**模拟点击，手指按下是记录有可能点击的控件和数据

	private int itemW,itemH;
	/**
	 * 初始化显示
	 */
	public void initWindow(){
		topMask.setVisibility(View.GONE);
		if(width==0){//**只有第一次进入才会进行测量
			width=ShortCut.screenWidth();
			itemW=getResources().getDimensionPixelSize(R.dimen.appItemWidth);
			itemH=getResources().getDimensionPixelSize(R.dimen.appItemHeight);
		}


		AppInfoIO appInfoIO=new AppInfoIO();
		appList=PublicData.getAppList();
		if(appList==null){
			initAppList();
			appInfoIO.saveAppInfo(appList);
		}


		setAdapter();
		if(reboot){//**如果reboot，证明需要重启桌面,清空所有数据
			parent.removeAllViews();
			dotParent.removeAllViews();
			wList.clear();
			wDList.clear();
		}
		for(int page=0;page<=appList.getMaxPage();page++){
			ArrayList<DesktopAppInfo> list= appList.getPage(page);
			int visible=View.GONE;
			if(page==appList.getPage()){
				visible=View.VISIBLE;
			}
			//createWindow(page,visible);
			FrameLayout v=createFrameLayout(visible);
			wList.add(v);
			parent.addView(v);
			ImageView imageView=createDot();
			wDList.add(imageView);
			dotParent.addView(imageView);

			createDotMenu(wDList.get(page));//**添加点击事件
			addAppToWindow(list,page);//**给每个窗口添加App
			addWidgetToWindow(appList.getPageWidget(page),page);

		}
		bind();
		wList.get(appList.getPage()).setVisibility(View.VISIBLE);
		selectDot(wDList.get(appList.getPage()));
		wList.get(appList.getPage()).setVisibility(View.VISIBLE);
		reboot=false;
	}

	/**
	 * 重新绘制窗口信息
	 * @param appList 数据源
	 * @param page page
	 */
	private void reDrawWindow(AppList appList,int page){
		wList.get(page).removeAllViews();
		addAppToWindow(appList.getPage(page),page);
		addWidgetToWindow(appList.getPageWidget(page),page);
	}

	/**
	 * 给dot添加长点击事件
	 * @param dot dot view
	 */
	private void createDotMenu(ImageView dot){
		dot.setOnLongClickListener(v -> {
            if(!v.equals(wDList.get(appList.getPage()))){
                return false;
            }
            PopMenu popMenu=new PopMenu(Main.this);
            popMenu.setItemPadding(0,20,0,20);
            popMenu.addItem("删除窗口", v13 -> {
                if(appList.getMaxPage()==0) return;//**至少有一个页面
                deleteWindow(appList.getPage());
            });
            popMenu.addItem("向前添加窗口", v12 -> {
                resumeDot(wDList.get(appList.getPage()));
                wList.get(appList.getPage()).setVisibility(View.GONE);
                createWindow(appList.getPage(),View.VISIBLE);
                wList.get(appList.getPage()).setVisibility(View.VISIBLE);
                selectDot(wDList.get(appList.getPage()));
                PublicData.saveData();
            });
            popMenu.addItem("向后添加窗口", v1 -> {
                resumeDot(wDList.get(appList.getPage()));
                wList.get(appList.getPage()).setVisibility(View.GONE);
                appList.setPage(appList.getPage()+1);
                createWindow(appList.getPage(),View.VISIBLE);
                wList.get(appList.getPage()).setVisibility(View.VISIBLE);
                selectDot(wDList.get(appList.getPage()));
                PublicData.saveData();
            });

            popMenu.addItem("创建分类",v1->{
            	DesktopAppInfo app=new DesktopAppInfo();
            	app.setAppType(AppList.AppType.folder);
			});

            popMenu.setWidthPercent(0.5f);
			int theme= AppConfigManager.instance().getThemeConfig().getTheme();
			switch (theme){
				case ThemeConfig.THEME_DEFAULT:{
					/*popMenu.setParentBackground();
					popMenu.*/

				}break;
				case ThemeConfig.THEME_CARTON:{
					popMenu.setParentBackground(R.drawable.alert_bg_xml);
				}break;
			}

            popMenu.show();
            return false;
        });
	}

	/**
	 * 给窗口添加插件
	 * @param widgets widget
	 * @param page page
	 */
	private void addWidgetToWindow(ArrayList<AppWidget> widgets,int page){
		if(widgets.size()==0)return;

		widgetHost=widgetHost==null?new AppWidgetHost(this,0x200):widgetHost;
		for(final AppWidget widget:widgets){
			View box=createWidgetView(widget);
			//**添加外部box
			wList.get(page).addView(box);
		}
	}

	/**
	 * 创建插件
	 * @param widget widget
	 * @return view
	 */
	private View createWidgetView(final AppWidget widget){
		int id=widget.getId();
		AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(this);
		final AppWidgetProviderInfo info=appWidgetManager.getAppWidgetInfo(id);
		final AppWidgetHostView view=widgetHost.createView(this,id,info);
		final AppWidgetParent box=new AppWidgetParent(this);
		FrameLayout.LayoutParams params_box=new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ShortCut.px(info.minHeight)+40
		);
		params_box.gravity=Gravity.TOP;
		params_box.setMargins(widget.getX(),widget.getY(),0,0);
		box.setLayoutParams(params_box);
		box.setPadding(20,20,20,20);
		box.addView(view);
		box.setBackgroundResource(R.drawable.view_only_border_dash);
		box.setScaleX(widget.getScale());
		box.setScaleY(widget.getScale());
		box.setOnLongClickListener((v)->{
			touchObj.downView=box;
			touchObj.widget=widget;
			touchObj.objType=TouchObj.WIDGET;
			return false;
		});
		return box;
	}

	/**
	 * 给窗口添加应用
	 * @param list 应用列表
	 * @param window wid
	 */
	private void addAppToWindow(ArrayList<DesktopAppInfo> list,int window){
		for(int i=0;i<list.size();i++){//**给每页添加应用
			final DesktopAppInfo app=list.get(i);
			if(app.getIntent()==null){
                try {
                    pManager.getApplicationInfo(app.getPkgName(),PackageManager.GET_SHARED_LIBRARY_FILES);
                } catch (PackageManager.NameNotFoundException e) {
                    appList.removeApp(app.getPkgName());
                    PublicData.saveData();
                    show("removed");
                    continue;
                }
            }
			View v=createAppView(app,window);
			wList.get(window).addView(v);
		}
	}

	/**
	 * 创建一个窗口
	 * @param page 窗口位置
	 */
	private void createWindow(int page,int visible){
		FrameLayout frameLayout=createFrameLayout(visible);
		ImageView dot=createDot();
		createDotMenu(dot);
		parent.addView(frameLayout);
		dotParent.addView(dot,page);
		wList.add(page,frameLayout);
		wDList.add(page,dot);
		appList.addPage(page);
	}
	//**删除页面动画是否正在进行
	private boolean deleteAction=false;
	/**
	 * 删除一个窗口的动画
	 * @param deletePage page
	 */
	private void deleteWindow(final int deletePage){
		final int showPage;
		if(deletePage==appList.getMaxPage()){
			showPage=deletePage-1;
			appList.setPage(showPage);
			if(showPage<0) return;
		}else{
			showPage=deletePage+1;
			appList.setPage(deletePage);
		}
		deleteAction=true;
		ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,1);
		valueAnimator.setDuration(500);
		wList.get(showPage).setAlpha(0.01f);
		wList.get(showPage).setVisibility(View.VISIBLE);
		valueAnimator.addUpdateListener(animation -> {
            float val= (float) animation.getAnimatedValue();
            wList.get(deletePage).setAlpha(1-val);
            wList.get(showPage).setAlpha(val);
            if(val==1){//**完成删除
                selectDot(wDList.get(showPage));
                //***移除相关数据
                ArrayList<AppWidget> appWidgets=appList.getPageWidget(deletePage);
                for(int i=0;i<appWidgets.size();i++){
                    appList.removeAppWidget(appWidgets.get(i).getId());
                }
                appList.removePage(deletePage);
                parent.removeView(wList.get(deletePage));
                wList.remove(deletePage);
                dotParent.removeView(wDList.get(deletePage));
                wDList.remove(deletePage);
                deleteAction=false;
				PublicData.saveData();
            }
        });
		valueAnimator.start();
	}

	/**
	 * 创建一个appView
	 * @param app app
	 * @return v
	 */
	private View createAppView(final DesktopAppInfo app, final int page){
		if(app.getPage()<0) app.setPage(0);
		View v= getLayoutInflater().inflate(R.layout.item_desktop_app_item,wList.get(page),false);
		FrameLayout.LayoutParams params= (FrameLayout.LayoutParams) v.getLayoutParams();
		params.setMargins(app.getX(),0,0,app.getY());
		params.gravity= Gravity.BOTTOM;
		v.setLayoutParams(params);
		ImageView img=v.findViewById(R.id.icon);
		switch (app.getAppType()){
			case shortcut:
			case systemApp:
			case thirdPartyApp:
				if(!app.isHasOtherIcon()&&app.getIntent()==null) {
					try {
						Drawable drawable=pManager.getApplicationIcon(app.getPkgName());
						img.setImageDrawable(drawable);
					} catch (PackageManager.NameNotFoundException e) {
						show(e.toString());
						appList.removeApp(app.getPkgName());
						PublicData.saveData();
					}
				}
				else if(app.isHasOtherIcon()){
					img.setImageBitmap(app.getOtherIcon());
				}else if(app.getIntent()!=null){
					String suffix=app.getPkgName();
					int pos=suffix.lastIndexOf('.');
					if(pos>=0){
						suffix=suffix.substring(pos+1);
					}else{
						suffix=null;
					}

					img.setImageResource(getDrawable(suffix));
				}
				break;
			case file:
			case folder://**显示分类图标

		}

		TextView textView=v.findViewById(R.id.title);
		//**是否显示App名称
		if(AppConfigManager.instance().getCommonConfig().isShowText()) {
			textView.setText(app.getOtherLabel() == null ? app.getLabel() : app.getOtherLabel());
			//**显示每次则设置字体颜色
			textView.setTextColor(AppConfigManager.instance().getCommonConfig().getTextColor());
		}



		v.setOnClickListener((view)->{
			if(switchPaged)return;
			if(app.getIntent()!=null){
				try {
					startActivity(Intent.parseUri(touchObj.info.getIntent(),Intent.URI_INTENT_SCHEME));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}else {
				try {
					addClickAmount(app.getPkgName());
					openActivity(app.getPkgName(),app.getName());
				}catch (Exception e){
					e.printStackTrace();
				}

			}
			releaseEvent();
		});
		v.setOnLongClickListener((view)->{
			touchObj.objType=TouchObj.APP;
			touchObj.downView= view;
			touchObj.info=app;
			return true;
		});

		/*v.setOnTouchListener((v1, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                if(touchObj.info!=null) return false;
                touchObj.objType=TouchObj.APP;
                touchObj.downView= v1;
                touchObj.info=app;
            }
            return false;
        });*/
		v.setScaleX(app.getScale());
		v.setScaleY(app.getScale());

		return v;
	}

	/**
	 * 创建一个窗口
	 * @return frameLayout
	 */
	private FrameLayout createFrameLayout(int visible){
		FrameLayout frameLayout=new FrameLayout(this);
		frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT));

		frameLayout.setAlpha(1);
		frameLayout.setVisibility(visible);
		return frameLayout;
	}

	/**
	 * 创建窗口对应的dot
	 * @return dot
	 */
	private ImageView createDot(){
		ImageView v= (ImageView) LayoutInflater.from(this).inflate(R.layout.app_dot,dotParent,false);
		v.setOnClickListener(obj->{
			int page=appList.getPage();
			for(int i=0;i<wDList.size();i++){
				if(v.equals(wDList.get(i))&&page!=i){
					if(i<page)
						endSwitchAnimator(appList.getPage(),i,0,width);
						//switchPage(TouchDirection.TOUCH_RIGHT,i,600);
					else
						endSwitchAnimator(appList.getPage(),i,0,-width);
						//switchPage(TouchDirection.TOUCH_LEFT,i,600);
				}
			}
		});

		resumeDot(v);
		return v;
	}

	/**
	 * 选中一个dot
	 * @param dot dot
	 */
	private void selectDot(ImageView dot){
		dot.setPadding(0,0,0,0);
		dot.setImageResource(R.drawable.dot_select);
	}

	/**
	 * 恢复一个dot
	 * @param dot dot
	 */
	private void resumeDot(ImageView dot){
		int px=ShortCut.px(2);
		dot.setPadding(px,px,px,px);
		dot.setImageResource(R.drawable.dot);
	}

	/**
	 * 初始化应用列表
	 */
	private void initAppList(){
		int wNum=4;
		int baseLeft=(ShortCut.screenWidth()/wNum-itemW)/2;
		int pageAppNum=8;
		//***获取所有可启动的应用
		Intent intent=new Intent(Intent.ACTION_MAIN,null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> mainData=pManager.queryIntentActivities(intent, 0);

		appList=new AppList();
		int index=0;
		for(int i=0;i<mainData.size();i++){//--生成屏幕下方的数据
			String nameString= mainData.get(i).activityInfo.applicationInfo.packageName;
			ResolveInfo info=mainData.get(i);
			if(nameString.indexOf("mms")>0||nameString.indexOf("contacts")>0||
					nameString.indexOf("camera")>0){
				if(appList.getShortCutList().size()<4){
					appList.addShortCutApp(new DesktopAppInfo(
							info.activityInfo.packageName,
							info.activityInfo.name,
							info.loadLabel(pManager).toString(),
							0,
							-1,
							-1,
							AppList.AppType.systemApp
					));
				}
			}
			else{
				AppList.AppType appType= AppList.AppType.thirdPartyApp;
				if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0){
					appType= AppList.AppType.systemApp;
				}
				DesktopAppInfo app;

				appList.addApp(app=new DesktopAppInfo(
						info.activityInfo.packageName,
						info.activityInfo.name,
						info.loadLabel(pManager).toString(),
						index/pageAppNum,
						baseLeft*((index%wNum+1)*2-1)+(index%wNum)*itemW,
						itemH*(index%pageAppNum/wNum),
						appType
				));
				app.setGx(index%wNum);
				app.setGy(index%pageAppNum/wNum);
				app.setWeight(100);

				index++;
			}
		}
	}



	private boolean switchAction=false;
	public void setAdapter(){//--设置适配器
		GridViewAdapter adapterShort = new GridViewAdapter(Main.this, R.layout.item_desktop_app_item,appList.getShortCutList(), pManager);
		shortcut.setAdapter(adapterShort);
	}

	/**
	 * 记录点击量，应用的点击+1
	 * @param pkgName packageName
	 */
	private void addClickAmount(String pkgName){
		if(!AppConfigManager.instance().getCommonConfig().isRecordClick())return;//**未开启点击数++
		new ClickDataIO().addClick(pkgName);
	}
	/**
	 * 打开一个应用
	 * @param pkgName packageName
	 * @param name className
	 */
	private void openActivity(String pkgName,String name){
		Intent intent=new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(new ComponentName(pkgName, name));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		try {
			startActivity(intent);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	private class ItemClick implements OnItemClickListener{//--打开一个应用
		ArrayList<DesktopAppInfo> data;
		ItemClick(ArrayList<DesktopAppInfo> data){
			this.data=data;
		}
		public void onItemClick(AdapterView<?> arg0, View v, int position,
								long arg3) {//--点击进入应用，同时记录点击次数
			String pkgName=data.get(position).getPkgName();
			if(AppConfigManager.instance().getCommonConfig().isRecordClick()){
				//***是否记录点击
				addClickAmount(pkgName);
			}

			openActivity(pkgName,data.get(position).getName());
		}
	}




	/**
	 * 按键操作
	 */
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		if(keyCode==KeyEvent.KEYCODE_BACK){//--屏蔽后退
			return true;
		}else if(keyCode==KeyEvent.KEYCODE_MENU){
			createMenu();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 显示主题选项
	 */
	private void showThemeSelect(){
		PopMenu popMenu=new PopMenu(this);
		popMenu.addItem("默认", v -> {
            AppConfigManager.instance().getThemeConfig().setTheme(ThemeConfig.THEME_DEFAULT);
        }).setPadding(0,20,0,20);
		
		popMenu.addItem("二次元", v -> {
			AppConfigManager.instance().getThemeConfig().setTheme(ThemeConfig.THEME_CARTON);
        }).setPadding(0,20,0,20);
		popMenu.setWidthPercent(0.5f);
		popMenu.show();
	}

	//**菜单view
	private ArrayList<View> menuItems=new ArrayList<>();
	/**
	 * 建立一个菜单
	 */
	private void createMenu(){
		if(boxMenu==null){
			boxMenu=new BoxMenu(this);
			menuItems.add(boxMenu.addItem("添加", v -> chooseAppWidget()));
			menuItems.add(boxMenu.addItem("主题", v -> showThemeSelect()));
			menuItems.add(boxMenu.addItem("壁纸",null));
			menuItems.add(boxMenu.addItem("应用管理", null));
			menuItems.add(boxMenu.addItem("屏幕管理", v -> {
                Intent intent=new Intent(Main.this,AppSelectAdd.class);
                intent.putExtra("page",appList.getPage());
                //Main.this.startActivity(intent);
                Main.this.startActivityForResult(intent,3);
            }));
			menuItems.add(boxMenu.addItem("桌面设置", v -> {
                Intent intent=new Intent(Main.this,Setting.class);
                startActivity(intent);
            }));
			showMenu();
		}else if(!boxMenu.isShow()){
			showMenu();
		}
	}
	private void showMenu(){
		//***根据主题显示对应样式
		int theme= AppConfigManager.instance().getThemeConfig().getTheme();
		switch (theme){
			case ThemeConfig.THEME_CARTON:{
				@DrawableRes int res[]=new int[]{R.drawable.bg_item1,R.drawable.bg_item2,R.drawable.bg_item3,
						R.drawable.bg_item4,R.drawable.bg_item5,R.drawable.bg_item6};
				for(int i=0;i<menuItems.size();i++){
					menuItems.get(i).setBackgroundResource(res[i]);
				}
			}break;
			case ThemeConfig.THEME_DEFAULT:{
				for(int i=0;i<menuItems.size();i++){
					menuItems.get(i).setBackgroundColor(Color.WHITE);
				}
			}
		}

		boxMenu.show();
	}
	private int preX=-1,preY=-1;
	private double preMoveTime=0;

	/**
	 * 移动app位置
	 */
	private void moveApp(int dx,int dy){
		//**空对象或者切换动画未停止是禁止移动app的
		if(touchObj.objType!=TouchObj.APP||touchObj.downView==null||switchAction) return;
		FrameLayout.LayoutParams params= (FrameLayout.LayoutParams) touchObj.downView.getLayoutParams();
		int left=params.leftMargin+dx;
		int bottom=params.bottomMargin-dy;
		//**左移动app到另一窗口
		if(left<-20){
			if(preMoveTime==0){
				preMoveTime=System.currentTimeMillis();
			}
			else if(System.currentTimeMillis()-preMoveTime>800){
				preMoveTime=System.currentTimeMillis();
			}else{
				return;
			}
			wList.get(appList.getPage()).removeView(touchObj.downView);//**从旧的窗口移除app
			appList.removeApp(touchObj.info.getPkgName());//**从存储列表中移除app
			//left=-20;//**app在新页面的新位置
			int nextPage=appList.getPage()-1;
			if(nextPage<0) nextPage=appList.getMaxPage();
			endSwitchAnimator(appList.getPage(),nextPage,0,width);//**换页面
			wList.get(appList.getPage()).addView(touchObj.downView);//**在新页面显示App
			touchObj.info.setPage(appList.getPage());//**重新设置app所属页面
			appList.addApp(touchObj.info);//**更新存储列表
			return;
		}else if(left>width-itemW+20){
			if(preMoveTime==0){
				preMoveTime=System.currentTimeMillis();
			}
			if(System.currentTimeMillis()-preMoveTime>800){
				preMoveTime=System.currentTimeMillis();
			}else{
				return;
			}
			wList.get(appList.getPage()).removeView(touchObj.downView);
			//left=width-itemW+20;
			int nextPage=appList.getPage()+1;
			if(nextPage>appList.getMaxPage()) nextPage=0;
			endSwitchAnimator(appList.getPage(),nextPage,0,-width);
			touchObj.info.setPage(appList.getPage());
			wList.get(appList.getPage()).addView(touchObj.downView);
			appList.addApp(touchObj.info);//**更新存储列表
			return;
		}
		params.setMargins(left,0,0,bottom);
		touchObj.info.setX(left);
		touchObj.info.setY(bottom);
		int res[]=new int[3];//**计算位置时返回的结果存储数组
		ShortCut.getGxGy(left,bottom,res);//***修改对象的gx gy和weight
		touchObj.info.setGx(res[0]);
		touchObj.info.setGy(res[1]);
		touchObj.info.setWeight(res[2]);

		touchObj.downView.setLayoutParams(params);
	}

	/**
	 * 移动插件
	 */
	private void moveWidget(int dx,int dy){
		if(touchObj.objType!=TouchObj.WIDGET||touchObj.downView==null) return;
		FrameLayout.LayoutParams params= (FrameLayout.LayoutParams) touchObj.downView.getLayoutParams();
		int left=params.leftMargin+dx;
		int top=params.topMargin+dy;

		params.setMargins(left,top,0,0);
		touchObj.widget.setX(left);
		touchObj.widget.setY(top);
		touchObj.downView.setLayoutParams(params);
	}

	/**
	 * 消除topMask
	 */
	private void hiddenLongClickItem(){
		topMask.setVisibility(View.GONE);
		releaseEvent();
	}

	/**
	 * 长点击
	 */
	private void showLongClickItem(){
		if(touchObj.downView==null) return;
		topMask.setVisibility(View.VISIBLE);
		LinearLayout re=topMask.findViewById(R.id.longClickBox);

		
		int reW=ShortCut.px(40*re.getChildCount());
		int reH=ShortCut.px(50);
		int left=touchObj.downView.getLeft();
		int top=touchObj.downView.getTop();
		left=left-reW/2+itemW/2;
		top=top-reH-20;
		if(left<=0) left=50;
		if(top<=0) top=50;
		if(width-left<reW){
			left=width-reW-50;
		}

		RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) re.getLayoutParams();
		params.setMargins(left,top,0,0);
		re.setLayoutParams(params);

		//**设置主题样式
		int theme= AppConfigManager.instance().getThemeConfig().getTheme();
		switch (theme){
			case ThemeConfig.THEME_DEFAULT:{
				re.setBackgroundColor(Color.WHITE);
			}break;
			case ThemeConfig.THEME_CARTON:{
				re.setBackgroundColor(getResources().getColor(R.color._blue));
			}break;
		}
	}

	/**
	 * 点击事件
	 * @param v veiw
	 */
	public void onClick(View v){
		switch (v.getId()){
			case R.id.item1:{//**隐藏
				try{
					if(touchObj.downView==null) return;
					if(touchObj.objType==TouchObj.APP){
						appList.remove(touchObj.info);
						touchObj.downView.setVisibility(View.GONE);
					}else if(touchObj.objType==TouchObj.WIDGET){
						appList.removeAppWidget(touchObj.widget);
						wList.get(appList.getPage()).removeView(touchObj.downView);
					}
					hiddenLongClickItem();
					PublicData.saveData();
				}catch (Exception e){
					e.printStackTrace();
				}

			}break;
			case R.id.item2:{//**卸载
				if(touchObj.downView==null) return;
				if(touchObj.objType==TouchObj.APP) {
					Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + touchObj.info.getPkgName()));
					startActivity(intent);
					hiddenLongClickItem();
				}
			}break;
			case R.id.item3:{//**改应用名
				if(touchObj.objType!=TouchObj.APP||touchObj.downView==null||touchObj.info==null)return;
				InputDialog dialog=new InputDialog(Main.this);
				dialog.setTitle("修改应用名称");
				dialog.setMsg(touchObj.info.getLabel());
				dialog.setLeft("取消", new InputDialog.OnClickListener() {
					@Override
					public void onClick(String text) {
						hiddenLongClickItem();
					}
				});
				dialog.setRight("修改", new InputDialog.OnClickListener() {
					@Override
					public void onClick(String text) {
						touchObj.info.setOtherLabel(text);
						((TextView)touchObj.downView.findViewById(R.id.title)).setText(text);
						hiddenLongClickItem();
						PublicData.saveData();
					}
				});
				dialog.show();
			}break;
			case R.id.item4:{//**改应用图标
				if(touchObj.objType!=TouchObj.APP||touchObj.info==null)return;
				Intent intent=new Intent(Main.this,IconReplace.class);
				intent.putExtra("pkgName",touchObj.info.getPkgName());
				hiddenLongClickItem();
				startActivityForResult(intent,4);
			}break;

			case R.id.item5:{//**放大
				if(touchObj.downView==null) return;
				if(touchObj.objType==TouchObj.APP) {
					if(touchObj.info.getScale()>=2)return;
					touchObj.info.setScale(touchObj.info.getScale() + 0.2f);
					touchObj.downView.setPivotX(0);
					touchObj.downView.setPivotY(0);
					touchObj.downView.setScaleX(touchObj.info.getScale());
					touchObj.downView.setScaleY(touchObj.info.getScale());
					PublicData.saveData();
				}
			}break;
			case R.id.item6:{//**缩小
				if(touchObj.downView==null) return;
				if(touchObj.objType==TouchObj.APP) {
					if(touchObj.info.getScale()<=0.5) return;
					touchObj.info.setScale(touchObj.info.getScale() - 0.2f);
					touchObj.downView.setPivotX(0);
					touchObj.downView.setPivotY(0);
					touchObj.downView.setScaleX(touchObj.info.getScale());
					touchObj.downView.setScaleY(touchObj.info.getScale());
					PublicData.saveData();
				}
			}break;
		}
	}

	/**
	 * 根据返回的id创建插件
	 * @param requestCode req
	 * @param resultCode res
	 * @param intent intent
	 */
	public void onActivityResult(int requestCode,int resultCode,Intent intent){
		if(resultCode==RESULT_OK) {
			if(requestCode==2){//******添加插件
				int id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);
				final AppWidget widget = new AppWidget(id,appList.getPage(),0,0);
				appList.addAppWidget(widget);
				//**添加外部box
				View box=createWidgetView(widget);
				wList.get(appList.getPage()).addView(box);
				PublicData.saveData();
			}
			else if(requestCode==3||requestCode==4){//***图标替换、窗口管理后需要更新窗口
				appList=PublicData.getAppList();
				reDrawWindow(appList,appList.getPage());
			}
		}

	}



	private void chooseAppWidget(){
		widgetHost=new AppWidgetHost(this,0x200);
		widgetHost.startListening();
		//**发出选择请求
		int wid=widgetHost.allocateAppWidgetId();
		Intent intent=new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,wid);
		startActivityForResult(intent,2);
	}
	
	private void reboot(){
		reboot=true;
		initWindow();
	}

	/**
	 * 程序被中止时保存当前所在的页面信息
	 */
	public void onPause(){
		PublicData.saveData();
		super.onPause();
	}

	private @DrawableRes int getDrawable(String suffix){
		int drawable=R.drawable.file;
		if(suffix==null){
			return drawable;
		}
		switch (suffix.toLowerCase()){
			case "txt":
			case "text":drawable=R.drawable.txt;break;
			case "gpeg":
			case "bmp":
			case "gif":
			case "jpg":
			case "png":drawable=R.drawable.img;break;
			case "zip":drawable=R.drawable.zip;break;
			case "rar":drawable=R.drawable.rar;break;
		}
		return drawable;
	}



	private static class TouchObj{
		private View downView;
		private @TYPE int objType=NONE;
		private DesktopAppInfo info;
		private AppWidget widget;
		private final static int APP=0;
		private final static int WIDGET=1;
		private final static int NONE=-1;
		@IntDef({APP,WIDGET,NONE})
		@interface TYPE{}
	}
}
























