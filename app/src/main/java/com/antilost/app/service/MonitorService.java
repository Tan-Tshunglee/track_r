package com.antilost.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.antilost.app.BuildConfig;
import com.antilost.app.receiver.Receiver;

public class MonitorService extends Service {
    public static final int ALARM_REPEAT_PERIOD = BuildConfig.DEBUG ? 1000 * 5  : 1000 * 5 * 60 ;
    private AlarmManager mAlarmManager;
    private BluetoothAdapter mBluetoothAdpater;

    public MonitorService() {
    }

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



    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
}
