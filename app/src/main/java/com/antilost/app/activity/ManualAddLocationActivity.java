package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.antilost.app.R;
import com.antilost.app.adapter.locationAdapter;
import com.antilost.app.dao.LocationTable;
import com.antilost.app.dao.TrackRDataBase;
import com.antilost.app.model.LocationBean;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.LocUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ManualAddLocationActivity extends Activity implements View.OnClickListener {

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
            locationadatper = new locationAdapter(this,locationBeans,mDb);
            mListView.setAdapter(locationadatper);
            Log.d(LOG_TAG, " mListView.setAdapter(locationadatper) ");
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int mposition = position;

                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f", locationBeans.get(mposition).getMlatitude(), locationBeans.get(mposition).getMlongitude());
//                        Uri uri = Uri.parse("geo:38.899533,-77.036476");
                    Log.d(LOG_TAG, "the string is " + uri);


                    Location location = new Location(LocationManager.NETWORK_PROVIDER);
                    double longitude = Double.valueOf(locationBeans.get(mposition).getMlatitude());
                    double latitude = Double.valueOf(locationBeans.get(mposition).getMlongitude());

                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    LocUtils.viewLocation(ManualAddLocationActivity.this, location);
                }
            });
            mListView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        modifyLocation(locationBeans.get(position));
                    } catch (Exception ex) {
                        Log.d(LOG_TAG, ex.toString());
                    }
                    return true;
                }
            });
        }
        mPrefsManager = PrefsManager.singleInstance(this);
        //debug
//        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
//        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork,-1, 15, this);
    }



    /**
     */
    public boolean modifyLocation(final LocationBean locationBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManualAddLocationActivity.this);
        builder.setTitle(locationBean.getmLocationName());
        builder.setItems(R.array.modify_location, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    // 重命�?
                    case 0:
                        modifyLocationName(locationBean);
                        break;
                    // 删除
                    case 1:
                        deleteSensor(locationBean);
                        break;
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        Dialog d = builder.show();
        d.setCanceledOnTouchOutside(true);
        return false;
    }

    /**
     * 修改名字
     *
     */
    private final void modifyLocationName(final LocationBean locationBean) {
        final EditText inputServer = new EditText(ManualAddLocationActivity.this);
        inputServer.setText(locationBean.getmLocationName());
        inputServer.setSelection(locationBean.getmLocationName().length());
        AlertDialog.Builder builder = new AlertDialog.Builder(ManualAddLocationActivity.this);
        builder.setTitle(R.string.modify_locationname);
        builder.setView(inputServer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sensorName = inputServer.getText().toString();
                if (TextUtils.isEmpty(sensorName)) {
                    Toast.makeText(ManualAddLocationActivity.this, R.string.modify_locationname_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                locationBean.setmLocationName(sensorName);
                LocationTable.getInstance().update(mDb, locationBean);

            }
        });
        builder.show();
    }

    /**
     * 删除房间
     *
     */
    private final void deleteSensor(final LocationBean locationBean) {
        LocationTable.getInstance().delete(mDb, locationBean);
        locationadatper.notifyDataSetChanged();
        if(LocationTable.getInstance().query(mDb)!=null){
            locationBeans =  LocationTable.getInstance().query(mDb);
            locationadatper = new locationAdapter(ManualAddLocationActivity.this,locationBeans,mDb);
            mListView.setAdapter(locationadatper);
        }else{
            locationadatper = new locationAdapter(ManualAddLocationActivity.this,null,mDb);
            mListView.setAdapter(locationadatper);
        }

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

//                        Location location    = GpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(mPrefsManager.getLastAMPALocation()==null){
                            LocationTable.getInstance().insert(mDb,new LocationBean(Name,timerStringday,(float)1222.3,(float)10.5));
                            Log.d(LOG_TAG,"the location is null");
                        }else{
//                            LocationTable.getInstance().insert(mDb,new LocationBean(Name,timerStringday,(float)location.getLongitude(),(float)location.getLongitude()));
                              LocationTable.getInstance().insert(mDb,new LocationBean(Name,timerStringday,(float)mPrefsManager.getLastAMPALocation().getLatitude(),(float)mPrefsManager.getLastAMPALocation().getLongitude()));
                            Log.d(LOG_TAG,"111 the getLastAMPALocation().getLatitude() "+(float)mPrefsManager.getLastAMPALocation().getLatitude()+"getLongitude:"+(float)mPrefsManager.getLastAMPALocation().getLongitude());
                        }
//                        Log.d(LOG_TAG,"222 the getLastAMPALocation().getLatitude() "+(float)mPrefsManager.getLastAMPALocation().getLatitude()+"getLongitude:"+(float)mPrefsManager.getLastAMPALocation().getLongitude());
                        if(LocationTable.getInstance().query(mDb)!=null){
                            locationBeans =  LocationTable.getInstance().query(mDb);
                            locationadatper = new locationAdapter(ManualAddLocationActivity.this,locationBeans,mDb);
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




    public boolean onKeyDown(int keyCode,KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出 退出确认框
            ManualAddLocationActivity.this.finish();
            return true;
        }
        return true;
    }
}
