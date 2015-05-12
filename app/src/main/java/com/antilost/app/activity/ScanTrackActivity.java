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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.bluetooth.UUID;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.Utils;
import com.antilost.app.view.DotsMarquee;

import java.util.Set;

public class ScanTrackActivity extends Activity implements View.OnClickListener {

    public static final int MSG_SHOW_CONNECTING_PAGE = 1;
    public static final int MSG_SHOW_SEARCH_FAILED_PAGE = 2;
    public static final int MSG_SHOW_SCANNING_PAGE = 3;

    public static final int MAX_COUNT = 5;

    private static final String LOG_TAG = "ScanTrackActivity";

    private static final long SCAN_PERIOD = 30 * 1000;
    public static final int MIN_RSSI_ACCEPTABLE = -60;
    private static final int MAX_RETRY_TIMES = 5;


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
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isFinishing()) {
                                scanLeDevice(false);
                                Log.v(LOG_TAG, "Activity is finishing, stop scan");
                                return;
                            }

                            String deviceName = device.getName();
                            //check device name first
                            if (!Utils.DEVICE_NAME.equals(deviceName)) {
                                Log.w(LOG_TAG, "get unkown device of name " + deviceName);
                                return;
                            }

                            if(rssi < MIN_RSSI_ACCEPTABLE) {
                                Log.i(LOG_TAG, String.format("%s 's rssi is too small, rssi:%d", device.getAddress(), rssi));
                                return;
                            }

                            mTrackIds = mPrefsManager.getTrackIds();
                            String deviceAddress = device.getAddress();
                            if (mTrackIds.contains(deviceAddress)) {
                                Log.v(LOG_TAG, "find already bind device");
                                return;
                            }
                            //stop scan for connecting
                            scanLeDevice(false);

                            if (!mTrackIds.contains(deviceAddress)) {
                                if(!isFinishing()) {
                                    Log.v(LOG_TAG, "find a new mTrack device." + deviceAddress);
                                    mHandler.removeMessages(MSG_SHOW_SEARCH_FAILED_PAGE);
                                    tryConnectBluetoothGatt(deviceAddress);
                                }
                            }
                        }
                    });
                }
            };

    private BluetoothGatt mConnectedBluetoothGatt;
    //flag to keep only one track is connecting.
    private boolean mConnectingGatt;

    //track connect failed retry times count;
    private int mRetryTimes = 0;
    private final BluetoothGattCallback  mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            Log.v(LOG_TAG, "mBluetoothGattCallback.onConnectionStateChange get state " + newState);
            //status 133 is too easy to happend.
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if(newState == BluetoothProfile.STATE_CONNECTED && !isFinishing()) {
                    Log.v(LOG_TAG, "bluetooth connection state is STATE_CONNECTED");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(gatt.discoverServices()) {
                                Log.i(LOG_TAG, "send gatt.discoverServices command success.");
                            } else {
                                Log.e(LOG_TAG, "send gatt.discoverServices command failed..");
                                mConnectingGatt = false;
                                gatt.close();
                                mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
                            }
                        }
                    }, 1000);
                } else {
                    mConnectingGatt = false;
                    Log.v(LOG_TAG, "Bluetooth disconnect");
                    gatt.close();
                    mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
                }
            //gatt status not ok
            } else {
                Log.e(LOG_TAG, "Scan Track  onConnectionStateChange   status is not success status: " + status);
                //TODO avoid infinite loop
                gatt.close();

                if(mRetryTimes < MAX_RETRY_TIMES) {
                    if(!isFinishing()) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(LOG_TAG, "Retry gatt connection.. after 1000 ms");
                                mRetryTimes++;
                                Utils.connectBluetoothGatt(gatt.getDevice(), ScanTrackActivity.this, mBluetoothGattCallback);
                            }
                        }, 500);
                    }
                } else {
                    Log.e(LOG_TAG, "after max retry times, give up connections");
                    mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
                    mConnectingGatt = false;
                }
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Log.d(LOG_TAG, "onServiceDiscovered... " + status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
               mHandler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       BluetoothGattService service = gatt.getService(com.antilost.app.bluetooth.UUID.CUSTOM_VERIFIED_SERVICE);
                       if(service != null) {
                           BluetoothGattCharacteristic pressedAddCharacteristic = service.getCharacteristic(UUID.CHARACTERISTIC_PRESSED_FOR_ADD);
                           if (pressedAddCharacteristic == null) {
                               Log.w(LOG_TAG, " pressed Add Characteristic is null");
                               gatt.close();
                               scanLeDevice(true);
                               return;
                           }

                           if(gatt.readCharacteristic(pressedAddCharacteristic)) {
                               Log.i(LOG_TAG, "read pressed characteristic ok");
                               return;
                           } else {
                               Log.i(LOG_TAG, "read pressed characteristic failed");
                           }
                       } else {
                           Log.e(LOG_TAG, "gatt has no custom verified service in onServicesDiscovered");
                       }
                       //show failed page except read ok
                       mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
                   }
               }, 500);
            } else {
                Log.v(LOG_TAG, "onServicesDiscovered status is not sucess.");
                gatt.close();
                mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BluetoothGattService service = gatt.getService(com.antilost.app.bluetooth.UUID.CUSTOM_VERIFIED_SERVICE);

            if(service != null) {
                BluetoothGattCharacteristic pressedAddCharacteristic = service.getCharacteristic(UUID.CHARACTERISTIC_PRESSED_FOR_ADD);
                if (pressedAddCharacteristic == null) {
                    Log.w(LOG_TAG, " pressed Add Characteristic is null");
                    gatt.close();
                    mConnectingGatt = false;
                    mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
                    return;
                }

                try {
                    byte[] valueBytes = pressedAddCharacteristic.getValue();
                    if(valueBytes != null) {
                        int keyPressed = pressedAddCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                        if(keyPressed == 0) {
                            Log.w(LOG_TAG, " scan and add an unpressed track");
                            gatt.close();
                            mConnectingGatt = false;
                            mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(LOG_TAG, "error happened while read key pressed value");
                    mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
                    return;
                }

                Log.i(LOG_TAG, "find a key pressed track.");
                characteristic = service.getCharacteristic(UUID.CHARACTERISTIC_CUSTOM_VERIFIED);
                if(characteristic != null) {
                    characteristic.setValue(Utils.VERIFY_CODE);
                    if(gatt.writeCharacteristic(characteristic)) {
                        log("Write verify code success.");
                        mConnectedBluetoothGatt = gatt;
                        scanLeDevice(false);
                        startTrackEdit();
                        return;
                    } else {
                        log("Write verify code failed.");
                    }
                } else {
                    Log.e(LOG_TAG, "gatt has no custom verified characteristic in verifyConnection");
                }
            } else {
                Log.e(LOG_TAG, "gatt has no custom verified service in verifyConnection");
            }
            mHandler.sendEmptyMessage(MSG_SHOW_SEARCH_FAILED_PAGE);
        }
    };
    private ImageView mSearchingIcon;
    private Animation mSearchingAnimation;

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
        startService(new Intent(ScanTrackActivity.this, BluetoothLeService.class));
    }

    private void reconnectMissingTrack(String address) {
        startService(new Intent(ScanTrackActivity.this, BluetoothLeService.class));
    }


    private IntentFilter makeBroadcastReceiverIntentFilter() {
        IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_GATT_CONNECTED);
        return filter;
    }

    private void tryConnectBluetoothGatt(final String deviceAddress) {
        if(mConnectedBluetoothGatt != null) {
            Log.d(LOG_TAG, "in tryConnectBluetoothGatt after le scan, already get one connected gatt.");
            return;
        }

        if (mConnectingGatt) {
            Log.d(LOG_TAG, "In tryConnectBluetoothGatt after le scan, one connecting gatt is ongoing.");
            return;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        mConnectingGatt = true;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mRetryTimes = 0;
                BluetoothGatt newGatt = Utils.connectBluetoothGatt(device, ScanTrackActivity.this, mBluetoothGattCallback);
                if (newGatt == null) {
                    Log.w(LOG_TAG, "Scan track connect bluetooth Gatt return null.");
                }
            }
        }, 2000);
    }

    private void startTrackEdit() {
        if(mConnectedBluetoothGatt != null) {
            sBluetoothConnected = mConnectedBluetoothGatt;
            Intent i = new Intent(ScanTrackActivity.this, TrackREditActivity.class);
            i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mConnectedBluetoothGatt.getDevice().getAddress());
            i.putExtra(TrackREditActivity.EXTRA_EDIT_NEW_TRACK, true);
            startActivity(i);
            finish();
            mConnectedBluetoothGatt = null;
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

        mSearchingIcon = (ImageView) findViewById(R.id.imageViewSearchIcon);
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
                        scanLeDevice(false);
                        mFirstPage.setVisibility(View.GONE);
                        mConnectingPage.setVisibility(View.GONE);
                        mFailedPage.setVisibility(View.VISIBLE);
                        break;
                    case MSG_SHOW_SCANNING_PAGE:
                        mFirstPage.setVisibility(View.VISIBLE);
                        mConnectingPage.setVisibility(View.GONE);
                        mFailedPage.setVisibility(View.GONE);
                        break;
                }
            }
        };

        scanLeDevice(true);
    }

    @Override
    protected void onDestroy() {
        if(mConnectedBluetoothGatt != null) {
            mConnectedBluetoothGatt.close();
            mConnectedBluetoothGatt = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        unregisterReceiver(mGattUpdateReceiver);
        mBindingDotsMarquee.stopMarquee();
        mConnecingDotsMarquee.stopMarquee();
        stopSearchAnimation();
    }

    private void stopSearchAnimation() {
    }

    private void startSearchingAnimation() {
        if(mSearchingAnimation == null) {
            mSearchingAnimation = AnimationUtils.loadAnimation(this, R.anim.searching);
        }
        mSearchingIcon.startAnimation(mSearchingAnimation);
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
        startSearchingAnimation();
    }


    /**
     * scan ble device and set timeout page
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            gattServiceIntent.setAction(BluetoothLeService.ACTION_STOP_BACKGROUND_LOOP);
            startService(gattServiceIntent);
            // Stops scanning after a pre-defined scan period.
            Log.w(LOG_TAG, "scanLeDevice " + enable);
            if(mScanning) {
                Log.w(LOG_TAG, "scan alreay start...");
                return;
            } else {
                mScanning = true;
                if(mBluetoothAdapter.startLeScan(mLeScanCallback)) {
                    mHandler.sendEmptyMessage(MSG_SHOW_SCANNING_PAGE);
                    Log.i(LOG_TAG, "startLeScan success");
                } else {
                    Log.w(LOG_TAG, "startLeScan failed retry");
                }
                mHandler.sendEmptyMessageDelayed(MSG_SHOW_SEARCH_FAILED_PAGE, SCAN_PERIOD);
            }

        } else {
            try {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.v(LOG_TAG, "stop device scan");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                if(mConnectedBluetoothGatt != null) {
                    mConnectedBluetoothGatt.close();
                }
                finish();
                break;
            case R.id.tryAgain:
                scanLeDevice(true);
                break;
        }
    }
}
