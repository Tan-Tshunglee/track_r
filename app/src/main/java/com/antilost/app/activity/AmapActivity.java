package com.antilost.app.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.antilost.app.R;

public class AmapActivity extends Activity {

    public static final String LOG_TAG = "AmapActivity";
    private MapView mAmapView;
    private AMap mAmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap);

        mAmapView = (MapView) findViewById(R.id.map);
        mAmapView.onCreate(savedInstanceState);// 必须要写
        mAmap = mAmapView.getMap();
        Uri uri = getIntent().getData();

        if(uri == null) {
            Log.e(LOG_TAG, "uri data is null");
            finish();
            return;
        }
        String schema = uri.getScheme();

        if("geo".equalsIgnoreCase(schema)) {
            MarkerOptions markerOptions = new MarkerOptions();
            String uriStr = uri.toString();
            Log.i(LOG_TAG, "uri to string is " + uriStr);
            String latlngStr = uriStr.split(":")[1];
            String[] latlngArr = latlngStr.split(",");
            double lat = Double.parseDouble(latlngArr[0]);
            double lng = Double.parseDouble(latlngArr[1]);
            markerOptions.position(new LatLng(lat, lng));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            markerOptions.title(getString(R.string.place_lost));
            Marker marker = mAmap.addMarker(markerOptions);
            marker.setPosition(new LatLng(lat, lng));
            marker.setVisible(true);
            CameraUpdate camera = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
            mAmap.moveCamera(camera);
            //max zoom level
            camera = CameraUpdateFactory.zoomTo(20);
            mAmap.moveCamera(camera);
        } else {
            Log.v(LOG_TAG, "unknown uri schema." + uri);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAmapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAmapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAmapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAmapView.onDestroy();
    }

}
