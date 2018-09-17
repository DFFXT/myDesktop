package com.example.background;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.example.config.PublicData;
import com.example.config.ShortCut;
import com.example.dataType.AppList;
import com.example.dataType.DesktopAppInfo;
import com.example.interface_.DesktopInterface;
import com.example.io.AppInfoIO;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class BackgroundService extends Service{
	private DesktopInterface desktopInterface;
	public class MyIBinder extends Binder {
		private BackgroundService service;
		MyIBinder(BackgroundService service){
			this.service=service;
		}

		/**
		 * 传入接口
		 * @param desktopInterface interface
		 */
		public void setDesktopInterface(DesktopInterface desktopInterface){
			BackgroundService.this.desktopInterface=desktopInterface;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		return new MyIBinder(this);
	}
	public int onStartCommand(Intent intent,int flag,int startId){
		String action=intent.getAction();
		Log.i("server",action+"");
		if(action==null) return START_NOT_STICKY;
		//**安装了一个应用
		switch (action) {
			case Intent.ACTION_PACKAGE_ADDED:
				installApp(intent);
				break;
			//**卸载了一个应用
			case Intent.ACTION_PACKAGE_REMOVED:
				removeApp(intent);
				break;
			//**对窗口进行了管理
			case "windowManage":
				if (desktopInterface != null) {
					int page = intent.getIntExtra("page", -1);
					desktopInterface.reDrawPage(page);
				}
				break;
			case "recovery":
				if (desktopInterface != null) {
					desktopInterface.reboot();
				}
				break;
			//***监听到创建了快捷方式
			case "com.android.launcher.action.INSTALL_SHORTCUT":
				createShortcut(intent);
				break;
		}
		return START_NOT_STICKY;
	}

	/**
	 * 安装了一个App
	 * @param intent intent
	 */
	private void installApp(Intent intent){
		if(intent.getData()!=null){
			String pkgName=intent.getData().getSchemeSpecificPart();
			String name=getAppStartActivity(pkgName);
			AppList appList=PublicData.getAppList();
			PackageManager pm=getPackageManager();
			try {
				ApplicationInfo info=pm.getApplicationInfo(pkgName,0);
				int page=appList.getPage();//**选择当前页面


				//**创建一个App对象
				DesktopAppInfo app=new DesktopAppInfo(
						pkgName,
						name,
						pm.getApplicationLabel(info).toString(),
						page,
						50,
						400,
						AppList.AppType.thirdPartyApp
				);
				ShortCut.setPosition(appList,app,page);
				appList.addApp(app);
				PublicData.saveData();
				if(desktopInterface!=null)
					desktopInterface.addApp(pkgName,page);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 移除一个App
	 * @param intent intent
	 */
	private void removeApp(Intent intent){
		if(intent.getData()!=null){
			AppList appList= PublicData.getAppList();
			String pkgName=intent.getData().getSchemeSpecificPart();
			DesktopAppInfo app=appList.getApp(pkgName);
			if(app==null) return ;
			int page=app.getPage();
			appList.removeApp(pkgName);
			PublicData.saveData();
			if(desktopInterface!=null)
				desktopInterface.removeApp(pkgName,page);
		}
	}

	/**
	 * 监听到创建快捷方式
	 * @param intent intent
	 */
	private void createShortcut(Intent intent){
		String action=intent.getAction();
		if(action==null)return;
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N_MR1){
			ShortcutManager sm=getSystemService(ShortcutManager.class);

		}else{

			createAppWidgetShortCut(intent);

		}
	}
	private void createAppWidgetShortCut(Intent intent){
		String label=intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		Parcelable bitmap=intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
		Intent sIntent=intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		ComponentName componentName=sIntent.getComponent();


		AppList appList=PublicData.getAppList();
		int page=appList.getPage();
		String pkg;
		String cln;
		if(Intent.ACTION_VIEW.equals(sIntent.getAction())||componentName==null){

			pkg=sIntent.getDataString();//****快捷方式的指向路径
			cln=sIntent.getDataString();
			if(sIntent.getType()==null){//***添加的是查看的快捷方式，但是没有MIME，需要添加全匹配
				sIntent.setType("*/*");
			}
		}else{
			pkg=componentName.getPackageName();
			cln=componentName.getClassName();
		}
		DesktopAppInfo app=new DesktopAppInfo(
				pkg,
				cln,
				label,
				page,
				50,
				400,
				AppList.AppType.shortcut
		);
		app.setIntent(sIntent.toUri(Intent.URI_INTENT_SCHEME));
		Log.i("log",sIntent.toString());
		Log.i("log",app.getIntent());
		try {
			Log.i("log",Intent.parseUri(app.getIntent(),Intent.URI_INTENT_SCHEME).toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		ShortCut.setPosition(appList,app,page);
		if(!appList.addApp(app)){
			show("已经存在啦");
			return ;
		}else {
			show("添加成功");
		}
		if(bitmap!=null)
			appList.replaceIcon(app, (Bitmap) bitmap);
		else{//**快捷方式没有bitmap
			//**一般为文件快捷方式，需要根据后缀判断需要显示的图片类型
			//app.setHasOtherIcon(true);
			//app.setIconPath();
		}
		PublicData.saveData();
		if(desktopInterface!=null)
			desktopInterface.reDrawPage(app.getPage());
	}


	private void show(Object str){
		Toast.makeText(this, str.toString(), Toast.LENGTH_LONG).show();
	}

	/**
	 * 根据pkgName获取启动类
	 * @param pkgName 包名
	 * @return startActivityName
	 */
	private String getAppStartActivity(String pkgName){
		Intent intent=new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> list=getPackageManager().queryIntentActivities(intent,0);
		for(int i=0;i<list.size();i++){
			if(list.get(i).activityInfo.packageName.equals(pkgName)){
				return list.get(i).activityInfo.name;
			}
		}
		return null;
	}




}










