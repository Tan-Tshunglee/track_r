package com.antilost.app.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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

public class BindingTrackRActivity extends Activity implements View.OnClickListener {

    public static final int MSG_SHOW_CONNECTING_PAGE = 1;
    public static final int MSG_SHOW_SEARCH_FAILED_PAGE = 2;
    public static final int MSG_SHOW_FIRST_PAGE = 3;

    public static final int MAX_COUNT = 5;

    public static final int MAX_RETRY_TIMES = 12;
    public static final int MSG_RETRY_SCAN_LE = 100;

    private static final String LOG_TAG = "BindingTrackRActivity";

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

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String deviceName = device.getName();

                            if(!Utils.DEVICE_NAME.equals(deviceName) ) {
                                if(BuildConfig.DEBUG && "Keyfobdemo".equals(deviceName)) {
                                    Log.i(LOG_TAG, "debug mode allow  device of name " + deviceName);
                                } else {
                                    Log.w(LOG_TAG, "get unkown device of name " + deviceName);
                                    return;
                                }

                            }
                            scanLeDevice(false);
                            mTrackIds = mPrefsManager.getTrackIds();
                            String deviceAddress = device.getAddress();
                            if(mTrackIds.contains(deviceAddress) && !mPrefsManager.isMissedTrack(deviceAddress) && !mPrefsManager.isClosedTrack(deviceAddress)) {
                                //mPrefsManager.addTrackId(deviceAddress);
                                Log.v(LOG_TAG, "find already bind device");
                                return;
                            }

//                            Toast.makeText(BindingTrackRActivity.this, "get device address " + deviceAddress, Toast.LENGTH_LONG).show();
                            Log.v(LOG_TAG, "found bluetooth device address + " + deviceAddress);
//                            startService(new Intent(BindingTrackRActivity.this, MonitorService.class));
                            if(mPrefsManager.isClosedTrack(deviceAddress)) {
                               reconnectedClosedTrackR(deviceAddress);
                               return;
                            }
                            if(mPrefsManager.isMissedTrack(deviceAddress)) {
                                Log.v(LOG_TAG, "found disconnected trackr.");
                                reconnectMissingTrack(deviceAddress);
                            } else {
                                startBindTackOnServer(deviceAddress);
                            }

                        }
                    });
                }
            };

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
        startService(new Intent(BindingTrackRActivity.this, BluetoothLeService.class));
        finish();
    }

    private void reconnectMissingTrack(String address) {
        mPrefsManager.saveMissedTrack(address, false);
        startService(new Intent(BindingTrackRActivity.this, BluetoothLeService.class));
        finish();
    }


    private IntentFilter makeBroadcastReceiverIntentFilter() {
        IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_GATT_CONNECTED);
        return filter;
    }

    private void startBindTackOnServer(final String deviceAddress) {
        mHandler.sendEmptyMessage(MSG_SHOW_CONNECTING_PAGE);
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SecureRandom random = new SecureRandom();
//
//                String Id = BuildConfig.DEBUG ?  String.valueOf(random.nextInt()) : deviceAddress;
//                final BindCommand command = new BindCommand(String.valueOf(mPrefsManager.getUid()), deviceAddress, Id, String.valueOf(1));
//                command.execTask();
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(command.success()) {
//                            Toast.makeText(BindingTrackRActivity.this, getString(R.string.binding_success), Toast.LENGTH_SHORT).show();
//                            Intent i = new Intent(BindingTrackRActivity.this, TrackREditActivity.class);
//                            i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, deviceAddress);
//                            startActivity(i);
//                            finish();
//                            return;
//                        } else if(command.resultError()) {
//                            Toast.makeText(BindingTrackRActivity.this, getString(R.string.already_binded), Toast.LENGTH_SHORT).show();
//                        } else if(command.isNetworkError()) {
//                            Log.v(LOG_TAG, "network status error!");
//                        } else if(command.isStatusBad()) {
//                            Log.v(LOG_TAG, "server bad  status error!");
//                        } else {
//                            Log.v(LOG_TAG, "unkown error!");
//                        }
////                        mPrefsManager.removeTrackId(deviceAddress);
//                        mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
//                    }
//                });
//
//            }
//        });
//        t.start();

        Intent i = new Intent(BindingTrackRActivity.this, TrackREditActivity.class);
        i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, deviceAddress);
        startActivity(i);
        finish();
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
            Toast.makeText(this,  getString(R.string.your_device_no_ble_support), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mPrefsManager = PrefsManager.singleInstance(this);

        if(mPrefsManager.getTrackIds().size() == MAX_COUNT) {
            Toast.makeText(this,  getString(R.string.max_device_limit_reached), Toast.LENGTH_SHORT).show();
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
                        if(mScanedTime < MAX_RETRY_TIMES * SCAN_PERIOD) {
                            Log.v(LOG_TAG, "scan last time " + mScanedTime / 1000);
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
        startService(gattServiceIntent);
        scanLeDevice(true);
    }

    private void finishAndEdit(String deviceAddress) {
        Intent i = new Intent(this, TrackREditActivity.class);
        i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, deviceAddress);
        startActivity(i);
        finish();
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
            mBluetoothAdapter.startLeScan(mLeScanCallback);
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
