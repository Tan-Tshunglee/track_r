package com.antilost.app.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAmapView.onResume();

        Uri uri = getIntent().getData();

        if(uri == null) {
            Log.e(LOG_TAG, "uri data is null");
            return;
        }


        String schema = uri.getScheme();

        if("geo:".equalsIgnoreCase(schema)) {
            MarkerOptions markerOptions = new MarkerOptions();
            String uriStr = uri.toString();
            String latlngStr = uriStr.split(":")[1];
            String[] latlngArr = latlngStr.split(",");
            double lat = Double.parseDouble(latlngArr[0]);
            double lng = Double.parseDouble(latlngArr[1]);
            markerOptions.position(new LatLng(lat, lng));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            markerOptions.title(getString(R.string.place_lost));
            mAmap.addMarker(markerOptions);
        } else {
            Log.v(LOG_TAG, "unknown uri schema.");
        }

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
