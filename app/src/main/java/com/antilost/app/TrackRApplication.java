package com.antilost.app;

import android.app.Application;

import com.android.camera.Util;

public class TrackRApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Util.initialize(this);
    }
    
}
