package com.antilost.app.util;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.antilost.app.activity.AmapActivity;
import com.baidu.location.BDLocation;

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
        Location location = new Location(LocationManager.NETWORK_PROVIDER);
        double latitude = Double.valueOf(pair[0]);
        double longitude = Double.valueOf(pair[1]);

        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;
    }

    public static final Location convertAmapLocation(AMapLocation amapLocation) {
        if(amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0){
            //获取位置信息
            double geoLat = amapLocation.getLatitude();
            double geoLng = amapLocation.getLongitude();

            Location location = new Location(LocationManager.NETWORK_PROVIDER);
            location.setLatitude(geoLat);
            location.setLongitude(geoLng);

            return location;
        }
        return null;
    }

    public static Location convertBaiduLocation(BDLocation bdLocation) {

        if(bdLocation != null) {
            Location loc = new Location(LocationManager.NETWORK_PROVIDER);
            loc.setLatitude(bdLocation.getLatitude());
            loc.setLongitude(bdLocation.getLongitude());
        }

        return null;
    }

    public static final void viewLocation(Context context, Location loc) {
        if (loc == null) {
            Log.e("LocUtils", "view null location.");
            return;
        }
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", loc.getLatitude(), loc.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//        try {
//            context.startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        intent = new Intent(context, AmapActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));

        try {
            context.startActivity(intent);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
