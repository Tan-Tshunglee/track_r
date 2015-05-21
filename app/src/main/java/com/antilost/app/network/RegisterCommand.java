package com.antilost.app.network;

import android.util.Base64;
import android.util.Log;

/**
 * Created by Tan on 2015/1/22.
 */
public class RegisterCommand extends Command {

    private static final String LOG_TAG = "RegisterCommand";
    private final String mEmail;
    private final String mPassword;

    public RegisterCommand(String userName, String password) {
        mEmail = userName;
        mPassword = password;
    }


    @Override
    protected String makeRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd:reg").append(LINE_SPLITTER)
            .append("type:1").append(LINE_SPLITTER)
            .append("user:").append(mEmail).append(LINE_SPLITTER)
            .append("password:").append(Base64.encodeToString(mPassword.getBytes(), Base64.NO_WRAP)).append(LINE_SPLITTER);
        String result = sb.toString();
        Log.v(LOG_TAG, "request string is " + result);
        return result;
    }


    public int getUid() {
        if(mResultMap == null) {
            return -1;
        }

        String uidStr = mResultMap.get("uid");

        try {
            int uid = Integer.parseInt(uidStr);
            if(uid > 0) {
                return uid;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }


}
