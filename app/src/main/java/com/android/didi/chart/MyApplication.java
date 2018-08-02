package com.android.didi.chart;

import android.app.Application;

import com.xuhao.android.libsocket.sdk.OkSocket;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OkSocket.initialize(this,true);
    }
}
