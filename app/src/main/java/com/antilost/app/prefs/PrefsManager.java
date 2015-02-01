package com.antilost.app.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.antilost.app.model.TrackR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tan on 2015/1/22.
 */
public class PrefsManager {

    public static final String PREFS_UID_KEY = "uid";
    public static final String PREFS_NAME_KEY = "name";
    public static final String PREFS_PASSWORD_KEY = "password";
    public static final String PREFS_TRACK_IDS_KEY = "tracks";
    public static final String PREFS_EMAIL_KEY = "email";
    public static final String PREFS_MISSING_KEY_PREFIX = "missing_track_prefix";

    private Context mCtx;
    private SharedPreferences mPrefs;

    public static final PrefsManager singleInstance(Context ctx) {
        if(instance == null) {
            instance = new PrefsManager(ctx);
        }
        return instance;
    }

    private static PrefsManager instance;
    private PrefsManager(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mCtx = ctx;
    }

    public int getUid() {
        return mPrefs.getInt(PREFS_UID_KEY, -1);
    }

    public boolean setUid(int uid) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(PREFS_UID_KEY, uid);
        return editor.commit();
    }

    public Set<String> getTrackIds() {
        return new HashSet<String>(mPrefs.getStringSet(PREFS_TRACK_IDS_KEY, new HashSet<String>()));
    }

    public boolean addTrackIds(String trackId) {
        Set<String> ids = getTrackIds();
        if(ids.add(trackId)) {
            return mPrefs.edit().putStringSet(PREFS_TRACK_IDS_KEY, ids).commit();
        }
        return false;
    }

    public boolean removeTrackIds(String trackId) {
        Set<String> ids = getTrackIds();
        if(ids.remove(trackId)) {
            return mPrefs.edit().putStringSet(PREFS_TRACK_IDS_KEY, ids).commit();
        }
        return false;
    }

    public boolean alreadyLogin() {

        return /*BuildConfig.DEBUG ? true :*/ getUid() > 0;
    }

    public void setEmail(String email) {
        mPrefs.edit().putString(PREFS_EMAIL_KEY, email).commit();
    }


    public String getEmail() {
        return mPrefs.getString(PREFS_EMAIL_KEY, "");
    }

    public void setPassword(String password) {
        mPrefs.edit().putString(PREFS_PASSWORD_KEY, password).commit();

    }

    public void addPrefsListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPrefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public void removePrefsListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    }


    public String getPassword() {
        return mPrefs.getString(PREFS_PASSWORD_KEY, "");

    }

    public boolean saveTrack(String address, TrackR track) {
        File dir = mCtx.getDir("tracks", 0);
        File trackFile = new File(dir, address);
        try {
            FileOutputStream out = new FileOutputStream(trackFile);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(track);
            objOut.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public TrackR getTrack(String address) {
        File dir = mCtx.getDir("tracks", 0);

        File trackFile = new File(dir, address);
        if(!trackFile.exists()) {
            return null;
        }

        try {
            FileInputStream in = new FileInputStream(trackFile);
            ObjectInputStream objIn = new ObjectInputStream(in);
            TrackR track = (TrackR) objIn.readObject();
            objIn.close();
            return track;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveMissedTrack(String address, boolean missing) {
        String key = PREFS_MISSING_KEY_PREFIX + address;
        mPrefs.edit().putBoolean(key, missing).commit();
        return;
    }

    public boolean isMissedTrack(String address) {
        String key = PREFS_MISSING_KEY_PREFIX + address;
        return mPrefs.getBoolean(key, false);
    }

    public Location getTrackLocMissed(String address) {
        Location location = new Location("custom");
        location.setLongitude(114.07);
        location.setLatitude(22.62);
        return location;
    }
}
