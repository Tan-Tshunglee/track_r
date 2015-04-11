package com.antilost.app.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Matrix;

import com.android.camera.Camera;
import com.antilost.app.R;
import com.antilost.app.TrackRApplication;
import com.antilost.app.model.TrackR;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.CsstSHImageData;
import com.antilost.app.util.CustomImageButton;
import com.antilost.app.util.LocUtils;

import java.util.HashMap;

public class TrackRActivity extends Activity implements View.OnClickListener {

    public static final String BLUETOOTH_ADDRESS_BUNDLE_KEY = "bluetooth_address_key";

    private static final String LOG_TAG = "TrackRActivity";
    private static final int TIMER_PERIOD_IN_MS = 20000;

    public static final int MSG_RESET_RING_STATE = 1;
    public static final int MSG_ENABLE_RING_BUTTON = 2;
    private static final int MSG_DELAY_INIT = 3;

    public static final int TIME_RINGING_STATE_KEEP = 10 * 1000;
    public static final int MAX_RSSI_LEVEL = -33;
    public static final int MIN_RSSI_LEVEL = -129;

    private String mBluetoothDeviceAddress;
    private CustomImageButton mTrackImage;
    private TextView mSleepTime;
    private TextView mConnection;
    private ImageView mDistanceImage;
    private ImageView mBatteryLeve;
    private ImageView mTrackRIcon;
    private Bitmap bmp;
    private float scaleWidth=1;
    private float scaleHeight=1;


    //ring state of every trackr;
    private HashMap<String, Boolean> mRingStateMap = new HashMap<String, Boolean>();


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESET_RING_STATE:
                    mRingStateMap.put(mBluetoothDeviceAddress, false);
                    mRingButton.setBackgroundResource(R.drawable.large_circle_btn_bkg);
                    break;
                case MSG_ENABLE_RING_BUTTON:
                    break;
                case MSG_DELAY_INIT:
                    updateRssi();
                    updateBatteryLevel();
                    break;
            }
        }
    };
    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            updateRssi();
            updateBatteryLevel();
            updateStateUi();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_BATTERY_LEVEL_READ.equals(action)) {
                int batteryLevel =mBluetoothLeService.getBatteryLevel(mBluetoothDeviceAddress);
                updateBatteryIcon(batteryLevel);
            } else if (BluetoothLeService.ACTION_RSSI_READ.equals(action)) {
                int rssi = mBluetoothLeService.getRssiLevel(mBluetoothDeviceAddress);
                //Toast.makeText(TrackRActivity.this, "Get rssi value is " + rssi, Toast.LENGTH_SHORT).show();
                updateIconPosition(rssi);
            } else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                restartMyself();
            } else if(BluetoothLeService.ACTION_DEVICE_RING_COMMAND_WRITE_DONE.equals(action)) {
                String address = intent.getStringExtra(BluetoothLeService.EXTRA_KEY_BLUETOOTH_ADDRESS);
                if(mBluetoothDeviceAddress.equals(address)) {
                    showTrackRinging();
                }
            }
            Log.v(LOG_TAG, "receive ACTION_GATT_CONNECTED");
        }
    };

    private void restartMyself() {
        Intent i = getIntent();
        finish();
        startActivity(i);
    }

    private PrefsManager mPrefsManager;
    private Button mRingButton;

    private void updateBatteryIcon(int level) {
        Log.i(LOG_TAG, "updateBatteryIcon " + level);
        int resId = R.drawable.battery_2;
        if(level < 25) {
            resId = R.drawable.battery_1;
        } else if(level < 50) {
            resId = R.drawable.battery_2;
        } else if(level < 75) {
            resId = R.drawable.battery_3;
        } else if(level <= 100) {
            resId = R.drawable.battery_4;
        }

        mBatteryLeve.setImageResource(resId);
    }


    private void updateIconPosition(int rssi) {
        if(rssi > MAX_RSSI_LEVEL || rssi < MIN_RSSI_LEVEL) {
            return;
        }

        int to_min = rssi - MIN_RSSI_LEVEL;
        float percentage = to_min / (float) (MAX_RSSI_LEVEL - MIN_RSSI_LEVEL);
        int top = mDistanceImage.getTop();
        int bottom = mDistanceImage.getBottom();

        int marginTop = (int) ((bottom - top) * percentage);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTrackRIcon.getLayoutParams();
        params.topMargin = marginTop;
        mTrackRIcon.setLayoutParams(params);

        Log.v(LOG_TAG, percentage + " margin top " + marginTop);

    }


    private void updateRssi() {
        if(mBluetoothLeService != null) {
            Log.d(LOG_TAG, "read rssi repeat.");
            updateIconPosition(mBluetoothLeService.getRssiLevel(mBluetoothDeviceAddress));
            mBluetoothLeService.startReadRssiRepeat(true, mBluetoothDeviceAddress);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);
        if(TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            finish();
            return;
        }

        mPrefsManager = PrefsManager.singleInstance(this);
        setContentView(R.layout.activity_track_r);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.btnSettings).setOnClickListener(this);
        findViewById(R.id.batteryStatus).setOnClickListener(this);
        findViewById(R.id.distanceLevel).setOnClickListener(this);

        findViewById(R.id.location).setOnClickListener(this);
        findViewById(R.id.ring).setOnClickListener(this);
        findViewById(R.id.photo).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);

