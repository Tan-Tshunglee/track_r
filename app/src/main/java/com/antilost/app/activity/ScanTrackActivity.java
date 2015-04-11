package com.antilost.app.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.antilost.app.BuildConfig;
import com.antilost.app.R;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.Utils;
import com.antilost.app.view.DotsMarquee;

import java.util.Set;

public class ScanTrackActivity extends Activity implements View.OnClickListener {

    public static final int MSG_SHOW_CONNECTING_PAGE = 1;
    public static final int MSG_SHOW_SEARCH_FAILED_PAGE = 2;
    public static final int MSG_SHOW_FIRST_PAGE = 3;

    public static final int MAX_COUNT = 5;

    public static final int MAX_RETRY_TIMES = 5;//zql  reduce to  25S
    public static final int MSG_RETRY_SCAN_LE = 100;

    private static final String LOG_TAG = "ScanTrackActivity";

    //Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;


    private RelativeLayout mFirstPage;
    private RelativeLayout mConnectingPage;
    private RelativeLayout mFailedPage;
    private ImageButton mBackBtn;
    private Button mTryAgain;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean mScanning;
    private PrefsManager mPrefsManager;
    private Set<String> mTrackIds;
    private int mScanedTime = 0;

    private Handler mHandler;
    public static BluetoothGatt sBluetoothConnected;
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String deviceName = device.getName();
                            //check device name first
                            if (!Utils.DEVICE_NAME.equals(deviceName)) {
                                if (BuildConfig.DEBUG && "Keyfobdemo".equals(deviceName)) {
                                    Log.i(LOG_TAG, "debug mode allow  device of name " + deviceName);
                                } else {
                                    Log.w(LOG_TAG, "get unkown device of name " + deviceName);
                                    return;
                                }

                            }
                            scanLeDevice(false);
                            mTrackIds = mPrefsManager.getTrackIds();
                            String deviceAddress = device.getAddress();
                            if (mTrackIds.contains(deviceAddress)
                                    && !mPrefsManager.isMissedTrack(deviceAddress)
                                    && !mPrefsManager.isClosedTrack(deviceAddress)) {
                                //mPrefsManager.addTrackId(deviceAddress);
                                Log.v(LOG_TAG, "find already bind device");
                                return;
                            }

