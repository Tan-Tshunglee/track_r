package com.antilost.app;

import android.app.Activity;
import android.app.Application;

import com.android.mod.Util;
import com.antilost.app.service.BluetoothLeService;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class TrackRApplication extends Application {
    private BluetoothLeService mBluetoothLeService;

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        BUG_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.BUG_TRACKER) ? analytics.newTracker(R.xml.bug_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        Util.initialize(this);

        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                getTracker(TrackerName.BUG_TRACKER),                                        // Currently used Tracker.
                Thread.getDefaultUncaughtExceptionHandler(),      // Current default uncaught exception handler.
                this);                                         // Context of the application.

        // Make myHandler the new default uncaught exception handler.
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
    }

    public void setBluetootLeService(BluetoothLeService service) {
        mBluetoothLeService = service;
    }


    public static void onUserInteraction(Activity activity) {
        TrackRApplication app = (TrackRApplication) activity.getApplication();
        if(app.mBluetoothLeService != null) {
            app.mBluetoothLeService.onUserInteraction();
        }
    }


}
