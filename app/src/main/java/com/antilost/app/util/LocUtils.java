package com.antilost.app.util;

import android.location.Location;
import android.location.LocationManager;

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
}
