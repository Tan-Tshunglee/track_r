package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.dao.TrackRDataBase;
import com.antilost.app.dao.UserDataTable;
import com.antilost.app.model.UserdataBean;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;

public class UserProfileActivity extends Activity implements TrackRInitialize, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final int REQUEST_CODE_FOR_TIME = 1;

    private String TAG = "UserProfileActivity";
    private ImageButton imgBack;
    private ImageView imgUser_usericon;
    private TextView tvtitle,tvtime;
    private RelativeLayout rluser_editor, rluser_noticetimer, rluser_language,
            rluser_tipback, rluser_background, rluser_version, rluser_topback, rluser_safezone;
    private Button btmexit;
    private CheckBox cbAppring, cbSafeZone;
    private PrefsManager mPrefsManager;
    private TrackRDataBase trackRDataBase;
    /** 数据库对象 */
    private SQLiteDatabase mDb = null;
    public final String AlartTime = "AlartTime";

    private UserdataBean   curUserDataBean;
    private CheckBox mSleepModeSwitch;
    private TextView mSleepStartTime;
    private TextView mSleepEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdata);
        mPrefsManager = PrefsManager.singleInstance(this);
        initWidget();
        initWidgetState();
        initWidgetListener();
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
        trackRDataBase = new TrackRDataBase(this);
        mDb = trackRDataBase.getWritDatabase();
        //first to init
        if(UserDataTable.getInstance().countRecord(mDb)==0){
            Log.d(TAG,"userdatatable is empty here "+getString(R.string.user_smallname));
            curUserDataBean = new UserdataBean("", "3", getString(R.string.user_smallname), "3",getString(R.string.user_borddate),getString(R.string.user_xuexing),
                    getString(R.string.user_liks),getString(R.string.user_editorself),getString(R.string.user_homepage));
            UserDataTable.getInstance().insert(mDb,curUserDataBean);
        }else{

            curUserDataBean = UserDataTable.getInstance().query(mDb);
            //setting icon
            if(curUserDataBean.getMimage()!="3"){
                int targetWidth = 100;
                int targetHeight = 100;
                Bitmap targetBitmap = Bitmap.createBitmap(
                        targetWidth,
                        targetHeight,
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(targetBitmap);
                Path path = new Path();
                path.addCircle(
                        ((float)targetWidth - 1) / 2,
                        ((float)targetHeight - 1) / 2,
                        (Math.min(((float)targetWidth), ((float)targetHeight)) / 2),
                        Path.Direction.CCW);
                canvas.clipPath(path);
                Bitmap sourceBitmap = BitmapFactory.decodeFile(curUserDataBean.getMimage());

                if(sourceBitmap != null) {

                    canvas.drawBitmap(
                            sourceBitmap,
                            new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()),
                            new Rect(0, 0, targetWidth, targetHeight),
                            null);

                    imgUser_usericon.setImageBitmap(targetBitmap);
                }
            }
            tvtime.setText(curUserDataBean.getMalarmtime()+getResources().getString(R.string.alarttime_second));

            if(curUserDataBean==null){
                Log.d(TAG," curUserDataBean==null");
            }else{
                Log.d(TAG," curUserDataBean!=null");
            }

        }
    }

    @Override
    public void initWidget() {
        imgBack = (ImageButton) findViewById(R.id.mBtnCancel);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        rluser_topback = (RelativeLayout) findViewById(R.id.rluser_topback);
        imgUser_usericon = (ImageView) findViewById(R.id.tvusericon);
        rluser_editor = (RelativeLayout) findViewById(R.id.rluser_editor);
        rluser_noticetimer = (RelativeLayout) findViewById(R.id.rluser_notice);
        tvtime = (TextView) findViewById(R.id.tvtime);
        rluser_language = (RelativeLayout) findViewById(R.id.rluser_langauage);
        rluser_tipback = (RelativeLayout) findViewById(R.id.rluser_backtip);
        rluser_version = (RelativeLayout) findViewById(R.id.rluser_version);
        rluser_background = (RelativeLayout) findViewById(R.id.rluser_selectbackground);
        rluser_safezone = (RelativeLayout) findViewById(R.id.rluser_safezone);
        btmexit = (Button) findViewById(R.id.mbtnuserexit);
        cbAppring = (CheckBox) findViewById(R.id.cbuser_appringswitch);
        cbSafeZone = (CheckBox) findViewById(R.id.cbuser_safezoneswitch);

        mSleepModeSwitch = (CheckBox) findViewById(R.id.sleepModeSwitch);
        mSleepStartTime = (TextView) findViewById(R.id.startTimeText);
        mSleepEndTime = (TextView) findViewById(R.id.endTimeText);

    }

    @Override
    public void initWidgetState() {
        // TODO Auto-generated method stub
        cbSafeZone.setChecked(mPrefsManager.getSafeZoneEnable());
        cbAppring.setChecked(mPrefsManager.getAlertRingEnabled());
        mSleepModeSwitch.setChecked(mPrefsManager.getSleepMode());

        updateSleepModeTime();
    }

    private void updateSleepModeTime() {
        long startTime = mPrefsManager.getSleepTime(true);
        long endTime = mPrefsManager.getSleepTime(false);

        int startHour = (int) startTime / ( 1000 * 60 * 60 );
        int startMinute = (int) (endTime / (1000 * 60 )) % 60;


        int endHour = (int) endTime / ( 1000 * 60 * 60 );
        int endMinute = (int) (endTime / (1000 * 60)) % 60;
        String SstartHour =null;
        if(startHour<10){
            SstartHour = "0"+startHour;
        }else{
            SstartHour = Integer.toString(startHour);
        }


        String SstartMinute =null;
        if(startMinute<10){
            SstartMinute = "0"+startMinute;
        }else{
            SstartMinute = Integer.toString(startMinute);
        }

        String SendHour =null;
        if(endHour<10){
            SendHour = "0"+endHour;
        }else{
            SendHour = Integer.toString(endHour);
        }


        String SendMinute =null;
        if(endMinute<10){
            SendMinute = "0"+endMinute;
        }else{
            SendMinute = Integer.toString(endMinute);
        }


        mSleepStartTime.setText(SstartHour + ":" + SstartMinute);
        mSleepEndTime.setText(SendHour + ":" + SendMinute);
    }

    @Override
    public void initWidgetListener() {
        // TODO Auto-generated method stub
        rluser_editor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(UserProfileActivity.this, TrackrUsereditor.class);
                startActivity(intent);
				UserProfileActivity.this.finish();
            }
        });
        rluser_safezone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(UserProfileActivity.this, SafeZonewifiActivity.class);
                startActivity(intent);
				UserProfileActivity.this.finish();
            }
        });
        btmexit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                mPrefsManager.setUid(-1);
                Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                intent.putExtra("exitcounter", "1");
                startActivity(intent);
                UserProfileActivity.this.finish();

                startService(new Intent(UserProfileActivity.this, BluetoothLeService.class));
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
//                Intent intent = new Intent(UserProfileActivity.this, MainTrackRListActivity.class);
//                startActivity(intent);
                UserProfileActivity.this.finish();

            }
        });

        rluser_noticetimer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(UserProfileActivity.this, AlartTime.class);
                intent.putExtra(AlartTime,curUserDataBean.getMalarmtime());
                startActivity(intent);
                UserProfileActivity.this.finish();

            }
        });
        cbSafeZone.setOnCheckedChangeListener(this);
        cbAppring.setOnCheckedChangeListener(this);
        rluser_tipback.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(UserProfileActivity.this, FeedBackActivity.class);
                startActivity(intent);
                UserProfileActivity.this.finish();

            }
        });

        mSleepModeSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void addWidgetListener() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        switch (id) {
            case R.id.cbuser_safezoneswitch:
                mPrefsManager.setSafeZoneEnable(b);
                break;
            case R.id.cbuser_appringswitch:
                mPrefsManager.setAlertRingEnabled(b);
                break;
            case R.id.sleepModeSwitch:
                mPrefsManager.setSleepMode(b);
                break;
        }
    }


    public boolean onKeyDown(int keyCode,KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出 退出确认框
            Intent intent = new Intent(UserProfileActivity.this, MainTrackRListActivity.class);
            startActivity(intent);
            UserProfileActivity.this.finish();
            return true;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sleepModeLayout:

                startActivityForResult(new Intent(this, StartAndEndTimerPickerActivity.class), REQUEST_CODE_FOR_TIME);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FOR_TIME:
                if(resultCode == RESULT_OK) {
                    updateSleepModeTime();
                }
                break;

        }
    }
}

