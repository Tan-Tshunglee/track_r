package com.antilost.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.antilost.app.prefs.PrefsManager;

public class NetworkSyncService extends Service {

    public static final String ACTION_SYNC_AFTER_LOGIN = "server_intent_action_sync_after_login";
    private PrefsManager mPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PrefsManager.singleInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    class SyncThread extends Thread {
        @Override
        public void run() {
            if(mPrefs.validUserLog()) {

            }
        }
    }
}
