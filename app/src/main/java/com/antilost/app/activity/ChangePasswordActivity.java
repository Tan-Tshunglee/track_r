package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
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

    private static final int CHANGING_USER_PASSWORD_DIALOG_ID = 1;
    private static final int CHANGE_PASSWORD_SUCCESS_DIALOG_ID = 2;
    private static final int CHANGE_PASSWORD_FAIL_DIALOG_ID = 3;
    private PrefsManager mPrefs;
    private Handler mHandler = new Handler();
    private EditText mOldPassEdit;
    private EditText mNewPassEdit;
    private EditText mNewPassConfirmEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        mPrefs = PrefsManager.singleInstance(this);
        mOldPassEdit = (EditText) findViewById(R.id.oldPassword);
        mNewPassEdit = (EditText) findViewById(R.id.newPassword);
        mNewPassConfirmEdit = (EditText) findViewById(R.id.newPasswordConfirm);

        findViewById(R.id.backBtn);
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
        String newPassConfirm = mNewPassConfirmEdit.getText().toString();

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
            Toast toast = Toast.makeText(this, getString(R.string.change_password_not_match), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        showDialog(CHANGING_USER_PASSWORD_DIALOG_ID);


        final ChangePasswordCommand command = new ChangePasswordCommand(mPrefs.getUid(), newPass, oldPass);
        Thread t = new Thread () {
            @Override
            public void run() {

                try {
                    command.execTask();
                    final int dialog_id;
                    dismissDialog(CHANGING_USER_PASSWORD_DIALOG_ID);
                    if(command.success()) {
                        dialog_id  = CHANGE_PASSWORD_SUCCESS_DIALOG_ID;
                    } else {
                        dialog_id = CHANGE_PASSWORD_FAIL_DIALOG_ID;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialog(dialog_id);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case CHANGING_USER_PASSWORD_DIALOG_ID:
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
                progressDialog.setTitle(R.string.changing_password);//设置标题
                progressDialog.setIndeterminate(false);//设置进度条是否为不明确
                progressDialog.setCancelable(true);//设置进度条是否可以按退回键取消
                progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                });

                dialog = progressDialog;
                break;
            case CHANGE_PASSWORD_FAIL_DIALOG_ID:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.warm_prompt);
                builder.setMessage(R.string.failed_change_password);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       dialogInterface.dismiss();
                    }
                });
                dialog = builder.create();
                break;

            case CHANGE_PASSWORD_SUCCESS_DIALOG_ID:

                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.warm_prompt);
                builder.setMessage(R.string.password_change_successfully);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                dialog = builder.create();

                break;
        }

        return dialog;
    }
}
