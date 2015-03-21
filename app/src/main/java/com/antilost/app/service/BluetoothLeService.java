package com.antilost.app.service;

import android.app.AlarmManager;
import android.app.Notification;
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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.antilost.app.BuildConfig;
import com.antilost.app.R;
import com.antilost.app.activity.DisconnectAlertActivity;
import com.antilost.app.activity.FindmeActivity;
import com.antilost.app.activity.MainTrackRListActivity;
import com.antilost.app.activity.TrackRActivity;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.receiver.Receiver;
import com.antilost.app.util.LocUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
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


    private static final int MSG_CLEANUP_DISCONNECTED_GATT = 2;
    private static final int MSG_LOOP_READ_RSSI = 3;
    private static final int MSG_FAST_REPEAT_MODE = 4;

    public static final int ALARM_REPEAT_PERIOD =  2 * 60 * 1000;
    public static final int FAST_ALARM_REPEAT_PERIOD =  10 * 1000;

    private static final int SCAN_PERIOD_OF_RSSI_READ = 30 * 1000;
    public static final int LOCATION_UPDATE_PERIOD_IN_MS = 20 * 1000;

    public static final int TIME_TO_KEEP_FAST_ALARM_REPEAT_MODE = 30 * 1000;

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

    public final static String ACTION_DEVICE_FAR_AWAY =
            "com.antilost.bluetooth.le.ACTION_DEVICE_FAR_AWAY";

    public final static String ACTION_DEVICE_CLICKED =
            "com.antilost.bluetooth.le.ACTION_DEVICE_CLICKED";
    public static final int MIN_DISTANCE = 20;


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private HashMap<String, BluetoothGatt> mBluetoothGatts = new HashMap<String, BluetoothGatt>();
    private HashMap<String, MyBluetootGattCallback> mBluetoothCallbacks = new HashMap<String, MyBluetootGattCallback>();
    private HashMap<String, Integer> mGattStates = new HashMap<String, Integer>();

    private HashMap<String, Integer> mGattsRssis = new HashMap<String, Integer>();
    private HashMap<String, Integer> mGattsBatteryLevel = new HashMap<String, Integer>();


//    private LocationManager mLocationManager;
    private Location mLastLocation;
    private WifiManager mWifiManager;
    private PendingIntent mPendingIntent;
    private LocationManagerProxy mAmapLocationManagerProxy;
    private long mLastStartCommandMeet;

