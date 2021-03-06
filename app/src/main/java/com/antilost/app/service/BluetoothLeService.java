package com.antilost.app.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.antilost.app.BuildConfig;
import com.antilost.app.R;
import com.antilost.app.TrackRApplication;
import com.antilost.app.activity.DisconnectAlertActivity;
import com.antilost.app.activity.FindmeActivity;
import com.antilost.app.activity.FoundByOthersActivity;
import com.antilost.app.activity.MainTrackRListActivity;
import com.antilost.app.activity.ScanTrackActivity;
import com.antilost.app.activity.TrackRActivity;
import com.antilost.app.model.TrackR;
import com.antilost.app.network.BindCommand;
import com.antilost.app.network.Command;
import com.antilost.app.network.FetchLostLocationCommand;
import com.antilost.app.network.LostDeclareCommand;
import com.antilost.app.network.ReportLostLocationCommand;
import com.antilost.app.network.ReportUnkownTrackLocationCommand;
import com.antilost.app.network.UpdateTrackImageCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.receiver.Receiver;
import com.antilost.app.util.LocUtils;
import com.antilost.app.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//import com.baidu.location.BDLocation;
//import com.baidu.location.BDLocationListener;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        AMapLocationListener {
    private final static String LOG_TAG = "BluetoothLeService";
    private static final int ONGOING_NOTIFICATION = 1;
    public static final int NOTIFICATION_ID_TRACK_FOUND_BY_OTHERS = 2;
    public static final int NOTIFICATION_ID_TRACK_DISCONNECTED = 3;
    public static final int NOTIFICATION_ID_TRACK_RECONNECTED = 3;


    public static  final String INTENT_FROM_BROADCAST_EXTRA_KEY_NAME = "INTENT_FROM_BROADCAST_EXTRA_KEY_NAME";


    //private static final int MSG_CLEANUP_DISCONNECTED_GATT = 2;
    private static final int MSG_LOOP_READ_RSSI = 3;
    private static final int MSG_FAST_REPEAT_MODE_FLAG = 4;
    private static final int MSG_DISCOVER_BLE_SERVICES = 5;
    private static final int MSG_VERIFY_CONNECTION_AFTER_SERVICE_DISCOVER = 6;
    private static final int MSG_DELAY_CHECK_NEW_TRACK_CONNECTED = 7;
    private static final int MSG_STOP_BLE_SCAN = 8;
    private static final int MSG_CONNECT_WAITING_TRACK = 9;
    private static final int MSG_BLOCKING_CLEAN_UP_DEAD_CONNECTION = 10;


    public static final int ALARM_REPEAT_PERIOD = 2 * 60 * 1000;

    public static final int FAST_ALARM_REPEAT_PERIOD = 10 * 1000;
    private static final int SCAN_PERIOD_OF_RSSI_READ = 10 * 1000;

    public static final int LOCATION_UPDATE_PERIOD_IN_MS = 5 * 60 * 1000;

    public static final int TIME_TO_KEEP_FAST_ALARM_REPEAT_MODE = 2 * 60 * 1000;

    public static final int MIN_DISTANCE = 20;
    public static final int SCAN_TIMEOUT_MS = 15 * 1000;




    public final static String ACTION_GATT_CONNECTED =
            "com.antilost.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.antilost.bluetooth.le.ACTION_GATT_DISCONNECTED";

    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.antilost.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.antilost.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.antilost.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_GATT_KEY_PRESSED =
            "com.antilost.bluetooth.le.ACTION_KEY_PRESSED";

    public final static String ACTION_BATTERY_LEVEL_READ =
            "com.antilost.bluetooth.le.ACTION_BATTERY_LEVEL_READ";

    public final static String ACTION_RSSI_READ =
            "com.antilost.bluetooth.le.ACTION_RSSI_READ";

    public final static String ACTION_DEVICE_CLOSED =
            "com.antilost.bluetooth.le.ACTION_DEVICE_CLOSED";

    public final static String ACTION_DEVICE_UNBIND =
            "com.antilost.bluetooth.le.ACTION_DEVICE_UNBIND";

    public final static String ACTION_DEVICE_FAR_AWAY =
            "com.antilost.bluetooth.le.ACTION_DEVICE_FAR_AWAY";

    public final static String ACTION_DEVICE_CLICKED =
            "com.antilost.bluetooth.le.ACTION_DEVICE_CLICKED";

    public final static String EXTRA_KEY_BLUETOOTH_ADDRESS =
            "EXTRA_KEY_BLUETOOTH_ADDRESS";

    public final static String ACTION_DEVICE_RING_COMMAND_WRITE_DONE =
            "com.antilost.bluetooth.le.ACTION_DEVICE_RING_COMMAND_WRITE_DONE";


    public final static String ACTION_DEVICE_STOP_RING_COMMAND_WRITE_DONE =
            "com.antilost.bluetooth.le.ACTION_DEVICE_STOP_RING_COMMAND_WRITE_DONE";


    public final static String ACTION_DEVICE_HARDWARE_VERSION_READ =
            "com.antilost.bluetooth.le.ACTION_DEVICE_HARDWARE_VERSION_READ";

    public final static String EXTRA_KEY_HARDWARE_VERSION =
            "EXTRA_KEY_HARDWARE_VERSION";


    public final static String ACTION_STOP_BACKGROUND_LOOP =
            "com.antilost.bluetooth.le.ACTION_STOP_BACKGROUND_LOOP";



    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private HashMap<String, BluetoothGatt> mBluetoothGatts = new HashMap<String, BluetoothGatt>(5);
    private HashMap<String, MyBluetootGattCallback> mBluetoothCallbacks = new HashMap<String, MyBluetootGattCallback>(5);
    private HashMap<String, Integer> mGattConnectionStates = new HashMap<String, Integer>(5);
    private HashMap<String, Integer> mGattsRssis = new HashMap<String, Integer>(5);
    private HashMap<String, Integer> mGattsBatteryLevel = new HashMap<String, Integer>(5);
    private HashSet<String> mLostGpsNeedUpdateIds = new HashSet<String>(5);
    private HashMap<String, Long> mDeclaredLostTrackLastFetchedTime = new HashMap<String, Long>(5);


    private HashSet<String> mUnkownTrackUpload = new HashSet<String>();
    private HashSet<String> mWaitingConnectionTracks = new HashSet<String>();
    private String mDelayAlertBySafeZone;


    //    private LocationManager mLocationManager;
    private Location mLastLocation;
    private WifiManager mWifiManager;
    private PendingIntent mPendingIntent;
    private LocationManagerProxy mAmapLocationManagerProxy;
    private long mLastStartCommandMeet;

