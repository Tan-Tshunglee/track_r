package com.antilost.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.network.SendPassowdCommand;
import com.antilost.app.util.Utils;

public class ForgetPasswordActivity extends Activity implements View.OnClickListener{

    private static final String LOG_TAG = "ForgetPasswordActivity";
    private EditText mEmailInput;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        mEmailInput = (EditText) findViewById(R.id.email_address);
        findViewById(R.id.sendBtn).setOnClickListener(this);
        findViewById(android.R.id.content).setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forget_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendBtn:
                trySendPasswordToMyEmail();
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

    private void trySendPasswordToMyEmail() {
        final String email = mEmailInput.getText().toString();
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Utils.makeText(this, getString(R.string.invalid_email_address), Toast.LENGTH_SHORT);
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
        mProgressDialog.setTitle(getString(R.string.sending_mail_which_contains_your_password));//设置标题
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
                final SendPassowdCommand command = new SendPassowdCommand(email);
                command.execTask();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(!mProgressDialog.isShowing()) {
                            Log.d(LOG_TAG, "user cancel send password");
                            return;
                        }
                        mProgressDialog.dismiss();
                        if(command.success()) {
                            Utils.makeText(ForgetPasswordActivity.this, getString(R.string.password_has_been_sent_to_your_email), Toast.LENGTH_LONG);
                            finish();
                        } else if(command.resultError()) {
                            Utils.makeText(ForgetPasswordActivity.this, getString(R.string.invalid_email_address), Toast.LENGTH_SHORT);
                        } else if(command.isNetworkError()){
                            Utils.makeText(ForgetPasswordActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT);
                        } else if(command.isStatusBad()) {
                            Utils.makeText(ForgetPasswordActivity.this, getString(R.string.network_status_error), Toast.LENGTH_SHORT);
                        } else {
                            Utils.makeText(ForgetPasswordActivity.this, getString(R.string.unknow_error), Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        };
        t.start();;
    }
}
