package com.antilost.app.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.antilost.app.R;
import com.antilost.app.service.BluetoothLeService;

public class CameraActivity extends FragmentActivity {


    private BroadcastReceiver mReceiver = new  BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(BluetoothLeService.ACTION_DEVICE_CLICKED.equals(intent.getAction())) {
                tryTakePicture();
            };
        }
    };


    private void tryTakePicture() {
        
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(BluetoothLeService.ACTION_DEVICE_CLICKED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

}
