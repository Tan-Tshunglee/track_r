package com.antilost.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.antilost.app.R;

public class ManualAddLocationActivity extends Activity implements View.OnClickListener{

    private static final int REQUEST_CODE_ADD_TRACK_R = 1;
    private static final String LOG_TAG = "ManualAddLocationActivity";


    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localtion_list);
        mListView = (ListView) findViewById(R.id.lslocation);
        findViewById(R.id.btnlocatinBack).setOnClickListener(this);
        findViewById(R.id.btnlocationAdd).setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnlocatinBack:
                back();
                break;
            case R.id.btnlocationAdd:
                break;
        }
    }
    private void back(){
        finish();
    }
}
