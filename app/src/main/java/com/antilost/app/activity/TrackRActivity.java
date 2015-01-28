package com.antilost.app.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.service.BluetoothLeService;

public class TrackRActivity extends Activity implements View.OnClickListener {

    public static final String BLUETOOTH_ADDRESS_BUNDLE_KEY = "bluetooth_address_key";
    private static final String LOG_TAG = "TrackRActivity";
    private String mBluetoothDeviceAddress;
    private ImageView mTrackImage;
    private TextView mSleepTime;
    private TextView mConnection;
    private ImageView mDistanceImage;
    private ImageView mBatteryLeve;

    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(LOG_TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.initialize();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);
        if(TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_track_r);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.btnSettings).setOnClickListener(this);
//        findViewById(R.id.batteryStatus).setOnClickListener(this);

        findViewById(R.id.location).setOnClickListener(this);
        findViewById(R.id.ring).setOnClickListener(this);
        findViewById(R.id.photo).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);


        mTrackImage = (ImageView) findViewById(R.id.track_r_photo);
        mSleepTime = (TextView) findViewById(R.id.sleepModeAndTime);
        mConnection = (TextView) findViewById(R.id.connectState);
        mDistanceImage = (ImageView) findViewById(R.id.distanceLevel);
        mBatteryLeve = (ImageView) findViewById(R.id.batteryStatus);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.btnSettings:
                break;

            case R.id.ring:
                makeTrackRRing();
                break;
        }
    }

    private void makeTrackRRing() {
        if(mBluetoothLeService != null) {
            mBluetoothLeService.ringTrackR(mBluetoothDeviceAddress);
        }
    }
}
