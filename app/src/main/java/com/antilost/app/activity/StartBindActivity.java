package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.antilost.app.R;

public class StartBindActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_bind);
        findViewById(R.id.mainAddBtn).setOnClickListener(this);
        findViewById(R.id.btnUserProfile).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnUserProfile:
                break;
            case R.id.mainAddBtn:
                Intent i = new Intent(this, BindingTrackRActivity.class);
                startActivity(i);
                finish();
                break;

        }
    }
}
