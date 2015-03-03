package com.antilost.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;

public class FeedBackEditor extends Activity implements TrackRInitialize {

    private Button  btmPush;
    private ImageButton btmBack;
    private EditText etfeedback;
    private TextView tvtitle;
    /** 按键监听*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.feedback);
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
        btmBack = (ImageButton) findViewById(R.id.mBtnCancel);
        btmPush = (Button) findViewById(R.id.mbtnpush);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        etfeedback = (EditText) findViewById(R.id.etfeedback);


    }

    @Override
    public void initWidgetState() {
        // TODO Auto-generated method stub
        tvtitle.setText(getResources().getString(R.string.feedback_title));
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
//                Intent intent = new Intent(FeedBackEditor.this, UserProfileActivity.class);
//                startActivity(intent);
                FeedBackEditor.this.finish();
            }
        });

        btmPush.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
//                Intent intent = new Intent(FeedBackEditor.this, UserProfileActivity.class);
//                startActivity(intent);

                FeedBackEditor.this.finish();
            }
        });

    }


}
