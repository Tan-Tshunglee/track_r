package com.antilost.app.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
            updateStateUi();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private PrefsManager mPrefsManager;
    private CheckBox mTrackAlert;
    private Switch mSleepMode;
    private ImageView trackImage;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BluetoothLeService.EXTRA_KEY_BLUETOOTH_ADDRESS);
            if(BluetoothLeService.ACTION_DEVICE_CLOSED.equals(action)) {
                finish();
                return;
            } else if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)
                    || BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if(mBluetoothDeviceAddress.equals(address)) {
                    updateDeclareLayoutVisibility();
                    updateStateUi();
                }
            }
        }
    };


    private IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_DEVICE_CLOSED);
    private ConnectivityManager mConnectivityManager;
    private CheckBox mPhoneAlert;
    private View mDeclaredLost;
    private TextView mDeclaredLostText;
    private volatile Thread mBackgroundThread;


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.v(LOG_TAG, String.format("TrackRSettingActivity receiver onReceive get action ") + action);
            if (BluetoothLeService.ACTION_BATTERY_LEVEL_READ.equals(action)) {
                int batteryLevel =mBluetoothLeService.getBatteryLevel(mBluetoothDeviceAddress);
            } else if (BluetoothLeService.ACTION_RSSI_READ.equals(action)) {
                int rssi = mBluetoothLeService.getRssiLevel(mBluetoothDeviceAddress);
                //Toast.makeText(TrackRActivity.this, "Get rssi value is " + rssi, Toast.LENGTH_SHORT).show();
            } else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                updateDeclareLayoutVisibility();
            }
            Log.v(LOG_TAG, "receive ACTION_GATT_CONNECTED");
        }
    };

    private void updateDeclareLayoutVisibility() {
        if(mPrefsManager.isMissedTrack(mBluetoothDeviceAddress)) {
            mDeclaredLost.setVisibility(View.VISIBLE);
            updateDeclareText();
        } else {
            mDeclaredLost.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);

        filter = new IntentFilter(BluetoothLeService.ACTION_DEVICE_CLOSED);
        filter.addAction(BluetoothLeService.ACTION_DEVICE_UNBIND);
        filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);

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


        trackImage = (ImageView) findViewById(R.id.icon);

        mDeclaredLost = findViewById(R.id.declared_lost);
        mDeclaredLostText = (TextView) findViewById(R.id.declared_lost_text);
        updateDeclareLayoutVisibility();

    }

    private void updateDeclareText() {
        if(mPrefsManager.isDeclaredLost(mBluetoothDeviceAddress)) {
            mDeclaredLostText.setText(getString(R.string.revoke_statement));
            mDeclaredLost.setBackground(getResources().getDrawable(R.drawable.red_bkg));
        } else {
            mDeclaredLostText.setText(getString(R.string.declare_lost));
            mDeclaredLost.setBackground(getResources().getDrawable(R.drawable.blue_bkg));
        }
    }


    private void updateStateUi() {
        String customIconUri = CsstSHImageData.getIconImageString(mBluetoothDeviceAddress);
        if(customIconUri != null) {
            trackImage.setImageBitmap(CsstSHImageData.toRoundCorner(customIconUri));
        } else {
            TrackR track = mPrefsManager.getTrack(mBluetoothDeviceAddress);
            trackImage.setImageResource(TrackREditActivity.DrawableIds[track.type]);
            trackImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    if(mBluetoothLeService == null) {
            trackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
            Log.v(LOG_TAG, "mBluetoothLeService == null");
        } else {
            if (mPrefsManager.isClosedTrack(mBluetoothDeviceAddress)) {
                trackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                Log.v(LOG_TAG, "isClosedTrack...");
            } else {
                if (mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    Log.v(LOG_TAG, "isGattConnected...");
                    trackImage.setBackgroundResource(R.drawable.connected_icon_bkg);
                } else {
                    Log.v(LOG_TAG, "disconnected...");
                    trackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                }
            }
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
        updateStateUi();
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    @Override
    public void onUserInteraction() {
        super.onUserInteraction();

    }

    @Override
    public void onClick(View view) {
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
                    Toast.makeText(this, getString(R.string.can_not_close_disconnected_itrack), Toast.LENGTH_SHORT).show();
                    return;
                }
                mBluetoothLeService.turnOffTrackR(mBluetoothDeviceAddress);
                break;
            case R.id.unbindTrackR:
                if(mBluetoothLeService == null) {
                    Log.w(LOG_TAG, "mBluetoothLeService is null");
                    return;
                }

                if(mBackgroundThread != null) {
                    return;
                }
                mBluetoothLeService.unbindTrackR(mBluetoothDeviceAddress);
                toast(getString(R.string.unbind_successfully));
                mBackgroundThread = new Thread() {
                    @Override
                    public void run() {
                        UnbindCommand command = new UnbindCommand(mPrefsManager.getUid(), mBluetoothDeviceAddress);
                        command.execTask();
                        mBackgroundThread = null;
                    }
                };
                mBackgroundThread.start();
                break;

            case R.id.declared_lost:

                if(mBackgroundThread != null) {
                    return;
                }
                mBackgroundThread = new Thread () {
                    @Override
                    public void run() {
                        int declareTobe = mPrefsManager.isDeclaredLost(mBluetoothDeviceAddress)  ? 0 : 1;
                        Command declareCommand = new LostDeclareCommand(mPrefsManager.getUid(), mBluetoothDeviceAddress, declareTobe);
                        try {
                            declareCommand.execTask();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(declareCommand.success()) {
                            mPrefsManager.saveDeclareLost(mBluetoothDeviceAddress, declareTobe == 0 ? false : true);
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
                        mBackgroundThread = null;
                    }
                };
                mBackgroundThread.start();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.itrack_alert_checkbox:
                mPrefsManager.saveTrackAlert(mBluetoothDeviceAddress, b);
                mBluetoothLeService.setTrackAlertMode(mBluetoothDeviceAddress, b);
            break;

            case R.id.phone_alert_checkbox:
                mPrefsManager.savePhoneAlert(mBluetoothDeviceAddress, b);
                break;

            case R.id.sleepModeSwitch:
                mPrefsManager.saveSleepMode(b);
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

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                ProgressDialog.Builder builder = new ProgressDialog.Builder(this);
                builder.setTitle(R.string.wait_a_moment);
                builder.setMessage(getString(R.string.delete_track_on_server));
                return builder.create();
        }
        return null;
    }
}
