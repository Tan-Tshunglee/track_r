package com.antilost.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NetworkSyncService extends Service {
    public NetworkSyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
