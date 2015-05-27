package com.antilost.app.activity;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.antilost.app.R;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.LocUtils;

public class AmapActivity extends Activity implements LocationSource, AMapLocationListener {

    public static final String LOG_TAG = "AmapActivity";
    private MapView mAmapView;
    private AMap mAmap;
    private PrefsManager mPrefsManager;
    private OnLocationChangedListener mOnLocationChangedListener;
    private LocationManagerProxy mAMapLocationManager;

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
        mPrefsManager = PrefsManager.singleInstance(this);
        String schema = uri.getScheme();
        String address = getIntent().getStringExtra(LocUtils.DEVICE_ADDRESS);
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
            if(mPrefsManager.isMissedTrack(address)) {
                markerOptions.title(getString(R.string.place_lost));
            }
            Marker marker = mAmap.addMarker(markerOptions);
            marker.setPosition(new LatLng(lat, lng));
            marker.setVisible(true);
            CameraUpdate camera = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
            mAmap.moveCamera(camera);
            //max zoom level
            camera = CameraUpdateFactory.zoomTo(20);
            mAmap.moveCamera(camera);


            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.strokeColor(Color.YELLOW);// 设置圆形的边框颜色
            myLocationStyle.radiusFillColor(Color.argb(100, 0, 256, 0));// 设置圆形的填充颜色
            myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
            mAmap.setMyLocationStyle(myLocationStyle);


            mAmap.setLocationSource(this);
            mAmap.setMyLocationEnabled(true);
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
        mAmap.setMyLocationEnabled(false);
        mAmap.setLocationSource(null);
        mAmapView.onDestroy();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        Log.d(LOG_TAG, "activate location source");
        mOnLocationChangedListener = onLocationChangedListener;

        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);

        }

        mAMapLocationManager.requestLocationUpdates(
                LocationProviderProxy.AMapNetwork, 2000, 10, this);
    }

    @Override
    public void deactivate() {

        Log.d(LOG_TAG, "deactivate location source");
        mOnLocationChangedListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destory();
        }
        mAMapLocationManager = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mOnLocationChangedListener != null && aMapLocation != null) {
            Log.v(LOG_TAG, "onLocationChanged set to mOnLocationChangedListener.");
            mOnLocationChangedListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
        } else {
            Log.v(LOG_TAG, "cannot update my current location im amap.");
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
}
