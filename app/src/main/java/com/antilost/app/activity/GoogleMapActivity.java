package com.antilost.app.activity;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.util.LocUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private MapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private Location mLostLocation;

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

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setBuildingsEnabled(false);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        MarkerOptions marker = new MarkerOptions();
        marker.title(getString(R.string.place_lost));
        LatLng latLng = new LatLng(mLostLocation.getLatitude(), mLostLocation.getLongitude());
        marker.position(latLng);

        mGoogleMap.addMarker(marker);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 21);
        mGoogleMap.moveCamera(cameraUpdate);
    }
}
