package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;

import java.util.Locale;

public class HelpActivity extends Activity implements TrackRInitialize {

    private String TAG = "HelpActivity";
    private ImageButton imgBack;
    private TextView tvtitle;
    private RelativeLayout rlhelp_privacity, rlhelp_use,rlterms_use,
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
        rlterms_use =  (RelativeLayout) findViewById(R.id.rlterms_use);
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
                Locale l = Locale.getDefault();
                String language = l.getLanguage();
                Log.d(TAG, "The Language is " + language);
                String whichCountry = null;
                if(language.equals("zh")){
                    whichCountry= "chn/term.html";
                }else if(language.equals("en")){
                    whichCountry= "eng/term.html";
                }else if(language.equals("fr")){
                    whichCountry= "fra/term.html";
                }
//                Uri uri = Uri.parse("http://www.ieasytec.com/help/"+whichCountry);
//                Intent it = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(it);
                Intent it = new Intent(HelpActivity.this, HelpWebView.class);
                it.putExtra("URL","http://www.ieasytec.com/help/" + whichCountry);
                startActivity(it);
            }
        });

        rlhelp_privacity.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Locale l = Locale.getDefault();
                String language = l.getLanguage();
                Log.d(TAG, "The Language is " + language);
                String whichCountry = null;
                if(language.equals("zh")){
                    whichCountry= "chn/policy.html";
                }else if(language.equals("en")){
                    whichCountry= "eng/policy.html";
                }else if(language.equals("fr")){
                    whichCountry= "fra/policy.html";
                }
//                Uri uri = Uri.parse("http://www.ieasytec.com/help/"+whichCountry);
//
//                 Intent it = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(it);
                Intent it = new Intent(HelpActivity.this, HelpWebView.class);
                it.putExtra("URL","http://www.ieasytec.com/help/" + whichCountry);
                startActivity(it);


            }
        });
        rlterms_use.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Locale l = Locale.getDefault();
                String language = l.getLanguage();
                Log.d(TAG, "The Language is " + language);
                String whichCountry = null;
                if(language.equals("zh")){
                    whichCountry= "chn/home.html";
                }else if(language.equals("en")){
                    whichCountry= "eng/home.html";
                }else if(language.equals("fr")){
                    whichCountry= "fra/home.html";
                }
//                Uri uri = Uri.parse("http://www.ieasytec.com/help/"+whichCountry);
//                Intent it = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(it);
                Intent it = new Intent(HelpActivity.this, HelpWebView.class);
                it.putExtra("URL","http://www.ieasytec.com/help/" + whichCountry);
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
