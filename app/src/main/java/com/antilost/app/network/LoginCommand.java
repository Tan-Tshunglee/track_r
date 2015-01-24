package com.antilost.app.network;

import android.util.Log;

/**
 * Created by Tan on 2015/1/22.
 */
public class LoginCommand extends Command {

    private static final String LOG_TAG = "LoginCommand";
    private final String mEmail;
    private final String mPassword;

    public LoginCommand(String email, String password) {
        mEmail = email;
        mPassword = password;
    }

    public int getUid() {
        String uidStr = mResultMap.get("uid");
        return mResultMap.get("uid") == null ? -1 : Integer.parseInt(uidStr);
    }

    @Override
    protected String makeRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd:login").append(LINE_SPLITTER)
                .append("type:1").append(LINE_SPLITTER)
                .append("user:").append(mEmail).append(LINE_SPLITTER)
                .append("password:").append(mPassword).append(LINE_SPLITTER);
        String result = sb.toString();
        Log.v(LOG_TAG, "request string is " + result);
        return result;
    }
}
