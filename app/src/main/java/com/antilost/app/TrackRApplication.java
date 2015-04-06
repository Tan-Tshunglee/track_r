package com.antilost.app;

import android.app.Activity;
import android.app.Application;

import com.android.camera.Util;
import com.antilost.app.service.BluetoothLeService;

public class TrackRApplication extends Application {
    private BluetoothLeService mBluetoothLeService;

    @Override
    public void onCreate() {
        super.onCreate();
        Util.initialize(this);
    }

    public void setBluetootLeService(BluetoothLeService service) {
        mBluetoothLeService = service;
    }


    public static void onUserInteraction(Activity activity) {
        TrackRApplication app = (TrackRApplication) activity.getApplication();
        if(app.mBluetoothLeService != null) {
            app.mBluetoothLeService.onUserInteraction();
        }
    }
}
