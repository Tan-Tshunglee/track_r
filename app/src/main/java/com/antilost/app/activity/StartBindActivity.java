package com.antilost.app.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.antilost.app.R;

public class StartBindActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_bind);
        findViewById(R.id.mainAddBtn).setOnClickListener(this);
        findViewById(R.id.btnUserProfile).setOnClickListener(this);
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultAdapter.enable();

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnUserProfile:
                showUserProfile();
                break;
            case R.id.mainAddBtn:
                Intent i = new Intent(this, BindingTrackRActivity.class);
                startActivity(i);
                finish();
                break;

        }
    }

    private void showUserProfile() {
        Intent i = new Intent(this, UserProfileActivity.class);
        startActivity(i);
        this.finish();

    }
}
