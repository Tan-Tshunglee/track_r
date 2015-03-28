package com.antilost.app.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.network.UserFeedbackCommand;
import com.antilost.app.prefs.PrefsManager;

public class FeedBackActivity extends Activity implements TrackRInitialize {

    private Button btmPush;
    private ImageButton btmBack;
    private EditText etfeedback;
    private TextView tvtitle;
    private PrefsManager mPrefManager;

    /**
     * 按键监听
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        initWidget();
        initWidgetState();
        initWidgetListener();
        addWidgetListener();
        initDataSource();

        mPrefManager = PrefsManager.singleInstance(this);
        if(!mPrefManager.validUserLog())  {
            Log.w("", "no valid user log");
        }
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
        btmBack = (ImageButton) findViewById(R.id.mBtnCancel);
        btmPush = (Button) findViewById(R.id.mbtnpush);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        etfeedback = (EditText) findViewById(R.id.etfeedback);
    }

    @Override
    public void initWidgetState() {
        tvtitle.setText(getResources().getString(R.string.feedback_title));
    }

    @Override
    public void initWidgetListener() {

    }

    @Override
    public void addWidgetListener() {
        btmBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FeedBackActivity.this.finish();
            }
        });

        btmPush.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String feedback = etfeedback.getText().toString().trim();

                if(TextUtils.isEmpty(feedback)) {
                    Toast.makeText(FeedBackActivity.this, getString(R.string.empty_feedback_can_not_be_submitted), Toast.LENGTH_SHORT).show();
                    return;
                }

                (new FeedBackUploader(mPrefManager.getEmail(), feedback)).execute();
            }
        });

    }

    private class FeedBackUploader extends AsyncTask<Void, Void, Void> {
        private final UserFeedbackCommand mUploadCommand;

        public FeedBackUploader(String email, String feedback) {
             mUploadCommand = new UserFeedbackCommand(email, feedback);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mUploadCommand.execTask();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(mUploadCommand.success()) {
                Toast.makeText(FeedBackActivity.this, getString(R.string.feed_back_success), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(FeedBackActivity.this, getString(R.string.feedback_failed), Toast.LENGTH_SHORT).show();
                mUploadCommand.dumpDebugInfo();
            }

        }
    }

}