//        findViewById(R.id.ring).setEnabled(false);

        mTrackImage = (CustomImageButton) findViewById(R.id.track_r_photo);
        mSleepTime = (TextView) findViewById(R.id.sleepModeAndTime);
        mConnection = (TextView) findViewById(R.id.connectState);
        mDistanceImage = (ImageView) findViewById(R.id.distanceLevel);
        mTrackRIcon = (ImageView) findViewById(R.id.trackIcon);
        mBatteryLeve = (ImageView) findViewById(R.id.batteryStatus);

        mRingButton = (Button) findViewById(R.id.ring);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        String customIconUri = CsstSHImageData.getIconImageString(mBluetoothDeviceAddress);

        if(customIconUri != null) {
            mTrackImage.setImageBitmap(CsstSHImageData.toRoundCorner(customIconUri));


//            mTrackImage.setImageURI(customIconUri);
        }else {

            TrackR track = mPrefsManager.getTrack(mBluetoothDeviceAddress);
            mTrackImage.setImageResource(TrackREditActivity.DrawableIds[track.type]);
            mTrackImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            bmp =BitmapFactory.decodeResource(getResources(),TrackREditActivity.DrawableIds[track.type]);
            mTrackImage.setImageBitmap(big());
        }


    }

    /* 图片放大的method */
    private Bitmap big() {
        int bmpWidth=bmp.getWidth();
        int bmpHeight=bmp.getHeight();

        Log.i(LOG_TAG, "bmpWidth = " + bmpWidth + ", bmpHeight = " + bmpHeight);

		/* 设置图片放大的比例 */
        double scale=3;
		/* 计算这次要放大的比例 */
        scaleWidth=(float)(scaleWidth*scale);
        scaleHeight=(float)(scaleHeight*scale);
		/* 产生reSize后的Bitmap对象 */
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizeBmp = Bitmap.createBitmap(bmp,0,0,bmpWidth,
                bmpHeight,matrix,true);
        return  resizeBmp;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mBluetoothDeviceAddress = intent.getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);
        if(TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            finish();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothLeService != null) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeBroadcastReceiverIntentFilter());
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_INIT, 5000);

        updateStateUi();

    }

    private void updateStateUi() {
        if(mBluetoothLeService == null) {
            mTrackRIcon.setImageResource(R.drawable.track_r_icon_red);
            mTrackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
        } else {
            if(mPrefsManager.isClosedTrack(mBluetoothDeviceAddress)) {
                mTrackImage.setText(getResources().getString(R.string.iTrack_close_tip));
                mTrackImage.setColor(getResources().getColor(R.color.whitesmoke));
                mTrackImage.setTextSize(32f);
                mTrackRIcon.setImageResource(R.drawable.track_r_icon_red);
                mConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_dot, 0, 0, 0);
                mConnection.setText(R.string.closed);
                mTrackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
            } else {
                if(mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    mTrackRIcon.setImageResource(R.drawable.track_r_icon_green);
                    mConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_dot, 0, 0, 0);
                    mConnection.setText(R.string.connected);
                    mTrackImage.setBackgroundResource(R.drawable.connected_icon_bkg);
                } else {
                    mTrackRIcon.setImageResource(R.drawable.track_r_icon_red);
                    mTrackImage.setBackgroundResource(R.drawable.disconnected_icon_bkg);
                    mConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_dot, 0, 0, 0);
                    mConnection.setText(R.string.disconnected);
                }
            }
        }

        if(mPrefsManager.getSleepMode()) {
            long startTime = mPrefsManager.getSleepTime(true);
            long endTime = mPrefsManager.getSleepTime(false);

            int startHour = (int) startTime / (1000 * 60 * 60);
            int endHour = (int) endTime / (1000 * 60 * 60);

            int startMinute = (int)(startTime / (1000 * 60)) % 60;
            int endMinute = (int)(endTime / (1000 * 60)) % 60;


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


            mSleepTime.setText(getString(R.string.sleep_mode_on_and_time_format, SstartHour + ":" + SstartMinute, SendHour + ":" + SendMinute));

        } else {
            mSleepTime.setText(R.string.sleep_mode_off);
        }
    }

    private IntentFilter makeBroadcastReceiverIntentFilter() {
        IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_RSSI_READ);
        filter.addAction(BluetoothLeService.ACTION_BATTERY_LEVEL_READ);
        filter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BluetoothLeService.ACTION_DEVICE_RING_COMMAND_WRITE_DONE);
        return filter;
    }

    private void updateBatteryLevel() {
        if(mBluetoothLeService != null) {
            Log.v(LOG_TAG, "updateBattery....");
            mBluetoothLeService.readBatteryLevel(mBluetoothDeviceAddress);
            int batteryLevel =mBluetoothLeService.getBatteryLevel(mBluetoothDeviceAddress);
            updateBatteryIcon(batteryLevel);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mGattUpdateReceiver);
        updateRssi();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.btnSettings:
                Intent i = new Intent(this, TrackRSettingActivity.class);
                i.putExtra(TrackRSettingActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothDeviceAddress);
                startActivity(i);
                TrackRActivity.this.finish();
                break;

            case R.id.ring:

                if(mBluetoothLeService == null) {
                    return;
                }
                if(!mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    return;
                }

                Boolean ringing = mRingStateMap.get(mBluetoothDeviceAddress);
                if(ringing == null) {
                    ringing = false;
                }
                if(ringing) {
                    Log.d(LOG_TAG, "trackr is ringing, ringing silent it");
//                    Drawable oldBackgroundDrawable = mRingButton.getBackground();
//                    if(oldBackgroundDrawable instanceof AnimationDrawable ) {
//                        AnimationDrawable animationDrawable = (AnimationDrawable) oldBackgroundDrawable;
//                        animationDrawable.stop();
//                    }

                    mRingButton.setBackgroundResource(R.drawable.large_circle_btn_bkg);
                    silentRing();
                    mRingStateMap.put(mBluetoothDeviceAddress, false);
                } else {
                    Log.d(LOG_TAG, "make trackr ring.");
                    if(makeTrackRRing()) {
                        //showTrackRinging();
                        Log.i(LOG_TAG, "Write track ring command ok");
                    } else {
                        Log.e(LOG_TAG, "Write track ring command failed...");
                    };
                }
                break;
            case R.id.location:

                if(mBluetoothLeService != null && mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress)) {
                    Location location = mBluetoothLeService.getLastLocation();
                    if(location != null) {
                        LocUtils.viewLocation(this, location);
                    } else {
                        Log.w(LOG_TAG, "no location found.");
                    }
                } else {

                    if(mPrefsManager.isDeclaredLost(mBluetoothDeviceAddress)
                            && mPrefsManager.isMissedTrack(mBluetoothDeviceAddress)) {
                        Location foundLoc = mPrefsManager.getLastLocFoundByOther(mBluetoothDeviceAddress);
                        if(foundLoc != null) {
                            LocUtils.viewLocation(this, foundLoc);
                            return;
                        }
                    }

                    Location location = mPrefsManager.getLastLostLocation(mBluetoothDeviceAddress);

                    if(location == null) {
                        location = mBluetoothLeService.getLastLocation();
                        if(location != null) {
                            LocUtils.viewLocation(this, location);
                        }
                    } else {
                        LocUtils.viewLocation(this, location);
                    }
                }

                break;

            case R.id.share:

                break;

            case R.id.photo:
                startActivity(new Intent(this, Camera.class));
                break;

            case R.id.batteryStatus:

                mBluetoothLeService.readBatteryLevel(mBluetoothDeviceAddress);
                break;
            case R.id.distanceLevel:
                updateRssi();
                break;
        }
    }

    private void showTrackRinging() {
        mRingButton.setBackgroundResource(R.drawable.ringing_btn_bkg);
        AnimationDrawable anim = (AnimationDrawable) mRingButton.getBackground();
        anim.start();
        mRingStateMap.put(mBluetoothDeviceAddress, true);
        mHandler.sendEmptyMessageDelayed(MSG_RESET_RING_STATE, TIME_RINGING_STATE_KEEP);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        TrackRApplication.onUserInteraction(this);
    }

    private boolean silentRing() {
        if(mBluetoothLeService != null) {
            mBluetoothLeService.silentRing(mBluetoothDeviceAddress);
        }
        return false;
    }

    private boolean makeTrackRRing() {
        if(mBluetoothLeService != null) {
            return mBluetoothLeService.ringTrackR(mBluetoothDeviceAddress);
        }
        return false;
    }

    private boolean isGattConnected() {
        if(mBluetoothLeService != null) {
            return mBluetoothLeService.isGattConnected(mBluetoothDeviceAddress);
        }
        return false;
    }
}
