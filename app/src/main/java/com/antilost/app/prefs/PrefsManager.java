package com.antilost.app.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.antilost.app.activity.ScanTrackActivity;
import com.antilost.app.model.TrackR;
import com.antilost.app.util.LocUtils;
import com.antilost.app.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

public class PrefsManager {

    public static final String PREFS_UID_KEY = "uid";
    @SuppressWarnings("unused")
    public static final String PREFS_NAME_KEY = "name";
    public static final String PREFS_PASSWORD_KEY = "password";
    public static final String PREFS_TRACK_IDS_KEY = "tracks";
    public static final String PREFS_EMAIL_KEY = "email";
    public static final String PREFS_MISSING_KEY_PREFIX = "missing_track_prefix";
    public static final String PREFS_CLOSED_KEY_PREFIX = "closed_track_prefix";
    public static final String PREFS_TRACK_ALERT_PREFIX = "track_alert_prefix";
    public static final String PREFS_PHONE_ALERT_PREFIX = "phone_alert_prefix";

    public static final String PREFS_HOME_WIFI_SSID_KEY = "home_wifi_ssid";
    public static final String PREFS_OFFICE_WIFI_SSID_KEY = "office_wifi_ssid";
    public static final String PREFS_OTHER_WIFI_SSID_KEY = "other_wifi_ssid";

    public static final String PREFS_SAFE_ZONE_ENABLED = "safe_zone_enabled";

    public static final String PREFS_ALERT_RING_ENABLED = "alert_ring_enabled";
    public static final String PREFS_LAST_LOST_LOCATION_KEY_PREFIX = "last_lost_location";
    public static final String PREFS_LAST_LOCATION_AMPA_KEY_PREFIX = "last_location";
    public static final String PREFS_LAST_LOST_TIME_KEY_PREFIX = "last_lost_time";

    public static final String PREFS_DECLARE_LOST_KEY_PREFIX = "declare_lost_";
    public static final String PREFS_SLEEP_MODE_KEY = "sleep_mode_prefs_key";

    public static final String PREFS_SLEEP_START_TIME_KEY = "sleep_mode_start_time_key";
    public static final String PREFS_SLEEP_END_TIME_KEY = "sleep_mode_end_time_key";


    public static final int SLEEP_MODE_STATR_TIME_OFFSET = 79200000;// 22 * 60 * 60 * 1000
    public static final int SLEEP_MODE_END_TIME_OFFSET = 28800000;// 8 * 60 * 60 * 1000
    private static final String LOG_TAG = "PrefsManager";

    private final Context mCtx;
    private SharedPreferences mPrefs;

    public final static PrefsManager singleInstance(Context ctx) {
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

    public boolean addTrackId(String trackId) {
        Set<String> ids = getTrackIds();
        if(ids.size() >= ScanTrackActivity.MAX_COUNT) {
            return false;
        }
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

    public boolean saveTrackToFile(String address, TrackR track) {
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

    public void setTracklAlert(String address, boolean enable) {
        String key = PREFS_TRACK_ALERT_PREFIX + address;
        mPrefs.edit().putBoolean(key, enable).commit();
    }

    public boolean getTrackAlert(String address) {
        String key = PREFS_TRACK_ALERT_PREFIX + address;
        return mPrefs.getBoolean(key, false);
    }


    public void setPhoneAlert(String address, boolean enable) {
        String key = PREFS_PHONE_ALERT_PREFIX + address;
        mPrefs.edit().putBoolean(key, enable).commit();
    }

    public boolean getPhoneAlert(String address) {
        String key = PREFS_PHONE_ALERT_PREFIX + address;
        return mPrefs.getBoolean(key, true);
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

    public void saveLastAMPALocation(Location loc) {
        String key = PREFS_LAST_LOCATION_AMPA_KEY_PREFIX ;
        mPrefs.edit().putString(key, LocUtils.convertLocation(loc)).commit();
    }
    public Location getLastAMPALocation() {
        String key = PREFS_LAST_LOCATION_AMPA_KEY_PREFIX ;
        String loc = mPrefs.getString(key, null);
        if(loc == null) {
            return null;
        }
        return LocUtils.convertLocation(loc);
    }

    public Location getLastLostLocation(String address) {
        String key = PREFS_LAST_LOST_LOCATION_KEY_PREFIX + address;
        String loc = mPrefs.getString(key, null);
        if(loc == null) {
            return null;
        }
        return LocUtils.convertLocation(loc);

    }

    public void saveLastLostTime(String address) {
        String key = PREFS_LAST_LOST_TIME_KEY_PREFIX + address;
        long time = System.currentTimeMillis();
        mPrefs.edit().putLong(key, time).commit();
    }

    public long getLastLostTime(String address) {
        String key = PREFS_LAST_LOST_TIME_KEY_PREFIX + address;
        return mPrefs.getLong(key, -1);
    }


    public void setSleepMode(boolean enable) {
        mPrefs.edit().putBoolean(PREFS_SLEEP_MODE_KEY, enable).commit();
    }

    public boolean getSleepMode() {
        return mPrefs.getBoolean(PREFS_SLEEP_MODE_KEY, false);
    }

    public void setSleepTime(boolean start, long timestamp) {
        String key = start ? PREFS_SLEEP_START_TIME_KEY : PREFS_SLEEP_END_TIME_KEY;
        mPrefs.edit().putLong(key, timestamp).commit();
    }

    public long getSleepTime(boolean isStart) {
        String key = isStart ? PREFS_SLEEP_START_TIME_KEY : PREFS_SLEEP_END_TIME_KEY;
        int defaultTime = isStart ? SLEEP_MODE_STATR_TIME_OFFSET : SLEEP_MODE_END_TIME_OFFSET;
        return mPrefs.getLong(key, defaultTime);
    }

    public void addTrackR(TrackR track) {
        if(track != null) {
            String address = track.address;
            if(Utils.isValidMacAddress(address)) {
                if(addTrackId(address)) {
                    saveMissedTrack(address, true);
                    saveTrackToFile(address, track);
                    Log.v(LOG_TAG, "add one track with address " + address);
                }
            }
        }
    }

    public boolean hasTrack() {
        if(getTrackIds().size() > 0 ) {
            return true;
        }
        return false;
    }


    public void setDeclareLost(String address, boolean declareLost) {
        String key = PREFS_DECLARE_LOST_KEY_PREFIX + address;

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(key, declareLost).commit();
    }

    public boolean isDeclaredLost(String address) {
        String key = PREFS_DECLARE_LOST_KEY_PREFIX + address;
        return mPrefs.getBoolean(key, false);
    }

    public void cleanUpAfterUserLogout() {
        Set<String> oldIds = mPrefs.getStringSet(PREFS_TRACK_IDS_KEY, new HashSet<String>());

        for(String id: oldIds) {
            setDeclareLost(id, false);
            saveClosedTrack(id, false);
            saveMissedTrack(id, false);
        }
        mPrefs.edit().putStringSet(PREFS_TRACK_IDS_KEY, new HashSet<String>()).commit();
    }
}
