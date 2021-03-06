package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.TrackRApplication;
import com.antilost.app.adapter.TrackRListAdapter;
import com.antilost.app.model.TrackR;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;

import java.util.Locale;
import java.util.Set;

public class MainTrackRListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, Dialog.OnClickListener {

    private static final int REQUEST_CODE_ADD_TRACK_R = 1;
    private static final String LOG_TAG = "MainTrackRListActivity";
    public static final int PROMPT_OPEN_LOCATION_SERVICE_DIAOLOG_ID = 1;
    public static final int BLUETOOTH_DISABLED_DIALOG = 2;


    private TrackRListAdapter mListViewAdapter;
    private ListView mListView;
    private PrefsManager mPrefsManager;


    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mListViewAdapter.updateData();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mListViewAdapter.updateData();
                Log.v(LOG_TAG, "receive ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mListViewAdapter.updateData();
                Log.v(LOG_TAG, "receive ACTION_GATT_DISCONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mListViewAdapter.updateData();
                Log.v(LOG_TAG, "receive ACTION_GATT_SERVICES_DISCOVERED");
            } else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                if(state == BluetoothAdapter.STATE_ON) {
                    try {
                        dismissDialog(BLUETOOTH_DISABLED_DIALOG);
                    } catch (Exception e) {
                    }
                }
            }
        }
    };
    private AlertDialog mDisconnectedDialog;
    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager mLocationManager;
    private RelativeLayout mNoLocationProviderAlertLayout;
    private AlertDialog mLocationUnavailableDialog;
    private AlertDialog mBluetoothDisableDialog;

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DEVICE_CLOSED);
        intentFilter.addAction(BluetoothLeService.ACTION_DEVICE_UNBIND);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "MainTrackRListActivity start.");
        mPrefsManager = PrefsManager.singleInstance(this);
        setContentView(R.layout.activity_main_track_rlist);
        mListView = (ListView) findViewById(R.id.listview);
        mListViewAdapter = new TrackRListAdapter(this, mPrefsManager);
        mListView.setAdapter(mListViewAdapter);
        findViewById(R.id.btnUserProfile).setOnClickListener(this);
        findViewById(R.id.btnLocation).setOnClickListener(this);
        findViewById(R.id.btnAdd).setOnClickListener(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<String> trackIds = mPrefsManager.getTrackIds();
        if(trackIds == null || trackIds.isEmpty()) {
            addNewTrackR();
        }

        mNoLocationProviderAlertLayout = (RelativeLayout) findViewById(R.id.noLocationProviderAlertLayout);
        mNoLocationProviderAlertLayout.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.v(LOG_TAG, "onWindowsFocusChanged is " + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            try {
                dismissDialog(BLUETOOTH_DISABLED_DIALOG);
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mListViewAdapter.updateData();
        updateLocationAlertVisibility();
        checkBluetoothAvailability();

        if(!mPrefsManager.validUserLog()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        startService(new Intent(this, BluetoothLeService.class));
    }

    private void checkBluetoothAvailability() {
        try {
            if(!mBluetoothAdapter.isEnabled()) {
                showDialog(BLUETOOTH_DISABLED_DIALOG);
            } else{
                dismissDialog(BLUETOOTH_DISABLED_DIALOG);
            }
        } catch (Exception e) {
        }
    }

    private void updateLocationAlertVisibility() {
        int visibility = checkLocationProviverAvailable() ? View.GONE: View.VISIBLE;
        mNoLocationProviderAlertLayout.setVisibility(visibility);
    }

    private boolean checkLocationProviverAvailable() {
        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))  {
            return true;
        }

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROMPT_OPEN_LOCATION_SERVICE_DIAOLOG_ID:
                return createPromptOpenLocationServiceDialog();
            case BLUETOOTH_DISABLED_DIALOG:
                return createBluetoothDisabledDialog();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
    }


    private  Dialog  createBluetoothDisabledDialog() {

        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(R.layout.tiplayout);
        TextView title = (TextView) window.findViewById(R.id.title_tip);
        TextView text = (TextView) window.findViewById(R.id.text_tip);
        ImageView icontrack = (ImageView) window.findViewById(R.id.tipicon);
        icontrack.setVisibility(View.GONE);
        text.setText(getResources().getString(R.string.bluethoot_disabled));
        title.setText(getResources().getString(R.string.notice));
        Button ok = (Button) window.findViewById(R.id.tipbtn_ok);

        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.cancel();
                Intent bluetoothSettingIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                bluetoothSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(bluetoothSettingIntent);
                } catch (Exception e) {
                }


            }

        });
        // 关闭alert对话框架
        Button cancel = (Button) window.findViewById(R.id.tipbtn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.cancel();
            }
        });
        return dlg;
    }


    private Dialog createBluetoothDisabledDialog1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.bluethoot_disabled));
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
        mBluetoothDisableDialog = builder.create();
        return mBluetoothDisableDialog;

    }

    private Dialog createPromptOpenLocationServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.warm_prompt_open_location_service));
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
        mLocationUnavailableDialog = builder.create();
        return mLocationUnavailableDialog;
    }


    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        TrackRApplication.onUserInteraction(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                addNewTrackR();
                break;
            case R.id.btnUserProfile:
                showUserProfile();
                break;
            case R.id.btnLocation:
                showLocations();
                break;
            case R.id.noLocationProviderAlertLayout:
                openLocationSettings();
                break;
        }
    }

    private void openLocationSettings() {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private void showLocations() {
        Intent i = new Intent(this, ManualAddLocationActivity.class);
        startActivity(i);
    }

    private void showUserProfile() {
        Intent i = new Intent(this, UserProfileActivity.class);
        startActivity(i);
    }

    private void addNewTrackR() {
        Intent i = new Intent(this, PrepareScanActivity.class);
        startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String address = (String) mListViewAdapter.getItem(position);

//        int state = mBluetoothLeService.getGattConnectState(address);
//        if(state == BluetoothProfile.STATE_DISCONNECTED) {
//            showDisconnectedTrack(address);
//        } else if(state == BluetoothProfile.STATE_CONNECTED){
//            editBluetoothDevice(address);
//        }
        Intent i = new Intent(this, TrackRActivity.class);
        i.putExtra(TrackRActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, address);
        startActivity(i);


    }

    private void showDisconnectedTrack(String address) {
        TrackR track = mPrefsManager.getTrack(address);
        String name = track.name;
        if(TextUtils.isEmpty(name)) {
            name = getResources().getStringArray(R.array.default_type_names)[track.type];
        }
        ensureDialog(name, address);
        mDisconnectedDialog.show();

    }

    private void ensureDialog(String name, final String address) {
        if(mDisconnectedDialog != null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disconnected  " + name);
        builder.setMessage("Reconnect or Find This TrackR?");
        builder.setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addNewTrackR();
            }
        });

        builder.setNegativeButton("Find", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Location loc = mPrefsManager.getTrackLocMissed(address);
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", loc.getLatitude(), loc.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });
        mDisconnectedDialog = builder.create();
    }


    public BluetoothLeService getBluetoothLeService() {
        return mBluetoothLeService;
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(dialogInterface == mBluetoothDisableDialog) {
            switch (i) {
                case DialogInterface.BUTTON_POSITIVE:
                    Intent bluetoothSettingIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    bluetoothSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(bluetoothSettingIntent);
                        dismissDialog(BLUETOOTH_DISABLED_DIALOG);
                    } catch (Exception e) {
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //do nothing;
                    break;
            }
        } else if(dialogInterface == mLocationUnavailableDialog) {
            switch (i) {
                case DialogInterface.BUTTON_POSITIVE:
                    Intent openGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    openGpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(openGpsIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //do nothing
                    break;
            }
        }
    }
}
