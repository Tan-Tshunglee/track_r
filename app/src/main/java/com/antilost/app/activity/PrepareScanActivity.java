package com.antilost.app.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.antilost.app.R;

public class PrepareScanActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_bind);
        findViewById(R.id.mainAddBtn).setOnClickListener(this);
        findViewById(R.id.btnprepareback).setOnClickListener(this);
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultAdapter.enable();

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnprepareback:
                showMainTrackRListActivitye();
                break;
            case R.id.mainAddBtn:
                Intent i = new Intent(this, ScanTrackActivity.class);
                startActivity(i);
                finish();
                break;

        }
    }

    private void showMainTrackRListActivitye() {
//        Intent i = new Intent(this, MainTrackRListActivity.class);
//        startActivity(i);
        this.finish();

    }
}
