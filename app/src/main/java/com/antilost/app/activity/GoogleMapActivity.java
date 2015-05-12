package com.antilost.app.activity;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.LocUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener{

    private MapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private Location mLostLocation;
    private LocationManager mLocationManager;
    private MarkerOptions mCurrentPositionMarker;
    private PrefsManager mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLostLocation = LocUtils.parseLocationUri(getIntent().getData());
        if(mLostLocation == null) {
            Toast.makeText(this, getString(R.string.no_location_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_google_map);
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20 * 1000, 20, this);
        mPrefs = PrefsManager.singleInstance(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setBuildingsEnabled(false);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        MarkerOptions marker = new MarkerOptions();
        if(mPrefs.isMissedTrack(getIntent().getStringExtra(LocUtils.DEVICE_ADDRESS))) {
            marker.title(getString(R.string.place_lost));
        }

        LatLng latLng = new LatLng(mLostLocation.getLatitude(), mLostLocation.getLongitude());
        marker.position(latLng);

        mGoogleMap.addMarker(marker);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 21);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mGoogleMap != null) {
            if(mCurrentPositionMarker == null) {
                mCurrentPositionMarker = new MarkerOptions();
                mCurrentPositionMarker.title(getString(R.string.you_are_here));
            }

            mCurrentPositionMarker.position(new LatLng(location.getLatitude(), location.getLongitude()));
            mGoogleMap.addMarker(mCurrentPositionMarker);
        }


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
