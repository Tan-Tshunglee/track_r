package com.antilost.app.util;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import java.util.Locale;

/**
 * Created by Tan on 2015/2/12.
 */
public class LocUtils {
    public static  final String convertLocation(Location loc) {
        if(loc == null) {
            return null;
        }
        return String.format("%f-%f", loc.getLatitude(), loc.getLongitude());
    }

    public static final Location convertLocation(String loc) {
        String[] pair = loc.split("-");
        Location location = new Location(LocationManager.GPS_PROVIDER);
        double latitude = Double.valueOf(pair[0]);
        double longitude = Double.valueOf(pair[1]);

        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;
    }

    public static final void viewLocation(Context context, Location loc) {
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", loc.getLatitude(), loc.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(intent);
    }
}