//    private LocationClient mBaiduLocationClient;

    private NotificationManager mNotificationManager;
    private IntentFilter mIntentFilter;
    private ConnectivityManager mConnectivityManager;
    private LocationManager mLocationManager;
    private ScanResultListener mScanResultListener;
    private boolean mUpdatingAllTrackSleepState;


    private enum ConnectionState {
        IDLE,//idle
        SCANNING,//scaning track
        CONNECTING, //connecting one track
        BLOCKING //scan and connection is doing in scan activity
    }

    public interface ScanResultListener {
        void onConnectionStart();
        void onSuccess();
    }

    private ConnectionState mConnectionState = ConnectionState.IDLE;
    private String mConnectingTrackAddress = null;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(LOG_TAG, "BluetoothLeService onSharedPreferenceChanged() key " + key);

        if (PrefsManager.PREFS_SLEEP_MODE_KEY.equals(key)
                || PrefsManager.PREFS_SLEEP_START_TIME_KEY.equals(key)
                || PrefsManager.PREFS_SLEEP_END_TIME_KEY.equals(key)
                || PrefsManager.PREFS_SAFE_ZONE_ENABLED.equals(key)) {
            Log.v(LOG_TAG, "sleep mode or safe zone mode change");
            updateAllTrackSleepState();
        }
    }

    private void updateAllTrackSleepState() {

        if(mUpdatingAllTrackSleepState) {
            return;
        }
        Thread t = new Thread () {
            @Override
            public void run() {
                Log.v(LOG_TAG, "background update all track sleep state.");
                mUpdatingAllTrackSleepState = true;
                Set<String> ids = mPrefsManager.getTrackIds();
                for(final String address: ids) {
                    Integer connectionState = mGattConnectionStates.get(address);
                    BluetoothGatt bluetoothGatt = mBluetoothGatts.get(address);
                    if(connectionState != null
                            && bluetoothGatt != null
                            && connectionState == BluetoothProfile.STATE_CONNECTED) {

                        if(!inFastRepeatMode()) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updatesSingleTrackSleepState(address);
                                }
                            });
                            try {
                                Log.d(LOG_TAG, "sleep one track ok waiting a time.");
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                mUpdatingAllTrackSleepState = false;
            }
        };
        t.start();
    }


    private void cleanUpDeadConnection() {
        Thread t = new Thread () {
            @Override
            public void run() {

                if(mHandler.hasMessages(MSG_BLOCKING_CLEAN_UP_DEAD_CONNECTION)) {
                    Log.v(LOG_TAG, "blocking clean up dean connection.");
                    return;
                }
                //wait for 3 hour to next check
                mHandler.sendEmptyMessageDelayed(MSG_BLOCKING_CLEAN_UP_DEAD_CONNECTION, 3 * 60 * 60 * 1000);
                Log.v(LOG_TAG, "background update all track sleep state.");

                Set<String> ids = mPrefsManager.getTrackIds();

                for(final String address: ids) {
                    Integer connectionState = mGattConnectionStates.get(address);
                    BluetoothGatt bluetoothGatt = mBluetoothGatts.get(address);
                    if(connectionState != null
                            && bluetoothGatt != null
                            && connectionState == BluetoothProfile.STATE_CONNECTED) {



                        if(!inFastRepeatMode()) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updatesSingleTrackSleepState(address);
                                }
                            });
                            try {
                                Log.d(LOG_TAG, "sleep one track ok waiting a time.");
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        t.start();
    }

    public boolean inSleepTime() {
        boolean sleepMode = mPrefsManager.getSleepMode();

        if (!sleepMode) {
            return false;
        }
        long startTime = mPrefsManager.getSleepTime(true);
        long endTime = mPrefsManager.getSleepTime(false);
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar startDate = new GregorianCalendar();
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        startDate.add(Calendar.MILLISECOND, (int) startTime);

        GregorianCalendar endDate = new GregorianCalendar();
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        endDate.add(Calendar.MILLISECOND, (int) endTime);

        boolean result = false;

        //start time same with end time
        if(startTime == endTime) {
        //cross the midnight
        } else if (startTime > endTime) {
            result = now.after(startDate) || now.before(endTime);
        } else {
            result = now.after(startDate) && now.before(endDate);
        }

        return result;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    if(!mPrefsManager.validUserLog()) {
                        Log.v(LOG_TAG, "User not login.");
                        return;
                    }
                    String name = device.getName();
                    if(!Utils.DEVICE_NAME.equals(name)
                            && !Utils.DEVICE_NAME_NEW.equals(name)) {
                        Log.d(LOG_TAG, "scan an unkown name devices... with name:" + name);
                        return;
                    }

                    Set<String> ids = mPrefsManager.getTrackIds();
                    String deviceAddress = device.getAddress();
                    if (ids.contains(deviceAddress)) {
                        if(mConnectionState == ConnectionState.SCANNING) {
                            try {
                                mConnectionState = ConnectionState.IDLE;
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        addTrackToWaitingSet(deviceAddress);
                    //found an unbind track
                    } else {
                        if(!mUnkownTrackUpload.contains(deviceAddress)) {
                            Log.v(LOG_TAG, "an unbind track has been scanned.");
                            uploadUnTrackGps(deviceAddress);
                        }
                    }
                }
            };


    private void uploadUnTrackGps(final String deviceAddress) {

        Thread uploadThread = new Thread() {
            @Override
            public void run() {
                if (mLastLocation != null) {
                    Command command = new ReportUnkownTrackLocationCommand(deviceAddress, mLastLocation);
                    command.setPassword(mPrefsManager.getPassword());
                    command.execTask();
                    if (command.success()) {
                        Log.v(LOG_TAG, "Update unkown track r 's location successfully.");
                    } else {
                        Log.w(LOG_TAG, "Fail to update unkown track r 's location ");
                    }
                    mUnkownTrackUpload.add(deviceAddress);
                } else {
                    Log.d(LOG_TAG, "found unknown track, but location is missing.");
                    registerLocationListener();
                }
            }
        };
        uploadThread.start();
    }

    private void addTrackToWaitingSet(final String address) {

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(LOG_TAG, "Bluetooth not support or disabled.");
            if(mConnectionState == ConnectionState.CONNECTING)
            mConnectionState = ConnectionState.IDLE;
            return;
        }

        Integer oldState = mGattConnectionStates.get(address);
        BluetoothGatt bluetoothGatt = mBluetoothGatts.get(address);
        if (bluetoothGatt != null
                && oldState != null
                && oldState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(LOG_TAG, "Device already connected after scan, just bail out.");
            return;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(LOG_TAG, "Device not found.  Unable to connectSingleTrack.");
            return;
        }

        if(mWaitingConnectionTracks.contains(address)) {
            Log.v(LOG_TAG, String.format("%s is already waiting for connection.", address));
            if(mConnectionState == ConnectionState.IDLE) {
                mHandler.sendEmptyMessage(MSG_CONNECT_WAITING_TRACK);
            }
        } else {
            if (mConnectionState == ConnectionState.IDLE) {
                Log.i(LOG_TAG, "Add track to waiting connection set." + address);
                mWaitingConnectionTracks.add(address);
                mHandler.sendEmptyMessage(MSG_CONNECT_WAITING_TRACK);
            } else {
                Log.w(LOG_TAG, address + " is already waiting for connectionry add address to waiting connection set while connection state is not idle.");
            }
        }
    }



    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("LocationManager", s + " status changed.");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("LocationManager", s + " is enabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("LocationManager", s + " is disabled");
    }


    public Location getLastLocation() {
        if(mLastLocation == null) {
            registerLocationListener();
        }
        return mLastLocation;
    }

    public void onUserInteraction() {
        enterFastRepeatMode();
        updateRepeatAlarmRegister(true);
        Log.v(LOG_TAG, "onUserInteraction in BluetoothLeService.");
    }


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.

    private class MyBluetootGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {

            final String address = gatt.getDevice().getAddress();
            Log.v(LOG_TAG, String.format("onConnectionStateChange address:%s, and status %d", address, status));

            if(!mPrefsManager.validUserLog()) {
                Log.v(LOG_TAG, "user logout close connection");
                mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                gatt.close();
                mWaitingConnectionTracks.clear();
                mConnectionState = ConnectionState.IDLE;
                mAddingDeviceAddress = null;
                return;
            }
            //
            if (address.equals(mAddingDeviceAddress)) {

                if(status != BluetoothGatt.GATT_SUCCESS) {
                    Log.i(LOG_TAG, "connect new add track failed retry.. status code:" + status);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gatt.close();
                            mBluetoothGatts.remove(address);
                            mGattConnectionStates.put(address, BluetoothProfile.STATE_CONNECTED);
                            tryConnectGatt(address, mBluetoothAdapter.getRemoteDevice(address));
                        }
                    }, 1000);
                    return;
                }
            } else {
                //not the adding track
                //status 8 means GATT_INSUF_AUTHORIZATION, on Samsung S5 android 5.0,
                //when track lose power, status is 8
                //phone bluetooth disable status is 22
                if (status != BluetoothGatt.GATT_SUCCESS
                        && status != 8
                        && status != 22
                        && newState != BluetoothProfile.STATE_DISCONNECTED) {
                    try {
                        Log.e(LOG_TAG, "onConnectionStateChange gatt status is not success. status is " + status);
                        mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                        mBluetoothGatts.remove(address);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mConnectionState = ConnectionState.IDLE;
                    mWaitingConnectionTracks.remove(address);
                    Log.e(LOG_TAG, "connect failed,  set state to idle");
                    return;
                }
            }


            if(!mPrefsManager.getTrackIds().contains(address)) {

                if(address != null && address.equals(mAddingDeviceAddress)) {
                    //allow adding device to connected.
                    Log.v(LOG_TAG, "onConnectionStateChange of newly add track...");
                } else {
                    Log.w(LOG_TAG, "close an old connection not managed or newly add.");
                    mGattConnectionStates.remove(address);
                    mBluetoothGatts.remove(address);
                    mWaitingConnectionTracks.remove(address);
                    gatt.close();
                    mConnectionState = ConnectionState.IDLE;
                    mAddingDeviceAddress = null;
                    return;
                }
            }

            Integer oldState = mGattConnectionStates.put(address, newState);
            if (oldState == null) {
                oldState = BluetoothProfile.STATE_DISCONNECTED;
            }

            mBluetoothGatts.put(address, gatt);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Attempts to discover services after successful connection.
                Message msg = mHandler.obtainMessage(MSG_DISCOVER_BLE_SERVICES, gatt);
                mHandler.sendMessageDelayed(msg, 1000);
                Log.i(LOG_TAG, "Attempting to fetch services delay 500 ms");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                if (oldState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(LOG_TAG, "Disconnected state GATT.");
                    enterFastRepeatMode();
                    sendBroadcast(new Intent(Receiver.REPEAT_BROADCAST_RECEIVER_ACTION));

                    if (mLostGpsNeedUpdateIds.add(address)) {
                        Log.v(LOG_TAG, "Add lost track's address to update list.");
                    }

                    if(mLastLocation != null) {
                        mPrefsManager.saveLastLostLocation(mLastLocation, address);
                    }
                    //request location update to save
                    unregisterAmapLocationListener();
                    registerLocationListener();

                    mPrefsManager.saveMissedTrack(address, true);
                    mPrefsManager.saveClosedTrack(address, false);
                    mPrefsManager.saveDeclareLost(address, false);

                    mPrefsManager.saveLastLostTime(address, System.currentTimeMillis());
                    broadcastUpdate(ACTION_GATT_DISCONNECTED, address);

                    if (mConnectionState == ConnectionState.CONNECTING) {
                        mHandler.removeMessages(MSG_CONNECT_WAITING_TRACK);
                        mConnectionState = ConnectionState.IDLE;
                    }

                    gatt.close();
                    mBluetoothGatts.remove(address);


                    if (inSafeZone()) {
                        Log.i(LOG_TAG, "In safe zone, ignore alert.");
                        mDelayAlertBySafeZone = address;
                        return;
                    }

                    if (mPrefsManager.getSleepMode() && inSleepTime()) {
                        Log.i(LOG_TAG, "In sleep duration, don't alert user.");
                        return;
                    }

                    if (mPrefsManager.getTrackIds().contains(address)) {
                        notifyUserDisconnected(address);
                        alertUserTrackDisconnected(address);
                    }

                    //other state no disconnected;
                } else {

                    Log.w(LOG_TAG, "onConnectionStateChange get disconnected state and old state is not connected.");
                    if(mConnectionState == ConnectionState.CONNECTING) {
                        mConnectionState = ConnectionState.IDLE;
                    }
                }
            }


            //even device is power off the newState can be STATE_CONNECTEDe,
            //we define device is connected after onServicesDiscovered is called success();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {

            final String address = gatt.getDevice().getAddress();

            if(!mPrefsManager.validUserLog()) {
                Log.v(LOG_TAG, "onServicesDiscovered user logout close connection");
                mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                gatt.close();
                mConnectionState = ConnectionState.IDLE;
                mWaitingConnectionTracks.clear();
                mAddingDeviceAddress = null;
                return;
            }


            if(address.equals(mAddingDeviceAddress)) {
                if(status != BluetoothGatt.GATT_SUCCESS) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tryConnectGatt(address, mBluetoothAdapter.getRemoteDevice(address));
                        }
                    }, 1000);
                    return;
                }
            }

            if(status != BluetoothGatt.GATT_SUCCESS) {
                mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                Log.e(LOG_TAG, "onServicesDiscovered called  with error status " + status);
                //let scan begin to work
                gatt.close();
                mConnectionState = ConnectionState.IDLE;
                mWaitingConnectionTracks.remove(address);
                if(address.equals(mAddingDeviceAddress)) {
                    tryConnectGatt(address, gatt.getDevice());
                }
                return;
            }
            Log.i(LOG_TAG, "gatt's service is discovered successfully");



            mGattConnectionStates.put(address, BluetoothProfile.STATE_CONNECTING);
            mBluetoothGatts.put(gatt.getDevice().getAddress(), gatt);
            Message msg = mHandler.obtainMessage(MSG_VERIFY_CONNECTION_AFTER_SERVICE_DISCOVER, gatt);
            mHandler.sendMessageDelayed(msg, 1000);
            registerLocationListener();
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, address);

        }

        private void registerKeyListener(BluetoothGatt gatt) {
            //registry key press notification;
            if (setCharacteristicNotification(
                    gatt,
                    UUID.fromString(com.antilost.app.bluetooth.UUID.SIMPLE_KEY_SERVICE_UUID_STRING),
                    com.antilost.app.bluetooth.UUID.CHARACTERISTIC_KEY_PRESS_UUID,
                    true)) {
                Log.v(LOG_TAG, "turn on key press notification ok");
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(setCharacteristicNotification(
                    gatt,
                    com.antilost.app.bluetooth.UUID.BATTERY_SERVICE_UUID,
                    com.antilost.app.bluetooth.UUID.CHARACTERISTIC_BATTERY_LEVEL_UUID,
                    true)) {
                Log.v(LOG_TAG, "turn on battery notification ok");
            }
        }

        public boolean setCharacteristicNotification(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid,
                                                     boolean enable) {
            try {
                if (BuildConfig.DEBUG)
                    Log.d(LOG_TAG, "setCharacteristicNotification(device=" + "  UUID="
                            + characteristicUuid + ", enable=" + enable + " )");

                BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);
                if(gatt.setCharacteristicNotification(characteristic, enable)) {
                    Log.i(LOG_TAG, "setCharacteristicNotification success.");
                } else {
                    Log.i(LOG_TAG, "setCharacteristicNotification failed.");
                }

                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                //descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                return gatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            final String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS) {


                UUID cId = characteristic.getUuid();
                UUID sId = characteristic.getService().getUuid();
                Log.d(LOG_TAG, String.format("onCharacteristicRead cId:%s and sId", cId, sId));
                if (sId.equals(com.antilost.app.bluetooth.UUID.BATTERY_SERVICE_UUID)
                        && cId.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_BATTERY_LEVEL_UUID)) {
                    int level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.d(LOG_TAG, "onCharacteristicRead callback battery is " + level);
                    mGattsBatteryLevel.put(gatt.getDevice().getAddress(), level);
                    broadcastBatteryLevel(level);
                } else if(cId.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_PRESSED_FOR_ADD)) {
                    int pressed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    if(pressed == 0) {
                        Log.w(LOG_TAG, "find a unpressed track ,ignore it.");
                        mAddingDeviceAddress = null;
                        if(mScanResultListener != null) {
                            Log.v(LOG_TAG, "restart scan for track add.");
                            gatt.close();
                            stopBackgroundOperation();
                            if(mBluetoothAdapter == null) {

                            } else {
                                mBluetoothAdapter.startLeScan(mScanForAddCallback);
                            }
                        } else {
                            Log.i(LOG_TAG, "mScanResultListener is null.");
                        }
                    } else {
                        Log.v(LOG_TAG, "find a pressed track to add..");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //after verify key write done we think this gatt as connected.
                                mGattConnectionStates.put(address, BluetoothProfile.STATE_CONNECTED);
                                broadcastUpdate(ACTION_GATT_CONNECTED, address);

                                if (mPrefsManager.isClosedTrack(address)) {
                                    Log.d(LOG_TAG, "reconnect to closed trackr");
                                    mPrefsManager.saveClosedTrack(address, false);
                                }
                                if (mPrefsManager.isMissedTrack(address)) {
//                                    notifyDeviceReconnected(address);
                                    mPrefsManager.saveMissedTrack(address, false);
                                }

                                if (mPrefsManager.isDeclaredLost(address)) {
                                    revokeLostDeclare(address);
                                }

                                mPrefsManager.saveDeclareLost(address, false);
                                mPrefsManager.saveLastLocFoundByOthers(null, address);
                                mPrefsManager.saveLastTimeFoundByOthers(-1, address);

//                                updateAndroidConnection(address);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyScanSuccess();
                                    }
                                }, 500);

                            }
                        }, 500);
                    }
                } else if(cId.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_HARDWARE_VERSION)) {
                    byte[] versions = characteristic.getValue();
                    Intent i = new Intent(ACTION_DEVICE_HARDWARE_VERSION_READ);
                    i.putExtra(EXTRA_KEY_BLUETOOTH_ADDRESS, address);
                    i.putExtra(EXTRA_KEY_HARDWARE_VERSION, versions);
                    sendBroadcast(i);
                }
            //status code not good
            } else {
                Log.e(LOG_TAG, "onCharacteristicRead status not success " + status);
                tryConnectGatt(address, gatt.getDevice());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            String address = gatt.getDevice().getAddress();
            Integer state = mGattConnectionStates.get(address);
            if(state == null || state != BluetoothProfile.STATE_CONNECTED) {
                gatt.close();
                return;
            }

            if(mPrefsManager.hasTrack(address)) {
                Log.v(LOG_TAG, "onCharacteristicChanged....");
                UUID charUuid = characteristic.getUuid();
                if (charUuid.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_KEY_PRESS_UUID)) {
                    byte[] data = characteristic.getValue();
                    int key = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    //key down is 0 and key up is 2;
                    Log.v(LOG_TAG, "onCharacteristicChanged key value is " + key);
                    if (key == 2) {
                        onTrackKeyLongPress();
                    } else if (key == 8) {
                        onTrackKeyClick(gatt.getDevice().getAddress());
                    }
                } else if(charUuid.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_BATTERY_LEVEL_UUID)) {
                    int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.v(LOG_TAG, "battery notify changed " + value);

                    mGattsBatteryLevel.put(gatt.getDevice().getAddress(), value);
                    broadcastBatteryLevel(value);
                }
            }
        }

        public void onCharacteristicWrite(final BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            final String address = gatt.getDevice().getAddress();


            UUID serviceUuid = characteristic.getService().getUuid();
            Log.v(LOG_TAG, "onCharacteristicWrite serviceUuid is " + serviceUuid);
            UUID charUuid = characteristic.getUuid();
            Log.i(LOG_TAG, "onCharacteristicWrite is " + charUuid);

            //
            if(status != BluetoothGatt.GATT_SUCCESS) {
                Log.v(LOG_TAG, "onCharacteristicWrite status is " + status);
                if(address.equals(mAddingDeviceAddress)) {
                    tryConnectGatt(address, gatt.getDevice());
                }
                gatt.close();
                return;
            }


            int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);


            if(charUuid == null || serviceUuid == null) {
                Log.e(LOG_TAG, "onCharacteristicWrite characteristic or service uuid is null");
                mAddingDeviceAddress = null;
                return;
            }
            Log.d(LOG_TAG, "onCharacteristicWrite value is " + value);

            //make track sleep command;
            if (serviceUuid.equals(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID)
                    && charUuid.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID)
                    ) {
                //value 3 means close track
                if(value == 3) {
                    Log.w(LOG_TAG, "sleep command write in onCharacteristicWrite ");

                    Integer newState = mGattConnectionStates.get(address);

                    if(!mPrefsManager.getTrackIds().contains(address)) {
                        //user unbind this track
                        gatt.close();
                        mBluetoothGatts.remove(address);
                        mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                        broadcastDeviceOff();
                    } else if(mPrefsManager.isClosedTrack(address)) {
                        //user turn off this track;
                        Log.d(LOG_TAG, "track close successfully.");
                        mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                        gatt.close();
                        mBluetoothGatts.remove(address);
                        broadcastDeviceOff();
                    } else {
                        //just make track sleep
                    }
                //2 means wake link lose
                } else if(value == 2) {
                    Log.v(LOG_TAG, "track wake up done");
                //0 means sleep track no link lost
                } else if(value == 0) {
                    Log.v(LOG_TAG, "track sleep done");
                }

            //custom verify code write
            } else if(serviceUuid.equals(com.antilost.app.bluetooth.UUID.CUSTOM_SERVICE)
                    && charUuid.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_CUSTOM_VERIFIED)) {
                Log.i(LOG_TAG, "write done callback of verify code ");
                registerKeyListener(gatt);
                if(address.equals(mDelayAlertBySafeZone)) {
                    mDelayAlertBySafeZone = null;
                }
                if(address.equals(mAddingDeviceAddress)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BluetoothGattService customService = gatt.getService(com.antilost.app.bluetooth.UUID.CUSTOM_SERVICE);
                            if (customService == null) {
                                tryConnectGatt(address, gatt.getDevice());
                            } else {
                                BluetoothGattCharacteristic pressedCharacterist
                                        = customService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_PRESSED_FOR_ADD);
                                gatt.readCharacteristic(pressedCharacterist);
                            }
                        }
                    }, 500);
                } else {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            mWaitingConnectionTracks.remove(address);
                            //after verify key write done we think this gatt as connected.
                            mGattConnectionStates.put(address, BluetoothProfile.STATE_CONNECTED);
                            broadcastUpdate(ACTION_GATT_CONNECTED, address);


                            if (mConnectionState == ConnectionState.CONNECTING) {
                                mConnectionState = ConnectionState.IDLE;
                                Log.d(LOG_TAG, "it 's time to read next track");
                                mHandler.sendEmptyMessageDelayed(MSG_CONNECT_WAITING_TRACK, 2000);

                                if (mPrefsManager.isClosedTrack(address)) {
                                    Log.d(LOG_TAG, "reconnect to closed trackr");
                                    mPrefsManager.saveClosedTrack(address, false);
                                }
                                if (mPrefsManager.isMissedTrack(address)) {
                                    notifyDeviceReconnected(address);
                                    mPrefsManager.saveMissedTrack(address, false);
                                }

                                if (mPrefsManager.isDeclaredLost(address)) {
                                    revokeLostDeclare(address);
                                }
                                mPrefsManager.saveDeclareLost(address, false);

                                mPrefsManager.saveLastLocFoundByOthers(null, address);
                                mPrefsManager.saveLastTimeFoundByOthers(-1, address);

//                                updateAndroidConnection(address);
//                                mHandler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        updatesSingleTrackSleepState(address);
//                                    }
//                                }, 500);
                            }
                        }
                    }, 1000);
                }


            //alert sound ring
            } else if(com.antilost.app.bluetooth.UUID.IMMEDIATE_ALERT_SERVICE_UUID.equals(serviceUuid)
                    && com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID.equals(charUuid)
                     ) {
                if(value == 2) {
                    broadcastUpdate(ACTION_DEVICE_RING_COMMAND_WRITE_DONE, address);
                } else if(value == 0) {
                    broadcastUpdate(ACTION_DEVICE_STOP_RING_COMMAND_WRITE_DONE, address);
                }

            }
        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
