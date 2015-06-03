package com.antilost.app.network;

import android.util.Base64;
import android.util.Log;

/**
 * Created by Tan on 2015/3/28.
 */
public class UserFeedbackCommand extends Command {

    private final String mEmail;
    private final String mFeedback;

    public UserFeedbackCommand(String email, String feedback) {
        mEmail = email;
        mFeedback = feedback;
    }
    @Override
    protected String makeRequestString() {

        StringBuilder sb = new StringBuilder();
        byte[] utf8Stream = mFeedback.getBytes();
        String encodeFeedback =  Base64.encodeToString(utf8Stream, Base64.NO_WRAP);
        sb.append("cmd:feedback").append(LINE_SPLITTER)
          .append("uid:") .append(mEmail).append(LINE_SPLITTER)
          .append("feedback:").append(encodeFeedback).append(LINE_SPLITTER);
        String result = sb.toString();
        return result;
    }
}
