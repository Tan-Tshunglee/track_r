package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.common.TrackRInitialize;
import com.antilost.app.dao.TrackRDataBase;
import com.antilost.app.dao.UserDataTable;
import com.antilost.app.model.UserdataBean;
import com.antilost.app.network.CommandPerformer;
import com.antilost.app.network.VersionCheckCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.service.UpdateService;
import com.antilost.app.util.CsstSHImageData;
import com.antilost.app.util.background.SkinSettingManager;

public class BackGroundSelector extends Activity implements TrackRInitialize,
        View.OnClickListener
     {
    public static final int REQUEST_CODE_FOR_TIME = 1;

    private static final int CHECKING_NEW_VERSION_DIALOG_ID = 1;
    private static final int PROMPT_USER_UPDATE_OR_NOT_DIALOG_ID = 2;
    private static final int NO_UPDATE_FOUND_DIALOG_ID = 3;

    public static final String ACTION_USER_LOGOUT = "com.antilost.app.ACTION_USER_LOGOUT";
    private static final String LOG_TAG = "BackGroundSelector";

    private String TAG = "BackGroundSelector";
    private TextView tvtitle;
    private Button btn_yellow,btn_gray,btn_login,btn_cloud,btn_silier,btn_black;
    private LinearLayout llbackgroudselector;
    private ImageButton imgBack;
    private PrefsManager mPrefsManager;
    /**
     * 数据库对象
     */
    private SQLiteDatabase mDb = null;

    public final static String SKIN_PREF = "skinSetting";
    public SharedPreferences skinSettingPreference;
    private int[] skinResources = { R.drawable.black,
            R.drawable.gray,R.drawable.silver,
            R.drawable.yellow
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backgroundselector);
        mPrefsManager = PrefsManager.singleInstance(this);
        BackGroundSelector.this.getWindow().setBackgroundDrawableResource(skinResources[2]);
        initWidget();
        initWidgetState();
        initWidgetListener();
        initDataSource();
        addWidgetListener();
        skinSettingPreference = this.getSharedPreferences(SKIN_PREF, 3);

    }

    public int getSkinType() {
        String key = "skin_type";
        return skinSettingPreference.getInt(key, 0);
    }
    public void setSkinType(int j) {
        SharedPreferences.Editor editor = skinSettingPreference.edit();
        String key = "skin_type";
        editor.putInt(key, j);
        editor.commit();
    }
    public int getCurrentSkinRes() {
        int skinLen = skinResources.length;
        int getSkinLen = getSkinType();
        if(getSkinLen >= skinLen){
            getSkinLen = 0;
        }
        return skinResources[getSkinLen];
    }


    @Override
    protected void onResume() {
        super.onResume();

   }
    @Override
    public void initDataSource() {


    }

    @Override
    public void initWidget() {
        imgBack = (ImageButton) findViewById(R.id.mBtnCancel);
        tvtitle = (TextView) findViewById(R.id.mTVTitle);
        btn_yellow =(Button) findViewById(R.id.bt_yellow);
        btn_gray =(Button) findViewById(R.id.bt_gray);
        llbackgroudselector =(LinearLayout) findViewById(R.id.llbackgroudselector);

    }

    @Override
    public void initWidgetState() {

    }


    @Override
    public void initWidgetListener() {
    }

    @Override
    public void addWidgetListener() {
        btn_yellow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "The button is bt_yellow");
                llbackgroudselector.setBackground(getResources().getDrawable(R.drawable.yellow));
//                setSkinType(1);
//                BackGroundSelector.this.getWindow().setBackgroundDrawable(null);
//                try {
//                    BackGroundSelector.this.getWindow().setBackgroundDrawableResource(getCurrentSkinRes());
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
            }
        });
        btn_gray.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "The button is bt_gray");
                llbackgroudselector.setBackground(getResources().getDrawable(R.drawable.gray));

//                setSkinType(2);
//                BackGroundSelector.this.getWindow().setBackgroundDrawable(null);
//                try {
//                    BackGroundSelector.this.getWindow().setBackgroundDrawableResource(getCurrentSkinRes());
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }

            }
        });


        imgBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "The button is bt_gray");
                BackGroundSelector.this.finish();
            }
        });
    }


    public boolean onKeyDown(int keyCode,KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出 退出确认框
            BackGroundSelector.this.finish();
            return true;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_yellow:
                Log.d(TAG,"The button is bt_yellow");
                llbackgroudselector.setBackground(getResources().getDrawable(R.drawable.yellow));
//                setSkinType(skinResources[1]);
//                this.getWindow().setBackgroundDrawable(null);
//                try {
//                    this.getWindow().setBackgroundDrawableResource(getCurrentSkinRes());
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
                break;
            case R.id.bt_gray:
                Log.d(TAG,"The button is bt_gray");
                llbackgroudselector.setBackground(getResources().getDrawable(R.drawable.gray));
//                setSkinType(skinResources[2]);
//                this.getWindow().setBackgroundDrawable(null);
//                try {
//                    this.getWindow().setBackgroundDrawableResource(getCurrentSkinRes());
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
                break;
        }
    }

}

