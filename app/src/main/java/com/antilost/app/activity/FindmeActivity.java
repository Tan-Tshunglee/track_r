package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.antilost.app.R;

public class FindmeActivity extends Activity implements DialogInterface.OnClickListener {

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findme);
        ensureDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlertDialog.show();
    }

    private void ensureDialog() {
        if(mAlertDialog == null) {
            AlertDialog.Builder  builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.i_am_here));
            builder.setMessage(getString(R.string.name_connected));
            builder.setPositiveButton(getString(R.string.find), this);
            mAlertDialog = builder.create();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }


}
