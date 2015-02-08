package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.prefs.PrefsManager;

public class UserProfileActivity extends Activity  implements TrackRInitialize, CompoundButton.OnCheckedChangeListener {

    private ImageButton imgBack;
    private TextView tvtitle,tvuser_usericon;
    private RelativeLayout rluser_editor,rluser_noticetimer,rluser_language,
            rluser_tipback,rluser_background,rluser_version,rluser_topback,rluser_safezone;
    private Button btmexit;
    private CheckBox cbAppring,cbSafeZone;
    private PrefsManager mPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdata);
        mPrefsManager = PrefsManager.singleInstance(this);
        initWidget();
        initWidgetState();
        initWidgetListener();
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
        tvuser_usericon= (TextView)findViewById(R.id.tvusericon);
        rluser_editor = (RelativeLayout)findViewById(R.id.rluser_editor);
        rluser_noticetimer = (RelativeLayout)findViewById(R.id.rluser_notice);
        rluser_language = (RelativeLayout)findViewById(R.id.rluser_langauage);
        rluser_tipback = (RelativeLayout)findViewById(R.id.rluser_backtip);
        rluser_version = (RelativeLayout)findViewById(R.id.rluser_version);
        rluser_background= (RelativeLayout)findViewById(R.id.rluser_selectbackground);
        rluser_safezone= (RelativeLayout)findViewById(R.id.rluser_safezone);
        btmexit = (Button)findViewById(R.id.mbtnuserexit);
        cbAppring = (CheckBox)findViewById(R.id.cbuser_appringswitch);
        cbSafeZone = (CheckBox)findViewById(R.id.cbuser_safezoneswitch);


    }

    @Override
    public void initWidgetState() {
        // TODO Auto-generated method stub
        cbSafeZone.setChecked(mPrefsManager.getSafeZoneEnable());
        cbSafeZone.setChecked(mPrefsManager.getAlertRingEnabled());

    }

    @Override
    public void initWidgetListener() {
        // TODO Auto-generated method stub
        rluser_editor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(UserProfileActivity.this, TrackrUsereditor.class);
                startActivity(intent);
//				UserProfileActivity.this.finish();
            }
        });
        rluser_safezone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(UserProfileActivity.this, SafeZonewifiActivity.class);
                startActivity(intent);
//				UserProfileActivity.this.finish();
            }
        });
        btmexit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                UserProfileActivity.this.finish();
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(UserProfileActivity.this, MainTrackRListActivity.class);
                startActivity(intent);
                UserProfileActivity.this.finish();

            }
        });
        cbSafeZone.setOnCheckedChangeListener(this);
        cbAppring.setOnCheckedChangeListener(this);
    }

    @Override
    public void addWidgetListener() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        switch (id) {
            case R.id.cbuser_safezoneswitch:
                mPrefsManager.setSafeZoneEnable(b);
                break;
            case R.id.cbuser_appringswitch:
                mPrefsManager.setAlertRingEnabled(b);
                break;
        }
    }
}

