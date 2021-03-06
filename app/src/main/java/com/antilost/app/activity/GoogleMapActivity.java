package com.antilost.app.activity;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.LocUtils;
import com.antilost.app.util.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {

    private MapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private Location mLostLocation;
    private LocationManager mLocationManager;
    private MarkerOptions mCurrentPositionMarker;
    private PrefsManager mPrefs;
    private Uri mData;
    private ImageButton mBackBtn;
    private TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mData = getIntent().getData();
        mLostLocation = LocUtils.parseLocationUri(getIntent().getData());



        if(mLostLocation == null) {
            Toast.makeText(this, getString(R.string.no_location_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_google_map);

        mBackBtn = (ImageButton) findViewById(R.id.mBtnCancel);
        mBackBtn.setOnClickListener(this);
        mTitleText = (TextView) findViewById(R.id.mTVTitle);
        mTitleText.setText(R.string.location);

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
        LatLng latLng = new LatLng(mLostLocation.getLatitude(), mLostLocation.getLongitude());

        String addressOrTitle = getIntent().getStringExtra(LocUtils.DEVICE_ADDRESS);

        if(Utils.isValidMacAddress(addressOrTitle))  {
            if(mPrefs.isMissedTrack(addressOrTitle)) {
                marker.title(getString(R.string.place_lost));
                marker.position(latLng);
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.itrackpro));
                mGoogleMap.addMarker(marker);
            }
        } else {
            marker.title(addressOrTitle);
            marker.position(latLng);
            mGoogleMap.addMarker(marker);
        }


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onLocationChanged(Location location) {
        String addressOrTitle = getIntent().getStringExtra(LocUtils.DEVICE_ADDRESS);
        if(Utils.isValidMacAddress(addressOrTitle)) {
            if(mGoogleMap != null) {
                if(mCurrentPositionMarker == null) {
                    mCurrentPositionMarker = new MarkerOptions();
                    mCurrentPositionMarker.title(getString(R.string.you_are_here));
                    mCurrentPositionMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }

                mCurrentPositionMarker.position(new LatLng(location.getLatitude(), location.getLongitude()));
                mGoogleMap.addMarker(mCurrentPositionMarker);
            }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtnCancel:
                finish();
                break;
        }
    }
}
