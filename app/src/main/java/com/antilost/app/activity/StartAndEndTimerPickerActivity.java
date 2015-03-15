package com.antilost.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.antilost.app.R;

public class StartAndEndTimerPickerActivity extends Activity implements View.OnClickListener {

    private Button mBtnCancel;
    private Button mBtnDone;
    private TextView mTextViewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_and_end_timer_picker);
        mBtnCancel = (Button) findViewById(R.id.mBtnCancel);
        mBtnDone = (Button) findViewById(R.id.mBtnDone);
        mTextViewTitle = (TextView) findViewById(R.id.mTVTitle);
        mTextViewTitle.setText(getString(R.string.sleep_mode_time_setting));

        mBtnCancel.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mBtnCancel:
                finish();
                break;
            case R.id.mBtnDone:
                break;
        }
    }
}