//            UUID uuid = descriptor.getUuid();
//            if(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID.equals(uuid)) {
//                Log.v(LOG_TAG, "CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID write done with value" + descriptor.getValue()[0]);
//            }
        }

        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

            String address = gatt.getDevice().getAddress();
            Log.i(LOG_TAG, "onReadRemoteRssi." + address);


            mGattsRssis.put(gatt.getDevice().getAddress(), rssi);
            broadcastRssiRead();
            receiverRssi(address, rssi);
        }
    }

//    private void updateAndroidConnection(String address) {
//        Integer state = mGattConnectionStates.get(address);
//        if(state != null && state == BluetoothProfile.STATE_CONNECTED) {
//            BluetoothGatt gatt = mBluetoothGatts.get(address);
//            BluetoothGattService customService = gatt.getService(com.antilost.app.bluetooth.UUID.CUSTOM_SERVICE);
//            if(customService != null) {
//                BluetoothGattCharacteristic androidCharacterisic =
//                        customService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ANDROID_SYSTEM_FLAG);
//
//                if(androidCharacterisic != null) {
//                    androidCharacterisic.setValue(new byte[] {1});
//                    if(gatt.writeCharacteristic(androidCharacterisic)) {
//                        Log.d(LOG_TAG, "gatt write android flag success.");
//                    } else {
//                        Log.e(LOG_TAG, "gatt write android flag failed.");
//                    };
//                } else {
//                    Log.e(LOG_TAG, "android flag characteristic is null.");
//                }
//            } else {
//                Log.e(LOG_TAG, "custom service is null.");
//            }
//
//        }
//    }

    private void notifyDeviceReconnected(String address) {
        mNotificationManager.cancel(address, NOTIFICATION_ID_TRACK_DISCONNECTED);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.app_name));
        TrackR trackR = mPrefsManager.getTrack(address);

        String trackName = null;
        if(!TextUtils.isEmpty(trackR.name)) {
            trackName = trackR.name;
        } else {
            String[] names = getResources().getStringArray(R.array.default_type_names);
            trackName =  names[trackR.type];
        }
        builder.setContentText(trackName + " has reconnected.");
        builder.setSmallIcon(R.drawable.ic_launcher);


        builder.setDefaults(Notification.DEFAULT_ALL);
        // Creates an Intent for the Activity
        Intent notifyIntent =
                new Intent(this, MainTrackRListActivity.class);

        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(notifyPendingIntent);
        mNotificationManager.notify(address, NOTIFICATION_ID_TRACK_RECONNECTED, builder.build());
    }

    private void notifyUserDisconnected(String address) {
        mNotificationManager.cancel(address, NOTIFICATION_ID_TRACK_RECONNECTED);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.app_name));
        TrackR trackR = mPrefsManager.getTrack(address);

        String trackName = null;
        if(!TextUtils.isEmpty(trackR.name)) {
            trackName = trackR.name;
        } else {
            String[] names = getResources().getStringArray(R.array.default_type_names);
            trackName =  names[trackR.type];
        }
        builder.setContentText(trackName + " has disconnected");
        builder.setSmallIcon(R.drawable.ic_launcher);


        // Creates an Intent for the Activity
        Intent notifyIntent =
                new Intent(this, MainTrackRListActivity.class);

        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(notifyPendingIntent);
        mNotificationManager.notify(address, NOTIFICATION_ID_TRACK_DISCONNECTED, builder.build());
    }

    private boolean inSafeZone() {
        String homeWifiSsid = mPrefsManager.getHomeWifiSsid();
        String officeSsid = mPrefsManager.getOfficeSsid();
        String otherSsid = mPrefsManager.getOtherSsid();
        WifiInfo info = mWifiManager.getConnectionInfo();
        String ssid = unornamatedSsid(info.getSSID());

        boolean safeWifiEnabled = mPrefsManager.getSafeZoneEnable();
        if (safeWifiEnabled &&
                ssid != null &&
                (    ssid.equals(homeWifiSsid)
                        || ssid.equals(officeSsid)
                        || ssid.equals(otherSsid))) {
            return true;
        }
        return false;
    }

    /**
     * remove double quote from start and end of the string
     */
    private String unornamatedSsid(String ssid) {
        ssid = ssid.replaceFirst("^\"", "");
        return ssid.replaceFirst("\"$", "");
    }

    private void reportTrackLostPosition(final String address) {
        if (mLastLocation != null) {
            mPrefsManager.saveLastLostLocation(mLastLocation, address);

            Thread t = new Thread() {
                @Override
                public void run() {
                    ReportLostLocationCommand command
                            = new ReportLostLocationCommand(mPrefsManager.getUid(), mLastLocation, address);
                    command.setPassword(mPrefsManager.getPassword());
                    command.execTask();
//                    command.dumpResult();
                }
            };
            t.start();
        }
    }

    private void revokeLostDeclare(String address) {
        int declareTobe =  0;
        Command declareCommand = new LostDeclareCommand(mPrefsManager.getUid(), address, declareTobe);
        try {
            declareCommand.setPassword(mPrefsManager.getPassword());
            declareCommand.execTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(declareCommand.success()) {
            Log.v(LOG_TAG, "revoke lost declaration ok.");
        } else {
            Log.e(LOG_TAG, "revoke lost declaration failed.");
        }
    }


    private void log(String msg) {
        if(BuildConfig.DEBUG) Log.d(LOG_TAG, msg);
    }


    private void enterFastRepeatMode() {
        mHandler.removeMessages(MSG_FAST_REPEAT_MODE_FLAG);
        mHandler.sendEmptyMessageDelayed(MSG_FAST_REPEAT_MODE_FLAG, TIME_TO_KEEP_FAST_ALARM_REPEAT_MODE);
    }

    private void exitFastRepeatMode() {
        mHandler.removeMessages(MSG_FAST_REPEAT_MODE_FLAG);
    }

    private boolean inFastRepeatMode() {
        return mHandler.hasMessages(MSG_FAST_REPEAT_MODE_FLAG);
    }


    private void receiverRssi(String address, int rssi) {

        //TODO far away alert;
//        if(rssi < -80) {
//            alertUserTrackDisconnected(address);
//        }

    }



    private void broadcastDeviceOff() {
        final Intent intent = new Intent(ACTION_DEVICE_CLOSED);
        sendBroadcast(intent);
    }

    private void broadcastRssiRead() {
        final Intent intent = new Intent(ACTION_RSSI_READ);
        sendBroadcast(intent);
    }

    private void broadcastBatteryLevel(int level) {
        final Intent intent = new Intent(ACTION_BATTERY_LEVEL_READ);
        intent.putExtra(EXTRA_DATA, level);
        sendBroadcast(intent);
    }

    private void alertUserTrackDisconnected(String address) {

        Intent i = new Intent(this, DisconnectAlertActivity.class);
        i.putExtra(DisconnectAlertActivity.EXTRA_KEY_DEVICE_ADDRESS, address);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

    }


    private Handler mHandler;

    private void onTrackKeyLongPress() {
        Log.v(LOG_TAG, "onTrackKeyLongPress...");

        Intent i = new Intent(this, FindmeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void onTrackKeyClick(String address) {
        Log.v(LOG_TAG, "onTrackKeyClick...");
        broadcastUpdate(ACTION_DEVICE_CLICKED, address);
    }


    private AlarmManager mAlarmManager;
    private PrefsManager mPrefsManager;


    private void broadcastUpdate(final String action, String address) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_KEY_BLUETOOTH_ADDRESS, address);
        sendBroadcast(intent);
    }



    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //connectSingleTrack devices when activity connectSingleTrack
        enterFastRepeatMode();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                if(state == BluetoothAdapter.STATE_ON) {
                    enterFastRepeatMode();
                    updateRepeatAlarmRegister(true);
                }
            } else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Log.d(LOG_TAG, "NETWORK_STATE_CHANGED_ACTION update all track sleep State");
                updateAllTrackSleepState();

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if(networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    if(mDelayAlertBySafeZone != null) {
                        alertUserTrackDisconnected(mDelayAlertBySafeZone);
                        mDelayAlertBySafeZone = null;
                    }
                }
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
//                    case MSG_CLEANUP_DISCONNECTED_GATT:
//                        removeMessages(MSG_CLEANUP_DISCONNECTED_GATT);
//                        Log.d(LOG_TAG, "handling MSG_CLEANUP_DISCONNECTED_GATT");
//                        Set<Map.Entry<String, Integer>> entrys = mGattConnectionStates.entrySet();
//                        for (Map.Entry<String, Integer> entry : entrys) {
//                            String address = entry.getKey();
//                            Integer state = entry.getValue();
//                            if (state == null) {
//                                Log.w(LOG_TAG, "Handler clean up device whose state is null." + address);
//                                mPrefsManager.saveMissedTrack(address, true);
//                                mBluetoothGatts.remove(address);
//
//                            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
//                                Log.w(LOG_TAG, "Handler clean up state STATE_DISCONNECTED device " + address);
//                                mPrefsManager.saveMissedTrack(address, true);
//                                mBluetoothGatts.remove(address);
//                            }
//                        }
//                        break;
                    case MSG_LOOP_READ_RSSI:
                        String address = (String) msg.obj;
                        Log.v(LOG_TAG, "will read rssi, whose address is " + address);
                        Integer state = mGattConnectionStates.get(address);
                        if (state != null && state == BluetoothProfile.STATE_CONNECTED) {
                            requestRssiLevel(address);
                        } else {
                            Log.w(LOG_TAG, "can not read rssi of disconnected device. state is " + state);
                        }
                        mHandler.removeMessages(MSG_LOOP_READ_RSSI);
                        msg = mHandler.obtainMessage(MSG_LOOP_READ_RSSI, address);
                        mHandler.sendMessageDelayed(msg, SCAN_PERIOD_OF_RSSI_READ);
                        break;

                    case MSG_VERIFY_CONNECTION_AFTER_SERVICE_DISCOVER:
                        Log.d(LOG_TAG, "try to verify connection");
                        BluetoothGatt gatt = (BluetoothGatt) msg.obj;
                        address  = gatt.getDevice().getAddress();
                        BluetoothGattService service = gatt.getService(com.antilost.app.bluetooth.UUID.CUSTOM_SERVICE);

                        if(service != null) {
                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_CUSTOM_VERIFIED);
                            if(characteristic != null) {
                                characteristic.setValue(Utils.VERIFY_CODE);
                                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                                if(gatt.writeCharacteristic(characteristic)) {
                                    log("write verify code success.");
                                    return;
                                } else {
                                    log("write verify code failed.");
                                }
                            } else {
                                Log.e(LOG_TAG, "gatt has no custom verified characteristic in verifyConnection");
                            }
                        } else {
                            Log.e(LOG_TAG, "gatt has no custom verified service in verifyConnection");
                        }
                        if(mConnectionState == ConnectionState.CONNECTING) {
                            //set idle to make scan go on;
                            mConnectionState = ConnectionState.IDLE;
                        }

                        mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                        if(address.equals(mAddingDeviceAddress)) {
                            tryConnectGatt(address, mBluetoothAdapter.getRemoteDevice(address));
                        }

                        break;
                    case MSG_DISCOVER_BLE_SERVICES:
                        try {
                            gatt = (BluetoothGatt) msg.obj;
                            if(gatt.discoverServices()) {
                                Log.v(LOG_TAG, "request discover services ok");
                            } else {
                                if(mConnectionState == ConnectionState.CONNECTING) {
                                    mConnectionState = ConnectionState.IDLE;
                                }

                                if(gatt.getDevice().getAddress().equals(mAddingDeviceAddress)) {
                                    tryConnectGatt(mAddingDeviceAddress, gatt.getDevice());
                                }
                                Log.e(LOG_TAG, "failed to discover services.");
                            }

                        } catch (Exception e) {
                            if(mConnectionState == ConnectionState.CONNECTING) {
                                mConnectionState = ConnectionState.IDLE;
                            }
                            e.printStackTrace();
                        }
                        break;

                    case MSG_DELAY_CHECK_NEW_TRACK_CONNECTED:

