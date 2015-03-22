package com.antilost.app.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.network.UnbindCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;

public class TrackRSettingActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String BLUETOOTH_ADDRESS_BUNDLE_KEY = "bluetooth_address_key";
    private static final String LOG_TAG = "TrackRSettingActivity";
    private String mBluetoothDeviceAddress;

    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.v(LOG_TAG, "onServiceConnected...");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private PrefsManager mPrefsManager;
    private Switch mTrackAlert;
    private Switch mSleepMode;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_DEVICE_CLOSED);
    private ConnectivityManager mConnectivityManager;
    private Switch mPhoneAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);

        if(TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            Log.w(LOG_TAG, "get empty bluetooth address.");
            finish();
            return;
        }
        setContentView(R.layout.activity_track_rsetting);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.turnOffTrackR).setOnClickListener(this);
        findViewById(R.id.unbindTrackR).setOnClickListener(this);

        mPrefsManager = PrefsManager.singleInstance(this);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.v(LOG_TAG, "bindService...");

        mTrackAlert = (Switch) findViewById(R.id.itrack_alert_checkbox);
        mPhoneAlert = (Switch) findViewById(R.id.phone_alert_checkbox);

        mTrackAlert.setChecked(mPrefsManager.getTrackAlert(mBluetoothDeviceAddress));
        mTrackAlert.setOnCheckedChangeListener(this);

        mPhoneAlert.setChecked(mPrefsManager.getPhoneAlert(mBluetoothDeviceAddress));
        mPhoneAlert.setOnCheckedChangeListener(this);

        boolean sleepMode = mPrefsManager.getSleepMode();

        mSleepMode = (Switch) findViewById(R.id.sleepModeSwitch);
        mSleepMode.setChecked(sleepMode);
        mSleepMode.setOnCheckedChangeListener(this);

        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.turnOffTrackR:
                if(mBluetoothLeService == null) {
                    Log.w(LOG_TAG, "mBluetoothLeService is null");
                    return;
                }

                mBluetoothLeService.turnOffTrackR(mBluetoothDeviceAddress);
                break;
            case R.id.unbindTrackR:
                if(mBluetoothLeService == null) {
                    Log.w(LOG_TAG, "mBluetoothLeService is null");
                    return;
                }
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        UnbindCommand command = new UnbindCommand(mPrefsManager.getUid(), mBluetoothDeviceAddress);
                        command.execTask();

                        if(command.success()) {
                            mBluetoothLeService.unbindTrackR(mBluetoothDeviceAddress);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast(getString(R.string.unbind_successfully));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast(getString(R.string.unbind_track_server_error));
                                }
                            });
                        }
                    }
                };
                t.start();


                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.itrack_alert_checkbox:
                mPrefsManager.setTracklAlert(mBluetoothDeviceAddress, b);
                mBluetoothLeService.setTrackAlertMode(mBluetoothDeviceAddress, b);
            break;

            case R.id.sleepModeSwitch:
                mPrefsManager.setSleepMode(b);
            break;
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
