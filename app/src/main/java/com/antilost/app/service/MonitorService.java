package com.antilost.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.antilost.app.BuildConfig;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.receiver.Receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MonitorService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int ALARM_REPEAT_PERIOD = BuildConfig.DEBUG ? 1000 * 5  : 1000 * 5 * 60 ;
    private static final String LOG_TAG = "MonitorService";
    private AlarmManager mAlarmManager;
    private BluetoothAdapter mBluetoothAdpater;
    private PrefsManager mPrefsManager;
    private Set<String> mIds;
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
    private HashMap<String, BluetoothGatt> mGattMap = new HashMap<String, BluetoothGatt>();


    private BluetoothGattService alertService;

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();
            }
        }

        //we can read all service data;
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            alertService = gatt.getService(UUID.fromString(com.antilost.app.bluetooth.UUID.IMMEDIATE_ALERT_SERVICE_UUID));
//            List<BluetoothGattCharacteristic> characteristics = alertService.getCharacteristics();
//            for(BluetoothGattCharacteristic c: characteristics) {
//                if(c.getUuid().toString().startsWith(com.antilost.app.bluetooth.UUID.ALARM_CHARACTERISTIC_UUID_PREFIX)) {
//                    int permissions = c.getPermissions();
//                    Log.v(LOG_TAG, "permission is " + permissions);
//                    byte[] data = c.getValue();
//                    if(data != null) {
//                        final StringBuilder sb = new StringBuilder(data.length);
//                        for(byte byteChar : data)
//                            sb.append(String.format("%02X ", byteChar));
//                        Log.v(LOG_TAG, "value is " + sb);
//                    } else {
//                        Log.e(LOG_TAG, "null data!");
//                    }
//                    byte[] sendData = new byte[] {0x01};
//                    c.setValue(sendData);
//                    mBluetoothGatt.writeCharacteristic(c);
//                };
//
//            }

            BluetoothGattService keyPressService = gatt.getService(UUID.fromString(com.antilost.app.bluetooth.UUID.KEY_PRESS_SERVICE_UUID));
            List<BluetoothGattCharacteristic> cs = alertService.getCharacteristics(); {
                for(BluetoothGattCharacteristic c : cs) {
                    UUID uuid = c.getUuid();
                    if(uuid.toString().startsWith(com.antilost.app.bluetooth.UUID.KEY_PRESS_CHARACTERISTIC_UUID_PREFIX)) {
                        setCharacteristicNotification(mBluetoothGatt, UUID.fromString(com.antilost.app.bluetooth.UUID.KEY_PRESS_SERVICE_UUID), uuid, true);
                    }
                }
            }

        }

        protected final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        public boolean setCharacteristicNotification(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid,
                                                     boolean enable) {
            if (BuildConfig.DEBUG)
                Log.d(LOG_TAG, "setCharacteristicNotification(device=" + "  UUID="
                        + characteristicUuid + ", enable=" + enable + " )");
            BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);
            gatt.setCharacteristicNotification(characteristic, enable);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            return gatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {

            Log.v(LOG_TAG, " read c is " + characteristic.getValue());
        }

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

            if(characteristic.getUuid().toString().startsWith(com.antilost.app.bluetooth.UUID.ALARM_CHARACTERISTIC_UUID_PREFIX)) {
                Log.v(LOG_TAG, "write status is " + status);
                Toast.makeText(MonitorService.this, "onCharacteristicWrite  status " + status, Toast.LENGTH_SHORT).show();
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
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
        }

    };
    private BluetoothGatt mBluetoothGatt;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent  = PendingIntent.getBroadcast(this, 0, new Intent(Receiver.REPEAT_BROADCAST_RECEIVER_ACTION), 0);
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.uptimeMillis(), ALARM_REPEAT_PERIOD, pendingIntent);

        mBluetoothAdpater = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdpater.enable();

        mPrefsManager = PrefsManager.singleInstance(this);
        mPrefsManager.addPrefsListener(this);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateBluetoothDeviceList();
        return Service.START_STICKY;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(PrefsManager.PREFS_UID_KEY.equals(key)) {
            updateBluetoothDeviceList();
        }
    }

    private void updateBluetoothDeviceList() {
        mIds = mPrefsManager.getTrackIds();
        String id = (String) mIds.toArray()[0];
        BluetoothDevice device = mBluetoothAdpater.getRemoteDevice(id);
        mBluetoothGatt  = device.connectGatt(this, false, mBluetoothGattCallback);

    }

    @Override
    public void onDestroy() {
        mPrefsManager.removePrefsListener(this);
    }
}
