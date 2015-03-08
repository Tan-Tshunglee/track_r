package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.antilost.app.R;
import com.antilost.app.model.TrackR;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.util.LocUtils;

import java.io.IOException;

public class DisconnectAlertActivity extends Activity implements DialogInterface.OnClickListener, SoundPool.OnLoadCompleteListener {

    public static final String EXTRA_KEY_DEVICE_ADDRESS = "device_address";

    private Dialog mAlertDialog;
    private PrefsManager mPrefsManager;
    private String mAddress;
    private TrackR mTrackR;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private LayoutInflater mLayoutInflater;
    private Handler mHandler = new Handler();

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.setVolume(1.0f, 1.0f);
        mLayoutInflater = getLayoutInflater();
        initAlertDialog();


    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initAlertDialog();

    }

    private void initAlertDialog() {

        mAddress = getIntent().getStringExtra(EXTRA_KEY_DEVICE_ADDRESS);
        mTrackR = mPrefsManager.getTrack(mAddress);
        ensureDialog();
        mAlertDialog.show();
        mVibrator.vibrate(500);


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
        switch (i) {
            case DialogInterface.BUTTON_NEGATIVE:
                finish();
                break;
            case DialogInterface.BUTTON_POSITIVE:
                Location loc = mPrefsManager.getLastLostLocation(mAddress);
                if(loc != null) {
                    LocUtils.viewLocation(this, loc);
                }

                finish();
                break;
        }
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i2) {
        playAlertSound();
    }

    private void playAlertSound() {
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        playAlertSound();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }, 60 * 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        mMediaPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}
