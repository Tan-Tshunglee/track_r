package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.*;

public class SafeZonewifi extends Activity implements TrackRInitialize {

	private ImageButton imgBack;
	private TextView tvtitle;
	private RelativeLayout rlsafezone_office,rlsafezone_home,rlsafezone_other,
	rluser_topback;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.safezone);
		initWidget();
		initWidgetState();
		initWidgetListener();
		addWidgetListener();
		initDataSource();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public void initDataSource() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initWidget() {
		// TODO Auto-generated method stub
		imgBack = (ImageButton)findViewById(R.id.mBtnCancel);
		tvtitle = (TextView)findViewById(R.id.mTVTitle);
		rluser_topback = (RelativeLayout)findViewById(R.id.rluser_topback);
		rlsafezone_home = (RelativeLayout)findViewById(R.id.rlsafezone_home);
		rlsafezone_other = (RelativeLayout)findViewById(R.id.rlsafezone_other);
		rlsafezone_office = (RelativeLayout)findViewById(R.id.rlsafezone_office);
		
	}

	@Override
	public void initWidgetState() {
		// TODO Auto-generated method stub
		tvtitle.setText(this.getResources().getString(R.string.safezone_title));
		
	}

	@Override
	public void initWidgetListener() {
		// TODO Auto-generated method stub
		imgBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SafeZonewifi.this, UserProfileActivity.class);
				startActivity(intent);
				SafeZonewifi.this.finish();
			}
		});
		rlsafezone_home.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SafeZonewifi.this, SafeZoneAddWifi.class);
				startActivity(intent);
				SafeZonewifi.this.finish();
			}
		});
		
		rlsafezone_office.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SafeZonewifi.this, SafeZoneAddWifi.class);
				startActivity(intent);
				SafeZonewifi.this.finish();
			}
		});
		
		rlsafezone_other.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SafeZonewifi.this, SafeZoneAddWifi.class);
				startActivity(intent);
				SafeZonewifi.this.finish();
			}
		});
	}

	@Override
	public void addWidgetListener() {
		// TODO Auto-generated method stub
		
	}

}