//    private LocationClient mBaiduLocationClient;


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(LOG_TAG, "BluetoothLeService onSharedPreferenceChanged() key " + key);
        if (PrefsManager.PREFS_TRACK_IDS_KEY.equals(key)) {
            Log.v(LOG_TAG, "key of preference changed is " + PrefsManager.PREFS_TRACK_IDS_KEY);
            initialize();
        }

        if(PrefsManager.PREFS_SLEEP_MODE_KEY.equals(key)) {
            Log.v(LOG_TAG, "on sleep mode change");
           onSleepModeChang();
        }
    }

    private void onSleepModeChang() {
        boolean sleepMode = mPrefsManager.getSleepMode();

        if(sleepMode) {
            if(inSleepTime()) {
                sleepAllTrackR();
            } else {
                wakeAllTrackR();
            }
        } else {
            wakeAllTrackR();
        }
    }

    private void sleepAllTrackR() {
        Set<String> addresses = mBluetoothGatts.keySet();
        for(String address: addresses) {

        }
    }

    private void wakeAllTrackR() {
        Set<String> addresses = mBluetoothGatts.keySet();
        for(String address: addresses) {

        }
    }

    private boolean inSleepTime() {
        boolean sleepMode = mPrefsManager.getSleepMode();

        if(!sleepMode) {
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
        //cross the midnight
        if(startTime > endTime) {
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
                    Log.v(LOG_TAG, "BluetoothAdapter.LeScanCallback get device " + device.getAddress());

                    Set<String> ids = mPrefsManager.getTrackIds();
                    String deviceAddress = device.getAddress();
                    if (ids.contains(deviceAddress)) {
                        Log.v(LOG_TAG, "In LeScanCallback , reconnect to " + deviceAddress);
                        reconnectMissingTrack(deviceAddress);
                        return;
                    }
                }
            };

    private void reconnectMissingTrack(String address) {
        mPrefsManager.saveMissedTrack(address, false);
        connect(address);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            mLastLocation =  location;
        }
        Log.i("LocationManager", "get current location from System LocationManager which is " + mLastLocation);
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

    public void tryConnect() {
        initialize();
    }

    public Location getLastLocation() {
        return mLastLocation;
    }


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.

    private class MyBluetootGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(status != BluetoothGatt.GATT_SUCCESS) {
                Log.v(LOG_TAG, "gatt status is not success." );
//                gatt.disconnect();
//                gatt.close();
//                mBluetoothGatts.remove(gatt.getDevice().getAddress());
                return;
            }
            String address = gatt.getDevice().getAddress();
            Integer oldState = mGattStates.put(address, newState);
            if(oldState == null) {
                oldState = BluetoothProfile.STATE_DISCONNECTED;
            }
            mBluetoothGatts.put(address, gatt);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i(LOG_TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(LOG_TAG, "Attempting to start service discovery:" + gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(LOG_TAG, "Disconnected from GATT server.");
                //if user manually close track r connection;
                //mGattStates will move that address in function onCharacteristicWrite
                if(mGattStates.containsKey(address))  {
                    broadcastUpdate(intentAction);
                }

                if(oldState == BluetoothProfile.STATE_CONNECTED) {

                    String homeWifiSsid = mPrefsManager.getHomeWifiSsid();
                    String officeSsid = mPrefsManager.getOfficeSsid();
                    String otherSsid = mPrefsManager.getOtherSsid();
                    mPrefsManager.saveMissedTrack(address, true);
                    if(mLastLocation != null) {
                        mPrefsManager.saveLastLostLocation(mLastLocation, address);
                    }

                    mPrefsManager.saveLastLostTime(address);

                    WifiInfo info = mWifiManager.getConnectionInfo();
                    String ssid = info.getSSID();
                    boolean safeWifiEnabled = mPrefsManager.getSafeZoneEnable();
                    if(safeWifiEnabled &&
                            ssid != null &&(
                            ssid.equals(homeWifiSsid)
                            || ssid.equals(officeSsid)
                            || ssid.equals(otherSsid)
                            )) {
                        Log.i(LOG_TAG, "we are in safe wifi zone, don't alert user");
                        return;
                    }
                    Log.v(LOG_TAG, "found disconnected device.");
                    if(gatt != null) {
                        gatt.disconnect();
                        gatt.close();
                        mBluetoothGatts.remove(address);
                    }

                    alertUserTrackDisconnected(address);
                    enterFastRepeatMode();
                    sendBroadcast(new Intent(Receiver.REPEAT_BROADCAST_RECEIVER_ACTION));
                } else {
                    Log.w(LOG_TAG, "onConnectionStateChange get disconnected state and old state is not connected.");
                }

            }
            //even device is power off the newState can be STATE_CONNECTEDe,
            //we think device is connected after onServicesDiscovered is called();
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                mGattStates.put(address, BluetoothProfile.STATE_CONNECTING);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(LOG_TAG, "onServicesDiscovered ....");
            String address = gatt.getDevice().getAddress();
            mGattStates.put(address, BluetoothProfile.STATE_CONNECTED);
            mBluetoothGatts.put(gatt.getDevice().getAddress(), gatt);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                //registry key press notification;
                if(setCharacteristicNotification(gatt,
                        UUID.fromString(com.antilost.app.bluetooth.UUID.SIMPLE_KEY_SERVICE_UUID),
                        com.antilost.app.bluetooth.UUID.CHARACTERISTIC_KEY_PRESS_UUID,
                        true)) {
                    Log.v(LOG_TAG, "setCharacteristicNotification ok");
                }

                //bidirectional lost alert
                if(mPrefsManager.getTrackAlert(address)) {
                    Log.d(LOG_TAG, "enable bidirectional alert...");
                    twowayMonitor(address, true);
                }


                readBatteryLevel(address);
                requestRssiLevel(address);

                String intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
            } else {
                Log.w(LOG_TAG, "onServicesDiscovered status is not success.: " + status);
            }
        }

        public boolean setCharacteristicNotification(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid,
                                                     boolean enable) {
            try {
                if (BuildConfig.DEBUG)
                    Log.d(LOG_TAG, "setCharacteristicNotification(device=" + "  UUID="
                            + characteristicUuid + ", enable=" + enable + " )");

                BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);
                gatt.setCharacteristicNotification(characteristic, enable);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
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
            if (status == BluetoothGatt.GATT_SUCCESS) {

                UUID cId = characteristic.getUuid();
                UUID sId = characteristic.getService().getUuid();
                if(sId.equals(com.antilost.app.bluetooth.UUID.BATTERY_SERVICE_UUID)
                        && cId.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_BATTERY_LEVEL_UUID)) {
                    int level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.d(LOG_TAG, "onCharacteristicRead callback battery is " + level);
                    mGattsBatteryLevel.put(gatt.getDevice().getAddress(), level);
                    broadcastBatteryLevel(level);
                } else {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            UUID charUuid = characteristic.getUuid();
            if(charUuid.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_KEY_PRESS_UUID)) {
                byte[] data = characteristic.getValue();
                int key = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                //key down is 0 and key up is 2;
                Log.v(LOG_TAG, "onCharacteristicChanged key value is " + key);
                if(key == 2) {
                    onTrackKeyLongPress();
                } else if(key == 1) {
                    onTrackKeyClick();
                }
            } else {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }

        }

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            UUID serviceUuid = characteristic.getService().getUuid();
            Log.v(LOG_TAG, "onCharacteristicWrite serviceUuid is " + serviceUuid);
            UUID charUuid = characteristic.getUuid();
            Log.i(LOG_TAG, "onCharacteristicWrite is " + charUuid);
            int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            Log.d(LOG_TAG, "onCharacteristicWrite value is " + value);

            //turn off track r command;
            if(serviceUuid.equals(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID)
                    && charUuid.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID)
                    && value == 3) {
                Log.w(LOG_TAG, "disconnect in onCharacteristicWrite ");
                String address = gatt.getDevice().getAddress();
                mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                gatt.disconnect();
                gatt.close();
                mBluetoothGatts.remove(address);
                broadcastDeviceOff();
            }

        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
        }

        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.i(LOG_TAG, "onReadRemoteRssi callback rssi is " + rssi);
            mGattsRssis.put(gatt.getDevice().getAddress(), rssi);
            broadcastRssiRead();
            String address = gatt.getDevice().getAddress();
            receiverRssi(address, rssi);
        }
    }

    private void enterFastRepeatMode() {
        mHandler.sendEmptyMessageDelayed(MSG_FAST_REPEAT_MODE, TIME_TO_KEEP_FAST_ALARM_REPEAT_MODE);
    }

    private void receiverRssi(String address, int rssi) {
        Log.v(LOG_TAG, String.format("address %s's status is %d", address, rssi));

//        if(rssi < -80) {
//            alertUserTrackDisconnected(address);
//        }

    }
