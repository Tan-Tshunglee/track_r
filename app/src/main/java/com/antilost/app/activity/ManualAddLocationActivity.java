package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.adapter.locationAdapter;
import com.antilost.app.dao.LocationTable;
import com.antilost.app.dao.TrackRDataBase;
import com.antilost.app.model.LocationBean;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.LocUtils;
import com.antilost.app.util.Utils;

import java.util.List;
import java.util.Locale;

public class ManualAddLocationActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_CODE_ADD_TRACK_R = 1;
    private static final String LOG_TAG = "ManualLocationActivity";


    private ListView mListView;
    private LocationManager mLocationManager;

    //数据库
    private TrackRDataBase trackRDataBase;
    /**
     * 数据库对象
     */
    private SQLiteDatabase mDb = null;

    /*列表 */
    private List<LocationBean> locationBeans;

    private locationAdapter locationadatper = null;

    private PrefsManager mPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localtion_list);
        mLocationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        mListView = (ListView) findViewById(R.id.lslocation);
        findViewById(R.id.btnlocatinBack).setOnClickListener(this);
        findViewById(R.id.btnlocationAdd).setOnClickListener(this);
        initdata();
    }

    private void initdata() {
        trackRDataBase = new TrackRDataBase(this);
        mDb = trackRDataBase.getWritDatabase();
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
                LocUtils.viewLocation(ManualAddLocationActivity.this, location, null);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "setOnItemLongClickListener");
                try {
                    modifyLocation(locationBeans.get(position));
                } catch (Exception ex) {
                    Log.d(LOG_TAG, ex.toString());
                }
                return true;
            }
        });
        if (LocationTable.getInstance().query(mDb) != null) {
            locationBeans = LocationTable.getInstance().query(mDb);
            locationadatper = new locationAdapter(this, locationBeans, mDb);
            mListView.setAdapter(locationadatper);
            Log.d(LOG_TAG, " mListView.setAdapter(locationadatper) ");

        }
        mPrefsManager = PrefsManager.singleInstance(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mDb != null) {
                mDb.close();
                mDb = null;
            }

            trackRDataBase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    // 重命名
                    case 0:
                        modifyLocationName(locationBean);
                        break;
                    // 删除
                    case 1:
                        deleteLocation(locationBean);
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
                    Utils.showShortToast(ManualAddLocationActivity.this, R.string.location_name_can_not_be_empty);
                    return;
                }
                locationBean.setmLocationName(sensorName);
                LocationTable.getInstance().update(mDb, locationBean);

            }
        });
        builder.show();
    }

    /**
     * 删除位置
     */
    private final void deleteLocation(final LocationBean locationBean) {
        LocationTable.getInstance().delete(mDb, locationBean);
        locationadatper.notifyDataSetChanged();
        if (LocationTable.getInstance().query(mDb) != null) {
            locationBeans = LocationTable.getInstance().query(mDb);
            locationadatper = new locationAdapter(ManualAddLocationActivity.this, locationBeans, mDb);
            mListView.setAdapter(locationadatper);
        } else {
            locationadatper = new locationAdapter(ManualAddLocationActivity.this, null, mDb);
            mListView.setAdapter(locationadatper);
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
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
        }, getMainLooper());
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnlocatinBack:
                finish();
                break;
            case R.id.btnlocationAdd:
                showLocationAddDialog();
                break;
        }
    }

    private void showLocationAddDialog() {
        final EditText inputServer = new EditText(ManualAddLocationActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(ManualAddLocationActivity.this);
        builder.setIcon(R.drawable.location);
        builder.setTitle(getString(R.string.locationsetting));
        builder.setView(inputServer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = inputServer.getText().toString().trim();
                        String timerStringday = new java.util.Date().toLocaleString();

                        if (TextUtils.isEmpty(name)) {
                            Utils.showShortToast(ManualAddLocationActivity.this, getString(R.string.location_name_can_not_be_empty));
                            return;
                        }
                        Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (lastLocation == null) {
                            lastLocation = mPrefsManager.getLastSavedLocation();
                        }
                        if (lastLocation == null) {
                            Toast.makeText(ManualAddLocationActivity.this, getString(R.string.unable_detemine_current_position), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            double lat = lastLocation.getLatitude();
                            double lng = lastLocation.getLongitude();

                            if(lat == 0 && lng == 0) {
                                return;
                            }
                            LocationTable.getInstance().insert(mDb, new LocationBean(name, timerStringday, (float) lastLocation.getLatitude(), (float)lastLocation.getLongitude()));
                        }


                        if (LocationTable.getInstance().query(mDb) != null) {
                            locationBeans = LocationTable.getInstance().query(mDb);
                            locationadatper = new locationAdapter(ManualAddLocationActivity.this, locationBeans, mDb);
                            mListView.setAdapter(locationadatper);
                            locationadatper.notifyDataSetChanged();
                        }
                    }
                }
        );
        builder.show();
    }
}
