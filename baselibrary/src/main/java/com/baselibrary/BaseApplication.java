package com.baselibrary;

import android.app.Application;

public class BaseApplication extends Application {

    public static BaseApplication context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
