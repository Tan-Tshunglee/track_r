package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.antilost.app.BuildConfig;
import com.antilost.app.R;
import com.antilost.app.network.LoginCommand;
import com.antilost.app.network.ResentActiveEmailCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.NetworkSyncService;
import com.antilost.app.util.Utils;

public class LoginActivity extends Activity implements View.OnClickListener, Dialog.OnClickListener {

    public static final String LOG_TAG = "LoginActivity";
    public static final int PROMPT_OPEN_NETWORK_ID = 1;
    public static final int FETCHING_TRACKS_DIALOG_ID = 2;
    public static final int ACTIVE_EMAIL_DIALOG_ID = 3;

    private Button mSignInBtn;
    private PrefsManager mPrefsManager;
    private EditText mEmailInput;
    private EditText mPasswordInput;
    private View mRememberPasswordTextView;
    private Button mForgetPassword;
    private Button mUserRegistration;
    private ProgressDialog mProgressDialog;
    private ConnectivityManager mConnectivityManager;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkSyncService.ACTION_TRACKS_FETCH_DONE.equals(intent.getAction())) {
                Log.i(LOG_TAG, "receiver ACTION_TRACKS_FETCH_DONE start main activity");
                startMainTrackRListActivity();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.

        if (!BuildConfig.DEBUG) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_support, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        Intent intent = getIntent();


        registerReceiver(mReceiver, new IntentFilter(NetworkSyncService.ACTION_TRACKS_FETCH_DONE));
        mPrefsManager = PrefsManager.singleInstance(this);
        if (mPrefsManager.validUserLog()) {
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
        findViewById(android.R.id.content).setOnClickListener(this);

        mUserRegistration = (Button) findViewById(R.id.userRegistrationBtn);
        mUserRegistration.setOnClickListener(this);

        boolean savePasswordChecked  = mPrefsManager.getSavePasswordChecked();
        if(savePasswordChecked) {
            mPasswordInput.setText(mPrefsManager.getPassword());
            mRememberPasswordTextView.setSelected(savePasswordChecked);
        }

        mEmailInput.setText(mPrefsManager.getEmail());
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
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
            case android.R.id.content:
                hideKeyBoard();
                break;

        }
    }

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEmailInput.getWindowToken(), 0);
    }
    private void onUserClickRememberMe() {
        mRememberPasswordTextView.setSelected(!mRememberPasswordTextView.isSelected());
    }

    private void validateUserInputAndLogin() {

        final String email = mEmailInput.getText().toString();
        if (!Utils.isValidEmailAddress(email)) {
            Utils.makeText(this, getString(R.string.invalid_email_address), Toast.LENGTH_SHORT);
            return;
        }
        final String password = mPasswordInput.getText().toString();

        if (password.length() < 6 || password.length() > 18) {
            Utils.makeText(this, getString(R.string.password_length_is_6_to_18_chars), Toast.LENGTH_SHORT);
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
        mProgressDialog.setTitle(getString(R.string.signing_in));//设置标题
        mProgressDialog.setIndeterminate(false);//设置进度条是否为不明确
        mProgressDialog.setCancelable(true);//设置进度条是否可以按退回键取消
        mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        mProgressDialog.show();

        updateRememberPassword(email, password);

        Thread t = new Thread() {
            @Override
            public void run() {
                final LoginCommand command = new LoginCommand(email, password);
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
                            mPrefsManager.saveUid(command.getUid());
                            Utils.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT);
                            startNetworkSyncService();
                            showDialog(FETCHING_TRACKS_DIALOG_ID);

                        } else if (command.unregisteredEmail()) {
                            Utils.makeText(LoginActivity.this, getString(R.string.unregister_email), Toast.LENGTH_SHORT);
                        } else if(command.invalidatePass()) {
                            Utils.makeText(LoginActivity.this, getString(R.string.wrong_pass), Toast.LENGTH_SHORT);
                        } else if(command.inActiveAccount()) {
//                            Utils.makeText(LoginActivity.this, getString(R.string.your_email_need_activation), Toast.LENGTH_LONG);
                            showDialog(ACTIVE_EMAIL_DIALOG_ID);
                        } else if (command.isNetworkError()) {
                            Utils.makeText(LoginActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT);
                        } else if (command.isStatusBad()) {
                            Utils.makeText(LoginActivity.this, getString(R.string.network_status_error), Toast.LENGTH_SHORT);
                        } else {
                            Utils.makeText(LoginActivity.this, getString(R.string.unknow_error), Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        };
        t.start();
    }

    private void startMainTrackRListActivity() {
        Intent i = new Intent(this, MainTrackRListActivity.class);
        startActivity(i);
        finish();
    }

    private void startNetworkSyncService() {
        Log.w(LOG_TAG, "startNetworkSyncService");
        Intent serviceIntent = new Intent(this, NetworkSyncService.class);
        serviceIntent.setAction(NetworkSyncService.ACTION_SYNC_AFTER_LOGIN);
        startService(serviceIntent);
    }

    private void updateRememberPassword(String email, String password) {
        mPrefsManager.setSavePasswordChecked(mRememberPasswordTextView.isSelected());
        mPrefsManager.saveEmail(email);
        mPrefsManager.savePassword(password);
    }

    @Override
    protected void onResume() {
        super.onResume();

        validateNetworkConnectivity();
    }

    private void validateNetworkConnectivity() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            showDialog(PROMPT_OPEN_NETWORK_ID);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == PROMPT_OPEN_NETWORK_ID) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warm_prompt);
            builder.setMessage(getString(R.string.enable_network_before_login));
            builder.setNegativeButton(R.string.cancel, this);
            builder.setPositiveButton(R.string.cancel, this);
            return builder.create();
        } else if (id == FETCHING_TRACKS_DIALOG_ID) {
            ProgressDialog.Builder builder = new ProgressDialog.Builder(this);
            builder.setTitle(R.string.just_a_moment);
            builder.setMessage(R.string.fetching_your_tracks);
            return builder.create();
        } else if(id == ACTIVE_EMAIL_DIALOG_ID) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warm_prompt);
            builder.setMessage(getString(R.string.your_email_need_activation));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dismissDialog(ACTIVE_EMAIL_DIALOG_ID);
                }
            });
            builder.setNegativeButton("Resent Active email", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    resentActiveEmail();
                }
            });
            return builder.create();
        }
        return null;
    }

    private void resentActiveEmail() {
        //TODO: finish api call;
        Thread t = new Thread() {
            @Override
            public void run() {
                ResentActiveEmailCommand command = new ResentActiveEmailCommand(mPrefsManager.getEmail());
                command.execTask();
                if(command.success()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.makeText(LoginActivity.this, getString(R.string.reset_active_email_success), Toast.LENGTH_SHORT);
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.makeText(LoginActivity.this, getString(R.string.resent_active_email_failed), Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        };
        t.start();
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    }
}
