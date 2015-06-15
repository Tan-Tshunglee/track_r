package com.antilost.app.activity;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.antilost.app.R;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.LocUtils;
import com.antilost.app.util.Utils;

public class AmapActivity extends Activity implements AMapLocationListener, View.OnClickListener {

    public static final String LOG_TAG = "AmapActivity";
    private MapView mAmapView;
    private AMap mAmap;
    private PrefsManager mPrefsManager;
    private LocationManagerProxy mAMapLocationManager;
    private Marker mLocationMarker;
    private MarkerOptions mCurrentPositionOptions;
    private Marker mCurrentLocationMarker;
    private ImageButton mBackBtn;
    private TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap);

        mBackBtn = (ImageButton) findViewById(R.id.mBtnCancel);
        mBackBtn.setOnClickListener(this);
        mTitleText = (TextView) findViewById(R.id.mTVTitle);
        mTitleText.setText(R.string.location);
        mAmapView = (MapView) findViewById(R.id.map);
        mAmapView.onCreate(savedInstanceState);// 必须要写
        mAmap = mAmapView.getMap();
        Uri uri = getIntent().getData();

        if(uri == null) {
            Log.e(LOG_TAG, "uri data is null");
            finish();
            return;
        }
        mPrefsManager = PrefsManager.singleInstance(this);
        mAMapLocationManager = LocationManagerProxy.getInstance(this);
        String schema = uri.getScheme();
        String addressOrTitle = getIntent().getStringExtra(LocUtils.DEVICE_ADDRESS);
        if("geo".equalsIgnoreCase(schema)) {
            MarkerOptions markerOptions = new MarkerOptions();
            String uriStr = uri.toString();
            Log.i(LOG_TAG, "uri to string is " + uriStr);
            String latlngStr = uriStr.split(":")[1];
            String[] latlngArr = latlngStr.split(",");
            double lat = Double.parseDouble(latlngArr[0]);
            double lng = Double.parseDouble(latlngArr[1]);
            markerOptions.position(new LatLng(lat, lng));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
            if(Utils.isValidMacAddress(addressOrTitle)) {
                if(mPrefsManager.isMissedTrack(addressOrTitle)) {
                    Marker marker = mAmap.addMarker(markerOptions);
                    markerOptions.title(getString(R.string.place_lost));
                    marker.setPosition(new LatLng(lat, lng));
                    marker.setVisible(true);
                    mLocationMarker = marker;
                }
            } else {
                Marker marker = mAmap.addMarker(markerOptions);
                markerOptions.title(addressOrTitle);
                marker.setPosition(new LatLng(lat, lng));
                marker.setVisible(true);
                mLocationMarker = marker;

            }



            CameraUpdate camera = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
            mAmap.moveCamera(camera);
            //max zoom level
            camera = CameraUpdateFactory.zoomTo(20);
            mAmap.moveCamera(camera);


            if(Utils.isValidMacAddress(addressOrTitle)) {
                mCurrentPositionOptions = new MarkerOptions().title(getString(R.string.current_position));
                mCurrentPositionOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mCurrentPositionOptions.position(new LatLng(lat, lng));
                mCurrentLocationMarker = mAmap.addMarker(mCurrentPositionOptions);
                mCurrentLocationMarker.setVisible(true);
            }

        } else {
            Log.v(LOG_TAG, "Unknown uri schema." + uri);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAmapView.onResume();
        mAMapLocationManager.requestLocationData(
                LocationProviderProxy.AMapNetwork, 5  * 1000, 15, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAmapView.onPause();

        mAMapLocationManager.removeUpdates(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAmapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAmap.setMyLocationEnabled(false);
        mAmap.setLocationSource(null);
        mAmapView.onDestroy();
    }



    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        Log.d(LOG_TAG, "get location from amap");
        if(mCurrentLocationMarker != null) {
            Double geoLat = aMapLocation.getLatitude();
            Double geoLng = aMapLocation.getLongitude();

            mCurrentLocationMarker.setPosition(new LatLng(geoLat, geoLng));
            mCurrentLocationMarker.setVisible(true);
        }



    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtnCancel:
                finish();
                break;
        }
    }
}
