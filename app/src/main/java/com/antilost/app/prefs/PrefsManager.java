package com.antilost.app.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    private final SharedPreferences mPrefs;

    public static final PrefsManager singleInstance(Context ctx) {
        if(instance == null) {
            instance = new PrefsManager(ctx);
        }
        return instance;
    }

    private static PrefsManager instance;
    private PrefsManager(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
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
}
