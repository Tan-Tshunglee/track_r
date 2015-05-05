package com.antilost.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.network.ChangePasswordCommand;
import com.antilost.app.prefs.PrefsManager;


public class ChangePasswordActivity extends Activity {

    private static final String LOG_TAG = "ChangePasswordActivity";
    private PrefsManager mPrefs;
    private Handler mHandler = new Handler();
    private EditText mOldPassEdit;
    private EditText mNewPassEdit;
    private EditText mNewPassConfirmEdit;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        mPrefs = PrefsManager.singleInstance(this);
        mOldPassEdit = (EditText) findViewById(R.id.oldPassword);
        mNewPassEdit = (EditText) findViewById(R.id.newPassword);
        mNewPassConfirmEdit = (EditText) findViewById(R.id.oldPassword);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changePassButton:
                tryToChangePassword();
                break;
            case R.id.backBtn:
                finish();
                break;
        }
    }

    private void tryToChangePassword() {
        if(!mPrefs.validUserLog()) {
            Log.v(LOG_TAG, "User not log in can not change password");
            return;
        }

        String oldPass = mOldPassEdit.getText().toString();
        String newPass = mNewPassEdit.getText().toString();
        String newPassConfirm = mNewPassEdit.getText().toString();

        if (oldPass.length() < 6 || oldPass.length() > 18) {
            Toast toast = Toast.makeText(this, getString(R.string.old_password_length_error), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (newPass.length() < 6 || newPass.length() > 18) {
            Toast toast = Toast.makeText(this, getString(R.string.new_password_length_error), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if(!newPass.equals(newPassConfirm)) {
            Toast toast = Toast.makeText(this, "New password don't match the New password confirm.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        if(mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
            mProgressDialog.setTitle(R.string.new_user_registration);//设置标题
            mProgressDialog.setIndeterminate(false);//设置进度条是否为不明确
            mProgressDialog.setCancelable(true);//设置进度条是否可以按退回键取消
            mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }

            });
        }

        mProgressDialog.show();


        final ChangePasswordCommand command = new ChangePasswordCommand(mPrefs.getUid(), newPass, oldPass);
        Thread t = new Thread () {
            @Override
            public void run() {

                final String msg;
                try {

                    command.execTask();

                    mProgressDialog.dismiss();
                    if(command.success()) {
                       msg = getString(R.string.password_change_successfully);
                    } else {
                       msg = getString(R.string.failed_change_password);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChangePasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        };
        t.start();

    }
}
