package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.antilost.app.R;
import com.antilost.app.common.*;

public class TrackrUsereditor extends Activity implements TrackRInitialize {

    private Button btmBack, btmDone;
    private TextView tvtitle, tvuser_usericon;
    private RelativeLayout rluser_smallname, rluser_bord, rluser_xuexing,
            rluser_likes, rluser_qianming, rluser_homepage, rlusereditor_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.usereditor);
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
        btmBack = (Button) findViewById(R.id.mBtnCancel);
        btmDone = (Button) findViewById(R.id.mBtnDone);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        tvuser_usericon = (TextView) findViewById(R.id.tvusereditor_icon);
        rlusereditor_icon = (RelativeLayout) findViewById(R.id.rlusereditor_icon);
        rluser_smallname = (RelativeLayout) findViewById(R.id.rlusereditor_smallname);
        rluser_bord = (RelativeLayout) findViewById(R.id.rlusereditor_borad);
        rluser_xuexing = (RelativeLayout) findViewById(R.id.rlusereditor_xuexing);
        rluser_likes = (RelativeLayout) findViewById(R.id.rlusereditor_likes);
        rluser_qianming = (RelativeLayout) findViewById(R.id.rlusereditor_qianming);
        rluser_homepage = (RelativeLayout) findViewById(R.id.rlusereditor_homepage);

    }

    @Override
    public void initWidgetState() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initWidgetListener() {
        // TODO Auto-generated method stub
    }

    @Override
    public void addWidgetListener() {
        // TODO Auto-generated method stub
        btmBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(TrackrUsereditor.this, UserProfileActivity.class);
                startActivity(intent);
                TrackrUsereditor.this.finish();
            }
        });


    }

}
