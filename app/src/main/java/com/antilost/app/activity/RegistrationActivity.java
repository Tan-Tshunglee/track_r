package com.antilost.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.antilost.app.network.RegisterCommand;

import java.util.regex.Matcher;

public class RegistrationActivity extends Activity implements View.OnClickListener {

    private static final String LOG_TAG = "RegistrationActivity";
    private EditText mEmailInput;
    private EditText mPasswordInput;
    private EditText mPassowrdConfirmInput;
    private ProgressDialog mProgressDialog;
    private Button mBackLoginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.registrationBtn).setOnClickListener(this);
        findViewById(R.id.backLoginActivity).setOnClickListener(this);

        mEmailInput = (EditText) findViewById(R.id.email_address);
        mPasswordInput = (EditText) findViewById(R.id.user_password);
        mPassowrdConfirmInput =  (EditText) findViewById(R.id.user_password_confirm);
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
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(email);
        if(!matcher.matches()) {
            Toast.makeText(this, R.string.invalid_email_address, Toast.LENGTH_SHORT).show();
            return;
        }

        final String password = mPasswordInput.getText().toString();

        if(password.length() < 6 || password.length() > 18) {
//            Toast.makeText(this, R.string.password_length_is_6_to_18_chars, Toast.LENGTH_SHORT).show();
            mPassowrdConfirmInput.setText(R.string.password_length_is_6_to_18_chars);
            mPasswordInput.setText(R.string.password_length_is_6_to_18_chars);
            return;
        }

        String passwordConfirm = mPasswordInput.getText().toString();
        if(!password.equals(passwordConfirm)) {
            Toast.makeText(this, getString(R.string.the_two_passwords_do_not_match), Toast.LENGTH_SHORT).show();
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

        Thread t  = new Thread() {
            @Override
            public void run() {
                final RegisterCommand command = new RegisterCommand(email, password);
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
                            Toast.makeText(RegistrationActivity.this, getString(R.string.registration_success), Toast.LENGTH_LONG).show();
                            finish();
                        } else if(command.resultError()) {
                            Toast.makeText(RegistrationActivity.this, getString(R.string.email_has_been_used), Toast.LENGTH_LONG).show();
                            Intent i = new Intent(RegistrationActivity.this, ForgetPasswordActivity.class);
                            startActivity(i);
                            finish();
                        } else if(command.isNetworkError()) {
                            Toast.makeText(RegistrationActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                        } else if(command.isStatusBad()) {
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
}
