package com.antilost.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.network.LoginCommand;
import com.antilost.app.prefs.PrefsManager;

import java.util.regex.Matcher;

public class LoginActivity extends Activity implements View.OnClickListener {

    public  static final String LOG_TAG = "LoginActivity";
    private Button mSignInBtn;
    private PrefsManager mPrefsManager;
    private EditText mEmailInput;
    private EditText mPasswordInput;
    private View mRememberPasswordTextView;
    private Button mForgetPassword;
    private Button mUserRegistration;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_support, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mPrefsManager = PrefsManager.singleInstance(this);
        if (mPrefsManager.alreadyLogin()) {
            Intent i = new Intent(this, MainTrackRListActivity.class);
            startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.activity_login);
        mSignInBtn = (Button) findViewById(R.id.signInBtn);
        mSignInBtn.setOnClickListener(this);

        mRememberPasswordTextView = findViewById(R.id.rememberPassword);
        mRememberPasswordTextView.setOnClickListener(this);
        mEmailInput = (EditText) findViewById(R.id.email_address);
        mPasswordInput = (EditText) findViewById(R.id.user_password);
        mForgetPassword = (Button) findViewById(R.id.forgetPassword);
        mForgetPassword.setOnClickListener(this);

        mUserRegistration = (Button) findViewById(R.id.userRegistrationBtn);
        mUserRegistration.setOnClickListener(this);

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
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.signInBtn:
                validateUserInputAndLogin();
                break;
            case R.id.rememberPassword:
                onUserClickRememberMe();
                break;
            case R.id.userRegistrationBtn:

                i = new Intent(this, RegistrationActivity.class);
                startActivity(i);
                break;

            case R.id.forgetPassword:
                i = new Intent(this, ForgetPasswordActivity.class);
                startActivity(i);
                break;

        }
    }

    private void onUserClickRememberMe() {
        mRememberPasswordTextView.setSelected(!mRememberPasswordTextView.isSelected());

    }

    private void validateUserInputAndLogin() {

        final String email = mEmailInput.getText().toString();
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(email);
        if(!matcher.matches()) {
            Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
            return;
        }
        final String password = mPasswordInput.getText().toString();

        if(password.length() < 6 || password.length() > 18) {
            Toast.makeText(this, "Password length of 6 to 18 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
        mProgressDialog.setTitle("Signing In");//设置标题
        mProgressDialog.setIndeterminate(false);//设置进度条是否为不明确
        mProgressDialog.setCancelable(true);//设置进度条是否可以按退回键取消
        mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        mProgressDialog.show();

        Thread t  = new Thread() {
            @Override
            public void run() {
                final LoginCommand command = new LoginCommand(email, password);
                command.execTask();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(!mProgressDialog.isShowing()) {
                            Log.d(LOG_TAG, "user cancel login");
                            return;
                        }
                        mProgressDialog.dismiss();
                        if(command.success()) {
                            mPrefsManager.setUid(command.getUid());
                            Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(LoginActivity.this, MainTrackRListActivity.class);
                            startActivity(i);
                            finish();
                        } else if(command.err()) {
                            Toast.makeText(LoginActivity.this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show();
                        } else if(command.isNetworkError()){
                            Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        } else if(command.isStatusBad()) {
                            Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Unkown Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        t.start();
    }
}
