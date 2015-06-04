package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.common.ICsstSHConstant;
import com.antilost.app.model.TrackR;
import com.antilost.app.network.Command;
import com.antilost.app.network.LostDeclareCommand;
import com.antilost.app.network.UnbindCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.CsstSHImageData;
import com.antilost.app.util.Utils;

public class TrackRSettingActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String BLUETOOTH_ADDRESS_BUNDLE_KEY = "bluetooth_address_key";

    private static final String LOG_TAG = "TrackRSettingActivity";
    private String mBluetoothDeviceAddress;

    private BluetoothLeService mBluetoothLeService;
    private Boolean mIsConnected = false;
    TrackR mTrack = null;
    //Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.v(LOG_TAG, "onServiceConnected...");
            updateStateUi();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private PrefsManager mPrefsManager;
    private CheckBox mTrackAlert;
    private Switch mSleepMode;
    private ImageView mTrackImage;
    private TextView mTrackName;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BluetoothLeService.EXTRA_KEY_BLUETOOTH_ADDRESS);
            if (BluetoothLeService.ACTION_DEVICE_CLOSED.equals(action)) {
                finish();
                return;
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)
                    || BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (mBluetoothDeviceAddress.equals(address)) {
                    updateDeclareLayoutVisibility();
                    updateStateUi();
                }
            } else if(BluetoothLeService.ACTION_DEVICE_HARDWARE_VERSION_READ.equals(action)) {
                if(mBluetoothDeviceAddress.equals(address)) {
                    byte[] versions = intent.getByteArrayExtra(BluetoothLeService.EXTRA_KEY_HARDWARE_VERSION);

                    if(versions != null) {
                        mTrackVersion.setVisibility(View.VISIBLE);
                        mTrackVersion.setText(
                                getString(
                                        R.string.track_hardware_name,
                                        String.valueOf(versions[0]),
                                        String.valueOf(versions[1])
                                )
                        );
                    } else {
                        mTrackVersion.setVisibility(View.GONE);
                    }

                }
            }
        }
    };


    private IntentFilter mBroadcastIntentFilter = new IntentFilter(BluetoothLeService.ACTION_DEVICE_CLOSED);
    private ConnectivityManager mConnectivityManager;
    private CheckBox mPhoneAlert;
    private View mDeclaredLost;
    private TextView mDeclaredLostText;
    private volatile Thread mBackgroundThread;
    private Button btnCloseItrack;
    //    private Bitmap bmp;
    private float scaleWidth = 1;
    private float scaleHeight = 1;
    private TextView mTrackVersion;
    private boolean mActivityPaused = false;


    private void updateDeclareLayoutVisibility() {
        if (mPrefsManager.isMissedTrack(mBluetoothDeviceAddress)) {
            mDeclaredLost.setVisibility(View.VISIBLE);
            updateDeclareText();
        } else {
            mDeclaredLost.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);

        mBroadcastIntentFilter = new IntentFilter(BluetoothLeService.ACTION_DEVICE_CLOSED);
        mBroadcastIntentFilter.addAction(BluetoothLeService.ACTION_DEVICE_UNBIND);
        mBroadcastIntentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mBroadcastIntentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        mBroadcastIntentFilter.addAction(BluetoothLeService.ACTION_DEVICE_HARDWARE_VERSION_READ);

        if (TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            Log.w(LOG_TAG, "get empty bluetooth address.");
            finish();
            return;
        }
        setContentView(R.layout.activity_track_rsetting);
        mTrackName = (TextView) findViewById(R.id.track_name);
        mTrackVersion = (TextView) findViewById(R.id.track_version);
        findViewById(R.id.backBtn).setOnClickListener(this);
        btnCloseItrack =(Button) findViewById(R.id.turnOffTrackR);
        findViewById(R.id.turnOffTrackR).setOnClickListener(this);
        findViewById(R.id.unbindTrackR).setOnClickListener(this);
        findViewById(R.id.declared_lost).setOnClickListener(this);
        findViewById(R.id.icon).setOnClickListener(this);
        findViewById(R.id.iconAndName).setOnClickListener(this);

        mPrefsManager = PrefsManager.singleInstance(this);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.v(LOG_TAG, "bindService...");

        mTrackAlert = (CheckBox) findViewById(R.id.itrack_alert_checkbox);
        mPhoneAlert = (CheckBox) findViewById(R.id.phone_alert_checkbox);

        mTrackAlert.setChecked(mPrefsManager.getTrackAlert(mBluetoothDeviceAddress));
        mTrackAlert.setOnCheckedChangeListener(this);

        mPhoneAlert.setChecked(mPrefsManager.getPhoneAlert(mBluetoothDeviceAddress));
        mPhoneAlert.setOnCheckedChangeListener(this);

        boolean sleepMode = mPrefsManager.getSleepMode();

        mSleepMode = (Switch) findViewById(R.id.sleepModeSwitch);
        mSleepMode.setChecked(sleepMode);
        mSleepMode.setOnCheckedChangeListener(this);

        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


        mTrackImage = (ImageView) findViewById(R.id.icon);

        mDeclaredLost = findViewById(R.id.declared_lost);
        mDeclaredLostText = (TextView) findViewById(R.id.declared_lost_text);
        updateDeclareLayoutVisibility();

    }

    private void updateDeclareText() {
        if (mPrefsManager.isDeclaredLost(mBluetoothDeviceAddress)) {
            mDeclaredLostText.setText(getString(R.string.revoke_statement));
            mDeclaredLost.setBackground(getResources().getDrawable(R.drawable.red_bkg));
        } else {
            mDeclaredLostText.setText(getString(R.string.declare_lost));
            mDeclaredLost.setBackground(getResources().getDrawable(R.drawable.blue_bkg));
        }
    }


    private void updateStateUi() {
        String customIconFilePath = CsstSHImageData.getIconImageString(mBluetoothDeviceAddress);
        mTrack = mPrefsManager.getTrack(mBluetoothDeviceAddress);

        if(mTrack == null) {
            finish();
            return;
        }
        mTrackName.setText(mTrack.name);

        if(Utils.NAME_NEED_VERSION.equals(mTrack.name)) {
            showTrackVersion();
        }
        if (customIconFilePath != null) {
            mTrackImage.setImageURI(null);
            float viewWidth = getResources().getDimensionPixelOffset(R.dimen.track_r_setting_icon_size)
                    - getResources().getDimensionPixelOffset(R.dimen.track_icon_padding) * 1.5f;
            float scaled  =  viewWidth / ICsstSHConstant.DEVICE_ICON_WIDTH;
            mTrackImage.setImageBitmap(Utils.scaleBitmap(customIconFilePath, scaled));
        } else {

            float viewWidth = getResources().getDimensionPixelOffset(R.dimen.track_r_setting_icon_size)
                    - getResources().getDimensionPixelOffset(R.dimen.track_icon_padding) * 2;

            Bitmap source = BitmapFactory.decodeResource(getResources(), TrackREditActivity.DrawableIds[mTrack.type]);
            float scaled  =  viewWidth / source.getWidth();
            mTrackImage.setImageBitmap(Utils.scaleBitmap(source, scaled));
        }

        if (mBluetoothLeService == null) {
            mTrackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
            mIsConnected = false;
//            失联之后就去掉close botton
            btnCloseItrack.setVisibility(View.GONE);
            Log.v(LOG_TAG, "mBluetoothLeService == null");
        } else {
            if (mPrefsManager.isClosedTrack(mBluetoothDeviceAddress)) {
                mTrackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                mIsConnected = false;
                Log.v(LOG_TAG, "isClosedTrack...");
            } else {
                if (mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    Log.v(LOG_TAG, "isGattConnected...");
                    //            失联之后就去掉close botton
                    btnCloseItrack.setVisibility(View.VISIBLE);
                    mIsConnected = true;
                    mTrackImage.setBackgroundResource(R.drawable.connected_icon_bkg);
                } else {
                    Log.v(LOG_TAG, "disconnected...");
                    mIsConnected = false;
                    mTrackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                    //            失联之后就去掉close botton
                    btnCloseItrack.setVisibility(View.GONE);
                }
            }
        }
    }

    private void showTrackVersion() {
        if(mBluetoothLeService != null) {
            mBluetoothLeService.requestTrackHardwareVersion(mBluetoothDeviceAddress);
        }
    }


    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mBroadcastIntentFilter);

        if(mActivityPaused) {
            mActivityPaused = false;
            updateStateUi();
        }


    }


    @Override
    protected void onPause() {
        mActivityPaused = true;
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    @Override
    public void onUserInteraction() {
        super.onUserInteraction();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backBtn:
                Intent i = new Intent(this, TrackRActivity.class);
                i.putExtra(TrackRSettingActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothDeviceAddress);
                startActivity(i);
                TrackRSettingActivity.this.finish();
                break;
            case R.id.iconAndName:
            case R.id.icon:
                i = new Intent(TrackRSettingActivity.this, TrackREditActivity.class);
                i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothDeviceAddress);
                startActivity(i);
                break;
            case R.id.turnOffTrackR:
                if (mBluetoothLeService == null) {
                    Log.w(LOG_TAG, "mBluetoothLeService is null");
                    Toast.makeText(TrackRSettingActivity.this, getString(R.string.can_not_close_disconnected_itrack), Toast.LENGTH_SHORT).show();
                    return;
                }
                confireNotice(view);
                break;
            case R.id.unbindTrackR:
                if (mBluetoothLeService == null) {
                    Log.w(LOG_TAG, "mBluetoothLeService is null");
                    return;
                }

                if (mBackgroundThread != null) {
                    return;
                }
                confireNotice(view);
                break;
            case R.id.declared_lost:
                if (mBackgroundThread != null) {
                    return;
                }
                confireNotice(view);
                break;
        }
    }


    /**
     * 修改名字
     */
    private void confireNotice(final View view) {

        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(R.layout.tiplayout);
        TextView title = (TextView) window.findViewById(R.id.title_tip);
        TextView text = (TextView) window.findViewById(R.id.text_tip);
        ImageView iconTrack = (ImageView) window.findViewById(R.id.tipicon);
        final Button tipbtn_ok = (Button) window.findViewById(R.id.tipbtn_ok);

        String customIconFilePath = CsstSHImageData.getIconImageString(mBluetoothDeviceAddress);

        if (customIconFilePath != null) {
            iconTrack.setImageURI(null);
            float viewWidth = getResources().getDimensionPixelOffset(R.dimen.track_r_photo_size)
                    - getResources().getDimensionPixelOffset(R.dimen.track_icon_padding) * 2;
            float scaled  =  viewWidth / ICsstSHConstant.DEVICE_ICON_WIDTH;
            iconTrack.setImageBitmap(Utils.scaleBitmap(customIconFilePath, scaled));
        } else {
            float viewWidth = getResources().getDimensionPixelOffset(R.dimen.track_r_photo_size)
                    - getResources().getDimensionPixelOffset(R.dimen.track_icon_padding) * 4;

            Bitmap source = BitmapFactory.decodeResource(getResources(), TrackREditActivity.DrawableIds[mTrack.type]);
            float scaled  =  (viewWidth / source.getWidth())  ;
            iconTrack.setImageBitmap(Utils.scaleBitmap(source, scaled));
        }



        if (mBluetoothLeService == null) {
            iconTrack.setBackgroundResource(R.drawable.disconnected_icon_bkg);
            Log.v(LOG_TAG, "mBluetoothLeService == null");
        } else {
            if (mPrefsManager.isClosedTrack(mBluetoothDeviceAddress)) {
                iconTrack.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                Log.v(LOG_TAG, "isClosedTrack...");
            } else {
                if (mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    Log.v(LOG_TAG, "isGattConnected...");
                    iconTrack.setBackgroundResource(R.drawable.connected_icon_bkg);
                } else {
                    Log.v(LOG_TAG, "disconnected...");
                    iconTrack.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                }
            }
        }

        switch (view.getId()) {
            case R.id.turnOffTrackR:
                text.setText(getResources().getString(R.string.notice_close_loser_tip_1) + mTrack.name + getResources().getString(R.string.notice_close_loser_tip_2));
                title.setText(getResources().getString(R.string.turn_off_track_r));
                break;
            case R.id.unbindTrackR:
                title.setText(getResources().getString(R.string.unbind_track_r));
                if (mIsConnected) {
                    text.setText(getResources().getString(R.string.notice_delete_loser_tip_1) + mTrack.name + getResources().getString(R.string.notice_delete_loser_tip_2));
                } else {
                    text.setText(getResources().getString(R.string.unbined_tip_disconnected_tip1) + mTrack.name + getResources().getString(R.string.unbined_tip_disconnected_tip2));
                }

                break;
            case R.id.declared_lost:
                text.setText(getResources().getString(R.string.notice_declare_loser_tip_1) + mTrack.name + getResources().getString(R.string.notice_declare_loser_tip_2));
                title.setText(getResources().getString(R.string.declare_lost));
                break;
        }
        tipbtn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (view.getId()) {
                    case R.id.turnOffTrackR:
                        mBluetoothLeService.turnOffTrackR(mBluetoothDeviceAddress);
                        dlg.cancel();
                        break;
                    case R.id.unbindTrackR:
                        mBluetoothLeService.unbindTrackR(mBluetoothDeviceAddress);
                        toast(getString(R.string.unbind_successfully));
                        mBackgroundThread = new Thread() {
                            @Override
                            public void run() {
                                UnbindCommand command = new UnbindCommand(mPrefsManager.getUid(), mBluetoothDeviceAddress);
                                command.setPassword(mPrefsManager.getPassword());
                                command.execTask();
                                mBackgroundThread = null;
                            }
                        };
                        mBackgroundThread.start();
                        dlg.cancel();

                        break;
                    case R.id.declared_lost:
                        mBackgroundThread = new Thread() {
                            @Override
                            public void run() {
                                int declareTobe = mPrefsManager.isDeclaredLost(mBluetoothDeviceAddress) ? 0 : 1;
                                Command declareCommand = new LostDeclareCommand(mPrefsManager.getUid(), mBluetoothDeviceAddress, declareTobe);
                                try {
                                    declareCommand.setPassword(mPrefsManager.getPassword());
                                    declareCommand.execTask();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (declareCommand.success()) {
                                    mPrefsManager.saveDeclareLost(mBluetoothDeviceAddress, declareTobe == 0 ? false : true);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(TrackRSettingActivity.this, getString(R.string.declare_success), Toast.LENGTH_SHORT).show();
                                            updateDeclareText();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(TrackRSettingActivity.this, getString(R.string.declaration_failed), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                mBackgroundThread = null;
                            }
                        };
                        mBackgroundThread.start();
                        dlg.cancel();
                        break;
                }

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
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.itrack_alert_checkbox:
                if(mBluetoothLeService.inSleepTime()) {
                    Toast.makeText(this, getString(R.string.in_sleep_mode_track_will_not_alert), Toast.LENGTH_SHORT).show();
                }
                mPrefsManager.saveTrackAlert(mBluetoothDeviceAddress, b);
                mBluetoothLeService.setTrackAlertMode(mBluetoothDeviceAddress, b);
                break;

            case R.id.phone_alert_checkbox:
                if(mBluetoothLeService.inSleepTime()) {
                    Toast.makeText(this, getString(R.string.in_sleep_mode_phone_no_alert), Toast.LENGTH_SHORT).show();
                }
                mPrefsManager.savePhoneAlert(mBluetoothDeviceAddress, b);
                break;

            case R.id.sleepModeSwitch:
                mPrefsManager.saveSleepMode(b);
                break;
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 弹出 退出确认框

            Intent i = new Intent(this, TrackRActivity.class);
            i.putExtra(TrackRSettingActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothDeviceAddress);
            startActivity(i);
            TrackRSettingActivity.this.finish();
            return true;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                ProgressDialog.Builder builder = new ProgressDialog.Builder(this);
                builder.setTitle(R.string.wait_a_moment);
                builder.setMessage(getString(R.string.delete_track_on_server));
                return builder.create();
        }
        return null;
    }
}
