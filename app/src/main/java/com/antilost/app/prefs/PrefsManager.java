package com.antilost.app.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.antilost.app.model.TrackR;
import com.antilost.app.util.LocUtils;

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
    public static final String PREFS_CLOSED_KEY_PREFIX = "closed_track_prefix";
    public static final String PREFS_BIDIRECTIONAL_ALERT_PREFIX = "bidirectional_alert_prefix";

    public static final String PREFS_HOME_WIFI_SSID_KEY = "home_wifi_ssid";
    public static final String PREFS_OFFICE_WIFI_SSID_KEY = "office_wifi_ssid";
    public static final String PREFS_OTHER_WIFI_SSID_KEY = "other_wifi_ssid";

    public static final String PREFS_SAFE_ZONE_ENABLED = "safe_zone_enabled";

    public static final String PREFS_ALERT_RING_ENABLED = "alert_ring_enabled";
    public static final String PREFS_LAST_LOST_LOCATION_KEY_PREFIX = "last_lost_location";

    public static final String PREFS_SLEEP_MODE_KEY = "sleep_mode_prefs_key";

    public static final String PREFS_SLEEP_START_TIME_KEY = "sleep_mode_start_time_key";
    public static final String PREFS_SLEEP_END_TIME_KEY = "sleep_mode_end_time_key";

    public static final int SLEEP_MODE_STATR_TIME_OFFSET = 79200000;// 22 * 60 * 60 * 1000
    public static final int SLEEP_MODE_END_TIME_OFFSET = 28800000;// 8 * 60 * 60 * 1000

    private final Context mCtx;
    private SharedPreferences mPrefs;

    public static final PrefsManager singleInstance(Context ctx) {
        if(instance == null) {
            instance = new PrefsManager(ctx);
        }
        return instance;
    }

    private static PrefsManager instance;
    private PrefsManager(Context ctx) {
        ctx = ctx.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mCtx = ctx.getApplicationContext();
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
        return ids.add(trackId) &&  mPrefs.edit().putStringSet(PREFS_TRACK_IDS_KEY, ids).commit();
    }

    public boolean removeTrackId(String trackId) {
        Set<String> ids = getTrackIds();
        if(ids.remove(trackId)) {
            return mPrefs.edit().putStringSet(PREFS_TRACK_IDS_KEY, ids).commit();
        }
        return false;
    }

    public boolean validUserLog() {

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
        if(TextUtils.isEmpty(address)) {
            return null;
        }
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

    public void saveClosedTrack(String address, boolean cloased) {
        String key = PREFS_CLOSED_KEY_PREFIX + address;
        mPrefs.edit().putBoolean(key, cloased).commit();
    }

    public boolean isClosedTrack(String address) {
        String key = PREFS_CLOSED_KEY_PREFIX + address;
        return mPrefs.getBoolean(key, false);
    }

    public Location getTrackLocMissed(String address) {
        Location location = new Location("custom");
        location.setLongitude(114.07);
        location.setLatitude(22.62);
        return location;
    }

    public void setBidirectionalAlert(String address, boolean enable) {
        String key = PREFS_BIDIRECTIONAL_ALERT_PREFIX + address;
        mPrefs.edit().putBoolean(key, enable).commit();
    }

    public boolean getBidirectionalAlert(String address) {
        String key = PREFS_BIDIRECTIONAL_ALERT_PREFIX + address;
        return mPrefs.getBoolean(key, false);
    }

    public void setSafeZoneEnable(boolean enabled) {
        mPrefs.edit().putBoolean(PREFS_SAFE_ZONE_ENABLED, enabled).commit();
    }

    public boolean getSafeZoneEnable() {
        return mPrefs.getBoolean(PREFS_SAFE_ZONE_ENABLED, false);
    }

    public void setHomeWifiSsid(String ssid) {
        mPrefs.edit().putString(PREFS_HOME_WIFI_SSID_KEY, ssid).commit();
    }

    public String getHomeWifiSsid() {
        return mPrefs.getString(PREFS_HOME_WIFI_SSID_KEY, "");
    }

    public void setOfficeSsid(String ssid) {
        mPrefs.edit().putString(PREFS_OFFICE_WIFI_SSID_KEY, ssid).commit();
    }

    public String getOfficeSsid() {
        return mPrefs.getString(PREFS_OFFICE_WIFI_SSID_KEY, "");
    }

    public void setOtherSsid(String ssid) {
        mPrefs.edit().putString(PREFS_OTHER_WIFI_SSID_KEY, ssid).commit();
    }

    public String getOtherSsid() {
        return mPrefs.getString(PREFS_OTHER_WIFI_SSID_KEY, "");
    }

    public void setAlertRingEnabled(boolean b) {
        mPrefs.edit().putBoolean(PREFS_ALERT_RING_ENABLED, b).commit();
    }

    public boolean getAlertRingEnabled() {
        return mPrefs.getBoolean(PREFS_ALERT_RING_ENABLED, true);
    }

    public void saveLastLostLocation(Location loc, String address) {

        String key = PREFS_LAST_LOST_LOCATION_KEY_PREFIX + address;
        mPrefs.edit().putString(key, LocUtils.convertLocation(loc));
    }

    public Location getLastLostLocation(String address) {
        String key = PREFS_LAST_LOST_LOCATION_KEY_PREFIX + address;
        String loc = mPrefs.getString(key, null);
        if(loc == null) {
            return null;
        }

        return LocUtils.convertLocation(loc);

    }


    public void setSleepMode(boolean enable) {
        mPrefs.edit().putBoolean(PREFS_SLEEP_MODE_KEY, enable).commit();
    }

    public boolean getSleepMode() {
        return mPrefs.getBoolean(PREFS_SLEEP_MODE_KEY, false);
    }

    public void setSleepTime(boolean start, long timestamp) {
        String key = start ? PREFS_SLEEP_START_TIME_KEY : PREFS_SLEEP_END_TIME_KEY;
        mPrefs.edit().putLong(key, timestamp);
    }

    public long getSleepTime(boolean start) {
        String key = start ? PREFS_SLEEP_START_TIME_KEY : PREFS_SLEEP_END_TIME_KEY;
        int defaultTime = start ? SLEEP_MODE_STATR_TIME_OFFSET : SLEEP_MODE_END_TIME_OFFSET;
        return mPrefs.getInt(key, defaultTime);
    }
}
