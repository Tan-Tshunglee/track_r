package com.antilost.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.antilost.app.model.TrackR;
import com.antilost.app.network.FetchAllTrackRCommand;
import com.antilost.app.prefs.PrefsManager;

import java.util.List;

public class NetworkSyncService extends Service {

    public static final String ACTION_SYNC_AFTER_LOGIN = "server_intent_action_sync_after_login";
    private static final String LOG_TAG = "NetworkSyncService"  ;
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
        if(ACTION_SYNC_AFTER_LOGIN.equals(intent.getAction())) {
            (new SyncThread()).start();
        }
        return START_NOT_STICKY;
    }


    class SyncThread extends Thread {
        @Override
        public void run() {
            if(mPrefs.validUserLog()) {
                int uid = mPrefs.getUid();
                FetchAllTrackRCommand fetchAllCommand = new FetchAllTrackRCommand(uid);
                fetchAllCommand.execTask();
                List<TrackR> trackRs = fetchAllCommand.getBoundTrackRs();

                if(trackRs == null) {
                    Log.i(LOG_TAG, "no track bound info return.");
                    return;
                }
                for(TrackR track: trackRs) {
                    mPrefs.addTrackR(track);
                }
            } else {
                Log.e(LOG_TAG, "will to sync data while user is not login.");
            }
        }
    }
}
