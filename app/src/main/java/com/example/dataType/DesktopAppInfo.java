package com.example.dataType;

import android.graphics.Bitmap;

import com.example.io.AppInfoIO;

import java.io.Serializable;

/**
 * 桌面应用信息
 * Created by home on 2018/3/6.
 */

public class DesktopAppInfo implements Serializable{
    //**应用类型
    private AppList.AppType appType;
    //**需要创建的Intent
    private String intent;
    //**地址
    //**启动类名称
    private String name;
    //**包名
    private String pkgName;
    //**默认应用名称
    private String label;
    //**自定义名称
    private String otherLabel;
    //**是否有自定义图标
    private boolean hasOtherIcon=false;
    //**自定义图标的位置
    private String iconPath;

    //**应用所在的页面
    private int page;
    //**应用所在的位置
    private int x,y;
    //**应用所占的格子，屏幕分成格子状
    private int gx,gy;
    //**所占格子的比重【0-100】值越大，占用格子的空间就越多
    private int weight;
    //**应用图标放大系数
    private float scale=1;

    private transient Bitmap icon;
    public DesktopAppInfo(String pkgName,String name,String label,int page,int x,int y,AppList.AppType appType){
        this.pkgName=pkgName;
        this.label=label;
        this.name=name;
        this.page=page;
        this.x=x;
        this.y=y;
        this.appType=appType;
    }
    public DesktopAppInfo(){}

    public void setGx(int gx) {
        this.gx = gx;
    }

    public void setGy(int gy) {
        this.gy = gy;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setOtherLabel(String otherLabel) {
        this.otherLabel = otherLabel;
    }

    public void setHasOtherIcon(boolean hasOtherIcon) {
        this.hasOtherIcon = hasOtherIcon;
    }

    public void setSystemApp(boolean systemApp) {
        systemApp = systemApp;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setAppType(AppList.AppType appType) {
        this.appType = appType;
    }

    public boolean isHasOtherIcon() {
        return hasOtherIcon;
    }

    public Bitmap getOtherIcon() {
        if(icon==null){
            icon=new AppInfoIO().readAppIcon(iconPath);
        }
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public String getOtherLabel() {
        return otherLabel;
    }

    public String getName() {
        return name;
    }

    public String getPkgName() {
        return pkgName;
    }

    public int getPage() {
        return page;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getScale() {
        return scale;
    }

    public int getGx() {
        return gx;
    }

    public int getGy() {
        return gy;
    }

    public int getWeight() {
        return weight;
    }

    public String getIntent() {
        return intent;
    }

    public AppList.AppType getAppType() {
        return appType;
    }


    public void clearIcon(){
        if(hasOtherIcon)
            new AppInfoIO().deleteAppIconSync(pkgName);
        hasOtherIcon=false;
    }
    public void clearLabel(){
        otherLabel=null;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }
}
