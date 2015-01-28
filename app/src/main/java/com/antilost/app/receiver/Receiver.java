package com.antilost.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.service.MonitorService;

public class Receiver extends BroadcastReceiver {
    public static final String REPEAT_BROADCAST_RECEIVER_ACTION = "REPEAT_ALARM_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)
                || REPEAT_BROADCAST_RECEIVER_ACTION.equals(action)) {
            startBluetoothMonitor(context);
        }
    }

    private void startBluetoothMonitor(Context context) {
        Intent i = new Intent(context, BluetoothLeService.class);
        context.startService(i);
    }
}
