package com.seiko.singlevideoplayer;

import android.app.Application;

import com.seiko.singlevideoplayer.utils.Utils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