                            if (!mTrackIds.contains(deviceAddress)) {
                                Log.v(LOG_TAG, "find a new track device.");
                                tryConnectBluetoothGatt(deviceAddress);
                            } else {
                                //find a diconnected or closed track;
                                Log.v(LOG_TAG, "found bluetooth device address + " + deviceAddress);
//                            startService(new Intent(ScanTrackActivity.this, MonitorService.class));
                                if (mPrefsManager.isClosedTrack(deviceAddress)) {
                                    Log.v(LOG_TAG, "reconnect to closed trackr.");
                                    reconnectedClosedTrackR(deviceAddress);
                                    return;
                                }
                                if (mPrefsManager.isMissedTrack(deviceAddress)) {
                                    Log.v(LOG_TAG, "found disconnected trackr.");
                                    reconnectMissingTrack(deviceAddress);
                                }
                            }
                        }
                    });
                }
            };

    private BluetoothGatt mConnectedBluetoothGatt;
    private final BluetoothGattCallback  mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            Log.v(LOG_TAG, "mBluetoothGattCallback.onConnectionStateChange get state " + newState);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if(newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.v(LOG_TAG, "bluetooth connection state is STATE_CONNECTED");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(gatt.discoverServices()) {
                                Log.i(LOG_TAG, "send gatt.discoverServices command success.");
                            } else {
                                Log.e(LOG_TAG, "send gatt.discoverServices command failed..");
                            }
                        }
                    }, 1000);
                } else {
                    Log.v(LOG_TAG, "bluetooth connection state is not STATE_CONNECTED");
                }
            } else {
                Log.e(LOG_TAG, "Scan Track Activity onConnectionStateChange get status is not :" + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(LOG_TAG, "onServiceDiscovered... " + status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(com.antilost.app.bluetooth.UUID.CUSTOM_VERIFIED_SERVICE);
                if(service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_CUSTOM_VERIFIED);
                    if(characteristic != null) {
                        characteristic.setValue(new byte[]{(byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5});
                        if(gatt.writeCharacteristic(characteristic)) {
                            log("Write verify code success.");
                            mConnectedBluetoothGatt = gatt;
                            startTrackEdit(gatt);
                        } else {
                            log("Write verify code failed.");
                        }
                    } else {
                        Log.e(LOG_TAG, "gatt has no custom verified characteristic in verifyConnection");
                    }
                } else {
                    Log.e(LOG_TAG, "gatt has no custom verified service in verifyConnection");
                }
            }
        }
    };

    private void log(String s) {
        Log.d(LOG_TAG, s);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                finish();
            }
            Log.v(LOG_TAG, "receive ACTION_GATT_CONNECTED");
        }
    };
    private DotsMarquee mBindingDotsMarquee;
    private DotsMarquee mConnecingDotsMarquee;


    private void reconnectedClosedTrackR(String address) {
        Toast.makeText(this, "Reconnect Closed TrackR Found.", Toast.LENGTH_SHORT).show();
        mPrefsManager.saveClosedTrack(address, false);
        startService(new Intent(ScanTrackActivity.this, BluetoothLeService.class));
        finish();
    }

    private void reconnectMissingTrack(String address) {
        mPrefsManager.saveMissedTrack(address, false);
        startService(new Intent(ScanTrackActivity.this, BluetoothLeService.class));
        finish();
    }


    private IntentFilter makeBroadcastReceiverIntentFilter() {
        IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_GATT_CONNECTED);
        return filter;
    }

    private void tryConnectBluetoothGatt(final String deviceAddress) {
        if(mConnectedBluetoothGatt != null && mConnectedBluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
            Log.d(LOG_TAG, "Try connect to a connected gatt.");
            return;
        }
        mHandler.sendEmptyMessage(MSG_SHOW_CONNECTING_PAGE);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                device.connectGatt(ScanTrackActivity.this, false, mBluetoothGattCallback);
            }
        });
    }

    private void startTrackEdit(BluetoothGatt gatt) {

        if(mConnectedBluetoothGatt != null) {
            sBluetoothConnected = gatt;
            Intent i = new Intent(ScanTrackActivity.this, TrackREditActivity.class);
            i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, gatt.getDevice().getAddress());
            i.putExtra(TrackREditActivity.EXTRA_EDIT_NEW_TRACK, true);
            startActivity(i);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        mBluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.your_device_no_ble_support), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mPrefsManager = PrefsManager.singleInstance(this);

        if (mPrefsManager.getTrackIds().size() == MAX_COUNT) {
            Toast.makeText(this, getString(R.string.max_device_limit_reached), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_binding);
        mFirstPage = (RelativeLayout) findViewById(R.id.firstPage);
        mConnectingPage = (RelativeLayout) findViewById(R.id.connectingPage);
        mFailedPage = (RelativeLayout) findViewById(R.id.failedPage);
        mFirstPage.setVisibility(View.VISIBLE);
        mConnectingPage.setVisibility(View.GONE);
        mFailedPage.setVisibility(View.GONE);
        mBackBtn = (ImageButton) findViewById(R.id.backBtn);
        mBackBtn.setOnClickListener(this);

        mTryAgain = (Button) findViewById(R.id.tryAgain);
        mTryAgain.setOnClickListener(this);

        mBindingDotsMarquee = (DotsMarquee) findViewById(R.id.bindingDotsMarquee);
        mConnecingDotsMarquee = (DotsMarquee) findViewById(R.id.connectingDotsMarquee);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.v(LOG_TAG, "handling Msg " + msg.toString());
                switch (msg.what) {
                    case MSG_SHOW_CONNECTING_PAGE:
                        mFirstPage.setVisibility(View.GONE);
                        mConnectingPage.setVisibility(View.VISIBLE);
                        mFailedPage.setVisibility(View.GONE);
                        break;
                    case MSG_SHOW_SEARCH_FAILED_PAGE:
                        mFirstPage.setVisibility(View.GONE);
                        mConnectingPage.setVisibility(View.GONE);
                        mFailedPage.setVisibility(View.VISIBLE);
                        break;
                    case MSG_SHOW_FIRST_PAGE:
                        mFirstPage.setVisibility(View.VISIBLE);
                        mConnectingPage.setVisibility(View.GONE);
                        mFailedPage.setVisibility(View.GONE);
                        break;
                    case MSG_RETRY_SCAN_LE:
                        mScanedTime += SCAN_PERIOD;
                        scanLeDevice(false);
                        if (mScanedTime < MAX_RETRY_TIMES * SCAN_PERIOD) {
                            Log.v(LOG_TAG, "Scan last time " + mScanedTime / 1000);
                            scanLeDevice(true);
                            return;
                        }
                        mScanedTime = 0;
                        sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
                        break;
                }
            }
        };
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        gattServiceIntent.setAction(BluetoothLeService.ACTION_STOP_BACKGROUND_LOOP);
        startService(gattServiceIntent);
        scanLeDevice(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        unregisterReceiver(mGattUpdateReceiver);
        mBindingDotsMarquee.stopMarquee();
        mConnecingDotsMarquee.stopMarquee();
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
        }

        registerReceiver(mGattUpdateReceiver, makeBroadcastReceiverIntentFilter());
        mBindingDotsMarquee.startMarquee();
        mConnecingDotsMarquee.startMarquee();

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            Log.w(LOG_TAG, "scanLeDevice " + enable);
            mScanning = true;
            if(mBluetoothAdapter.startLeScan(mLeScanCallback)) {
                Log.i(LOG_TAG, "startLeScan success");
            } else {
                Log.i(LOG_TAG, "startLeScan failed");
            }

            mHandler.sendEmptyMessageDelayed(MSG_RETRY_SCAN_LE, SCAN_PERIOD);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mHandler.removeMessages(MSG_RETRY_SCAN_LE);
            Log.v(LOG_TAG, "stop device scan");
        }
        invalidateOptionsMenu();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.tryAgain:
                mHandler.sendEmptyMessage(MSG_SHOW_FIRST_PAGE);
                scanLeDevice(true);
                break;
        }
    }
}
