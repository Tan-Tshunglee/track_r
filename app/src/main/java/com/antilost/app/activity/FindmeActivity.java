package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.antilost.app.R;

public class FindmeActivity extends Activity implements DialogInterface.OnClickListener {

    private AlertDialog mAlertDialog;
    private LayoutInflater mLayoutInflater;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findme);
        mLayoutInflater = getLayoutInflater();
        mHandler = new Handler();
        ensureDialog();

        //make sound when system is in silent mode;
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (ringtone != null) {
            ringtone.setStreamType(AudioManager.STREAM_ALARM);
            ringtone.play();
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //make sound when system is in silent mode;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (ringtone != null) {
            ringtone.setStreamType(AudioManager.STREAM_ALARM);
            ringtone.play();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlertDialog.show();

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

    private void ensureDialog() {
        if(mAlertDialog == null) {
            AlertDialog.Builder  builder = new AlertDialog.Builder(this);
//            builder.setTitle(getString(R.string.i_am_here));
            View v = mLayoutInflater.inflate(R.layout.custom_dialog_layout, null);
            builder.setView(v);
            ImageView iv = (ImageView) v.findViewById(R.id.dialogIcon);
            TextView tv = (TextView) v.findViewById(R.id.dialogText);

            iv.setImageResource(R.drawable.info_icon);
            tv.setText(getString(R.string.i_am_here_find_me));
            builder.setPositiveButton(getString(R.string.i_known), this);
            mAlertDialog = builder.create();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }


}
