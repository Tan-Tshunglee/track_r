package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.antilost.app.prefs.PrefsManager;

public class SafeZonewifiActivity extends Activity implements TrackRInitialize {

    private static final int REQUEST_CODE_SET_HOME_WIFI = 1;
    private static final int REQUEST_CODE_SET_OFFICE_WIFI = 1;
    private static final int REQUEST_CODE_SET_OTHER_WIFI = 1;

    private ImageButton imgBack;
    private TextView tvtitle;
    private RelativeLayout rlsafezone_office, rlsafezone_home, rlsafezone_other,
            rluser_topback;
    private PrefsManager mPrefsManager;
    private TextView mHomeWifiSsid;
    private TextView mOfficeWifiSsid;
    private TextView mOtherWifiSsid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.safezone);

        mPrefsManager = PrefsManager.singleInstance(this);
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
        imgBack = (ImageButton) findViewById(R.id.mBtnCancel);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        rluser_topback = (RelativeLayout) findViewById(R.id.rluser_topback);
        rlsafezone_home = (RelativeLayout) findViewById(R.id.rlsafezone_home);
        rlsafezone_other = (RelativeLayout) findViewById(R.id.rlsafezone_other);
        rlsafezone_office = (RelativeLayout) findViewById(R.id.rlsafezone_office);

        mHomeWifiSsid = (TextView) findViewById(R.id.tvsafezonehomeadd);
        mOfficeWifiSsid = (TextView) findViewById(R.id.tvsafezone_officeadd);
        mOtherWifiSsid = (TextView) findViewById(R.id.tvsafezone_otheradd);
    }

    @Override
    public void initWidgetState() {
        // TODO Auto-generated method stub
        tvtitle.setText(this.getResources().getString(R.string.safezone_title));
        String homeSsid = mPrefsManager.getHomeWifiSsid();
        if(!TextUtils.isEmpty(homeSsid)) {
            mHomeWifiSsid.setText(homeSsid);
        }

        String officeSsid = mPrefsManager.getOfficeSsid();
        if(!TextUtils.isEmpty(officeSsid)) {
            mOfficeWifiSsid.setText(officeSsid);
        }

        String otherSsid = mPrefsManager.getOtherSsid();
        if(!TextUtils.isEmpty(otherSsid)) {
            mOtherWifiSsid.setText(otherSsid);
        }
    }

    @Override
    public void initWidgetListener() {
        // TODO Auto-generated method stub
        imgBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(SafeZonewifiActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });
        rlsafezone_home.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(SafeZonewifiActivity.this, SafeZoneAddWifiActivity.class);
                intent.putExtra(SafeZoneAddWifiActivity.EXTRA_KEY_TARGET, SafeZoneAddWifiActivity.TARGET_HOME);
                startActivity(intent);

            }
        });

        rlsafezone_office.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(SafeZonewifiActivity.this, SafeZoneAddWifiActivity.class);
                intent.putExtra(SafeZoneAddWifiActivity.EXTRA_KEY_TARGET, SafeZoneAddWifiActivity.TARGET_OFFICE);
                startActivity(intent);
            }
        });

        rlsafezone_other.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(SafeZonewifiActivity.this, SafeZoneAddWifiActivity.class);
                intent.putExtra(SafeZoneAddWifiActivity.EXTRA_KEY_TARGET, SafeZoneAddWifiActivity.TARGET_OTHER);
                startActivity(intent);
            }
        });
    }

    @Override
    public void addWidgetListener() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
