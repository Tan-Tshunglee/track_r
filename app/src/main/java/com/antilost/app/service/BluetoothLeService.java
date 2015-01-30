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
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.antilost.app.BuildConfig;
import com.antilost.app.R;
import com.antilost.app.activity.MainTrackRListActivity;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.receiver.Receiver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String LOG_TAG = "BluetoothLeService";
    private static final int ONGOING_NOTIFICATION = 1;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private HashMap<String, BluetoothGatt> mBluetoothGatts = new HashMap<String, BluetoothGatt>();
    private HashMap<String, Integer> mGattStates = new HashMap<String, Integer>();

    public static final int ALARM_REPEAT_PERIOD = BuildConfig.DEBUG ? 1000 * 5 : 1000 * 5 * 60;

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(LOG_TAG, "BluetoothLeService onSharedPreferenceChanged() key " + key);
        if (PrefsManager.PREFS_TRACK_IDS_KEY.equals(key)) {
            initialize();
        }
    }




    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.

    private class MyBluetootGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String address = gatt.getDevice().getAddress();
            mGattStates.put(address, newState);
            mBluetoothGatts.put(address, gatt);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
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

                mGattStates.remove(address);
                mBluetoothGatts.remove(address);
                gatt.disconnect();

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(LOG_TAG, "onServicesDiscovered ....");
            mBluetoothGatts.put(gatt.getDevice().getAddress(), gatt);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                if(setCharacteristicNotification(gatt,
                        UUID.fromString(com.antilost.app.bluetooth.UUID.SIMPLE_KEY_SERVICE_UUID),
                        com.antilost.app.bluetooth.UUID.CHARACTERISTIC_KEY_PRESS_UUID,
                        true)) {
                    Log.v(LOG_TAG, "setCharacteristicNotification ok");
                }
            } else {
                Log.w(LOG_TAG, "onServicesDiscovered received: " + status);
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
                if(cId.equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_BATTERY_LEVEL_UUID)) {
                    int level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.v(LOG_TAG, "battery level is " + level);
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
                Log.v(LOG_TAG, "onCharacteristicChanged value is " + key);
                if(key == 0) {
                    onTrackKeyDown();
                } else {
                    onTrackKeyUp();
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

            Log.d(LOG_TAG, "value is " + value);
            //user manully close track r
            if(serviceUuid.equals(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID)
                    && characteristic.getUuid().equals(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID)
                    && characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 0) {
                Log.i(LOG_TAG, "use choose to close track r.");
                String address = gatt.getDevice().getAddress();
                mPrefsManager.removeTrackIds(address);
                mGattStates.put(address, BluetoothProfile.STATE_DISCONNECTED);
                gatt.disconnect();
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
            Log.v(LOG_TAG, "rssi is "   + rssi);
        }
    }

    private void onTrackKeyDown() {
        Log.v(LOG_TAG, "onTrackKeyDown...");
    }

    private void onTrackKeyUp() {
        Log.v(LOG_TAG, "onTrackKeyUp...");
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
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(Receiver.REPEAT_BROADCAST_RECEIVER_ACTION), 0);
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.uptimeMillis(), ALARM_REPEAT_PERIOD, pendingIntent);
        mPrefsManager = PrefsManager.singleInstance(this);

        mPrefsManager.addPrefsListener(this);
        Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.track_r_is_running), System.currentTimeMillis());


        Intent notificationIntent = new Intent(this, MainTrackRListActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getString(R.string.track_r_is_running),
                getString(R.string.track_r_is_running), pendingIntent);
        startForeground(ONGOING_NOTIFICATION, notification);

        initialize();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPrefsManager.removePrefsListener(this);

        Set<Map.Entry<String, BluetoothGatt>> gatts = mBluetoothGatts.entrySet();
        for (Map.Entry<String, BluetoothGatt> entry : gatts) {
            entry.getValue().disconnect();
        }
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    private boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(LOG_TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
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
        if (mBluetoothAdapter == null || address == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(LOG_TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.

        mGattStates.put(address, BluetoothProfile.STATE_CONNECTING);
        BluetoothGatt bluetoothGatt = mBluetoothGatts.get(address);
        int oldState = mGattStates.get(address);
        if (bluetoothGatt != null && oldState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(LOG_TAG, "device already connected");
            return true;
        }


        bluetoothGatt = device.connectGatt(this, false, new MyBluetootGattCallback());
        mBluetoothGatts.put(address, bluetoothGatt);
        Log.i(LOG_TAG, "Trying to create a new connection to " + address);
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.disconnect();
        //fix it
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
//        if (mBluetoothGatt == null) {
//            return;
//        }
//        mBluetoothGatt.close();
//        mBluetoothGatt = null;
        //TODO fix close resouce;
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
        return state == null ? BluetoothProfile.STATE_CONNECTING : state;
    }

    public HashMap<String, BluetoothGatt> getBluetoothGatts() {
        return mBluetoothGatts;
    }

    public void clossTrackR(String address) {
        int state = mGattStates.get(address);
        if(state == BluetoothProfile.STATE_CONNECTED) {
            BluetoothGatt gatt = mBluetoothGatts.get(address);
            BluetoothGattService linkLoss = gatt.getService(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERVICE_UUID);
            BluetoothGattCharacteristic alertLevelChar = linkLoss.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_ALERT_LEVEL_UUID);
            alertLevelChar.setValue(new byte[] {03});
            gatt.writeCharacteristic(alertLevelChar);
        } else {
            Log.w(LOG_TAG, "can not close unconnected track r");
        }
    }

    public void ringTrackR(String bluetoothDeviceAddress) {
        Integer state = mGattStates.get(bluetoothDeviceAddress);
        Log.v(LOG_TAG, "silentRing state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if (gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
            initialize();
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
        c.setValue(new byte[] {02});
        gatt.writeCharacteristic(c);
    }

    public void silentRing(String bluetoothDeviceAddress) {
        Integer state = mGattStates.get(bluetoothDeviceAddress);
        Log.v(LOG_TAG, "silentRing state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if (gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
            initialize();
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
            initialize();
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
        Log.v(LOG_TAG, "readBatteryLevel state is " + state);
        BluetoothGatt gatt = mBluetoothGatts.get(bluetoothDeviceAddress);

        if(gatt == null) {
            Log.w(LOG_TAG, "gatt has not connected....");
            initialize();
        } else {
            BluetoothGattService batteryService = gatt.getService(com.antilost.app.bluetooth.UUID.BATTERY_SERVICE_UUID);
            BluetoothGattCharacteristic c = batteryService.getCharacteristic(com.antilost.app.bluetooth.UUID.CHARACTERISTIC_BATTERY_LEVEL_UUID);
            gatt.readCharacteristic(c);
        }
    }

    public void readRssLevel(String bluetoothDeviceAddress) {
        Integer state = mGattStates.get(bluetoothDeviceAddress);
        Log.v(LOG_TAG, "readRssLevel state is " + state);

    }
}
