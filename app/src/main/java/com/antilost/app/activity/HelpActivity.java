package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.prefs.PrefsManager;

public class HelpActivity extends Activity implements TrackRInitialize {


    private ImageButton imgBack;
    private TextView tvtitle;
    private RelativeLayout rlhelp_privacity, rlhelp_use,
            rluser_topback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);

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

    }

    @Override
    public void initWidget() {
        imgBack = (ImageButton) findViewById(R.id.mBtnCancel);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        rluser_topback = (RelativeLayout) findViewById(R.id.rluser_topback);
        rlhelp_use = (RelativeLayout) findViewById(R.id.rlhelp_use);
        rlhelp_privacity = (RelativeLayout) findViewById(R.id.rlhelp_privacity);

    }

    @Override
    public void initWidgetState() {
        tvtitle.setText(this.getResources().getString(R.string.help_title));
    }

    @Override
    public void initWidgetListener() {
        imgBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                HelpActivity.this.finish();
            }
        });
        rlhelp_use.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Uri uri = Uri.parse("http://wifi.360.cn/howto.html");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
            }
        });

        rlhelp_privacity.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                 Uri uri = Uri.parse("http://www.chofn.com/ysxy.html");
                 Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
            }
        });

    }

    @Override
    public void addWidgetListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        initWidgetState();
    }

}
