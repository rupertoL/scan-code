package cn.lp.scanmode;

import android.app.Application;

import cn.shequren.scancode.ScanCodeMangerUtils;

public class MyApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        ScanCodeMangerUtils.newInstance().init(this);

    }


}