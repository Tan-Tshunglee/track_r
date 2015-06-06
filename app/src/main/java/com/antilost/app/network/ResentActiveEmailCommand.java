package com.antilost.app.network;

import android.util.Base64;

/**
 * Created by tanch on 2015/6/6.
 */
public class ResentActiveEmailCommand extends Command {

    private final String mEmail;

    public ResentActiveEmailCommand(String email) {
        mEmail = email;
    }
    @Override
    protected String makeRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd:resentactive").append(LINE_SPLITTER)
                .append("user:").append(mEmail).append(LINE_SPLITTER);
        return sb.toString();
    }
}
