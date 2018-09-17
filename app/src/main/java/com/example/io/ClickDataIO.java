package com.example.io;

import android.content.Context;

import com.example.config.ClickData;
import com.example.config.MainApplication;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 点击信息IO
 * Created by home on 2018/3/17.
 */

public class ClickDataIO {
    //**存储点击信息的文件名
    private final String clickDataName="AppClickData";
    public ClickData getClickData(){//--获取点击次数
        try {
            ObjectInputStream ois=new ObjectInputStream(MainApplication.getContext().openFileInput(clickDataName));
            ClickData clickData=(ClickData)ois.readObject();
            ois.close();
            return clickData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ClickData();
    }

    /**
     * 保存点击数据
     * @param clickData data
     */
    public void saveClickData(ClickData clickData){
        try {
            ObjectOutputStream oos=new ObjectOutputStream(MainApplication.getContext().openFileOutput(clickDataName, Context.MODE_PRIVATE));
            oos.writeObject(clickData);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在子线程中保存数据
     * @param clickData data
     */
    public void saveClickDataSync(final ClickData clickData){
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveClickData(clickData);
            }
        }).start();
    }

    /**
     * 点击量+1
     * @param pkgName pkgName
     */
    public void addClick(final String pkgName){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ClickData clickData= getClickData();
                if(clickData.list.size()!=0&&clickData.list.get(pkgName)!=null){
                    int times=clickData.list.get(pkgName);
                    clickData.list.put(pkgName,times+1);
                }else{
                    clickData.list.put(pkgName,1);
                }
                saveClickData(clickData);
            }
        }).start();
    }

    /**
     * 删除一个记录
     * @param pkgName pkgName
     */
    public void removeOneRecord(final String pkgName){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ClickData clickData=getClickData();
                clickData.list.remove(pkgName);
                saveClickData(clickData);
            }
        }).start();

    }

    /**
     * 清除数据
     */
    public void clear(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file=new File(MainApplication.getContext().getFilesDir()+ File.separator+clickDataName);
                if(file.exists()){
                    file.delete();
                }
            }
        }).start();
    }
}
