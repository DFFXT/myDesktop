package com.example.dataType;

import org.litepal.crud.DataSupport;

public class AppList_sql extends DataSupport{
    //**应用类型
    private AppList.AppType appType;
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
}
