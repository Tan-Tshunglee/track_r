package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;

import com.antilost.app.R;
import com.antilost.app.model.TrackR;
import com.antilost.app.prefs.PrefsManager;

import java.io.IOException;
import java.util.Locale;

public class DisconnectAlertActivity extends Activity implements DialogInterface.OnClickListener, SoundPool.OnLoadCompleteListener {

    public static final String EXTRA_KEY_DEVICE_ADDRESS = "device_address";

    private Dialog mAlertDialog;
    private PrefsManager mPrefsManager;
    private String mAddress;
    private TrackR mTrackR;
    private Vibrator mVibrator;
    private SoundPool mSoundPool;
    private int mAlertSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnect_alert);
        mPrefsManager = PrefsManager.singleInstance(this);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        AssetManager assets = getAssets();
        try {
            AssetFileDescriptor fd = assets.openFd("alert.mp3");
            mSoundPool = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
            mAlertSoundId = mSoundPool.load(fd, 0);
            mSoundPool.setOnLoadCompleteListener(this);
        } catch (IOException e) {
            e.printStackTrace();
            mAlertSoundId = -1;
        }
        initAlertDialog();
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initAlertDialog();
        playAlertSound();
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
            builder.setTitle("Warning");
            String name = mTrackR.name;
            if(TextUtils.isEmpty(name)) {
                name = getResources().getStringArray(R.array.default_type_names)[mTrackR.type];
            }
            builder.setMessage("name\n disconnected");
            builder.setNegativeButton("I know", this);
            builder.setPositiveButton("Find", this);
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
                Location loc = mPrefsManager.getTrackLocMissed(mAddress);
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", loc.getLatitude(), loc.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i2) {
        playAlertSound();
    }

    private void playAlertSound() {
        mSoundPool.play(mAlertSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSoundPool.stop(mAlertSoundId);
    }
}
