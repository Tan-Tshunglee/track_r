package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.antilost.app.R;
import com.antilost.app.adapter.locationAdapter;
import com.antilost.app.dao.LocationTable;
import com.antilost.app.dao.TrackRDataBase;
import com.antilost.app.model.LocationBean;
import com.antilost.app.prefs.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ManualAddLocationActivity extends Activity implements View.OnClickListener,AMapLocationListener {

    private static final int REQUEST_CODE_ADD_TRACK_R = 1;
    private static final String LOG_TAG = "ManualLocationActivity";


    private ListView mListView;
    private LocationManager GpsManager;

    //数据库
    private TrackRDataBase trackRDataBase;
    /** 数据库对象 */
    private SQLiteDatabase mDb = null;

    /*列表 */
    private List<LocationBean> locationBeans;

    private locationAdapter locationadatper= null;

    LocationManagerProxy mLocationManagerProxy;
    Location location = null;

    private PrefsManager mPrefsManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localtion_list);
        GpsManager = (LocationManager)getSystemService(this.LOCATION_SERVICE);
        mListView = (ListView) findViewById(R.id.lslocation);
        findViewById(R.id.btnlocatinBack).setOnClickListener(this);
        findViewById(R.id.btnlocationAdd).setOnClickListener(this);
        initdata();



    }
    private void initdata(){
        trackRDataBase = new TrackRDataBase(this);
        mDb = trackRDataBase.getWritDatabase();
        //debug
//        if(LocationTable.getInstance().query(mDb)==null){
//            LocationTable.getInstance().insert(mDb,new LocationBean("HOME","2015年11月25日10时25分",(float)12.5,(float)12.5));
//            LocationTable.getInstance().insert(mDb,new LocationBean("OFFICE","2015年11月25日08时26分",(float)12.5,(float)12.5));
//        }
        if(LocationTable.getInstance().query(mDb)!=null){
            locationBeans =  LocationTable.getInstance().query(mDb);
            locationadatper = new locationAdapter(this,locationBeans);
            mListView.setAdapter(locationadatper);
        }
        mPrefsManager = PrefsManager.singleInstance(this);
        //debug
//        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
//        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork,-1, 15, this);




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mLocationManagerProxy.destroy();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnlocatinBack:
                back();
                break;
            case R.id.btnlocationAdd:
                Dailog();
                break;
        }
    }
    private void Dailog(){


        final EditText inputServer = new EditText(ManualAddLocationActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(ManualAddLocationActivity.this);
        builder.setIcon(R.drawable.location);
        builder.setTitle(getString(R.string.locationsetting));
        builder.setView(inputServer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.d(LOG_TAG,"the string is "+mPrefsManager.getHomeWifiSsid());

                        String  Name = inputServer.getText().toString().trim();
                        String timerStringday =new SimpleDateFormat("yyyy年MM月dd日hh时mm分").format(new java.util.Date());

                        Location location    = GpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(location==null){
                            LocationTable.getInstance().insert(mDb,new LocationBean(Name,timerStringday,(float)1222.3,(float)10.5));
                        }else{
//                            LocationTable.getInstance().insert(mDb,new LocationBean(Name,timerStringday,(float)location.getLongitude(),(float)location.getLongitude()));
                              LocationTable.getInstance().insert(mDb,new LocationBean(Name,timerStringday,(float)mPrefsManager.getLastAMPALocation().getLatitude(),(float)mPrefsManager.getLastAMPALocation().getLongitude()));
                            Log.d(LOG_TAG,"111 the getLastAMPALocation().getLatitude() "+(float)mPrefsManager.getLastAMPALocation().getLatitude()+"getLongitude:"+(float)mPrefsManager.getLastAMPALocation().getLongitude());
                        }
                        Log.d(LOG_TAG,"222 the getLastAMPALocation().getLatitude() "+(float)mPrefsManager.getLastAMPALocation().getLatitude()+"getLongitude:"+(float)mPrefsManager.getLastAMPALocation().getLongitude());
                        if(LocationTable.getInstance().query(mDb)!=null){
                            locationBeans =  LocationTable.getInstance().query(mDb);
                            locationadatper = new locationAdapter(ManualAddLocationActivity.this,locationBeans);
                            mListView.setAdapter(locationadatper);
                        }
                    }
                }
        );
        builder.show();

    }

    private void back(){
        finish();
    }



    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG,"the location is the lat is "+location.getLatitude()+ "  the long is "+location.getLongitude());
        this.location =location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(AMapLocation arg0) {
        //定位回调
        if(arg0!=null&&arg0.getAMapException().getErrorCode() == 0){
            Log.e(LOG_TAG, arg0.toString());


            //debug
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f",arg0.getLatitude(),arg0.getLongitude());
//                        Uri uri = Uri.parse("geo:38.899533,-77.036476");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent = new Intent(ManualAddLocationActivity.this, AmapActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(uri));
                                    try {
                            ManualAddLocationActivity.this.startActivity(intent);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
        }

    }
}