//
//    private BDLocationListener mBaidLocationListener = new BDLocationListener() {
//        @Override
//        public void onReceiveLocation(BDLocation bdLocation) {
//            if(bdLocation != null) {
//                int type = bdLocation.getLocType();
//                Log.i(LOG_TAG, "baidu location return code is " + type);
//                if(type == 61
//                        || type == 65
//                        || type == 68
//                        || type == 161) {
//                    Location loc = LocUtils.convertBaiduLocation(bdLocation);
//                    if(loc != null) {
//                        mLastLocation = loc;
//                    }
//                }
//            }
//        }
//    };


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

        if(mPrefsManager.getPhoneAlert(address)) {
            Log.v(LOG_TAG, "alertUserTrackDisconnected() " + address);
            Intent i = new Intent(this, DisconnectAlertActivity.class);
            i.putExtra(DisconnectAlertActivity.EXTRA_KEY_DEVICE_ADDRESS, address);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else {
            Log.i(LOG_TAG, "User turn off phone alert, ignore disconnection event!");
        }

    }


    private Handler mHandler;
    private void onTrackKeyLongPress() {
        Log.v(LOG_TAG, "onTrackKeyLongPress...");
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
        if(!ringtone.isPlaying()) {
            ringtone.play();
        }

        Intent i = new Intent(this, FindmeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void onTrackKeyClick() {
        Log.v(LOG_TAG, "onTrackKeyClick...");
        broadcastUpdate(ACTION_DEVICE_CLICKED);
    }


    private AlarmManager mAlarmManager;
    private PrefsManager mPrefsManager;


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        UUID charUuid = characteristic.getUuid();

        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }



    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //connect devices when activity connect
        enterFastRepeatMode();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        });

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
//        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CLEANUP_DISCONNECTED_GATT:
                        removeMessages(MSG_CLEANUP_DISCONNECTED_GATT);
                        Log.d(LOG_TAG, "handling MSG_CLEANUP_DISCONNECTED_GATT");
                        Set<Map.Entry<String, Integer>> entrys = mGattStates.entrySet();
                        for(Map.Entry<String, Integer> entry: entrys) {
                            String address = entry.getKey();
                            Integer state = entry.getValue();
                            if (state == null) {
                                Log.w(LOG_TAG, "Handler vlean up device whose state is null." + address);
                                BluetoothGatt gatt = mBluetoothGatts.get(address);
                                if (gatt != null) {
                                    Log.e(LOG_TAG, "code logic error, state is null but BluetoothGatt is empty.");
                                    gatt.disconnect();
                                    gatt.close();
                                    mBluetoothGatts.remove(address);
                                }
                                mPrefsManager.saveMissedTrack(address, true);
                                mBluetoothGatts.remove(address);

                            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                                Log.w(LOG_TAG, "Handler clean up state STATE_DISCONNECTED device " + address);
                                BluetoothGatt gatt = mBluetoothGatts.get(address);
                                if (gatt != null) {
                                    gatt.disconnect();
                                    gatt.close();
                                    Log.i(LOG_TAG, "disconnect and close gatt.");
                                }
                                mPrefsManager.saveMissedTrack(address, true);
                                mBluetoothGatts.remove(address);
                            }
                        }
                        break;
                    case MSG_LOOP_READ_RSSI:
                        String address = (String) msg.obj;
                        Log.v(LOG_TAG, "will read rssi, whose address is " + address);
                        Integer state = mGattStates.get(address);
                        if(state != null && state == BluetoothProfile.STATE_CONNECTED) {
                            Log.v(LOG_TAG, "readBatteryLevel()");
                            requestRssiLevel(address);
                        } else {
                            Log.w(LOG_TAG, "can not read rssi of disconnected device. state is " + state);
                        }
                        mHandler.removeMessages(MSG_LOOP_READ_RSSI);
                        msg = mHandler.obtainMessage(MSG_LOOP_READ_RSSI, address);
                        mHandler.sendMessageDelayed(msg, SCAN_PERIOD_OF_RSSI_READ);
                        break;
                }
            }
        };

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(Receiver.REPEAT_BROADCAST_RECEIVER_ACTION), 0);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);



        mPrefsManager = PrefsManager.singleInstance(this);
        mPrefsManager.addPrefsListener(this);

        if(!mPrefsManager.validUserLog()) {
            Log.i(LOG_TAG, "user not login, stop service");
            stopSelf();
            return;
        }

        if(!mPrefsManager.hasTrack()) {
            Log.i(LOG_TAG, "no track, stop service");
            stopSelf();
            return;
        }
        updateRepeatAlarmRegister();

        Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.track_r_is_running), System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainTrackRListActivity.class);
        mPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getString(R.string.track_r_is_running),
                getString(R.string.track_r_is_running), mPendingIntent);
        startForeground(ONGOING_NOTIFICATION, notification);


