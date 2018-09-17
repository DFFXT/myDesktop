package com.example.interface_;

/**
 * 桌面接口
 * Created by home on 2018/3/10.
 */

public interface DesktopInterface {
    void addApp(String pkgName, int page);
    void removeApp(String pkgName,int page);
    void reDrawPage(int page);
    void reboot();
}
