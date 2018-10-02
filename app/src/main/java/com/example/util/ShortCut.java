package com.example.util;

import android.os.Message;
import android.util.Log;
import android.util.TypedValue;

import com.example.config.MainApplication;
import com.example.dataType.AppList;
import com.example.dataType.DesktopAppInfo;
import com.example.desktop.R;

import java.util.ArrayList;

/**
 * Created by home on 2018/3/7.
 */

public class ShortCut {
    public static Message makeMsg(int what,Object obj){
        return ShortCut.makeMsg(what,obj,0,0);
    }
    public static Message makeMsg(int what,Object obj,int arg1,int atg2){
        Message msg=new Message();
        msg.what=what;
        msg.obj=obj;
        msg.arg1=arg1;
        msg.arg2=atg2;
        return msg;
    }
    /**
     * dp转px
     * @param dp dp
     * @return px
     */
    public static int px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp, MainApplication.getContext().getResources().getDisplayMetrics());
    }


    /**
     * 确定App的位置
     * @param appList appList
     * @param newApp 新添加的APP
     * @param page page
     */
    public static boolean setPosition(AppList appList, DesktopAppInfo newApp, int page){
        if(page>appList.getMaxPage()){//**页面参数不对，强制写入坐标
            newApp.setY(400);
            newApp.setX(50);
            newApp.setWeight(100);
            return true;
        }
        ArrayList<DesktopAppInfo> apps=appList.getPage(page);
        int screenWidth= ShortCut.screenWidth();
        int itemW=MainApplication.getContext().getResources().getDimensionPixelSize(R.dimen.appItemWidth);
        int itemH=ShortCut.px(74);

        boolean hasSpace=false;
        for(int gy=0;gy<6&&!hasSpace;gy++){
            for(int gx=0;gx<4;gx++){
                DesktopAppInfo app;
                for(int i=0;i<apps.size();i++){
                    app=apps.get(i);
                    if(app.getGx()==gx&&app.getGy()==gy&&app.getWeight()>60){
                        hasSpace=false;
                        break;
                    }
                    hasSpace=true;
                }
                if(hasSpace){
                    Log.i("log",gx+"+"+gy);
                    newApp.setX(gx*screenWidth/4+(screenWidth/4-itemW)/2);
                    newApp.setY(gy*itemH+10);
                    newApp.setGx(gx);
                    newApp.setGy(gy);
                    newApp.setWeight(100);
                    break;
                }
            }
        }
        return hasSpace;
    }

    public static void getGxGy(int x,int y,int[] gxGyWeight){
        int screenWidth= ShortCut.screenWidth();
        int itemW=MainApplication.getContext().getResources().getDimensionPixelSize(R.dimen.appItemWidth);
        int itemH=ShortCut.px(70);
        int gx=(x+itemW/2)/(screenWidth/4);
        int gy=(y+itemH/2)/itemH;
        int xy[]=new int[2];
        getXY(gx,gy,xy);
        gxGyWeight[0]=gx;
        gxGyWeight[1]=gy;
        gxGyWeight[2]=100-Math.abs(xy[0]-x)-Math.abs(xy[1]-y);
        double a=Math.sqrt((xy[0]-x)*(xy[0]-x)+(xy[1]-y)*(xy[1]-y));
        double b=Math.sqrt(itemH*itemH/4+screenWidth*screenWidth/64);
        gxGyWeight[2]=100-(int) (100*a/b);
    }

    public static void getXY(int gx,int gy,int[] xy){
        int screenWidth= ShortCut.screenWidth();
        int itemW=MainApplication.getContext().getResources().getDimensionPixelSize(R.dimen.appItemWidth);
        int itemH=ShortCut.px(70);
        int gap=(screenWidth/4-itemW)/2;
        int x=gx*screenWidth/4+gap;
        int y=gy*itemH;
        xy[0]=x;
        xy[1]=y;
    }

    public static void sleep(int mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
















    /**
     * 保存数据
     */
    public static int screenWidth(){
        return MainApplication.getContext().getResources().getDisplayMetrics().widthPixels;
    }
    public static int screenHeight(){
        return MainApplication.getContext().getResources().getDisplayMetrics().heightPixels;
    }


}
