package com.antilost.app.activity;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;


import com.antilost.app.adapter.SafeZoneWifiListViewAdapter;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.util.myWiFi;

public class SafeZoneAddWifi extends Activity implements TrackRInitialize {
	private String TAG = "SafeZoneAddWifi";

	private ImageButton imgBack;
	private TextView tvtitle;
	private RelativeLayout rluser_topback;
	private ListView lvwifilist;
	private SafeZoneWifiListViewAdapter safezonelistadapter;
	private List<String> listwifi;
	private List<ScanResult> listScanWifi;
	private myWiFi mywifi;
	
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
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public void initDataSource() {
		// TODO Auto-generated method stub
		mywifi = new myWiFi(this);
		listwifi = new ArrayList<String>();
		mywifi.startScan();
		listScanWifi  = mywifi.getWifiList();
		if(listScanWifi!=null){
			for(int i=0;i<listScanWifi.size();i++){
				listwifi.add(listScanWifi.get(i).SSID.toString());
				Log.d(TAG, "the ssid wifi add to "+listwifi.get(i));
			}
			safezonelistadapter = new SafeZoneWifiListViewAdapter(this, listwifi);
			lvwifilist.setAdapter(safezonelistadapter);
		}
	
		
	}

	@Override
	public void initWidget() {
		// TODO Auto-generated method stub
		imgBack = (ImageButton)findViewById(R.id.mBtnCancel);
		tvtitle = (TextView)findViewById(R.id.mTVTitle);
		rluser_topback = (RelativeLayout)findViewById(R.id.rluser_topback);
		lvwifilist =  (ListView)findViewById(R.id.lvsafezoneaddwifi);
		
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
				Intent intent = new Intent(SafeZoneAddWifi.this, SafeZonewifi.class);
				startActivity(intent);
				SafeZoneAddWifi.this.finish();
			}
		});
		
	}

	@Override
	public void addWidgetListener() {
		// TODO Auto-generated method stub
		
	}

}
