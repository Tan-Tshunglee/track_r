package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.network.RegisterCommand;
import com.antilost.app.util.Utils;

import java.util.regex.Matcher;

public class RegistrationActivity extends Activity implements View.OnClickListener, DialogInterface.OnClickListener {

    private static final String LOG_TAG = "RegistrationActivity";
    public static final int PROMPT_OPEN_NETWORK_ID = 1;
    public static final int ACTIVE_EMAIL_DIALOG = 2;
    public static final int EMAIL_REGISTERED_DIALOG = 3;
    private EditText mEmailInput;
    private EditText mPasswordInput;
    private EditText mPassowrdConfirmInput;
    private ProgressDialog mProgressDialog;
    private Button mBackLoginActivity;
    private ConnectivityManager mConnectivityManager;
    private AlertDialog mOpenNetworkDialog;
    private AlertDialog mActiveEmailDialog;
    private AlertDialog mEmailRegisteredDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.registrationBtn).setOnClickListener(this);
        findViewById(R.id.backLoginActivity).setOnClickListener(this);

        mEmailInput = (EditText) findViewById(R.id.email_address);
        mPasswordInput = (EditText) findViewById(R.id.user_password);
        mPassowrdConfirmInput = (EditText) findViewById(R.id.user_password_confirm);

        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwokAvailablity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.registrationBtn:
                tryRegistrationUser();
                break;
            case R.id.backLoginActivity:
                finish();
                break;
        }
    }

    private void tryRegistrationUser() {


        final String email = mEmailInput.getText().toString();
        if (!Utils.isValidEmailAddress(email)) {
            Toast toast = Toast.makeText(this, R.string.invalid_email_address, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        final String password = mPasswordInput.getText().toString();

        if (password.length() < 6 || password.length() > 18) {
//            Toast.makeText(this, R.string.password_length_is_6_to_18_chars, Toast.LENGTH_SHORT).show();
//            mPassowrdConfirmInput.setText(R.string.password_length_is_6_to_18_chars);
//            mPasswordInput.setText(R.string.password_length_is_6_to_18_chars);

            Toast toast = Toast.makeText(this, R.string.password_length_is_6_to_18_chars, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        String passwordConfirm = mPassowrdConfirmInput.getText().toString();
        if(!password.equals(passwordConfirm)) {
//            Toast.makeText(this, getString(R.string.the_two_passwords_do_not_match), Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(this, R.string.the_two_passwords_do_not_match, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
//
//            LinearLayout toastView = (LinearLayout) toast.getView();
//            ImageView imageCodeProject = new ImageView(getApplicationContext());
//            imageCodeProject.setImageResource(R.drawable.icon);
//            toastView.addView(imageCodeProject, 0);
            return;

        }

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
        mProgressDialog.show();

        Thread t = new Thread() {
            @Override
            public void run() {
                final RegisterCommand command = new RegisterCommand(email, password);
                command.execTask();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!mProgressDialog.isShowing()) {
                            Log.d(LOG_TAG, "user cancel login");
                            return;
                        }

                        mProgressDialog.dismiss();
                        if (command.success()) {
                            showDialog(ACTIVE_EMAIL_DIALOG);
                        } else if (command.resultError()) {
                            showDialog(EMAIL_REGISTERED_DIALOG);
                        } else if (command.isNetworkError()) {
                            Toast.makeText(RegistrationActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                        } else if (command.isStatusBad()) {
                            Toast.makeText(RegistrationActivity.this, getString(R.string.registration_error), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistrationActivity.this, R.string.unknow_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        t.start();
    }

    private void checkNetwokAvailablity() {

        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            showDialog(PROMPT_OPEN_NETWORK_ID);
            ;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == PROMPT_OPEN_NETWORK_ID) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warm_prompt);
            builder.setMessage(R.string.sign_up_need_network);
            builder.setNegativeButton(R.string.cancel, this);
            builder.setPositiveButton(R.string.ok, this);
            mOpenNetworkDialog = builder.create();
            return mOpenNetworkDialog;
        } else if (id == ACTIVE_EMAIL_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warm_prompt);
            builder.setMessage(R.string.registration_success);
            builder.setPositiveButton(R.string.ok, this);
            mActiveEmailDialog = builder.create();
            return mActiveEmailDialog;
        } else if (id == EMAIL_REGISTERED_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warm_prompt);
            builder.setMessage(R.string.email_has_been_used);
            builder.setPositiveButton(R.string.ok, this);
            mEmailRegisteredDialog = builder.create();
            return mEmailRegisteredDialog;
        }
        return null;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (dialogInterface == mOpenNetworkDialog) {
            switch (i) {
                case DialogInterface.BUTTON_POSITIVE:
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        } else if (dialogInterface == mActiveEmailDialog) {
            switch (i) {
                case DialogInterface.BUTTON_POSITIVE:
                    finish();
                    break;
            }
        } else if (dialogInterface == mEmailRegisteredDialog) {
            Intent intent = new Intent(RegistrationActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
