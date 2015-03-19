package com.antilost.app.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.camera.Camera_MainActivity_zql;
import com.antilost.app.camera.Camera_MainActivity_zql;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.CsstSHImageData;
import com.antilost.app.util.LocUtils;

import java.util.HashMap;

public class TrackRActivity extends Activity implements View.OnClickListener {

    public static final String BLUETOOTH_ADDRESS_BUNDLE_KEY = "bluetooth_address_key";

    private static final String LOG_TAG = "TrackRActivity";
    private static final int TIMER_PERIOD_IN_MS = 20000;

    public static final int MSG_RESET_RING_STATE = 1;
    public static final int MSG_ENABLE_RING_BUTTON = 2;
    private static final int MSG_DELAY_INIT = 3;

    public static final int TIME_RINGING_STATE_KEEP = 10 * 1000;
    public static final int MAX_RSSI_LEVEL = -33;
    public static final int MIN_RSSI_LEVEL = -129;

    private String mBluetoothDeviceAddress;
    private ImageView mTrackImage;
    private TextView mSleepTime;
    private TextView mConnection;
    private ImageView mDistanceImage;
    private ImageView mBatteryLeve;
    private ImageView mTrackRIcon;

    //ring state of every trackr;
    private HashMap<String, Boolean> mRingStateMap = new HashMap<String, Boolean>();


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESET_RING_STATE:
                    mRingStateMap.put(mBluetoothDeviceAddress, false);
                    break;
                case MSG_ENABLE_RING_BUTTON:
                    break;
                case MSG_DELAY_INIT:
                    updateRssi();
                    updateBatteryLevel();
                    break;
            }
        }
    };
    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            updateRssi();
            updateBatteryLevel();
            updateStateUi();
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
                int batteryLevel =mBluetoothLeService.getBatteryLevel(mBluetoothDeviceAddress);
                updateBatteryIcon(batteryLevel);
            } else if (BluetoothLeService.ACTION_RSSI_READ.equals(action)) {
                int rssi = mBluetoothLeService.getRssiLevel(mBluetoothDeviceAddress);
                //Toast.makeText(TrackRActivity.this, "Get rssi value is " + rssi, Toast.LENGTH_SHORT).show();
                updateIconPosition(rssi);
            }
            Log.v(LOG_TAG, "receive ACTION_GATT_CONNECTED");
        }
    };
    private PrefsManager mPrefsManager;

    private void updateBatteryIcon(int level) {
        Log.i(LOG_TAG, "updateBatteryIcon " + level);
        int resId = R.drawable.battery_2;
        if(level < 25) {
            resId = R.drawable.battery_1;
        } else if(level < 50) {
            resId = R.drawable.battery_2;
        } else if(level < 75) {
            resId = R.drawable.battery_3;
        } else if(level <= 100) {
            resId = R.drawable.battery_4;
        }

        mBatteryLeve.setImageResource(resId);
    }


    private void updateIconPosition(int rssi) {
        if(rssi > MAX_RSSI_LEVEL || rssi < MIN_RSSI_LEVEL) {
            return;
        }

        int to_min = rssi - MIN_RSSI_LEVEL;
        float percentage = to_min / (float) (MAX_RSSI_LEVEL - MIN_RSSI_LEVEL);
        int top = mDistanceImage.getTop();
        int bottom = mDistanceImage.getBottom();

        int marginTop = (int) ((bottom - top) * percentage);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTrackRIcon.getLayoutParams();
        params.topMargin = marginTop;
        mTrackRIcon.setLayoutParams(params);

        Log.v(LOG_TAG, percentage + " margin top " + marginTop);

    }


    private void updateRssi() {
        if(mBluetoothLeService != null) {
            Log.d(LOG_TAG, "read rssi repeat.");
            updateIconPosition(mBluetoothLeService.getRssiLevel(mBluetoothDeviceAddress));
            mBluetoothLeService.startReadRssiRepeat(true, mBluetoothDeviceAddress);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);
        if(TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            finish();
            return;
        }

        mPrefsManager = PrefsManager.singleInstance(this);
        setContentView(R.layout.activity_track_r);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.btnSettings).setOnClickListener(this);
        findViewById(R.id.batteryStatus).setOnClickListener(this);
        findViewById(R.id.distanceLevel).setOnClickListener(this);

        findViewById(R.id.location).setOnClickListener(this);
        findViewById(R.id.ring).setOnClickListener(this);
        findViewById(R.id.photo).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);

