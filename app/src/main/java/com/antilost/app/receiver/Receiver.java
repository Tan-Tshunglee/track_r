package com.antilost.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.antilost.app.service.BluetoothLeService;

public class Receiver extends BroadcastReceiver {
    public static final String REPEAT_BROADCAST_RECEIVER_ACTION = "com.antilost.app.ACTION_REPEAT_ALARM_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        String action = intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)
                || REPEAT_BROADCAST_RECEIVER_ACTION.equals(action)) {
            Log.v("Receiver", "receive broadcast then start service");
            startBluetoothMonitor(context);
        }
    }

    private void startBluetoothMonitor(Context context) {
        Intent i = new Intent(context, BluetoothLeService.class);
        i.putExtra(BluetoothLeService.INTENT_FROM_BROADCAST_EXTRA_KEY_NAME, true);
        context.startService(i);
    }
}
