package com.example.dataType;

import java.io.Serializable;

/**
 * Created by home on 2018/3/12.
 */

public class AppWidget  implements Serializable{
    //**id
    private int id;
    //**页面
    private int page;
    //**位置
    private int x,y;
    public AppWidget(int id,int page,int x,int y){
        this.id=id;
        this.page=page;
        this.x=x;
        this.y=y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getPage() {
        return page;
    }

    public int getId() {
        return id;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
