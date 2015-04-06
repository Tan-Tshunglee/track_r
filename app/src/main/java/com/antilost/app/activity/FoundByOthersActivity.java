package com.antilost.app.activity;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.antilost.app.R;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.LocUtils;
import com.antilost.app.util.Utils;

public class FoundByOthersActivity extends Activity {

    public static final String EXTRA_TRACK_ADDRESS = "track_address";
    private static final String LOG_TAG = "FoundByOthersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String address = getIntent().getStringExtra(EXTRA_TRACK_ADDRESS);

        PrefsManager prefs = PrefsManager.singleInstance(this);
        if(Utils.isValidMacAddress(address)) {
            Location loc = prefs.getLastLocFoundByOther(address);
            if(loc != null) {
                LocUtils.viewLocation(this, loc);
            }
        } else {
            Log.e(LOG_TAG, "get invalid mac address.");
        }
        finish();
    }
}
