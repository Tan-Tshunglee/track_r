package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;

public class TrackrUsereditor extends Activity implements TrackRInitialize {

    private Button btmBack, btmDone;
    private TextView tvtitle, tvuser_usericon;
    private RelativeLayout rluser_smallname, rluser_bord, rluser_xuexing,
            rluser_likes, rluser_qianming, rluser_homepage, rlusereditor_icon;
    private TextView tvusereditor_name,tvusereditor_board,tvusereditorxuexing,tvusereditorlikes,tvSignature,tvusereditorhomepage;
    /** �������*/
    private BtnListener mBtnListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.usereditor);
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
        btmBack = (Button) findViewById(R.id.mBtnCancel);
        btmDone = (Button) findViewById(R.id.mBtnDone);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        tvuser_usericon = (TextView) findViewById(R.id.tvusereditor_icon);
        rlusereditor_icon = (RelativeLayout) findViewById(R.id.rlusereditor_icon);
        rluser_smallname = (RelativeLayout) findViewById(R.id.rlusereditor_smallname);
        rluser_bord = (RelativeLayout) findViewById(R.id.rlusereditor_borad);
        rluser_xuexing = (RelativeLayout) findViewById(R.id.rlusereditor_xuexing);
        rluser_likes = (RelativeLayout) findViewById(R.id.rlusereditor_likes);
        rluser_qianming = (RelativeLayout) findViewById(R.id.rlusereditor_qianming);
        rluser_homepage = (RelativeLayout) findViewById(R.id.rlusereditor_homepage);

        tvusereditor_name=(TextView) findViewById(R.id.tvusereditor_name);
        tvusereditor_board=(TextView) findViewById(R.id.tvusereditor_board);
        tvusereditorxuexing=(TextView) findViewById(R.id.tvusereditorxuexing);
        tvusereditorlikes=(TextView) findViewById(R.id.tvusereditorlikes);
        tvSignature=(TextView) findViewById(R.id.tvSignature);
        tvusereditorhomepage=(TextView) findViewById(R.id.tvusereditorhomepage);

    }

    @Override
    public void initWidgetState() {
        // TODO Auto-generated method stub
        mBtnListener = new BtnListener();
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
                Intent intent = new Intent(TrackrUsereditor.this, UserProfileActivity.class);
                startActivity(intent);
                TrackrUsereditor.this.finish();
            }
        });
        tvuser_usericon.setOnClickListener(mBtnListener);
        rlusereditor_icon.setOnClickListener(mBtnListener);
        rluser_smallname.setOnClickListener(mBtnListener);
        rluser_bord.setOnClickListener(mBtnListener);
        rluser_xuexing.setOnClickListener(mBtnListener);
        rluser_likes.setOnClickListener(mBtnListener);
        rluser_qianming.setOnClickListener(mBtnListener);
        rluser_homepage.setOnClickListener(mBtnListener);

    }
    /**
     * �������
     *
     */
    private final class BtnListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Dailog(v);
            switch (v.getId()) {
                case R.id.tvusereditor_icon:

                    break;
                case R.id.rlusereditor_smallname:
                case R.id.rlusereditor_borad:
                case R.id.rlusereditor_xuexing:
                case R.id.rlusereditor_likes:
                case R.id.rlusereditor_qianming:
                case R.id.rlusereditor_homepage:
                    Dailog(v);
                    break;
            }
        }

    }
    private void Dailog(View view){
        final View v = view;
        String title=null;
        final EditText inputServer = new EditText(TrackrUsereditor.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(TrackrUsereditor.this);
        switch (v.getId()) {
            case R.id.rlusereditor_smallname:
                title= getResources().getString(R.string.user_smallname);
                break;
            case R.id.rlusereditor_borad:
                title= getResources().getString(R.string.user_borddate);
                break;
            case R.id.rlusereditor_xuexing:
                title= getResources().getString(R.string.user_xuexing);
                break;
            case R.id.rlusereditor_likes:
                title= getResources().getString(R.string.user_liks);
                break;
            case R.id.rlusereditor_qianming:
                title= getResources().getString(R.string.user_editorself);
                break;
            case R.id.rlusereditor_homepage:
                title= getResources().getString(R.string.user_homepage);
                break;
        }
        builder.setTitle(title);
        builder.setView(inputServer);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String  Name = inputServer.getText().toString().trim();
                        switch (v.getId()) {
                            case R.id.rlusereditor_smallname:
                                tvusereditor_name.setText(getResources().getString(R.string.user_smallname)+Name);
                                break;
                            case R.id.rlusereditor_borad:
                                tvusereditor_board.setText(getResources().getString(R.string.user_borddate)+Name);
                                break;
                            case R.id.rlusereditor_xuexing:
                                tvusereditorxuexing.setText(getResources().getString(R.string.user_xuexing)+Name);
                                break;
                            case R.id.rlusereditor_likes:
                                tvusereditorlikes.setText(getResources().getString(R.string.user_liks)+Name);
                                break;
                            case R.id.rlusereditor_qianming:
                                tvSignature.setText(getResources().getString(R.string.user_editorself)+Name);
                                break;
                            case R.id.rlusereditor_homepage:
                                tvusereditorhomepage.setText(getResources().getString(R.string.user_homepage)+Name);
                                break;
                        }
                    }
                }
        );
        builder.show();

    }

}
