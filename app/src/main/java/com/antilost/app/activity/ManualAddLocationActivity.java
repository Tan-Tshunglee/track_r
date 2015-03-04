package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.antilost.app.R;
import com.antilost.app.dao.LocationTable;
import com.antilost.app.dao.TrackRDataBase;
import com.antilost.app.model.LocationBean;
import com.antilost.app.adapter.locationAdapter;
import java.text.SimpleDateFormat;
import java.util.List;

public class ManualAddLocationActivity extends Activity implements View.OnClickListener{

    private static final int REQUEST_CODE_ADD_TRACK_R = 1;
    private static final String LOG_TAG = "ManualAddLocationActivity";


    private ListView mListView;
    private  LocationManager GpsManager;

    //数据库
    private TrackRDataBase trackRDataBase;
    /** 数据库对象 */
    private SQLiteDatabase mDb = null;

    /*列表 */
    private List<LocationBean> locationBeans;

    private locationAdapter locationadatper= null;

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
        if(LocationTable.getInstance().query(mDb)==null){
            LocationTable.getInstance().insert(mDb,new LocationBean("HOME","2015年11月25日10时25分",(float)12.5,(float)12.5));
            LocationTable.getInstance().insert(mDb,new LocationBean("OFFICE","2015年11月25日08时26分",(float)12.5,(float)12.5));
        }
        if(LocationTable.getInstance().query(mDb)!=null){
            locationBeans =  LocationTable.getInstance().query(mDb);
            locationadatper = new locationAdapter(this,locationBeans);
            mListView.setAdapter(locationadatper);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                        String  Name = inputServer.getText().toString().trim();
                        String timerStringday =new SimpleDateFormat("yyyy年MM月dd日hh时mm分").format(new java.util.Date());

                        Location   location    = GpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(location==null){
                            LocationTable.getInstance().insert(mDb,new LocationBean(Name,timerStringday,(float)1222.3,(float)10.5));
                        }else{
                            LocationTable.getInstance().insert(mDb,new LocationBean(Name,timerStringday,(float)location.getLongitude(),(float)location.getLongitude()));
                        }

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
}
