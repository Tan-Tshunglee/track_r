package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.model.TrackR;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.LocUtils;

import java.io.IOException;

public class DisconnectAlertActivity extends Activity implements DialogInterface.OnClickListener {

    public static final String EXTRA_KEY_DEVICE_ADDRESS = "device_address";
    private static final String LOG_TAG = "DisconnectAlertActivity";

    private Dialog mAlertDialog;
    private PrefsManager mPrefsManager;
    private String mBluetoothAddress;
    private TrackR mTrackR;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private LayoutInflater mLayoutInflater;
    private Handler mHandler = new Handler();

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.v(LOG_TAG, "receive ACTION_GATT_SERVICES_DISCOVERED");
                String address = intent.getStringExtra(BluetoothLeService.EXTRA_KEY_BLUETOOTH_ADDRESS);
                if(mTrackR != null
                        && mTrackR.address.equals(address)) {
                    finish();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_disconnect_alert);
        mPrefsManager = PrefsManager.singleInstance(this);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        AssetManager assets = getAssets();
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(assets.openFd("alert.mp3").getFileDescriptor());
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.setVolume(1.0f, 1.0f);
        mLayoutInflater = getLayoutInflater();
        vibrateAndMakeALertSound();


    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if(mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        vibrateAndMakeALertSound();

    }

    private void vibrateAndMakeALertSound() {

        mBluetoothAddress = getIntent().getStringExtra(EXTRA_KEY_DEVICE_ADDRESS);
        mTrackR = mPrefsManager.getTrack(mBluetoothAddress);
        ensureDialog();
        mVibrator.vibrate(500);
        playAlertSound();
    }

    private void ensureDialog() {
        if(mAlertDialog == null) {
            AlertDialog.Builder  builder = new AlertDialog.Builder(this);
            String name = "";
            if(mTrackR != null) {
                name = mTrackR.name;
                if(TextUtils.isEmpty(name)) {
                    name = getResources().getStringArray(R.array.default_type_names)[mTrackR.type];
                }
            }

            View v = mLayoutInflater.inflate(R.layout.custom_dialog_layout, null);
            builder.setView(v);
            ImageView iv = (ImageView) v.findViewById(R.id.dialogIcon);
            TextView tv = (TextView) v.findViewById(R.id.dialogText);

            //builder.setMessage(getString(R.string.name_connected, name));
            tv.setText(getString(R.string.name_connected, name));
            builder.setNegativeButton(getString(R.string.i_known), this);
            builder.setPositiveButton(getString(R.string.find), this);
            builder.setCancelable(false);
            mAlertDialog = builder.create();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        switch (i) {
            case DialogInterface.BUTTON_NEGATIVE:
                finish();
                break;
            case DialogInterface.BUTTON_POSITIVE:
                Location loc = mPrefsManager.getLastLostLocation(mBluetoothAddress);
                if(loc != null) {
                    LocUtils.viewLocation(this, loc);
                }

                finish();
                break;
        }
    }


    private Runnable mStopRingRunnable = new Runnable() {
        @Override
        public void run() {
            if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                Log.v(LOG_TAG, "alert time run out ,stop  mediaplayer.");
                mMediaPlayer.pause();
            }
        }
    };

    private void playAlertSound() {

        boolean globalAlertEnabled = mPrefsManager.getGlobalAlertRingEnabled();
        boolean trackAlertEnabled = mPrefsManager.getPhoneAlert(mBluetoothAddress);

        if(globalAlertEnabled && trackAlertEnabled) {
            if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mHandler.removeCallbacks(mStopRingRunnable);
            }
            mMediaPlayer.start();
            Log.d(LOG_TAG, "playe alert sound...");
            int alertSecond = mPrefsManager.getAlertTime();
            mHandler.postDelayed(mStopRingRunnable, alertSecond * 1000);
        } else {
            Log.i(LOG_TAG, "alert sound turn off.");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAlertDialog.show();

        Log.v(LOG_TAG, "onResume called.");
        registerReceiver(mGattUpdateReceiver, new IntentFilter(BluetoothLeService.ACTION_GATT_CONNECTED));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mGattUpdateReceiver);
        super.onPause();

        if(mAlertDialog != null) {
            mAlertDialog.dismiss();
        }

        if(mMediaPlayer != null) {

            if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        Log.v(LOG_TAG, "onPause called.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mMediaPlayer.release();
            mMediaPlayer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
