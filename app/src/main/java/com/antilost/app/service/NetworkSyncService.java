package com.antilost.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.antilost.app.model.TrackR;
import com.antilost.app.network.FetchTrackImageCommand;
import com.antilost.app.network.FetchAllTrackRCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.CsstSHImageData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NetworkSyncService extends Service {

    public static final String ACTION_SYNC_AFTER_LOGIN = "server_intent_action_sync_after_login";

    public static final String ACTION_TRACKS_FETCH_DONE = "user_tracks_fetched_done";

    private static final String LOG_TAG = "NetworkSyncService"  ;
    private PrefsManager mPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PrefsManager.singleInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ACTION_SYNC_AFTER_LOGIN.equals(intent.getAction())) {
            Log.i(LOG_TAG, "onStartCommand get sync after login intent");
            (new TrackListSyncThread()).start();
        }
        return START_NOT_STICKY;
    }


   private class TrackListSyncThread extends Thread {
        @Override
        public void run() {
            if(mPrefs.validUserLog()) {
                int uid = mPrefs.getUid();
                FetchAllTrackRCommand fetchAllCommand = new FetchAllTrackRCommand(uid);
                fetchAllCommand.execTask();
                HashMap<String, TrackR> trackRs = fetchAllCommand.getBoundTrackRs();

                if(mPrefs.userChanged()) {
                   mPrefs.cleanUpTracks();
                }

                if (trackRs == null || trackRs.isEmpty()) {
                    Log.i(LOG_TAG, "no track bound info return.");
                } else {
                    Set<Map.Entry<String, TrackR>> entrySet = trackRs.entrySet();
                    for (Map.Entry<String, TrackR> entry : entrySet) {
                        mPrefs.addTrackR(entry.getValue());
                    }
                    TrackPhotosFetcher fetcher = new TrackPhotosFetcher();
                    fetcher.start();
                }
                sendBroadcast(new Intent(ACTION_TRACKS_FETCH_DONE));



            } else {
                Log.e(LOG_TAG, "will to sync data while user is not login.");
            }
        }
    }

    private class TrackPhotosFetcher extends Thread {
        @Override
        public void run() {
            Set<String> ids = mPrefs.getTrackIds();
            for(String id : ids) {
                final FetchTrackImageCommand command = new FetchTrackImageCommand(mPrefs.getUid(), id);
                command.execTask();
                byte[] rawImageData = command.getRawImageData();
                if(rawImageData != null) {
                    Log.v(LOG_TAG, "get rawImageData length is " + rawImageData.length);
                    saveDataToFile(rawImageData, id);
                } else {
                    CsstSHImageData.removePhoto(id);
                    Log.e(LOG_TAG, "fetch track image No rawImageData return.");
                }
            }
        }
    }

    private void saveDataToFile(byte[] rawImageData, String address) {
        File folder = ensureIconFolder();
        File iconFile = new File(folder, address);
        try {
            FileOutputStream out = new FileOutputStream(iconFile);
            out.write(rawImageData);
            out.close();

            Log.i(LOG_TAG, "saveDataToFile finish successfull .");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File ensureIconFolder() {
        File folder = new File(CsstSHImageData.TRACKR_IMAGE_FOLDER);
        if(folder.exists() && folder.isFile()) {
            folder.delete();
            folder.mkdir();
        } else if(!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }
}
