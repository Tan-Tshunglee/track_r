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



    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                Log.v(LOG_TAG, "BluetoothGatt Connected.");
                mBluetoothGatt.discoverServices();
            }
        }

        //we can read all service data;
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            //send alarm
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
//                    byte[] sendData = new byte[] {0x01}; //{0x02}; 低音 高音
//                    c.setValue(sendData);
//                    mBluetoothGatt.writeCharacteristic(c);
//                };
//
//            }

//            //register key press notification
//            BluetoothGattService keyPressService = gatt.getService(UUID.fromString(com.antilost.app.bluetooth.UUID.KEY_PRESS_SERVICE_UUID));
//            List<BluetoothGattCharacteristic> cs = keyPressService.getCharacteristics(); {
//                for(BluetoothGattCharacteristic c : cs) {
//                    UUID uuid = c.getUuid();
//                    if(uuid.toString().startsWith(com.antilost.app.bluetooth.UUID.KEY_PRESS_CHARACTERISTIC_UUID_PREFIX)) {
//                        setCharacteristicNotification(mBluetoothGatt, UUID.fromString(com.antilost.app.bluetooth.UUID.KEY_PRESS_SERVICE_UUID), uuid, true);
//                    }
//                }
//            }

            //request battery level read
//            BluetoothGattService batteryLevel = gatt.getService(UUID.fromString(com.antilost.app.bluetooth.UUID.BATTERY_SERVICE_UUID));
//            List<BluetoothGattCharacteristic> cs = batteryLevel.getCharacteristics();
//                for(BluetoothGattCharacteristic c: cs) {
//                String uuid = c.getUuid().toString();
//                if(uuid.startsWith(com.antilost.app.bluetooth.UUID.BATTERY_LEVEL_CHARACTERISTIC_UUID_PREFIX)) {
//                    mBluetoothGatt.readCharacteristic(c);
//
//                }
//            }

            //request battery level read
//            BluetoothGattService batteryLevel = gatt.getService(UUID.fromString(com.antilost.app.bluetooth.UUID.BATTERY_SERVICE_UUID));
//            List<BluetoothGattCharacteristic> cs = batteryLevel.getCharacteristics();
//            for(BluetoothGattCharacteristic c: cs) {
//                String uuid = c.getUuid().toString();
//                if(uuid.startsWith(com.antilost.app.bluetooth.UUID.BATTERY_LEVEL_CHARACTERISTIC_UUID_PREFIX)) {
//                    mBluetoothGatt.readCharacteristic(c);
//
//                }
//            }

            //
            BluetoothGattService linkLossService = gatt.getService(UUID.fromString(com.antilost.app.bluetooth.UUID.LINK_LOSS_SERICE_UUID));
            List<BluetoothGattCharacteristic> cs = linkLossService.getCharacteristics();
            for(BluetoothGattCharacteristic c: cs) {
                String uuid = c.getUuid().toString();
                if(uuid.startsWith(com.antilost.app.bluetooth.UUID.ALERT_LEVEL_CHARACTERISTIC_UUID_PREFIX)) {
                    c.setValue(new byte[] {01});
                    mBluetoothGatt.writeCharacteristic(c);

                }
            }
        }

        protected final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        public boolean setCharacteristicNotification(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid,
                                                     boolean enable) {
            try {
                if (BuildConfig.DEBUG)
                    Log.d(LOG_TAG, "setCharacteristicNotification(device=" + "  UUID="
                            + characteristicUuid + ", enable=" + enable + " )");
                BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);
                gatt.setCharacteristicNotification(characteristic, enable);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                return gatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
            }catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {
            //read battery level call back
//            int level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//            if(characteristic.getUuid().toString().startsWith(
//                    com.antilost.app.bluetooth.UUID.BATTERY_LEVEL_CHARACTERISTIC_UUID_PREFIX)) {
//                Log.v(LOG_TAG, "battery level is " + level);
//            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

            if(characteristic.getUuid().toString().startsWith(com.antilost.app.bluetooth.UUID.ALARM_CHARACTERISTIC_UUID_PREFIX)) {
                Log.v(LOG_TAG, "write status is " + status + " characteristic uuid is " + characteristic.getUuid());
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //key press notification

//            UUID uuid = characteristic.getUuid();
//            Log.v(LOG_TAG, "onCharacteristicChanged service uuid. " + uuid);
//            if(characteristic.getUuid().toString().startsWith(com.antilost.app.bluetooth.UUID.KEY_PRESS_CHARACTERISTIC_UUID_PREFIX)) {
//                byte[] data = characteristic.getValue();
//                int key = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//                //key down is 0 and key up is 2;
//                Log.v(LOG_TAG, "onCharacteristicChanged value is " + key);
//            }
        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            Log.v(LOG_TAG, "onDescriptorRead value is " + descriptor.getValue());
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.v(LOG_TAG, "onDescriptorWrite value is " + descriptor.getValue());
        }
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.v(LOG_TAG, "onReliableWriteCompleted");
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.v(LOG_TAG, "onReliableWriteCompleted");
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
        connectOneBluetoothGatt();
        return Service.START_STICKY;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(LOG_TAG, "onSharedPreferenceChanged key is " + key);
        if(PrefsManager.PREFS_UID_KEY.equals(key)) {
            connectOneBluetoothGatt();
        }
    }

    private void connectOneBluetoothGatt() {
        mIds = mPrefsManager.getTrackIds();
        String id = (String) mIds.toArray()[0];
        BluetoothDevice device = mBluetoothAdpater.getRemoteDevice(id);
        mBluetoothGatt  = device.connectGatt(this, false, mBluetoothGattCallback);
        //mBluetoothGatt.disconnect();

    }

    @Override
    public void onDestroy() {
        mPrefsManager.removePrefsListener(this);
    }
}
