package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.antilost.app.R;
import com.antilost.app.adapter.TrackRListAdapter;
import com.antilost.app.model.TrackR;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;

import java.util.Locale;
import java.util.Set;

public class MainTrackRListActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int REQUEST_CODE_ADD_TRACK_R = 1;
    private static final String LOG_TAG = "MainTrackRListActivity";


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
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                mListViewAdapter.updateData();
                Log.v(LOG_TAG, "receive ACTION_DATA_AVAILABLE");
            }
        }
    };
    private AlertDialog mDisconnectedDialog;
    private BluetoothAdapter mBluetoothAdapter;

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        mListViewAdapter.updateData();

        mListView.setOnItemClickListener(this);
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

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mListViewAdapter.updateData();
        if(!mBluetoothAdapter.isEnabled()) {

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
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
        }
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
        Intent i = new Intent(this, StartBindActivity.class);
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

    private void editBluetoothDevice(String deviceAddress) {
        Intent i = new Intent(this, TrackREditActivity.class);
        i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, deviceAddress);
        startActivity(i);
    }

    public BluetoothLeService getBluetoothLeService() {
        return mBluetoothLeService;
    }

    public boolean isBluetoothConnected(String address) {
        if(mBluetoothLeService == null) {
            return false;
        }

        if(mBluetoothLeService.isGattConnected(address)) {
            return true;
        }
        return false;
    }
}
