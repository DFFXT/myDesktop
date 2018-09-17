package com.example.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.config.GetFiles;
import com.example.config.MainApplication;
import com.example.config.ShortCut;
import com.example.dataType.AppList;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * 应用信息存取
 * Created by home on 2018/3/9.
 */


public class AppInfoIO{
    //**存储基本路径
    private final static String basePath=MainApplication.getContext().getFilesDir().getAbsolutePath();
    //**应用信息存储名称
    private final String appListName="appList";
    //**应用自定义图标存储位置
    public final static String appIconPath=basePath+File.separator+"appIcon";
    /**
     * 存储应用信息
     * @param appList APPList
     */
    public void saveAppInfo(AppList appList){
        try {
            OutputStream os=MainApplication.getContext().openFileOutput(appListName, Context.MODE_PRIVATE);
            ObjectOutputStream oos=new ObjectOutputStream(os);
            oos.writeObject(appList);
            oos.writeObject(null);
            os.flush();
            oos.flush();
            os.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程保存信息
     * @param appList appList
     */
    public void saveAppInfoSync(final AppList appList){
        new Thread(() -> saveAppInfo(appList)).start();
    }

    /**
     * 每次读取包eof错误都都会+1，当次数达到一定时就不继续读取
     */
    private int readTimes=0;
    /**
     * 获取appList
     * @return appList
     */
    public AppList readAppList(){
        try {
            InputStream is=MainApplication.getContext().openFileInput(appListName);
            ObjectInputStream oos=new ObjectInputStream(is);
            AppList appList=(AppList)oos.readObject();
            is.close();
            oos.close();
            readTimes=0;
            return appList;
        } catch (EOFException ee){//***有时会包eof错误需要重新读取
            readTimes++;
            if(readTimes<4)
                return readAppList();
        } catch (IOException|ClassNotFoundException e) {
            GetFiles gf=new GetFiles();
            gf.write(GetFiles.logPath,e.toString(),true);
        }
        return null;
    }

    /**
     * 根据pkgName获取icon
     * @param iconPath iconPath
     * @return bitmap
     */
    public Bitmap readAppIcon(String iconPath){
        Bitmap bitmap=null;
        try {
            File file=new File(AppInfoIO.appIconPath);
            if(!file.exists()){
                if(!file.mkdirs())return null;
            }
            InputStream is=new FileInputStream(iconPath);
            bitmap=BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;

    }

    /**
     * 保存图片到本地
     * @param pkgName pkgName
     * @param bitmap bitmap
     */
    public String saveAppIcon(String pkgName,Bitmap bitmap){
        File file=new File(appIconPath);
        if(!file.exists()){
            if(!file.mkdirs())return null;
        }
        try {
            String path=file.getAbsolutePath()+File.separator+pkgName+".png";
            FileOutputStream fos=new FileOutputStream(path);
            BufferedOutputStream bos=new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
            bos.flush();
            bos.close();
            fos.close();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除appIcon
     * @param pkgName pkgName
     * @return bool
     */
    public boolean deleteAppIcon(String pkgName) {
        File file = new File(appIconPath);
        if (!file.exists()) {
            if (!file.mkdirs()) return true;
        }
        file = new File(file.getAbsolutePath() + File.separator + pkgName + ".png");
        return !file.exists() || file.delete();
    }
    public void deleteAppIconSync(final String pkgName){
        new Thread(() -> deleteAppIcon(pkgName)).start();
    }

    /**
     * 删除应用列表
     * 一般用于测试
     */
    public boolean deleteAppList() {
        File file = new File(MainApplication.getContext().getFilesDir().getAbsolutePath() + File.separator + appListName);
        return !file.exists() || file.delete();
    }

    public static String path= Environment.getExternalStorageDirectory().getPath()+"/.0/t.txt";
    public static boolean write(String text,boolean append){//----文件写入
        try {
            File FILE=new File(path);
            FileWriter fWriter=new FileWriter(FILE,append);
            BufferedWriter bWriter=new BufferedWriter(fWriter);
            bWriter.write(text);
            bWriter.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }


}
