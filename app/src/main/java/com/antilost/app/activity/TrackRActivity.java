package com.antilost.app.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
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
    private boolean mRingBtnSend = false;
    private Handler mHandler = new Handler();
    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_BATTERY_LEVEL_READ.equals(action)) {
                int level = intent.getIntExtra(BluetoothLeService.EXTRA_DATA, -1);
            } else if (BluetoothLeService.ACTION_RSSI_READ.equals(action)) {
                int rssi = mBluetoothLeService.getRssiLevel(mBluetoothDeviceAddress);
            }
            Log.v(LOG_TAG, "receive ACTION_GATT_CONNECTED");
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
        findViewById(R.id.batteryStatus).setOnClickListener(this);
        findViewById(R.id.distanceLevel).setOnClickListener(this);

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
    protected void onResume() {
        super.onResume();
        if(mBluetoothLeService != null) {
            if(!mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                finish();
            };
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.btnSettings:
                Intent i = new Intent(this, TrackRSettingActivity.class);
                i.putExtra(TrackRSettingActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothDeviceAddress);
                startActivity(i);
                break;

            case R.id.ring:
                if(!mRingBtnSend) {
                    makeTrackRRing();
                    mRingBtnSend = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRingBtnSend = false;
                        }
                    }, 10 * 1000);
                } else {
                    silentRing();
                }
                break;

            case R.id.location:

                break;

            case R.id.share:

                break;

            case R.id.batteryStatus:

                mBluetoothLeService.readBatteryLevel(mBluetoothDeviceAddress);
                break;
            case R.id.distanceLevel:

                mBluetoothLeService.requestRssiLevel(mBluetoothDeviceAddress);
                break;
        }
    }

    private void silentRing() {
        if(mBluetoothLeService != null) {
            mBluetoothLeService.silentRing(mBluetoothDeviceAddress);
        }
    }

    private void makeTrackRRing() {
        if(mBluetoothLeService != null) {
            mBluetoothLeService.ringTrackR(mBluetoothDeviceAddress);
        }
    }
}
