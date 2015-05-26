package com.antilost.app.util;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.antilost.app.activity.AmapActivity;
import com.antilost.app.activity.GoogleMapActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Locale;

//import com.baidu.location.BDLocation;

/**
 * Created by Tan on 2015/2/12.
 */
public class LocUtils {

    public static final String LOG_TAG = "LocUtils";
    public static final String DEVICE_ADDRESS = "device_address";

    public static  final String convertLocation(Location loc) {
        if(loc == null) {
            return null;
        }
        return String.format("%f-%f", loc.getLatitude(), loc.getLongitude());
    }

    public static final Location convertLocation(String loc) {
        if(loc == null) {
            return null;
        }
        Location location = null;
        try {
            String[] pair = loc.split("-");
            location = new Location(LocationManager.NETWORK_PROVIDER);
            double latitude = Double.valueOf(pair[0]);
            double longitude = Double.valueOf(pair[1]);

            location.setLatitude(latitude);
            location.setLongitude(longitude);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

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

//    public static Location convertBaiduLocation(BDLocation bdLocation) {
//
//        if(bdLocation != null) {
//            Location loc = new Location(LocationManager.NETWORK_PROVIDER);
//            loc.setLatitude(bdLocation.getLatitude());
//            loc.setLongitude(bdLocation.getLongitude());
//        }
//
//        return null;
//    }

    public static final void viewLocation(Context context, Location loc, String address) {
        if (loc == null) {
            Log.e(LOG_TAG, "view null location.");
            return;
        }

        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", loc.getLatitude(), loc.getLongitude());
        Intent intent;

        int googleServiceAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if(googleServiceAvailable == ConnectionResult.SUCCESS) {
            Log.i(LOG_TAG, "device with updated google play service use google map");
            intent = new Intent(context, GoogleMapActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(DEVICE_ADDRESS, address);
            intent.setData(Uri.parse(uri));
            try {
                context.startActivity(intent);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            Log.i(LOG_TAG, "device without google play service use amap");
            intent = new Intent(context, AmapActivity.class);
            intent.putExtra(DEVICE_ADDRESS, address);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));

            try {
                context.startActivity(intent);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }



    }

    public static Location parseLocationUri(Uri uri) {

        if(uri == null) {
            return null;
        }
        String schema = uri.getScheme();

        if("geo".equalsIgnoreCase(schema)) {
            String uriStr = uri.toString();
            String latlngStr = uriStr.split(":")[1];
            String[] latlngArr = latlngStr.split(",");
            double lat = Double.parseDouble(latlngArr[0]);
            double lng = Double.parseDouble(latlngArr[1]);

            Location loc = new Location(LocationManager.PASSIVE_PROVIDER);
            loc.setLatitude(lat);
            loc.setLongitude(lng);

            return loc;
        }
        return null;
    }

}
