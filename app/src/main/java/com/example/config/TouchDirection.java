package com.example.config;

import android.support.annotation.IntDef;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 判断Touch的方向
 * Created by home on 2018/2/15.
 */

public abstract class TouchDirection implements View.OnTouchListener{
    private int id=0;//***点击的id
    private float preX,preY;//**点击位置
    private float movePreX,movePreY;
    private boolean up;
    private boolean longClicked=false;//**这次点下的长点击是否有效
    private boolean moved=false;
    private LinkedBlockingDeque deque=new LinkedBlockingDeque();
    private ThreadPoolExecutor executor=new ThreadPoolExecutor(1,2,100, TimeUnit.MILLISECONDS,deque);
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        onTouch(event);
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            down(event);
            up=false;
            id++;
            longClicked=false;
            moved=false;
            movePreX=preX=event.getX();
            movePreY=preY=event.getY();
            //Log.i("down","down");
            int tmpId=id;
            executor.submit(() -> {//***模拟LongClick
                try {
                    Thread.sleep(600);
                    if(tmpId==id){
                        if(!up&&!longClicked&&!moved){
                            longClicked=true;
                            v.post(() -> longOnClick(preX,preY));
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            return false;
        }else if(event.getAction()==MotionEvent.ACTION_MOVE){
            if(!longClicked&&Math.abs(preX-event.getX())>Math.abs(preY-event.getY())*0.5){//左右滑动
                touchDirection(event.getX()-movePreX);
                moved=true;
            }
            float abs=Math.abs(preX-event.getX())+Math.abs(preY-event.getY());
            if(abs>20){
                moved=true;
            }
            if(longClicked&&abs>10){//**长点击有效，可以拖动图标
                moved=true;
                longClickMove(event.getX(),event.getY());
            }
            movePreX=event.getX();
            movePreY=event.getY();
            return true;
        }
        else if(event.getAction()==MotionEvent.ACTION_UP){
            up=true;
            if(Math.abs(preX-event.getX())+Math.abs(preY-event.getY())<20){
                if(!longClicked&&!moved) {
                    onClick(preX, preY);
                }
            }
            if(longClicked){//**长点击有效,点击接收时【显示选项】
                if(!moved)
                    longClickUpNoMove(event.getX(),event.getY());
                else
                    longClickUpMoved(event.getX(),event.getY());
            }
        }
        return false;

    }

    /**
     * 水平滑动
     * @param distance 滑动距离
     */
    public abstract void touchDirection(float distance);

    /**
     * 点击事件
     * @param x x
     * @param y y
     */
    public void onClick(float x,float y){};

    /**
     * 长点击
     * @param x x
     * @param y y
     */
    public abstract void longOnClick(float x,float y);
    /**
     * 长点击移动
     * @param x x
     * @param y y
     */
    public abstract void longClickMove(float x,float y);
    /**
     * 长点击结束
     * @param x x
     * @param y y
     */
    public abstract void longClickUpNoMove(float x,float y);
    public abstract void longClickUpMoved(float x,float y);
    public abstract void down(MotionEvent event);
    public void onTouch(MotionEvent event){}

}
