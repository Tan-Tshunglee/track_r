package com.antilost.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.antilost.app.R;
import com.antilost.app.prefs.PrefsManager;

public class StartAndEndTimerPickerActivity extends Activity implements View.OnClickListener, TimePicker.OnTimeChangedListener {

    private Button mBtnCancel;
    private Button mBtnDone;
    private TextView mTextViewTitle;
    private TimePicker mStartTimePicker;
    private TimePicker mEndTimerPicker;
    private PrefsManager mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_and_end_timer_picker);
        mBtnCancel = (Button) findViewById(R.id.mBtnCancel);
        mBtnCancel.setText(R.string.cancel);
        mBtnDone = (Button) findViewById(R.id.mBtnDone);
        mTextViewTitle = (TextView) findViewById(R.id.mTVTitle);
        mTextViewTitle.setText(getString(R.string.sleep_mode_time_setting));

        mBtnCancel.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);

        mStartTimePicker = (TimePicker) findViewById(R.id.startTimePicker);
        mEndTimerPicker = (TimePicker) findViewById(R.id.endTimePicker);

        mStartTimePicker.setIs24HourView(true);
        mEndTimerPicker.setIs24HourView(true);

        mPrefs = PrefsManager.singleInstance(this);

        long startTime = mPrefs.getSleepTime(true);
        long endTime = mPrefs.getSleepTime(false);

        int startHour = (int) startTime / ( 1000 * 60 * 60 );
        int startMinute = (int) (startTime / (1000 * 60 )) % 60;

        mStartTimePicker.setCurrentHour(startHour);
        mStartTimePicker.setCurrentMinute(startMinute);

        int endHour = (int) endTime / ( 1000 * 60 * 60 );
        int endMinute = (int) (endTime / (1000 * 60)) % 60;

        mEndTimerPicker.setCurrentHour(endHour);
        mEndTimerPicker.setCurrentMinute(endMinute);

        mStartTimePicker.setOnTimeChangedListener(this);
        mEndTimerPicker.setOnTimeChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mBtnCancel:
                finish();
                break;
            case R.id.mBtnDone:

                int startHour = mStartTimePicker.getCurrentHour();
                int startMinute = mStartTimePicker.getCurrentMinute();

                int startTime  = (startHour * 60 + startMinute) * 60 * 1000;

                int endHour = mEndTimerPicker.getCurrentHour();
                int endMinute = mEndTimerPicker.getCurrentMinute();

                int endTime  = (endHour * 60 + endMinute) * 60 * 1000;

                mPrefs.saveSleepTime(true, startTime);
                mPrefs.saveSleepTime(false, endTime);
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
        switch (timePicker.getId()) {
            case R.id.startTimePicker:
                break;
            case R.id.endTimePicker:
                break;
        }
    }


}