//        findViewById(R.id.ring).setEnabled(false);

        mTrackImage = (ImageView) findViewById(R.id.track_r_photo);
        mSleepTime = (TextView) findViewById(R.id.sleepModeAndTime);
        mConnection = (TextView) findViewById(R.id.connectState);
        mDistanceImage = (ImageView) findViewById(R.id.distanceLevel);
        mTrackRIcon = (ImageView) findViewById(R.id.trackIcon);
        mBatteryLeve = (ImageView) findViewById(R.id.batteryStatus);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        Uri customIconUri = CsstSHImageData.getIconImageUri(mBluetoothDeviceAddress);

        if(customIconUri != null) {
            mTrackImage.setImageURI(customIconUri);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mBluetoothDeviceAddress = intent.getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);
        if(TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            finish();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothLeService != null) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeBroadcastReceiverIntentFilter());
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_INIT, 5000);

        updateStateUi();

    }

    private void updateStateUi() {
        if(mBluetoothLeService == null) {
            mTrackRIcon.setImageResource(R.drawable.track_r_icon_red);
        } else {
            if(mPrefsManager.isClosedTrack(mBluetoothDeviceAddress)) {
                mTrackRIcon.setImageResource(R.drawable.track_r_icon_red);
                mConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_dot, 0, 0, 0);
                mConnection.setText(R.string.closed);
            } else {
                if(mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    mTrackRIcon.setImageResource(R.drawable.track_r_icon_green);
                    mConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_dot, 0, 0, 0);
                    mConnection.setText(R.string.connected);
                } else {
                    mTrackRIcon.setImageResource(R.drawable.track_r_icon_red);
                    mConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_dot, 0, 0, 0);
                    mConnection.setText(R.string.disconnected);
                }
            }
        }
    }

    private IntentFilter makeBroadcastReceiverIntentFilter() {
        IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_RSSI_READ);
        filter.addAction(BluetoothLeService.ACTION_BATTERY_LEVEL_READ);
        return filter;
    }

    private void updateBatteryLevel() {
        if(mBluetoothLeService != null) {
            Log.v(LOG_TAG, "updateBattery....");
            mBluetoothLeService.readBatteryLevel(mBluetoothDeviceAddress);
            int batteryLevel =mBluetoothLeService.getBatteryLevel(mBluetoothDeviceAddress);
            updateBatteryIcon(batteryLevel);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mGattUpdateReceiver);
        updateRssi();
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
                Boolean ringing = mRingStateMap.get(mBluetoothDeviceAddress);

                if(ringing == null) {
                    ringing = false;
                }
                if(ringing) {
                    Log.d(LOG_TAG, "trackr is ringing, ringing silent it");
                    silentRing();
                    mRingStateMap.put(mBluetoothDeviceAddress, false);
                } else {
                    Log.d(LOG_TAG, "make trackr ring.");
                    makeTrackRRing();
                    mRingStateMap.put(mBluetoothDeviceAddress, true);
                    mHandler.sendEmptyMessageDelayed(MSG_RESET_RING_STATE, TIME_RINGING_STATE_KEEP);
                }
                break;

            case R.id.location:

                if(mBluetoothLeService != null && mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    Location location = mBluetoothLeService.getLastLocation();
                    if(location != null) {
                        LocUtils.viewLocation(this, location);
                    } else {
                        Log.w(LOG_TAG, "no location found.");
                    }
                } else {
                    Location location = mPrefsManager.getLastLostLocation(mBluetoothDeviceAddress);

                    if(location == null) {
                        location = mBluetoothLeService.getLastLocation();
                        if(location != null) {
                            LocUtils.viewLocation(this, location);
                        }
                    } else {
                        LocUtils.viewLocation(this, location);
                    }
                }


                break;

            case R.id.share:

                break;

            case R.id.photo:
                startActivity(new Intent(this, Camera_MainActivity_zql.class));
                break;

            case R.id.batteryStatus:

                mBluetoothLeService.readBatteryLevel(mBluetoothDeviceAddress);
                break;
            case R.id.distanceLevel:
                updateRssi();
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

    private boolean isGattConnected() {
        if(mBluetoothLeService != null) {
            return mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress);
        }
        return false;
    }
}