//                        state = mGattConnectionStates.get(msg.obj);
//                        if(state == null || state != BluetoothProfile.STATE_CONNECTED) {
//                            Log.e(LOG_TAG, "reconnect new track in delay check");
//                            gatt = mBluetoothGatts.get(msg.obj);
//                            if(gatt != null) {
//                                gatt.close();
//                            }
//                            address = (String) msg.obj;
//                            mBluetoothGatts.put(address, null);
//                            if(address != null && mBluetoothAdapter != null) {
//                                tryConnectGatt(address, mBluetoothAdapter.getRemoteDevice(address));
//                            }
//
//                        }
                        break;
                    case MSG_STOP_BLE_SCAN:
                        if(mBluetoothAdapter != null) {
                            try {
                                if(mConnectionState == ConnectionState.SCANNING) {
                                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                    mConnectionState = ConnectionState.IDLE;
                                    Log.v(LOG_TAG, "stop ble scan after a time");
                                } else {
                                    Log.w(LOG_TAG, "Handling MSG_STOP_BLE_SCAN but state is not scanning.");
                                }
                            } catch (Exception e) {
                                //this may cause java.lang.NullPointerException
                            }
                        }

                        break;
                    //idle mean connect new track
                    //connection means reconnect failed track.
                    case MSG_CONNECT_WAITING_TRACK:

                        if(mBluetoothAdapter == null
                                || !mBluetoothAdapter.isEnabled()) {
                            mConnectionState = ConnectionState.IDLE;
                            return;
                        }

                        if (mConnectionState == ConnectionState.IDLE) {
                            Iterator<String> it = mWaitingConnectionTracks.iterator();
                            if (it.hasNext()) {
                                mConnectionState = ConnectionState.CONNECTING;
                                mConnectingTrackAddress = it.next();
                                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectingTrackAddress);
                                Log.v(LOG_TAG, "connect to waiting track:" + mConnectingTrackAddress);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        tryConnectGatt(mConnectingTrackAddress, device);
                                    }
                                }, 1000);
                                //delay check connection timeout;
                            } else {
                                Log.d(LOG_TAG, "no more track waiting for connection.");
                                if(!allTrackConnected()) {
                                    Log.v(LOG_TAG, "Not all tracks connected ,restart scan.");
                                    scanLeDevice();
                                } else {
                                    Log.i(LOG_TAG, "all tracks connected ,nice.");
                                }
                            }
                        } else if (mConnectionState == ConnectionState.CONNECTING) {
                            Log.w(LOG_TAG, "handle mConnectionState while state is CONNECTING");
                        }
                        break;
                }
            }
        };

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(Receiver.REPEAT_BROADCAST_RECEIVER_ACTION), 0);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        mPrefsManager = PrefsManager.singleInstance(this);
        mPrefsManager.addPrefsListener(this);

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mConnectivityManager = (ConnectivityManager)  getSystemService(CONNECTIVITY_SERVICE);

        if (!mPrefsManager.validUserLog()) {
            Log.i(LOG_TAG, "user not login, stop service");
            stopSelf();
            return;
        }

        enterFastRepeatMode();
        updateRepeatAlarmRegister(true);

        goForeground();
        registerLocationListener();
        TrackRApplication app = (TrackRApplication) getApplication();
        app.setBluetootLeService(this);
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPrefsManager.removePrefsListener(this);
        mAlarmManager.cancel(mPendingIntent);

        unregisterAmapLocationListener();


        Set<Map.Entry<String, BluetoothGatt>> gatts = mBluetoothGatts.entrySet();
        for (Map.Entry<String, BluetoothGatt> entry : gatts) {
            try {
                String address = entry.getKey();
                entry.getValue().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mBluetoothGatts.clear();
        mBluetoothCallbacks.clear();

        if(mAmapLocationManagerProxy != null) {
            mAmapLocationManagerProxy.destroy();
            mAmapLocationManagerProxy = null;
        }


        startReadRssiRepeat(false, null);
        TrackRApplication app = (TrackRApplication) getApplication();
        app.setBluetootLeService(null);
        if(mIntentFilter != null) {
            unregisterReceiver(mReceiver);
        }
    }



    private void goForeground() {
        Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.track_r_is_running), System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainTrackRListActivity.class);
        PendingIntent startMainActivity = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getString(R.string.track_r_is_running),
                getString(R.string.track_r_is_running), startMainActivity);
        startForeground(ONGOING_NOTIFICATION, notification);
    }

    private void updateRepeatAlarmRegister(boolean enabled) {
        if(enabled) {
            int repeatPeriod = ALARM_REPEAT_PERIOD;
            if (inFastRepeatMode()) {
                repeatPeriod = FAST_ALARM_REPEAT_PERIOD;
            }
            mAlarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, //type
                    System.currentTimeMillis() + repeatPeriod, //trigger time
                    repeatPeriod, //intervalMillis
                    mPendingIntent //operation
            );
        } else {
            mAlarmManager.cancel(mPendingIntent);
            Log.v(LOG_TAG, "Cancel alarm repeat.");
        }

    }


    private void registerLocationListener() {

        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            if(mLocationManager == null) {
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, getMainLooper());
        } else {
            //使用高德定位API
            if(mAmapLocationManagerProxy == null) {
                mAmapLocationManagerProxy = LocationManagerProxy.getInstance(this);
            }

            mAmapLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, LOCATION_UPDATE_PERIOD_IN_MS, MIN_DISTANCE, this);
            AMapLocation loc = mAmapLocationManagerProxy.getLastKnownLocation(LocationProviderProxy.AMapNetwork);

            if (loc != null) {
                mLastLocation = LocUtils.convertAmapLocation(loc);
            }
        }

    }

    private void unregisterAmapLocationListener() {

        if (mAmapLocationManagerProxy != null) {
            mAmapLocationManagerProxy.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        Location loc = LocUtils.convertAmapLocation(amapLocation);
        handleLocationFound(loc);
    }

    private void handleLocationFound(Location loc) {

        if (loc != null) {

            if(loc.getLatitude() == 0 && loc.getLongitude() == 0) {
                Log.w(LOG_TAG, "get zero location, try again it");
                registerLocationListener();
                return;
            }
            mLastLocation = loc;
            mPrefsManager.saveLastLocation(loc);

            HashSet<String> idsNeedUpdateLocation = new HashSet<String>(mLostGpsNeedUpdateIds);
            mLostGpsNeedUpdateIds.clear();
            for(String id: idsNeedUpdateLocation) {
                Log.v(LOG_TAG, "update last loat location info ");
                mPrefsManager.saveLastLostLocation(loc, id);
                reportTrackLostPosition(id);
            }

            Log.i(LOG_TAG, "get location info,  lat is " + loc.getLatitude() + "the long is " + loc.getLongitude());
            unregisterAmapLocationListener();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            handleLocationFound(location);
        }
        Log.i("LocationManager", "get current location from System LocationManager which is " + mLastLocation);
    }





    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "onStartCommand");
        long start = SystemClock.uptimeMillis();

        if (intent != null) {
            if(mBluetoothAdapter == null) {
                Log.e(LOG_TAG, "mBluetoothAdapter is null in onStartCommand");
                return START_NOT_STICKY;
            }
            //scan activity need scan, stop background service scan
            if(ACTION_STOP_BACKGROUND_LOOP.equals(intent.getAction())) {
                stopBackgroundOperation();
            } else {
                mLastStartCommandMeet = System.currentTimeMillis();
                repeatConnectLoop();

                //intent source activity or broadcast;
                if(!intent.getBooleanExtra(INTENT_FROM_BROADCAST_EXTRA_KEY_NAME, false)) {
                    enterFastRepeatMode();
                    if(mConnectionState == ConnectionState.BLOCKING) {
                        mConnectionState = ConnectionState.IDLE;
                    }
                }
                updateRepeatAlarmRegister(true);
                registerLocationListener();
            }

            //some bind info may not upload successfully.
            //upload now;
            uploadBindInfoDelay();
        }
        Log.i(LOG_TAG,  "onStartCommand  used time:" + (SystemClock.uptimeMillis() - start) + " ms");
        return START_STICKY;
    }

    private void stopBackgroundOperation() {
        mHandler.sendEmptyMessage(MSG_STOP_BLE_SCAN);
        updateRepeatAlarmRegister(false);
        //set blocking mark to stop all background blocking;
        if(mConnectionState == ConnectionState.SCANNING) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        Log.v(LOG_TAG, "service going to block state.");
        mConnectionState = ConnectionState.BLOCKING;
    }

    private void uploadBindInfoDelay() {
        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if(mPrefsManager.getNewlyTrackIds().size() > 0
                && activeNetworkInfo != null
                && activeNetworkInfo.isConnected()) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    Log.v(LOG_TAG, "start delay track update.");
                    Set<String> newTracks = mPrefsManager.getNewlyTrackIds();
                    for(String id: newTracks) {
                        TrackR track = mPrefsManager.getTrack(id);

                        if(track != null) {
                            final BindCommand bindcommand = new BindCommand(String.valueOf(mPrefsManager.getUid()),
                                    track.name,
                                    id,
                                    String.valueOf(track.type));
                            bindcommand.setPassword(mPrefsManager.getPassword());
                            bindcommand.execTask();
                            boolean bindOk = bindcommand.success();
                            if(bindOk) {
                                Log.i(LOG_TAG, "Delay Bind mTrack ok.");
                                UpdateTrackImageCommand uploadImageCommand = new UpdateTrackImageCommand(mPrefsManager.getUid(), id);
                                uploadImageCommand.setPassword(mPrefsManager.getPassword());
                                uploadImageCommand.execTask();

                                if(uploadImageCommand.success()) {
                                    Log.v(LOG_TAG, "delay upload mTrack photo to server success.");
                                    mPrefsManager.removeNewlyTrackId(id);
                                } else {
                                    Log.e(LOG_TAG, "delay upload mTrack photo to server failed.");
                                }
                            } else {
                                Log.e(LOG_TAG, "Delay Bind iRrack on Server Error.");
                            }
                        }
                    }
                }
            };
            t.start();
        }
    }

    private boolean repeatConnectLoop() {

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(LOG_TAG, "Unable to repeatConnectLoop BluetoothManager.");
                return false;
            }
        }

        if(!mBluetoothAdapter.isEnabled()) {
            if(mConnectionState == ConnectionState.CONNECTING) {
                mConnectionState = ConnectionState.IDLE;
            }
            Log.w(LOG_TAG, "Bluetooth disable...");
            return false;
        }

        int uid = mPrefsManager.getUid();
        if (uid == PrefsManager.INVALID_UID) {
            Log.v(LOG_TAG, "User has logout, exit.");
            disconectAllTracksAndStopSelf();
            return true;
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        //scan devices
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cleanUpDeadConnection();
                scanLeDevice();
            }
        }, 2500);

        //if all device connected, exit fast repeat mode;
        if(allTrackConnected()) {
            Log.i(LOG_TAG, "all trackr connected, exit fast repeat mode");
            exitFastRepeatMode();
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                Set<String> ids = mPrefsManager.getTrackIds();
                for (String id: ids) {
                    if(mPrefsManager.isDeclaredLost(id) && mPrefsManager.getLastLocFoundByOther(id) == null) {
                        Long lastFetchTime = mDeclaredLostTrackLastFetchedTime.get(id);
                        //waint 30 minutes to next same fetch if fetch failed
                        if(lastFetchTime == null || (System.currentTimeMillis() - lastFetchTime) > 30 * 60 * 1000) {
                            fetchDeclaredLostTrackGps(id);
                        }
                    }
                }
            }
        };
        t.start();

        return true;
    }



    private boolean allTrackConnected() {
        Set<String> ids = new HashSet<String>(mPrefsManager.getTrackIds());
        boolean allConnected = true;
        for (String address : ids) {
            HashMap<String, Integer> states = (HashMap<String, Integer>) mGattConnectionStates.clone();
            Integer state = states.get(address);
            if(state == null || state != BluetoothProfile.STATE_CONNECTED) {
                allConnected = false;
                break;
            }
        }
        return allConnected;
    }


    private void disconectAllTracksAndStopSelf() {

        Set<String> ids = mPrefsManager.getTrackIds();
        for (String address : ids) {
            if (!TextUtils.isEmpty(address)) {
                mPrefsManager.saveMissedTrack(address, true);
                BluetoothGatt gatt = mBluetoothGatts.get(address);
                try {
                    if(gatt != null) {
                        gatt.close();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        stopForeground(true);
        stopSelf();

    }

    private boolean sleepTrack(String address) {
        Integer state = mGattConnectionStates.get(address);
        if(state == null) {
            Log.w(LOG_TAG, "trying to sleep an unknown state track.");
            return false;
        }
        if (state == BluetoothProfile.STATE_CONNECTED) {
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            try {
                BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);
                BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
                alertLevelChar.setValue(new byte[]{00});
                if( gatt.writeCharacteristic(alertLevelChar)) {
                    Log.v(LOG_TAG, "write sleep character ok");
                    return true;
                } else {
                    Log.e(LOG_TAG, "write sleep character failed");
                    throw new DeadObjectException();
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, "sleepTrack ", e) ;
                mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                broadcastUpdate(ACTION_GATT_DISCONNECTED, address);
                mBluetoothGatts.remove(address);
                if(gatt != null) {
                    gatt.close();
                }

                return false;
            }
        } else {
            Log.w(LOG_TAG, "can not close unconnected track r");
            return false;
        }
    }

    private boolean wakeupTrack(String address) {
        Integer state = mGattConnectionStates.get(address);
        if(state == null) {
            Log.w(LOG_TAG, "trying to sleep an unknown state track.");
            return false;
        }

        if (state == BluetoothProfile.STATE_CONNECTED) {
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            try {

                BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);
                BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
                boolean trackAlertEnabled = mPrefsManager.getTrackAlert(address);
                alertLevelChar.setValue(new byte[]{(byte) (trackAlertEnabled ? 02 : 00)});
                if(gatt.writeCharacteristic(alertLevelChar)) {
                    Log.v(LOG_TAG, "write wake character ok");
                    return true;
                } else {
                    Log.v(LOG_TAG, "write wake character failed");
                    throw new DeadObjectException();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "wakeupTrack ", e);
                broadcastUpdate(ACTION_GATT_DISCONNECTED, address);
                mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                mBluetoothGatts.remove(address);
                if(gatt != null) {
                    gatt.close();
                }
                return false;
            }
        } else {
            Log.w(LOG_TAG, "can not close unconnected track r");
        }
        return false;
    }


    private void scanLeDevice() {

        if(mBluetoothAdapter == null) {
            return;
        }

        if(mConnectionState == ConnectionState.IDLE) {
            if(!mBluetoothAdapter.startLeScan(mLeScanCallback)) {
                Log.e(LOG_TAG, "mBluetoothAdapter.startLeScan return false.");
            } else {
                Log.v(LOG_TAG, "start ble scan successfully.");
            }
            mConnectionState = ConnectionState.SCANNING;
            mHandler.sendEmptyMessageDelayed(MSG_STOP_BLE_SCAN, SCAN_TIMEOUT_MS);
        } else {
            Log.w(LOG_TAG, "in scanLeDevice mConnectionState is not idle, state is " + mConnectionState);
        }

    }


    /**
     * Update track 's sleep mode state
     * @param address
     */
    private boolean updatesSingleTrackSleepState(String address) {
        Log.i(LOG_TAG, "updateSingleTrackSleepState.... with address " + address);
        boolean sleepMode = mPrefsManager.getSleepMode();
        boolean inSleepTime = inSleepTime();
        boolean safeZone = inSafeZone();
        boolean alertEnabled = mPrefsManager.getTrackAlert(address);
        //safe zone and sleep mode in sleep time
        if(safeZone || (sleepMode && inSleepTime)) {
            sleepTrack(address);
        } else {
            if(alertEnabled) {
                wakeupTrack(address);
            } else {
                sleepTrack(address);
            }
        }
        return false;
    }


    /**
     * 向服务器请求声明丢失的防丢器位置
     */
    private void fetchDeclaredLostTrackGps(final String address) {
        Thread t = new Thread() {
            @Override
            public void run() {
                Long lastTime = mDeclaredLostTrackLastFetchedTime.get(address);
                if(lastTime == null) {
                    FetchLostLocationCommand command = new FetchLostLocationCommand(address, mPrefsManager.getUid());
                    command.setPassword(mPrefsManager.getPassword());
                    command.execTask();
                    if(command.success()) {
                        try {
                            Log.d(LOG_TAG, "fetch lost track's location success.");
                            Location loc = new Location(LocationManager.NETWORK_PROVIDER);
                            loc.setLatitude(command.getLatitude());
                            loc.setLongitude(command.getLongitude());
                            long timeFound = command.getLostTime();

                            mPrefsManager.saveLastLocFoundByOthers(loc, address);
                            mPrefsManager.saveLastTimeFoundByOthers(timeFound, address);
                            mDeclaredLostTrackLastFetchedTime.put(address, System.currentTimeMillis());
                            notifyFoundByOthers(address, loc, timeFound);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        t.start();
    }

    private void notifyFoundByOthers(String address, Location loc, long timeFound) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.good_news));
        String contentText = getString(R.string.your_track_is_found_by_others_at, Utils.convertTimeStampToLiteral(timeFound));

        builder.setContentText(contentText);
        builder.setSmallIcon(R.drawable.ic_launcher);


        // Creates an Intent for the Activity
        Intent notifyIntent =
                new Intent(this, FoundByOthersActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        notifyIntent.putExtra(FoundByOthersActivity.EXTRA_TRACK_ADDRESS, address);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(notifyPendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID_TRACK_FOUND_BY_OTHERS, builder.build());
    }

    private void tryConnectGatt(final String address, final BluetoothDevice device) {
        Log.v(LOG_TAG, "tryConnectGatt " + address);
        if(mBluetoothAdapter == null) {
            Log.w(LOG_TAG, "In tryConnectGatt mBluetoothAdapter is null");
            mAddingDeviceAddress = null;
            return;

        }

        if(!mBluetoothAdapter.isEnabled()) {
            Log.w(LOG_TAG, "In tryConnectGatt mBluetoothAdapter is disabled");
            mAddingDeviceAddress = null;
            return;
        }

        final BluetoothGatt bluetoothGatt = mBluetoothGatts.get(address);
        boolean needReconnectionDelay = false;
        try {
            if(bluetoothGatt != null) {
                Log.i(LOG_TAG, "close old connection waiting gatt." + address);
                bluetoothGatt.close();
                mBluetoothGatts.remove(address);
                needReconnectionDelay = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyBluetootGattCallback oldCallback = mBluetoothCallbacks.get(address);
                if (oldCallback == null) {
                    oldCallback = new MyBluetootGattCallback();
                }

                BluetoothGatt newGatt = Utils.connectBluetoothGatt(device, BluetoothLeService.this, oldCallback);

                if (newGatt != null) {
                    mBluetoothCallbacks.put(address, oldCallback);
                    mBluetoothGatts.put(address, newGatt);
                } else {
                    Log.e(LOG_TAG, "Device connectGatt return null.");
                    mBluetoothCallbacks.put(address, oldCallback);
                }
            }
        }, needReconnectionDelay ? 1000 : 0);
    }



    public int getGattConnectState(String address) {
        Integer state = mGattConnectionStates.get(address);
        return state == null ? BluetoothProfile.STATE_DISCONNECTED : state;
    }


    public boolean isGattConnected(String address) {
        Integer state = mGattConnectionStates.get(address);
        return state == null ? false : state == BluetoothProfile.STATE_CONNECTED;
    }

    public HashMap<String, BluetoothGatt> getBluetoothGatts() {
        return mBluetoothGatts;
    }

    public void turnOffTrackR(String address) {
        Log.i(LOG_TAG, "turnOffTrackR");
        Integer state = mGattConnectionStates.get(address);

        //turn off connected trackr;
        if (state != null && state == BluetoothProfile.STATE_CONNECTED) {
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);
            BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
            alertLevelChar.setValue(new byte[]{03});
            if(gatt.writeCharacteristic(alertLevelChar)) {
                mPrefsManager.saveClosedTrack(address, true);
                Log.d(LOG_TAG, "In method turnOffTrackR, send sleep command ok.");
            }  else {
                Log.e(LOG_TAG, "In method turnOffTrackR, send sleep command failed.");
            };
            //disconnected gatt can be reused later ;
        } else {
            Toast.makeText(this, getString(R.string.can_not_close_disconnected_itrack), Toast.LENGTH_SHORT).show();
            mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
            Log.i(LOG_TAG, "turn off disconnected Track");
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            if(gatt != null) {
                gatt.close();
            }
            broadcastDeviceOff();
        }
    }


    public void unbindTrackR(final String address) {
        Integer state = mGattConnectionStates.get(address);

        if (state != null && state == BluetoothProfile.STATE_CONNECTED) {
            try {
                mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                BluetoothGatt gatt = mBluetoothGatts.get(address);
                BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);
                BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);

                alertLevelChar.setValue(new byte[]{03});

                if(!gatt.writeCharacteristic(alertLevelChar)) {
                    Log.e(LOG_TAG, "unbindTrackR, write sleep command failed, forcely close the connection");
                    mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                    gatt.close();
                    broadcastDeviceOff();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i(LOG_TAG, "unbind disconnected TrackR");
            mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
            broadcastDeviceOff();
        }

        mPrefsManager.removeTrackId(address);
        mPrefsManager.saveMissedTrack(address, false);
        mPrefsManager.saveClosedTrack(address, false);
        mPrefsManager.saveDeclareLost(address, false);
        mPrefsManager.saveLastTimeFoundByOthers(-1, address);
        mPrefsManager.saveLastLostLocation(null, address);
        mPrefsManager.removeTrackImageAndInfo(address);

        broadcastDeviceUbbind(address);

    }

    private void broadcastDeviceUbbind(String address) {
        broadcastUpdate(ACTION_DEVICE_UNBIND, address);
    }


    public boolean ringTrackR(String address) {
        Integer state = mGattConnectionStates.get(address);
        Log.v(LOG_TAG, "ringTrackR() gatt connection state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(address);

        if (gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
            return false;
        }

        if (state != BluetoothProfile.STATE_CONNECTED) {
            Log.w(LOG_TAG, "ring trackr whose state is not connected, bail out");
            return false;
        }


        BluetoothGattService alertService = gatt.getService(com.antilost.app.bluetooth.UUID.IMMEDIATE_ALERT_SERVICE_UUID);
        if (alertService == null) {
            Log.w(LOG_TAG, "silentRing No IMMEDIATE_ALERT_SERVICE_UUID_STRING....");
            return false;
        }

        BluetoothGattCharacteristic c = alertService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
        if (c == null) {
            Log.e(LOG_TAG, "ring characteristic is null");
            return false;
        }
        //{0x02}; //高音 {0x01}; 低音
        c.setValue(new byte[]{02});

        if(gatt.writeCharacteristic(c)) {
            Log.v(LOG_TAG, "write ring characteristic ok");
            return true;
        } else {
            Log.v(LOG_TAG, "write ring characteristic failed");
            gatt.close();
            mBluetoothCallbacks.remove(address);
            mBluetoothGatts.remove(address);
            mGattConnectionStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
        }
        return false;
    }

    public boolean silentRing(String bluetoothDeviceAddress) {
        Integer state = mGattConnectionStates.get(bluetoothDeviceAddress);
        Log.v(LOG_TAG, "silentRing() gatt connection state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if (gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
            return false;
        }
        BluetoothGattService alertService = gatt.getService(com.antilost.app.bluetooth.UUID.IMMEDIATE_ALERT_SERVICE_UUID);
        if (alertService == null) {
            Log.w(LOG_TAG, "No IMMEDIATE_ALERT_SERVICE_UUID_STRING....");
            return false;
        }

       // {00} 静音，{0x02}; //高音 {0x01}; 低音
        BluetoothGattCharacteristic c = alertService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
        c.setValue(new byte[]{00});
        return gatt.writeCharacteristic(c);
    }

    public void setTrackAlertMode(String bluetoothDeviceAddress, boolean enable) {
        updatesSingleTrackSleepState(bluetoothDeviceAddress);
    }

    public void readBatteryLevel(String bluetoothDeviceAddress) {
        Integer state = mGattConnectionStates.get(bluetoothDeviceAddress);
        if (state != null && state != BluetoothProfile.STATE_CONNECTED) {
            Log.w(LOG_TAG, "Read a unconnected device battery level.");
            return;
        }

        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if (gatt == null) {
            Log.w(LOG_TAG, "Gatt has not connected....");
        } else {
            try {
                BluetoothGattService batteryService = gatt.getService(com.antilost.app.bluetooth.UUID.BATTERY_SERVICE_UUID);
                BluetoothGattCharacteristic c = batteryService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_BATTERY_LEVEL_UUID);
                gatt.readCharacteristic(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int requestRssiLevel(String bluetoothDeviceAddress) {
        Integer state = mGattConnectionStates.get(bluetoothDeviceAddress);
        Log.d(LOG_TAG, "readRssLevel state is " + state);

        Integer rssiValue = mGattsRssis.get(bluetoothDeviceAddress);

        if (rssiValue == null) {
            rssiValue = TrackRActivity.MAX_RSSI_LEVEL;
        }

        if (state == null) {
            state = BluetoothProfile.STATE_DISCONNECTED;
        }
        if (state != null && state != BluetoothProfile.STATE_CONNECTED) {
            Log.w(LOG_TAG, "read a unconnected device rssi level.");
            return rssiValue;
        }
        if (state == BluetoothProfile.STATE_CONNECTED) {
            BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);
            if (gatt != null) {
                if(gatt.readRemoteRssi()) {
                    gatt.readRemoteRssi();
                    return rssiValue;
                }
            }
        }
        return rssiValue;
    }

    public int getRssiLevel(String address) {
        Integer rssi = mGattsRssis.get(address);
        return rssi == null ? TrackRActivity.MAX_RSSI_LEVEL : rssi;
    }

    public int getBatteryLevel(String address) {
        Integer level = mGattsBatteryLevel.get(address);
        return level == null ? 0 : level;
    }

    public int startReadRssiRepeat(boolean enabled, String address) {
        Integer rssi = mGattsRssis.get(address);
        if (rssi == null) {
            rssi = TrackRActivity.MAX_RSSI_LEVEL;
        }


        if (enabled) {
            mHandler.removeMessages(MSG_LOOP_READ_RSSI);
            Message msg = mHandler.obtainMessage(MSG_LOOP_READ_RSSI, address);
            mHandler.sendMessage(msg);
        } else {
            mHandler.removeMessages(MSG_LOOP_READ_RSSI);
        }
        return rssi;
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }


    private String mAddingDeviceAddress;

    public String getAddingDeviceAddress() {
        return mAddingDeviceAddress;
    }
    private BluetoothAdapter.LeScanCallback mScanForAddCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            if(rssi > ScanTrackActivity.MIN_RSSI_ACCEPTABLE) {
                String name = bluetoothDevice.getName();
                if(!Utils.DEVICE_NAME.equals(name)
                        && !Utils.DEVICE_NAME_NEW.equals(name)) {
                    Log.i(LOG_TAG, "onLeScan for add, with unknown name " + name);
                    return;
                }
                String address = bluetoothDevice.getAddress();
                if(mPrefsManager.getTrackIds().contains(address)) {
                    return;
                } else {
                    mBluetoothAdapter.stopLeScan(mScanForAddCallback);

                    if(mAddingDeviceAddress != null) {
                        Log.v(LOG_TAG, "Already adding a track.");
                        mBluetoothAdapter.stopLeScan(mScanForAddCallback);
                        return;
                    }
                    mAddingDeviceAddress = address;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tryConnectGatt(mAddingDeviceAddress, bluetoothDevice);
                        }
                    }, 1000);
                }
            }
        }
    };

    /**
     * if listner is null mean unregister listener ,do not scan for add
     * @param listener
     */

    public void startBleScanForAdd(ScanResultListener listener) {
        mScanResultListener = listener;
        mAddingDeviceAddress = null;
        if(listener != null) {
            stopBackgroundOperation();
            if(mBluetoothAdapter == null) {

            } else {
                mBluetoothAdapter.startLeScan(mScanForAddCallback);
            }
        }
    }


    private void notifyScanSuccess() {
        if(mScanResultListener != null) {
            mScanResultListener.onSuccess();
        } else {
            Log.e(LOG_TAG, "mScanResultListener is null in notifyScanSuccess()");
        }
    }

    public void giveUpConnectNewTrack() {

        BluetoothGatt gatt = mBluetoothGatts.get(mAddingDeviceAddress);
        if(gatt != null) {
            gatt.close();
        }

        mAddingDeviceAddress = null;
        mScanResultListener = null;

    }

    public void clearAfterAddSuccess() {
        mAddingDeviceAddress = null;
        mScanResultListener = null;
    }

    public void requestTrackHardwareVersion(String address) {
        Integer state = mGattConnectionStates.get(address);
        if(state != null && state == BluetoothProfile.STATE_CONNECTED) {
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            BluetoothGattService service = gatt.getService(com.antilost.app.bluetooth.UUID.CUSTOM_SERVICE);
            if(service != null) {
                BluetoothGattCharacteristic hardWareVersionCharacteristic = service.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_HARDWARE_VERSION);
                if(hardWareVersionCharacteristic != null) {
                    gatt.readCharacteristic(hardWareVersionCharacteristic);
                } else {
                    Log.e(LOG_TAG, "read hareware version characteristic is null in requestTrackHardwareVersion");
                }
            } else {
                Log.e(LOG_TAG, "custom service is null in requestTrackHardwareVersion");
            }
        }
    }

}
