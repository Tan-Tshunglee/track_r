package com.antilost.app.network;

import android.util.Log;

/**
 * Created by Tan on 2015/1/24.
 */
public class SendPassowdCommand extends Command {

    private static final String LOG_TAG = "SendPassowdCommand";
    private final String mEmail;

    public SendPassowdCommand(String email) {
        mEmail = email;
    }
    @Override
    protected String makeRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd:getpasswd").append(LINE_SPLITTER)
                .append("uid:") .append(mEmail).append(LINE_SPLITTER);
        String result = sb.toString();
        Log.v(LOG_TAG, "request string is " + result);
        return result;

    }
}