//        mLocationManager =        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//        List<String> allProviders = mLocationManager.getAllProviders();
//        try {
//            if(allProviders.contains(LocationManager.GPS_PROVIDER)) {
//                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_PERIOD_IN_MS, MIN_DISTANCE, this);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            if(allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
//                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_PERIOD_IN_MS, MIN_DISTANCE, this);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //增加高德定位API
        mAmapLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mAmapLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, LOCATION_UPDATE_PERIOD_IN_MS, MIN_DISTANCE, this);

        AMapLocation loc = mAmapLocationManagerProxy.getLastKnownLocation(LocationProviderProxy.AMapNetwork);

        if(loc != null) {
           mLastLocation =  LocUtils.convertAmapLocation(loc);
        }


    }

    private void updateRepeatAlarmRegister() {
        int repeatPeriod = ALARM_REPEAT_PERIOD;
        if(mHandler.hasMessages(MSG_FAST_REPEAT_MODE)) {
            repeatPeriod = FAST_ALARM_REPEAT_PERIOD;
        }
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + repeatPeriod, repeatPeriod, mPendingIntent);

        Log.d(LOG_TAG, "updateRepeatAlarmRegister ... repeat period is " + repeatPeriod);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        Location loc = LocUtils.convertAmapLocation(amapLocation);
        if(loc != null) {
            mLastLocation = loc;
            mPrefsManager.saveLastAMPALocation(loc);
            Log.i(LOG_TAG, "get location info from amap location,  lat is "+loc.getLatitude()+ "the long is "+ loc.getLongitude());
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPrefsManager.removePrefsListener(this);

        Set<Map.Entry<String, BluetoothGatt>> gatts = mBluetoothGatts.entrySet();
        for (Map.Entry<String, BluetoothGatt> entry : gatts) {
            String address = entry.getKey();
            entry.getValue().disconnect();
            mBluetoothGatts.remove(address);
        }

//        mBaiduLocationClient.stop();
//        mBaiduLocationClient.unRegisterLocationListener(mBaidLocationListener);


//        mLocationManager.removeUpdates(this);
        if(mAmapLocationManagerProxy != null) {
            mAmapLocationManagerProxy.removeUpdates(this);
            mAmapLocationManagerProxy.destroy();
            mAmapLocationManagerProxy = null;
        }
        mAlarmManager.cancel(mPendingIntent);
        startReadRssiRepeat(false, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "onStartCommand");
        if(intent != null) {
            mLastStartCommandMeet = System.currentTimeMillis();
            initialize();
        }
        return START_STICKY;
    }

    private boolean initialize() {

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(LOG_TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
//        int requestResult = mBaiduLocationClient.requestLocation();
//        Log.i(LOG_TAG, "send request Result code is " + requestResult);

        int uid = mPrefsManager.getUid();
        Log.v(LOG_TAG, "current uid is " + uid);
        if(uid == -1) {
            Log.v(LOG_TAG, "user has logout, exit.");
            cleanupAndExit();
            return true;
        }

        boolean sleepMode = mPrefsManager.getSleepMode();

        if(sleepMode) {
            Log.i(LOG_TAG, "User turn on sleep mode  check time is in sleep range?");
            long startTime = mPrefsManager.getSleepTime(true);
            long endTime = mPrefsManager.getSleepTime(false);

            GregorianCalendar now = new GregorianCalendar();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            int second = now.get(Calendar.SECOND);
            int msNow = now.get(Calendar.MILLISECOND);
            msNow += (second + minute * 60  + hour * 60 * 60) * 1000;


            if(msNow < endTime || msNow > startTime) {
                Log.i(LOG_TAG, "current time in sleep mode");
                Set<String> ids = mPrefsManager.getTrackIds();
                for(String id: ids) {
                    Integer state = mGattStates.get(id);
                    if(state != null && state == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(LOG_TAG, "turnoff trackr in sleep mode whose id is " + id);
                        silentlyTurnOffTrack(id);
                    }
                }
            }
            return true;
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        Set<String> ids = mPrefsManager.getTrackIds();
        for (String address : ids) {
            connect(address);
        }
        return true;


    }

    private void cleanupAndExit() {

        Set<Map.Entry<String, BluetoothGatt>> entrys = mBluetoothGatts.entrySet();
        for(Map.Entry<String, BluetoothGatt> e : entrys) {
            String address = e.getKey();
            if(!TextUtils.isEmpty(address)) {
                silentlyTurnOffTrack(address);
            }
        }

        stopForeground(true);
        stopSelf();

    }

    private void silentlyTurnOffTrack(String address) {
        Integer state = mGattStates.get(address);

        if (state == null) {
            Log.w(LOG_TAG, "trying to turn off an unknown state track.");
            return;
        }
        if (state == BluetoothProfile.STATE_CONNECTED) {
            mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);
            BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
            alertLevelChar.setValue(new byte[]{03});
            gatt.writeCharacteristic(alertLevelChar);
        } else {
            Log.w(LOG_TAG, "can not close unconnected track r");
            return;
        }
        mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
    }

    private void scanLeDevice() {
        // Stops scanning after a pre-defined scan period.
        Log.v(LOG_TAG, "scanLeDevice");
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "auto delay stop scan after scanLeDevice");
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mHandler.sendEmptyMessage(MSG_CLEANUP_DISCONNECTED_GATT);
            }
        }, 10 * 1000);
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    private boolean connect(final String address) {
        Log.d(LOG_TAG, "connect().. connecting to " + address);
        if (mBluetoothAdapter == null || address == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if(mPrefsManager.isClosedTrack(address)) {
            Log.d(LOG_TAG, "don't connected to closed trackr");
            return false;
        }

        if(mPrefsManager.isMissedTrack(address)) {
            mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
            //mPrefsManager.saveMissedTrack(address, false);
            Log.w(LOG_TAG, "trying to scan  a disconnected track");
            scanLeDevice();
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(LOG_TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.

        BluetoothGatt bluetoothGatt = mBluetoothGatts.get(address);

        Integer oldState = mGattStates.get(address);
        if (bluetoothGatt != null
                && oldState != null
                && oldState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(LOG_TAG, "Device already connected, just bail out.");
            return true;
        }


        //already send an connect request;
        if(bluetoothGatt != null) {
            Log.v(LOG_TAG, "Disconnect old gatt state is not connected.");
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            mGattStates.remove(address);
            mBluetoothGatts.remove(address);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.w(LOG_TAG, "retry connect a long waiting trackr");
                    connect(address);
                }
            });
            return false;
        }

        //
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MyBluetootGattCallback oldCallback = mBluetoothCallbacks.get(address);
                BluetoothGatt bluetoothGatt;
                if(oldCallback == null) {
                    Log.i(LOG_TAG, "Trying to create a new callback to " + address);
                    oldCallback  = new MyBluetootGattCallback();
                    bluetoothGatt = device.connectGatt(BluetoothLeService.this, false, oldCallback);

                } else {
                    Log.v(LOG_TAG, "Use old callback to connect gatt");
                    bluetoothGatt = device.connectGatt(BluetoothLeService.this, false, oldCallback);
                }


                if(bluetoothGatt != null) {
                    mBluetoothCallbacks.put(address, oldCallback);
                } else {
                    Log.e(LOG_TAG, "Device connectGatt return null.");
                }

                mBluetoothGatts.put(address, bluetoothGatt);
                mGattStates.put(address, BluetoothProfile.STATE_CONNECTING);
            }
        });
        return true;
    }


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(String address, BluetoothGattCharacteristic characteristic) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        gatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

    }


    public int getGattConnectState(String address) {
        Integer state = mGattStates.get(address);
        return state == null ? BluetoothProfile.STATE_DISCONNECTED : state;
    }


    public boolean isGattConnected(String address) {
        Integer state = mGattStates.get(address);
        return state == null ? false : state == BluetoothProfile.STATE_CONNECTED;
    }

    public HashMap<String, BluetoothGatt> getBluetoothGatts() {
        return mBluetoothGatts;
    }

    public void turnOffTrackR(String address) {
        Integer state = mGattStates.get(address);
        //turn off connected trackr;
        if(state != null && state == BluetoothProfile.STATE_CONNECTED) {
            mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);
            BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
            alertLevelChar.setValue(new byte[]{03});
            gatt.writeCharacteristic(alertLevelChar);
        } else {
            Log.i(LOG_TAG, "turnoff disconnected TrackR");
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
            if(gatt != null) {
                gatt.disconnect();
                gatt.close();
            }
            mBluetoothGatts.remove(address);
            broadcastDeviceOff();
        }
        mPrefsManager.saveClosedTrack(address, true);
        mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
    }


    public void unbindTrackR(final String address) {
        Integer state = mGattStates.get(address);
        //turn off connected trackr;
        if(state != null && state == BluetoothProfile.STATE_CONNECTED) {
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);
            BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
            alertLevelChar.setValue(new byte[] {03});
            gatt.writeCharacteristic(alertLevelChar);
        } else {
            Log.i(LOG_TAG, "unbind disconnected TrackR");
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
            if(gatt != null) {
                gatt.disconnect();
                gatt.close();
            }
            mBluetoothGatts.remove(address);
            broadcastDeviceOff();
        }
        mPrefsManager.saveMissedTrack(address, false);
        mPrefsManager.removeTrackId(address);

        mGattStates.remove(address);
        mBluetoothGatts.remove(address);

    }


    public void ringTrackR(String bluetoothDeviceAddress) {
        Integer state = mGattStates.get(bluetoothDeviceAddress);
        Log.v(LOG_TAG, "ringTrackR() gatt connection state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if (gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
            return;
        }

        if(state != BluetoothProfile.STATE_CONNECTED) {
            Log.w(LOG_TAG, "ring trackr whose state is not connected, bail out");
            return;
        }


        BluetoothGattService alertService = gatt.getService(UUID.fromString(com.antilost.app.bluetooth.UUID.IMMEDIATE_ALERT_SERVICE_UUID));
        if (alertService == null) {
            Log.w(LOG_TAG, "silentRing No IMMEDIATE_ALERT_SERVICE_UUID....");
            return;
        }
//        List<BluetoothGattCharacteristic> characteristics = alertService.getCharacteristics();
//
//        for (BluetoothGattCharacteristic c : characteristics) {
//            if (c.getUuid().toString().startsWith(com.antilost.app.bluetooth.UUID.ALARM_CHARACTERISTIC_UUID_PREFIX)) {
//                byte[] sendData = new byte[]{0x02}; //高音 {0x01}; 低音
//                c.setValue(sendData);
//                gatt.writeCharacteristic(c);
//            }
//        }
        BluetoothGattCharacteristic c = alertService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
        if(c == null) {
            return;
        }
        c.setValue(new byte[] {02});
        gatt.writeCharacteristic(c);
    }

    public void silentRing(String bluetoothDeviceAddress) {
        Integer state = mGattStates.get(bluetoothDeviceAddress);
        Log.v(LOG_TAG, "silentRing() gatt connection state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if (gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
            return;
        }
        BluetoothGattService alertService = gatt.getService(UUID.fromString(com.antilost.app.bluetooth.UUID.IMMEDIATE_ALERT_SERVICE_UUID));
        if (alertService == null) {
            Log.w(LOG_TAG, "No IMMEDIATE_ALERT_SERVICE_UUID....");
            return;
        }
//        List<BluetoothGattCharacteristic> characteristics = alertService.getCharacteristics();
//
//        for (BluetoothGattCharacteristic c : characteristics) {
//            if (c.getUuid().toString().startsWith(com.antilost.app.bluetooth.UUID.ALARM_CHARACTERISTIC_UUID_PREFIX)) {
//                byte[] sendData = new byte[]{0x02}; //高音 {0x01}; 低音
//                c.setValue(sendData);
//                gatt.writeCharacteristic(c);
//            }
//        }
        BluetoothGattCharacteristic c = alertService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
        c.setValue(new byte[] {00});
        gatt.writeCharacteristic(c);
    }

    public void twowayMonitor(String bluetoothDeviceAddress, boolean enable) {
        Integer state = mGattStates.get(bluetoothDeviceAddress);
        Log.v(LOG_TAG, "twowayMonitor state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if (gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
            return;
        } else {
            BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);

            if(linkLoss == null) {
                Log.i(LOG_TAG, "linkLoss is null in twowayMonitor");
            }

            BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
            byte value = (byte) (enable ? 0x02 : 0x00);
            alertLevelChar.setValue(new byte[] {value});
            gatt.writeCharacteristic(alertLevelChar);
        }
    }

    public void readBatteryLevel(String bluetoothDeviceAddress) {
        Integer state = mGattStates.get(bluetoothDeviceAddress);
        if(state != null && state != BluetoothProfile.STATE_CONNECTED) {
            Log.w(LOG_TAG, "read a unconnected device battery level.");
            return;
        }
        Log.w(LOG_TAG, "readBatteryLevel state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if(gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
        } else {
            BluetoothGattService batteryService = gatt.getService(com.antilost.app.bluetooth.UUID.BATTERY_SERVICE_UUID);
            BluetoothGattCharacteristic c = batteryService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_BATTERY_LEVEL_UUID);
            gatt.readCharacteristic(c);
        }
    }

    public int requestRssiLevel(String bluetoothDeviceAddress) {
        Integer state = mGattStates.get(bluetoothDeviceAddress);
        Log.d(LOG_TAG, "readRssLevel state is " + state);

        Integer rssiValue = mGattsRssis.get(bluetoothDeviceAddress);

        if(rssiValue == null) {
            rssiValue = TrackRActivity.MIN_RSSI_LEVEL;
        }

        if(state == null) {
            state = BluetoothProfile.STATE_DISCONNECTED;
        }
        if(state != null && state != BluetoothProfile.STATE_CONNECTED) {
            Log.w(LOG_TAG, "read a unconnected device rssi level.");
            return rssiValue;
        }
        if(state == BluetoothProfile.STATE_CONNECTED) {
            BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);
            if(gatt != null) {
                gatt.readRemoteRssi();
            }
        }

        return rssiValue;
    }

    public int getRssiLevel(String address) {
        Integer rssi = mGattsRssis.get(address);
        return rssi == null ? TrackRActivity.MIN_RSSI_LEVEL :rssi;
    }

    public int getBatteryLevel(String address) {
        Integer level = mGattsBatteryLevel.get(address) ;
        return level == null ? 0 : level;
    }

    public int startReadRssiRepeat(boolean enabled, String address) {
        Integer rssi = mGattsRssis.get(address);
        if(rssi == null) {
            rssi = TrackRActivity.MIN_RSSI_LEVEL;
        }


        if(enabled) {
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

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

}
