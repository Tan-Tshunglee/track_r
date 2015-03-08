package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.adapter.SafeZoneWifiListViewAdapter;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.WiFiManager;

import java.util.ArrayList;
import java.util.List;

public class SafeZoneAddWifiActivity extends Activity implements TrackRInitialize, ListView.OnItemClickListener {

    public static final String EXTRA_KEY_TARGET = "extra_key_target";
    public static final int TARGET_HOME = 1;
    public static final int TARGET_OFFICE = 2;
    public static final int TARGET_OTHER = 3;


    private String TAG = "SafeZoneAddWifi";

    private ImageButton imgBack;
    private TextView tvtitle;
    private RelativeLayout rluser_topback;
    private ListView lvwifilist;
    private SafeZoneWifiListViewAdapter safezonelistadapter;
    private List<String> listwifi;
    private List<ScanResult> listScanWifi;
    private WiFiManager mywifi;
    private PrefsManager mPrefsManager;
    private int mTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.safezoneaddwifi);
        initWidget();
        initWidgetState();
        initWidgetListener();
        initDataSource();
        addWidgetListener();
        mPrefsManager = PrefsManager.singleInstance(this);
        mTarget = getIntent().getIntExtra(EXTRA_KEY_TARGET, -1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public void initDataSource() {
        // TODO Auto-generated method stub
        mywifi = new WiFiManager(this);
        listwifi = new ArrayList<String>();
        mywifi.startScan();
        listScanWifi = mywifi.getWifiList();
        if (listScanWifi != null) {
            for (int i = 0; i < listScanWifi.size(); i++) {
                listwifi.add(listScanWifi.get(i).SSID.toString());
                Log.d(TAG, "the ssid wifi add to " + listwifi.get(i));
            }
            safezonelistadapter = new SafeZoneWifiListViewAdapter(this, listwifi);
            lvwifilist.setAdapter(safezonelistadapter);
        }


    }

    @Override
    public void initWidget() {
        // TODO Auto-generated method stub
        imgBack = (ImageButton) findViewById(R.id.mBtnCancel);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        rluser_topback = (RelativeLayout) findViewById(R.id.rluser_topback);
        lvwifilist = (ListView) findViewById(R.id.lvsafezoneaddwifi);

    }

    @Override
    public void initWidgetState() {
        // TODO Auto-generated method stub
        tvtitle.setText(this.getResources().getString(R.string.safezone_wifiselcettitle));

    }

    @Override
    public void initWidgetListener() {
        // TODO Auto-generated method stub
        imgBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(SafeZoneAddWifiActivity.this, SafeZonewifiActivity.class);
                startActivity(intent);
                SafeZoneAddWifiActivity.this.finish();
            }
        });

        lvwifilist.setOnItemClickListener(this);

    }

    @Override
    public void addWidgetListener() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(i > listwifi.size()) {
            Log.w(TAG, "item click position out of range");
        }

        String ssid = listwifi.get(i);
        Log.i(TAG, String.format("user set %d 's ssid is %s", mTarget, ssid));
        switch (mTarget) {
            case TARGET_HOME:
                mPrefsManager.setHomeWifiSsid(ssid);
                break;
            case TARGET_OFFICE:
                mPrefsManager.setOfficeSsid(ssid);
                break;
            case TARGET_OTHER:
                mPrefsManager.setOtherSsid(ssid);
                break;
        }
        Intent intent = new Intent(SafeZoneAddWifiActivity.this, SafeZonewifiActivity.class);
        startActivity(intent);
        SafeZoneAddWifiActivity.this.finish();
    }
}
