package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.dao.TrackRDataBase;
import com.antilost.app.dao.UserDataTable;
import com.antilost.app.model.UserdataBean;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.wheelview.NumericWheelAdapter;
import com.antilost.app.util.wheelview.OnWheelChangedListener;
import com.antilost.app.util.wheelview.OnWheelScrollListener;
import com.antilost.app.util.wheelview.WheelView;

public class AlartTimeActivity extends Activity implements TrackRInitialize {
    private String TAG = "AlartTime";
    private Button btmBack, btmDone;
    private TextView tvtitle ;
    private TextView tvAlarttime;
    private WheelView wvAlarTime;
    private int  ialarttime = 0;
    //数据库
    private TrackRDataBase trackRDataBase;
    /** 数据库对象 */
    private SQLiteDatabase mDb = null;

    private UserdataBean   curUserDataBean;

    private boolean timeScrolled = false;
    private PrefsManager mPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.alarttime);
        mPrefsManager = PrefsManager.singleInstance(this);
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
        trackRDataBase = new TrackRDataBase(this);
        mDb = trackRDataBase.getWritDatabase();
        if(UserDataTable.getInstance().countRecord(mDb)!=0){
            curUserDataBean = UserDataTable.getInstance().query(mDb);
        }
        tvtitle.setText(getResources().getString(R.string.alarttime_title));
        wvAlarTime.setAdapter(new NumericWheelAdapter(0, 60));
        wvAlarTime.setLabel("  " +getResources().getString(R.string.alarttime_second));
        wvAlarTime.setVisibleItems(5);

        wvAlarTime.setCurrentItem(mPrefsManager.getAlertTime());


        OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!timeScrolled) {
                    ialarttime=  wvAlarTime.getCurrentItem();
                    tvAlarttime.setText(getResources().getString(R.string.alarttime_tip)+wvAlarTime.getCurrentItem() + getResources().getString(R.string.alarttime_second));
                }
            }
        };
        wvAlarTime.addChangingListener(wheelListener);
        OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                timeScrolled = true;
            }

            public void onScrollingFinished(WheelView wheel) {
                timeScrolled = false;
                ialarttime=  wvAlarTime.getCurrentItem();
                tvAlarttime.setText(getResources().getString(R.string.alarttime_tip)+wvAlarTime.getCurrentItem() + getResources().getString(R.string.alarttime_second));
                mPrefsManager.saveAlertTime(wvAlarTime.getCurrentItem());
            }
        };
        wvAlarTime.addScrollingListener(scrollListener);
    }

    @Override
    public void initWidget() {
        btmBack = (Button) findViewById(R.id.mBtnCancel);
        btmDone = (Button) findViewById(R.id.mBtnDone);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        wvAlarTime = (WheelView) findViewById(R.id.wvalartselect);
        tvAlarttime=(TextView) findViewById(R.id.tvalarttime_tip);
    }

    @Override
    public void initWidgetState() {
        btmBack.setText(R.string.cancel);
    }

    @Override
    public void initWidgetListener() {

    }

    @Override
    public void addWidgetListener() {
        btmBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlartTimeActivity.this.finish();
            }
        });
        btmDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(curUserDataBean!=null){
                    curUserDataBean.setMalarmtime(Integer.toString(ialarttime));
                    UserDataTable.getInstance().update(mDb, curUserDataBean);
                }
                AlartTimeActivity.this.finish();
            }
        });
    }


}
