package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class UserProfileActivity extends Activity implements TrackRInitialize,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener,
        Dialog.OnClickListener {
    public static final int REQUEST_CODE_FOR_TIME = 1;

    private static final int CHECKING_NEW_VERSION_DIALOG_ID = 1;
    private static final int PROMPT_USER_UPDATE_OR_NOT_DIALOG_ID = 2;
    private static final int NO_UPDATE_FOUND_DIALOG_ID = 3;

    public static final String ACTION_USER_LOGOUT = "com.antilost.app.ACTION_USER_LOGOUT";
    private static final String LOG_TAG = "UserProfileActivity";

    private String TAG = "UserProfileActivity";
    private ImageButton imgBack;
    private ImageView imgUser_usericon;
    private TextView tvtitle, tvtime;
    private RelativeLayout rluser_editor, rluser_noticetimer, rluser_language,
            rluser_tipback, rluser_selectbackground, rluser_version, rluser_topback, rluser_safezone, rluser_help;
    private Button mButtonLogout;
    private CheckBox cbAppring, cbSafeZone;
    private PrefsManager mPrefsManager;
    private TrackRDataBase trackRDataBase;
    /**
     * 数据库对象
     */
    private SQLiteDatabase mDb = null;
    public final String AlartTime = "AlartTime";

    private UserdataBean curUserDataBean;
    private CheckBox mSleepModeSwitch;
    private TextView mSleepStartTime;
    private TextView mSleepEndTime;
    private VersionCheckCommand mCommand;
    private ProgressDialog mCheckingUpdatingDialog;
    private AlertDialog mPromtUpdateOrNotDialog;
    private AlertDialog mAppIsUpdatedDialog;
    private RelativeLayout mChangePasswordItem;


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
    protected void onResume() {
        super.onResume();
    //first to init
       if (UserDataTable.getInstance().countRecord(mDb) == 0) {
           Log.d(TAG, "userdatatable is empty here " + getString(R.string.user_smallname));
           curUserDataBean = new UserdataBean("", "3", getString(R.string.user_smallname), "3", getString(R.string.user_borddate), getString(R.string.user_xuexing),
                   getString(R.string.user_liks), getString(R.string.user_editorself), getString(R.string.user_homepage));
           UserDataTable.getInstance().insert(mDb, curUserDataBean);
       } else {

           curUserDataBean = UserDataTable.getInstance().query(mDb);
           //setting icon
           if (curUserDataBean.getMimage() != "3") {
               int targetWidth = 100;
               int targetHeight = 100;
               Bitmap targetBitmap = Bitmap.createBitmap(
                       targetWidth,
                       targetHeight,
                       Bitmap.Config.ARGB_8888);
               Canvas canvas = new Canvas(targetBitmap);
               Path path = new Path();
               path.addCircle(
                       ((float) targetWidth - 1) / 2,
                       ((float) targetHeight - 1) / 2,
                       (Math.min(((float) targetWidth), ((float) targetHeight)) / 2),
                       Path.Direction.CCW);
               canvas.clipPath(path);
               Bitmap sourceBitmap = BitmapFactory.decodeFile(curUserDataBean.getMimage());

               if (sourceBitmap != null) {

                   canvas.drawBitmap(
                           sourceBitmap,
                           new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()),
                           new Rect(0, 0, targetWidth, targetHeight),
                           null);

//                    imgUser_usericon.setImageBitmap(targetBitmap);
                   imgUser_usericon.setImageBitmap(CsstSHImageData.toRoundCorner(curUserDataBean.getMimage()));
               }
           }
           tvtime.setText(mPrefsManager.getAlertTime() + getResources().getString(R.string.alarttime_second));

           if (curUserDataBean == null) {
               Log.d(TAG, " curUserDataBean==null");
           } else {
               Log.d(TAG, " curUserDataBean!=null");
           }

       }
   }
    @Override
    public void initDataSource() {
        trackRDataBase = new TrackRDataBase(this);
        mDb = trackRDataBase.getWritDatabase();


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
//        rluser_language = (RelativeLayout) findViewById(R.id.rluser_langauage);
        rluser_tipback = (RelativeLayout) findViewById(R.id.rluser_backtip);
        rluser_version = (RelativeLayout) findViewById(R.id.rluser_version);
        rluser_safezone = (RelativeLayout) findViewById(R.id.rluser_safezone);

        rluser_selectbackground= (RelativeLayout) findViewById(R.id.rluser_selectbackground);

        rluser_help = (RelativeLayout) findViewById(R.id.rluser_helep);

        mButtonLogout = (Button) findViewById(R.id.mbtnuserexit);
        cbAppring = (CheckBox) findViewById(R.id.cbuser_appringswitch);
        cbSafeZone = (CheckBox) findViewById(R.id.cbuser_safezoneswitch);

        mSleepModeSwitch = (CheckBox) findViewById(R.id.sleepModeSwitch);
        mSleepStartTime = (TextView) findViewById(R.id.startTimeText);
        mSleepEndTime = (TextView) findViewById(R.id.endTimeText);

        mChangePasswordItem = (RelativeLayout) findViewById(R.id.changePassItem);
    }

    @Override
    public void initWidgetState() {
        cbSafeZone.setChecked(mPrefsManager.getSafeZoneEnable());
        cbAppring.setChecked(mPrefsManager.getGlobalAlertRingEnabled());
        mSleepModeSwitch.setChecked(mPrefsManager.getSleepMode());

        updateSleepModeTime();
    }

    private void updateSleepModeTime() {
        long startTime = mPrefsManager.getSleepTime(true);
        long endTime = mPrefsManager.getSleepTime(false);

        int startHour = (int) startTime / (1000 * 60 * 60);
        int startMinute = (int) (startTime / (1000 * 60)) % 60;


        int endHour = (int) endTime / (1000 * 60 * 60);
        int endMinute = (int) (endTime / (1000 * 60)) % 60;


        mSleepStartTime.setText(String.format("%02d:%02d", startHour, startMinute));
        mSleepEndTime.setText(String.format("%02d:%02d", endHour, endMinute));
    }

    @Override
    public void initWidgetListener() {

        rluser_help.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(UserProfileActivity.this, HelpActivity.class);
                startActivity(intent);

            }
        });

        rluser_selectbackground.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(UserProfileActivity.this, BackGroundSelector.class);
                startActivity(intent);

            }
        });

        rluser_editor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(UserProfileActivity.this, TrackrUsereditor.class);
                startActivity(intent);

            }
        });
        rluser_safezone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(UserProfileActivity.this, SafeZonewifiActivity.class);
                startActivity(intent);

            }
        });
        mButtonLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                confireNotice();

            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                UserProfileActivity.this.finish();
            }
        });

        rluser_noticetimer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(UserProfileActivity.this, AlartTimeActivity.class);
                intent.putExtra(AlartTime, curUserDataBean.getMalarmtime());
                startActivity(intent);


            }
        });
        cbSafeZone.setOnCheckedChangeListener(this);
        cbAppring.setOnCheckedChangeListener(this);
        rluser_tipback.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(UserProfileActivity.this, FeedBackActivity.class);
                startActivity(intent);

            }
        });

        mSleepModeSwitch.setOnCheckedChangeListener(this);
        rluser_version.setOnClickListener(this);
        mChangePasswordItem.setOnClickListener(this);
    }
    private  void confireNotice() {

        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(R.layout.tiplayout);
        TextView title = (TextView) window.findViewById(R.id.title_tip);
        TextView text = (TextView) window.findViewById(R.id.text_tip);
        ImageView icontrack = (ImageView) window.findViewById(R.id.tipicon);
        icontrack.setVisibility(View.GONE);
        text.setText(getResources().getString(R.string.sihnout_tip));
        title.setText(getResources().getString(R.string.sihnout_tip_title));
        Button ok = (Button) window.findViewById(R.id.tipbtn_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPrefsManager.logoutUser();
                mPrefsManager.setSavePasswordChecked(false);
                sendBroadcast(new Intent(ACTION_USER_LOGOUT));
                UserProfileActivity.this.finish();
                startService(new Intent(UserProfileActivity.this, BluetoothLeService.class));
                dlg.cancel();
            }

        });
        // 关闭alert对话框架
        Button cancel = (Button) window.findViewById(R.id.tipbtn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.cancel();
            }
        });
    }

    @Override
    public void addWidgetListener() {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        switch (id) {
            case R.id.cbuser_safezoneswitch:
                mPrefsManager.saveSafeZoneEnable(b);
                break;
            case R.id.cbuser_appringswitch:
                mPrefsManager.saveGlobalAlertRingEnabled(b);
                break;
            case R.id.sleepModeSwitch:
                mPrefsManager.saveSleepMode(b);
                break;
        }
    }


    public boolean onKeyDown(int keyCode,KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出 退出确认框
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
            case R.id.rluser_version:
                tryCheckUpdate();
                break;
            case R.id.changePassItem:
                showChangePasswordActivity();
                break;
        }
    }

    private void showChangePasswordActivity() {
        startActivity(new Intent(this, ChangePasswordActivity.class));
    }

    private void tryCheckUpdate() {
        if (VersionCheckCommand.isUpdating) {
            Log.w(LOG_TAG, "updation already start");
            return;
        }

        showDialog(CHECKING_NEW_VERSION_DIALOG_ID);
        try {
            mCommand = new VersionCheckCommand(getApplicationContext(), false);
            CommandPerformer t = new CommandPerformer(mCommand);
            t.setPostExective(new Handler(), new Runnable() {
                @Override
                public void run() {
                    if (mCheckingUpdatingDialog.isShowing()) {
                        mCheckingUpdatingDialog.dismiss();

                        if (!mCommand.success()) {
                            Log.e(LOG_TAG, "network request failed");
                            showUserNoUpdateFound();
                            return;
                        }
                        if (mCommand.hasNewVersion()) {
                            Log.v(LOG_TAG, "found new version");
                            showDialog(PROMPT_USER_UPDATE_OR_NOT_DIALOG_ID);
                        } else {
                            showUserNoUpdateFound();
                            Log.v(LOG_TAG, "no new version found");
                        }
                    }
                }
            });
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUserNoUpdateFound() {
        showDialog(NO_UPDATE_FOUND_DIALOG_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        switch (id) {
            case CHECKING_NEW_VERSION_DIALOG_ID:
                if (mCheckingUpdatingDialog != null) {
                    return mCheckingUpdatingDialog;
                }

                mCheckingUpdatingDialog = new ProgressDialog(this);
                mCheckingUpdatingDialog.setTitle(getString(R.string.wait_a_moment));
                mCheckingUpdatingDialog.setMessage(getString(R.string.checking_for_new_version));
                mCheckingUpdatingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                return mCheckingUpdatingDialog;
            case PROMPT_USER_UPDATE_OR_NOT_DIALOG_ID:
                if (mPromtUpdateOrNotDialog != null) {
                    return mPromtUpdateOrNotDialog;
                }

                builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.new_version_found));
                builder.setMessage(getString(R.string.new_version_has_been_released, mCommand.newVersionName()));
                builder.setNegativeButton(R.string.cancel, this);
                builder.setPositiveButton(R.string.ok, this);
                mPromtUpdateOrNotDialog = builder.create();

                return mPromtUpdateOrNotDialog;
            case NO_UPDATE_FOUND_DIALOG_ID:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.no_update_found));
                builder.setMessage(getString(R.string.your_app_is_updated));
                mAppIsUpdatedDialog = builder.create();
                return mAppIsUpdatedDialog;
        }
        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FOR_TIME:
                if (resultCode == RESULT_OK) {
                    updateSleepModeTime();
                }
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (dialogInterface == mPromtUpdateOrNotDialog) {
            switch (i) {
                case DialogInterface.BUTTON_NEGATIVE:
                    //do nothing
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    Intent updateIntent = new Intent(this, UpdateService.class);
                    updateIntent.putExtra(UpdateService.EXTRA_KEY_NEW_VERSION_URL, mCommand.newVersionUrl());
                    startService(updateIntent);
                    break;
            }
        }
    }
}

