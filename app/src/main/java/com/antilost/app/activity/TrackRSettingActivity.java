package com.antilost.app.activity;

import android.app.Activity;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.model.TrackR;
import com.antilost.app.network.Command;
import com.antilost.app.network.LostDeclareCommand;
import com.antilost.app.network.UnbindCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.CsstSHImageData;

public class TrackRSettingActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String BLUETOOTH_ADDRESS_BUNDLE_KEY = "bluetooth_address_key";
    private static final String LOG_TAG = "TrackRSettingActivity";
    private String mBluetoothDeviceAddress;

    private BluetoothLeService mBluetoothLeService;
    //Code to manage Service lifecycle.
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
    private CheckBox mTrackAlert;
    private Switch mSleepMode;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_DEVICE_CLOSED);
    private ConnectivityManager mConnectivityManager;
    private CheckBox mPhoneAlert;
    private View mDeclaredLost;
    private TextView mDeclaredLostText;


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
        findViewById(R.id.declared_lost).setOnClickListener(this);
        findViewById(R.id.icon).setOnClickListener(this);

        mPrefsManager = PrefsManager.singleInstance(this);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.v(LOG_TAG, "bindService...");

        mTrackAlert = (CheckBox) findViewById(R.id.itrack_alert_checkbox);
        mPhoneAlert = (CheckBox) findViewById(R.id.phone_alert_checkbox);

        mTrackAlert.setChecked(mPrefsManager.getTrackAlert(mBluetoothDeviceAddress));
        mTrackAlert.setOnCheckedChangeListener(this);

        mPhoneAlert.setChecked(mPrefsManager.getPhoneAlert(mBluetoothDeviceAddress));
        mPhoneAlert.setOnCheckedChangeListener(this);

        boolean sleepMode = mPrefsManager.getSleepMode();

        mSleepMode = (Switch) findViewById(R.id.sleepModeSwitch);
        mSleepMode.setChecked(sleepMode);
        mSleepMode.setOnCheckedChangeListener(this);

        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        String customIconUri = CsstSHImageData.getIconImageString(mBluetoothDeviceAddress);
        ImageView trackImage = (ImageView) findViewById(R.id.icon);




        if(customIconUri != null) {

//            trackImage.setImageURI(customIconUri);

            trackImage.setImageBitmap(CsstSHImageData.toRoundCorner(customIconUri));


        } else {
            TrackR track = mPrefsManager.getTrack(mBluetoothDeviceAddress);
            trackImage.setImageResource(TrackREditActivity.DrawableIds[track.type]);
            trackImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
        if(mBluetoothLeService == null) {
            trackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
        } else {
            if (mPrefsManager.isClosedTrack(mBluetoothDeviceAddress)) {
                trackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
            } else {
                if (mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    trackImage.setBackgroundResource(R.drawable.connected_icon_bkg);
                } else {
                    trackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                }
            }
        }




        mDeclaredLost = findViewById(R.id.declared_lost);
        mDeclaredLostText = (TextView) findViewById(R.id.declared_lost_text);
        if(mPrefsManager.isMissedTrack(mBluetoothDeviceAddress)) {
            mDeclaredLost.setVisibility(View.VISIBLE);
            updateDeclareText();
        } else {
            mDeclaredLost.setVisibility(View.GONE);
        }

    }

    private void updateDeclareText() {
        if(mPrefsManager.getDeclareLost(mBluetoothDeviceAddress)) {
            mDeclaredLostText.setText(getString(R.string.revoke_statement));
            mDeclaredLost.setBackground(getResources().getDrawable(R.drawable.red_bkg));
        } else {
            mDeclaredLostText.setText(getString(R.string.declare_lost));
            mDeclaredLost.setBackground(getResources().getDrawable(R.drawable.blue_bkg));
        }
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
        Thread t;
        switch (view.getId()) {
            case R.id.backBtn:
                Intent i = new Intent(this, TrackRActivity.class);
                i.putExtra(TrackRSettingActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothDeviceAddress);
                startActivity(i);
                TrackRSettingActivity.this.finish();
                break;
            case R.id.icon:
                i = new Intent(TrackRSettingActivity.this, TrackREditActivity.class);
                i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothDeviceAddress);
                startActivity(i);
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
                t = new Thread() {
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

            case R.id.declared_lost:

                t = new Thread () {
                    @Override
                    public void run() {
                        int declareTobe = mPrefsManager.getDeclareLost(mBluetoothDeviceAddress)  ? 0 : 1;
                        Command declareCommand = new LostDeclareCommand(mPrefsManager.getUid(), mBluetoothDeviceAddress, declareTobe);
                        try {
                            declareCommand.execTask();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(declareCommand.success()) {
                            mPrefsManager.setDeclareLost(mBluetoothDeviceAddress, declareTobe == 0 ? false : true);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TrackRSettingActivity.this, getString(R.string.declare_success), Toast.LENGTH_SHORT).show();
                                    updateDeclareText();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TrackRSettingActivity.this, getString(R.string.declaration_failed), Toast.LENGTH_SHORT).show();
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


    public boolean onKeyDown(int keyCode,KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出 退出确认框

            Intent i = new Intent(this, TrackRActivity.class);
            i.putExtra(TrackRSettingActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothDeviceAddress);
            startActivity(i);
            TrackRSettingActivity.this.finish();
            return true;
        }
        return true;
    }
}
